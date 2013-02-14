package org.jboss.brms.test.service.metrics;

import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEvent;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.model.ProcessInstanceIdentifier;
import org.jboss.brms.test.service.MetricsService;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.jbpm.workflow.instance.node.RuleSetNodeInstance;

public class ProcessEventListener extends DefaultProcessEventListener {
    private final MetricsService metricsService;
    private final Long metricsId;

    public ProcessEventListener(final MetricsService metricsService, final Long metricsId) {
        this.metricsService = metricsService;
        this.metricsId = metricsId;
    }

    @Override
    public void beforeProcessStarted(final ProcessStartedEvent event) {
        // Set the event in the corresponding process instance.
        metricsService.setProcessInstanceStartTime(getProcInstFromEvent(event));
    }

    @Override
    public void beforeProcessCompleted(final ProcessCompletedEvent event) {
        // Set the event in the corresponding process instance.
        metricsService.setProcessInstanceEndTime(getProcInstFromEvent(event));
    }

    @Override
    public void afterNodeTriggered(final ProcessNodeTriggeredEvent event) {
        if (event.getNodeInstance() instanceof RuleSetNodeInstance) {
            // Set the event in the corresponding rule.
            final RuleSetNode rsn = (RuleSetNode) ((RuleSetNodeInstance) event.getNodeInstance()).getNode();
            metricsService.setRuleStartTime(getProcInstFromEvent(event), rsn.getRuleFlowGroup(), rsn.getUniqueId());
        } else if (event.getNodeInstance() instanceof HumanTaskNodeInstance) {
            // Set the event in the corresponding Human Task.
            final HumanTaskNode htn = (HumanTaskNode) ((HumanTaskNodeInstance) event.getNodeInstance()).getNode();
            metricsService.setHumanTaskStartTime(getProcInstFromEvent(event), (String) htn.getWork().getParameter("TaskName"), (String) htn.getWork()
                    .getParameter("GroupId"), htn.getUniqueId());
        }
    }

    @Override
    public void beforeNodeLeft(final ProcessNodeLeftEvent event) {
        metricsService.addNodeVisited(getProcInstFromEvent(event));

        if (event.getNodeInstance() instanceof RuleSetNodeInstance) {
            // Set the event in the corresponding rule.
            final RuleSetNode rsn = (RuleSetNode) ((RuleSetNodeInstance) event.getNodeInstance()).getNode();
            metricsService.setRuleEndTime(metricsId, rsn.getRuleFlowGroup(), rsn.getUniqueId());
        } else if (event.getNodeInstance() instanceof HumanTaskNodeInstance) {
            // Set the event in the corresponding Human Task.
            final HumanTaskNode htn = (HumanTaskNode) ((HumanTaskNodeInstance) event.getNodeInstance()).getNode();
            metricsService.setHumanTaskEndTime(metricsId, (String) htn.getWork().getParameter("TaskName"), htn.getUniqueId());
        }
    }

    private ProcessInstanceIdentifier getProcInstFromEvent(final ProcessEvent event) {
        return new ProcessInstanceIdentifier(metricsId, event.getProcessInstance().getProcess().getPackageName(), event.getProcessInstance().getProcess()
                .getId(), ((StatefulKnowledgeSession) ((RuleFlowProcessInstance) event.getProcessInstance()).getKnowledgeRuntime()).getId(), event
                .getProcessInstance().getId());
    }
}
