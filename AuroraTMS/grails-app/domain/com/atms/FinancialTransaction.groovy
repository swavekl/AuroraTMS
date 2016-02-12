package com.atms

class FinancialTransaction {

	// date & time of transaction
	Date createdDate
	
	// who entered this transaction
	String createdBy
	
	// amount in cents e.g. $23.45 is 2345
	long amount;
	
	// type of transaction (payment i.g. charge)
	enum Type {
		Charge, Refund
	}
	
	Type type
	
	
	enum PaymentMethod {
		CreditCard, Check, Cash, PayPal 
	}
	PaymentMethod paymentMethod
	
	// some identifier for this transaction e.g. Stripe token representing credit card, check number 
	String paymentMethodIdentifier
	
	// is owned by tournament entry but, and has reference back to it, has FK to tournament entry PK in db
	static belongsTo = [tournamentEntry: TournamentEntry]
	
	// transaction goes to one account
	Account account
	
    static constraints = {
//		createdDate blank: false
//		createdBy blank: false
//		type nullable: false
//		paymentMethod nullable: false
//		paymentMethodIdentifier blank: false, nullable: false
    }
}
