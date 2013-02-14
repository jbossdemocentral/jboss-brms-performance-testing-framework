package org.jboss.brms.test.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.brms.test.service.MetricsService;

/**
 * Data necessary to uniquely identify a process instance.
 */
@Embeddable
public class ProcessInstanceIdentifier implements Serializable {
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

    /** The ID of the knowledge session this instance runs/ran in. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private Integer ksessionId;

    /** The ID of the process instance. */
    @Column(nullable = false, updatable = false)
    @NotNull
    private Long processInstanceId;

    /** Default constructor, required by JPA. */
    protected ProcessInstanceIdentifier() {
    }

    /**
     * Parameterized constructor, for use by the {@link MetricsService}.
     * 
     * @param metricsId
     *            The ID of the {@link Metrics} the process instance belongs to.
     * @param packageName
     *            The name of the package the process is saved under in Guvnor.
     * @param processId
     *            The name of the process.
     * @param ksessionId
     *            The ID of the knowledge session the instance runs/ran in.
     * @param processInstanceId
     *            The ID of the process instance.
     */
    public ProcessInstanceIdentifier(final Long metricsId, final String packageName, final String processId, final Integer ksessionId,
            final Long processInstanceId) {
        this.metricsId = metricsId;
        this.packageName = packageName;
        this.processId = processId;
        this.ksessionId = ksessionId;
        this.processInstanceId = processInstanceId;
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

    public Integer getKsessionId() {
        return ksessionId;
    }

    void setKsessionId(final Integer ksessionId) {
        this.ksessionId = ksessionId;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    void setProcessInstanceId(final Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public ProcessIdentifier toProcessIdentifier() {
        return new ProcessIdentifier(metricsId, packageName, processId);
    }

    public String print() {
        return new StringBuilder().append("\n\t\t\t* Knowledge session ID: ").append(ksessionId).append("\n\t\t\t* Process instance ID: ")
                .append(processInstanceId).toString();
    }

    @Override
    public int hashCode() {
        int result = PersistentObject.HASH_SEED;
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(metricsId);
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(packageName);
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(processId);
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(ksessionId);
        result = (PersistentObject.PRIME * result) + ObjectUtils.hashCode(processInstanceId);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProcessInstanceIdentifier)) {
            return false;
        }

        final ProcessInstanceIdentifier other = (ProcessInstanceIdentifier) obj;
        return ObjectUtils.equals(metricsId, other.getMetricsId()) && ObjectUtils.equals(packageName, other.getPackageName())
                && ObjectUtils.equals(processId, other.getProcessId()) && ObjectUtils.equals(ksessionId, other.getKsessionId())
                && ObjectUtils.equals(processInstanceId, other.getProcessInstanceId());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("packageName=").append(packageName).append(", processId=").append(processId).append(super.toString())
                .append(", ksessionId=").append(ksessionId).append(", processInstanceId=").append(processInstanceId).toString();
    }
}
