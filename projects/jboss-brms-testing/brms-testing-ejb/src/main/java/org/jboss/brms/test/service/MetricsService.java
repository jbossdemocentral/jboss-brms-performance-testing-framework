package org.jboss.brms.test.service;

import java.lang.management.ManagementFactory;
import java.util.Date;

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
     * @param metricsId
     *            The ID of the {@link Metrics} this rule belongs to.
     * @param ruleFlowGroup
     *            The group ID of the required rule.
     * @param nodeId
     *            The unique ID of the Rule node this rule was called for.
     * @return The intended rule, or <code>null</code> if it was not available.
     */
    public MeasuredRule findRule(final Long metricsId, final String ruleFlowGroup, final String nodeId) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredRule> cq = cb.createQuery(MeasuredRule.class);
        final Root<MeasuredRule> ruleRoot = cq.from(MeasuredRule.class);
        cq.where(cb.equal(ruleRoot.get(MeasuredRule_.metricsId), metricsId), cb.equal(ruleRoot.get(MeasuredRule_.ruleFlowGroup), ruleFlowGroup),
                cb.equal(ruleRoot.get(MeasuredRule_.nodeId), nodeId));
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
     * @param metricsId
     *            The ID of the {@link Metrics} this task belongs to.
     * @param taskName
     *            The name of the required task.
     * @param nodeId
     *            The unique ID of the Human Task node this task was called for.
     * @return The intended task, or <code>null</code> if it was not available.
     */
    public MeasuredHumanTask findHumanTask(final Long metricsId, final String taskName, final String nodeId) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredHumanTask> cq = cb.createQuery(MeasuredHumanTask.class);
        final Root<MeasuredHumanTask> taskRoot = cq.from(MeasuredHumanTask.class);
        cq.where(cb.equal(taskRoot.get(MeasuredHumanTask_.metricsId), metricsId), cb.equal(taskRoot.get(MeasuredHumanTask_.taskName), taskName),
                cb.equal(taskRoot.get(MeasuredHumanTask_.nodeId), nodeId));
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
        final MeasuredRule rule = em.merge(new MeasuredRule(identifier.getMetricsId(), ruleFlowGroup, nodeId));

        // Add it to the corresponding process instance.
        final MeasuredProcessInstance processInstance = findProcessInstance(identifier);
        if (processInstance == null) {
            throw new IllegalStateException("Rule started for process instance [" + identifier + "], which cannot be found.");
        }
        processInstance.addRule(rule);

        // Set the time.
        rule.setStartingTime(new Date());
    }

    public void setRuleEndTime(final Long metricsId, final String ruleFlowGroup, final String nodeId) {
        findRule(metricsId, ruleFlowGroup, nodeId).setEndingTime(new Date());
    }

    public void setHumanTaskStartTime(final ProcessInstanceIdentifier identifier, final String taskName, final String groupId, final String nodeId) {
        // Create the task (one for each call).
        final MeasuredHumanTask task = em.merge(new MeasuredHumanTask(identifier.getMetricsId(), taskName, groupId, nodeId));

        // Add it to the corresponding process instance.
        final MeasuredProcessInstance processInstance = findProcessInstance(identifier);
        if (processInstance == null) {
            throw new IllegalStateException("Human Task started for process instance [" + identifier + "], which cannot be found.");
        }
        processInstance.addHumanTask(task);

        // Set the time.
        task.setStartingTime(new Date());
    }

    public void setHumanTaskEndTime(final Long metricsId, final String taskName, final String nodeId) {
        findHumanTask(metricsId, taskName, nodeId).setEndingTime(new Date());
    }
}
