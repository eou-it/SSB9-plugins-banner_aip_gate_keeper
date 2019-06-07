grails.config.locations = [
        BANNER_APP_CONFIG:           "banner_configuration.groovy",
]

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

grails.views.default.codec = "html" // none, html, base64  **** Charlie note: Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.databinding.useSpringBinder = true
grails.converters.domain.include.version = true
grails.converters.json.date = "javascript"
grails.converters.json.pretty.print = true

banner {
    sso {
        authenticationProvider = 'default'
        authenticationAssertionAttribute = 'udcId'
    }
}

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = false

// enable GSP preprocessing: replace head -> g:captureHead, title -> g:captureTitle, meta -> g:captureMeta, body -> g:captureBody
grails.views.gsp.sitemesh.preprocess = true

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
        commmgrDataSourceEnabled = false

    }
    production {

    }
}
