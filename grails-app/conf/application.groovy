/*********************************************************************************
 Copyright 2018-2019 Ellucian Company L.P. and its affiliates .
 **********************************************************************************/


// ******************************************************************************
//
//                       +++ EXTERNALIZED CONFIGURATION +++
//
// ******************************************************************************
//
// Config locations should be added to the map used below. They will be loaded based upon this search order:
// 1. Load the configuration file if its location was specified on the command line using -DmyEnvName=myConfigLocation
// 2. Load the configuration file if it exists within the user's .grails directory (i.e., convenient for developers)
// 3. Load the configuration file if its location was specified as a system environment variable
//
//Added for integration tests to run in plugin level
grails.config.locations = [
        BANNER_APP_CONFIG:           "banner_configuration.groovy"
]

dataSource {
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    loggingSql = false
}

hibernate {
    dialect = "org.hibernate.dialect.Oracle10gDialect"
    show_sql = false
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'org.hibernate.cache.SingletonEhCacheRegionFactory'
    flush.mode = AUTO
    packagesToScan="net.hedtech.**.*"
    config.location = [
            "classpath:hibernate-banner-core.cfg.xml",
            "classpath:hibernate-banner-general-utility.cfg.xml",
            "classpath:hibernate-banner-general-common.cfg.xml",
            "classpath:hibernate-banner-general-person.cfg.xml",
            "classpath:hibernate-banner-general-validation-common.cfg.xml"
    ]
}

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        ssbEnabled = true
        ssbOracleUsersProxied = true
        commmgrDataSourceEnabled = true

    }
    test {
        ssbEnabled = true
        ssbOracleUsersProxied = true
        commmgrDataSourceEnabled = true

    }
    production {

    }
}


