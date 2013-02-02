package org.jboss.brms.test.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;

/**
 * Metrics for a process instance as used in a test run.
 */
@Entity
public class MeasuredProcessInstance extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The unique ID of this process instance. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private Long processInstanceId;

    /** The moment the instance was started. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startingTime;

    /** The moment the instance was concluded. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endingTime;

    private Integer numberOfNodesVisited;

    /** Default constructor, required by JPA. */
    protected MeasuredProcessInstance() {
    }

    /**
     * Parameterized constructor.
     * 
     * @param id
     *            The unique ID of the process instance for which the metrics are kept in this object.
     */
    public MeasuredProcessInstance(final Long id) {
        processInstanceId = id;
        numberOfNodesVisited = Integer.valueOf(0);
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    void setProcessInstanceId(final Long processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public Long getTimeToComplete() {
        Long timeToComplete = null;
        if ((startingTime != null) && (endingTime != null)) {
            timeToComplete = endingTime.getTime() - startingTime.getTime();
        }
        return timeToComplete;
    }

    public Integer getNumberOfNodesVisited() {
        return numberOfNodesVisited;
    }

    void setNumberOfNodesVisited(final Integer numberOfNodesVisited) {
        this.numberOfNodesVisited = numberOfNodesVisited;
    }

    public void increaseNumberOfNodesVisited() {
        ++numberOfNodesVisited;
    }

    public String print() {
        final StringBuilder sb = new StringBuilder().append("\nMeasuredProcessInstance:\n * Process instance ID: ").append(processInstanceId);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        if (endingTime != null) {
            sb.append("\n\n * Duration: ").append(endingTime.getTime() - startingTime.getTime()).append(" ms (starting time = ")
                    .append(timeFormat.format(startingTime)).append(", ending time = ").append(timeFormat.format(endingTime)).append(")");
        } else if (startingTime != null) {
            sb.append("\n\n * Instance started at ").append(timeFormat.format(startingTime)).append(" but did not end yet.");
        } else {
            sb.append("\n\n * Instance not started yet.");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = HASH_SEED;
        result = (PRIME * result) + ObjectUtils.hashCode(processInstanceId);
        result = (PRIME * result) + ObjectUtils.hashCode(startingTime);
        result = (PRIME * result) + ObjectUtils.hashCode(endingTime);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MeasuredProcessInstance)) {
            return false;
        }

        final MeasuredProcessInstance other = (MeasuredProcessInstance) obj;
        return ObjectUtils.equals(processInstanceId, other.getProcessInstanceId()) && ObjectUtils.equals(startingTime, other.getStartingTime())
                && ObjectUtils.equals(endingTime, other.getEndingTime());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcessInstance [processInstanceId=").append(processInstanceId).append(", startingTime=")
                .append(startingTime).append(", endingTime=").append(endingTime).append("]").toString();
    }
}
