package com.atms



import static org.springframework.http.HttpStatus.*
//import grails.rest.RestfulController;
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

import com.stripe.model.Charge
import com.stripe.exception.CardException;
import com.stripe.Stripe;

@Transactional(readOnly = true)
class FinancialTransactionController {

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
	
	def financialTransactionService
	
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
			financialTransactionInstance.errors.allErrors.each {
				def defaultMsg = it.defaultMessage
				def errorMsg = message(code: it, args: it.arguments)
				errors.add(errorMsg)
				}
            render status: NOT_ACCEPTABLE
            return
        }
		
		try {
			def apiKey = null
			def tournamentEntry = financialTransactionInstance.tournamentEntry
			if (tournamentEntry != null) {
				if (tournamentEntry.tournament != null)
				tournamentEntry.tournament.accounts.each {
					apiKey = it.stripeSecretKey
				}
			}
			if (apiKey != null) {
				// set key identifying account
				Stripe.apiKey = apiKey
				
				// Use Stripe's library to make requests...
				Map<String, Object> chargeParams = new HashMap<String, Object>();
				chargeParams.put("amount", financialTransactionInstance.amount);
				chargeParams.put("currency", "usd");
				chargeParams.put("source", financialTransactionInstance.paymentMethodIdentifier) // obtained with Stripe.js
				chargeParams.put("description", "Charge for entering tournament");
				Map<String, String> initialMetadata = new HashMap<String, String>();
				initialMetadata.put("order_id", "6735");
				chargeParams.put("metadata", initialMetadata);
				
				Charge.create(chargeParams);
				
				//financialTransactionInstance.save flush:true
				financialTransactionService.create(financialTransactionInstance)
				respond financialTransactionInstance, [status: CREATED]
			}
	  } catch (CardException e) {
		// Since it's a decline, CardException will be caught
		System.out.println("Status is: " + e.getCode());
		System.out.println("Message is: " + e.getMessage());
		def errors = ['errors', e.getCode(), e.getMessage()]
		respond errors, [status: NOT_ACCEPTABLE]
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

        //financialTransactionInstance.save flush:true
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

        //financialTransactionInstance.delete flush:true
		financialTransactionService.delete(financialTransactionInstance)
        render status: NO_CONTENT
    }
}
