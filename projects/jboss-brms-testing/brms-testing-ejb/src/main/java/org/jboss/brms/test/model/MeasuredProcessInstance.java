package org.jboss.brms.test.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import org.apache.commons.lang.ObjectUtils;
import org.jboss.brms.test.service.MetricsService;

/**
 * Metrics for a process instance as used in a test run.
 */
@Entity
public class MeasuredProcessInstance extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** Data to uniquely identify a process instance. */
    @Embedded
    @Valid
    private ProcessInstanceIdentifier identifier;

    /** The moment the instance was started. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startingTime;

    /** The moment the instance was concluded. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endingTime;

    private Integer numberOfNodesVisited;

    /** The rules used for this instance. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Valid
    private Set<MeasuredRule> rules;

    /** The Human Tasks used for this instance. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Valid
    private Set<MeasuredHumanTask> humanTasks;

    /** Default constructor, required by JPA. */
    protected MeasuredProcessInstance() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param identifier
     *            Data to uniquely identify a process instance.
     */
    public MeasuredProcessInstance(final ProcessInstanceIdentifier identifier) {
        this.identifier = new ProcessInstanceIdentifier(identifier.getMetricsId(), identifier.getPackageName(), identifier.getProcessId(),
                identifier.getKsessionId(), identifier.getProcessInstanceId());
        numberOfNodesVisited = Integer.valueOf(0);
    }

    public ProcessInstanceIdentifier getIdentifier() {
        return identifier;
    }

    void setIdentifier(final ProcessInstanceIdentifier identifier) {
        this.identifier = identifier;
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
        final StringBuilder sb = new StringBuilder().append("\n\n\t\t\tMeasuredProcessInstance:").append(identifier.print())
                .append("\n\t\t\t* Number of nodes visited = ").append(numberOfNodesVisited);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        if (endingTime != null) {
            sb.append("\n\t\t\t* Duration: ").append(endingTime.getTime() - startingTime.getTime()).append(" ms (starting time = ")
                    .append(timeFormat.format(startingTime)).append(", ending time = ").append(timeFormat.format(endingTime)).append(")");
        } else if (startingTime != null) {
            sb.append("\n\t\t\t* Instance started at ").append(timeFormat.format(startingTime)).append(" but did not end yet.");
        } else {
            sb.append("\n\t\t\t* Instance not started yet.");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return (PRIME * HASH_SEED) + ObjectUtils.hashCode(identifier);
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
        return ObjectUtils.equals(identifier, other.getIdentifier());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcessInstance [").append(identifier).append(", startingTime=").append(startingTime)
                .append(", endingTime=").append(endingTime).append("]").toString();
    }
}
