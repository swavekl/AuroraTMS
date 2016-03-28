package com.atms

import grails.rest.*
// pivotal video - RESTful Grails 2 
// https://www.youtube.com/watch?v=8xYi9n0pYFs
//@Resource(uri='/tournaments', formats=['json', 'xml'])
class Tournament {
	String name
	// name of the venue
	String venue
	String address
	String city
	String state
	int starLevel
	Date startDate
	Date endDate
	String websiteURL
	String blankEntryFormURL
	
	// eligibility ratings date 
	// use rating as of this date to qualify for rating restricted events
	Date ratingCutoffDate
	// start charging late entry fees after this date
	Date lateEntryStartDate
	// entries close after this date
	Date entryCutoffDate
	// date after which refunds are not offered
	Date refundCutoffDate
	
	// contact information
	String contactName
	String contactEmail
	String contactPhone
	
//	// payment information
//	String checkPayableTo
//	String checkMailAddress
//	String checkMailCity
//	String checkMailState
//	String checkMailZipCode
	
	int tablesCount
	
	// fee for processing entry
	float adminFee
	float lateEntryFee
	
//	// e.g. By each event (default), by number of events played, by both, by lesser amount
//	int eventFeesCalcMethod
//	// standard round robin fees 
//	double stdAdultRRFee
//	double stdJuniorRRFee
//	// standard single elimination fees
//	double stdAdultSEFee
//	double stdJuniorSEFee
	
	// stripe key used for charging/refunding
	String stripeKey

	// count of players who entered a tournament - added here so we can send it back when requested
	int entriesCount

	// tournament owns events, tournamentEntries, and multiple payment accounts
	static hasMany = [events:Event, tournamentEntries: TournamentEntry, accounts:Account]

    static constraints = {
		name blank: false
		city blank: false
		state blank: false
		starLevel range: 0..5
		startDate blank: false
		endDate blank: false
		websiteURL blank : true, nullable: true
		blankEntryFormURL blank : true, nullable: true
//		checkPayableTo blank : true, nullable: true
//		checkMailAddress blank : true, nullable: true
//		checkMailCity blank : true, nullable: true
//		checkMailState blank : true, nullable: true
//		checkMailZipCode blank : true, nullable: true
		tablesCount min: 0
		adminFee scale: 2, min: 0f
		lateEntryFee scale: 2, min: 0f
		stripeKey blank: false 
    }
}
