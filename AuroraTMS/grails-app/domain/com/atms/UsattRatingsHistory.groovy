package com.atms

/**
 * Class for storing USATT ratings history showing what the rating was on a given date
 * 
 * @author Swavek
 *
 */
class UsattRatingsHistory {

	// USATT membership id
	int memberId
	
	// player rating
	int rating
	
	// date on which this rating was attained
	Date asOfDate
	
    static constraints = {
		// no need for version here
		version: false
		// define indexes for searching
		memberId index: 'memberid_idx'
		asOfDate index: 'asofdate_idx'
    }
}

