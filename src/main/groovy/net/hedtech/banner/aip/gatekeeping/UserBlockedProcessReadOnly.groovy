/*********************************************************************************
 Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.aip.gatekeeping

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table
import javax.persistence.Version


@NamedQueries(value = [
        @NamedQuery(name = "UserBlockedProcessReadOnly.fetchBlockedProcesses",
                query = """FROM UserBlockedProcessReadOnly a
                           WHERE a.pidm = :myPidm 
                            AND trunc(CURRENT_DATE) BETWEEN trunc(a.actionItemStartDate) AND trunc(a.actionItemEndDate)
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

    @EmbeddedId
    UserActonItemBlockProcessURLCompositeKey id

    @Column(name = "ACTION_ITEM_ID")
    Long actionItemId

    /**
     * PIDM of the user assigned with this action item
     */

    @Column(name = "ACTION_ITEM_PIDM")
    Long pidm

    /**
     * Persona of process to be blocked
     */

    @Column(name = "PROCESS_PERSONA")
    String persona

    /**
     * Does this action Item Block the url
     */

    @Type(type = "yes_no")
    @Column(name = "ACTION_ITEM_IS_BLOCKING")
    Boolean isBlocking = false

    @Type(type = "yes_no")
    @Column(name = "action_item_status_def_ind")
    Boolean actionItemStatusDefInd

    @Column(name = "action_item_status_rule_name")
    String actionItemStatusRuleName


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


    @Column(name = "PROCESS_PERSONA_BLKD_ALLOWED")
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

    /**
     *
     * @param pidm
     * @return
     */
    static def fetchBlockedProcesses( Long pidm ) {
        UserBlockedProcessReadOnly.withSession {session ->
            session.getNamedQuery( 'UserBlockedProcessReadOnly.fetchBlockedProcesses' )
                    .setLong( 'myPidm', pidm )
                    .list()
        }
    }
}

@Embeddable
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
class UserActonItemBlockProcessURLCompositeKey implements Serializable {
    @Column(name = 'gcrablk_surrogate_id')
    Long gcrablkSurrogateId
    @Column(name = "gcraact_surrogate_id")
    Long gcraactSurrogateId
    @Column(name = "gcvasts_surrogate_id")
    Long gcvastsSurrogateId
    @Column(name = "gcbbprc_surrogate_id")
    Long gcbbprcSurrogateId
    @Column(name = "gcrprcu_surrogate_id")
    Long gcrprcuSurrogateId
}
