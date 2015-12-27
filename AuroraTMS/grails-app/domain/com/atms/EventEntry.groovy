package com.atms

class EventEntry {

	// reserved to hold until payment is complete, to be revoked if user abandons registration
	// not selected by user
	// confirmed payment went through now they have it,
	// waiting list they entered waiting list
	enum EntryStatus {
		NOT_SELECTED, PENDING, CONFIRMED 
	}

	EntryStatus status
	
	Date dateEntered
	
	enum AvailabilityStatus {
		ENTERED, AVAILABLE, FULL, WAITING_LIST, RATING, WRONG_AGE, WRONG_GENDER, TIME_CONFLICT  
	}

	// a reason the event is not available - e.g. Rating, Full, Age, time conflict etc.
	AvailabilityStatus availabilityStatus

	static transients = [ "availabilityStatus" ]

	// reference property back to TournamentEntry FK created in database, cascading saves and deletes from TournamentEntry
	static belongsTo = [tournamentEntry: TournamentEntry]
	
	// reference back to Event so we know which event it is (name, date and start time etc.)
	static hasOne = [event: Event]

	static constraints = { 
		status blank : false 
		//availabilityStatus bindable:true  // not persistent in DB, but bound value from map constructor
	}
}
