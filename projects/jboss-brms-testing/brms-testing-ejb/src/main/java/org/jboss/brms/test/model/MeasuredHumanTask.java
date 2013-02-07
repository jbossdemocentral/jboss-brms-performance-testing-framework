package org.jboss.brms.test.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Metrics for a Human Task as used in a test run.
 */
@Entity
public class MeasuredHumanTask extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The identifier of the task. */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String taskName;

    /** The identifier of the task group. */
    @Column(updatable = false)
    private String groupId;

    /** The unique ID of the Human Task node this call was made for. */
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
    protected MeasuredHumanTask() {
    }

    /**
     * Parameterized constructor.
     * 
     * @param taskName
     *            The identifier of the task.
     * @param groupId
     *            The identifier of the task group.
     * @param nodeId
     *            The unique ID of the Human Task node this call was made for.
     */
    public MeasuredHumanTask(final String taskName, final String groupId, final String nodeId) {
        setTaskName(taskName);
        this.groupId = groupId;
        this.nodeId = nodeId;
    }

    public String getTaskName() {
        return taskName;
    }

    void setTaskName(final String taskName) {
        this.taskName = taskName;
    }

    public String getGroupId() {
        return groupId;
    }

    void setGroupId(final String groupId) {
        this.groupId = groupId;
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
        final StringBuilder sb = new StringBuilder().append("\n\n    MeasuredHumanTask:\n     * Task Name: ").append(taskName);
        if (groupId != null) {
            sb.append("\n     * Group ID: ").append(groupId);
        }
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        if (endingTime != null) {
            sb.append("\n     * Duration: ").append(endingTime.getTime() - startingTime.getTime()).append(" ms (starting time = ")
                    .append(timeFormat.format(startingTime)).append(", ending time = ").append(timeFormat.format(endingTime)).append(")");
        } else if (startingTime != null) {
            sb.append("\n     * Task executed at ").append(timeFormat.format(startingTime)).append(" but did not end yet.");
        } else {
            sb.append("\n     * Rule not executed yet.");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = (PRIME * result) + ObjectUtils.hashCode(taskName);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MeasuredHumanTask)) {
            return false;
        }

        final MeasuredHumanTask other = (MeasuredHumanTask) obj;
        return super.equals(other) && ObjectUtils.equals(taskName, other.getTaskName());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredHumanTask [taskName=").append(taskName).append(", groupId=").append(groupId).append("]").toString();
    }
}
