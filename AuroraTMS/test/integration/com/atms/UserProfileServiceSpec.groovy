package com.atms

//import grails.test.mixin.Mock
//import grails.test.mixin.TestFor
import grails.test.spock.IntegrationSpec
//import spock.lang.*

class UserProfileServiceSpec extends IntegrationSpec {
	
	def userProfileService

	def setup() {
	}

	def cleanup() {
	}

	void "test create and find by combined first and last name"() {

		setup:
			def userProfile = new UserProfile(
					usattID: 390123,
					firstName : 'John',
					lastName : 'Lennon',
					dateOfBirth : new Date(),
					gender : 'M',
					email : 'johl@yahoo.com',
					phone : "630-111-2222",
					streetAddress : '123 Nice street',
					city : 'Aurora',
					state : 'IL',
					zipCode : '60502',
					country : 'USA',
					club: 'FVTTC'
					)
			userProfileService.create (userProfile)
		def userProfile2 = new UserProfile(
				usattID: 390456,
				firstName : 'Paul',
				lastName : 'McCartney',
				dateOfBirth : new Date(),
				gender : 'M',
				email : 'paulm@yahoo.com',
				phone : "630-111-2222",
				streetAddress : '456 Nice street',
				city : 'Tuscon',
				state : 'AZ',
				zipCode : '60502',
				country : 'USA',
				club: 'FVTTC'
				)
		userProfileService.create (userProfile2)

		when:
			def found = userProfileService.findByFirstLastName ('John Lennon')

		then:
			found != null
			found.usattID == 390123
			
		when:
			found = userProfileService.findByFirstLastName ('Paul McCartney')
			
			then:
				found != null
				found.usattID == 390456
					
		when:
		// unknown	
		found = userProfileService.findByFirstLastName ('George Harrison')

		then:
			found == null
		}
}
