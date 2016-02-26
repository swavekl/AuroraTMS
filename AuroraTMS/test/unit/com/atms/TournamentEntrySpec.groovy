package com.atms

//import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import spock.lang.Specification
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date;

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestMixin(HibernateTestMixin)
@Domain([TournamentEntry, EventEntry, Tournament, Event, FinancialTransaction, Account])
class TournamentEntrySpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

	void "Test count people"() {
		expect: "Test execute Hibernate count query"
			TournamentEntry.count() == 0
			sessionFactory != null
			transactionManager != null
			session != null
	}
/*	
	void "test something"() {
		setup:
//		def df = new SimpleDateFormat("MM/dd/yyyy")
//		Tournament t = new Tournament (name: "2016 Aurora Cup", venue: 'Vaughan Athletic Center', address: '2121 W. Indian Trail', city: "Aurora", state: "IL", startDate: df.parse('05/16/2016'), endDate: df.parse('05/17/2016'), starLevel: 4)
////		if (!t.validate()) {
////			println t.errors
////		}
//		t.save (flush: true)
		def te = new TournamentEntry (dateEntered: new Date(), eligibilityRating: 1234, seedRating: 1223)
		te.save (flush: true)

		when:
		println 'TournamentEntry ' + te.id
		def ee1 = new EventEntry(dateEntered: new Date(), fee: 0.0, status: EventEntry.EntryStatus.PENDING, availabilityStatus: EventEntry.AvailabilityStatus.AVAILABLE, tournamentEntry: te)
		te.addToEventEntries(ee1)
		te.save flush: true
		
		then:
		te.eventEntries.size() == 1
		def te2 = TournamentEntry.get(teid)
		te2.eventEntries.size() == 1
    }
    */
}
