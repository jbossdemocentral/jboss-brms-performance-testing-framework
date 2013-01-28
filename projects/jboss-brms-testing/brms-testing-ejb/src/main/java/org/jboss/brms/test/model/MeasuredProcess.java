package org.jboss.brms.test.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.Valid;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Metrics for a process (definition) as used in a test run.
 */
@Entity
public class MeasuredProcess extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

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
     * Parameterized constructor.
     * 
     * @param id
     *            The name of the process.
     */
    public MeasuredProcess(final String id) {
        processId = id;
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

    @Override
    public int hashCode() {
        int result = HASH_SEED;
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
        return ObjectUtils.equals(processId, other.getProcessId());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcess [processId=").append(processId).append("]").toString();
    }
}
