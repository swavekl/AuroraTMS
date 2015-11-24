package com.atms

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import java.text.SimpleDateFormat
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(UserProfileController)
@Mock([UserProfile])
class UserProfileControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test index"() {
		given:
		def df = new SimpleDateFormat("MM/dd/yyyy")
		def userProfile1 = new UserProfile (firstName: 'Justine', lastName: 'Lorenc', dateOfBirth: df.parse('10/11/1994'), email: 'justinelorenc@yahoo.com', phone: '630-999-9980', streetAddress: '2458 Angela Ln', city: 'Aurora', state: 'IL', zipCode: '60103')
		userProfile1.save(flush: true, failOnError: true)
		def userProfile2 = new UserProfile (firstName: 'Danielle', lastName: 'Lorenc', dateOfBirth: df.parse('10/11/1994'), email: 'daniellelorenc@yahoo.com', phone: '630-999-1180', streetAddress: '2223 New York Ave', city: 'Aurora', state: 'IL', zipCode: '60103')
		userProfile2.save(flush: true, failOnError: true)
		
		when:
		request.method = 'GET'
		response.format = 'json'
		controller.index()

		then:
		response.status == 200
		response.text == '[{"class":"com.atms.UserProfile","id":1,"city":"Aurora","dateOfBirth":"1994-10-11T05:00:00Z","email":"justinelorenc@yahoo.com","firstName":"Justine","lastName":"Lorenc","phone":"630-999-9980","state":"IL","streetAddress":"2458 Angela Ln","usattID":0,"zipCode":"60103"},{"class":"com.atms.UserProfile","id":2,"city":"Aurora","dateOfBirth":"1994-10-11T05:00:00Z","email":"daniellelorenc@yahoo.com","firstName":"Danielle","lastName":"Lorenc","phone":"630-999-1180","state":"IL","streetAddress":"2223 New York Ave","usattID":0,"zipCode":"60103"}]'
    }

    void "test save"() {
//		given:
//		def df = new SimpleDateFormat("MM/dd/yyyy")
//		def userProfile1 = new UserProfile (firstName: 'Justine', lastName: 'Lorenc', dateOfBirth: df.parse('10/11/1994'), email: 'justinelorenc@yahoo.com', phone: '630-999-9980', streetAddress: '2458 Angela Ln', city: 'Aurora', state: 'IL', zipCode: '60103')
//		userProfile1.save(flush: true, failOnError: true)
//		def userProfile2 = new UserProfile (firstName: 'Danielle', lastName: 'Lorenc', dateOfBirth: df.parse('10/11/1994'), email: 'daniellelorenc@yahoo.com', phone: '630-999-1180', streetAddress: '2223 New York Ave', city: 'Aurora', state: 'IL', zipCode: '60103')
//		userProfile2.save(flush: true, failOnError: true)
		
		when:
		request.method = 'POST'
		request.format = 'json'
		request.text = '{"city":"Aurora","dateOfBirth":"1994-10-11T05:00:00Z","email":"justinelorenc@yahoo.com","firstName":"Swavek","lastName":"Lorenc","phone":"630-999-9980","state":"IL","streetAddress":"2458 Angela Ln","usattID":0,"zipCode":"60103"}'
		response.format = 'json'
		controller.save()

		then:
		response.status == 200
		response.text == '[{"class":"com.atms.UserProfile","id":1,"city":"Aurora","dateOfBirth":"1994-10-11T05:00:00Z","email":"justinelorenc@yahoo.com","firstName":"Swavek","lastName":"Lorenc","phone":"630-999-9980","state":"IL","streetAddress":"2458 Angela Ln","usattID":0,"zipCode":"60103"}]'
    }
}

/*
 * 
 import groovyx.net.http.HTTPBuilder
def http = new HTTPBuilder('http://yourdomain.com/catalog/')
http.request(POST, JSON) {
  requestContentType = ContentType.APPLICATION_JSON // coreesponding to application/json
  body = ["descripcion": "bla", "nombre" : "lalala", "numeroParametros":3, "parametros":[{ "tipoParametro":"string", "json":"bla"}],"url":"google.com"]

  response.success = { resp ->
    assert resp.statusLine.statusCode == 200
  }
} 
 * */
