package com.atms

import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import spock.lang.Specification
//import grails.buildtestdata.mixin.Build
import org.springframework.security.authentication. UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH


/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(EventEntryService)
@Mock([EventEntry])
//@Build(Author)
class EventEntryServiceSpec extends Specification {

	def setup() {
		createUsers ()
		// have to be authenticated as an admin to create ACLs
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
				'swavek', 'swavek',
				AuthorityUtils.createAuthorityList('ROLE_TOURNAMENT_DIRECTOR'))
		
	}

	def cleanup() {
		SCH.clearContext()
	}
	
	void createUsers () {
		def userRole = SecRole.findByAuthority ('ROLE_USER') ?: new SecRole (authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = SecRole.findByAuthority ('ROLE_ADMIN') ?: new SecRole (authority: 'ROLE_ADMIN').save(failOnError: true)
		def tournamentDirectorRole = SecRole.findByAuthority ('ROLE_TOURNAMENT_DIRECTOR') ?: new SecRole (authority: 'ROLE_TOURNAMENT_DIRECTOR').save(failOnError: true)

		def user1 = SecUser.findByUsername('swavek') ?: new SecUser(username: 'swavek', enabled: true, password: 'swavek', email: 'swaveklorenc@yahoo.com').save(failOnError: true)
		if (!user1.authorities.contains(userRole)) {
			SecUserSecRole.create(user1, userRole, true)
		}
		if (!user1.authorities.contains(adminRole)) {
			SecUserSecRole.create(user1, adminRole, true)
		}

		def user2 = SecUser.findByUsername('matthew') ?: new SecUser(username: 'matthew', enabled: true, password: 'matthew', email: 'matthewlorenc@yahoo.com').save(failOnError: true)
		if (!user2.authorities.contains(userRole)) {
			SecUserSecRole.create(user2, userRole, true)
		}

		def user3 = SecUser.findByUsername('craig') ?: new SecUser(username: 'craig', enabled: true, password: 'craig', email: 'support@omnipong.com').save(failOnError: true)
		if (!user3.authorities.contains(userRole)) {
			SecUserSecRole.create(user3, userRole, true)
		}
		if (!user3.authorities.contains(tournamentDirectorRole)) {
			SecUserSecRole.create(user3, tournamentDirectorRole, true)
		}

		def user4 = SecUser.findByUsername('ed') ?: new SecUser(username: 'ed', enabled: true, password: 'ed', email: 'ed@landmarkbilling.com').save(failOnError: true)
		if (!user4.authorities.contains(userRole)) {
			SecUserSecRole.create(user4, userRole, true)
		}
		if (!user4.authorities.contains(tournamentDirectorRole)) {
			SecUserSecRole.create(user4, tournamentDirectorRole, true)
		}
	}

	void "testFirst"() {
		given: "List of Entries"
		//          List<EventEntry> entries = createEntries()
		def eventEntry1 = new EventEntry()
		eventEntry1.status = EventEntry.EntryStatus.PENDING
		eventEntry1.dateEntered = new Date()
		service.create (eventEntry1, new HashMap())
		def eventEntry2 = new EventEntry()
		eventEntry2.status = EventEntry.EntryStatus.CONFIRMED
		eventEntry2.dateEntered = new Date()
		service.create (eventEntry1, new HashMap())

		when: "service is called"
		//"service" represents the grails service you are testing for
		def result = service.count()

		then: "Expect something to happen"
		//Assertion goes here
		println 'result = ' + result
	}

//	def createEntries () {
//		def eventEntry1 = new EventEntry()
//		eventEntry1.status = EventEntry.EntryStatus.PENDING
//		eventEntry1.dateEntered = new Date()
//		service.create (eventEntry1, new HashMap())
//		def eventEntry2 = new EventEntry()
//		eventEntry2.status = EventEntry.EntryStatus.CONFIRMED
//		eventEntry2.dateEntered = new Date()
//		service.create (eventEntry1, new HashMap())
//		[eventEntry1, eventEntry2]
//		//[EventEntry.build(), EventEntry.build()]
//	}
}
