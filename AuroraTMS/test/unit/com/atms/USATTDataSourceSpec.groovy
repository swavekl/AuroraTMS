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
		playerRecordMap['Name'].equals('Swavek Lorenc')
		playerRecordMap['Location'].contains('Aurora')
		playerRecordMap['Location'].contains('IL')
		playerRecordMap['USATT #'].equals('84639')
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

	void "test player record by last, first name, city and state"() {
		setup:

		when:
		def listOfRecords = USATTDataSource.getPlayerRecordBy("Lorenc", 'Swavek', 'Aurora', 'IL')

		then:
		listOfRecords.size() == 1
		listOfRecords.each () {playerRecordMap ->
			playerRecordMap.size() > 0
			if (playerRecordMap['Name'].equals('Swavek Lorenc')) {
				playerRecordMap['Location'].indexOf('Aurora') >= 0
				playerRecordMap['Location'].indexOf('IL') >= 0
				playerRecordMap['USATT #'].equals('84639')
				playerRecordMap['Rating'] != null
				int rating = playerRecordMap['Rating'] as Integer
				rating > 1500
			}
		}
		when:
		listOfRecords = USATTDataSource.getPlayerRecordBy("Liu", 'John', 'Naperville', 'IL')
		//John Liu 	Naperville, IL 	1401 	86206 	03/31/2016
		//listOfRecords = USATTDataSource.getPlayerRecordBy("Liu", 'John Zhong', 'Aurora', 'IL')

		then:
		listOfRecords.size() == 1
		listOfRecords.each () {playerRecordMap ->
			playerRecordMap.size() > 0
			if (playerRecordMap['Name'].equals('John Liu')) {
				playerRecordMap['Location'].indexOf('Naperville') >= 0
				playerRecordMap['Location'].indexOf('IL') >= 0
				playerRecordMap['USATT #'].equals('86206')
				playerRecordMap['Rating'] != null
				int rating = playerRecordMap['Rating'] as Integer
				rating > 1300
			}
		}

	}

	void "match city state"() {
		setup:
		
		when:
			boolean matching1 = USATTDataSource.isMatchingCityState ("Aurora,     IL", 'Aurora', 'IL')
			boolean matching2 = USATTDataSource.isMatchingCityState (",   IL", 'Aurora', 'IL')
			boolean matching3 = USATTDataSource.isMatchingCityState ("Lake In the Hills,   IL", 'Lake in the Hills', 'IL')
			boolean matching4 = USATTDataSource.isMatchingCityState ("N/A", 'Aurora', 'IL')
			
		then:
			matching1 == true
			matching2 == false
			matching3 == true
			matching4 == false
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
		def listOfRecordsLast = USATTDataSource.getPlayerRecordsByState ('IL', 35)
		def listOfRecordsEmpty = USATTDataSource.getPlayerRecordsByState ('IL', 36)

		then:
		listOfRecords0.size() == 100
		listOfRecords0.each () {playerRecordMap ->
			playerRecordMap.size() == 5
		}
		listOfRecords1.size() == 100
		listOfRecords1.each () {playerRecordMap ->
			playerRecordMap.size() == 5
		}
		listOfRecords2.size() == 100
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
