package org.jboss.brms.test.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.brms.test.service.MetricsService;

/**
 * Metrics for a rule (flow group) as used in a test run.
 */
@Entity
public class MeasuredRule extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** Data to uniquely identify the process instance the rule instance belongs to. */
    @Embedded
    @Valid
    private ProcessInstanceIdentifier identifier;

    /** The identifier of the rule flow group. */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String ruleFlowGroup;

    /** The unique ID of the Rule node this call was made for. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private String nodeId;

    /** The moment the instance was started. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startingTime;

    /** The moment the instance was concluded. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endingTime;

    /** Default constructor, required by JPA. */
    protected MeasuredRule() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param identifier
     *            Data to uniquely identify a process instance.
     * @param ruleFlowGroup
     *            The identifier of the rule flow group.
     * @param nodeId
     *            The unique ID of the Rule node this call was made for.
     */
    public MeasuredRule(final ProcessInstanceIdentifier identifier, final String ruleFlowGroup, final String nodeId) {
        this.identifier = identifier;
        this.ruleFlowGroup = ruleFlowGroup;
        this.nodeId = nodeId;
    }

    public ProcessInstanceIdentifier getIdentifier() {
        return identifier;
    }

    void setIdentifier(final ProcessInstanceIdentifier identifier) {
        this.identifier = identifier;
    }

    public String getRuleFlowGroup() {
        return ruleFlowGroup;
    }

    void setRuleFlowGroup(final String ruleFlowGroup) {
        this.ruleFlowGroup = ruleFlowGroup;
    }

    public String getNodeId() {
        return nodeId;
    }

    void setNodeId(final String nodeId) {
        this.nodeId = nodeId;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(final Date startingTime) {
        this.startingTime = startingTime;
    }

    public Date getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(final Date endingTime) {
        this.endingTime = endingTime;
    }

    public String print() {
        final StringBuilder sb = new StringBuilder().append("\n\n\t\t\t\tMeasuredRule:\n\t\t\t\t* Rule flow group: ").append(ruleFlowGroup);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        if (endingTime != null) {
            sb.append("\n\t\t\t\t* Duration: ").append(endingTime.getTime() - startingTime.getTime()).append(" ms (starting time = ")
                    .append(timeFormat.format(startingTime)).append(", ending time = ").append(timeFormat.format(endingTime)).append(")");
        } else if (startingTime != null) {
            sb.append("\n\t\t\t\t* Rule activated at ").append(timeFormat.format(startingTime)).append(" but did not end yet.");
        } else {
            sb.append("\n\t\t\t\t* Rule not activated yet.");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = HASH_SEED;
        result = (PRIME * result) + ObjectUtils.hashCode(identifier);
        result = (PRIME * result) + ObjectUtils.hashCode(ruleFlowGroup);
        result = (PRIME * result) + ObjectUtils.hashCode(nodeId);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MeasuredRule)) {
            return false;
        }

        final MeasuredRule other = (MeasuredRule) obj;
        return ObjectUtils.equals(identifier, other.getIdentifier()) && ObjectUtils.equals(ruleFlowGroup, other.getRuleFlowGroup())
                && ObjectUtils.equals(nodeId, other.getNodeId());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredRule [ruleFlowGroup=").append(ruleFlowGroup).append("]").toString();
    }
}
