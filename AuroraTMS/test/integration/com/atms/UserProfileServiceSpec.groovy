package com.atms

import java.text.SimpleDateFormat
import java.util.Map;
import java.util.List;

import grails.test.mixin.*
import grails.test.spock.IntegrationSpec
import groovy.time.TimeDuration

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH

class UserProfileServiceSpec extends IntegrationSpec {
	
	def userProfileService

	def eventEntryService
	def tournamentEntryService

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

    void "test getting user profiles for event and tournament"() {
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
				
				// associate with user
				int memberId = 900000 + i 
				def userProfile = new UserProfile(
					usattID: memberId,
					firstName : 'firstName_' + i,
					lastName : 'lastName_' + i,
					dateOfBirth : new Date(),
					gender : 'M',
					email : 'johl' + i + '@yahoo.com',
					phone : "630-111-2222",
					streetAddress : '123 Nice street',
					city : 'Aurora',
					state : 'IL',
					zipCode : '60502',
					country : 'USA',
					club: 'FVTTC'
					)
				userProfile.addToTournamentEntries(tournamentEntry)
				userProfileService.create (userProfile)
				//userProfiles.save (failOnError: true, flush: true)
				
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
			
		when:
			def eventEntries = eventEntryService.listEventEntries(event2.id, EventEntry.EntryStatus.CONFIRMED)
			
		then:
			eventEntries.size() == 15
			
		when:
			def tournamentEntries = tournamentEntryService.listForEventEntries(eventEntries)
		
		then:
			tournamentEntries.size() == 15 
		
		when:
			def userProfiles = userProfileService.listProfilesForTournamentEntries(tournamentEntries)
		
		then:
			userProfiles.size() == 15
			userProfiles.each () {
				println it.dump()
			} 
		
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
