package org.jboss.brms.test.service;

import java.util.ArrayList;
import java.util.List;

public class ProcessStartParameters {
    private List<ProcessIndicator> indicators;
    private boolean startInParallel;
    private boolean runInIndividualKnowledgeSession;

    public List<ProcessIndicator> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<ProcessStartParameters.ProcessIndicator>();
        }
        return indicators;
    }

    void setIndicators(final List<ProcessIndicator> indicators) {
        this.indicators = indicators;
    }

    public boolean addIndicator(final ProcessIndicator indicator) {
        return getIndicators().add(indicator);
    }

    public boolean isStartInParallel() {
        return startInParallel;
    }

    public void setStartInParallel(final boolean startInParallel) {
        this.startInParallel = startInParallel;
    }

    public boolean isRunInIndividualKnowledgeSession() {
        return runInIndividualKnowledgeSession;
    }

    public void setRunInIndividualKnowledgeSession(final boolean runInIndividualKnowledgeSession) {
        this.runInIndividualKnowledgeSession = runInIndividualKnowledgeSession;
    }

    public static class ProcessIndicator {
        private final String packageName;
        private final String processId;
        private final int numberOfInstances;

        public ProcessIndicator(final String packageName, final String processId, final int numberOfInstances) {
            this.packageName = packageName;
            this.processId = processId;
            this.numberOfInstances = numberOfInstances;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getProcessId() {
            return processId;
        }

        public int getNumberOfInstances() {
            return numberOfInstances;
        }
    }
}
