package com.atms

import grails.test.spock.IntegrationSpec
import com.atms.RatingsUpdateJob
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import spock.lang.Ignore

class RatingsUpdateJobSpec extends IntegrationSpec {
	
	def usattRatingsHistoryService
	def userProfileService
	
	def df = new SimpleDateFormat("MM/dd/yyyy")
	
	def setup() {
    }

    def cleanup() {
    }

	@Ignore("do not need to test this right now")
	void "test processing TD ratings files"() {
		setup:
		def ratingsUpdateJob = new RatingsUpdateJob()
		ratingsUpdateJob.usattRatingsHistoryService = usattRatingsHistoryService
		ratingsUpdateJob.userProfileService = userProfileService
		
		when:
		Date asOfDate1 = df.parse ('03/17/16')
		ratingsUpdateJob.processTDRatingsFile("c:\\grails\\data\\ratings\\TD Rating 3-17-16.csv", asOfDate1)
		int count = usattRatingsHistoryService.count()
		println 'count ' + count
		
		then:
		count > 0
		
		when:
		int rating1 = usattRatingsHistoryService.findLatestRatingAsOf(84639, asOfDate1)
		
		then:
		rating1 == 1771
		
		when:
		Date asOfDate2 = df.parse ('03/23/2016')
		ratingsUpdateJob.processTDRatingsFile("c:\\grails\\data\\ratings\\TD Rating 3-22-16.saved.csv", asOfDate2)
		count = usattRatingsHistoryService.count()
		println 'new count ' + count
		int rating2 = usattRatingsHistoryService.findLatestRatingAsOf(84639, asOfDate2)
		
		then:
		rating2 == 1774
    }
	
	void "test scrape player data"() {
		setup:
			def ratingsUpdateJob = new RatingsUpdateJob()
			ratingsUpdateJob.usattRatingsHistoryService = usattRatingsHistoryService
			ratingsUpdateJob.userProfileService = userProfileService
			Date today = new Date()
			
		when:
// 			 def filename = ratingsUpdateJob.scrapeRatingsDataIntoFile(asOfDate1)
			 // players from Kentucky
			 def filename = "c:\\grails\\data\\ratings\\Ratings 3-23-16.csv"
			 def file = new File(filename)
			 def fileSize = file.length()
			 int linesCount = 0
			 file.withReader { reader ->
				 def line = null
				 while (line = reader.readLine()) {
					 linesCount++
				 }
			 }
 
		then:
			fileSize > 0
//			linesCount > 90000
			
		when:
			
			def userProfile = userProfileService.findByFirstLastName('John Abraham')
			println userProfile
			
			// put in in db
			ratingsUpdateJob.processRatingsFile(filename, today)
			int countOfRatings = usattRatingsHistoryService.count()
			
		then:
			countOfRatings == 796
			
		when:
			int rating2 = usattRatingsHistoryService.findLatestRatingAsOf(25017, today)
			
		then:
			rating2 == 1388

	}
}
