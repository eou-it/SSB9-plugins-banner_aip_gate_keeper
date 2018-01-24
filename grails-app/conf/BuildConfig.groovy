/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

grails.project.class.dir = "target/classes"
grails.project.lib.dir = "lib"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

// When deploying a war it is important to exclude the Oracle database drivers.  Not doing so will
// result in the all-too-familiar exception:
// "Cannot cast object 'oracle.jdbc.driver.T4CConnection@6469adc7'... to class 'oracle.jdbc.OracleConnection'
grails.war.resources = {stagingDir ->
    delete( file: "${stagingDir}/WEB-INF/lib/ojdbc6.jar" )
}
grails.plugin.location.'i18n_core' = "../i18n_core.git"
grails.plugin.location.'banner-general-common' = "../banner_general_common.git"
grails.plugin.location.'banner-general-utility' = "../banner_general_utility.git"

grails.project.dependency.resolver = "maven" // or maven

grails.project.dependency.resolution = {

    inherits "global" // inherit Grails' default dependencies
    log "warn"        // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        if (System.properties['PROXY_SERVER_NAME']) {
            mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
        }
        grailsCentral()
        mavenCentral()
        mavenRepo "http://repository.jboss.org/maven2/"
    }


    dependencies {
    }

    plugins {
    }

}

// CodeNarc rulesets
codenarc.ruleSetFiles = "rulesets/banner.groovy"
codenarc.reportName = "target/CodeNarcReport.html"
codenarc.propertiesFile = "grails-app/conf/codenarc.properties"
