package com.atms

/**
 * Class to get around Grails inability to send transient fields.  This is so that we can send additional fields to F.E.
 * @author Swavek
 *
 */
class EventInfo extends Event {
	
	// count of confirmed entries
	int confirmedEntriesCount
	
	// count of entries on the waiting list
	int waitingListEntriesCount

}
