/*********************************************************************************
 Copyright 2010-2020 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

package banner.aip.gate.keeper

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
