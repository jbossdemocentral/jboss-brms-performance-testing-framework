package org.jboss.brms.test.service;

import java.util.Map;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.service.data.DataProviderLookup;
import org.jboss.brms.test.service.data.ProcessInstanceDataProvider;
import org.jbpm.task.service.TaskClient;

@Stateless
public class ProcessInstanceRunner {
    @Asynchronous
    public void runAsync(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession, final TaskClient taskClient) {
        run(ksession, processId, disposeSession, taskClient);
    }

    public void runSync(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession, final TaskClient taskClient) {
        run(ksession, processId, disposeSession, taskClient);
    }

    private void run(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession, final TaskClient taskClient) {
        final ProcessInstanceDataProvider dataProvider = DataProviderLookup.getDataProvider(processId);
        Map<String, Object> params = null;
        if (dataProvider != null) {
            dataProvider.registerWorkItemHandlers(ksession.getWorkItemManager());
            params = dataProvider.getStartParameters();
        }

        ksession.startProcess(processId, params);
        ksession.fireAllRules();
        if ((dataProvider != null) && (dataProvider.containsHumanTasks())) {
            dataProvider.performTasks(taskClient);
        }

        if (disposeSession) {
            ksession.dispose();
        }
    }
}
