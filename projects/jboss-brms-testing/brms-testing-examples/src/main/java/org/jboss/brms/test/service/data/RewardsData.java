package org.jboss.brms.test.service.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;

public class RewardsData extends AbstractDataProvider {
    private static final String PROCESS_ID = "org.jbpm.approval.rewards.extended";

    /** {@inheritDoc} */
    @Override
    public String getProcessId() {
        return PROCESS_ID;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsHumanTasks() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void registerWorkItemHandlers(final WorkItemManager workItemManager) {
        workItemManager.registerWorkItemHandler("Log", new SystemOutWorkItemHandler());
        workItemManager.registerWorkItemHandler("Email", new SystemOutWorkItemHandler());
    }

    /** {@inheritDoc} */
    @Override
    public void performTasks(final TaskClient taskClient) {
        // Get task.
        final BlockingTaskSummaryResponseHandler taskSummaryHandler = new BlockingTaskSummaryResponseHandler();
        taskClient.getTasksAssignedAsPotentialOwner("peter", "en-UK", taskSummaryHandler);
        final TaskSummary task = taskSummaryHandler.getResults().get(0);

        // Complete task.
        final ContentData content = new ContentData();
        content.setAccessType(AccessType.Inline);
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("Explanation", "Great work");
        taskParams.put("Outcome", "Approved");
        content.setContent(SerializationUtils.serialize((Serializable) taskParams));
        final BlockingTaskOperationResponseHandler taskOperationHandler = new BlockingTaskOperationResponseHandler();
        taskClient.complete(task.getId(), "peter", content, taskOperationHandler);
        taskOperationHandler.waitTillDone(1000L);
    }
}
