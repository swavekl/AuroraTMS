package com.atms.utils

class RegionStateInfo {
	
	// USATT regions
	static regions = [
		'East', 'Midwest', 'Mountain', 'North', 'Northwest', 'Pacific', 'South Central', 'Southeast'
	]

	// map of region to their member states
	static regionStatesMap = [
		   'East' 			: ['CT', 'DE', 'ME', 'MD', 'MA', 'NH', 'NJ', 'NY', 'PA', 'RI', 'VT', 'VA', 'WV'],
		   'Midwest'		: ['IL', 'IN', 'MI', 'KY', 'OH'],
		   'Mountain'		: ['CO', 'NE', 'NM', 'UT', 'WY'],
		   'North'			: ['IA', 'MN', 'ND', 'SD', 'WI'],
		   'Northwest'		: ['AK', 'ID', 'MT', 'OR', 'WA'],
		   'Pacific'   		: ['AZ', 'CA', 'HI', 'NV'],
		   'South Central' 	: ['AR', 'KS', 'LA', 'MO', 'OK', 'TX'],
		   'Southeast' 		: ['AL', 'FL', 'GA', 'MS', 'NC', 'SC', 'TN']
	 ]

	// translation map of state code to full name
	static stateCodeToNameMap = [
			  'AL' : 'Alabama',
			  'AK' : 'Alaska',
			  'AZ' : 'Arizona',
			  'AR' : 'Arkansas',
			  'CA' : 'California',
			  'CO' : 'Colorado',
			  'CT' : 'Connecticut',
			  'DE' : 'Delaware',
			  'FL' : 'Florida',
			  'GA' : 'Georgia',
			  'HI' : 'Hawaii',
			  'ID' : 'Idaho',
			  'IL' : 'Illinois',
			  'IN' : 'Indiana',
			  'IA' : 'Iowa',
			  'KS' : 'Kansas',
			  'KY' : 'Kentucky',
			  'LA' : 'Louisiana',
			  'ME' : 'Maine',
			  'MD' : 'Maryland',
			  'MA' : 'Massachusetts',
			  'MI' : 'Michigan',
			  'MN' : 'Minnesota',
			  'MS' : 'Mississippi',
			  'MO' : 'Missouri',
			  'MT' : 'Montana',
			  'NE' : 'Nebraska',
			  'NV' : 'Nevada',
			  'NH' : 'New Hampshire',
			  'NJ' : 'New Jersey',
			  'NM' : 'New Mexico',
			  'NY' : 'New York',
			  'NC' : 'North Carolina',
			  'ND' : 'North Dakota',
			  'OH' : 'Ohio',
			  'OK' : 'Oklahoma',
			  'OR' : 'Oregon',
			  'PA' : 'Pennsylvania',
			  'RI' : 'Rhode Island',
			  'SC' : 'South Carolina',
			  'SD' : 'South Dakota',
			  'TN' : 'Tennessee',
			  'TX' : 'Texas',
			  'UT' : 'Utah',
			  'VT' : 'Vermont',
			  'VA' : 'Virginia',
			  'WA' : 'Washington',
			  'WV' : 'West Virginia',
			  'WI' : 'Wisconsin',
			  'WY' : 'Wyoming'
		  ]

	static String [] listRegions () {
		return regions
	}
	
	static String [] getRegionStates (String region) {
		def regionStates = regionStatesMap [region];
		return regionStates;
	}
	
	static String getStateFullName (String stateCode) {
		String stateName = stateCodeToNameMap [stateCode]
		return stateName
	}
	
	static String lookupRegionByState (String stateCode) {
		String region = null
		regionStatesMap.each { key, value ->
			def regionStatesList = value 
			regionStatesList.each {
				if (it == stateCode) {
					region = key
				}
			}
		}
		return region
	}
}
