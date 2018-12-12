
package au.gov.api

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource
import java.util.Date

import com.fasterxml.jackson.databind.ObjectMapper


@Component
class EventManager{

        @Value("\${spring.datasource.url}")
        private var dbUrl: String? = null

        @Autowired
        private lateinit var dataSource: DataSource

		constructor(){}

		constructor(theDataSource:DataSource){
			dataSource = theDataSource
		}

        private fun createTable(connection:Connection){
            val stmt = connection.createStatement()
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS events(event jsonb)")
        }

		fun newEvent(event:Event):String{
			insertEvent(event)
			return event.timestamp
		}

        fun insertEvent(event:Event) {
            var connection: Connection? = null
            try {
                connection = dataSource.connection
				createTable(connection)
                val insertStatement = connection.prepareStatement("INSERT INTO events values(?::JSON)")
				insertStatement.setObject(1, event.toJSON())
				insertStatement.execute()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (connection != null) connection.close()
            }
        }

        fun getEvents(startDate:String, endDate:String, name:String, limit:Int):Iterable<Event> {


            var connection: Connection? = null
            try {
                connection = dataSource.connection

                val q = connection.prepareStatement("SELECT * FROM events WHERE (event->>'timestamp')::timestamp with time zone Between ?::timestamp with time zone AND ?::timestamp with time zone AND event->>'name' = ? ORDER BY (event->>'timestamp')::timestamp with time zone DESC LIMIT ?;")
                q.setString(1, startDate)
                q.setString(2, endDate)
                q.setString(3, name)
                q.setInt(4, limit)

                val rv: MutableList<Event> = mutableListOf()
                var rs = q.executeQuery()
                val om = ObjectMapper()
                while (rs.next()) {
                    var item = om.readValue(rs.getString("event"), Event::class.java)
                    if (item.type.toLowerCase() != "key") rv.add(item)
                }
                return rv
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception()
            } finally {
                if (connection != null) connection.close()
            }
        }

        @Bean
        @Throws(SQLException::class)
        fun dataSource(): DataSource? {
            if (dbUrl?.isEmpty() ?: true) {
                return HikariDataSource()
            } else {
                val config = HikariConfig()
                config.jdbcUrl = dbUrl
                try {
                    return HikariDataSource(config)
                } catch (e: Exception) {
                    return null
                }
            }
        }
}
