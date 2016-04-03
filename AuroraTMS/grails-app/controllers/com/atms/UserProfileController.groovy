package com.atms

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import grails.transaction.Transactional;

import java.lang.invoke.MethodHandleImpl.BindCaller.T

import static org.springframework.http.HttpStatus.*

import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpStatus

import grails.converters.*
import groovy.json.JsonBuilder


@Secured(['ROLE_USER'])
class UserProfileController extends RestfulController {

	static responseFormats = ['json', 'xml']
	static allowedMethods = [index: "GET", save: "POST", update: "PUT", patch: "PATCH", delete: "DELETE"]
	
	def userProfileService

	UserProfileController () {
		super(UserProfile, false)
	}

	UserProfileController (boolean readOnly) {
		super(UserProfile, readOnly)
	}

	def index(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		respond listAllResources(params), model: [("${resourceName}Count".toString()): countResources()]
	}

	protected UserProfile queryForResource(Serializable id) {
		def result = null
		if (params.username != null) {
			def secuser = SecUser.findByUsername(params.username)
			if (secuser != null) {
				result = secuser.userProfile
			}

			if (result == null) {
				result = createResource()

				// if this is user profile for user without USATT membership
				// then assign a temporary number
				def createParamsMap = []
				if (params.memberId != null && params.memberId == '0') {
					result.usattID = userProfileService.computeTemporaryMemberId()
				}
			}
		} else {
			result = resource.get(id)
		}
		result
	}

	/**
	 * Updates a resource for the given id
	 * @param id
	 */
	@Transactional
	def patch() {
		if (params.username != null) {
			if(handleReadOnly()) {
				return
			}

			UserProfile instance = null
			def secuser = SecUser.findByUsername(params.username)
			if (secuser != null) {
				instance = UserProfile.get(params.id)
				if (instance != null) {
					secuser.userProfile = instance;
					secuser.save flush:true
				}
			}

			request.withFormat {
				form multipartForm {
					flash.message = message(code: 'default.updated.message', args: [message(code: "${resourceClassName}.label".toString(), default: resourceClassName), instance.id])
					redirect instance
				}
				'*'{
					response.addHeader(HttpHeaders.LOCATION,
							g.createLink(
							resource: this.controllerName, action: 'show',id: instance.id, absolute: true,
							namespace: hasProperty('namespace') ? this.namespace : null ))
					respond instance, [status: OK]
				}
			}
		} else {
			update()
		}
	}
}
