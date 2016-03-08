package com.atms

import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)
class AccountController extends RestfulController {

	def accountService
	
	static responseFormats = ['json', 'xml']
	static allowedMethods = [index: 'GET', save: "POST", update: "PUT", delete: "DELETE"]

	AccountController () {
		super(Account, false)
	}

	AccountController (boolean readOnly) {
		super(Account, readOnly)
	}

	@Secured(['permitAll'])
	def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
		def accountList = accountService.list(params)
        respond accountList, [status: OK]
    }
	
	@Secured(['permitAll'])
	def show() {
		long id = params.id as Long
		respond accountService.show(id)
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
	def save(Account account) {
		if (account == null) {
			render status: NOT_FOUND
			return
		}

		account.validate()
		if (account.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		accountService.create(account, params)
		respond account, [status: CREATED]
	}

	@Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
	def update(Account account) {
		if (account == null) {
			render status: NOT_FOUND
			return
		}

		account.validate()
		if (account.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		accountService.update(account, params)
		respond account, [status: OK]
	}

	@Transactional
	@Secured(['ROLE_TOURNAMENT_DIRECTOR'])
	def delete(Account account) {

		if (account == null) {
			render status: NOT_FOUND
			return
		}

		accountService.delete account
		render status: NO_CONTENT
	}
}
