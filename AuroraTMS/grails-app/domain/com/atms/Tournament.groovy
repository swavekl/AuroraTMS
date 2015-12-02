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
	
	Date ratingCutoffDate
	Date lateEntryStartDate
	Date entryCutoffDate
	
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
	
	// fees
	float usattRatingFee
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
	
	static hasMany = [events:Event, tournamentEntries: TournamentEntry]

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
		usattRatingFee scale: 2, min: 0f
		adminFee scale: 2, min: 0f
		lateEntryFee scale: 2, min: 0f 
    }
}
