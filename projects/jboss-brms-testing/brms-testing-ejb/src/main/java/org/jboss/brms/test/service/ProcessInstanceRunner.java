package org.jboss.brms.test.service;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.drools.runtime.StatefulKnowledgeSession;

@Stateless
public class ProcessInstanceRunner {
    @Asynchronous
    public void runAsync(final StatefulKnowledgeSession ksession, final String processId) {
        ksession.startProcess(processId);
        ksession.fireAllRules();
    }

    public void runSync(final StatefulKnowledgeSession ksession, final String processId) {
        ksession.startProcess(processId);
        ksession.fireAllRules();
    }
}
