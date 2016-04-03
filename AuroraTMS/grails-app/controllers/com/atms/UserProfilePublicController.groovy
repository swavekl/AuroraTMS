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

	static namespace = 'public'

	static responseFormats = ['json', 'xml']
	static allowedMethods = [show: "GET", index: "GET"]
	
	def eventEntryService
	def tournamentEntryService
	def userProfileService
	
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
	
	/**
	 * Lists public information for players who entered a particular event 
	 * 
	 * @return
	 */
	List<UserProfileInfo> index () {
		
		List<UserProfileInfo> userProfileInfoList = []
		if (params.eventId != null) {
			long eventId = params.eventId as Long
			
			// first find all event entries for this event
			def eventEntries = eventEntryService.listEventEntries(eventId, EventEntry.EntryStatus.CONFIRMED)
			// then get tournament entries associated with them
			def tournamentEntries = tournamentEntryService.listForEventEntries(eventEntries)
			// make a map of tournament entry id to eligibility rating
			def teIdToratingMap = [:]
			tournamentEntries.each {
				TournamentEntry te = it as TournamentEntry
				teIdToratingMap [te.id] = te.eligibilityRating
			}
			
			// finally get user profiles
			def userProfiles = userProfileService.listProfilesForTournamentEntries(tournamentEntries)
			// extract public information from them
			userProfiles.each () {
				UserProfileInfo upi = new UserProfileInfo()
				userProfileInfoList.add (upi)
				upi.id = it.id
				upi.firstName = it.firstName
				upi.lastName = it.lastName
				upi.usattID = it.usattID
				// find the rating in the map
				def playerTournamentEntries = it.tournamentEntries
				playerTournamentEntries.each () { te ->
					def eligibilityRating = teIdToratingMap[te.id]
					if (eligibilityRating != null) {
						upi.rating = eligibilityRating as Integer
					}
				}
			}
			
			// sort by rating in descending order
			userProfileInfoList.sort{it.rating}
			Collections.reverse(userProfileInfoList)
		}
		respond userProfileInfoList
	}
}
