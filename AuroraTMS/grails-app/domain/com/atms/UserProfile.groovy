package com.atms

import java.util.Date;
import grails.rest.*

class UserProfile {

    String firstName
	String lastName
	Date dateOfBirth
	
	// USATT membership information
	long usattID
	Date expirationDate
	
	// contact information
	String email
	String phone
	String streetAddress
	String city
	String state
	String zipCode
	String country
	String gender
	String club
	
	
	// no reference to SecUser
	static belongsTo = SecUser
	
	static hasMany = [tournamentEntries: TournamentEntry]
	
	static constraints = {
		firstName blank: false
		lastName blank: false
		dateOfBirth blank: false
		gender blank: false
		email blank: false
		phone blank: false
		streetAddress blank: false
		city blank: false
		state blank: false
		zipCode blank: false
		country blank: false
		expirationDate nullable: true
    }
}
