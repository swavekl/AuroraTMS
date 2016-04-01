package com.atms

import grails.test.spock.IntegrationSpec
import java.text.SimpleDateFormat

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 * 
 * You need to do 5 things to make it an integration test:
1. Move the test to the integration folder
2. Remove the @TestFor annotation
3. Extend IntegrationSpec, not Specification
4. Declare and instantiate your controller as “def xController = new xController()” (@TestFor gave you the “controller” variable so you have to manually create it now)
5. Don’t worry about any dependent services – integration tests are wired up by Grails dependency injection so it should just work


import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
def userService = AH.application.mainContext.userService

 */
class UsattRatingsHistoryServiceSpec extends IntegrationSpec {
	
	def usattRatingsHistoryService
	
	String dateFormat = "MM/dd/yyyy"
	def df = new SimpleDateFormat(dateFormat)
	
	def setup() {
	}

	def cleanup() {
	}

	void "test getting latest rating"() {
		setup:
		Date asOfDate1 = df.parse ('02/18/2016')
		usattRatingsHistoryService.create(123, 1200, asOfDate1)
		Date asOfDate2 = df.parse ('02/25/2016')
		usattRatingsHistoryService.create(123, 1250, asOfDate2)
		Date asOfDate3 = df.parse ('03/18/2016')
		usattRatingsHistoryService.create(123, 1230, asOfDate3)
		
		when:
		// before
		Date asOfDate4 = df.parse ('02/17/2016')
		int rating1 = usattRatingsHistoryService.findLatestRatingAsOf(123, asOfDate4)

		then:
		//println 'rating1 = ' + rating1
		rating1 == 0

		when:
		// on date
		int rating2 = usattRatingsHistoryService.findLatestRatingAsOf(123, asOfDate1)

		then:
		//println 'rating2 = ' + rating2
		rating2 == 1200

		when:
		// between dates
		Date asOfDate5 = df.parse ('03/01/2016')
		int rating3 = usattRatingsHistoryService.findLatestRatingAsOf(123, asOfDate5)

		then:
		//println 'rating3 = ' + rating3
		rating3 == 1250


		when:
		// between dates
		Date asOfDate6 = df.parse ('03/22/2016')
		int rating4 = usattRatingsHistoryService.findLatestRatingAsOf(123, asOfDate6)

		then:
		println 'rating4 = ' + rating4
		rating4 == 1230
	}
	
	void "test getting list of latest ratings" () {
		setup:
		def asOfDate1 = df.parse("03/12/2016")
		def asOfDate2 = df.parse("03/19/2016")
		
		when:
			usattRatingsHistoryService.create(123, 1200, asOfDate1)
			usattRatingsHistoryService.create(456, 1450, asOfDate1)
			usattRatingsHistoryService.create(123, 1253, asOfDate2)
			usattRatingsHistoryService.create(456, 1410, asOfDate2)
			int ratingP1 = usattRatingsHistoryService.findLatestRatingAsOf(456, asOfDate1)
			int ratingP2 = usattRatingsHistoryService.findLatestRatingAsOf(456, asOfDate2)
			
		then:
			ratingP1 == 1450
			ratingP2 == 1410
			
		when:
			def latestRatings = usattRatingsHistoryService.findAllLatestRatingsAsOf(asOfDate2)
		
		then:
			latestRatings.size() == 2
			
		when:
			def newRatingsMap = [:]
			def asOfDate3 = df.parse("03/21/2016")
			newRatingsMap[456] = new UsattRatingsHistory (memberId: 456, rating: 1534, asOfDate: asOfDate3)
			usattRatingsHistoryService.updatePlayerRatings (newRatingsMap)
			
			int ratingP3 = usattRatingsHistoryService.findLatestRatingAsOf(456, asOfDate2)
			int ratingP4 = usattRatingsHistoryService.findLatestRatingAsOf(456, asOfDate3)
			
		then:
			ratingP3 == 1410
			ratingP4 == 1534

		when:
			def newRatingsMap2 = [:]
			def asOfDate4 = df.parse("03/22/2016")
			newRatingsMap2[123] = new UsattRatingsHistory (memberId: 123, rating: 1282, asOfDate: asOfDate4)
			newRatingsMap2[456] = new UsattRatingsHistory (memberId: 456, rating: 1421, asOfDate: asOfDate4)
			// test new player
			newRatingsMap2[789] = new UsattRatingsHistory (memberId: 789, rating: 998, asOfDate: asOfDate4)
			usattRatingsHistoryService.updatePlayerRatings (newRatingsMap2)
			int ratingP5 = usattRatingsHistoryService.findLatestRatingAsOf(123, asOfDate4)
			int ratingP6 = usattRatingsHistoryService.findLatestRatingAsOf(456, asOfDate4)
			int ratingP7 = usattRatingsHistoryService.findLatestRatingAsOf(789, asOfDate4)
			
		then:
			ratingP5 == 1282
			ratingP6 == 1421
			ratingP7 == 998
	}
}
