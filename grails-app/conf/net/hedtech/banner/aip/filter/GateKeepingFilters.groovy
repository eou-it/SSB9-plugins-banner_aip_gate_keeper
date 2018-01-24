/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.aip.filter

import net.hedtech.banner.aip.gatekeeping.UserBlockedProcessReadOnly
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.security.BannerUser
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.servlet.GrailsUrlPathHelper
import org.springframework.security.core.context.SecurityContextHolder

import javax.servlet.http.HttpSession

class GateKeepingFilters {
    private final Log log = LogFactory.getLog( this.getClass() )

    // Same as in GeneralSsbConfigService. Didn't want to create dependency on General App. This code needs to be consumable by Student Apps
    static final String GENERAL_SSB = 'GENERAL_SSB' // GORICCR_SQPR_CODE
    static final String ENABLE_ACTION_ITEMS = 'ENABLE.ACTION.ITEMS' // GORICCR_ICSN_CODE

    static final String BLOCKREGISTERFORCOURSES = '/ssb/term/termSelection?mode=registration'
    private static final String SLASH = "/"

    private static final String QUESTION_MARK = "?"


    def springSecurityService

    //def userBlockedProcessReadOnlyService

    def dependsOn = [net.hedtech.banner.security.AccessControlFilters.class]

    def filters = {
        actionItemFilter( controller: "selfServiceMenu|login|logout|error|dateConverter", invert: true ) {
            before = {
                if ('N' == IntegrationConfiguration.fetchByProcessCodeAndSettingName(
                        GENERAL_SSB, ENABLE_ACTION_ITEMS ).value) {
                    return true;
                }
                // FIXME: get urls from tables. Check and cache
                // only want to look at type 'document'? not stylesheet, script, gif, font, ? ?
                // at this point he getRequestURI returns the forwared dispatcher URL */aip/myplace.dispatch
                String path = getServletPath( request )
                log.info( "take a look at: " + request.getRequestURI() + " as user: " + userPidm )
                //if (!ApiUtils.isApiRequest() && !request.xhr) {
                if (isBlockingUrl( path )) { // checks path against list from DB
                    HttpSession session = request.getSession()
                    if (springSecurityService.isLoggedIn() && path != null) {
                        if (path.equals( BLOCKREGISTERFORCOURSES )) {
                            //if ('classRegistration'.equals( reqController ) && ! 'getTerms'.equals( reqAction )) {
                            // Test that we can get db items here with user info

                            // FIXME: pull in registration info (urls and session variable) from tables
                            // TODO: may need to look at session variable to see if student in Registration
                            log.info( "roleCode: " + session.getAttribute( 'selectedRole' )?.persona?.code )
                            // provide this limited set of values for personas in shipped data
                            // if ('STUDENT'.equals( session.getAttribute( 'selectedRole' )?.persona?.code )) { // FIXME: Handle persona requirements
                            if (true) {
                                def isBlocked = false
                                try {
                                    //isBlocked = UserBlockedProcessReadOnly.fetchBlockingProcessesROByPidm( userPidm)
                                    isBlocked = UserBlockedProcessReadOnly.fetchProcessesROByPidm( userPidm )
                                    // this doesn't check the block
                                    // indicator. just here for proof of concept
                                    log.info( "isBlocked: " + isBlocked + " for: " + userPidm )
                                } catch (Throwable t) {
                                    log.info( "isBlocked: service call failed. Keep an eye on this. Was failing intermittently. I think it is " +
                                                      "right now" )
                                    log.info( t )
                                }

                                if (isBlocked) {
                                    String uri = request.getScheme() + "://" +   // "http" + "://
                                            request.getServerName()
                                    //response.addHeader("Access-Control-Allow-Origin", "*");
                                    // FIXME: goto general app. We need to be able to look up the location
                                    // FIXME: make this configurable
                                    redirect( url: "http://csr-daily-1.ellucian.com:8080/BannerGeneralSsb/ssb/aip/informedList#/informedList" )
                                    //    redirect( url: uri + ":8090/StudentRegistrationSsb/ssb/registrationHistory/registrationHistory" )
                                    return false
                                }
                            }
                        }
                    }
                    return true
                }
            }
        }
    }

// who am I?
    private def getUserPidm() {
        def user = SecurityContextHolder?.context?.authentication?.principal
        if (user instanceof BannerUser) {
            //TODO:: extract necessary user information and return. (ex: remove pidm, etc)
            return user.pidm
        }
        return null
    }


    private getServletPath( request ) {
        GrailsUrlPathHelper urlPathHelper = new GrailsUrlPathHelper();
        String path = urlPathHelper.getOriginatingRequestUri( request );
        if (path != null) {
            path = path.substring( request.getContextPath().length() )
            if (SLASH.equals( path )) {
                path = null
            } else if (request?.getQueryString()) {
                path = path + QUESTION_MARK + request?.getQueryString()
            }
        }
        return path
    }

    // look a ThemeUtil for expiring cache pattern
    private boolean isBlockingUrl( String path ) {
        // compare to cached list, if exists (expiring?)
        // if not
        // call BlockedProcessReadOnlyService.getBlockedProcessUrlsAndActionItemIds()
        // create list
        // cache list
        return true
    }
}
