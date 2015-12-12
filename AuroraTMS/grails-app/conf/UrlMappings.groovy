class UrlMappings {

	static mappings = {
		"/api/usattprofiles"(resources: "usattProfile")
		"/api/userprofiles"(resources: "userProfile")
		"/api/tournaments"(resources: "tournament") {
			"/events"(resources: "event")
			"/tournamententries"(resources: "tournamentEntry") 
		}
		"/api/tournamententries"(resources: "tournamentEntry") {
			"/evententries"(resources: "eventEntry")
		}

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
//		'/templates/intro'(view:'/templates/_intro')
		
		
	}
}
