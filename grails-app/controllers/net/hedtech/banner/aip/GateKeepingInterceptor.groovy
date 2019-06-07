/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.aip

import grails.util.Holders
import net.hedtech.banner.aip.gatekeeping.ProcessUrls
import net.hedtech.banner.aip.gatekeeping.UserBlockedProcessReadOnly
import org.grails.web.servlet.GrailsUrlPathHelper



class GateKeepingInterceptor {
    //TODO check the precedence using constants
    int order = HIGHEST_PRECEDENCE + 50


    def springSecurityService
    private static final String SLASH = '/'
    private static final String QUESTION_MARK = '?'
    private static final String YES = 'Y'
    private static final String NO = 'N'
    private static final String ENABLED = 'ENABLED'
    private static final String DISABLED = 'DISABLED'
    private static final String PERSONA_EVERYONE = 'EVERYONE'


    GateKeepingInterceptor(){
        def BANNER_AIP_EXCLUDE_LIST = Holders.config.BANNER_AIP_EXCLUDE_LIST
        matchAll().excludes(controllerName:"$BANNER_AIP_EXCLUDE_LIST")
    }

    boolean before() {     String path = getServletPath( request )
        if (!path) {
            return true // No Path set then no need to redirect
        }
        if (!springSecurityService.isLoggedIn()) {
            return true // No Action If not logged in
        }

        def urlList = []
        if (!servletContext['urlList']) {
            log.debug( "inside setting urlList in app context" )
            urlList.addAll( ProcessUrls.fetchUrls() )
            servletContext['urlList'] = urlList
        }
        urlList = servletContext['urlList']
        if (!urlList) {
            return true // No Action it no process URL maintained in System
        }

        // get urls from tables. Check and cache
        // only want to look at type 'document'? not stylesheet, script, gif, font, ? ?
        // at this point he getRequestURI returns the forwared dispatcher URL */aip/myplace.dispatch
        boolean noUrlToCheck = urlList.find {it.contains( path )} == null
        log.debug( "No Url to check $noUrlToCheck" )
        if (noUrlToCheck) {
            return true // No Action it requested path is not among URL
        }
        String aipEnabledStatus = session.getAttribute( "aipEnabledStatus" )
        if (!aipEnabledStatus) {
            // aipEnabledStatus can have either ENABLED or DISABLED but it should not be null
            aipEnabledStatus = IntegrationConfiguration.fetchByProcessCodeAndSettingName( 'GENERAL_SSB', 'ENABLE.ACTION.ITEMS' ).value == YES ? ENABLED : DISABLED
            session.setAttribute( "aipEnabledStatus", aipEnabledStatus )
        }
        log.debug( "AIP $aipEnabledStatus" )
        if (aipEnabledStatus == DISABLED) {
            return true // NO ACTION If AIP Not enabled
        }

        def persona = session.getAttribute( 'selectedRole' )?.persona?.code
        log.debug( "Persona $persona" )
        def isBlockingUrl = isBlockingUrl( springSecurityService.getAuthentication().user.pidm, path, persona )
        log.debug( "isBlockingUrl $isBlockingUrl" )
        if (!isBlockingUrl) {
            return true // No Action if no process process
        }
        String mepCode = session.getAttribute( 'mep' )
        log.debug( "mepCode $mepCode" )
        def configUrl = Holders.config.GENERALLOCATION + '/ssb/aip/informedList/informedList'
        def encodePath = getEncodeUrl(request)
        configUrl = "$configUrl?reUrl=$encodePath"
        if (mepCode) {
            configUrl = "$configUrl&mepCode=$mepCode"
        }
        log.debug( "URL to redirect $configUrl" )
        redirect( url: configUrl )
    }

    boolean after() { true }

    void afterView() {
        // no-op
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

        List<UserBlockedProcessReadOnly> blockedActionItemList = UserBlockedProcessReadOnly.fetchBlockedProcesses( pidm )

        if (!blockedActionItemList) {
            return false
        }
        log.debug( "Inside isBlockingUrl blockedActionItemList size ${blockedActionItemList.size()}" )
        def isBlockProcess = blockedActionItemList.find {it.processGlobalBlockInd == YES}?.processGlobalBlockInd == YES
        log.debug( "isBlockProcess $isBlockProcess" )
        if (isBlockProcess) {
            return true // Blocked process if any assigned action item has been associated the global block process
        }
        blockedActionItemList.retainAll() {
            it.processGlobalBlockInd == NO
        }
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

    /**
     *
     * @param request
     * @return
     */
    private static def getEncodeUrl(request){
        GrailsUrlPathHelper urlPathHelper = new GrailsUrlPathHelper()
        String path = urlPathHelper.getOriginatingRequestUri( request )
        if (request?.getQueryString()) {
            path = path + QUESTION_MARK + request?.getQueryString()
        }
        String url = request.getRequestURL()
        if(url){
            url = url.substring(0,url.indexOf(request.getContextPath())) + path
            url = URLEncoder.encode(url,'utf-8')
        }
        return url

    }
}
