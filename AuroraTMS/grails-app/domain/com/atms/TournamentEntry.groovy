package com.atms

class TournamentEntry {
	
	// date user entered the tournament
	Date dateEntered
	
	// rating as of the Tournament.ratingCutoffDate date  it may go up after that date but will not effect eligibility for entered events
	// can be null i.e. unrated 
	int eligibilityRating
	
	// current rating used for seeding within events
	int seedRating
	
	// reference back to Tournament, but cascading saves and deletes
	static belongsTo = [tournament:Tournament]
	
	// collection of tournament entries
	static hasMany = [eventEntries: EventEntry]
	
    static constraints = {
		eligibilityRating nullable: true
		seedRating nullable: true  
    }
}
