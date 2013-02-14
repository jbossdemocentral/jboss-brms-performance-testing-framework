package org.jboss.brms.test.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jboss.brms.test.service.MetricsService;

/**
 * Metrics for a single test run on a single JVM.
 */
@Entity
public class Metrics extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** JVM identification used to run the processes on, host name part. */
    @NotNull
    private String hostName;

    /** JVM identification used to run the processes on, process ID part. */
    @NotNull
    private String pid;

    /** The number of machines used in the test run. */
    private Integer numberOfMachines;

    /** If multiple machines were used, was load balancing applied? */
    private Boolean loadBalancingUsed;

    /** Are the process instances started to run at the same time? */
    private Boolean processesStartedInParallel;

    /** Are the process instances each started in their own stateful knowledge session? */
    private Boolean processesRunInIndividualKnowledgeSession;

    /** The processes used in the test run. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Valid
    private Set<MeasuredProcess> processes;

    /** The moment the test run began. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startingTime;

    /** The moment the test run ended. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endingTime;

    /** Default constructor, required by JPA. */
    protected Metrics() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param hostName
     *            JVM identification used to run the processes on, host name part.
     * @param pid
     *            JVM identification used to run the processes on, process ID part.
     * @param numberOfMachines
     *            The number of machines used in the test run.
     * @param loadBalancingUsed
     *            If multiple machines were used, was load balancing applied?
     * @param processesStartedInParallel
     *            Are the process instances started to run at the same time?
     * @param processesRunInIndividualKnowledgeSession
     *            Are the process instances each started in their own stateful knowledge session?
     */
    public Metrics(final String hostName, final String pid, final Integer numberOfMachines, final Boolean loadBalancingUsed,
            final Boolean processesStartedInParallel, final Boolean processesRunInIndividualKnowledgeSession) {
        this.hostName = hostName;
        this.pid = pid;
        this.numberOfMachines = numberOfMachines;
        this.loadBalancingUsed = loadBalancingUsed;
        this.processesStartedInParallel = processesStartedInParallel;
        this.processesRunInIndividualKnowledgeSession = processesRunInIndividualKnowledgeSession;
    }

    public String getHostName() {
        return hostName;
    }

    void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public String getPid() {
        return pid;
    }

    void setPid(final String pid) {
        this.pid = pid;
    }

    public Integer getNumberOfMachines() {
        return numberOfMachines;
    }

    void setNumberOfMachines(final Integer numberOfMachines) {
        this.numberOfMachines = numberOfMachines;
    }

    public Boolean getLoadBalancingUsed() {
        return loadBalancingUsed;
    }

    void setLoadBalancingUsed(final Boolean loadBalancingUsed) {
        this.loadBalancingUsed = loadBalancingUsed;
    }

    public Boolean getProcessesStartedInParallel() {
        return processesStartedInParallel;
    }

    void setProcessesStartedInParallel(final Boolean processesStartedInParallel) {
        this.processesStartedInParallel = processesStartedInParallel;
    }

    public Boolean getProcessesRunInIndividualKnowledgeSession() {
        return processesRunInIndividualKnowledgeSession;
    }

    void setProcessesRunInIndividualKnowledgeSession(final Boolean processesRunInIndividualKnowledgeSession) {
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
        final StringBuilder sb = new StringBuilder().append("\nMetrics:\n\t* Host name: ").append(hostName).append("\n\t* Process ID: ").append(pid)
                .append("\n\t* Number of machines in test: ").append(numberOfMachines).append("\n\t* Was load balancing used: ").append(loadBalancingUsed)
                .append("\n\t* Were processes started in parallel: ").append(processesStartedInParallel)
                .append("\n\t* Were processes run in an individual knowledge session: ").append(processesRunInIndividualKnowledgeSession);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        if (endingTime != null) {
            sb.append("\n\n\t* Duration: ").append(endingTime.getTime() - startingTime.getTime()).append(" ms (starting time = ")
                    .append(timeFormat.format(startingTime)).append(", ending time = ").append(timeFormat.format(endingTime)).append(")");
        } else if (startingTime != null) {
            sb.append("\n\n\t* Test started at ").append(timeFormat.format(startingTime)).append(" but did not end yet.");
        } else {
            sb.append("\n\n\t* Test not started yet.");
        }
        return sb.toString();
    }

    public String printAll() {
        final StringBuilder sb = new StringBuilder(print());
        for (final MeasuredProcess mp : getProcesses()) {
            sb.append(mp.print());
            for (final MeasuredProcessInstance mpi : mp.getInstances()) {
                sb.append(mpi.print());
                for (final MeasuredRule mr : mpi.getRules()) {
                    sb.append(mr.print());
                }
                for (final MeasuredHumanTask mht : mpi.getHumanTasks()) {
                    sb.append(mht.print());
                }
            }
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
        return new StringBuilder().append("Metrics [hostName=").append(hostName).append(", pid=").append(pid).append(", numberOfMachines=")
                .append(numberOfMachines).append(", loadBalancingUsed=").append(loadBalancingUsed).append(", processesStartedInParallel=")
                .append(processesStartedInParallel).append(", processesRunInIndividualKnowledgeSession=").append(processesRunInIndividualKnowledgeSession)
                .append(", startingTime=").append(startingTime).append(", endingTime=").append(endingTime).append("]").toString();
    }
}
