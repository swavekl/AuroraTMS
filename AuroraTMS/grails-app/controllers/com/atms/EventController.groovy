package com.atms

import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)
class EventController extends RestfulController {

	def eventService
	
	static responseFormats = ['json', 'xml']
	static allowedMethods = [index: 'GET', save: "POST", update: "PUT", delete: "DELETE"]

	EventController () {
		super(Event, false)
	}

	EventController (boolean readOnly) {
		super(Event, readOnly)
	}

	@Secured(['permitAll'])
	def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
		def eventList = eventService.list(params)
        respond eventList, [status: OK]
    }
	
	@Secured(['permitAll'])
	def show() {
		long id = params.id as Long
		respond eventService.show(id)
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
	def save(Event event) {
		if (event == null) {
			render status: NOT_FOUND
			return
		}

		event.validate()
		if (event.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		eventService.create(event, params)
		respond event, [status: CREATED]
	}

	@Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
	def update(Event event) {
		if (event == null) {
			render status: NOT_FOUND
			return
		}

		event.validate()
		if (event.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		eventService.update(event, params)
		respond event, [status: OK]
	}

	@Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
	def delete(Event event) {

		if (event == null) {
			render status: NOT_FOUND
			return
		}

		eventService.delete event
		render status: NO_CONTENT
	}
}
