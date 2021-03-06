dataSource {
    pooled = true
    jmxExport = true
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
//    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
//    cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
	cache.region.factory_class = 'grails.plugin.cache.ehcache.hibernate.BeanEhcacheRegionFactory4' // For EhCache method caching + Hibernate 4.0 and higher	
	singleSession = true // configure OSIV singleSession mode
    flush.mode = 'manual' // OSIV session flush mode outside of transactional context
//      format_sql = true
//      use_sql_comments = true
}

// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
	        dialect = com.atms.ImprovedH2Dialect
		    driverClassName = "org.h2.Driver"
		    username = "sa"
		    password = ""
			//logSql = true
        }
    }
    test {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
       		url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
	        dialect = com.atms.ImprovedH2Dialect
		    driverClassName = "org.h2.Driver"
		    username = "sa"
		    password = ""

//            dbCreate = "update"
//            dbCreate = "create-drop"
//			url = "jdbc:mysql://localhost/auroratms_test"
//			driverClassName = "com.mysql.jdbc.Driver"
//			dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
//		    username = "atmsuser"
//		    password = "atmsuser2015"
        }
    }
    production {
        dataSource {
//            dbCreate = "create-drop"
            dbCreate = "update"
			url = "jdbc:mysql://localhost/auroratms"
			driverClassName = "com.mysql.jdbc.Driver"
			dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
		    username = "atmsuser"
		    password = "atmsuser2015"

            properties {
               // See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
				jmxEnabled = true
               initialSize = 5
               maxActive = 50
               minIdle = 5
               maxIdle = 25
               maxWait = 10000
               maxAge = 10 * 60000
               timeBetweenEvictionRunsMillis = 5000
               minEvictableIdleTimeMillis = 60000
               validationQuery = "SELECT 1"
               validationQueryTimeout = 3
               validationInterval = 15000
               testOnBorrow = true
               testWhileIdle = true
               testOnReturn = false
               jdbcInterceptors = "ConnectionState"
               defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
            }
        }
    }
}
