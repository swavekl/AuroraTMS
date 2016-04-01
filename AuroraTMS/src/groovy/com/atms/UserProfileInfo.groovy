package com.atms

import java.util.Date;

/**
 * Class for sending back public profile information including current rating
 * 
 * @author Swavek
 *
 */
class UserProfileInfo {
	// user profile id
	int id
    String firstName
	String lastName
	// USATT membership id
	long usattID
	// USATT membership expiration date
	Date expirationDate
	// current USATT rating
	int rating
}
