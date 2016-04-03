class UrlMappings {

	static mappings = {
		"/api/usattprofiles"(resources: "usattProfile")
		
		"/api/userprofiles"(resources: "userProfile")
		"/api/public/userprofiles/$id"(namespace: 'public', controller: 'userProfilePublic', action:'show', method: 'GET')
		"/api/public/userprofiles"    (namespace: 'public', controller: 'userProfilePublic', action:'index', method: 'GET')
		
		"/api/tournaments"(resources: "tournament") {
			"/events"(resources: "event")
			"/tournamententries"(resources: "tournamentEntry") 
		}
		
		"/api/tournamententries"(resources: "tournamentEntry") {
			"/evententries"(resources: "eventEntry")
		}
		
		"/api/accounts"(resources: "account")
		
		"/api/financialtransactions"(resources: "financialTransaction")
		
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
