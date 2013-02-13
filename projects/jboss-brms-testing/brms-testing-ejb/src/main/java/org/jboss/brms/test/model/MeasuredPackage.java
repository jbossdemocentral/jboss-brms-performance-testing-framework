package org.jboss.brms.test.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.brms.test.service.MetricsService;

/**
 * Metrics for a knowledge package, containing processes and rules.
 */
@Entity
public class MeasuredPackage extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The ID of the {@link Metrics} object this package belongs to. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private Long metricsId;

    /** The name of the process, as known in Guvnor. */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String packageName;

    /** The processes used in the test run. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Valid
    private Set<MeasuredProcess> processes;

    /** Default constructor, required by JPA. */
    protected MeasuredPackage() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} this package belongs to.
     * @param packageName
     *            The name of the process.
     */
    public MeasuredPackage(final Long metricsId, final String packageName) {
        this.metricsId = metricsId;
        this.packageName = packageName;
    }

    public Long getMetricsId() {
        return metricsId;
    }

    void setMetricsId(final Long metricsId) {
        this.metricsId = metricsId;
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
        result = (PRIME * result) + ObjectUtils.hashCode(metricsId);
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
        return ObjectUtils.equals(metricsId, other.getMetricsId()) && ObjectUtils.equals(packageName, other.getPackageName());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredProcess [packageName=").append(packageName).append("]").toString();
    }
}
