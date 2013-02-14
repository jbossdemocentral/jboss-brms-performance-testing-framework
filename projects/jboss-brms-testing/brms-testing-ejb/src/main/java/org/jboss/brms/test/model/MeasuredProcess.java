package org.jboss.brms.test.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.Valid;

import org.apache.commons.lang.ObjectUtils;
import org.jboss.brms.test.service.MetricsService;

/**
 * Metrics for a process (definition) as used in a test run.
 */
@Entity
public class MeasuredProcess extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** Data to uniquely identify a process. */
    @Embedded
    @Valid
    private ProcessIdentifier identifier;

    /** The instances started from this process (definition). */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Valid
    private Set<MeasuredProcessInstance> instances;

    /** Default constructor, required by JPA. */
    protected MeasuredProcess() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param identifier
     *            Data to uniquely identify a process.
     */
    public MeasuredProcess(final ProcessIdentifier identifier) {
        this.identifier = identifier;
    }

    public ProcessIdentifier getIdentifier() {
        return identifier;
    }

    void setIdentifier(final ProcessIdentifier identifier) {
        this.identifier = identifier;
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
        return new StringBuilder().append("\n\n\t\tMeasuredProcess:").append(identifier.print()).append("\n\t\t* Number of times instantiated: ")
                .append(getInstances().size()).toString();
    }

    @Override
    public int hashCode() {
        return (PRIME * HASH_SEED) + ObjectUtils.hashCode(identifier);
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
        return ObjectUtils.equals(identifier, other.getIdentifier());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcess [").append(identifier).append("]").toString();
    }
}
