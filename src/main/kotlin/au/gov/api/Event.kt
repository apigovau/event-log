package au.gov.api

import com.beust.klaxon.Klaxon
import java.util.Date
import java.util.UUID
import org.mindrot.jbcrypt.BCrypt
import java.math.BigInteger
import java.security.MessageDigest


class Event(){
    var timestamp:String = ""
    var key:String = ""
    var action:String = ""
    var type:String = ""
    var name:String = ""
    var reason:String = ""
    var content:String = ""

    constructor(iKey:String, iAction:String,iType:String,iName:String,iReason:String, icontent:String) : this(){
        timestamp = Date(System.currentTimeMillis()).toString()
        key = iKey
        action = iAction
        type = iType
        name = iName
        reason = iReason
        content = icontent
    }

    override fun toString() = "Event(${timestamp}):"

    fun toJSON() = Klaxon().toJsonString(this)
}
