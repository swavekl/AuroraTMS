package com.atms

import grails.rest.*
// pivotal video - RESTful Grails 2 
// https://www.youtube.com/watch?v=8xYi9n0pYFs
//@Resource(uri='/tournaments', formats=['json', 'xml'])
class Tournament {
	String name
	// name of the venue
	String venue
	String address
	String city
	String state
	int starLevel
	Date startDate
	Date endDate
	
	static hasMany = [events:Event]

    static constraints = {
		name blank: false
		city blank: false
		state blank: false
		starLevel range: 0..5
		startDate blank: false
		endDate blank: false
    }
}
