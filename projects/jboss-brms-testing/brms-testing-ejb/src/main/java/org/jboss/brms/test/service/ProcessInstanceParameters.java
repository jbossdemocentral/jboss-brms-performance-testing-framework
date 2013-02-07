package org.jboss.brms.test.service;

import java.io.Serializable;
import java.util.Date;

import org.drools.runtime.StatefulKnowledgeSession;

public class ProcessInstanceParameters implements Serializable {
    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    private StatefulKnowledgeSession ksession;
    private String processId;
    private Date startMoment;

    public ProcessInstanceParameters(final StatefulKnowledgeSession ksession, final String processId) {
        this.ksession = ksession;
        this.processId = processId;
    }

    public StatefulKnowledgeSession getKsession() {
        return ksession;
    }

    public void setKsession(final StatefulKnowledgeSession ksession) {
        this.ksession = ksession;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(final String processId) {
        this.processId = processId;
    }

    public Date getStartMoment() {
        return startMoment;
    }

    public void setStartMoment(final Date startMoment) {
        this.startMoment = startMoment;
    }
}
