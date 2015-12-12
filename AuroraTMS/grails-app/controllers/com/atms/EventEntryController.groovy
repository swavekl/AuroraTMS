package com.atms

import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)

class EventEntryController extends RestfulController {

	def EventEntryService

	static responseFormats = ['json', 'xml']
	static allowedMethods = [index: 'GET', save: "POST", update: "PUT", delete: "DELETE"]

	EventEntryController () {
		super(EventEntry, false)
	}

	EventEntryController (boolean readOnly) {
		super(EventEntry, readOnly)
	}

	@Secured(['permitAll'])
	def index(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		def tournamentEntriesList = null
		if (params.containsKey('owned')) {
			tournamentEntriesList = EventEntryService.listOwned(params)
		} else {
			tournamentEntriesList = EventEntryService.list(params)
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
		def EventEntry = EventEntryService.show(id)
		respond EventEntry
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
		respond createResource()
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def save(EventEntry tournamenEntry) {
		if (tournamenEntry == null) {
			render status: NOT_FOUND
			return
		}

		tournamenEntry.validate()
		if (tournamenEntry.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		EventEntryService.create(tournamenEntry, params)
		respond tournamenEntry, [status: CREATED]
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def update(EventEntry tournamenEntry) {
		if (tournamenEntry == null) {
			render status: NOT_FOUND
			return
		}

		tournamenEntry.validate()
		if (tournamenEntry.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		EventEntryService.update(tournamenEntry, params)
		respond tournamenEntry, [status: OK]
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def delete(EventEntry tournamenEntry) {

		if (tournamenEntry == null) {
			render status: NOT_FOUND
			return
		}

		EventEntryService.delete tournamenEntry
		render status: NO_CONTENT
	}
}
