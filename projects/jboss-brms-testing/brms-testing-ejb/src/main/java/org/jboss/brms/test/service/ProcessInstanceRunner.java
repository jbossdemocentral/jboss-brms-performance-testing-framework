package org.jboss.brms.test.service;

import java.util.Map;
import java.util.UUID;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.drools.SystemEventListenerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jboss.brms.test.service.data.DataProviderLookup;
import org.jboss.brms.test.service.data.ProcessInstanceDataProvider;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.hornetq.CommandBasedHornetQWSHumanTaskHandler;
import org.jbpm.task.service.hornetq.HornetQTaskClientConnector;
import org.jbpm.task.service.hornetq.HornetQTaskClientHandler;

@Stateless
public class ProcessInstanceRunner {
    @Asynchronous
    public void runAsync(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession) {
        run(ksession, processId, disposeSession);
    }

    public void runSync(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession) {
        run(ksession, processId, disposeSession);
    }

    private void run(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession) {
        final ProcessInstanceDataProvider dataProvider = DataProviderLookup.getDataProvider(processId);
        TaskClient taskClient = null;
        Map<String, Object> params = null;
        if (dataProvider != null) {
            dataProvider.registerWorkItemHandlers(ksession.getWorkItemManager());
            if (dataProvider.containsHumanTasks()) {
                taskClient = getTaskClient(ksession);
            }
            params = dataProvider.getStartParameters();
        }

        ksession.startProcess(processId, params);
        ksession.fireAllRules();
        if ((dataProvider != null) && (taskClient != null)) {
            dataProvider.performTasks(taskClient);
        }

        if (disposeSession) {
            ksession.dispose();
        }
    }

    private TaskClient getTaskClient(final StatefulKnowledgeSession ksession) {
        final TaskClient client = new TaskClient(new HornetQTaskClientConnector("HornetQConnector" + UUID.randomUUID(), new HornetQTaskClientHandler(
                SystemEventListenerFactory.getSystemEventListener())));
        client.connect("127.0.0.1", 5153);
        final CommandBasedHornetQWSHumanTaskHandler handler = new CommandBasedHornetQWSHumanTaskHandler(ksession);
        handler.setClient(client);
        handler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        return client;
    }
}
