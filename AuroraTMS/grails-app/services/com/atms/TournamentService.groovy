package com.atms

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

import com.atms.utils.RegionStateInfo;

import grails.transaction.Transactional

@Transactional
class TournamentService {

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService

	void addPermission(Tournament tournament, String username, int permission) {
		addPermission tournament, username, aclPermissionFactory.buildFromMask(permission)
	}

//	@Transactional
	@PreAuthorize("hasPermission(#tournament, admin)")
	void addPermission(Tournament tournament, String username, Permission permission) {
		aclUtilService.addPermission tournament, username, permission
	}

//	@Transactional
	@PreAuthorize("hasRole('ROLE_TOURNAMENT_DIRECTOR')")
	Tournament create(Tournament tournament, Map params) {
		//		Tournament tournament = new Tournament(params)
		tournament.save(flush: true)
		println "granting ownership to user " + springSecurityService.authentication.name
		// Grant the current principal administrative permission
		addPermission tournament, springSecurityService.authentication.name, BasePermission.ADMINISTRATION

		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission tournament, it.username, BasePermission.ADMINISTRATION
			}
		}

		tournament
	}

	@PreAuthorize("hasPermission(#id, 'com.atms.Tournament', read) or hasPermission(#id, 'com.atms.Tournament', admin)")
	@Transactional(readOnly = true)
	Tournament get(long id) {
		Tournament.get id
	}

	// anyone can see a tournament
	@Transactional(readOnly = true)
	Tournament show(long id) {
		Tournament.get id
	}

	@PreAuthorize("hasRole('ROLE_TOURNAMENT_DIRECTOR')")
	@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	@Transactional(readOnly = true)
	List<Tournament> listOwned(Map params) {
		Tournament.list params
	}

	@Transactional(readOnly = true)
	int count() {
		Tournament.count()
	}

	// anybody can use it
	@Transactional(readOnly = true)
	List<Tournament> list(Map params) {
//		println "list for user " + (springSecurityService.authentication) ? springSecurityService.authentication.name : 'anonymous'
//		println "list params " + params

		def region = params.get('region')
		// get current USATT region from session
		// translate it to state abbreviations

		def currentUsername = params.get('username')
		if (currentUsername != '' && region == '') {
//			println 'Searching by user name default region for user ' + currentUsername
			// if some user is logged in
			// get current user profile and the state from it
			def query = SecUser.where {username == currentUsername}
			SecUser currentUser = query.get ()
			if (currentUser != null) {
				// now fetch his profile and get state
				def state = currentUser.userProfile.state;
				// translate to region
				region = RegionStateInfo.lookupRegionByState (state)
			}
		}

		def statesList = null
		if (region != '') {
			def regionToStateMap = RegionStateInfo.getRegionStatesMap();
			statesList = regionToStateMap.getAt(region)
		} else {
			region = 'All regions'
		}
		
		if (statesList != null) {
			def offset = params.get('offset')
			def max = params.get('max')
			Tournament.executeQuery(
					"from Tournament where state in (:statesList) order by state, startDate",
					[statesList: statesList], [offset:offset, max:max])
		} else {
			Tournament.list params
		}
	}

//	@Transactional
	@PreAuthorize("hasPermission(#tournament, write) or hasPermission(#tournament, admin)")
	void update(Tournament tournament, Map params) {
		tournament.save(flush: true)
		
		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission tournament, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

//	@Transactional
	@PreAuthorize("hasPermission(#tournament, delete) or hasPermission(#tournament, admin)")
	void delete(Tournament tournament) {
		tournament.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl tournament
	}

//	@Transactional
	@PreAuthorize("hasPermission(#tournament, admin)")
	void deletePermission(Tournament tournament, String username, Permission permission) {
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
}
