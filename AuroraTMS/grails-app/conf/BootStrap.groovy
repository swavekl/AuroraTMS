import groovy.time.*

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date;

import javax.swing.text.DateFormatter

import liquibase.util.file.FilenameUtils;
import net.sf.ehcache.CacheManager

import com.atms.Event.GenderRestriction;
import com.atms.Account
import com.atms.SecRole
import com.atms.SecUser
import com.atms.SecUserSecRole
import com.atms.Tournament
import com.atms.Event
import com.atms.UsattProfile
import com.atms.UserProfile

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
import static org.springframework.security.acls.domain.BasePermission.DELETE
import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH

class BootStrap {
	
	def aclService
	def aclUtilService
	def objectIdentityRetrievalStrategy
	def sessionFactory
	def tournamentService
	def eventService

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
		

		// create via service
		def tournament5 = new Tournament (name: "2016 Aurora Cup", venue: 'Vaughan Athletic Center', address: '2121 W. Indian Trail', city: "Aurora", state: "IL", startDate: df.parse('05/16/2016'), endDate: df.parse('05/17/2016'), starLevel: 4)
		fillOtherTournamentDefaults (tournament5, "Swavek Lorenc", "swaveklorenc@yahoo.com")
		
		tournament5.lateEntryStartDate = df.parse ('04/27/2016')
		Map params1 = [:];
		tournamentService.create(tournament5, params1)
		
		Map paramsE = [:];
		
		def event1 = new Event(ordinalNumber: 1, name:'Open Singles', day: 1, startTime: 9.0, feeAdult: 32.0, feeJunior: 28.0) 
		event1.tournament = tournament5
		eventService.create(event1, paramsE)
		
		def event2 = new Event(ordinalNumber: 2, name:'U2600', day: 1, startTime: 11.5, maxPlayerRating: 2599, feeAdult: 32.0, feeJunior: 28.0) 
		event2.tournament = tournament5
		eventService.create(event2, paramsE)

		def event3 = new Event(ordinalNumber: 3, name:'U2200', day: 2, startTime: 16.0, maxPlayerRating: 2199, feeAdult: 32.0, feeJunior: 28.0) 
		event3.tournament = tournament5
		eventService.create(event3, paramsE)
		
		def event4 = new Event(ordinalNumber: 4, name:'U2100', day: 2, startTime: 14.0, maxPlayerRating: 2099, feeAdult: 32.0, feeJunior: 28.0) 
		event4.tournament = tournament5
		eventService.create(event4, paramsE)
		
		def event5 = new Event(ordinalNumber: 5, name:'U1900', day: 1, startTime: 11.5, maxPlayerRating: 1899, feeAdult: 28.0, feeJunior: 25.0) 
		event5.tournament = tournament5
		eventService.create(event5, paramsE)
		
		def event6 = new Event(ordinalNumber: 6, name:'U1700', day: 2, startTime: 9.0, maxPlayerRating: 1699, feeAdult: 28.0, feeJunior: 25.0) 
		event6.tournament = tournament5
		eventService.create(event6, paramsE)
		
		def event7 = new Event(ordinalNumber: 7, name:'U1600', day: 2, startTime: 11.0, maxPlayerRating: 1599, feeAdult: 28.0, feeJunior: 25.0)
		event7.tournament = tournament5
		eventService.create(event7, paramsE)
		
		def event8 = new Event(ordinalNumber: 8, name:'Women Singles', day: 2, startTime: 11.0, genderRestriction: GenderRestriction.FEMALE, feeAdult: 25.0, feeJunior: 22.0)
		event8.tournament = tournament5
		eventService.create(event8, paramsE)

		def event9 = new Event(ordinalNumber: 9, name:'Boys Under 18', day: 1, startTime: 9.0, genderRestriction: GenderRestriction.MALE, maxPlayerAge: 17, feeAdult: 0, feeJunior: 20.0)
		event9.tournament = tournament5
		eventService.create(event9, paramsE)

		def event10 = new Event(ordinalNumber: 10, name:'40 and Over', day: 1, startTime: 11.0, minPlayerAge: 40, feeAdult: 32.0, feeJunior: 0)
		event10.tournament = tournament5
		eventService.create(event10, paramsE)

		sessionFactory.currentSession.flush()
		
		def paramsT = [ tournament : tournament5.id]
		def tournamentCheck = tournamentService.list(paramsT)
		def events = tournamentCheck.events

		// logout
		SCH.clearContext()

		// have to be authenticated as an admin to create ACLs
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
				'ed', 'ed',
				AuthorityUtils.createAuthorityList('ROLE_TOURNAMENT_DIRECTOR'))

		def tournament6 = new Tournament (name: "U.S.Open", venue: 'Las Vegas Convention Center', address: '123 W. Strip Ave', city: "Las Vegas", state: "NV", startDate: df.parse('07/11/2015'), endDate: df.parse('07/16/2015'), starLevel: 5)
		fillOtherTournamentDefaults (tournament6, "Tiffany Oldland", "tiffanyol@yahoo.com")
		tournamentService.create(tournament6, params1)

		importPlayerData()
		sessionFactory.currentSession.flush()

		// logout
		SCH.clearContext()
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
		tournament.accounts = []
		tournament.accounts.add (account)
		tournament.stripeKey = account.stripePublicKey
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
			new UsattProfile(memberId: memberId,
			lastName: lastName,
			firstName: firstName,
			middleName: middleName,
			address1: address1,
			address2: address2,
			city: city,
			state: state,
			zipCode: zipCode,
			country: country,
			gender: gender,
			dateOfBirth: dateOfBirth,
			rating: rating,
			expirationDate: expirationDate,
			lastPlayedDate: lastPlayed
			)
			.save(failOnError: true)
		}
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