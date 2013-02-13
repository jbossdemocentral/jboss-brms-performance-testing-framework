package org.jboss.brms.test.service;

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
import org.jboss.brms.test.model.MeasuredPackage;
import org.jboss.brms.test.model.MeasuredPackage_;
import org.jboss.brms.test.model.MeasuredProcess;
import org.jboss.brms.test.model.MeasuredProcessInstance;
import org.jboss.brms.test.model.MeasuredProcessInstance_;
import org.jboss.brms.test.model.MeasuredProcess_;
import org.jboss.brms.test.model.MeasuredRule;
import org.jboss.brms.test.model.MeasuredRule_;
import org.jboss.brms.test.model.Metrics;
import org.jboss.brms.test.model.PersistentObject_;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class MetricsService {
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
        return em.merge(new Metrics(numberOfMachines, loadBalancingUsed, processesStartedInParallel, processesRunInIndividualKnowledgeSession));
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
     * Find a {@link MeasuredPackage}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} this package belongs to.
     * @param packageName
     *            The name of the required package.
     * @return The intended package, or <code>null</code> if it was not available.
     */
    public MeasuredPackage findPackage(final Long metricsId, final String packageName) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredPackage> cq = cb.createQuery(MeasuredPackage.class);
        final Root<MeasuredPackage> packageRoot = cq.from(MeasuredPackage.class);
        cq.where(cb.equal(packageRoot.get(MeasuredPackage_.metricsId), metricsId), cb.equal(packageRoot.get(MeasuredPackage_.packageName), packageName));
        MeasuredPackage pakkage = null;
        try {
            pakkage = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple packages with name " + packageName + " under one Metrics found, unexpectedly.", nurEx);
        }
        return pakkage;
    }

    /**
     * Find a {@link MeasuredProcess}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} this process belongs to.
     * @param processId
     *            The ID of the required process.
     * @return The intended process, or <code>null</code> if it was not available.
     */
    public MeasuredProcess findProcess(final Long metricsId, final String processId) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredProcess> cq = cb.createQuery(MeasuredProcess.class);
        final Root<MeasuredProcess> processRoot = cq.from(MeasuredProcess.class);
        cq.where(cb.equal(processRoot.get(MeasuredProcess_.metricsId), metricsId), cb.equal(processRoot.get(MeasuredProcess_.processId), processId));
        MeasuredProcess process = null;
        try {
            process = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple processes with ID " + processId + " under one Metrics found, unexpectedly.", nurEx);
        }
        return process;
    }

    /**
     * Find a {@link MeasuredProcessInstance}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} this process instance belongs to.
     * @param processId
     *            The ID of the process from which a specific instance is required.
     * @param processInstanceId
     *            The ID of the required process instance.
     * @return The intended process instance, or <code>null</code> if it was not available.
     */
    public MeasuredProcessInstance findProcessInstance(final Long metricsId, final String processId, final Long processInstanceId) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<MeasuredProcessInstance> cq = cb.createQuery(MeasuredProcessInstance.class);
        final Root<MeasuredProcessInstance> processInstanceRoot = cq.from(MeasuredProcessInstance.class);
        cq.where(cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.metricsId), metricsId),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.processId), processId),
                cb.equal(processInstanceRoot.get(MeasuredProcessInstance_.processInstanceId), processInstanceId));
        MeasuredProcessInstance processInstance = null;
        try {
            processInstance = em.createQuery(cq).getSingleResult();
        } catch (final NoResultException nrEx) {
            // Leave NULL, wasn't available.
        } catch (final NonUniqueResultException nurEx) {
            log.error("Multiple process instances with ID " + processInstanceId + " under one Metrics found, unexpectedly.", nurEx);
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
            log.error("Multiple process instances with ID " + ruleFlowGroup + " under one Metrics found, unexpectedly.", nurEx);
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
            log.error("Multiple process instances with ID " + taskName + " under one Metrics found, unexpectedly.", nurEx);
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
     * @param metricsId
     *            The ID of the {@link Metrics} for the test.
     * @param processId
     *            The ID of the process for which an instance is started.
     * @param processInstanceId
     *            The ID of the instance itself.
     * @param packageName
     */
    public void setProcessInstanceStartTime(final Long metricsId, final String packageName, final String processId, final long processInstanceId) {
        MeasuredProcess process = findProcess(metricsId, processId);
        if (process == null) {
            // First instance of this process, so create it.
            MeasuredPackage pakkage = findPackage(metricsId, packageName);
            if (pakkage == null) {
                // Even first process in the package, so create that too.
                pakkage = em.merge(new MeasuredPackage(metricsId, packageName));
                findMetricsById(metricsId).addPackage(pakkage);
            }
            process = em.merge(new MeasuredProcess(metricsId, processId));
            pakkage.addProcess(process);
        }
        final MeasuredProcessInstance processInstance = em.merge(new MeasuredProcessInstance(metricsId, processId, processInstanceId));
        process.addInstance(processInstance);
        processInstance.setStartingTime(new Date());
        log.info("Instance " + processInstanceId + " started at " + processInstance.getStartingTime());
    }

    public void setProcessInstanceEndTime(final Long metricsId, final String processId, final long processInstanceId) {
        findProcessInstance(metricsId, processId, processInstanceId).setEndingTime(new Date());
    }

    public void setRuleStartTime(final Long metricsId, final String processId, final long processInstanceId, final String ruleFlowGroup, final String nodeId) {
        final MeasuredProcessInstance processInstance = findProcessInstance(metricsId, processId, processInstanceId);
        if (processInstance == null) {
            throw new IllegalStateException("Rule started for process instance [" + processId + ", " + processInstanceId + "] that cannot be found.");
        }
        final MeasuredRule rule = em.merge(new MeasuredRule(metricsId, ruleFlowGroup, nodeId));
        processInstance.addRule(rule);
        rule.setStartingTime(new Date());
    }

    public void setRuleEndTime(final Long metricsId, final String ruleFlowGroup, final String nodeId) {
        findRule(metricsId, ruleFlowGroup, nodeId).setEndingTime(new Date());
    }

    public void setHumanTaskStartTime(final Long metricsId, final String processId, final long processInstanceId, final String taskName, final String groupId,
            final String nodeId) {
        final MeasuredProcessInstance processInstance = findProcessInstance(metricsId, processId, processInstanceId);
        if (processInstance == null) {
            throw new IllegalStateException("Human Task started for process instance [" + processId + ", " + processInstanceId + "] that cannot be found.");
        }
        final MeasuredHumanTask task = em.merge(new MeasuredHumanTask(metricsId, taskName, groupId, nodeId));
        processInstance.addHumanTask(task);
        task.setStartingTime(new Date());
    }

    public void setHumanTaskEndTime(final Long metricsId, final String taskName, final String nodeId) {
        findHumanTask(metricsId, taskName, nodeId).setEndingTime(new Date());
    }
}
