package com.atms

import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)

class TournamentController extends RestfulController {

	def tournamentService
	def tournamentEntryService

	static responseFormats = ['json', 'xml']
    static allowedMethods = [index: 'GET', save: "POST", update: "PUT", delete: "DELETE"]

	TournamentController () {
		super(Tournament, false)
	}

	TournamentController (boolean readOnly) {
		super(Tournament, readOnly)
	}

	@Secured(['permitAll'])
	def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
		def tournamentInstanceList = null
		if (params.containsKey('owned')) {
			tournamentInstanceList = tournamentService.listOwned(params)
		} else {
			tournamentInstanceList = tournamentService.list(params)
		}
        respond tournamentInstanceList, [status: OK]
    }
	
	/**
	 * To protect account sensitive data clear secret key before sending
	 * @param tournament
	 * @return
	 */
	def cloneAccounts (Tournament tournament) {
		def clonedAccounts = [] 
		if (tournament.accounts != null) {
			tournament.accounts.each {
				def accountClone = new Account()
				accountClone.id = it.id
				accountClone.stripeSecretKey = null
				accountClone.stripePublicKey = it.stripePublicKey
				accountClone.gatewayType = it.gatewayType
				accountClone.version = it.version
				accountClone.financialTransactions = it.financialTransactions
				clonedAccounts.add (accountClone)
			}
		}
		tournament.accounts = clonedAccounts
	}

    /**
     * Shows a single resource
     * @param id The id of the resource
     * @return The rendered resource or a 404 if it doesn't exist
     */
	@Secured(['permitAll'])
    def show() {
		long id = params.id as Long
		def tournament = tournamentService.show(id)
		cloneAccounts (tournament)
		
		// get count of entries (i.e. players) who entered the tournament
		tournament.entriesCount = tournamentEntryService.getTournamentEntriesCount(id)

		respond tournament
    }

    /**
     * Displays a form to create a new resource
     */
	@Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
    def create() {
        if(handleReadOnly()) {
            return
        }
        respond createResource()
    }

	@Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
    def save(Tournament tournamentInstance) {
        if (tournamentInstance == null) {
            render status: NOT_FOUND
            return
        }

        tournamentInstance.validate()
        if (tournamentInstance.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

		tournamentService.create(tournamentInstance, params)
        respond tournamentInstance, [status: CREATED]
    }

    @Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
    def update(Tournament tournamentInstance) {
        if (tournamentInstance == null) {
            render status: NOT_FOUND
            return
        }

        tournamentInstance.validate()
        if (tournamentInstance.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

		tournamentService.update(tournamentInstance, params)
        respond tournamentInstance, [status: OK]
    }

    @Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
    def delete(Tournament tournamentInstance) {

        if (tournamentInstance == null) {
            render status: NOT_FOUND
            return
        }

		tournamentService.delete tournamentInstance
        render status: NO_CONTENT
    }
}
