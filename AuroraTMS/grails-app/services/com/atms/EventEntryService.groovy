package com.atms

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

import com.atms.utils.RegionStateInfo;

import grails.transaction.Transactional

@Transactional
class EventEntryService {

	static transactional = false

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService

	void addPermission(EventEntry eventEntry, String username, int permission) {
		addPermission eventEntry, username, aclPermissionFactory.buildFromMask(permission)
	}

	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, admin)")
	void addPermission(EventEntry eventEntry, String username, Permission permission) {
		aclUtilService.addPermission eventEntry, username, permission
	}

	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, admin)")
	void deletePermission(EventEntry eventEntry, String username, Permission permission) {
		def acl = aclUtilService.readAcl(eventEntry)

		// Remove all permissions associated with this particular
		// recipient (string equality to KISS)
		acl.entries.eachWithIndex { entry, i ->
			if (entry.sid.equals(recipient) && entry.permission.equals(permission)) {
				acl.deleteAce i
			}
		}

		aclService.updateAcl acl
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_USER')")
	EventEntry create(EventEntry eventEntry, Map params) {
		//		EventEntry eventEntryEntry = new eventEntry(params)
		EventEntry.save(flush: true)
		println "granting ownership of tournament Entry to user " + springSecurityService.authentication.name
		def currentPrincipal = springSecurityService.authentication.name
		
		// Grant the current principal administrative permission
		addPermission eventEntry, currentPrincipal, BasePermission.ADMINISTRATION
		
		grantAdminPermissions (eventEntry)

		eventEntry
	}
	
	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, write) or hasPermission(#eventEntry, admin)")
	void update(EventEntry eventEntry, Map params) {
		EventEntry.save(flush: true)
		
		grantEntryPermissions(eventEntry)
	}

	@Transactional
	@PreAuthorize("hasPermission(#eventEntry, delete) or hasPermission(#eventEntry, admin)")
	void delete(EventEntry eventEntry) {
		EventEntry.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl eventEntry
	}

	void grantEntryPermissions (EventEntry eventEntry) {
		// find tournament director who configured this tournament and grant him admin privileges on this entry
		def tdRole = SecRole.findByAuthority("ROLE_TOURNAMENT_DIRECTOR")
		def tournamentDirectors = SecUserSecRole.findAllBySecRole(tdRole).secUser
		tournamentDirectors.each {
			if (it.username != currentPrincipal) {
				println 'granting access to TOURNAMENT_DIRECTOR" ' + it.username
				// check if this TD created it
				addPermission eventEntry, it.username, BasePermission.ADMINISTRATION
			}
		}
		
		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != currentPrincipal) {
				println 'granting access to ADMIN ' + it.username
				addPermission eventEntry, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

	@PreAuthorize("hasPermission(#id, 'com.atms.TournamentEntry', read) or hasPermission(#id, 'com.atms.Tournament', admin)")
	EventEntry get(long id) {
		EventEntry.get id
	}

	// anyone can see a eventEntry
	EventEntry show(long id) {
		EventEntry.get id
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	List<EventEntry> listOwned(Map params) {
//		println 'listOwned params ' + springSecurityService.authentication.name
		EventEntry.list params
	}

	int count() {
		EventEntry.count()
	}

	// anybody can use it
	List<EventEntry> list(Map params) {
//		println "list for user " + (springSecurityService.authentication) ? springSecurityService.authentication.name : 'anonymous'
//		println "list params " + params

		EventEntry.list params
	}

}

