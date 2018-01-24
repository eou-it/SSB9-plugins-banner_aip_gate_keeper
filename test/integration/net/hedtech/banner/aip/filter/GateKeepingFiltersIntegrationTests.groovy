/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.aip.filter

import grails.util.GrailsWebUtil
import grails.util.Holders
import groovy.sql.Sql
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

    public static final String UNBLOCKEDURI = '/somethingrandom'
    static final String BLOCKREGISTERFORCOURSES = '/ssb/term/termSelection?mode=registration'

    def filterInterceptor

    def grailsApplication

    def grailsWebRequest


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @After
    public void tearDown() {
        logout()
        super.tearDown()
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
        setGORICCR('Y')
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
        setGORICCR('Y')
        def person = PersonUtility.getPerson( "CSRSTU002" ) // user has blocking AIs
        assertNotNull person
        loginSSB( person.bannerId, '111111' )

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.characterEncoding = 'UTF-8'
        request.setRequestURI( BLOCKREGISTERFORCOURSES ) // will need to fix this when values came from DB

        doRequest( request )

        assertNotNull( response.redirectedUrl )
        CharSequence cs1 = "informedList";
        assertTrue ( response.redirectedUrl.contains(cs1) )
    }


    @Test
    void testGORICCRBlockingOn() {
        setGORICCR('Y')
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
        setGORICCR('N')
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


    def getResponse() {
        grailsWebRequest.currentResponse
    }


    def doRequest( MockHttpServletRequest mockRequest ) {
        grailsApplication.config.formControllerMap = formControllerMap
        grailsWebRequest = GrailsWebUtil.bindMockWebRequest(
                Holders.getGrailsApplication().mainContext, mockRequest, new MockHttpServletResponse() )

        filterInterceptor.preHandle( grailsWebRequest.request, grailsWebRequest.response, null )
    }


    private void setGORICCR( String value ) {
        def nameSQL = """update goriccr set goriccr_value = ? where goriccr_icsn_code = 'ENABLE.ACTION.ITEMS' and goriccr_sqpr_code = 'GENERAL_SSB'"""
        sessionFactory.getCurrentSession().createSQLQuery(nameSQL).setString(0, value).executeUpdate()

        assertEquals( value, IntegrationConfiguration.fetchByProcessCodeAndSettingName( 'GENERAL_SSB', 'ENABLE.ACTION.ITEMS' ).value )
    }
}
