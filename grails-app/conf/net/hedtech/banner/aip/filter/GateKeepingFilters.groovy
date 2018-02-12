/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.aip.filter

import grails.util.Holders
import net.hedtech.banner.aip.gatekeeping.ProcessUrls
import net.hedtech.banner.aip.gatekeeping.UserBlockedProcessReadOnly
import net.hedtech.banner.general.overall.IntegrationConfiguration
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.servlet.GrailsUrlPathHelper

class GateKeepingFilters {
    private final Log log = LogFactory.getLog( this.getClass() )
    // Same as in GeneralSsbConfigService. Didn't want to create dependency on General App. This code needs to be consumable by Student Apps
    def springSecurityService
    private static final String SLASH = '/'
    private static final String QUESTION_MARK = '?'
    private static final String YES = 'Y'
    private static final String NO = 'N'
    private static final String PERSONA_EVERYONE = 'EVERYONE'
    def dependsOn = [net.hedtech.banner.security.AccessControlFilters.class]
    def filters = {
        def BANNER_AIP_EXCLUDE_LIST = 'selfServiceMenu|login|logout|error|dateConverter'
        //def BANNER_AIP_EXCLUDE_LIST = Holders.config.BANNER_AIP_EXCLUDE_LIST
        println 'BANNER_AIP_EXCLUDE_LIST ' + BANNER_AIP_EXCLUDE_LIST
        actionItemFilter( controller: "$BANNER_AIP_EXCLUDE_LIST", invert: true ) {
            before = {
                if (!springSecurityService.isLoggedIn()) {
                    return true // No Action If not logged in
                }
                boolean isAipEnabled = false
                if (!session['isAipEnabled']) {
                    session['isAipEnabled'] = IntegrationConfiguration.fetchByProcessCodeAndSettingName( 'GENERAL_SSB', 'ENABLE.ACTION.ITEMS' ).value == 'Y'
                }
                isAipEnabled = session['isAipEnabled']
                println 'Is AIP Enabled ' + isAipEnabled
                if (!isAipEnabled) {
                    return true // NO ACTION If AIP Not enabled
                }
                def urlList = []
                if (!servletContext['urlList']) {
                    println 'inside setting urlList in app context'
                    urlList.addAll( ProcessUrls.fetchUrls() )
                    servletContext['urlList'] = urlList
                }
                urlList = servletContext['urlList']
                if (!urlList) {
                    return true // No Action it no process URL maintained in System
                }
                println 'urlList from application context ' + servletContext['urlList']
                // get urls from tables. Check and cache
                // only want to look at type 'document'? not stylesheet, script, gif, font, ? ?
                // at this point he getRequestURI returns the forwared dispatcher URL */aip/myplace.dispatch
                String path = getServletPath( request )
                println 'The path requested ' + path
                boolean noUrlToCheck = urlList.find {it.contains( path )} == null
                println 'No Url to check ' + noUrlToCheck
                if (noUrlToCheck) {
                    return true // No Action it requested path is not among URL
                }
                def persona = session.getAttribute( 'selectedRole' )?.persona?.code
                println 'Persona  ' + persona
                def isBlockingUrl = isBlockingUrl( springSecurityService.getAuthentication().user.pidm, path, persona )
                println 'isBlockingUrl  ' + isBlockingUrl
                if (!isBlockingUrl) {
                    return true // No Action if no process process
                }
                response.addHeader( 'Access-Control-Allow-Origin', '*' )
                String base = Holders.config.GENERALLOCATION
                println 'base ' + base
                def finalUrl = base + '/ssb/aip/informedList#/informedList'
                println 'Final Url ' + finalUrl
                redirect( url: finalUrl )
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
        List<UserBlockedProcessReadOnly> blockedActionItemList = UserBlockedProcessReadOnly.fetchBlockedProcesses( pidm )
        if (!blockedActionItemList) {
            return false
        }
        println 'Inside isBlockingUrl blockedActionItemList size ' + blockedActionItemList.size()
        def isBlockProcess = blockedActionItemList.find {it.processGlobalBlockInd == YES}?.processGlobalBlockInd == YES
        println 'isBlockProcess' + isBlockProcess
        if (isBlockProcess) {
            return true // Blocked process if any assigned action item has been associated the global block process
        }
        blockedActionItemList.retainAll() {
            it.processGlobalBlockInd == NO
        }
        println 'blockedActionItemList size with No global block' + blockedActionItemList.size()
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
                return param == PERSONA_EVERYONE || param.persona == pathPersona
            }
            return true
        }
        false
    }
}
