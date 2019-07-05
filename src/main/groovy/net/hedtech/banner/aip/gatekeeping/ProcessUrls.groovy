/*********************************************************************************
 Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.aip.gatekeeping

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Version


@NamedQueries(value = [
        @NamedQuery(name = "ProcessUrls.fetchUrls",
                query = """ select distinct processUrl
                            FROM ProcessUrls a
                  """)
])

@Entity
@Table(name = "GCRPRCU")
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
/**
 * Domain class for Process Urls that are subject to be blocked
 */
class ProcessUrls implements Serializable {

    /**
     * Surrogate ID for GCRPRCU
     */

    @Id
    @Column(name = "GCRPRCU_SURROGATE_ID")
    @SequenceGenerator(name = "GCRPRCU_SEQ_GEN", allocationSize = 1, sequenceName = "GCRPRCU_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRPRCU_SEQ_GEN")
    Long id

    /**
     * Blocking process Id
     */
    @Column(name = "GCRPRCU_GCBBPRC_ID")
    Long processId

    /**
     * The process URL
     */
    @Column(name = "GCRPRCU_PROCESS_URL")
    String processUrl

    /**
     * Blocking process url pertains to
     */
    @Column(name = "GCRPRCU_USER_ID")
    String lastModifiedBy

    /**
     * Last activity date for the Blocking process url
     */
    @Column(name = "GCRPRCU_ACTIVITY_DATE")
    Date lastModified

    /**
     * Version of the Blocking process url
     */
    @Version
    @Column(name = "GCRPRCU_VERSION")
    Long version

    /**
     * Data Origin column
     */
    @Column(name = "GCRPRCU_DATA_ORIGIN")
    String dataOrigin

    /**
     * Fetch Blocking Process Urls
     * @return
     */
    static def fetchUrls( ) {
        ProcessUrls.withNewSession{session ->
            session.getNamedQuery( 'ProcessUrls.fetchUrls' )
                    .list()
        }
    }

}
