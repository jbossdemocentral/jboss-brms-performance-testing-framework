package org.jboss.brms.test.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.brms.test.service.MetricsService;

/**
 * Metrics for a process (definition) as used in a test run.
 */
@Entity
public class MeasuredProcess extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The ID of the {@link Metrics} object this process belongs to. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private Long metricsId;

    /** The name of the process, as known in Guvnor. */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String processId;

    @Valid
    private Set<MeasuredProcessInstance> instances;

    /** Default constructor, required by JPA. */
    protected MeasuredProcess() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} this package belongs to.
     * @param id
     *            The name of the process.
     */
    public MeasuredProcess(final Long metricsId, final String id) {
        this.metricsId = metricsId;
        processId = id;
    }

    public Long getMetricsId() {
        return metricsId;
    }

    void setMetricsId(final Long metricsId) {
        this.metricsId = metricsId;
    }

    public String getProcessId() {
        return processId;
    }

    void setProcessId(final String processId) {
        this.processId = processId;
    }

    public Set<MeasuredProcessInstance> getInstances() {
        if (instances == null) {
            instances = new HashSet<MeasuredProcessInstance>();
        }
        return instances;
    }

    void setInstances(final Set<MeasuredProcessInstance> instances) {
        this.instances = instances;
    }

    public boolean addInstance(final MeasuredProcessInstance instance) {
        return getInstances().add(instance);
    }

    public String print() {
        return new StringBuilder().append("\n\n  MeasuredProcess:\n   * Process ID: ").append(processId).append("\n   * Number of times instantiated: ")
                .append(getInstances().size()).toString();
    }

    @Override
    public int hashCode() {
        int result = HASH_SEED;
        result = (PRIME * result) + ObjectUtils.hashCode(metricsId);
        result = (PRIME * result) + ObjectUtils.hashCode(processId);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MeasuredProcess)) {
            return false;
        }

        final MeasuredProcess other = (MeasuredProcess) obj;
        return ObjectUtils.equals(metricsId, other.getMetricsId()) && ObjectUtils.equals(processId, other.getProcessId());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcess [processId=").append(processId).append("]").toString();
    }
}
