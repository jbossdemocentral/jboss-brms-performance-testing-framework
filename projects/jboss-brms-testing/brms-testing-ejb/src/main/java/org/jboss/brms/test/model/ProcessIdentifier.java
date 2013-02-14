package org.jboss.brms.test.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.brms.test.service.MetricsService;

/**
 * Data necessary to uniquely identify a process.
 */
@Embeddable
public class ProcessIdentifier implements Serializable {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** The ID of the {@link Metrics} object the process belongs to. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private Long metricsId;

    /** The name of the package the process is saved under in Guvnor. */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String packageName;

    /** The name of the process, as known in Guvnor. */
    @Column(nullable = false, updatable = false)
    @NotBlank
    private String processId;

    /** Default constructor, required by JPA. */
    protected ProcessIdentifier() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} this process belongs to.
     * @param packageName
     *            The name of the package the process is saved under in Guvnor.
     * @param processId
     *            The name of the process.
     */
    public ProcessIdentifier(final Long metricsId, final String packageName, final String processId) {
        this.metricsId = metricsId;
        this.packageName = packageName;
        this.processId = processId;
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

    void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getProcessId() {
        return processId;
    }

    void setProcessId(final String processId) {
        this.processId = processId;
    }

    public String print() {
        return new StringBuilder().append("\n\t\t* Package name: ").append(packageName).append("\n\t\t* Process ID: ").append(processId).toString();
    }

    @Override
    public int hashCode() {
        int result = PersistentObject.HASH_SEED;
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(metricsId);
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(packageName);
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(processId);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProcessIdentifier)) {
            return false;
        }

        final ProcessIdentifier other = (ProcessIdentifier) obj;
        return ObjectUtils.equals(metricsId, other.getMetricsId()) && ObjectUtils.equals(packageName, other.getPackageName())
                && ObjectUtils.equals(processId, other.getProcessId());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("packageName=").append(packageName).append(", processId=").append(processId).toString();
    }
}
