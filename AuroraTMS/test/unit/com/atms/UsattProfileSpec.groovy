package com.atms

import grails.test.mixin.TestFor
import spock.lang.Specification
import grails.test.mixin.Mock
import groovy.xml.*
import java.text.SimpleDateFormat

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(UsattProfile)
@Mock([UsattProfile])
class UsattProfileSpec extends Specification {

	def setup() {
	}

	def cleanup() {
	}

	void testLoading() {
		given: "test loading"
		println "Importing USATT player data"
//		def xmlFile = "C:\\grails\\OmniPong\\web-app\\data\\USATT_Membership.xml"
		def xmlFile = "C:\\grails\\newworkspace\\AuroraTMS\\USATTMembersSmall.xml"
		def parsedFile = new XmlSlurper().parse(xmlFile)
println 'Finished parsing'
		def count = 0
		def dateFormatter = new SimpleDateFormat('M/d/yyyy') // 9/30/1958
		parsedFile.Table.each() {table ->
			println 'player ' + table.LastName.text() + " " + table.FirstName.text()
			count++
			
			def dateOfBirth = table.DOB.text() != "" ? dateFormatter.parse(table.DOB.text()) : null
			def expirationDate = table.ExpirationDate.text() != "" ? dateFormatter.parse(table.ExpirationDate.text()) : null
			def lastPlayed = table.LastPlayed.text() != "" ? dateFormatter.parse(table.LastPlayed.text()) : null
			int memberId = table.MemberID.toInteger();
			String lastName = table.LastName.text()
			String firstName = table.FirstName.text()
			String middleName = table.MiddleName.text() ?: ''
			String address1 = table.Address1.text() ?: ''
			String address2 = table.Address2.text() ?: ''
			String city = table.City.text() ?: ''
			String state = table.State.text() ?: ''
			String zipCode = table.ZipCode.text() ?: ''
			String country = table.Country.text() ?: ''
			String gender = table.Sex.text() ?: 'M'
			int rating = table.Rating.toInteger();
			new UsattProfile(id: memberId, 
				lastName: lastName, 
				firstName: firstName,
				middleName: middleName,
				address1: address1,
				address2: address2,
				city: city,
				state: state,
				zipCode: zipCode,
				country: country,
				gender: gender,
				dateOfBirth: dateOfBirth,
				rating: rating,
				expirationDate: expirationDate,
				lastPlayedDate: lastPlayed
				)
			.save(failOnError: true)
		  }
		println "Done importing " + count + " player's records"
//		def params = [:].withDefault { [] }
//		params ['lastName'] << 'Lorenc'
//		def lorences = UsattProfile.list(params)
		def c = UsattProfile.createCriteria()
		def lorences = c.list {
			like('lastName', 'Lorenc')
			order('dateOfBirth')
		}
		assert 2 == lorences.size()
		println 'Found ' + lorences.size() + ' players with last name Lorenc'
		lorences.each {
			println it.firstName + " " + it.lastName + " " + it.dateOfBirth + " " + it.address1 + ", " + it.address2 + ", " + it.city + ", " + it.state + ", " + it.zipCode
		} 

		when: "test when"

		then: "test then"
	}

//	void testAnother() {
//		given: "abc"
//
//		def xml = '''
//<books xmlns:meta="http://meta/book/info" count="3">
//  <book id="1">
//    <title lang="en">Groovy in Action</title>
//    <meta:isbn>1-932394-84-2</meta:isbn>
//  </book>
//  <book id="2">
//    <title lang="en">Groovy Programming</title>
//    <meta:isbn>0123725070</meta:isbn>
//  </book>
//  <book id="3">
//    <title>Groovy &amp; Grails</title>
//    <!--Not yet available.-->
//  </book>
//  <book id="4">
//    <title>Griffon Guide</title>
//  </book>
//</books>
//'''
//println 'Another test'		
//		def books = new XmlSlurper().parseText(xml).declareNamespace([meta:'http://meta/book/info'])
//		assert books instanceof groovy.util.slurpersupport.GPathResult
//		books.book.each {
//			println 'Title: ' + it.title.text()
//		}
//		assert 4 == books.book.size()
//		assert 11 == books.breadthFirst().size()
//		assert 'Groovy in Action' == books.book[0].title.text()
//		assert 'Groovy Programming' == books.book.find { it.@id == '2' }.title as String
//		assert [1, 2, 3] == books.book.findAll { it.title =~ /Groovy/ }.'@id'*.toInteger()
//		assert ['1-932394-84-2', '0123725070'] == books.book.'meta:isbn'*.toString()
//		
//		when: "def"
//		then: "ghi"
//	}	   
}

