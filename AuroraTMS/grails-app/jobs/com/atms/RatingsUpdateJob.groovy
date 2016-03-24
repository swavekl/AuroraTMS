package com.atms

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date;
import liquibase.util.csv.opencsv.CSVReader
import liquibase.util.csv.opencsv.CSVWriter
import com.atms.utils.USATTDataSource
import com.atms.utils.RegionStateInfo


/**
 * This job will pull down the ratings file from USATT every day and update the ratings and expiration date of USATT membership.
 * 
 * The ratings come in a CSV format with the following information
 * Member ID,Last Name,First Name,Rating,State,Zip,Gender,Date of Birth,Expiration Date,Last Played Date
 * 84639,Lorenc,Swavek,1771,IL,60502,M,1964-10-27,2016-01-31,2015-10-25
 * 
 * This rating in this file as well as the expiration date need to be updated in the USATT profile as well as USATTRatingsHistory table
 * 
 * @author Swavek
 *
 */
class RatingsUpdateJob {

	def usattRatingsHistoryService

	def userProfileService

	static triggers = {
		// fire at 8 pm, they only process tournaments from Monday through Thursday
		cron name: 'ratingsUpdateTrigger', cronExpression: "0 0 20 ? * MON-THU"
	}

	def execute() {
		// use this date for filename and timestamp in history table
		Date asOfDate = new Date()

		// download the file from USATT and store it in the data folder
		// for now get the ratings data by scraping the USATT pages
		// 90,000 records takes about 30 minutes to download and store in a file
		// getting all data in one 3 MB file should be faster but is not available yet
		boolean tdRatingsAvailable = false
		if (!tdRatingsAvailable) {
			String scrapedRatingsFilename = scrapeRatingsDataIntoFile (asOfDate)
			processRatingsFile (scrapedRatingsFilename, asOfDate)
		} else {
			// USATT plans to make the TD ratings file available for nightly download which will
			// include birth date for eligibility testing and other player data
			// we don't know when this will become available though
			def filanme = downloadTournamentDirectorFile(asOfDate)
			processTDRatingsFile(filename, asOfDate)
		}
	}

	/**
	 * 
	 * @return
	 */
	def String scrapeRatingsDataIntoFile (Date asOfDate) {
		// the filename will be named like this 'TD Rating 3-17-16'
		// construct the name using today's date
		def df = new SimpleDateFormat ('M-d-yy')
		String today = df.format (asOfDate)

		//String stagingDir = grailsApplication.config.ratingsStagingDir
		String stagingDir = "c:\\grails\\data\\ratings"
		String filename = stagingDir + "\\Ratings " + today + '.csv'

		// get a list of states for all regions
		def states = ['KY']
		//		String [] regions = RegionStateInfo.getRegions()
		//		regions.each { region ->
		//			String [] regionStates = RegionStateInfo.getRegionStates(region)
		//			states.addAll(regionStates)
		//		}

		// create a file by scraping pages from USATT website 'Simply Compete' format
		// they contain less information (no zip code or birth date) but enough to update rating history
		long totalStart = System.currentTimeMillis()
		int totalPlayerCount = 0
		def writer = new CSVWriter (new FileWriter (filename))
		String [] headerLine = ['Member ID', 'First and Last Name', 'Rating', 'State', 'Expiration Date']
		writer.writeNext(headerLine)
		states.each () {state ->
			long start = System.currentTimeMillis()
			int pageNumber = 0
			int stateRecordsCount = 0
			def listOfRecords = USATTDataSource.getPlayerRecordsByState (state, pageNumber)
			while (listOfRecords.size() > 0) {
				stateRecordsCount += listOfRecords.size()
				//println "Got " + listOfRecords.size() + " records of players from " + state + " page " + pageNumber

				listOfRecords.each { record ->
					String firstLastName = record['Name']
					String memberId = record['USATT #']
					String rating = record['Rating']
					String expirationDate = record['Expiration Date']
					String [] line = [memberId, firstLastName, rating, state, expirationDate]
					writer.writeNext(line)
				}
				pageNumber++
				listOfRecords = USATTDataSource.getPlayerRecordsByState (state, pageNumber)
			}
			long end = System.currentTimeMillis()
			println 'Wrote ' + stateRecordsCount + ' player records for state of ' + state + " in " + ((end - start) / 1000) + " s"
			totalPlayerCount += stateRecordsCount
		}
		writer.close()
		long totalEnd = System.currentTimeMillis()
		println 'Wrote all ' + totalPlayerCount + ' player records for all states in ' + ((totalEnd - totalStart) / 1000) + " s to file: " + filename

		return filename
	}

	/**
	 * Download the ratings file which contains all USATT player records including birth date and last played time
	 *  
	 * @return
	 */
	def downloadTournamentDirectorFile (Date asOfDate) {

		// the filename will be named like this 'TD Rating 3-17-16'
		// construct the name using today's date
		def df = new SimpleDateFormat ('M-d-yy')
		String asOfDateStr = df.format (asOfDate)

		//String stagingDir = grailsApplication.config.ratingsStagingDir
		String stagingDir = "c:\\grails\\data\\ratings"
		String filename = stagingDir + "\\TD Ratings " + asOfDateStr + '.csv'

		// TODO: this file is not yet available from USATT website but may be in the future
		//		def downloadFiles = { sourceUrls->
		//			def stagingDir = "c:\\grails\\data\\ratings"
		//			new File(stagingDir).mkdirs()
		//			sourceUrls.each { sourceUrl ->
		//			  def filename = sourceUrl.tokenize(\'/\')[-1]
		//			  def file = new FileOutputStream(\"$stagingDir/$filename\")
		//			  def protocolUrlTokens = sourceUrl.tokenize(\':\')
		//			  def sourceUrlAsURI = new URI(protocolUrlTokens[0],
		//				  protocolUrlTokens[1..(protocolUrlTokens.size-1)].join(\":\"), \"\")
		//			  def out = new BufferedOutputStream(file)
		//			  out << sourceUrlAsURI.toURL().openStream()
		//			  out.close()
		//			}
		//		   }
		//
		//		   downloadFiles(
		//			[\"http://lavezzo.com/saic/mvnBuildLifecycle.png\",
		//			\"http://lavezzo.com/saic/I have a space.png\"
		//			])
		return filename
	}

	/**
	 * Processes ratings file which has the following format
	 * "Member ID","First and Last Name","Rating","State","Expiration Date"
	 * "25017","John Abraham","1388","KY","12/29/2015"
	 * 
	 * @param ratingsFile
	 * @param asOfDate
	 * @return
	 */
	def processRatingsFile (String ratingsFile, Date asOfDate) {
		// read the csv file
		CSVReader reader = new CSVReader(new FileReader(ratingsFile), (char)',', (char)'\"', 1);
		List<String[]> rows = reader.readAll()
		
		// remove duplicates
		def cleanedUpRecords = cleanupUpRatingRecords (rows)
		
		// process good records
		// map of member id to ratings info read from this file
		def memberIdToRatingHistoryMap = [:]
		int count = 0
		cleanedUpRecords.each () {row ->
			count++

			def strMemberId = row[0]
			def strRating = row[2]

			int memberId = strMemberId as Integer
			int rating = strRating as Integer
			// save in the map
			memberIdToRatingHistoryMap[memberId] = new UsattRatingsHistory (rating: rating, memberId: memberId, asOfDate: asOfDate)
		}
		println 'Processed ' + count + " records"

		// now get the latest ratings for each player from the database and update it with data from the map
		def newlyAddedMemberIds = []
		int countUpdated = usattRatingsHistoryService.updatePlayerRatings (memberIdToRatingHistoryMap, newlyAddedMemberIds)
		println "Updated ratings for " + countUpdated + " players"

		updateProfileRecords (cleanedUpRecords, newlyAddedMemberIds, true)
	}
	
	/**
	 * Removes duplicate records
	 * @param rows
	 * @return
	 */
	def List<String[]> cleanupUpRatingRecords (List<String[]> rows) {

		// remove records without member id, rating or expiration date 
		def cleanedUpRecords = rows.findAll { row ->
			def strMemberId = row[0]
			def firstLastName = row[1]
			def strRating = row[2]
			def state = row[3]
			def strExpirationDate = row[4]

			// some players have double records one with and one without expiration date - discard the one without
			if (!strRating.isEmpty() && !strMemberId.isEmpty() && !strExpirationDate.isEmpty()) {
				return true
			}
			
			println "Mising data record found for : " + firstLastName + ", " + state + ", " + strRating + ", " + strExpirationDate
			return false
		}
		
		// remove duplicate records
		def df = new SimpleDateFormat ('MM/dd/yyyy')
		def savedRecordsMap = [:]
		println 'Deduplicating records...'
		cleanedUpRecords.each { row ->
			def strMemberId = row[0]
			def firstLastName = row[1]
			def strRating = row[2]
			def state = row[3]
			def strExpirationDate = row[4]
			// not seen this record just add it
			if(savedRecordsMap[firstLastName] == null) {
				savedRecordsMap[firstLastName] = row
			} else {
				// we have a duplicate - one of them is better
				// Swavek Lorenc 	Aurora, IL 	1778 	84639 	03/31/2018
				// Swavek Lorenc 	N/A 		0 		132506 	09/21/2010
				def previousRecord = savedRecordsMap[firstLastName]
				def str1 = previousRecord.join(", ")
				def str2 = row.join(", ")
				println 'previous record ' + str1
				println 'current  record ' + str2

				def previousExpirationDate = previousRecord[4]
				Date previousED = df.parse(previousExpirationDate)
				Date thisED = df.parse(strExpirationDate)
				if (previousED.before(thisED)) {
					// this record more up to date
					println 'using current record '
					savedRecordsMap[firstLastName] = row
				} else if (previousED.equals(thisED)) {
					// date alone can't decide
					// check city, state if better
				}
			}
		}
		
		cleanedUpRecords.clear()
		cleanedUpRecords.addAll(savedRecordsMap.values())
		
		int removedRecords =  rows.size() - cleanedUpRecords.size()
		println 'Removed '+ removedRecords + " duplicate or invalid records"
		
		return cleanedUpRecords
	}

	/**
	 * Processes TD ratings file
	 * 
	 * @param ratingsFile
	 * @param asOfDate
	 * @return
	 */
	def processTDRatingsFile (String ratingsFile, Date asOfDate) {
		// read data file
		CSVReader reader = new CSVReader(new FileReader(ratingsFile), (char)',', (char)'\"', 1);
		List<String[]> rows = reader.readAll()
		
		// remove duplicates etc.
		def cleanedUpRecords = cleanupUpTDRatingRecords(rows)
		
		// process good records
		int count = 0
		def memberIdToRatingHistoryMap = [:]
		cleanedUpRecords.each { row ->
			count++
			int memberId = row[0] as Integer
			int rating = row[3] as Integer
			// save in the map
			memberIdToRatingHistoryMap[memberId] = new UsattRatingsHistory (rating: rating, memberId: memberId, asOfDate: asOfDate)
		}
		println 'Processed ' + count + " players from TD ratings file"

		// now get the latest ratings for each player from the database and update it with data from the map
		def newlyAddedMemberIds = []
		int countUpdated = usattRatingsHistoryService.updatePlayerRatings (memberIdToRatingHistoryMap, newlyAddedMemberIds)
		println "Updated ratings for " + countUpdated + " players"

		updateProfileRecords (cleanedUpRecords, newlyAddedMemberIds, false)
	}
	
	/**
	 * Removes duplicate records
	 * @param rows
	 * @return
	 */
	def List<String[]> cleanupUpTDRatingRecords (List<String[]> rows) {
		Set<String> deduplicateSet = new HashSet<String>()

		// remove duplicate records
		def cleanedUpRecords = rows.findAll { row ->
			def strMemberId = row[0]
			def lastName = row[1]
			def firstName = row[2]
			def strRating = row[3]

			String firstLastName = firstName + " " + lastName
			// some players have double records one with and one without expiration date - discard the one without
			if (!strRating.isEmpty() && !strMemberId.isEmpty() && !deduplicateSet.contains(firstLastName)) {
				deduplicateSet.add(firstLastName)
				return true
			}
			
			println "Duplicate record found for : " + firstLastName + ", " + state + ", " + strRating + ", " + strExpirationDate
			return false
		}
		
		int removedRecords =  rows.size() - cleanedUpRecords.size()
		println 'Removed '+ removedRecords + " duplicate or invalid records"
		
		return cleanedUpRecords
	}


	/**
	 * New members now have a USATT assigned member id. Find them by historical data and update
	 * 
	 * @param rows player data from file
	 * @param newlyAddedMemberIds ids of new members who were not in the ratings history
	 * @return
	 */
	def updateProfileRecords (List<String[]> rows, List<Integer> newlyAddedMemberIds, boolean scrapedFile) {
		// find new player data
		def newPlayerData = rows.findAll { row ->
			//println "memberId " + row[0] + " name " + row [1] + " " + row[2] + ' ' + row[3]
			int memberId = row[0] as Integer
			return newlyAddedMemberIds.contains(memberId)
		}
		println 'Got ' + newPlayerData.size() + ' records for new players'

		def dateFormatter = new SimpleDateFormat('M/d/yyyy') // 9/30/1958

		// now locate the USER profile and update it
		newPlayerData.each { row ->
			// TD file has this format
			//Member ID	Last Name	First Name	Rating	State	Zip	Gender	Date of Birth	Expiration Date	Last Played Date
			//65574	Sable	Evan	873	NJ	8544	M		12/29/2015	2/19/1994
			
			// scraped HTML file has this format
			//Member ID	First and Last Name	Rating	State	Expiration Date
			//25017	John Abraham	1388	KY	12/29/2015
			
			def userProfile = null
			String firstLastName = ""
			int memberId = 0
			Date expirationDate = null 
			if (scrapedFile) {
				memberId = row[0] as Integer
				firstLastName = row[1]
				def strRating = row[2]
				def strState = row[3]
				def strExpirationDate = row[4]
				if (!strExpirationDate.isEmpty()) {
					expirationDate = dateFormatter.parse(strExpirationDate)
				}
				
				println "searchig for profile using firstLastName \'" + firstLastName + "'"
				userProfile = userProfileService.findByFirstLastName (firstLastName)
				
			} else {
				memberId = row[0] as Integer
				def strLastName = row[1]
				def strFirstName = row[2]
				def strRating = row[3]
				def strState = row[4]
				def strZip = row[5]
				def strGender = row[6]
				def dob = row[7]
				def strExpirationDate = row[8]
				def lastPlayedDate = row[9]

				if (!strExpirationDate.isEmpty()) {
					expirationDate = dateFormatter.parse(strExpirationDate)
				}

				firstLastName = strFirstName + " " + strLastName
	
				userProfile = UserProfile.where {
					firstName == strFirstName &&
							lastName == strLastName &&
							state == strState &&
							zipCode == strZip &&
							gender == strGender &&
							dateOfBirth == dob
				}.get()
	
				if (userProfile == null) {
					userProfile = UserProfile.where {
						firstName == strFirstName &&
								lastName == strLastName &&
								state == strState &&
								zipCode == strZip &&
								gender == strGender
					}.get()
				}
	
				if (userProfile == null) {
					userProfile = UserProfile.where {
						firstName == strFirstName &&
								lastName == strLastName &&
								state == strState &&
								zipCode == strZip
					}.get()
				}
			}

			if (userProfile != null) {
				println 'Setting USATT assigned membership id to ' + memberId + ' for temporary id ' + userProfile.usattID + " for player " + strFirstName + " " + strLastName
				userProfile.usattID = memberId
				userProfile.expirationDate = expirationDate
				userProfile.save(failOnErrof: true, flush: true)
			} else {
				println 'User profile not found for ' + firstLastName
			}
		}
	}
}
