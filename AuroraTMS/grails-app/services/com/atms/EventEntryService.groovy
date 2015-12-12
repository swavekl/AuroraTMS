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

	void addPermission(TournamentEntry tournamentEntry, String username, int permission) {
		addPermission tournamentEntry, username, aclPermissionFactory.buildFromMask(permission)
	}

	@Transactional
	@PreAuthorize("hasPermission(#tournament, admin)")
	void addPermission(TournamentEntry tournamentEntry, String username, Permission permission) {
		aclUtilService.addPermission tournamentEntry, username, permission
	}

	@Transactional
	@PreAuthorize("hasPermission(#tournament, admin)")
	void deletePermission(TournamentEntry tournamentEntry, String username, Permission permission) {
		def acl = aclUtilService.readAcl(tournament)

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
	TournamentEntry create(TournamentEntry tournamentEntry, Map params) {
		//		TournamentEntry tournamentEntryEntry = new tournamentEntry(params)
		tournamentEntry.save(flush: true)
		println "granting ownership of tournament Entry to user " + springSecurityService.authentication.name
		def currentPrincipal = springSecurityService.authentication.name
		
		// Grant the current principal administrative permission
		addPermission tournamentEntry, currentPrincipal, BasePermission.ADMINISTRATION
		
		grantAdminPermissions (tournamentEntry)

		tournamentEntry
	}
	
	@Transactional
	@PreAuthorize("hasPermission(#tournamentEntry, write) or hasPermission(#tournamentEntry, admin)")
	void update(TournamentEntry tournamentEntry, Map params) {
		tournamentEntry.save(flush: true)
		
		grantEntryPermissions(tournamentEntry)
	}

	@Transactional
	@PreAuthorize("hasPermission(#tournament, delete) or hasPermission(#tournament, admin)")
	void delete(TournamentEntry tournamentEntry) {
		tournamentEntry.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl tournamentEntry
	}

	void grantEntryPermissions (TournamentEntry tournamentEntry) {
		// find tournament director who configured this tournament and grant him admin privileges on this entry
		def tdRole = SecRole.findByAuthority("ROLE_TOURNAMENT_DIRECTOR")
		def tournamentDirectors = SecUserSecRole.findAllBySecRole(tdRole).secUser
		tournamentDirectors.each {
			if (it.username != currentPrincipal) {
				println 'granting access to TOURNAMENT_DIRECTOR" ' + it.username
				// check if this TD created it
				addPermission tournamentEntry, it.username, BasePermission.ADMINISTRATION
			}
		}
		
		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != currentPrincipal) {
				println 'granting access to ADMIN ' + it.username
				addPermission tournamentEntry, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

	@PreAuthorize("hasPermission(#id, 'com.atms.TournamentEntry', read) or hasPermission(#id, 'com.atms.Tournament', admin)")
	TournamentEntry get(long id) {
		tournamentEntry.get id
	}

	// anyone can see a tournamentEntry
	TournamentEntry show(long id) {
		tournamentEntry.get id
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	List<TournamentEntry> listOwned(Map params) {
//		println 'listOwned params ' + springSecurityService.authentication.name
		tournamentEntry.list params
	}

	int count() {
		tournamentEntry.count()
	}

	// anybody can use it
	List<TournamentEntry> list(Map params) {
//		println "list for user " + (springSecurityService.authentication) ? springSecurityService.authentication.name : 'anonymous'
//		println "list params " + params

		tournamentEntry.list params
	}

}

