package com.atms

import java.util.List;
import java.util.Map;

import grails.plugin.springsecurity.annotation.Secured

import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission


import grails.transaction.Transactional

@Transactional
class FinancialTransactionService {

	def aclPermissionFactory
	def aclService
	def aclUtilService
	def springSecurityService

	void addPermission(FinancialTransaction financialTransaction, String username, int permission) {
		addPermission financialTransaction, username, aclPermissionFactory.buildFromMask(permission)
	}

	@PreAuthorize("hasPermission(#financialTransaction, admin)")
	void addPermission(FinancialTransaction financialTransaction, String username, Permission permission) {
		aclUtilService.addPermission financialTransaction, username, permission
	}
	
	@Transactional (readOnly = true)
	@PreAuthorize("hasRole('ROLE_USER')")
//	@PreAuthorize("hasPermission(#financialTransaction, read) or hasPermission(#financialTransaction, admin)")
	FinancialTransaction show(long id) {
		FinancialTransaction.get id
	}

	//@Transactional
	@PreAuthorize("hasRole('ROLE_USER')")
	FinancialTransaction create(FinancialTransaction financialTransaction) {
		financialTransaction.save(flush: true)
		println "granting ownership of financialTransaction to user " + springSecurityService.authentication.name
		// Grant the current principal administrative permission
		addPermission financialTransaction, springSecurityService.authentication.name, BasePermission.ADMINISTRATION

		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission financialTransaction, it.username, BasePermission.ADMINISTRATION
			}
		}

		financialTransaction
	}
	
	/**
	 * List all transactions associated with a tournament entry (user) or all transactions
	 * @param params
	 * @return
	 */
	@Transactional (readOnly = true)
	@PreAuthorize("hasRole('ROLE_USER')")
	@PostFilter("hasPermission(filterObject, read) or hasPermission(filterObject, admin)")
	List<FinancialTransaction> list(Map params) {
		
		if (params.containsKey('tournamentEntryId')) {
			def tournamentEntryId = params.tournamentEntryId
			FinancialTransaction.where {
				tournamentEntry.id == tournamentEntryId
			}.list(sort:'createdDate')
		} else if (params.containsKey('tournamentId')) {
			def tournamentId = params.tournamentId
//			FinancialTransaction.where {
//				tournament.id == tournamentId
//			}.list(sort:'createdDate')
		}
	}

	@Transactional (readOnly = true)
	int count() {
		FinancialTransaction.count()
	}
	
//	@Transactional
//	@PreAuthorize("hasRole('ROLE_USER')")
	@PreAuthorize("hasPermission(#financialTransaction, write) or hasPermission(#financialTransaction, admin)")
	void update(FinancialTransaction financialTransaction) {
		financialTransaction.save(flush: true)
		
		def adminRole = SecRole.findByAuthority("ROLE_ADMIN")
		def admins = SecUserSecRole.findAllBySecRole(adminRole).secUser
		admins.each {
			if (it.username != springSecurityService.authentication.name) {
				println 'granting access to ADMIN ' + it.username
				addPermission financialTransaction, it.username, BasePermission.ADMINISTRATION
			}
		}
	}

//	@Transactional
	@PreAuthorize("hasPermission(#financialTransaction, delete) or hasPermission(#financialTransaction, admin)")
	void delete(FinancialTransaction financialTransaction) {
		financialTransaction.delete()

		// Delete the ACL information as well
		aclUtilService.deleteAcl financialTransaction
	}

//	@Transactional
	@PreAuthorize("hasPermission(#financialTransaction, admin)")
	void deletePermission(FinancialTransaction financialTransaction, String username, Permission permission) {
		def acl = aclUtilService.readAcl(financialTransaction)

		// Remove all permissions associated with this particular
		// recipient (string equality to KISS)
		acl.entries.eachWithIndex { entry, i ->
			if (entry.sid.equals(recipient) && entry.permission.equals(permission)) {
				acl.deleteAce i
			}
		}

		aclService.updateAcl acl
	}
	
	/**
	 * Make a list of refund transactions (there may be partial refunds of partially refunded charges)
	 * @param tournamentEntryId
	 * @param amountToRefund
	 * @return
	 */
	@Transactional (readOnly = true)
	@PreAuthorize("hasPermission(#financialTransaction, read) or hasPermission(#financialTransaction, admin)")
	List<FinancialTransaction> makeRefundTransactions (long tournamentEntryId, long amountToRefund) {
		
		def transactionIdToAmountMap = [:]

				// get all transactions made by this user
		def allTransactions = FinancialTransaction.where {
			tournamentEntry.id == tournamentEntryId
		}.list(sort:'createdDate')
		// prepare transactions which will amount to amountToRefund

		// first collect all charges and their original amounts
		allTransactions.each {
			FinancialTransaction financialTransaction = it as FinancialTransaction
			if (financialTransaction.type == FinancialTransaction.Type.Charge) {
				transactionIdToAmountMap[financialTransaction.stripeChargeIdentifier] = financialTransaction.amount
			}
		}
		
		// then subtract refunds from each of charge transaction to get amount still available for refund for each charge transaction
		allTransactions.each {
			FinancialTransaction financialTransaction = it as FinancialTransaction
			if (financialTransaction.type == FinancialTransaction.Type.Refund) {
				// whatever amount is still available for refund from this transaction
				def remainingAmountAvailableForRefund = transactionIdToAmountMap [financialTransaction.stripeChargeIdentifier]
				if (remainingAmountAvailableForRefund != null) {
					long amountAvailable = remainingAmountAvailableForRefund as Long
					amountAvailable -= financialTransaction.refundedAmount
					transactionIdToAmountMap[financialTransaction.stripeChargeIdentifier] = amountAvailable
				}
			}
		}
		
		// make refund transactions using amounts available for refund from each transaction
		List<FinancialTransaction> refundTransactions = []
		transactionIdToAmountMap.each { key, value -> 
			String stripeChargeIdentifier = key as String
			long amountAvailableForRefund = value as Long
			if (amountToRefund >= amountAvailableForRefund) {
				amountToRefund -= amountAvailableForRefund
				
				FinancialTransaction refundTransaction = new FinancialTransaction()
				refundTransaction.createdDate = new Date()
				refundTransaction.createdBy = springSecurityService.authentication.name
				refundTransaction.amount = amountAvailableForRefund
				refundTransaction.refundedAmount = amountAvailableForRefund
				refundTransaction.type = FinancialTransaction.Type.Refund
				// original credit card charge transaction
				refundTransaction.stripeChargeIdentifier = stripeChargeIdentifier
				refundTransactions.add(refundTransaction)
			}
		}		
		return refundTransactions
	}
}
