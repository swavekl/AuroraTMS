package com.atms

import org.junit.internal.runners.statements.FailOnTimeout;

import grails.transaction.Transactional

@Transactional
class UsattRatingsHistoryService {

	def sessionFactory
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

	def create(int memberId, int rating, Date asOfDate) {
		new UsattRatingsHistory (
				memberId: memberId,
				rating: rating,
				asOfDate: asOfDate)
				.save(failOnError: true)
	}

	def int count () {
		UsattRatingsHistory.count
	}

	/**
	 * helper method which periodically pushes changes into the database 
	 * @return
	 */
	def cleanUpGorm() {
		def session = sessionFactory.currentSession
		session.flush()
		session.clear()
		propertyInstanceMap.get().clear()
	}

	/**
	 * Reconciles the new ratings with existing ones and stores only new rating values
	 *  
	 * @param newRatingsMap map of member id to the record with rating
	 * @return
	 */
	def int updatePlayerRatings (Map<Integer, UsattRatingsHistory> newRatingsMap, List<Integer> newlyAddedMemberIds) {
		// get the latest ratings for each player from the database
		def currentRatingsList = findAllLatestRatingsAsOf(new Date())

		int countUpdated = 0
		// when we are updating find the new records for existing players
		// iterate over these ratings and find the corresponding new rating in the map
		def updatedMemberIdList = []	
		currentRatingsList.each {urh ->
			Integer memberId = urh.memberId
			updatedMemberIdList.add(memberId)
			def updatedURH = newRatingsMap[memberId]
			if (updatedURH != null) {
				// update rating if necessary
				if (updatedURH.asOfDate.after(urh.asOfDate)) {
					updatedURH.save(failOnError: true)
					countUpdated++
					// update in batches
					if (countUpdated % 100 == 0) {
						cleanUpGorm()
					}
				}
			}
		}
		
		// now insert the new ratings for new players
		newRatingsMap.each {key, value ->
			UsattRatingsHistory usattRatingsHistory = value as UsattRatingsHistory
			if (!updatedMemberIdList.contains(usattRatingsHistory.memberId)) {
				newlyAddedMemberIds.add(usattRatingsHistory.memberId)
				usattRatingsHistory.save(failOnError: true)
				countUpdated++
				if (countUpdated % 100 == 0) {
					cleanUpGorm()
				}
			}
		}
		cleanUpGorm()

		return countUpdated
	}

	/**
	 * Finds player rating as of a given date.  returns 0 if not found
	 * 
	 * @param cMemberId
	 * @param cAsOfDate
	 * @return
	 */
	def int findLatestRatingAsOf (int memberId, Date asOfDate) {
		// get the date
//		def query = UsattRatingsHistory.where {
//			memberId == cMemberId as Integer &&
//					asOfDate <= cAsOfDate
//		}
//
//		def ratingHistoryList = query.list(sort: 'asOfDate', order: 'desc', max: 1, offset: 0)
		
		// MySQL version
//				WHERE DATE(urh3.as_of_date) <= :asOfDate AND urh3.member_id = :memberId
		String query = $/
			select urh2.member_id, urh2.rating, urh2.as_of_date 
			from ( 
				SELECT urh3.member_id, max(as_of_date) as max_as_of_date 
				FROM usatt_ratings_history as urh3 
				WHERE urh3.as_of_date <= :asOfDate AND urh3.member_id = :memberId
				GROUP BY urh3.member_id 
			) as urh1 
			inner join ( 
			  select * from usatt_ratings_history as urh4 where urh4.member_id = :memberId
			) as urh2 
			where urh1.member_id = urh2.member_id AND urh1.max_as_of_date = urh2.as_of_date
		/$
			
		//def ratingHistoryList = UsattRatingsHistory.findAll(query, [asOfDate: asOfDate, memberId : memberId])
		def ratingHistoryList = new UsattRatingsHistory()
		.domainClass
		.grailsApplication
		.mainContext
		.sessionFactory
		.currentSession
		.createSQLQuery(query)
				.setInteger('memberId', memberId)
				.setDate('asOfDate', asOfDate)
				.list()
				
				
		int rating = 0
		println 'list len = ' + ratingHistoryList.size()
		if (ratingHistoryList.size() == 1) {
			def playerHistory = ratingHistoryList.get(0)
			rating = playerHistory[1] as Integer
		}
		return rating
	}

	/**
	 * Fetches latest ratings as of today
	 * @return
	 */
	def List<UsattRatingsHistory> findAllLatestRatingsAsOf (Date asOfDate) {
		// MySQL version
		//WHERE DATE(as_of_date) <= ? \
		
		String query = $/
			select urh2.member_id, urh2.rating, urh2.as_of_date 
			from ( 
				SELECT member_id, max(as_of_date) as max_as_of_date 
				FROM usatt_ratings_history 
				WHERE as_of_date <= :asOfDate 
				GROUP BY member_id 
			) as urh1 
			inner join ( 
			  select * from usatt_ratings_history 
			) as urh2 
			where urh1.member_id = urh2.member_id AND urh1.max_as_of_date = urh2.as_of_date
			/$
		
		def result = new UsattRatingsHistory()
		.domainClass
		.grailsApplication
		.mainContext
		.sessionFactory
		.currentSession
		.createSQLQuery(query)
				.setDate('asOfDate', asOfDate)
				.list()
				
		// convert to list of objects
		def ratingHistoryList = []
		Calendar calendar = Calendar.getInstance();
		result.each { it
			def urh = new UsattRatingsHistory()
			urh.memberId = it[0]
			urh.rating = it[1]
			// convert from java.sql.Timestamp to java.util.Date
			calendar.setTimeInMillis( it[2].getTime() );
			urh.asOfDate = calendar.getTime()
			ratingHistoryList.add(urh)
		} 
				
		return ratingHistoryList
	}
}
