package au.gov.api


import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Test

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

import com.opentable.db.postgres.embedded.EmbeddedPostgres

class RegistrationManagerTests{

    var manager = EventManager(MockDataSource())

	@Test
    fun can_round_trip_event(){

		val keyJson = """{"key": "abcd@api.gov.au", "name": "b42d47df-7954-4bfc-a88d-12f8ba23a5db", "type": "Service", "action": "Updated", "reason": "Revision from 0 to 1", "timestamp": "Wed Dec 12 14:12:39 AEDT 2018"}"""
		val om = ObjectMapper()
		val event =  om.readValue(keyJson, Event::class.java)
		manager.newEvent(event)

		val retEvent = manager.getEvents("12/12/2018","12/12/2100","b42d47df-7954-4bfc-a88d-12f8ba23a5db",10).first()

		Assert.assertEquals("abcd@api.gov.au", retEvent.key)
		Assert.assertEquals("b42d47df-7954-4bfc-a88d-12f8ba23a5db", retEvent.name)
		Assert.assertEquals("Service", retEvent.type)
		Assert.assertEquals("Updated", retEvent.action)
		Assert.assertEquals("Revision from 0 to 1", retEvent.reason)
    }

	@Test 
	fun cant_get_key_events(){

		val keyJson = """{"key": "abcd@api.gov.au", "name": "b42d47df-7954-4bfc-a88d-12f8ba23a5db", "type": "key", "action": "Updated", "reason": "Revision from 0 to 1", "timestamp": "Wed Dec 12 14:12:39 AEDT 2018"}"""
		val om = ObjectMapper()
		val event =  om.readValue(keyJson, Event::class.java)
		manager.newEvent(event)

		var retEvent = manager.getEvents("12/12/2018","12/12/2100","b42d47df-7954-4bfc-a88d-12f8ba23a5db",10).firstOrNull()

		Assert.assertEquals(null, retEvent)

	}


}
