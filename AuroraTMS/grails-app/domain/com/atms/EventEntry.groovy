package com.atms

class EventEntry {

	// reserved to hold until payment is complete, to be revoked if user abandons registration
	// confirmed payment went through now they have it,
	// waiting list they entered waiting list
	enum EntryStatus {
		PENDING, CONFIRMED, WAITINGLIST
	}

	EntryStatus status

	Date dateEntered

	// no reference property back to TournamentEntry but FK created in database, cascading saves and deletes from TournamentEntry
	static belongsTo = TournamentEntry

	// reference back to Event so we know which event it is (name, date and start time etc.)
	static hasOne = [event: Event]

	static constraints = { status blank : false }
}
