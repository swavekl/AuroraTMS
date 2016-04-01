package com.atms

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import grails.transaction.Transactional;

import java.lang.invoke.MethodHandleImpl.BindCaller.T
import java.text.SimpleDateFormat
import java.util.Date;

import static org.springframework.http.HttpStatus.*

import org.codehaus.groovy.grails.web.servlet.HttpHeaders
import org.springframework.http.HttpStatus

import com.atms.utils.USATTDataSource;

import grails.converters.*
import groovy.json.JsonBuilder

@Transactional (readOnly = true)
@Secured(['permitAll'])
class UserProfilePublicController extends RestfulController {

	static responseFormats = ['json', 'xml']
	static allowedMethods = [show: "GET"]

	UserProfilePublicController () {
		super(UserProfile, false)
	}

	UserProfilePublicController (boolean readOnly) {
		super(UserProfile, readOnly)
	}

	/**
	 * Shows a single resource
	 * @param id The id of the resource
	 * @return The rendered resource or a 404 if it doesn't exist
	 */
	def show() {
		def userProfile = queryForResource(params.id)
		UserProfileInfo userProfileInfo = new UserProfileInfo()
		if (userProfile != null) {
			userProfileInfo.id = userProfile.id
		    userProfileInfo.firstName = userProfile.firstName
			userProfileInfo.lastName = userProfile.lastName
			userProfileInfo.usattID = userProfile.usattID
			
			String fullName = userProfileInfo.firstName + " " + userProfileInfo.lastName
			def df = new SimpleDateFormat ('MM/dd/yyyy')
			
			// calls usatt website
			def recordList = USATTDataSource.getPlayerRecordById(userProfile.usattID)
			if (recordList.size() > 0) {
				def record = recordList.get(0)
				userProfileInfo.rating = record['Rating'] as Integer
				String strExpirationDate = record['Expiration Date']
				userProfileInfo.expirationDate = df.parse(strExpirationDate)
			} else {
				// perhaps user was a new USATT member and had a temporary USATT id
				recordList = USATTDataSource.getPlayerRecordBy(userProfile.lastName, userProfile.firstName, userProfile.city, userProfile.state)
				if (recordList.size() > 0) {
					recordList.each () {record ->
						if (record['Name'].equals(fullName)) {
							userProfileInfo.rating = record['Rating'] as Integer
							String strExpirationDate = record['Expiration Date']
							userProfileInfo.expirationDate = df.parse(strExpirationDate)
						}
					}
				}
			}
		} 
		
		respond userProfileInfo
	}
}
