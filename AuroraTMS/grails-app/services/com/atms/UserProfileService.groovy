package com.atms

import grails.transaction.Transactional

@Transactional (readOnly = true)
class UserProfileService {
	
	int USATT_TEMP_ID_START = 900000

    @Transactional
	def create(UserProfile userProfile) {
		
		userProfile.validate()
		if (userProfile.hasErrors()) {
			return
		}
		
		userProfile.save(failOnError: true, flush: true)
    }
	
	def UserProfile findByFirstLastName (String firstLastName) {
		def userProfile = null
		def result = UserProfile.executeQuery(
			"from UserProfile u where :fullName = concat(trim(u.firstName), ' ', trim(u.lastName))",
			[fullName: firstLastName])
		if (result.size() > 0) {
			userProfile = result.get(0)
		}
		return userProfile

	}
	
	/**
	 * Finds the next available temporary USATT member id
	 * 
	 * @return
	 */
	int computeTemporaryMemberId () {
		def query = UserProfile.where {
			usattID > USATT_TEMP_ID_START
		}.projections {
			max ('usattID')
		}
		def maxUSATTid = query.find()
		println 'maxUSATTid string ' + maxUSATTid
		if (maxUSATTid != null) {
			maxUSATTid = maxUSATTid as Integer
		} else {
			maxUSATTid = USATT_TEMP_ID_START
		}
		maxUSATTid += 1
		println 'maxUSATTid ' + maxUSATTid
		return maxUSATTid
	}
	
	def UserProfile get(long id) {
		return UserProfile.get(id)
	}
	
	/**
	 * 
	 * @param tournamentEntries
	 * @return
	 */
	List<UserProfile> listProfilesForTournamentEntries (List<TournamentEntry> tournamentEntryList) {
		def tournamentEntryIds = []
		tournamentEntryList.each () {
			tournamentEntryIds.add(it.id)
		}
		return UserProfile.withCriteria {
			tournamentEntries {
				'in' ('id', tournamentEntryIds)
			}
		}
	}
}
