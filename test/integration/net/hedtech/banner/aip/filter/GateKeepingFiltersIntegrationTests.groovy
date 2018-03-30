/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.aip.filter

import grails.util.GrailsWebUtil
import grails.util.Holders
import net.hedtech.banner.general.overall.IntegrationConfiguration
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

/**
 * GateKeepingFiltersIntegrationTests.
 */
class GateKeepingFiltersIntegrationTests extends BaseIntegrationTestCase {

    static final String UNBLOCKEDURI = '/somethingrandom'

    static final String BLOCKREGISTERFORCOURSES = '/ssb/term/termSelection?mode=preReg'

    static final String TESTGENERALURL = 'https://someplace.mytestplace.edu'

    static final String SHIPPEDURI = "/ssb/aip/informedList/informedList"

    def filterInterceptor

    def grailsApplication

    def grailsWebRequest


    @Before
    void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        Holders.config.BANNER_AIP_BLOCK_PROCESS_PERSONA = ['EVERYONE', 'STUDENT', 'REGISTRAR', 'FACULTYINSTRUCTOR', 'FACULTYADVISOR', 'FACULTYBOTH']
        Holders.config.BANNER_AIP_EXCLUDE_LIST = ['selfServiceMenu', 'login', 'error', 'dateConverter']
        Holders.config.GENERALLOCATION = TESTGENERALURL
    }


    @After
    void tearDown() {
        logout()
        super.tearDown()
        Holders.config.BANNER_AIP_BLOCK_PROCESS_PERSONA = []
        Holders.config.BANNER_AIP_EXCLUDE_LIST = []
        Holders.config.GENERALLOCATION = null
    }

    def formControllerMap = [
            'banner'                                 : ['SCACRSE'],
            'mainpage'                               : ['SELFSERVICE'],
            'menu'                                   : ['SELFSERVICE'],
            'selfservicemenu'                        : ['SELFSERVICE-EMPLOYEE', 'SELFSERVICE'],
            'survey'                                 : ['SELFSERVICE'],
            'useragreement'                          : ['SELFSERVICE'],
            'securityqa'                             : ['SELFSERVICE'],
            'general'                                : ['SELFSERVICE'],
            'updateaccount'                          : ['SELFSERVICE'],
            'accountlisting'                         : ['SELFSERVICE'],
            'directdepositconfiguration'             : ['SELFSERVICE'],
            '/ssb/registration/**'                   : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/ssb/registration/registerPostSignIn/**': ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M'],
            '/ssb/classRegistration/**'              : ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M'],
            '/ssb/term/**'                           : ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M'],
            '/**'                                    : ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-REGISTRAR_BAN_DEFAULT_M',
                                                        'ROLE_SELFSERVICE-FACULTY_BAN_DEFAULT_M'],
            'aip'                                    : ['SELFSERVICE'],
            'aipgroup'                               : ['SELFSERVICE'],
            'aipadmin'                               : ['SELFSERVICE']
    ]


    @Test
    void testFilterNoRedirect() {
        setGORICCR( 'Y' )
        def person = PersonUtility.getPerson( "CSRSTU013" ) // user has no blocking AIs
        assertNotNull person
        loginSSB( person.bannerId, '111111' )

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.characterEncoding = 'UTF-8'
        request.setRequestURI( UNBLOCKEDURI )

        def result = doRequest( request )
        assert result

        assertNull( response.redirectedUrl )
    }


    @Test
    void testFilterRedirect() {
        setGORICCR( 'Y' )
        sessionFactory.getCurrentSession().createSQLQuery( """update gcbbprc set gcbbprc_persona_blkd_allowed='N'""" ).executeUpdate()
        def person = PersonUtility.getPerson( "CSRSTU002" ) // user has blocking AIs
        assertNotNull person
        loginSSB( person.bannerId, '111111' )

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.characterEncoding = 'UTF-8'
        request.setRequestURI( BLOCKREGISTERFORCOURSES ) // will need to fix this when values came from DB

        doRequest( request )

        assertNotNull( response.redirectedUrl )
        CharSequence cs1 = "informedList";
        assertTrue( response.redirectedUrl.contains( cs1 ) )
    }


    @Test
    void testGORICCRBlockingOn() {
        sessionFactory.getCurrentSession().createSQLQuery( """update gcbbprc set gcbbprc_persona_blkd_allowed='N'""" ).executeUpdate()
        setGORICCR( 'Y' )
        def person = PersonUtility.getPerson( "CSRSTU002" ) // user has blocking AIs
        assertNotNull person
        loginSSB( person.bannerId, '111111' )

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.characterEncoding = 'UTF-8'
        request.setRequestURI( BLOCKREGISTERFORCOURSES ) // will need to fix this when values came from DB

        doRequest( request )

        assertNotNull( response.redirectedUrl )
        CharSequence cs1 = "informedList";
        assertTrue( response.redirectedUrl.contains( cs1 ) )

    }


    @Test
    void testGORICCRBlockingOff() {
        setGORICCR( 'N' )
        def person = PersonUtility.getPerson( "CSRSTU002" ) // user has blocking AIs
        assertNotNull person
        loginSSB( person.bannerId, '111111' )

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.characterEncoding = 'UTF-8'
        request.setRequestURI( BLOCKREGISTERFORCOURSES ) // will need to fix this when values came from DB

        def result = doRequest( request )
        assert result

        assertNull( response.redirectedUrl )
    }


    @Test
    void testConfiguredLookupRedirectLocation() {
        setGORICCR( 'Y' )
        sessionFactory.getCurrentSession().createSQLQuery( """update gcbbprc set gcbbprc_persona_blkd_allowed='N'""" ).executeUpdate()
        String configuredBase = Holders.config.GENERALLOCATION


        def person = PersonUtility.getPerson( "CSRSTU002" ) // user has blocking AIs
        assertNotNull person
        loginSSB( person.bannerId, '111111' )

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.characterEncoding = 'UTF-8'
        request.setRequestURI( BLOCKREGISTERFORCOURSES ) // will need to fix this when values came from DB

        doRequest( request )

        assertNotNull( response.redirectedUrl )
        CharSequence cs1 = "informedList"
        assertTrue( response.redirectedUrl.contains( cs1 ) )
        assertEquals( configuredBase + SHIPPEDURI, response.redirectedUrl )
    }


    @Test
    void testChangedLookupRedirectLocation() {
        setGORICCR( 'Y' )
        // change location to something not ellucian
        sessionFactory.getCurrentSession().createSQLQuery( """update gcbbprc set gcbbprc_persona_blkd_allowed='N'""" ).executeUpdate()
        setGeneralAppLocation( TESTGENERALURL )

        def person = PersonUtility.getPerson( "CSRSTU002" ) // user has blocking AIs
        assertNotNull person
        loginSSB( person.bannerId, '111111' )

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.characterEncoding = 'UTF-8'
        request.setRequestURI( BLOCKREGISTERFORCOURSES ) // will need to fix this when values came from DB

        doRequest( request )

        assertNotNull( response.redirectedUrl )
        CharSequence cs1 = "informedList";
        CharSequence cs2 = TESTGENERALURL;
        assertTrue( response.redirectedUrl.contains( cs1 ) )
        assertTrue( response.redirectedUrl.contains( cs2 ) )
    }


    def getResponse() {
        grailsWebRequest.currentResponse
    }


    def doRequest( MockHttpServletRequest mockRequest ) {
        grailsApplication.config.formControllerMap = formControllerMap
        grailsWebRequest = GrailsWebUtil.bindMockWebRequest(
                Holders.getGrailsApplication().mainContext, mockRequest, new MockHttpServletResponse() )

        filterInterceptor.preHandle( grailsWebRequest.request, grailsWebRequest.response, null )
    }


    private void setGeneralAppLocation( String value ) {
        Holders.config.GENERALLOCATION = value
        assertEquals( value, Holders.config.GENERALLOCATION )
    }


    private void setGORICCR( String value ) {
        def nameSQL = """update goriccr set goriccr_value = ? where goriccr_icsn_code = 'ENABLE.ACTION.ITEMS' and goriccr_sqpr_code = 'GENERAL_SSB'"""
        sessionFactory.getCurrentSession().createSQLQuery( nameSQL ).setString( 0, value ).executeUpdate()

        assertEquals( value, IntegrationConfiguration.fetchByProcessCodeAndSettingName( 'GENERAL_SSB', 'ENABLE.ACTION.ITEMS' ).value )
    }
}
