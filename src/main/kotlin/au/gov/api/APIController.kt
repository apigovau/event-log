
package au.gov.api

import au.gov.api.config.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import java.util.*
import khttp.get
import khttp.structures.authorization.BasicAuthorization
import java.text.SimpleDateFormat
import java.text.DateFormat



@RestController
class APIController {

    @Autowired
    private lateinit var manager: EventManager

    @Autowired
    private lateinit var environment: Environment

    private fun isAuthorisedToSaveService(request:HttpServletRequest, space:String):Boolean{
        if(environment.getActiveProfiles().contains("prod")){
            val AuthURI = Config.get("AuthURI")

            // http://www.baeldung.com/get-user-in-spring-security
            val raw = request.getHeader("authorization")
            if (raw==null) return false;
            val apikey = String(Base64.getDecoder().decode(raw.removePrefix("Basic ")))

            val user = apikey.split(":")[0]
            val pass= apikey.split(":")[1]


            val authorisationRequest = get(AuthURI + "api/canWrite",
                    params=mapOf("space" to space),
                    auth=BasicAuthorization(user, pass)
            )
            if(authorisationRequest.statusCode != 200) return false
            return authorisationRequest.text == "true"
        }
        return true
    }

    @PostMapping("/api/new")
    fun newEvent(request:HttpServletRequest, @RequestBody event:Event):String{
        if(isAuthorisedToSaveService(request, "api-gov-event-log")){
            if (event.timestamp =="") event.timestamp = Date(System.currentTimeMillis()).toString()
            return manager.newEvent(event)
        }
        throw UnauthorisedToCreateEvent()
    }

    @GetMapping("/api/list")
    fun getEvents(request:HttpServletRequest,
                  @RequestParam(required = false, defaultValue="") startDate:String,
                  @RequestParam(required = false, defaultValue="") endDate:String,
                  @RequestParam name:String,
                  @RequestParam(required = false, defaultValue="1") limit:Int):List<Event>{

        val formatter = SimpleDateFormat("MM/dd/yyyy")

        var sd = startDate
        var ed = endDate
        var nm = name
        var lim = limit
        if (startDate=="") sd = "12/12/2018"
        if(endDate=="")
        {
            val dt = System.currentTimeMillis() + 86400000
            val dt1 = Date(dt)

            ed = formatter.format(dt1)
        }

        if (limit >10) lim=10
        if(isAuthorisedToSaveService(request, "api-gov-event-log")){
            return manager.getEvents(sd,ed,nm,lim).toList()
        }
        throw UnauthorisedToViewEvents()
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    class UnauthorisedToCreateEvent() : RuntimeException()
    class UnauthorisedToViewEvents() : RuntimeException()


}
