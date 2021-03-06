package com.atms

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

import com.atms.utils.RegionStateInfo;

import grails.transaction.Transactional

@Transactional
class TournamentEntryService {

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService

	void addPermission(TournamentEntry tournamentEntry, String username, int permission) {
		addPermission tournamentEntry, username, aclPermissionFactory.buildFromMask(permission)
	}

//	@Transactional
	@PreAuthorize("hasPermission(#tournament, admin)")
	void addPermission(TournamentEntry tournamentEntry, String username, Permission permission) {
		aclUtilService.addPermission tournamentEntry, username, permission
	}

//	@Transactional
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

//	@Transactional
	@PreAuthorize("hasRole('ROLE_USER')")
	TournamentEntry create(TournamentEntry tournamentEntry, Map params) {
//		println 'Saving TournamentEntry...'
		tournamentEntry.save(flush: true)
//		println 'Saved TournamentEntry with id ' + tournamentEntry.id
//		println "granting ownership of tournament Entry to user " + springSecurityService.authentication.name
		def currentPrincipal = springSecurityService.authentication.name

		// Grant the current principal administrative permission
		addPermission tournamentEntry, currentPrincipal, BasePermission.ADMINISTRATION

		grantEntryPermissions (tournamentEntry, currentPrincipal)

		tournamentEntry = TournamentEntry.get(tournamentEntry.id)
	}

//	@Transactional
	@PreAuthorize("hasPermission(#tournamentEntry, write) or hasPermission(#tournamentEntry, admin)")
	void update(TournamentEntry tournamentEntry, Map params) {
		tournamentEntry.save(flush: true)
	}

//	@Transactional
	@PreAuthorize("hasPermission(#tournamentEntry, delete) or hasPermission(#tournamentEntry, admin)")
	void delete(TournamentEntry tournamentEntry) {
		tournamentEntry.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl tournamentEntry
	}

	void grantEntryPermissions (TournamentEntry tournamentEntry, String currentPrincipal) {
		// find tournament director who configured this tournament and grant him admin privileges on this entry
		def tdRole = SecRole.findByAuthority("ROLE_TOURNAMENT_DIRECTOR")
		def tournamentDirectors = SecUserSecRole.findAllBySecRole(tdRole).secUser
		tournamentDirectors.each {
			if (it.username != currentPrincipal) {
				//println 'granting access to TOURNAMENT_DIRECTOR" ' + it.username
				// check if this TD created it
				addPermission tournamentEntry, it.username, BasePermission.ADMINISTRATION
			}
		}

		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != currentPrincipal) {
				//println 'granting access to ADMIN ' + it.username
				addPermission tournamentEntry, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

	@PreAuthorize("hasPermission(#id, 'com.atms.TournamentEntry', read) or hasPermission(#id, 'com.atms.TournamentEntry', admin)")
	@Transactional(readOnly = true)
	TournamentEntry get(long id) {
		TournamentEntry.get id
	}

	// anyone can see a tournamentEntry
	@Transactional(readOnly = true)
	TournamentEntry show(long id) {
		TournamentEntry.get id
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	@Transactional(readOnly = true)
	List<TournamentEntry> listOwned(Map params) {
		//		println 'listOwned params ' + springSecurityService.authentication.name
		TournamentEntry.list params
	}

	@Transactional(readOnly = true)
	int count() {
		TournamentEntry.count()
	}

	// anybody can use it
	@Transactional(readOnly = true)
	List<TournamentEntry> list(Map params) {
		//		println "list for user " + (springSecurityService.authentication) ? springSecurityService.authentication.name : 'anonymous'
		//		println "list params " + params

		TournamentEntry.list params
	}
	
	@Transactional(readOnly = true)
	List<TournamentEntry> listForEventEntries(List<EventEntry> eventEntryList) {
		def tournamentEntryIds = []
		eventEntryList.each () { 
			tournamentEntryIds.add(it.tournamentEntry.id)
		}
		return TournamentEntry.findAllByIdInList(tournamentEntryIds)
	}
	
	/**
	 * Gets count of entries in a tournament
	 * @param tournamentId
	 * @return
	 */
	@Transactional(readOnly = true)
	int getTournamentEntriesCount (long tournamentId) {
		def c = TournamentEntry.createCriteria()
		return c.count {
			eq("tournament.id", tournamentId)
		}
	}
}

