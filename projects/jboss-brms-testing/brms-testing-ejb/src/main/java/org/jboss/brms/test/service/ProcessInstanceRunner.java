package org.jboss.brms.test.service;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.drools.runtime.StatefulKnowledgeSession;

@Stateless
public class ProcessInstanceRunner {
    @Asynchronous
    public void runAsync(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession) {
        ksession.startProcess(processId);
        ksession.fireAllRules();

        if (disposeSession) {
            ksession.dispose();
        }
    }

    public void runSync(final StatefulKnowledgeSession ksession, final String processId, final boolean disposeSession) {
        ksession.startProcess(processId);
        ksession.fireAllRules();

        if (disposeSession) {
            ksession.dispose();
        }
    }
}
