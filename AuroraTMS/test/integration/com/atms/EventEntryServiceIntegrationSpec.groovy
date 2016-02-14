package com.atms

import grails.test.spock.IntegrationSpec

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH

class EventEntryServiceIntegrationSpec extends IntegrationSpec {

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService

    def setup() {
    }

    def cleanup() {
		// logout
    }

    void "test something"() {
		given: 'Foo Controller'
        def fc = new EventEntryService()
		fc.aclPermissionFactory = aclPermissionFactory
		fc.aclService = aclService
		fc.aclUtilService = aclUtilService
		fc.springSecurityService = springSecurityService

        and: 'with authorized user'
        fc.springSecurityService = [authentication: [name: 'swavek']]
//		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
//			'swavek', 'swavek',
//			AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

        when: 'create is called'
		def eventEntry = new EventEntry()
		eventEntry.status = EventEntry.EntryStatus.PENDING
        fc.create(eventEntry, new HashMap())
		def count = fc.count()
		
        then: 'check redirect url and error message'
//		SCH.clearContext()
		count == 1
            
	}
}
