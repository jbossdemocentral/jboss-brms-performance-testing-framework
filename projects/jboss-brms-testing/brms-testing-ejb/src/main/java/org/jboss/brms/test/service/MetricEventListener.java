package org.jboss.brms.test.service;

import java.util.Date;

import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEvent;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.jboss.brms.test.model.MeasuredProcess;
import org.jboss.brms.test.model.MeasuredProcessInstance;
import org.jboss.brms.test.model.Metrics;

public class MetricEventListener extends DefaultProcessEventListener {
    private final Metrics metrics;

    public MetricEventListener() {
        metrics = new Metrics();

        // Hard-code for now:
        metrics.setNumberOfMachines(Integer.valueOf(1));
        metrics.setLoadBalancingUsed(Boolean.FALSE);
        metrics.setProcessesStartedInParallel(Boolean.FALSE);
        metrics.setProcessesRunInIndividualKnowledgeSession(Boolean.FALSE);
    }

    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public void beforeProcessStarted(final ProcessStartedEvent event) {
        final Date timeOfEvent = new Date();

        // Set the event in the corresponding process instance..
        final MeasuredProcessInstance mpi = findProcessInstance(event);
        mpi.setStartingTime(timeOfEvent);
    }

    @Override
    public void beforeProcessCompleted(final ProcessCompletedEvent event) {
        final Date timeOfEvent = new Date();

        // Set the event in the corresponding process instance..
        final MeasuredProcessInstance mpi = findProcessInstance(event);
        mpi.setEndingTime(timeOfEvent);
    }

    @Override
    public void beforeNodeLeft(final ProcessNodeLeftEvent event) {
        // Set the event in the corresponding process instance..
        final MeasuredProcessInstance mpi = findProcessInstance(event);
        mpi.increaseNumberOfNodesVisited();
    }

    private MeasuredProcessInstance findProcessInstance(final ProcessEvent event) {
        MeasuredProcess process = findProcess(event.getProcessInstance().getProcessId());
        if (process != null) {
            MeasuredProcessInstance instance = findProcessInstance(process, event.getProcessInstance().getId());
            if (instance != null) {
                // The instance already exists.
                return instance;
            } else {
                // No such instance, but the process already exists.
                instance = new MeasuredProcessInstance(event.getProcessInstance().getId());
                process.addInstance(instance);
                return instance;
            }
        } else {
            // No such process or instance.
            process = new MeasuredProcess(event.getProcessInstance().getProcessId());
            metrics.addProcess(process);
            final MeasuredProcessInstance instance = new MeasuredProcessInstance(event.getProcessInstance().getId());
            process.addInstance(instance);
            return instance;
        }
    }

    private MeasuredProcess findProcess(final String procId) {
        MeasuredProcess process = null;
        for (final MeasuredProcess mp : metrics.getProcesses()) {
            if (mp.getProcessId().equals(procId)) {
                process = mp;
                break;
            }
        }
        return process;
    }

    private MeasuredProcessInstance findProcessInstance(final MeasuredProcess process, final long procInstId) {
        MeasuredProcessInstance instance = null;
        for (final MeasuredProcessInstance mpi : process.getInstances()) {
            if (mpi.getProcessInstanceId().longValue() == procInstId) {
                instance = mpi;
                break;
            }
        }
        return instance;
    }
}
