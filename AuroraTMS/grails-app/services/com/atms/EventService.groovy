package com.atms

import java.util.List;
import java.util.Map;

import grails.plugin.springsecurity.annotation.Secured

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

import grails.transaction.Transactional

class EventService {

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService

	void addPermission(Event event, String username, int permission) {
		addPermission event, username, aclPermissionFactory.buildFromMask(permission)
	}

	@Transactional
	@PreAuthorize("hasPermission(#event, admin)")
	void addPermission(Event event, String username, Permission permission) {
		aclUtilService.addPermission event, username, permission
	}
	
	// anyone can see an event
	Event show(long id) {
		Event.get id
	}

		@Transactional
	@PreAuthorize("hasRole('ROLE_TOURNAMENT_DIRECTOR')")
	Event create(Event event, Map params) {
		event.save(flush: true)
		println "granting ownership of event to user " + springSecurityService.authentication.name
		// Grant the current principal administrative permission
		addPermission event, springSecurityService.authentication.name, BasePermission.ADMINISTRATION

		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission event, it.username, BasePermission.ADMINISTRATION
			}
		}

		event
	}
	
//	@PreAuthorize("hasRole('ROLE_TOURNAMENT_DIRECTOR')")
//	@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	//@Secured(['permitAll'])
	List<Event> list(Map params) {
		def tournamentId = params.tournamentId
		Event.where {
			tournament.id == tournamentId
		}.list()
	}

	int count() {
		Event.count()
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_TOURNAMENT_DIRECTOR')")
	//@PreAuthorize("hasPermission(#event, write) or hasPermission(#event, admin)")
	void update(Event event, Map params) {
		event.save(flush: true)
		
		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission event, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#event, delete) or hasPermission(#event, admin)")
	void delete(Event event) {
		event.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl event
	}

	@Transactional
	@PreAuthorize("hasPermission(#event, admin)")
	void deletePermission(Event event, String username, Permission permission) {
		def acl = aclUtilService.readAcl(event)

		// Remove all permissions associated with this particular
		// recipient (string equality to KISS)
		acl.entries.eachWithIndex { entry, i ->
			if (entry.sid.equals(recipient) && entry.permission.equals(permission)) {
				acl.deleteAce i
			}
		}

		aclService.updateAcl acl
	}


}
