/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.aip.filter

import grails.util.Holders
import net.hedtech.banner.aip.gatekeeping.ProcessUrls
import net.hedtech.banner.aip.gatekeeping.UserBlockedProcessReadOnly
import net.hedtech.banner.general.overall.IntegrationConfiguration
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.servlet.GrailsUrlPathHelper

class GateKeepingFilters {
    private static final log = Logger.getLogger( this.class )
    // Same as in GeneralSsbConfigService. Didn't want to create dependency on General App. This code needs to be consumable by Student Apps
    def springSecurityService
    private static final String SLASH = '/'
    private static final String QUESTION_MARK = '?'
    private static final String YES = 'Y'
    private static final String NO = 'N'
    private static final String PERSONA_EVERYONE = 'EVERYONE'
    def dependsOn = [net.hedtech.banner.security.AccessControlFilters.class]
    def filters = {
        def BANNER_AIP_EXCLUDE_LIST = Holders.config.BANNER_AIP_EXCLUDE_LIST
        println 'BANNER_AIP_EXCLUDE_LIST ' + BANNER_AIP_EXCLUDE_LIST
        log.debug( "BANNER_AIP_EXCLUDE_LIST $BANNER_AIP_EXCLUDE_LIST" )
        actionItemFilter( controller: "$BANNER_AIP_EXCLUDE_LIST", invert: true ) {
            before = {
                println 'BANNER_AIP_EXCLUDE_LIST ' + BANNER_AIP_EXCLUDE_LIST
                String path = getServletPath( request )
                println 'The path requested ' + path
                log.debug( "The path requested $path" )
                if (!path) {
                    return true // No Path set then no need to redirect
                }
                if (!springSecurityService.isLoggedIn()) {
                    return true // No Action If not logged in
                }
                boolean isAipEnabled = false
                isAipEnabled = IntegrationConfiguration.fetchByProcessCodeAndSettingName( 'GENERAL_SSB', 'ENABLE.ACTION.ITEMS' ).value == YES
                println 'Is AIP Enabled ' + isAipEnabled
                log.debug( "Is AIP Enabled $isAipEnabled" )
                if (!isAipEnabled) {
                    return true // NO ACTION If AIP Not enabled
                }
                def urlList = []
                if (!servletContext['urlList']) {
                    println 'inside setting urlList in app context'
                    log.debug( "inside setting urlList in app context" )
                    urlList.addAll( ProcessUrls.fetchUrls() )
                    servletContext['urlList'] = urlList
                }
                urlList = servletContext['urlList']
                if (!urlList) {
                    return true // No Action it no process URL maintained in System
                }
                log.debug( "urlList from application context${servletContext['urlList']}" )
                println 'urlList from application context ' + servletContext['urlList']
                // get urls from tables. Check and cache
                // only want to look at type 'document'? not stylesheet, script, gif, font, ? ?
                // at this point he getRequestURI returns the forwared dispatcher URL */aip/myplace.dispatch
                boolean noUrlToCheck = urlList.find {it.contains( path )} == null
                println 'No Url to check ' + noUrlToCheck
                log.debug( "No Url to check $noUrlToCheck" )
                if (noUrlToCheck) {
                    return true // No Action it requested path is not among URL
                }
                def persona = session.getAttribute( 'selectedRole' )?.persona?.code
                println 'Persona  ' + persona
                log.debug( "Persona $persona" )
                def isBlockingUrl = isBlockingUrl( springSecurityService.getAuthentication().user.pidm, path, persona )
                println 'isBlockingUrl  ' + isBlockingUrl
                log.debug( "isBlockingUrl $isBlockingUrl" )
                if (!isBlockingUrl) {
                    return true // No Action if no process process
                }
                redirect( url: Holders.config.GENERALLOCATION + '/ssb/aip/informedList#/informedList' )
            }
        }
    }

    /**
     *
     * @param request
     * @return
     */
    private static def getServletPath( request ) {
        GrailsUrlPathHelper urlPathHelper = new GrailsUrlPathHelper()
        String path = urlPathHelper.getOriginatingRequestUri( request )
        if (path != null) {
            path = path.substring( request.getContextPath().length() )
            if (SLASH == path) {
                path = null
            } else if (request?.getQueryString()) {
                path = path + QUESTION_MARK + request?.getQueryString()
            }
        }
        path
    }

    // look a ThemeUtil for expiring cache pattern
    private boolean isBlockingUrl( long pidm, String path, String persona ) {
        println "param => pidm: $pidm , path : $path, persona : $persona"
        log.debug( "param => pidm: $pidm , path : $path, persona : $persona" )
        List<UserBlockedProcessReadOnly> blockedActionItemList = UserBlockedProcessReadOnly.fetchBlockedProcesses( pidm )
        if (!blockedActionItemList) {
            return false
        }
        log.debug( "Inside isBlockingUrl blockedActionItemList size ${blockedActionItemList.size()}" )
        println 'Inside isBlockingUrl blockedActionItemList size ' + blockedActionItemList.size()
        def isBlockProcess = blockedActionItemList.find {it.processGlobalBlockInd == YES}?.processGlobalBlockInd == YES
        println 'isBlockProcess' + isBlockProcess
        log.debug( "isBlockProcess $isBlockProcess" )
        if (isBlockProcess) {
            return true // Blocked process if any assigned action item has been associated the global block process
        }
        blockedActionItemList.retainAll() {
            it.processGlobalBlockInd == NO
        }
        println 'blockedActionItemList size with No global block' + blockedActionItemList.size()
        log.debug( "blockedActionItemList size with No global block ${blockedActionItemList.size()}" )
        blockedActionItemList.find() {checkUrl( it, path, persona )} != null
    }

    /**
     *
     * @param param
     * @param pathParam
     * @param pathPersona
     * @return
     */
    private static boolean checkUrl( def param, String pathParam, String pathPersona ) {
        if (param.blockedProcessUrl == pathParam) {
            if (param.processPersonaBlkdAllowed == YES) {
                return param.persona == PERSONA_EVERYONE || param.persona == pathPersona
            }
            return true
        }
        false
    }
}
