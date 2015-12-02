package com.atms

class EventEntry {

	// if true the entry is onto the waiting list, if false it is a normal entry
	boolean waitingList
	
	Date dateEntered
	
    // no reference property back to TournamentEntry but FK created in database, cascading saves and deletes from TournamentEntry
	static belongsTo = TournamentEntry
	
	// reference back to Event so we know which event it is (name, date and start time etc.)
	static hasOne = [event: Event]
	
	static constraints = {
    }
}
