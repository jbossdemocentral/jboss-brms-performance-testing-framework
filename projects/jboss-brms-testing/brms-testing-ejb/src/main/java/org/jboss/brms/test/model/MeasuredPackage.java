package org.jboss.brms.test.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.Valid;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Metrics for a knowledge package, containing processes and rules.
 */
@Entity
public class MeasuredPackage extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The name of the process, as known in Guvnor. */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String packageName;

    /** The processes used in the test run. */
    @Valid
    private Set<MeasuredProcess> processes;

    /** Default constructor, required by JPA. */
    protected MeasuredPackage() {
    }

    /**
     * Parameterized constructor.
     * 
     * @param packageName
     *            The name of the process.
     */
    public MeasuredPackage(final String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public Set<MeasuredProcess> getProcesses() {
        if (processes == null) {
            processes = new HashSet<MeasuredProcess>();
        }
        return processes;
    }

    void setProcesses(final Set<MeasuredProcess> processes) {
        this.processes = processes;
    }

    public boolean addProcess(final MeasuredProcess process) {
        return getProcesses().add(process);
    }

    public String print() {
        return new StringBuilder().append("\n\n MeasuredPackage:\n  * Package Name: ").append(packageName).toString();
    }

    @Override
    public int hashCode() {
        int result = HASH_SEED;
        result = (PRIME * result) + ObjectUtils.hashCode(packageName);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MeasuredPackage)) {
            return false;
        }

        final MeasuredPackage other = (MeasuredPackage) obj;
        return ObjectUtils.equals(packageName, other.getProcesses());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcess [packageName=").append(packageName).append("]").toString();
    }
}
