package com.atms

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

import com.atms.utils.RegionStateInfo;

//import grails.transaction.Transactional
import org.springframework.transaction.annotation.Transactional

@Transactional
class EventEntryService {

	static transactional = false

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService
	
	def tournamentService

	void addPermission(EventEntry eventEntry, String username, int permission) {
		addPermission eventEntry, username, aclPermissionFactory.buildFromMask(permission)
	}

	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, admin)")
	void addPermission(EventEntry eventEntry, String username, Permission permission) {
		aclUtilService.addPermission eventEntry, username, permission
	}

	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, admin)")
	void deletePermission(EventEntry eventEntry, String username, Permission permission) {
		def acl = aclUtilService.readAcl(eventEntry)

		// Remove all permissions associated with this particular
		// recipient (string equality to KISS)
		acl.entries.eachWithIndex { entry, i ->
			if (entry.sid.equals(recipient) && entry.permission.equals(permission)) {
				acl.deleteAce i
			}
		}

		aclService.updateAcl acl
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_USER')")
	EventEntry create(EventEntry eventEntry, Map params) {
		//		EventEntry eventEntryEntry = new eventEntry(params)
		eventEntry.save(flush: true)
		println "granting ownership of Event Entry to user " + springSecurityService.authentication.name
		def currentPrincipal = springSecurityService.authentication.name
		
		// Grant the current principal administrative permission
		addPermission eventEntry, currentPrincipal, BasePermission.ADMINISTRATION
		
		grantAdminPermissions (eventEntry)
	}
	
	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, write) or hasPermission(#eventEntry, admin)")
	void update(EventEntry eventEntry, Map params) {
		eventEntry.save(flush: true)
		
		grantAdminPermissions(eventEntry)
	}

	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, delete) or hasPermission(#eventEntry, admin)")
	void delete(EventEntry eventEntry) {
		eventEntry.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl eventEntry
	}

	void grantAdminPermissions (EventEntry eventEntry) {
		// find tournament director who configured this tournament and grant him admin privileges on this entry
		def tdRole = SecRole.findByAuthority("ROLE_TOURNAMENT_DIRECTOR")
		def tournamentDirectors = SecUserSecRole.findAllBySecRole(tdRole).secUser
		def currentPrincipal = springSecurityService.authentication.name
		tournamentDirectors.each {
			if (it.username != currentPrincipal) {
				println 'granting access to eventEntry to TOURNAMENT_DIRECTOR" ' + it.username
				// check if this TD created it
				addPermission eventEntry, it.username, BasePermission.ADMINISTRATION
			}
		}
		
		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != currentPrincipal) {
				println 'granting access to eventEntry to ADMIN ' + it.username
				addPermission eventEntry, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

	@PreAuthorize("hasPermission(#id, 'com.atms.TournamentEntry', read) or hasPermission(#id, 'com.atms.Tournament', admin)")
	EventEntry get(long id) {
		EventEntry.get id
	}

	// anyone can see a eventEntry
	EventEntry show(long id) {
		EventEntry.get id
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	List<EventEntry> listOwned(Map params) {
//		println 'listOwned params ' + springSecurityService.authentication.name
		EventEntry.list params
	}

	int count() {
		EventEntry.count()
	}
	
	int count (eventId) {
		def result = EventEntry.withCriteria {
			idEq(eventId)
			projections { rowCount() }
		}
		int count = result[0]
		return count
	}

	// anybody can use it
	List<EventEntry> list(Map params) {
		EventEntry.list params
	}
	
	//
	// list entered events and those that are and are not available for some reason
	//
	List<EventEntry> listAllStatus (long tournamentId, long tournamentEntryId) {
		// list all events
		Tournament tournament = Tournament.get tournamentId
		def tournamentEvents = tournament.events

		// if there is an entry we will have some events that this user entered
		def eventEntries = []
		def tournamentEntry = null
		if (tournamentEntryId != 0) {
			tournamentEntry = TournamentEntry.get(tournamentEntryId)
			tournamentEntry.eventEntries.each {
				eventEntries.push(it)
			}
		}
		
		// now get all events and add those that player is not it to mark them as available or not available
		// first mark the entered events status
		for (eventEntry in eventEntries) {
			eventEntry.availabilityStatus = EventEntry.AvailabilityStatus.ENTERED
		}

		// second create event entries marked AVAILABLE for events that user didn't enter
		def eventsNotEntered = [];
		for (event in tournamentEvents) {
			boolean alreadyEntered = false;
			for (eventEntry in eventEntries) {
				if (eventEntry.event.id == event.id) {
					alreadyEntered = true;
					break;
				}
			}
			
			if (!alreadyEntered) {
				def notEnteredEventEntry = new EventEntry()
				notEnteredEventEntry.event = event
				notEnteredEventEntry.tournamentEntry = tournamentEntry
				notEnteredEventEntry.status = EventEntry.EntryStatus.NOT_SELECTED
				notEnteredEventEntry.availabilityStatus = EventEntry.AvailabilityStatus.AVAILABLE
				eventsNotEntered.add (notEnteredEventEntry)
			}
		}
		
		// get profile to get users age, gender and eligibility or seed rating
		int eligibilityRating = 1958
		int ageYears = 13
		String gender = 'M'
		for (event in tournamentEvents) {
			evaluateConflicts (event, eventEntries, eventsNotEntered, eligibilityRating, ageYears, gender)
		}
		
		for (eventNotEntered in eventsNotEntered) {
			// time conflicts
			evaluateTimeConflicts (eventNotEntered, eventEntries)
		}
		
		// set prices per age
		for (event in tournamentEvents) {
			evaluatePrices (event, eventEntries, eventsNotEntered, ageYears)
		}
		
		// combine the two lists
		def allEntries = []
		allEntries.addAll(eventEntries)
		allEntries.addAll(eventsNotEntered)

		// third evaluate rules for various conflicts
		return allEntries
	}
	
	/**
	 * 
	 * @param eventToEvaluate
	 * @param eventEntries
	 * @param eventsNotEntered
	 * @param eligibilityRating
	 * @param ageYears
	 * @param gender
	 * @return
	 */
	void evaluateConflicts (Event eventToEvaluate, List<EventEntry> eventEntries, List<EventEntry> eventsNotEntered, int eligibilityRating, int ageYears, String gender) {
		// ratings restrictions
		evaluateRatingsRules (eventToEvaluate, eventEntries, eventsNotEntered, eligibilityRating)
		// age restrictions
		evaluateAgeRules (eventToEvaluate, eventEntries, eventsNotEntered, ageYears)
		
		// gender restrictions
		Event.GenderRestriction playerGender = (gender == 'M') ? Event.GenderRestriction.MALE : Event.GenderRestriction.FEMALE
		evaluateGenderRules (eventToEvaluate, eventEntries, eventsNotEntered, playerGender)
		
	}
	
	/**
	 * Helper for finding event entry to be evaluated/marked
	 * 
	 * @param eventToEvaluate
	 * @param eventEntries
	 * @param eventsNotEntered
	 * @return
	 */
	private EventEntry findEventEntry (Event eventToEvaluate, List<EventEntry> eventEntries, List<EventEntry> eventsNotEntered) {
		// find event entry for this event
		EventEntry foundEventEntry = null
		for (eventEntry in eventEntries) {
			if (eventEntry.event.id == eventToEvaluate.id) {
				foundEventEntry = eventEntry
				break
			}
		}
		
		if (foundEventEntry == null) {
			for (eventEntry in eventsNotEntered) {
				if (eventEntry.event.id == eventToEvaluate.id) {
					foundEventEntry = eventEntry
					break
				}
			}
		}
		return foundEventEntry
	} 

	
	/**
	 * 
	 * @param eventToEvaluate
	 * @param eventEntries
	 * @param eventsNotEntered
	 * @param eligibilityRating
	 * @param ageYears
	 * @param gender
	 * @return
	 */
	void evaluateRatingsRules (Event eventToEvaluate, List<EventEntry> eventEntries, List<EventEntry> eventsNotEntered, int eligibilityRating) {
		// find event entry for this event
		EventEntry foundEventEntry = findEventEntry (eventToEvaluate, eventEntries, eventsNotEntered)
		if (foundEventEntry != null && 
			(foundEventEntry.availabilityStatus == EventEntry.AvailabilityStatus.AVAILABLE || foundEventEntry.availabilityStatus == EventEntry.AvailabilityStatus.ENTERED)) {
			if ((eventToEvaluate.maxPlayerRating != 0 && eligibilityRating > eventToEvaluate.maxPlayerRating) || 
				(eventToEvaluate.minPlayerRating != 0 && eligibilityRating < eventToEvaluate.minPlayerRating)) {
				//println ('wrong rating for player with rating ' + eligibilityRating + ' in event ' + eventToEvaluate.name)
				foundEventEntry.availabilityStatus = EventEntry.AvailabilityStatus.RATING
			}
		}
	}
	
	/**
	 * 
	 * @param eventToEvaluate
	 * @param eventEntries
	 * @param eventsNotEntered
	 * @param ageYears
	 */
	void evaluateAgeRules (Event eventToEvaluate, List<EventEntry> eventEntries, List<EventEntry> eventsNotEntered, int ageYears) {
		if (eventToEvaluate.maxPlayerAge != 0 || eventToEvaluate.minPlayerAge != 0) {
			// find event entry for this event
			EventEntry foundEventEntry = findEventEntry (eventToEvaluate, eventEntries, eventsNotEntered)
			if (foundEventEntry != null && 
				(foundEventEntry.availabilityStatus == EventEntry.AvailabilityStatus.AVAILABLE || foundEventEntry.availabilityStatus == EventEntry.AvailabilityStatus.ENTERED)) {
				if ((eventToEvaluate.maxPlayerAge != 0 && ageYears > eventToEvaluate.maxPlayerAge ) ||
					(eventToEvaluate.minPlayerAge != 0 && ageYears < eventToEvaluate.minPlayerAge)) {
					foundEventEntry.availabilityStatus = EventEntry.AvailabilityStatus.WRONG_AGE
				}
			}
		}
	}
	
	/**
	 * Checks if player is able to enter event given his gender
	 * @param eventToEvaluate
	 * @param eventEntries
	 * @param eventsNotEntered
	 * @param gender
	 */
	void evaluateGenderRules (Event eventToEvaluate, List<EventEntry> eventEntries, List<EventEntry> eventsNotEntered, Event.GenderRestriction playerGender) {
		if (eventToEvaluate.genderRestriction != Event.GenderRestriction.NONE) {
			EventEntry foundEventEntry = findEventEntry (eventToEvaluate, eventEntries, eventsNotEntered)
			if (foundEventEntry != null && 
				(foundEventEntry.availabilityStatus == EventEntry.AvailabilityStatus.AVAILABLE || foundEventEntry.availabilityStatus == EventEntry.AvailabilityStatus.ENTERED)) {
				if (eventToEvaluate.genderRestriction != playerGender) {
					foundEventEntry.availabilityStatus = EventEntry.AvailabilityStatus.WRONG_GENDER
				}
			}
		}
	}
	
	/**
	 * Evaluates if this event starts at the same time as other events
	 * @param eventToEvaluate
	 * @param eventEntries
	 * @param eventsNotEntered
	 */
	void evaluateTimeConflicts (EventEntry eventNotEntered, List<EventEntry> eventEntries) {
//		println "event to check time conflict " + eventNotEntered.event.name + ' has starting day ' + eventNotEntered.event.day + " time " + eventNotEntered.event.startTime
		// check if this event has conflict with any event that is already entered
		for (eventEntry in eventEntries) {
			// no other conflict was already marked
			if (eventNotEntered.availabilityStatus == EventEntry.AvailabilityStatus.AVAILABLE) {
				// on the same day
				if (eventNotEntered.event.day == eventEntry.event.day) {
					// starting time within half an hour of each other or less is considered time conflict
					// fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
//					println 'found not entry on the same day at ' + eventEntry.event.startTime
					if (Math.abs (eventNotEntered.event.startTime - eventEntry.event.startTime) <= 0.5) {
						eventNotEntered.availabilityStatus = EventEntry.AvailabilityStatus.TIME_CONFLICT
					}
				}
			}
		}
	}
	
	/**
	 * Set prices dependent on age
	 * @param evenToEvaluate
	 * @param eventEntries
	 * @param eventsNotEntered
	 */
	void evaluatePrices (Event eventToEvaluate, List<EventEntry> eventEntries, List<EventEntry> eventsNotEntered, int ageYears) {
		EventEntry foundEventEntry = findEventEntry (eventToEvaluate, eventEntries, eventsNotEntered)
		if (foundEventEntry != null) {
			if (ageYears < 18) {
				foundEventEntry.fee = (eventToEvaluate.feeJunior != 0) ? eventToEvaluate.feeJunior : eventToEvaluate.feeAdult
			} else {
			foundEventEntry.fee = eventToEvaluate.feeAdult
			}
		}
	}
}

