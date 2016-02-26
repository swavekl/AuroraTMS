package com.atms

import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)

class TournamentEntryController extends RestfulController {

	def tournamentEntryService
	def userProfileService
	def eventEntryService

	static responseFormats = ['json', 'xml']
	static allowedMethods = [index: 'GET', save: "POST", update: "PUT", delete: "DELETE", patch: 'PATCH']

	TournamentEntryController () {
		super(TournamentEntry, false)
	}

	TournamentEntryController (boolean readOnly) {
		super(TournamentEntry, readOnly)
	}

	@Secured(['permitAll'])
	def index(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		def tournamentEntriesList = null
		if (params.containsKey('owned')) {
			tournamentEntriesList = tournamentEntryService.listOwned(params)
		} else {
			tournamentEntriesList = tournamentEntryService.list(params)
		}
		respond tournamentEntriesList, [status: OK]
	}

	/**
	 * Shows a single resource
	 * @param id The id of the resource
	 * @return The rendered resource or a 404 if it doesn't exist
	 */
	@Secured(['permitAll'])
	def show() {
		long id = params.id as Long
		def tournamentEntry = tournamentEntryService.show(id)
		respond tournamentEntry
	}

	/**
	 * Creates a new tournament entry without saving it
	 */
	@Transactional
	@Secured(['ROLE_USER'])
	def create() {
		if(handleReadOnly()) {
			return
		}
		def tournamentEntry = createResource()
		
		tournamentEntry.dateEntered = new Date();
		
//		long userId = params.userId as Long
//		tournamentEntry.userProfile = userId 

		// find the player's rating on the date specified in the tournament eligibility date
		tournamentEntry.eligibilityRating = 1532
		// find current rating
		tournamentEntry.seedRating = 1587
		
		respond tournamentEntry
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def save(TournamentEntry tournamentEntry) {
		if (tournamentEntry == null) {
			render status: NOT_FOUND
			return
		}

		tournamentEntry.validate()
		if (tournamentEntry.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		tournamentEntryService.create(tournamentEntry, params)
		respond tournamentEntry, [status: CREATED]
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def update(TournamentEntry tournamentEntry) {
		if (tournamentEntry == null) {
			render status: NOT_FOUND
			return
		}

		tournamentEntry.validate()
		if (tournamentEntry.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		tournamentEntryService.update(tournamentEntry, params)
		respond tournamentEntry, [status: OK]
	}


	@Transactional
	@Secured(['ROLE_USER'])
	def delete(TournamentEntry tournamentEntry) {
		if (tournamentEntry == null) {
			render status: NOT_FOUND
			return
		}

		tournamentEntryService.delete tournamentEntry
		render status: NO_CONTENT
	}
	
	@Transactional
	@Secured(['ROLE_USER'])
	def patch (TournamentEntry tournamentEntry) {
		//long tournamentEntryId = params.tournamentEntryId as Long
		long tournamentEntryId = tournamentEntry.id as Long
		eventEntryService.confirmEventEntries (tournamentEntryId)
		render status: OK
	}

}
