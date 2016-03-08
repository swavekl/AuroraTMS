package com.atms

import java.util.List;
import java.util.Map;

import grails.plugin.springsecurity.annotation.Secured

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

import grails.transaction.Transactional

class AccountService {

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService

	void addPermission(Account account, String username, int permission) {
		addPermission account, username, aclPermissionFactory.buildFromMask(permission)
	}

	@Transactional
	@PreAuthorize("hasPermission(#account, admin)")
	void addPermission(Account account, String username, Permission permission) {
		aclUtilService.addPermission account, username, permission
	}
	
	// anyone can see an account
	Account show(long id) {
		Account.get id
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_TOURNAMENT_DIRECTOR')")
	Account create(Account account, Map params) {
		account.save(flush: true)
		println "granting ownership of account to user " + springSecurityService.authentication.name
		// Grant the current principal administrative permission
		addPermission account, springSecurityService.authentication.name, BasePermission.ADMINISTRATION

		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission account, it.username, BasePermission.ADMINISTRATION
			}
		}

		account
	}
	
	//@PreAuthorize("hasRole('ROLE_USER')")
	//@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	//@Secured(['permitAll'])
	List<Account> list(Map params) {
		def tournamentId = params.tournamentId
		Account.where {
			tournament.id == tournamentId
		}.list(sort:'gatewayType')
	}

	int count() {
		Account.count()
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_TOURNAMENT_DIRECTOR')")
	//@PreAuthorize("hasPermission(#account, write) or hasPermission(#account, admin)")
	void update(Account account, Map params) {
		account.save(flush: true)
		
		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission account, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

	@Transactional
	@PreAuthorize("hasPermission(#account, delete) or hasPermission(#account, admin)")
	void delete(Account account) {
		account.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl account
	}

	@Transactional
	@PreAuthorize("hasPermission(#account, admin)")
	void deletePermission(Account account, String username, Permission permission) {
		def acl = aclUtilService.readAcl(account)

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
