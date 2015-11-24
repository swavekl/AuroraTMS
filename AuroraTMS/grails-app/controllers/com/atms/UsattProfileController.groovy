package com.atms

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import grails.transaction.Transactional;

import java.io.Serializable;
import java.lang.invoke.MethodHandleImpl.BindCaller.T

import static org.springframework.http.HttpStatus.*

import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpStatus

import grails.converters.*
import groovy.json.JsonBuilder

@Secured(['ROLE_USER'])
class UsattProfileController extends RestfulController {

	static responseFormats = ['json', 'xml']
	static allowedMethods = [index: "GET", save: "POST", update: "PUT", patch: "PATCH", delete: "DELETE"]

	UsattProfileController () {
		super(UsattProfile, false)
	}

	UsattProfileController (boolean readOnly) {
		super(UsattProfile, readOnly)
	}

	def index(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		respond listAllResources(params), model: [("${resourceName}Count".toString()): countResources()]
	}
	
    protected List<T> listAllResources(Map params) {
		def c = resource.createCriteria ()
		String lastName = params.get('lastName')
		String firstName = params.get('firstName')
		String memberId = params.get('memberId')
		String max = params.get('max')
		String offset = params.get('offset');
		 
		if (lastName != "" || firstName != "") {
			return c.list (max:max, offset:offset) {
				ilike ('lastName', '%' + lastName + '%')
				or {
				ilike ('firstName', '%' + firstName + '%')
				}
				order ('firstName') 
			}
		} else if (memberId != "") {
		int mid = Integer.parseInt(memberId)	
		return c.list {
				eq ('memberId', mid)
			}
		}
        resource.list(params)
    }
}
