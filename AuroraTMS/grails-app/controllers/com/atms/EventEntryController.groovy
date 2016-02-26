package com.atms

import static org.springframework.http.HttpStatus.*

import com.sun.org.apache.xalan.internal.xsltc.compiler.ForEach;

import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)

class EventEntryController extends RestfulController {

	def eventEntryService
	def eventService
	def userProfileService

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
		List eventEntries = []
		if (params.containsKey('tournamentId') && params.get('tournamentId') != null) {
			long tournamentId = params.tournamentId as Long
			long tournamentEntryId = params.tournamentEntryId as Long
			eventEntries = eventEntryService.listAllStatus(tournamentId, tournamentEntryId)
		} else if (params.containsKey('owned')) {
			eventEntries = eventEntryService.listOwned(params)
		// check which events are available etc
		} else {
			eventEntries = eventEntryService.list(params)
		}

		def eventEntryInfos = []
		eventEntries.each {
			def eventEntryInfo = new EventEntryInfo()
			eventEntryInfo.eventEntry = it
			eventEntryInfo.availabilityStatus = it.availabilityStatus as String
			eventEntryInfo.fee = it.fee
			eventEntryInfos.push(eventEntryInfo)
		}

		respond eventEntryInfos, [status: OK]
	}
	
	/**
	 * Shows a single resource
	 * @param id The id of the resource
	 * @return The rendered resource or a 404 if it doesn't exist
	 */
	@Secured(['permitAll'])
	def show() {
		long id = params.id as Long
		def eventEntry = eventEntryService.show(id)
		respond eventEntry
	}

	/**
	 * Creates a new event entry
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

		if (eventEntryService.create(eventEntry, params) == null) {
			render status: NOT_ACCEPTABLE
		}
		
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
