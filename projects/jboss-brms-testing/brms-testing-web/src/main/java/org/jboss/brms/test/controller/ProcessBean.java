package org.jboss.brms.test.controller;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.jboss.brms.test.service.ProcessService;

@Named
@SessionScoped
public class ProcessBean implements Serializable {
    /** Serial version ID. */
    private static final long serialVersionUID = 1L;

    @Inject
    private transient Logger log;

    @Inject
    private ProcessService processService;

    public void startProcessInstance(final String packageName, final String processId) {
        log.info("Calling ProcessService to start process instance.");

        processService.startInstance(packageName, processId);
    }
}
