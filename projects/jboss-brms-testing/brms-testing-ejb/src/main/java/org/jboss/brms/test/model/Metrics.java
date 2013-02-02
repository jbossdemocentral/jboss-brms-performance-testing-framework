package org.jboss.brms.test.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * Metrics for a single test run.
 */
@Entity
public class Metrics extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** Number of machines used to run the processes. */
    @NotNull
    private Integer numberOfMachines;

    /** If multiple machines were used, was load balancing applied? */
    private Boolean loadBalancingUsed;

    /** Are the process instances started to run at the same time? */
    private Boolean processesStartedInParallel;

    /** Are the process instances each started in their own stateful knowledge session? */
    private Boolean processesRunInIndividualKnowledgeSession;

    /** The processes used in the test run. */
    private Set<MeasuredProcess> processes;

    /** The moment the test run began. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startingTime;

    /** The moment the test run ended. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endingTime;

    /** Default constructor, required by JPA. */
    public Metrics() {
    }

    public Integer getNumberOfMachines() {
        return numberOfMachines;
    }

    public void setNumberOfMachines(final Integer numberOfMachines) {
        this.numberOfMachines = numberOfMachines;
    }

    public Boolean getLoadBalancingUsed() {
        return loadBalancingUsed;
    }

    public void setLoadBalancingUsed(final Boolean loadBalancingUsed) {
        this.loadBalancingUsed = loadBalancingUsed;
    }

    public Boolean getProcessesStartedInParallel() {
        return processesStartedInParallel;
    }

    public void setProcessesStartedInParallel(final Boolean processesStartedInParallel) {
        this.processesStartedInParallel = processesStartedInParallel;
    }

    public Boolean getProcessesRunInIndividualKnowledgeSession() {
        return processesRunInIndividualKnowledgeSession;
    }

    public void setProcessesRunInIndividualKnowledgeSession(final Boolean processesRunInIndividualKnowledgeSession) {
        this.processesRunInIndividualKnowledgeSession = processesRunInIndividualKnowledgeSession;
    }

    public Set<MeasuredProcess> getProcesses() {
        if (processes == null) {
            processes = new HashSet<MeasuredProcess>();
        }
        return processes;
    }

    void setProcesses(final Set<MeasuredProcess> processes) {
        this.processes = processes;
    }

    public boolean addProcess(final MeasuredProcess process) {
        return getProcesses().add(process);
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

    public String print() {
        final StringBuilder sb = new StringBuilder().append("\nMetrics:\n * Number of machines: ").append(numberOfMachines)
                .append("\n * Was load balancing used: ").append(loadBalancingUsed).append("\n * Were processes started in parallel: ")
                .append(processesStartedInParallel).append("\n * Were processes run in an individual knowledge session: ")
                .append(processesRunInIndividualKnowledgeSession);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        if (endingTime != null) {
            sb.append("\n\n * Duration: ").append(endingTime.getTime() - startingTime.getTime()).append(" ms (starting time = ")
                    .append(timeFormat.format(startingTime)).append(", ending time = ").append(timeFormat.format(endingTime)).append(")");
        } else if (startingTime != null) {
            sb.append("\n\n * Test started at ").append(timeFormat.format(startingTime)).append(" but did not end yet.");
        } else {
            sb.append("\n\n * Test not started yet.");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Metrics)) {
            return false;
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Metrics [numberOfMachines=").append(numberOfMachines).append(", loadBalancingUsed=").append(loadBalancingUsed)
                .append(", processesStartedInParallel=").append(processesStartedInParallel).append(", processesRunInIndividualKnowledgeSession=")
                .append(processesRunInIndividualKnowledgeSession).append(", startingTime=").append(startingTime).append(", endingTime=").append(endingTime)
                .append("]").toString();
    }

}
