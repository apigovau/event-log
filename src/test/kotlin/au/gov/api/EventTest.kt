package au.gov.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Test

class RegistrationTest{

    @Test
    fun can_deserialise_event(){
        val keyJson = """{"key": "abcd@api.gov.au", "name": "b42d47df-7954-4bfc-a88d-12f8ba23a5db", "type": "Service", "action": "Updated", "reason": "Revision from 0 to 1", "timestamp": "Wed Dec 12 14:12:39 AEDT 2018"}"""
		val om = ObjectMapper()
		val event =  om.readValue(keyJson, Event::class.java)

		Assert.assertEquals("abcd@api.gov.au", event.key)
		Assert.assertEquals("b42d47df-7954-4bfc-a88d-12f8ba23a5db", event.name)
		Assert.assertEquals("Service", event.type)
		Assert.assertEquals("Updated", event.action)
		Assert.assertEquals("Revision from 0 to 1", event.reason)

	}
}
