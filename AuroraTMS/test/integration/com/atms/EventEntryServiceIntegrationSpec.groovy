package com.atms

import java.text.SimpleDateFormat
import java.util.Map;

import grails.test.mixin.*
import grails.test.spock.IntegrationSpec
import groovy.time.TimeDuration

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH

class EventEntryServiceIntegrationSpec extends IntegrationSpec {

	def eventEntryService

	def df = new SimpleDateFormat("MM/dd/yyyy")

	def setup() {

		// login as admin have to be authenticated as an admin to create ACLs
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
				'swavek', 'swavek',
				AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
    }

    def cleanup() {
		// logout
		SCH.clearContext()
    }

    void "test getting counts of confirmed event entries"() {
		given: 'define tournament with some events'
			// create tournament
			Tournament tournament = new Tournament (name: "2018 Aurora Cup", venue: 'Vaughan Athletic Center', address: '2121 W. Indian Trail', city: "Aurora", state: "IL", 
				startDate: df.parse('05/16/2018'), endDate: df.parse('05/17/2018'), starLevel: 4, stripeKey: "test_123131231lkjas")
			tournament.contactEmail = 'swavek@abc.com'
			tournament.contactName = 'Swavek'
			tournament.contactPhone = "123-456-0989"
			tournament.entryCutoffDate = df.parse('05/17/2018')
			tournament.lateEntryStartDate = df.parse('05/17/2018')
			tournament.ratingCutoffDate = df.parse('05/17/2018')
			tournament.refundCutoffDate = df.parse('05/17/2018')
			tournament.save(failOnError: true, flush: true)
			//println 't id ' + tournament.id
			
			// create some events
			int ordinalNum = 0
			def event1 = new Event(ordinalNumber: ++ordinalNum, name:'Open Doubles RR', day: 1, startTime: 18.5, feeAdult: 32.0, feeJunior: 32.0)
			def event2 = new Event(ordinalNumber: ++ordinalNum, name:'Under 3800 Doubles RR', day: 1, startTime: 18.5, feeAdult: 32.0, feeJunior: 32.0)
			def event3 = new Event(ordinalNumber: ++ordinalNum, name:'Under 18 Youth', day: 1, startTime: 18.5, maxPlayerAge: 17, feeAdult: 0, feeJunior: 20.0)
			def event4 = new Event(ordinalNumber: ++ordinalNum, name:'Over 50 RR', day: 1, startTime: 18.5, minPlayerAge: 50, feeAdult: 32.0, feeJunior: 0)
			def event5 = new Event(ordinalNumber: ++ordinalNum, name:'Under 1500 RR', day: 1, startTime: 18.5, maxPlayerRating: 1499, feeAdult: 28.0, feeJunior: 28.0)
			Event[] eventsArray = [event1, event2, event3, event4, event5]
			eventsArray.each () {
				it.tournament = tournament
				tournament.addToEvents(it)
				tournament.save(flush: true)
				it.save(flush: true)
			}
		
        and: 'create tournament entries for several players'
			int [] playerEntries = [1, 3, 4, 2, 4, 1, 5, 3, 4, 2, 2, 4, 1, 3, 5, 3, 2, 1 ,3]
			for (int i = 0; i < playerEntries.length; i++) {
				def tournamentEntry = new TournamentEntry()
				tournamentEntry.dateEntered = new Date()
				tournamentEntry.membershipOption = 0
				tournamentEntry.tournament = tournament
				tournamentEntry.save (failOnError: true, flush: true)
				
				int entriesToMake = playerEntries [i]
				for (int k = 0; k < entriesToMake; k++){
					EventEntry eventEntry = new EventEntry ()
					eventEntry.dateEntered = new Date()
					eventEntry.status = EventEntry.EntryStatus.CONFIRMED
					eventEntry.event = eventsArray[k]
					eventEntry.tournamentEntry = tournamentEntry
					eventEntry.save (failOnError: true, flush: true) 
				}
			}
		
        when: 'get counts'
			Map expectedValues = [:]
			expectedValues [1] = 19;
			expectedValues [2] = 15;
			expectedValues [3] = 11;
			expectedValues [4] = 6;
			expectedValues [5] = 2;
	        Map counts = eventEntryService.getEventEntriesCount(tournament.id, EventEntry.EntryStatus.CONFIRMED)
		
        then: 'check counts'
			counts.size() == expectedValues.size()
			counts.each () {key, value ->
				def expectedValue = expectedValues [key]
				expectedValue == value  
			}
	}
}
