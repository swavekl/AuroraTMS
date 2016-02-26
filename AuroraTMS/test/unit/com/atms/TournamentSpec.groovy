package com.atms

import grails.test.mixin.TestFor
import java.text.SimpleDateFormat
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Tournament)
class TournamentSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }
    void "test simple"() {
		setup:
		def df = new SimpleDateFormat("MM/dd/yyyy")
		def tournament = new Tournament (name: "2017 Aurora Cup", venue: 'Vaughan Athletic Center', address: '2121 W. Indian Trail', city: "Aurora", state: "IL", startDate: df.parse('05/16/2016'), endDate: df.parse('05/17/2016'), starLevel: 4)
		tournament.save (flush: true)
		
		when:
		def event1 = new Event(ordinalNumber: 1, name:'Open Singles', day: 1, startTime: 9.0, feeAdult: 32.0, feeJunior: 28.0) 
		tournament.addToEvents(event1)
		tournament.save (flush: true)
		
		then:
		tournament.events.size() == 1
		tournament.events.getAt(0).name == 'Open Singles'
    }
}
