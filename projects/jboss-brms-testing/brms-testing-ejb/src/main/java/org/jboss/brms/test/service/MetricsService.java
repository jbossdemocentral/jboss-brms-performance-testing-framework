package org.jboss.brms.test.service;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.jboss.brms.test.model.MeasuredHumanTask;
import org.jboss.brms.test.model.MeasuredHumanTask_;
import org.jboss.brms.test.model.MeasuredProcess;
import org.jboss.brms.test.model.MeasuredProcessInstance;
import org.jboss.brms.test.model.MeasuredProcessInstance_;
import org.jboss.brms.test.model.MeasuredProcess_;
import org.jboss.brms.test.model.MeasuredRule;
import org.jboss.brms.test.model.MeasuredRule_;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.model.PersistentObject_;
import org.jboss.brms.test.model.ProcessIdentifier;
import org.jboss.brms.test.model.ProcessIdentifier_;
import org.jboss.brms.test.model.ProcessInstanceIdentifier;
import org.jboss.brms.test.model.ProcessInstanceIdentifier_;
import org.jboss.brms.test.service.ProcessStartParameters.ProcessIndicator;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MetricsService {
    private static Object LOCK = new Object();

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    /**
     * Create a new {@link Metrics} object to collect data from a test run in.
     * 
     * @param numberOfMachines
     *            Number of machines used to run the processes.
     * @param loadBalancingUsed
     *            If multiple machines were used, was load balancing applied?
     * @param processesStartedInParallel
     *            Are the process instances started to run at the same time?
     * @param processesRunInIndividualKnowledgeSession
     *            Are the process instances each started in their own stateful knowledge session?
     * @return The freshly created (and persisted) {@link Metrics}.
     */
    public Metrics createMetrics(final Integer numberOfMachines, final Boolean loadBalancingUsed, final Boolean processesStartedInParallel,
            final Boolean processesRunInIndividualKnowledgeSession) {
        final String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
        final String hostName = runtimeName.substring(runtimeName.indexOf("@") + 1);
        final String pid = runtimeName.substring(0, runtimeName.indexOf("@"));
        return em.merge(new Metrics(hostName, pid, numberOfMachines, loadBalancingUsed, processesStartedInParallel, processesRunInIndividualKnowledgeSession));
    }

    /**
     * Create a new {@link MeasuredProcess} object to collect data from runs by its instances in.
     * 
     * @param metricsId
     *            ID of the containing {@link Metrics} object.
     * @param packageName
     *            The name of the package under which the process is kept in Guvnor.
     * @param processId
     *            The ID of the process.
     * @return The freshly created (and persisted) {@link MeasuredProcess}.
     */
    public MeasuredProcess createProcess(final Long metricsId, final String packageName, final String processId) {
        final MeasuredProcess process = em.merge(new MeasuredProcess(new ProcessIdentifier(metricsId, packageName, processId)));
        findMetricsById(metricsId).addProcess(process);
        return process;
    }

    /**
     * Find a {@link Metrics} object by its ID.
     * 
     * @param id
     *            The database ID of the required package.
     * @return The intended {@link Metrics}, or <code>null</code> if it was not available.
     */
    public Metrics findMetricsById(final Long id) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Metrics> cq = cb.createQuery(Metrics.class);
        final Root<Metrics> metricsRoot = cq.from(Metrics.class);
        cq.where(cb.equal(metricsRoot.get(PersistentObject_.id), id));
        Metrics metrics = null;
        try {
            metrics = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple metrics with ID " + id + " found, unexpectedly.", nurEx);
        }
        return metrics;
    }

    /**
     * Find a {@link MeasuredProcess}.
     * 
     * @param identifier
     *            The data to uniquely identify a process.
     * @return The intended process, or <code>null</code> if it was not available.
     */
    public MeasuredProcess findProcess(final ProcessIdentifier identifier) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredProcess> cq = cb.createQuery(MeasuredProcess.class);
        final Root<MeasuredProcess> processRoot = cq.from(MeasuredProcess.class);
        cq.where(cb.equal(processRoot.get(MeasuredProcess_.identifier).get(ProcessIdentifier_.metricsId), identifier.getMetricsId()),
                cb.equal(processRoot.get(MeasuredProcess_.identifier).get(ProcessIdentifier_.packageName), identifier.getPackageName()),
                cb.equal(processRoot.get(MeasuredProcess_.identifier).get(ProcessIdentifier_.processId), identifier.getProcessId()));
        MeasuredProcess process = null;
        try {
            process = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple processes for indentity " + identifier + " found, unexpectedly.", nurEx);
        }
        return process;
    }

    /**
     * Find a {@link MeasuredProcessInstance}.
     * 
     * @param identifier
     *            The data to uniquely identify a process.
     * @return The intended process instance, or <code>null</code> if it was not available.
     */
    public MeasuredProcessInstance findProcessInstance(final ProcessInstanceIdentifier identifier) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredProcessInstance> cq = cb.createQuery(MeasuredProcessInstance.class);
        final Root<MeasuredProcessInstance> processInstanceRoot = cq.from(MeasuredProcessInstance.class);
        cq.where(
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.metricsId), identifier.getMetricsId()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.packageName), identifier.getPackageName()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.processId), identifier.getProcessId()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.ksessionId), identifier.getKsessionId()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.processInstanceId),
                        identifier.getProcessInstanceId()));
        MeasuredProcessInstance processInstance = null;
        try {
            processInstance = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple process instances with identity " + identifier + " found, unexpectedly.", nurEx);
        }
        return processInstance;
    }

    /**
     * Find a {@link MeasuredRule}.
     * 
     * @param identifier
     *            Data to uniquely identify a process instance.
     * @param ruleFlowGroup
     *            The group ID of the required rule.
     * @param nodeId
     *            The unique ID of the Rule node this rule was called for.
     * @return The intended rule, or <code>null</code> if it was not available.
     */
    public MeasuredRule findRule(final ProcessInstanceIdentifier identifier, final String ruleFlowGroup, final String nodeId) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredRule> cq = cb.createQuery(MeasuredRule.class);
        final Root<MeasuredRule> ruleRoot = cq.from(MeasuredRule.class);
        cq.where(cb.equal(ruleRoot.get(MeasuredRule_.identifier).get(ProcessInstanceIdentifier_.metricsId), identifier.getMetricsId()),
                cb.equal(ruleRoot.get(MeasuredRule_.identifier).get(ProcessInstanceIdentifier_.packageName), identifier.getPackageName()),
                cb.equal(ruleRoot.get(MeasuredRule_.identifier).get(ProcessInstanceIdentifier_.processId), identifier.getProcessId()),
                cb.equal(ruleRoot.get(MeasuredRule_.identifier).get(ProcessInstanceIdentifier_.ksessionId), identifier.getKsessionId()),
                cb.equal(ruleRoot.get(MeasuredRule_.identifier).get(ProcessInstanceIdentifier_.processInstanceId), identifier.getProcessInstanceId()),
                cb.equal(ruleRoot.get(MeasuredRule_.ruleFlowGroup), ruleFlowGroup), cb.equal(ruleRoot.get(MeasuredRule_.nodeId), nodeId));
        MeasuredRule rule = null;
        try {
            rule = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple rules with ID " + ruleFlowGroup + " under one Metrics found, unexpectedly.", nurEx);
        }
        return rule;
    }

    /**
     * Find a {@link MeasuredHumanTask}.
     * 
     * @param identifier
     *            Data to uniquely identify a process instance.
     * @param taskName
     *            The name of the required task.
     * @param nodeId
     *            The unique ID of the Human Task node this task was called for.
     * @return The intended task, or <code>null</code> if it was not available.
     */
    public MeasuredHumanTask findHumanTask(final ProcessInstanceIdentifier identifier, final String taskName, final String nodeId) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredHumanTask> cq = cb.createQuery(MeasuredHumanTask.class);
        final Root<MeasuredHumanTask> taskRoot = cq.from(MeasuredHumanTask.class);
        cq.where(cb.equal(taskRoot.get(MeasuredHumanTask_.identifier).get(ProcessInstanceIdentifier_.metricsId), identifier.getMetricsId()),
                cb.equal(taskRoot.get(MeasuredHumanTask_.identifier).get(ProcessInstanceIdentifier_.packageName), identifier.getPackageName()),
                cb.equal(taskRoot.get(MeasuredHumanTask_.identifier).get(ProcessInstanceIdentifier_.processId), identifier.getProcessId()),
                cb.equal(taskRoot.get(MeasuredHumanTask_.identifier).get(ProcessInstanceIdentifier_.ksessionId), identifier.getKsessionId()),
                cb.equal(taskRoot.get(MeasuredHumanTask_.identifier).get(ProcessInstanceIdentifier_.processInstanceId), identifier.getProcessInstanceId()),
                cb.equal(taskRoot.get(MeasuredHumanTask_.taskName), taskName), cb.equal(taskRoot.get(MeasuredHumanTask_.nodeId), nodeId));
        MeasuredHumanTask task = null;
        try {
            task = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple Human Tasks with ID " + taskName + " under one Metrics found, unexpectedly.", nurEx);
        }
        return task;
    }

    /**
     * Set the time at which a test starts.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} for the test.
     */
    public void setTestStartTime(final Long metricsId) {
        findMetricsById(metricsId).setStartingTime(new Date());
    }

    /**
     * Set the time at which a test ends.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} for the test.
     */
    public void setTestEndTime(final Long metricsId) {
        findMetricsById(metricsId).setEndingTime(new Date());
    }

    /**
     * Set the time at which a process instance starts.
     * 
     * @param identifier
     *            Data to uniquely identify the process instance.
     */
    public void setProcessInstanceStartTime(final ProcessInstanceIdentifier identifier) {
        // First event, so create the instance.
        final MeasuredProcessInstance processInstance = em.merge(new MeasuredProcessInstance(identifier));

        // Add it to the originating definition.
        synchronized (LOCK) { // Synchronized because concurrent instances will cause locking exceptions.
            final MeasuredProcess process = findProcess(identifier.toProcessIdentifier());
            if (process == null) {
                throw new IllegalStateException("Instance started for process [" + identifier.toProcessIdentifier() + "], which cannot be found.");
            }
            process.addInstance(processInstance);
            em.flush();
        }

        // Set the time.
        processInstance.setStartingTime(new Date());
    }

    /**
     * Set the time at which a process instance ends.
     * 
     * @param identifier
     *            Data to uniquely identify the process instance.
     */
    public void setProcessInstanceEndTime(final ProcessInstanceIdentifier identifier) {
        findProcessInstance(identifier).setEndingTime(new Date());
    }

    /**
     * Increase the number of nodes visited by a process instance.
     * 
     * @param identifier
     *            Data to uniquely identify the process instance.
     */
    public void addNodeVisited(final ProcessInstanceIdentifier identifier) {
        findProcessInstance(identifier).increaseNumberOfNodesVisited();
    }

    public void setRuleStartTime(final ProcessInstanceIdentifier identifier, final String ruleFlowGroup, final String nodeId) {
        // Create the rule (one for each call).
        final MeasuredRule rule = em.merge(new MeasuredRule(identifier, ruleFlowGroup, nodeId));

        // Add it to the corresponding process instance.
        final MeasuredProcessInstance processInstance = findProcessInstance(identifier);
        if (processInstance == null) {
            throw new IllegalStateException("Rule started for process instance [" + identifier + "], which cannot be found.");
        }
        processInstance.addRule(rule);

        // Set the time.
        rule.setStartingTime(new Date());
    }

    public void setRuleEndTime(final ProcessInstanceIdentifier identifier, final String ruleFlowGroup, final String nodeId) {
        findRule(identifier, ruleFlowGroup, nodeId).setEndingTime(new Date());
    }

    public void setHumanTaskStartTime(final ProcessInstanceIdentifier identifier, final String taskName, final String groupId, final String nodeId) {
        // Create the task (one for each call).
        final MeasuredHumanTask task = em.merge(new MeasuredHumanTask(identifier, taskName, groupId, nodeId));

        // Add it to the corresponding process instance.
        final MeasuredProcessInstance processInstance = findProcessInstance(identifier);
        if (processInstance == null) {
            throw new IllegalStateException("Human Task started for process instance [" + identifier + "], which cannot be found.");
        }
        processInstance.addHumanTask(task);

        // Set the time.
        task.setStartingTime(new Date());
    }

    public void setHumanTaskEndTime(final ProcessInstanceIdentifier identifier, final String taskName, final String nodeId) {
        findHumanTask(identifier, taskName, nodeId).setEndingTime(new Date());
    }

    public int getNumberOfInstancesStarted(final List<ProcessIdentifier> identifiers) {
        int result = 0;
        for (final ProcessIdentifier identifier : identifiers) {
            result += getNumberOfInstancesStarted(identifier);
        }
        return result;
    }

    private int getNumberOfInstancesStarted(final ProcessIdentifier identifier) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final Root<MeasuredProcessInstance> processInstanceRoot = cq.from(MeasuredProcessInstance.class);
        cq.where(
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.metricsId), identifier.getMetricsId()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.packageName), identifier.getPackageName()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.processId), identifier.getProcessId()));
        cq.select(cb.countDistinct(processInstanceRoot));
        return em.createQuery(cq).getSingleResult().intValue();
    }

    public int getNumberOfInstancesEnded(final List<ProcessIdentifier> identifiers) {
        int result = 0;
        for (final ProcessIdentifier identifier : identifiers) {
            result += getNumberOfInstancesEnded(identifier);
        }
        return result;
    }

    private int getNumberOfInstancesEnded(final ProcessIdentifier identifier) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        final Root<MeasuredProcessInstance> processInstanceRoot = cq.from(MeasuredProcessInstance.class);
        cq.where(
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.metricsId), identifier.getMetricsId()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.packageName), identifier.getPackageName()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.processId), identifier.getProcessId()),
                cb.isNotNull(processInstanceRoot.get(MeasuredProcessInstance_.endingTime)));
        cq.select(cb.countDistinct(processInstanceRoot));
        return em.createQuery(cq).getSingleResult().intValue();
    }

    public ProcessRuntimeMetrics getRuntimeMetrics(final List<ProcessIdentifier> identifiers) {
        final ProcessRuntimeMetrics totalMetrics = new ProcessRuntimeMetrics();
        long sumOfRuntimes = 0;
        long sumOfInstances = 0;
        for (final ProcessIdentifier identifier : identifiers) {
            final ProcessRuntimeMetrics procMetrics = getRuntimeMetrics(identifier);
            sumOfRuntimes += procMetrics.getMeanRuntime() * procMetrics.getNumberOfInstances();
            sumOfInstances += procMetrics.getNumberOfInstances();
            if ((totalMetrics.getMinRuntime() == 0) || (totalMetrics.getMinRuntime() > procMetrics.getMinRuntime())) {
                totalMetrics.setMinRuntime(procMetrics.getMinRuntime());
            }
            if ((totalMetrics.getMaxRuntime() == 0) || (totalMetrics.getMaxRuntime() < procMetrics.getMaxRuntime())) {
                totalMetrics.setMaxRuntime(procMetrics.getMaxRuntime());
            }
        }
        totalMetrics.setNumberOfInstances(sumOfInstances);
        if (sumOfInstances != 0) {
            totalMetrics.setMeanRuntime(sumOfRuntimes / sumOfInstances);
        } else {
            totalMetrics.setMeanRuntime(0);
        }
        return totalMetrics;
    }

    private ProcessRuntimeMetrics getRuntimeMetrics(final ProcessIdentifier identifier) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredProcessInstance> cq = cb.createQuery(MeasuredProcessInstance.class);
        final Root<MeasuredProcessInstance> processInstanceRoot = cq.from(MeasuredProcessInstance.class);
        cq.where(
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.metricsId), identifier.getMetricsId()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.packageName), identifier.getPackageName()),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.identifier).get(ProcessInstanceIdentifier_.processId), identifier.getProcessId()));
        final List<MeasuredProcessInstance> instances = em.createQuery(cq).getResultList();

        final ProcessRuntimeMetrics metrics = new ProcessRuntimeMetrics();
        long totalRuntime = 0L;
        for (final MeasuredProcessInstance instance : instances) {
            if (instance.getTimeToComplete() == null) {
                continue;
            }
            final long runtime = instance.getTimeToComplete().longValue();
            totalRuntime += runtime;
            if ((metrics.getMinRuntime() == 0) || (metrics.getMinRuntime() > runtime)) {
                metrics.setMinRuntime(runtime);
            }
            if ((metrics.getMaxRuntime() == 0) || (metrics.getMaxRuntime() < runtime)) {
                metrics.setMaxRuntime(runtime);
            }
        }
        final long totalInstances = instances.size();
        metrics.setNumberOfInstances(totalInstances);
        if (totalInstances != 0) {
            metrics.setMeanRuntime(totalRuntime / totalInstances);
        } else {
            metrics.setMeanRuntime(0);
        }
        return metrics;
    }

    @Asynchronous
    public void waitForTestToEnd(final ProcessStartParameters parameters, final Long metricsId) {
        if (parameters.isStartInParallel()) {
            boolean stillProcessing = true;
            do {
                // Check whether the instances are all done.
                stillProcessing = false;
                for (final ProcessIndicator indicator : parameters.getIndicators()) {
                    if (getNumberOfInstancesEnded(new ProcessIdentifier(metricsId, indicator.getPackageName(), indicator.getProcessId())) < indicator
                            .getNumberOfInstances()) {
                        stillProcessing = true;
                        break;
                    }
                }
                if (stillProcessing) {
                    // Wait for the next check.
                    try {
                        Thread.sleep(250);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (stillProcessing);
        }
        setTestEndTime(metricsId);
        log.info("Test with Metrics ID=" + metricsId + " has ended.");
    }
}
