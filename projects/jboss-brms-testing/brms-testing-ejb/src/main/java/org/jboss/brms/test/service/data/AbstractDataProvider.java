package org.jboss.brms.test.service.data;

import java.util.Map;

import org.drools.runtime.process.WorkItemManager;
import org.jbpm.task.service.TaskClient;

public abstract class AbstractDataProvider implements ProcessInstanceDataProvider {
    /** {@inheritDoc} */
    @Override
    public abstract String getProcessId();

    /** {@inheritDoc} */
    @Override
    public boolean containsHumanTasks() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void registerWorkItemHandlers(final WorkItemManager workItemManager) {
        // No handlers to be registered.
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getStartParameters() {
        // No parameters to be inserted at the start.
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void performTasks(final TaskClient taskClient) {
        // No tasks to be performed.
    }
}
