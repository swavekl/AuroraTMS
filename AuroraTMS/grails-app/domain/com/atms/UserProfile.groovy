package com.atms

import java.util.Date;
import grails.rest.*

class UserProfile {

    String firstName
	String lastName
	Date dateOfBirth
	long usattID
	// contact information
	String email
	String phone
	String streetAddress
	String city
	String state
	String zipCode
	String gender
	String club
	
	
	// no reference to SecUser
	static belongsTo = SecUser
	
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
		
    }
}
