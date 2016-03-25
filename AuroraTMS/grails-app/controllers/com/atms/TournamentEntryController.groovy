package com.atms

import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured
import com.atms.utils.USATTDataSource

@Transactional(readOnly = true)

class TournamentEntryController extends RestfulController {

	def tournamentEntryService
	def userProfileService
	def eventEntryService
	def usattRatingsHistoryService
	def tournamentService

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

	@Transactional
	@Secured(['ROLE_USER'])
    def edit() {
        if(handleReadOnly()) {
            return
        }
        respond queryForResource(params.id)
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
		
		// find user profile 
		long userProfileId = params.userProfileId as Long
		def userProfile = userProfileService.get (userProfileId)
		int memberId = (userProfile != null) ? userProfile.usattID : 0
		// find players latest rating
		int latestRating = (memberId != 0) ? USATTDataSource.getPlayerRatingById(memberId) : 0
		
		// find tournament information to get ratingCutoffDate date
		int tournamentId = params.tournamentId as Integer
		def tournament = tournamentService.get(tournamentId)
		Date ratingCutoffDate = (tournament != null) ? tournament.ratingCutoffDate : new Date()

		// find the player's rating on the date specified in the tournament ratingCutoffDate date
		tournamentEntry.eligibilityRating = usattRatingsHistoryService.findLatestRatingAsOf (memberId, ratingCutoffDate)

		// just in case we don't have it just use the latest if we have it
		tournamentEntry.eligibilityRating = (tournamentEntry.eligibilityRating == 0) ? latestRating : tournamentEntry.eligibilityRating
		
		// save current rating for seeding purposes - can be different if player played in tournaments after ratingCutoffDate date
		tournamentEntry.seedRating = latestRating
		
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
		println "te is attached " + tournamentEntry.attached
		
		long tournamentEntryId = tournamentEntry.id as Long
		eventEntryService.confirmEventEntries (tournamentEntryId)
		render status: OK
	}

}
