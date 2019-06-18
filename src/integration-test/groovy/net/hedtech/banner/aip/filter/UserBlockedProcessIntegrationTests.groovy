/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.aip.filter

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import net.hedtech.banner.aip.gatekeeping.UserBlockedProcessReadOnly
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * GateKeepingInterceptorIntegrationTests.
 */
@Integration
@Rollback
class UserBlockedProcessIntegrationTests extends BaseIntegrationTestCase {

    def springSecurityService


    @Before
    void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
    }


    @After
    void tearDown() {
        logout()
        super.tearDown()
    }


    @Test
    void testFilterNoRedirect() {
        loginSSB( 'CSRSTU002', '111111' )
        def result = UserBlockedProcessReadOnly.fetchBlockedProcesses( springSecurityService.getAuthentication().user.pidm )
        assert result.size() > 0
    }

}
