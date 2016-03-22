package com.atms

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import com.atms.utils.USATTDataSource

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class USATTDataSourceSpec extends Specification {

	def setup() {
	}

	def cleanup() {
	}

	void "test player record by id"() {
		setup:

		when:
		def listOfRecords = USATTDataSource.getPlayerRecordById (84639)

		then:
		
		listOfRecords.size() == 1
		def playerRecordMap = listOfRecords[0]
		playerRecordMap['Name'] == 'Swavek Lorenc'
		playerRecordMap['Location'].contains('Aurora')
		playerRecordMap['Location'].contains('IL')
		playerRecordMap['USATT #'] == '84639'
	}

	void "test player record by last name"() {
		setup:

		when:
		def listOfRecords = USATTDataSource.getPlayerRecordByLastName("Lorenc")

		then:
		listOfRecords.size() >= 1
		listOfRecords.each () {playerRecordMap ->
			playerRecordMap.size() > 0
			if (playerRecordMap['Name'].equals('Swavek Lorenc')) {
				playerRecordMap['Location'].contains('Aurora')
				playerRecordMap['Location'].contains('IL')
				playerRecordMap['USATT #'].equals('84639')
				playerRecordMap['Rating'] != null
				int rating = playerRecordMap['Rating'] as Integer
				rating > 1500
			} else if (playerRecordMap['Name'].equals('Mario Lorenc')) {
				playerRecordMap['Location'].contains('Phoenix')
				playerRecordMap['Location'].contains('AZ')
				playerRecordMap['USATT #'].equals('51434')
				playerRecordMap['Rating'] != null
				int rating = playerRecordMap['Rating'] as Integer
				rating > 2000
			}
		}
	}

	void "test rating by id"() {
		setup:

		when:
		int rating = USATTDataSource.getPlayerRatingById (84639)

		then:
		println 'rating is ' + rating
		rating > 1700
	}
	
	void "test state of IL players page"() {
		setup:

		when:
		def listOfRecords0 = USATTDataSource.getPlayerRecordsByState ('IL', 0)
		def listOfRecords1 = USATTDataSource.getPlayerRecordsByState ('IL', 1)
		def listOfRecords2 = USATTDataSource.getPlayerRecordsByState ('IL', 2)
		def listOfRecordsLast = USATTDataSource.getPlayerRecordsByState ('IL', 178)
		def listOfRecordsEmpty = USATTDataSource.getPlayerRecordsByState ('IL', 179)

		then:
		listOfRecords0.size() == 20
		listOfRecords0.each () {playerRecordMap ->
			playerRecordMap.size() == 5
		}
		listOfRecords1.size() == 20
		listOfRecords1.each () {playerRecordMap ->
			playerRecordMap.size() == 5
		}
		listOfRecords2.size() == 20
				listOfRecords2.each () {playerRecordMap ->
				playerRecordMap.size() == 5
		}
		listOfRecordsLast.size() > 0
		listOfRecordsLast.each () {playerRecordMap ->
		playerRecordMap.size() == 5
		}
		listOfRecordsEmpty.size() == 0
	}
}
