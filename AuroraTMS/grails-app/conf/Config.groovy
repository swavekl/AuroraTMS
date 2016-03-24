// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = 'com.atms' // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
    all:           '*/*', // 'all' maps to '*' or the first available format in withFormat
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    hal:           ['application/hal+json','application/hal+xml'],
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
//	assets {
//		angular {
//			// Defaults
//			templateFolder = "templates"
//			compressHtml = true
//			preserveHtmlComments = false
//			includePathInName = false
//		}
//	}
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false

// to make ecache work with grails 2.5.0
grails{
    cache {
        order = 2000 // higher than default (1000) and plugins, usually 1500
        enabled = true
        clearAtStartup=true // reset caches when redeploying
        ehcache {
            // ehcacheXmlLocation = 'classpath:ehcache.xml' // no custom xml config location (no xml at all)
            reloadable = false
        }
    }
}

def uniqueCacheManagerName = appName + "ConfigEhcache-" + System.currentTimeMillis()
 
// customize temp ehcache cache manager name upon startup
grails.cache.ehcache.cacheManagerName = uniqueCacheManagerName
 
grails.cache.config = {
    provider {
        updateCheck false
        monitoring 'on'
        dynamicConfig false
        // unique name when configuring caches
        name uniqueCacheManagerName
    }
    defaultCache {
        maxElementsInMemory 10000
        eternal false
        timeToIdleSeconds 120
        timeToLiveSeconds 120
        overflowToDisk false // no disk use, this would require more config
        maxElementsOnDisk 10000000
        diskPersistent false
        diskExpiryThreadIntervalSeconds 120
        memoryStoreEvictionPolicy 'LRU' // least recently used gets kicked out
    }
}

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j.main = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
		   
//	debug   'grails.plugin.springsecurity',
//			'grails.app.controllers.grails.plugin.springsecurity',
//			'grails.app.services.grails.plugin.springsecurity',
//			'org.pac4j',
//			'org.springframework.security'
		   
		   // Enable Hibernate SQL logging with param values,
		   // this is in addition to 'logSql = true' in DataSource for development environment data source
//		   trace 'org.hibernate.type'
//		   trace 'org.hibernate.SQL'
//		   debug 'org.hibernate.transaction'
//		   debug 'org.springframework.transaction.support'
	   
}

grails.plugin.springsecurity.portMapper.httpPort = 8080
grails.plugin.springsecurity.portMapper.httpsPort = 8443
grails.plugin.springsecurity.auth.forceHttps = true

// Added by the Spring Security Core plugin:
grails {
	plugin {
		springsecurity {
			userLookup.userDomainClassName = 'com.atms.SecUser'
			userLookup.authorityJoinClassName = 'com.atms.SecUserSecRole'
			authority.className = 'com.atms.SecRole'
			rememberMe.key = 'fvttc'
			rememberMe.tokenValiditySeconds = 86400

			// this is defualt			
//			rejectIfNoRule = true
//			fii.rejectPublicInvocations = false
//			securityConfigType = "Annotation"   // default

			roleHierarchy = '''
				ROLE_ADMIN > ROLE_TOURNAMENT_DIRECTOR
				ROLE_TOURNAMENT_DIRECTOR > ROLE_USER
				'''

			secureChannel {
				useHeaderCheckChannelSecurity = true
//				secureHeaderName = 'X-FORWARDED-PROTO'
//				secureHeaderValue = 'http'
//				insecureHeaderName = 'X-FORWARDED-PROTO'
//				insecureHeaderValue = 'https'
				
				// one of REQUIRES_INSECURE_CHANNEL
				definition = [
					'/':                              'REQUIRES_SECURE_CHANNEL',
					'/index':                         'ANY_CHANNEL',
					'/index.gsp':                     'REQUIRES_SECURE_CHANNEL',
					'/assets/**':                     'REQUIRES_SECURE_CHANNEL',
					'/**/js/**':                      'REQUIRES_SECURE_CHANNEL',
					'/**/css/**':                     'ANY_CHANNEL',
					'/**/images/**':                  'ANY_CHANNEL',
					'/**/favicon.ico':                'ANY_CHANNEL',
					'/dbconsole/**':         		  'REQUIRES_SECURE_CHANNEL',
					'/register/**':                   'REQUIRES_SECURE_CHANNEL',
					'/**':                            'REQUIRES_SECURE_CHANNEL'
				]
			}
			
			// when annotation is used these are extra static rules in addition to those done via annotations
			controllerAnnotations.staticRules = [
				'/':                              ['permitAll'],
				'/index':                         ['permitAll'],
				'/index.gsp':                     ['permitAll'],
				'/assets/**':                     ['permitAll'],
				'/**/js/**':                      ['permitAll'],
				'/**/css/**':                     ['permitAll'],
				'/**/images/**':                  ['permitAll'],
				'/**/favicon.ico':                ['permitAll'],
				'/dbconsole/**':         		  ['ROLE_ADMIN'],
				'/register/**':                   ['permitAll']
//				'/api/logout':                 ['isAuthenticated()']
			]
		}
	}
}

grails.plugin.springsecurity.filterChain.chainMap = [
	// anonymous
//                    '/api/guest/**': 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor',
//                    '/api/**': 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter',
//                    '/**': 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'
	
	'/api/**': 'JOINED_FILTERS,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter',  // Stateless chain
	'/**': 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'                                                                          // Traditional chain
]

grails.plugin.springsecurity.rest.login.active=true
//grails.plugin.springsecurity.rest.login.endpointUrl="j_spring_security_check"
//grails.plugin.springsecurity.rest.login.endpointUrl="/api/login"
//grails.plugin.springsecurity.rest.login.failureStatusCode=401

// from JSON request
grails.plugin.springsecurity.rest.login.useJsonCredentials=false
//grails.plugin.springsecurity.rest.login.usernamePropertyName="username"
//grails.plugin.springsecurity.rest.login.passwordPropertyName="password"
// from request params
grails.plugin.springsecurity.rest.login.useRequestParamsCredentials=true
//grails.plugin.springsecurity.rest.login.usernamePropertyName="username"
//grails.plugin.springsecurity.rest.login.passwordPropertyName="password"
// logout config
//grails.plugin.springsecurity.rest.logout.endpointUrl="/api/logout"
// token validation
grails.plugin.springsecurity.rest.token.validation.useBearerToken=true
//grails.plugin.springsecurity.rest.token.validation.enableAnonymousAccess = true
            
//grails.plugin.springsecurity.rest.token.validation.headerName="X-Auth-Token"

//grails.plugin.springsecurity.rest.token.storage.useGorm = true
//grails.plugin.springsecurity.rest.token.storage.gorm.tokenDomainClassName = 'com.atms.AuthenticationToken'
//
//// token generation strategy
//grails.plugin.springsecurity.rest.token.generation.useSecureRandom=true
//grails.plugin.springsecurity.rest.token.generation.useUUID=false

grails {
	//And the configuration for sending via a Yahoo account:
	   mail {
		  host = "smtp.mail.yahoo.com"
		  port = 465
		  username = "swaveklorenc"
		  password = "Gadocha1972"
		  props = [ "mail.smtp.auth":"true",
						   "mail.smtp.socketFactory.port":"465",
						   "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
						   "mail.smtp.socketFactory.fallback":"false"  ]
	   }
	}
	
	grails.plugin.springsecurity.ui.register.emailFrom = 'swaveklorenc@yahoo.com'
	grails.plugin.springsecurity.ui.register.postRegisterUrl = "registerVerified"
	//grails.plugin.springsecurity.ui.password.validationRegex = '^.*(?=.*\\d)(?=.*[a-zA-Z])$'
	
	// specify date formats submitted by Angular JS $resource calls and accepted by REST implementation e.g. - 1960-10-26T00:00:00.000Z
	grails.databinding.dateFormats = ['MM/dd/yyyy', 'yyyy-MM-dd HH:mm:ss.S', "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", "yyyy-MM-dd'T'hh:mm:ss'Z'"]

// staging directory where ratings file will be downloaded
ratingsStagingDir="c:\\grails\\data\\ratings"