package com.atms

/**
 * Class which gets around problems with sending transient fields in the response.  Only non-transients get sent in response but we don't want to persist
 * the availability status or price.
 * 
 * @author Swavek
 *
 */
class EventEntryInfo {
	// event entry which is persisted
	EventEntry eventEntry
	
	// event availability status
	String availabilityStatus
	
	// event price based on player's age or other criteria
	float fee
}
