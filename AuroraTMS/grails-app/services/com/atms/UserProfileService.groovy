package com.atms

import grails.transaction.Transactional

@Transactional
class UserProfileService {

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
	
	def UserProfile get(long id) {
		return UserProfile.get(id)
	}
}
