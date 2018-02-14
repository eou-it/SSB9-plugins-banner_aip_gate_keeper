/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package net.hedtech.banner.aip

import grails.util.Holders

/**
 * AIP Controller class to have all API endpoints
 */
class AipGateKeepingController {
    def index() {
        def finalUrl = Holders.config.GENERALLOCATION + '/ssb/aip/informedList#/informedList'
        render( model: [url: finalUrl], view: "gatekeeper" )
    }
}
