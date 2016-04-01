package com.atms



import static org.springframework.http.HttpStatus.*
import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

import com.stripe.model.Charge
import com.stripe.model.Refund
import com.stripe.exception.CardException;
import com.stripe.Stripe;

@Transactional (readOnly = true)
class FinancialTransactionController extends RestfulController {

	static responseFormats = ['json', 'xml']
    static allowedMethods = [index: 'GET', save: "POST", update: "PUT", delete: "DELETE"]

	def financialTransactionService

	FinancialTransactionController () {
		super(FinancialTransaction, false)
	}

	FinancialTransactionController (boolean readOnly) {
		super(FinancialTransaction, readOnly)
	}

	@Secured(['ROLE_USER'])
	def index(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		respond financialTransactionService.list(params), [status: OK]
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def save(FinancialTransaction financialTransactionInstance) {
		if (financialTransactionInstance == null) {
			render status: NOT_FOUND
			return
		}

		financialTransactionInstance.validate()
		if (financialTransactionInstance.hasErrors()) {
			def errors = ['errors']
			financialTransactionInstance.errors.allErrors.each {
				def defaultMsg = it.defaultMessage
				def errorMsg = message(code: it, args: it.arguments)
				println errorMsg
				errors.add(errorMsg)
			}
			respond errors, status: NOT_ACCEPTABLE
			return
		}

		try {
			def apiKey = null
			def tournamentEntry = financialTransactionInstance.tournamentEntry
			if (tournamentEntry != null) {
				//tournamentEntry = tournamentEntry.merge()
				if (tournamentEntry.tournament != null)
					tournamentEntry.tournament.accounts.each {
						apiKey = it.stripeSecretKey
					}
			}
			if (apiKey != null) {
				// set key identifying account
				Stripe.apiKey = apiKey

				if (financialTransactionInstance.type == FinancialTransaction.Type.Charge) {
					// Use Stripe's library to make requests...
					Map<String, Object> chargeParams = new HashMap<String, Object>();
					chargeParams.put("amount", financialTransactionInstance.amount);
					chargeParams.put("currency", "usd");
					chargeParams.put("source", financialTransactionInstance.paymentMethodIdentifier) // obtained with Stripe.js
					chargeParams.put("description", "Charge for entering tournament");
					Map<String, String> initialMetadata = new HashMap<String, String>();
					initialMetadata.put("order_id", "6735");
					chargeParams.put("metadata", initialMetadata);

					def chargeResponse = Charge.create(chargeParams);
					financialTransactionInstance.stripeChargeIdentifier = chargeResponse.id
					financialTransactionInstance.createdDate = new Date();

					financialTransactionService.create(financialTransactionInstance)
					respond financialTransactionInstance, [status: CREATED]
					return
				} else if (financialTransactionInstance.type == FinancialTransaction.Type.Refund) {
					// this could be a partial refund or full refund of multiple charges
					// figure out how many refund transactions it will take to refund the requested amount
					long amountToRefund = Math.abs (financialTransactionInstance.amount) as Long
					long tournamentEntryId = tournamentEntry.id as Long
					List<FinancialTransaction> refundToIssueList = financialTransactionService.makeRefundTransactions (tournamentEntryId, amountToRefund)
					def refundIdentifiers = "";
					refundToIssueList.each { it
							FinancialTransaction refundTransaction = it as FinancialTransaction
							Map<String, Object> refundParams = new HashMap<String, Object>();
							refundParams.put("charge", refundTransaction.stripeChargeIdentifier);
							refundParams.put("amount", refundTransaction.amount);
							//refundParams.put("currency", "usd");
							def refundResponse = Refund.create(refundParams);
							
							// record the refund
							refundTransaction.refundedAmount = refundResponse.amount
							refundTransaction.stripeRefundIdentifier = refundResponse.id
							refundTransaction.paymentMethod = financialTransactionInstance.paymentMethod
							refundTransaction.paymentMethodIdentifier = "non_null_paymentMethodIdentifier";
							refundTransaction.account = financialTransactionInstance.account
							refundTransaction.tournamentEntry = financialTransactionInstance.tournamentEntry;
							refundTransaction.validate()
							if (!refundTransaction.hasErrors()) {
								financialTransactionService.create(refundTransaction)
								
								// make a string with many refund identifiers because we can't return an array of refunds from 'save' method
								refundIdentifiers = (refundIdentifiers == "") ? refundTransaction.stripeRefundIdentifier : ", " + refundTransaction.stripeRefundIdentifier
							} else {
								refundTransaction.getErrors().each {
									println "error: " + it
//									def defaultMsg = it.defaultMessage
//									def errorMsg = message(code: it, args: it.arguments)
//									println errorMsg
								}
							}
					}
					// return one transaction with many refund identifiers 
					financialTransactionInstance.stripeRefundIdentifier = refundIdentifiers
					respond financialTransactionInstance, [status: CREATED]
					return
				}
			}
		} catch (CardException e) {
			// Since it's a decline, CardException will be caught
			//		System.out.println("Status is: " + e.getCode());
			//		System.out.println("Message is: " + e.getMessage());
			def errors = ['errors', e.getCode(), e.getMessage()]
			respond errors, [status: NOT_ACCEPTABLE]
//			return
			//	  } catch (RateLimitException e) {
			//		// Too many requests made to the API too quickly
			//	  } catch (InvalidRequestException e) {
			//		// Invalid parameters were supplied to Stripe's API
			//	  } catch (AuthenticationException e) {
			//		// Authentication with Stripe's API failed
			//		// (maybe you changed API keys recently)
			//	  } catch (APIConnectionException e) {
			//		// Network communication with Stripe failed
			//	  } catch (StripeException e) {
			//		// Display a very generic error to the user, and maybe send
			//		// yourself an email
		} catch (Exception e) {
			// Something else happened, completely unrelated to Stripe
		println "e " + e.getMessage();
			def errors = ['errors', e.getCode(), e.getMessage()]
			respond errors, [status: NOT_ACCEPTABLE]
//			return
		}
		render status: NOT_ACCEPTABLE
		return
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def update(FinancialTransaction financialTransactionInstance) {
		if (financialTransactionInstance == null) {
			render status: NOT_FOUND
			return
		}

		financialTransactionInstance.validate()
		if (financialTransactionInstance.hasErrors()) {
			render status: NOT_ACCEPTABLE
			return
		}

		financialTransactionService.update(financialTransactionInstance)
		respond financialTransactionInstance, [status: OK]
	}

	@Transactional
	@Secured(['ROLE_USER'])
	def delete(FinancialTransaction financialTransactionInstance) {

		if (financialTransactionInstance == null) {
			render status: NOT_FOUND
			return
		}

		financialTransactionService.delete(financialTransactionInstance)
		render status: NO_CONTENT
	}
}
