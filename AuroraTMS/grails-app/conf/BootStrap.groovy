import groovy.time.*

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date;

import javax.swing.text.DateFormatter

import liquibase.util.file.FilenameUtils;
import net.sf.ehcache.CacheManager

import com.atms.Event.GenderRestriction;
import com.atms.Account
import com.atms.EventEntry
import com.atms.SecRole
import com.atms.SecUser
import com.atms.SecUserSecRole
import com.atms.Tournament
import com.atms.Event
import com.atms.TournamentEntry
import com.atms.UsattProfile
import com.atms.UserProfile

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.DELETE
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH

import grails.util.Environment

class BootStrap {
	
	def aclService
	def aclUtilService
	def objectIdentityRetrievalStrategy
	def sessionFactory
	def tournamentService
	def eventService
	def tournamentEntryService
	def eventEntryService
	def userProfileService

	def grailsCacheManager // generic grails cache manager (contains ehcache cache manager)
	CacheManager ehcacheCacheManager // uses DataSource properties to register mbean
	String displayName = ""
	def df = new SimpleDateFormat("MM/dd/yyyy")
	
	def df2 = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
	
	def init = { servletContext ->
		log.info "================ BootStrap #init ===================="
		log.info "manager name: ${ehcacheCacheManager?.name}"

		displayName = servletContext.servletContextName
		log.info "Started application: ${displayName}"

		createUsers()
		loginAsAdmin()
		grantPermissions()
		
		if (Environment.current != Environment.TEST) {
			int countExisting = UsattProfile.count()
			if (countExisting > 0) {
				println countExisting + " players already imported.  Skipping import"
			} else {
				importPlayerData()
			}
		}
		sessionFactory.currentSession.flush()
		
		if (Environment.current != Environment.TEST) {
			def tournament5 = configureAuroraCup ()
			
			sessionFactory.currentSession.flush()
			
			def paramsT = [ tournament : tournament5.id]
			def tournamentCheck = tournamentService.list(paramsT)
			def events = tournamentCheck.events
		}

		// logout
		SCH.clearContext()

		// have to be authenticated as an admin to create ACLs
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
				'ed', 'ed',
				AuthorityUtils.createAuthorityList('ROLE_TOURNAMENT_DIRECTOR'))

		def tournament6 = new Tournament (name: "U.S.Open", venue: 'Las Vegas Convention Center', address: '123 W. Strip Ave', city: "Las Vegas", state: "NV", startDate: df.parse('07/11/2015'), endDate: df.parse('07/16/2015'), starLevel: 5)
		fillOtherTournamentDefaults (tournament6, "Tiffany Oldland", "tiffanyol@yahoo.com")
		Map params1 = [:]
		tournamentService.create(tournament6, params1)
		
		// logout
		SCH.clearContext()
	}
	
	def configureAuroraCup () {
		println 'Configuring 2016 Aurora Cup tournament'
		// create via service
		Tournament tournament = new Tournament (name: "2016 Aurora Cup", venue: 'Vaughan Athletic Center', address: '2121 W. Indian Trail', city: "Aurora", state: "IL", 
			startDate: df.parse('05/16/2016'), endDate: df.parse('05/17/2016'), starLevel: 4)
		fillOtherTournamentDefaults (tournament, "Swavek Lorenc", "swaveklorenc@yahoo.com")
		tournament.lateEntryStartDate = df.parse ('04/27/2016')
		Map params1 = [:];
		tournamentService.create(tournament, params1)
		println 'tournament id for Aurora Cup is ' + tournament.id
		int ordinalNum = 0
		def eventsMap = [:]
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Open Doubles RR', day: 1, startTime: 18.5, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 32)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 3800 Doubles RR', day: 1, startTime: 18.5, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 32)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 18 Youth', day: 1, startTime: 18.5, maxPlayerAge: 17, feeAdult: 0, feeJunior: 20.0, maxEntries: 16))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Over 50 RR', day: 1, startTime: 18.5, minPlayerAge: 50, feeAdult: 32.0, feeJunior: 0, maxEntries: 20))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1500 RR', day: 1, startTime: 18.5, maxPlayerRating: 1499, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 28))
		
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Open Singles RR', day: 2, startTime: 9.0, feeAdult: 46.0, feeJunior: 46.0, maxEntries: 44)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1200 RR', day: 2, startTime: 9.0, maxPlayerRating: 1199, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 36))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1900 RR', day: 2, startTime: 11.0, maxPlayerRating: 1899, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 64)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1700 RR', day: 2, startTime: 11.0, maxPlayerRating: 1699, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 48))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1400 RR', day: 2, startTime: 11.5, maxPlayerRating: 1399, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 40))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 2000 RR', day: 2, startTime: 14.0, maxPlayerRating: 1999, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 64))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 14 Youth', day: 2, startTime: 14.0, maxPlayerAge: 14, feeAdult: 0, feeJunior: 28.0, maxEntries: 16))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 2300 RR', day: 2, startTime: 16.0, maxPlayerRating: 2299, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 64))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under  800 RR', day: 2, startTime: 17.5, maxPlayerRating: 799, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 32))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1800 RR', day: 2, startTime: 18.0, maxPlayerRating: 1799, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 64))
		
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 2600 RR', day: 3, startTime: 9.0, maxPlayerRating: 2599, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 32)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'40 and Over', day: 3, startTime: 9.0, minPlayerAge: 40, feeAdult: 32.0, feeJunior: 0, maxEntries: 28))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 2100 RR', day: 3, startTime: 11.0, maxPlayerRating: 2099, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 64))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1600 RR', day: 3, startTime: 11.0, maxPlayerRating: 1599, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 48))
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 2400 RR', day: 3, startTime: 13.0, maxPlayerRating: 2399, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 48)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1750 RR', day: 3, startTime: 13.5, maxPlayerRating: 1749, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 48)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 2200 RR', day: 3, startTime: 15.0, maxPlayerRating: 2199, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 64)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1000 RR', day: 3, startTime: 16.0, maxPlayerRating: 999, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 32)) 
		makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Women Singles RR', day: 3, startTime: 16.0, genderRestriction: GenderRestriction.FEMALE, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 8))
		// early start ??
		//makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1900 RR 9 AM start', day: 2, startTime: 9.0, maxPlayerRating: 1899, feeAdult: 32.0, feeJunior: 32.0, maxEntries: 8)) 
		//makeEvent(tournament, eventsMap, new Event(ordinalNumber: ++ordinalNum, name:'Under 1700 RR 9 AM start', day: 2, startTime: 9.0, maxPlayerRating: 1699, feeAdult: 28.0, feeJunior: 28.0, maxEntries: 8))
		
		importPlayers (tournament, eventsMap)
		
		return tournament
	}
	
	def makeEvent (Tournament tournament, Map eventsMap,  Event event) {
		event.tournament = tournament
		def paramsE = [:]
		eventService.create(event, paramsE)
		eventsMap[event.name] = event
	}
	
	def importPlayers (tournament, eventsMap) {
		def userRole = SecRole.findByAuthority ('ROLE_USER') ?: new SecRole (authority: 'ROLE_USER').save(failOnError: true)
		
		def registrationFileURL = "C:\\grails\\data\\AuroraCupPlayers.html"
		println 'Registering players for Aurora Cup'
		Date today = new Date()
		def page = new XmlSlurper(new org.cyberneko.html.parsers.SAXParser()).parse(registrationFileURL)
		int count = 0
		int fakeMemberId = 91000
		// find all nodes
		def node = page.'**'.grep {
			//	<tr>
			//		<td Align="Left" title="Club: Wheaton TTC"><a
			//			href="http://www.omnipong.com/T-tourney.asp?t=102&r=210&h=nh&e=11|24|"
			//			target="_self">Adarkwa, Sam</a></td>
			//		<td Align="Center">1542</td>
			//		<td Align="Left"><b title="Under 1800 RR">&nbsp;&nbsp;U1800</b>&nbsp;&nbsp;<b
			//			title="Over 50 RR">&nbsp;&nbsp;OVR50</b>&nbsp;&nbsp;</td>
			//	</tr>
//			 println "it.name() = " + it.name() + " it.@class.toString() = " + it.@class.toString()
			if (it.name().equals("TR")) {
				def tournamentEntry = new TournamentEntry()
				tournamentEntry.dateEntered = today
				tournamentEntry.membershipOption = 0
				tournamentEntry.tournament = tournament
				tournamentEntry = tournamentEntryService.create(tournamentEntry, [:])
				String cLastName = null
				String cFirstName = null
				//println "adding player "
				it.TD.eachWithIndex { td, index ->
					switch (index) {
						case 0:
							String club = td.@title.toString()
							//println 'club ' + club
							def a = td.A
							String lastFirstName = a.text()
							String [] names = lastFirstName.split(",")
							cLastName = names[0].trim()
							cFirstName = names[1].trim()
							//println "last name " + lastName + " first name " + firstName
						break;
						
						case 1:
							String rating = td.text()
							//println 'rating ' + rating
							tournamentEntry.eligibilityRating = rating as Integer
							tournamentEntry.seedRating = tournamentEntry.eligibilityRating
							break;
							
						case 2:
							//tournamentEntry.eventEntries = []
							//print "events "
							td.B.each { b ->
								String eventName = b.@title.toString().trim()
								//print eventName + " "
								def eventEntry = new EventEntry()
								eventEntry.status = EventEntry.EntryStatus.CONFIRMED
								eventEntry.dateEntered = today
								eventEntry.availabilityStatus = EventEntry.AvailabilityStatus.ENTERED
								int doubles = eventName.indexOf(' - Partner:');
								if (doubles != -1) {
									eventName = eventName.substring(0, doubles).trim()
								}
								eventEntry.event = eventsMap[eventName]
								if (eventEntry.event != null) {
									eventEntry.fee = eventEntry.event.feeAdult
									eventEntry.tournamentEntry = tournamentEntry
									eventEntryService.create(eventEntry, [:])
								} else {
									println 'event ' + eventName + " not found"
								}							
							}
							//println ""
						break;
					}
				}
//				String firstLastName = cFirstName + " " + cLastName
//				firstLastName = firstLastName.trim()
//				def userProfile = userProfileService.findByFirstLastName(firstLastName)
//				if (userProfile != null) {
////				def userProfiles = UserProfile.where {firstName == cFirstName && lastName == cLastName}.list()
////				if (userProfiles != null && userProfiles.size() >= 1) {
//					println 'profile FOUND for ' + cFirstName + " " + cLastName
////					def userProfile = userProfiles.get(0)
//					userProfile.addToTournamentEntries(tournamentEntry)
//					userProfile.save(failOnError: true)
////					if (userProfiles.size() > 1) {
////						println 'Duplicate profiles found for '  + cFirstName + " " + cLastName 
////					}
//				} else {
////					println "Creating user profile for " + cFirstName + " " + cLastName
					// now save the User profile so we can enter users in
//					fakeMemberId++
//					def userProfile = new UserProfile ()
//					userProfile.firstName = cFirstName
//					userProfile.lastName = cLastName
//					userProfile.dateOfBirth =  new Date()
//					userProfile.usattID = fakeMemberId
//					userProfile.expirationDate = new Date()
//					userProfile.email = 'abc@yahoo.com'
//					userProfile.phone = '630-111-22222'
//					userProfile.streetAddress = "123 Nice street"
//					userProfile.city = 'Aurora'
//					userProfile.state = 'IL'
//					userProfile.zipCode = '60504'
//					userProfile.country = 'USA'
//					userProfile.gender = 'M'
//					userProfile.club = 'FVTTC'
//					userProfile.save(failOnError: true)
//				
//					String userName = (!cFirstName.isEmpty())? cFirstName.toLowerCase().charAt(0) : ""
//					userName += cLastName.toLowerCase().trim()
//					userName += fakeMemberId
//					String email = userName + '@yahoo.com'
//					def user = SecUser.findByUsername(userName)
//					if (user == null) {
//						user = new SecUser()
//						user.username = userName
//						user.enabled = true
//						user.password = userName
//						user.email = email
//						user.save(failOnError: true)
//					}
//					if (!user.authorities.contains(userRole)) {
//						SecUserSecRole.create(user, userRole, true)
//					}
//					user.userProfile = userProfile
//					user.save(failOnError: true)
//				}
				
				count++
				if (count % 20 == 0) {
					println 'Registered ' + count + ' players'
					cleanUpGorm()
				}
			}
		}
		cleanUpGorm()
		
		println 'Registered a total of ' + count + ' players'
		Date end = new Date();
		TimeDuration td = TimeCategory.minus( end, today )

		println 'Done importing registrations ' + td 
	}
	

	def createUsers () {
		def userRole = SecRole.findByAuthority ('ROLE_USER') ?: new SecRole (authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = SecRole.findByAuthority ('ROLE_ADMIN') ?: new SecRole (authority: 'ROLE_ADMIN').save(failOnError: true)
		def tournamentDirectorRole = SecRole.findByAuthority ('ROLE_TOURNAMENT_DIRECTOR') ?: new SecRole (authority: 'ROLE_TOURNAMENT_DIRECTOR').save(failOnError: true)

		def user1 = SecUser.findByUsername('swavek') ?: new SecUser(username: 'swavek', enabled: true, password: 'swavek', email: 'swaveklorenc@yahoo.com').save(failOnError: true)
		if (!user1.authorities.contains(userRole)) {
			SecUserSecRole.create(user1, userRole, true)
		}
		if (!user1.authorities.contains(adminRole)) {
			SecUserSecRole.create(user1, adminRole, true)
		}

		def user2 = SecUser.findByUsername('matthew') ?: new SecUser(username: 'matthew', enabled: true, password: 'matthew', email: 'matthewlorenc@yahoo.com').save(failOnError: true)
		if (!user2.authorities.contains(userRole)) {
			SecUserSecRole.create(user2, userRole, true)
		}

		def user3 = SecUser.findByUsername('craig') ?: new SecUser(username: 'craig', enabled: true, password: 'craig', email: 'support@omnipong.com').save(failOnError: true)
		if (!user3.authorities.contains(userRole)) {
			SecUserSecRole.create(user3, userRole, true)
		}
		if (!user3.authorities.contains(tournamentDirectorRole)) {
			SecUserSecRole.create(user3, tournamentDirectorRole, true)
		}

		def user4 = SecUser.findByUsername('ed') ?: new SecUser(username: 'ed', enabled: true, password: 'ed', email: 'ed@landmarkbilling.com').save(failOnError: true)
		if (!user4.authorities.contains(userRole)) {
			SecUserSecRole.create(user4, userRole, true)
		}
		if (!user4.authorities.contains(tournamentDirectorRole)) {
			SecUserSecRole.create(user4, tournamentDirectorRole, true)
		}

		def userProfile1 = new UserProfile (firstName: 'Swavek', lastName: 'Lorenc', dateOfBirth: df.parse('10/11/1960'), email: 'swaveklorenc@yahoo.com', phone: '630-999-9980', streetAddress: '2458 Angela Ln', city: 'Aurora', state: 'IL', zipCode: '60103', gender: 'M', club: 'Fox Valley Table Tennis Club')
		userProfile1.usattID = 84639 
		userProfile1.expirationDate = df.parse ('01/31/2016')
		userProfile1.country = "USA"
		userProfile1.save(failOnError: true)
		user1.userProfile = userProfile1
		user1.save(failOnError: true)

		// not USATT member
		def userProfile2 = new UserProfile (firstName: 'Matthew', lastName: 'Lorenc', dateOfBirth: df.parse('12/11/1998'), email: 'matthewlorenc@yahoo.com', phone: '630-999-1180', streetAddress: '2223 New York Ave', city: 'Aurora', state: 'IL', zipCode: '60103', gender: 'M', club: 'Xilin Club')
		//userProfile2.expirationDate = df.parse ('12/31/2015')
		userProfile2.country = "USA"
		userProfile2.save(failOnError: true)
		user2.userProfile = userProfile2
		user2.save(failOnError: true)

		// expired membership
		def userProfile3 = new UserProfile (firstName: 'Craig', lastName: 'Krum', dateOfBirth: df.parse('01/11/1958'), email: 'craigkrum22@yahoo.com', phone: '630-111-1220', streetAddress: '111 Maple Ave', city: 'Sacramento', state: 'CA', zipCode: '90219', gender: 'M', club: 'Sacramento Table Tennis Club')
		userProfile3.usattID = 5470 
		userProfile3.expirationDate = df.parse ('06/30/2015')
		userProfile3.country = "USA"
		userProfile3.save(failOnError: true)
		user3.userProfile = userProfile3
		user3.save(failOnError: true)

		// life membership
		def userProfile4 = new UserProfile (firstName: 'Ed', lastName: 'Hogshead', dateOfBirth: df.parse('01/21/1962'), email: 'edh@yahoo.com', phone: '630-111-1220', streetAddress: '111 Maple Ave', city: 'Rocford', state: 'IL', zipCode: '60234', gender: 'M', club: 'Rockford Table Tennis Club')
		userProfile4.usattID = 6727
		userProfile4.expirationDate = df.parse ('12/31/2099')
		userProfile4.country = "Germany"
		userProfile4.save(failOnError: true)
		user4.userProfile = userProfile4
		user4.save(failOnError: true)
	}

	private void loginAsAdmin() {
		// have to be authenticated as an admin to create ACLs
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
				'swavek', 'swavek',
				AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
	}

	def grantPermissions() {
		def tournament = new Tournament (name: "2015 JOOLA Aurora Spring Open", venue: 'Eola Community Center', address: "555 S. Eola Rd.", city: "Aurora", state: "IL", startDate: df.parse('03/14/2015'), endDate: df.parse('03/14/2015'), starLevel: 2, )
		fillOtherTournamentDefaults (tournament, "Swavek Lorenc", "swaveklorenc@yahoo.com")
		tournament.save(failOnError: true)
		aclService.createAcl(objectIdentityRetrievalStrategy.getObjectIdentity(tournament))
		
		def tournament2 = new Tournament (name: "2015 Aurora Summer Open", , venue: 'Eola Community Center', address: "555 S. Eola Rd.", city: "Aurora", state: "IL", startDate: df.parse('06/20/2015'), endDate: df.parse('06/20/2015'), starLevel: 2)
		fillOtherTournamentDefaults (tournament2, "Swavek Lorenc", "swaveklorenc@yahoo.com")
		tournament2.save(failOnError: true)
		aclService.createAcl(objectIdentityRetrievalStrategy.getObjectIdentity(tournament2))

		def tournament3 = new Tournament (name: "2015 Meiklejohn North American Seniors Open", venue: 'Clubhouse 5', address: "Clubhouse 5, 24262 Punta Alta", city: "Laguna Woods", state: "CA", startDate: df.parse('06/04/2015'), endDate: df.parse('06/07/2015'), starLevel: 2)
		fillOtherTournamentDefaults (tournament3, "Craig Krum", "cragkrum@yahoo.com")
		tournament3.save(failOnError: true)
		aclService.createAcl(objectIdentityRetrievalStrategy.getObjectIdentity(tournament3))
		
		def tournament4 = new Tournament (name: "America's Team Championship", venue: 'Forest City Tennis Center', city: "Rockford", address: '7801 East State St', state: "IL",startDate: df.parse('05/23/2015'), endDate: df.parse('05/24/2015'), starLevel: 4)
		fillOtherTournamentDefaults (tournament4, "Ed Hogshead", "edhg@yahoo.com")
		tournament4.save(failOnError: true)
		aclService.createAcl(objectIdentityRetrievalStrategy.getObjectIdentity(tournament4))
		
		aclUtilService.addPermission tournament, 'swavek', ADMINISTRATION
		aclUtilService.addPermission tournament2, 'swavek', ADMINISTRATION
		aclUtilService.addPermission tournament3, 'craig', ADMINISTRATION
		aclUtilService.addPermission tournament3, 'swavek', ADMINISTRATION
		aclUtilService.addPermission tournament4, 'ed', ADMINISTRATION
		aclUtilService.addPermission tournament4, 'swavek', ADMINISTRATION
		
	}
	
	def fillOtherTournamentDefaults (Tournament tournament, String contactName, String contactEmail) {
		tournament.contactEmail = contactEmail
		tournament.contactName = contactName
		tournament.contactPhone = "630-123-4568"
//		tournament.websiteURL = "http://www.fvttc.net/sanctioned-tournaments/2016-tournaments/2016-aurora-cup.aspx"
//		tournament.blankEntryFormURL = 'http://www.fvttc.net/resources/site1/General/2016-Aurora-Cup/2016JOOLAAuroraCupBlankEntryForm.pdf'
//		tournament.tablesCount = 12
//		tournament.checkPayableTo = contactName 
//		tournament.checkMailingAddress = '1234 Nice Str' 
//		tournament.checkMailCity = tournament.city 
//		tournament.checkMailState = tournament.state 
//		tournament.checkMailZipCode = '60504-9589'
		def MILLIS_IN_DAY = 24 * 60 * 60 * 1000
//		def calendar = Calendar.instance
//		calendar.setTime tournament.startDate.time
		tournament.ratingCutoffDate = tournament.startDate - 35 
		tournament.lateEntryStartDate = tournament.startDate - 14
		tournament.entryCutoffDate = tournament.startDate - 7
		tournament.refundCutoffDate = tournament.startDate - 7
		//tournament.usattRatingFee = 5.0f
		tournament.adminFee = 5.0f
		tournament.lateEntryFee = 10.0f
		
		def account = createAccount ()
		account.tournament = tournament
		tournament.accounts = []
		tournament.accounts.add (account)
		tournament.stripeKey = account.stripePublicKey
	}
	
	def cleanUpGorm () {
		def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
		def session = sessionFactory.currentSession
		session.flush()
		session.clear()
		propertyInstanceMap.get().clear()

	}

	def importPlayerData() {
		Date start = new Date();
		
		println "Importing USATT player data"
		def xmlFile = "C:\\grails\\data\\USATT_Membership.xml"
		//def xmlFile = "C:\\grails\\newworkspace\\AuroraTMS\\USATT_Membership_Medium.xml"
		def parsedFile = new XmlSlurper().parse(xmlFile)
		assert parsedFile instanceof groovy.util.slurpersupport.GPathResult
		def count = 0;
		def dateFormatter = new SimpleDateFormat('M/d/yyyy') // 9/30/1958
		parsedFile.Table.each() {table ->
			count++
			
			// push into database to speed things up
			if (count % 100 == 0) {
				cleanUpGorm()
			}

			if (count % 1000 == 0) {
				println  count
			}
			
			//			println 'player ' + table.LastName.text() + ", " + table.FirstName.text()

			def dateOfBirth = table.DOB.text() != "" ? dateFormatter.parse(table.DOB.text()) : null
			def expirationDate = null
			String expDate = table.ExpirationDate.text();
			if (expDate == 'LIFE') {
				expirationDate = new Date ('12/31/2099');
			} else {
				expirationDate = (expDate != "") ? dateFormatter.parse(expDate) : null
			}
			def lastPlayed = table.LastPlayed.text() != "" ? dateFormatter.parse(table.LastPlayed.text()) : null
			int memberId = table.MemberID.toInteger();
			String lastName = table.LastName.text()
			String firstName = table.FirstName.text()
			String middleName = table.MiddleName.text() ?: ''
			String address1 = table.Address1.text() ?: ''
			String address2 = table.Address2.text() ?: ''
			String city = table.City.text() ?: ''
			String state = table.State.text() ?: ''
			String zipCode = table.ZipCode.text() ?: ''
			String country = table.Country.text() ?: 'USA'
			String gender = table.Sex.text() ?: 'M'
			int rating = table.Rating.toInteger();
			def usattProfile = new UsattProfile()
			usattProfile.lastName = lastName
			usattProfile.firstName = firstName
			usattProfile.middleName = middleName
			usattProfile.address1 = address1
			usattProfile.address2 = address2
			usattProfile.city = city
			usattProfile.state = state
			usattProfile.zipCode = zipCode
			usattProfile.country = country
			usattProfile.gender = gender
			usattProfile.dateOfBirth = dateOfBirth
			usattProfile.rating = rating
			usattProfile.expirationDate = expirationDate
			usattProfile.lastPlayedDate = lastPlayed
			usattProfile.save(failOnError: true)
			
		}

		cleanUpGorm()

		Date end = new Date();
		TimeDuration td = TimeCategory.minus( end, start )
		println "Done importing " + count + " player's records in " + td
		//		def playersList = UsattProfile.list(firstName: 'Swavek', lastName: 'Lorenc')
		//		println ' list size ' + playersList.size()
	}

	def destroy = {
		log.info "================ BootStrap #destroy ===================="
		log.info "Shut down application: ${displayName}"
	}
	
	def getKey = {
	 }
	
	def createAccount = {
		try
		{
			// read the keys for encoding cc transactions
			String propertiesFile = "C:\\grails\\data\\myprops.properties"
			Properties prop = new Properties();
			FileInputStream inputStream = new FileInputStream(propertiesFile)
			prop.load(inputStream);
			String pub_key = prop.getProperty("pub_key");
			String secret_key = prop.getProperty("secret_key");
			
			// temporary way to initialize payment account - later it will be done in tournament payment tab 
			def account = new Account ()
			account.gatewayType = Account.GatewayType.Stripe
			account.stripePublicKey = pub_key
			account.stripeSecretKey = secret_key
			account.save (flush: true)
			return account
		}
		catch (IOException e)
		{
			log.debug"Exception occured while reading properties file :"+e
		}
	}

}