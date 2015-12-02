package com.atms

class Event {
	// event number listed on blank entry form (used for sorting)
	int	ordinalNumber
	
	// name e.g. U-2200, Open Singles, Under 17, Over 40 etc.
	String name
	
	// day 1, 2, 3 etc
	int day;
	
	// fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
	double startTime;

	// single elimination (true), round robin (false) 
	boolean singleElimination
	
	// doubles (true) or singles (false) 
	boolean doubles
	
	// maximum entries, 0 if no limit
	int maxEntries
	
	int minPlayerRating
	int maxPlayerRating
	
	int minPlayerAge
	int maxPlayerAge
	
	// round robin options
	int playersPerGroup
	int drawMethod 
	
	// best of 3, 5, 7 or 9 games per match
	int numberOfGames
	
	// number of players to advance, 0, 1 or 2
	int playersToAdvance
	
	// number of players to seed directly into next round
	int playersToSeed
	
	static belongsTo = [tournament: Tournament]
	
	static hasMany = [eventEntries: EventEntry]

    static constraints = {
		ordinalNumber min: 1
		name blank: false
		day: 1..10
//		startTime: 7.5..22.5
		maxEntries range: 0..10000
		minPlayerRating min: 0
		maxPlayerRating min: 0
		minPlayerAge range: 0..120
		maxPlayerAge range: 0..120
//		playersPerGroup min: 2
//		numberOfGames min: 3
		playersToAdvance min: 0
		playersToSeed min: 0
		
    }
}
