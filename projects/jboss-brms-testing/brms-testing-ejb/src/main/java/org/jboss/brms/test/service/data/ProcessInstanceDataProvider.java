package org.jboss.brms.test.service.data;

import java.util.Map;

import org.drools.runtime.process.WorkItemManager;
import org.jbpm.task.service.TaskClient;

/**
 * This interface should be implemented by classes that provide any data needed to run a specific process/scenario.
 */
public interface ProcessInstanceDataProvider {
    /**
     * @return The process ID, as defined in the BPMN2 definition.
     */
    String getProcessId();

    /**
     * @return Whether the process (or more specifically, the scenario that is to be run) contains <i>any</i> Human Tasks.
     */
    boolean containsHumanTasks();

    /**
     * Register the handlers for the work items as included in the process definition.
     * <p>
     * A handler for Human Tasks need not be included here, as it is included by default if the provider indicates the process has any.
     * 
     * @param workItemManager
     *            The {@link WorkItemManager} to which the handlers can be registered.
     */
    void registerWorkItemHandlers(WorkItemManager workItemManager);

    /**
     * @return The parameters that should be used to start the process.
     */
    Map<String, Object> getStartParameters();

    /**
     * 'Perform' Human Tasks, i.e. take care of their life cycle and data output, as they are expected throughout the test run.
     * 
     * @param taskClient
     *            The API for handling tasks from within the process.
     */
    void performTasks(final TaskClient taskClient);
}