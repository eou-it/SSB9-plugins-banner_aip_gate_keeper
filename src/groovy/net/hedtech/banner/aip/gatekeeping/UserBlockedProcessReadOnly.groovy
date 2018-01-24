/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.aip.gatekeeping

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*


@NamedQueries(value = [
        @NamedQuery(name = "UserBlockedProcessReadOnly.fetchIsBlockedROsByPidmAndId",
                query = """FROM UserBlockedProcessReadOnly a
                   WHERE a.pidm = :myPidm
                   AND a.actionItemId = :aid
                   AND a.isBlocking = true
                   """),
        @NamedQuery(name = "UserBlockedProcessReadOnly.fetchIsBlockedROsByPidm",
                query = """FROM UserBlockedProcessReadOnly a
                           WHERE a.pidm = :myPidm                           
                           AND a.isBlocking = true
                           """),
        @NamedQuery(name = "UserBlockedProcessReadOnly.fetchROsByPidm",
                query = """FROM UserBlockedProcessReadOnly a
                                   WHERE a.pidm = :myPidm                                                              
                                   """)
])
@Entity
@Table(name = "GVQ_GCRABLK")
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
class UserBlockedProcessReadOnly implements Serializable {
    /**
     *  Action Item ID in GCRABLK
     */

    @Id
    @Column(name = "ACTION_ITEM_ID")
    Long actionItemId

    /**
     * PIDM of the user assigned with this action item
     */

    @Column(name = "ACTION_ITEM_PIDM")
    Long pidm

    /**
     * Does this action Item Block the url
     */

    @Type(type = "yes_no")
    @Column(name = "ACTION_ITEM_IS_BLOCKING")
    Boolean isBlocking = false


    @Column(name = "BLOCKED_PROCESS_ID")
    Long blockedProcessId

    @Column(name = "BLOCKED_PROCESS_NAME")
    String blockedProcessName

    @Column(name = "BLOCKED_PROCESS_URL")
    String blockedProcessUrl


    @Column(name = "ACTION_ITEM_START_DATE")
    Date actionItemStartDate


    @Column(name = "ACTION_ITEM_END_DATE")
    Date actionItemEndDate


    @Column(name = "PROCESS_GTVSYSI_CODE")
    String processGtvsysiCode


    @Column(name = "PROCESS_GLOBAL_BLOCK_IND")
    String processGlobalBlockInd


    @Column(name = "PROCESS_PERSONAL_BLKD_ALLOWED")
    String processPersonaBlkdAllowed


    @Column(name = "PROCESS_SYSTEM_REQUIRED_IND")
    String processSystemRequiredInd


    @Version
    @Column(name = "BLOCK_VERSION")
    Long version


    @Column(name = "block_user_id")
    String lastModifiedBy


    @Column(name = "block_activity_date")
    Date lastModified

    //static constraints = {
    //    actionItemId( nullable: false, maxSize: 19 )
    //    isBlocking( nullable: false, maxSize: 1 )
    //    pidm( nullable: false, maxSize: 9 )
    //}

    /**
     *
     * @param pidm
     * @param aid
     * @return
     */
    static def fetchBlockingProcessesROByPidmAndActionItemId( Long pidm, Long aid ) {
        UserBlockedProcessReadOnly.withSession {session ->
            session.getNamedQuery( 'UserBlockedProcessReadOnly.fetchIsBlockedROsByPidmAndId' )
                    .setLong( 'myPidm', pidm ).setLong( 'aid', aid )?.list()[0]
        }
    }

    /**
     *
     * @param pidm
     * @param aid
     * @return
     */
    static def fetchBlockingProcessesROByPidm( Long pidm ) {
        UserBlockedProcessReadOnly.withSession {session ->
            session.getNamedQuery( 'UserBlockedProcessReadOnly.fetchIsBlockedROsByPidm' )
                    .setLong( 'myPidm', pidm )?.list()[0]
        }
    }

    /**
     *
     * @param pidm
     * @param aid
     * @return
     */
    // Probably not needed. using in proof of concept
    static def fetchProcessesROByPidm( Long pidm ) {
        UserBlockedProcessReadOnly.withSession {session ->
            session.getNamedQuery( 'UserBlockedProcessReadOnly.fetchROsByPidm' )
                    .setLong( 'myPidm', pidm )?.list()[0]
        }
    }
}
