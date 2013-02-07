package org.jboss.brms.test.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.apache.commons.lang.ObjectUtils;

/**
 * Abstract base class for all persistent entities.
 */
@MappedSuperclass
public abstract class PersistentObject implements Serializable {
    /** Serial version identifier. */
    private static final long serialVersionUID = 1L;

    /** Seed for calculating specific hash codes. */
    public static final int HASH_SEED = 17;

    /** Prime multiplication factor for calculating specific hash codes. */
    public static final int PRIME = 31;

    /** The persistent object's database identity. */
    @Id
    @GeneratedValue
    @Column(name = "PO_ID")
    private Long id;

    /** The persistent object's version, to enable optimistic locking. */
    @Version
    @Column(name = "PO_VERSION")
    private Long version;

    /** The persistent object's original creation timestamp. */
    @Column(name = "PO_CREATED_ON", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    /** The persistent object's last modification timestamp. */
    @Column(name = "PO_MODIFIED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedOn;

    /** A unique identifier that can be used if the concrete persistent object has no distinctive business key. */
    @Column(name = "PO_UUID", nullable = false, updatable = false, unique = true)
    private UUID uuid;

    /** Default constructor, required by JPA. */
    protected PersistentObject() {
        uuid = UUID.randomUUID();
    }

    public Long getId() {
        return id;
    }

    void setId(final Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    void setVersion(final Long version) {
        this.version = version;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    @PrePersist
    void setCreationDate() {
        createdOn = new Date();
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    @PreUpdate
    void setModificationDate() {
        modifiedOn = new Date();
    }

    public UUID getUuid() {
        return uuid;
    }

    void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        int result = HASH_SEED;
        result = (PRIME * result) + ObjectUtils.hashCode(uuid);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        // Shortcuts.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PersistentObject)) {
            return false;
        }

        final PersistentObject other = (PersistentObject) obj;
        return ObjectUtils.equals(uuid, other.getUuid());
    }
}
