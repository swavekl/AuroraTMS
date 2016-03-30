package com.atms.utils

//import groovyx.net.http.HTTPBuilder
//import groovyx.net.http.ContentType
//import groovyx.net.http.Method
//import groovyx.net.http.RESTClient
import org.cyberneko.html.parsers.SAXParser

/* This is how the Simply Compete page looks with a single player record
 
 <table class="table table-striped list-area">
 <thead>
 <tr>
 <th class="sortable" ><a href="/userAccount/s?arh=&amp;max=20&amp;query=84639&amp;searchBy=usattNumber&amp;arl=&amp;format=&amp;sort=lastname&amp;order=asc">Name</a></th>
 <th class="sortable" ><a href="/userAccount/s?arh=&amp;max=20&amp;query=84639&amp;searchBy=usattNumber&amp;arl=&amp;format=&amp;sort=state&amp;order=asc">Location</a></th>
 <th class="mobile-hide sortable" ><a href="/userAccount/s?arh=&amp;max=20&amp;query=84639&amp;searchBy=usattNumber&amp;arl=&amp;format=&amp;sort=latestTrnRating&amp;order=asc">Rating</a></th>
 <th class="mobile-hide sortable" ><a href="/userAccount/s?arh=&amp;max=20&amp;query=84639&amp;searchBy=usattNumber&amp;arl=&amp;format=&amp;sort=usattNumber&amp;order=asc">USATT #</a></th>
 <th class="mobile-hide sortable" ><a href="/userAccount/s?arh=&amp;max=20&amp;query=84639&amp;searchBy=usattNumber&amp;arl=&amp;format=&amp;sort=membershipExpiration&amp;order=asc">Expiration Date</a></th>
 </tr>
 </thead>
 <tbody>
 <tr class="user-row list-item"
 onclick="location.href = '/userAccount/up/4723';">
 <td class="list-column list-column-main">
 <img width=28px class='backup_picture pull-left margin-right-30' src="/static/images/no-photo-male.jpg"/>
 Swavek Lorenc
 </td>
 <td class="list-column list-column-main">
 Aurora,
 IL
 </td>
 <td class="list-column list-column-main mobile-hide">1774</td>
 <td class="list-column list-column-main mobile-hide">84639</td>
 <td class="list-column list-column-main mobile-hide">03/31/2018</td>
 </tr>
 </tbody>
 </table>
 
 */
/**
 * This class is used to extract up to date USATT ratings and membership expiration dates from USATT system.  
 * This system is built by Simply Compete.
 * @author Swavek
 *
 */
class USATTDataSource {

	// base URL for all requests
	static String baseUrl = "https://usatt.simplycompete.com/userAccount/s"

	/**
	 * gets player record for player with specified USATT id
	 * @param usattId
	 * @return
	 */
	static List<Map<String, String>> getPlayerRecordById (long usattId) {
		String ratingsPage = baseUrl + "?searchBy=usattNumber&query=" + usattId + "&max=20&arl=&arh="
		return getPlayerRecords (ratingsPage)
	}

	/**
	 * Gets player records for players with the specified last name
	 * @param lastName
	 * @return
	 */
	static List<Map<String, String>> getPlayerRecordByLastName (String lastName) {
		String ratingsPage = baseUrl + "?searchBy=lastname&query=" + lastName + "&max=20"
				return getPlayerRecords (ratingsPage)
	}
	
	/**
	 * Gets player records for players with the specified last name, first name, living in a city and state
	 * @param lastName
	 * @return
	 */
	static List<Map<String, String>> getPlayerRecordBy (String lastName, String firstName, String city, String state) {
		// https://usatt.simplycompete.com/userAccount/s?searchBy=lastname&query=Liu&max=100
		// https://usatt.simplycompete.com/userAccount/s?max=100&query=Liu&searchBy=lastname&format=&offset=100
		
		// some vary popular last names result in several hundred hits so we need to go through all of them to find the right person
		String ratingsPage = baseUrl + "?searchBy=lastname&query=" + lastName + "&max=100"
		println ratingsPage
		def pageResults = getPlayerRecords (ratingsPage)
		println "pageResults.size() = " + pageResults.size()
		def allRecords = []
		int pageSize = 100
		int pageNum = 0
		while (pageResults.size() > 0) {
			allRecords.addAll (pageResults)
			// skip last request if there are fewer than page worth of results in current page - i.e. that was the last page
			if (pageResults.size() == pageSize) {
				// get next page
				pageNum++
				int offset = pageNum * pageSize
				ratingsPage = baseUrl + "?max=" + pageSize + "&query=" + lastName + "&searchBy=lastname&format=&offset=" + offset
				println ratingsPage
				pageResults = getPlayerRecords (ratingsPage)
				println "pageResults.size() = " + pageResults.size()
			} else {
				pageResults = []
			}
		}
		
		// now filter out the records that are not not matching 
		String firstAndAndLastName = firstName + " " + lastName
		def playerRecords = allRecords.findAll () { record ->
			String recFirstAndAndLastName = record['Name']
			if (firstAndAndLastName.equals(recFirstAndAndLastName)) {
				String location = record['Location']
				return isMatchingCityState(location, city, state)
			} else {
				return false
			}
		}
			
		return playerRecords
	}
	
	/**
	 * 
	 * @param location
	 * @param city
	 * @param state
	 * @return
	 */
	static boolean isMatchingCityState (String location, String city, String state) {
		// location is city and state with extra spacing e.g. 'Aurora,     IL'
		String [] locationParts = location.split(',')
		String locationCity = (locationParts.length > 1) ? locationParts[0].trim() : ""
		String locationState = (locationParts.length > 1) ? locationParts[1].trim() : ""
		if (!locationCity.isEmpty() && !city.isEmpty() && city.equalsIgnoreCase(locationCity) &&
			!locationState.isEmpty() && !state.isEmpty() && state.equals(locationState)) {
			return true
		}
		return false
	}

	/**
	 * Gets page worth of player records starting with page number.  Each page will contain up to 20 records 
	 * @param state
	 * @param pageNum
	 * @return
	 */
	static List<Map<String, String>> getPlayerRecordsByState (String state, int pageNum) {
		int pageSize = 100
		// https://usatt.simplycompete.com/userAccount/s?max=100&query=IL&searchBy=state&format=
		// https://usatt.simplycompete.com/userAccount/s?max=100&query=IL&searchBy=state&format=&offset=100
		// https://usatt.simplycompete.com/userAccount/s?max=100&query=IL&searchBy=state&format=&offset=200
		String ratingsPage = baseUrl + "?arh=&max=" + pageSize + "&query=" + state + "&searchBy=state&arl=&format="
		int offset = pageNum * pageSize
		if (pageNum > 0) {
			ratingsPage += "&offset=" + offset
		}
		return getPlayerRecords (ratingsPage)
	}

	/**
	 * Gets a single players rating based on his USATT id
	 * @param usattId
	 * @return
	 */
	static int getPlayerRatingById (long usattId) {
		def listOfRecords = getPlayerRecordById (usattId)

		int rating = 0
		// now extract rating value
		listOfRecords.each { fieldToValueMap ->
			fieldToValueMap.each {key, value ->
				String fieldName = key as String
				if (fieldName.equals("Rating")) {
					rating = value as Integer
				}
			}
		}
		return rating

	}
	
	/**
	 * Extracts player records from a given HTML page and places them in the list of maps (field to value)
	 * 
	 * @param playerRecordsUrl url to fetch the data from.
	 * @return
	 */
	static List<Map<String, String>> getPlayerRecords (String playerRecordsUrl) {
		def indexToFieldMap = [:]
		def listOfRecords = []
		def page = new XmlSlurper(new org.cyberneko.html.parsers.SAXParser()).parse(playerRecordsUrl)
		// find all nodes
		def node = page.'**'.grep {

			// get the names of the columns
			//	<table class="table table-striped list-area">
			//		<thead>
			//			<tr>
            //				<th class="sortable" ><a href="/userAccount/s?arh=&amp;max=20&amp;query=84639&amp;searchBy=usattNumber&amp;arl=&amp;format=&amp;sort=lastname&amp;order=asc">Name</a>
			//              </th>
 			if (it.name().equals("TABLE") && it.@class.toString().equals("table table-striped list-area")){
				def thead = it.THEAD
				thead.TR.each { tr2 ->
					tr2.TH.eachWithIndex { th, index ->
						def a = th.A
						String href = a.@href.toString()
						if (href.startsWith("/userAccount/s")) {
							String fieldName = a.text().trim()
							indexToFieldMap[index] = fieldName
						}
					}
				}
			}
			 
			// get the values for the columns and place them in the map of field name to value
			//  <tr class="user-row list-item"
			 // or 
			 // <tr class="list-item"
			 //println "it.name() = " + it.name() + " it.@class.toString() = " + it.@class.toString()
			if (it.name().equals("TR")) {
				String className = it.@class.toString()
				if (className.equals("list-item") || className.equals("user-row list-item")) {
					// Name 	Location 	Rating 	USATT # 	Expiration Date
					// Swavek Lorenc 	Aurora, IL 	1774 	84639 	03/31/2018
					def fieldToValueMap = [:]
					// add map to the list of maps
					listOfRecords.add(fieldToValueMap)
	//				println "adding record and processing its children "
					it.TD.eachWithIndex { td, index2 ->
						String fieldValue = td.text()
						fieldValue = fieldValue.replaceAll("\n", "")
						fieldValue = fieldValue.trim()
						fieldValue = fieldValue.replaceAll("  ", " ")
						String fieldName = indexToFieldMap[index2];
						fieldToValueMap[fieldName] = fieldValue;
	//					println "fieldToValueMap ['" + fieldName + "'] = '" + fieldValue + "'"
					}
	
				}
			}
		}
//		println "Found " + listOfRecords.size() + " records"
		return listOfRecords
	}

	//	static def postText(String baseUrl, String path, query, method = Method.POST) {
	//		try {
	//			def ret = null
	//			def http = new HTTPBuilder(baseUrl)
	//
	//			// perform a POST request, expecting TEXT response
	//			http.request(method, ContentType.TEXT) {
	//				uri.path = path
	//				uri.query = query
	//				headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
	//
	//				// response handler for a success response code
	//				response.success = { resp, reader ->
	//					println "response status: ${resp.statusLine}"
	//					println 'Headers: -----------'
	//					resp.headers.each { h ->
	//						println " ${h.name} : ${h.value}"
	//					}
	//
	//					ret = reader.getText()
	//
	//					println 'Response data: -----'
	//					println ret
	//					println '--------------------'
	//				}
	//			}
	//			return ret
	//
	//		} catch (groovyx.net.http.HttpResponseException ex) {
	//			ex.printStackTrace()
	//			return null
	//		} catch (java.net.ConnectException ex) {
	//			ex.printStackTrace()
	//			return null
	//		}
	//	}
	//
	//	static def getText(String baseUrl, String path, query) {
	//		return postText(baseUrl, path, query, Method.GET)
	//	}

}

