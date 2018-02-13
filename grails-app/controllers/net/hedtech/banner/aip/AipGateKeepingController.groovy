/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.aip

import grails.converters.JSON

/**
 * AIP Controller class to have all API endpoints
 */
class AipGateKeepingController {
    def index() {
        def finalUrl = 'http://localhost:8081/BannerGeneralSsb/ssb/aip/informedList#/informedList'
        //response.sendRedirect(finalUrl )
        //String xz="abc"
        render( model: [url: finalUrl], view: "gatekeeper" )
    }
}
