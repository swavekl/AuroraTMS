package com.atms

import java.util.Date;

class UsattProfile {

	int memberId
	String lastName
	String firstName
	String middleName
	String address1
	String address2
	String city
	String state
	String zipCode
	String country
	String gender
	Date dateOfBirth
	int rating
	Date expirationDate
	Date lastPlayedDate
	
	static constraints = {
		memberId blank: false
		lastName blank: false
		firstName nullable: true
		middleName nullable: true
		address1 nullable: true
		address2 nullable: true
		city nullable: true
		state nullable: true
		zipCode nullable: true
		country nullable: true
		gender blank: false
		dateOfBirth nullable: true
		expirationDate nullable: true
		lastPlayedDate nullable: true
    }
}
