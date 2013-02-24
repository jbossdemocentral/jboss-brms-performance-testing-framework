package org.jboss.brms.test.service.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.Status;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RewardsData extends AbstractDataProvider {
    private static final String PROCESS_ID = "org.jbpm.approval.rewards.extended";

    private static final Logger LOGGER = LoggerFactory.getLogger(RewardsData.class);

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
        taskClient.getTasksAssignedAsPotentialOwner("Peter Parker", Arrays.asList("HR"), "en-UK", taskSummaryHandler);
        final List<TaskSummary> taskList = taskSummaryHandler.getResults();
        if (taskList.isEmpty()) {
            LOGGER.error("Unable to retrieve task to be performed: aborting.");
            return;
        }
        TaskSummary task = null;
        for (final TaskSummary summary : taskList) {
            if (Status.Ready.equals(summary.getStatus())) {
                task = summary;
                break;
            }
        }
        if (task == null) {
            LOGGER.error("No 'READY' tasks to be performed: aborting.");
            return;
        }

        // Claim task.
        BlockingTaskOperationResponseHandler taskOperationHandler = new BlockingTaskOperationResponseHandler();
        taskClient.claim(task.getId(), "Peter Parker", Arrays.asList("HR"), taskOperationHandler);
        taskOperationHandler.waitTillDone(1000L);

        // Start task.
        taskOperationHandler = new BlockingTaskOperationResponseHandler();
        taskClient.start(task.getId(), "Peter Parker", taskOperationHandler);
        taskOperationHandler.waitTillDone(1000L);

        // Complete task.
        final ContentData content = new ContentData();
        content.setAccessType(AccessType.Inline);
        final Map<String, Object> taskParams = new HashMap<String, Object>();
        taskParams.put("Explanation", "Great work");
        taskParams.put("Outcome", "Approved");
        content.setContent(SerializationUtils.serialize((Serializable) taskParams));
        taskOperationHandler = new BlockingTaskOperationResponseHandler();
        taskClient.complete(task.getId(), "Peter Parker", content, taskOperationHandler);
        taskOperationHandler.waitTillDone(1000L);
    }
}
