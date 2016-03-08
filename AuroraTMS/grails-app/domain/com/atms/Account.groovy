package com.atms

/**
 * Account to/from which the payments/refunds are flowing e.g. Stripe account
 * @author Swavek
 *
 */
class Account {
	
	enum GatewayType {
		Stripe, PayPal
	}
	GatewayType gatewayType
	
	// public key to use when generating secure tokens for protecting credit cards
	String stripePublicKey
	// secret key used when submitting charges/refund from the server - identifies the Stripe account
	String stripeSecretKey

	// is owned by tournament entry (but doesn't need reference back to it)
	static belongsTo = [tournament:Tournament]
	
	// is associated with many transactions
	static hasMany = [financialTransactions: FinancialTransaction]
	
    static constraints = {
		gatewayType nullable: false
		stripePublicKey blank : false
		stripeSecretKey blank: false
    }
}
