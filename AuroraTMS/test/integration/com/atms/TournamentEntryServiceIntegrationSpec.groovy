package com.atms

import grails.test.spock.IntegrationSpec
import spock.lang.Specification


class TournamentEntryServiceIntegrationSpec extends IntegrationSpec {

	def setup() {
	}

	def cleanup() {
	}

	void "test something"() {
		setup:
		int x = 5
		
		when:
		x = x + 3
		
		then:
		x == 8
		
	}
}
