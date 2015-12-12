package com.atms

import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)

class EventEntryController extends RestfulController {

	def eventEntryService

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
			tournamentEntriesList = eventEntryService.listOwned(params)
		} else {
			tournamentEntriesList = eventEntryService.list(params)
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
		def EventEntry = eventEntryService.show(id)
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
	def save(EventEntry eventEntry) {
		if (eventEntry == null) {
			render status: NOT_FOUND
			return
		}

		eventEntry.validate()
		if (eventEntry.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		eventEntryService.create(eventEntry, params)
		respond eventEntry, [status: CREATED]
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def update(EventEntry eventEntry) {
		if (eventEntry == null) {
			render status: NOT_FOUND
			return
		}

		eventEntry.validate()
		if (eventEntry.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		eventEntryService.update(eventEntry, params)
		respond eventEntry, [status: OK]
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def delete(EventEntry eventEntry) {

		if (eventEntry == null) {
			render status: NOT_FOUND
			return
		}

		eventEntryService.delete eventEntry
		render status: NO_CONTENT
	}
}
