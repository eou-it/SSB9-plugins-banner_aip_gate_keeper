/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/


import net.hedtech.banner.configuration.ApplicationConfigurationUtils as ConfigFinder

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
// Map [ environment variable or -D command line argument name : file path ]
grails.config.locations = [] // leave this initialized to an empty list, and add your locations
// in the APPLICATION CONFIGURATION section below.
def locationAdder = ConfigFinder.&addLocation.curry( grails.config.locations )

[BANNER_APP_CONFIG           : "banner_configuration.groovy",
 WEB_APP_EXTENSIBILITY_CONFIG: "WebAppExtensibilityConfig.class"
].each {envName, defaultFileName -> locationAdder( envName, defaultFileName )}

//[bannerGrailsAppConfig: "${userHome}/.grails/banner_configuration.groovy",
//        customRepresentationConfig: "grails-app/conf/CustomRepresentationConfig.groovy",
//].each { envName, defaultFileName -> locationAdder(envName, defaultFileName) }

grails.databinding.useSpringBinder = true
// You must create a small configuration file named 'banner_on_grails-local-config.groovy' (as referenced
// in the above map defining externalized configuration files) that contains your own specific
// configuration (e.g., URIs, usernames, etc.).  Following is an example of this file:


grails.project.groupId = "net.hedtech" // used when deploying to a maven repo

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html         : ['text/html', 'application/xhtml+xml'],
                     xml          : ['text/xml', 'application/xml', 'application/vnd.sungardhe.student.v0.01+xml'],
                     text         : 'text/plain',
                     js           : 'text/javascript',
                     rss          : 'application/rss+xml',
                     atom         : 'application/atom+xml',
                     css          : 'text/css',
                     csv          : 'text/csv',
                     all          : '*/*',
                     json         : ['application/json', 'text/json'],
                     form         : 'application/x-www-form-urlencoded',
                     multipartForm: 'multipart/form-data'
]

// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64  **** Charlie note: Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.databinding.useSpringBinder = true
grails.converters.domain.include.version = true
grails.converters.json.date = "javascript"
grails.converters.json.pretty.print = true

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = false

// enable GSP preprocessing: replace head -> g:captureHead, title -> g:captureTitle, meta -> g:captureMeta, body -> g:captureBody
grails.views.gsp.sitemesh.preprocess = true

environments {
    test {
        ssbEnabled = true
        ssbOracleUsersProxied = true
    }
}

banner {
    sso {
        authenticationProvider = 'default'
        authenticationAssertionAttribute = 'udcId'
    }
}
// ******************************************************************************
//
//                       +++ DATA ORIGIN CONFIGURATION +++
//
// ******************************************************************************
// This field is a Banner standard, along with 'lastModifiedBy' and lastModified.
// These properties are populated automatically before an entity is inserted or updated
// within the database. The lastModifiedBy uses the username of the logged in user,
// the lastModified uses the current timestamp, and the dataOrigin uses the value
// specified here:
dataOrigin = "Banner"
grails.plugin.springsecurity.useRequestMapDomainClass = false

