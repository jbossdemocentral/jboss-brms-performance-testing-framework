package org.jboss.brms.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Metrics for a rule (flow group) as used in a test run.
 */
@Entity
public class MeasuredRule extends PersistentObject {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String ruleFlowGroup;

    private Integer numberOfTimesActivated;

    /** Default constructor, required by JPA. */
    protected MeasuredRule() {
    }

    /**
     * Parameterized constructor.
     * 
     * @param ruleFlowGroup
     *            The identifier of the rule flow group.
     */
    public MeasuredRule(final String ruleFlowGroup) {
        this.ruleFlowGroup = ruleFlowGroup;
    }

    public String getRuleFlowGroup() {
        return ruleFlowGroup;
    }

    void setRuleFlowGroup(final String ruleFlowGroup) {
        this.ruleFlowGroup = ruleFlowGroup;
    }

    public Integer getNumberOfTimesActivated() {
        if (numberOfTimesActivated == null) {
            numberOfTimesActivated = Integer.valueOf(0);
        }
        return numberOfTimesActivated;
    }

    void setNumberOfTimesActivated(final Integer numberOfTimesActivated) {
        this.numberOfTimesActivated = numberOfTimesActivated;
    }

    public void increaseNumberOfTimesActivated() {
        setNumberOfTimesActivated(getNumberOfTimesActivated() + 1);
    }

    public String print() {
        return new StringBuilder().append("\n\n    MeasuredRule:\n     * Rule flow group: ").append(ruleFlowGroup)
                .append("\n     * Number of times activated: ").append(numberOfTimesActivated).toString();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = (PRIME * result) + ObjectUtils.hashCode(ruleFlowGroup);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MeasuredRule)) {
            return false;
        }

        final MeasuredRule other = (MeasuredRule) obj;
        return super.equals(other) && ObjectUtils.equals(ruleFlowGroup, other.getRuleFlowGroup());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("MeasuredRule [ruleFlowGroup=").append(ruleFlowGroup).append("]").toString();
    }
}
