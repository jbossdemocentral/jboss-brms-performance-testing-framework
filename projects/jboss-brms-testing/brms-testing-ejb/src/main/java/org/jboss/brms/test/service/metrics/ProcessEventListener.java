package org.jboss.brms.test.service.metrics;

import java.util.Date;

import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEvent;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.jboss.brms.test.model.MeasuredPackage;
import org.jboss.brms.test.model.MeasuredProcess;
import org.jboss.brms.test.model.MeasuredProcessInstance;
import org.jboss.brms.test.model.MeasuredRule;
import org.jboss.brms.test.model.Metrics;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.instance.node.RuleSetNodeInstance;

public class ProcessEventListener extends DefaultProcessEventListener {
    private final Metrics metrics;

    public ProcessEventListener(final Metrics metrics) {
        this.metrics = metrics;
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
        // Set the event in the corresponding process instance.
        final MeasuredProcessInstance mpi = findProcessInstance(event);
        mpi.increaseNumberOfNodesVisited();

        // Store the rule invocations under the process instance.
        if (event.getNodeInstance() instanceof RuleSetNodeInstance) {
            final RuleSetNode rsn = (RuleSetNode) ((RuleSetNodeInstance) event.getNodeInstance()).getNode();
            final String rfg = rsn.getRuleFlowGroup();
            MeasuredRule mr = findRule(mpi, rfg);
            if (mr == null) {
                mr = new MeasuredRule(rfg);
                mpi.addRule(mr);
            }
            mr.increaseNumberOfTimesActivated();
        }
    }

    private MeasuredProcessInstance findProcessInstance(final ProcessEvent event) {
        MeasuredPackage pakkage = findPackage(event.getProcessInstance().getProcess().getPackageName());
        if (pakkage != null) {
            MeasuredProcess process = findProcess(pakkage, event.getProcessInstance().getProcessId());
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
                // No such process.
                process = new MeasuredProcess(event.getProcessInstance().getProcessId());
                pakkage.addProcess(process);
                final MeasuredProcessInstance instance = new MeasuredProcessInstance(event.getProcessInstance().getId());
                process.addInstance(instance);
                return instance;
            }
        } else {
            // No such package.
            pakkage = new MeasuredPackage(event.getProcessInstance().getProcess().getPackageName());
            metrics.addPackage(pakkage);
            final MeasuredProcess process = new MeasuredProcess(event.getProcessInstance().getProcessId());
            pakkage.addProcess(process);
            final MeasuredProcessInstance instance = new MeasuredProcessInstance(event.getProcessInstance().getId());
            process.addInstance(instance);
            return instance;
        }
    }

    private MeasuredPackage findPackage(final String packageName) {
        MeasuredPackage pakkage = null;
        for (final MeasuredPackage mpak : metrics.getPackages()) {
            if (mpak.getPackageName().equals(packageName)) {
                pakkage = mpak;
                break;
            }
        }
        return pakkage;
    }

    private MeasuredProcess findProcess(final MeasuredPackage pakkage, final String procId) {
        MeasuredProcess process = null;
        for (final MeasuredProcess mp : pakkage.getProcesses()) {
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

    private MeasuredRule findRule(final MeasuredProcessInstance instance, final String ruleFlowGroup) {
        MeasuredRule rule = null;
        for (final MeasuredRule mr : instance.getRules()) {
            if (mr.getRuleFlowGroup().equals(ruleFlowGroup)) {
                rule = mr;
                break;
            }
        }
        return rule;
    }
}
