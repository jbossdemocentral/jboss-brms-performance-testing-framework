package org.jboss.brms.test.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.jboss.brms.test.service.MetricsService;

/**
 * Metrics for a process instance as used in a test run.
 */
@Entity
public class MeasuredProcessInstance extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The ID of the {@link Metrics} object this process instance belongs to. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private Long metricsId;

    /** The ID of the process this is an instance of. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private String processId;

    /** The ID of this process instance. */
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

    /** The rules used for this instance. */
    private Set<MeasuredRule> rules;

    /** The Human Tasks used for this instance. */
    private Set<MeasuredHumanTask> humanTasks;

    /** Default constructor, required by JPA. */
    protected MeasuredProcessInstance() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} this package belongs to.
     * @param processId
     *            The ID of the process this is an instance of.
     * @param processInstanceId
     *            The ID of the process instance for which the metrics are kept in this object.
     */
    public MeasuredProcessInstance(final Long metricsId, final String processId, final Long processInstanceId) {
        this.metricsId = metricsId;
        setProcessId(processId);
        this.processInstanceId = processInstanceId;
        numberOfNodesVisited = Integer.valueOf(0);
    }

    public Long getMetricsId() {
        return metricsId;
    }

    void setMetricsId(final Long metricsId) {
        this.metricsId = metricsId;
    }

    public String getProcessId() {
        return processId;
    }

    void setProcessId(final String processId) {
        this.processId = processId;
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
        if (numberOfNodesVisited == null) {
            numberOfNodesVisited = Integer.valueOf(0);
        }
        return numberOfNodesVisited;
    }

    void setNumberOfNodesVisited(final Integer numberOfNodesVisited) {
        this.numberOfNodesVisited = numberOfNodesVisited;
    }

    public void increaseNumberOfNodesVisited() {
        setNumberOfNodesVisited(getNumberOfNodesVisited() + 1);
    }

    public Set<MeasuredRule> getRules() {
        if (rules == null) {
            rules = new HashSet<MeasuredRule>();
        }
        return rules;
    }

    void setRules(final Set<MeasuredRule> rules) {
        this.rules = rules;
    }

    public boolean addRule(final MeasuredRule rule) {
        return getRules().add(rule);
    }

    public Set<MeasuredHumanTask> getHumanTasks() {
        if (humanTasks == null) {
            humanTasks = new HashSet<MeasuredHumanTask>();
        }
        return humanTasks;
    }

    void setHumanTasks(final Set<MeasuredHumanTask> humanTasks) {
        this.humanTasks = humanTasks;
    }

    public boolean addHumanTask(final MeasuredHumanTask humanTask) {
        return getHumanTasks().add(humanTask);
    }

    public String print() {
        final StringBuilder sb = new StringBuilder().append("\n\n   MeasuredProcessInstance:\n    * Process instance ID: ").append(processInstanceId);
        sb.append("\n    * Number of nodes visited = ").append(numberOfNodesVisited);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        if (endingTime != null) {
            sb.append("\n    * Duration: ").append(endingTime.getTime() - startingTime.getTime()).append(" ms (starting time = ")
                    .append(timeFormat.format(startingTime)).append(", ending time = ").append(timeFormat.format(endingTime)).append(")");
        } else if (startingTime != null) {
            sb.append("\n    * Instance started at ").append(timeFormat.format(startingTime)).append(" but did not end yet.");
        } else {
            sb.append("\n    * Instance not started yet.");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = HASH_SEED;
        result = (PRIME * result) + ObjectUtils.hashCode(metricsId);
        result = (PRIME * result) + ObjectUtils.hashCode(processId);
        result = (PRIME * result) + ObjectUtils.hashCode(processInstanceId);
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
        return ObjectUtils.equals(metricsId, other.getMetricsId()) && ObjectUtils.equals(processId, other.getProcessId())
                && ObjectUtils.equals(processInstanceId, other.getProcessInstanceId());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcessInstance [processInstanceId=").append(processInstanceId).append(", startingTime=")
                .append(startingTime).append(", endingTime=").append(endingTime).append("]").toString();
    }
}
