package com.mycookcode.bigData.ignite.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaolu on 2018/1/29.
 */
public class Organization {

    /** */
    private static final AtomicLong ID_GEN = new AtomicLong();

    /** Organization ID (indexed). */
    @QuerySqlField(index = true)
    private Long id;

    /** Organization name (indexed). */
    @QuerySqlField(index = true)
    private String name;

    /** Address. */
    private Address addr;

    /** Type. */
    private OrganizationType type;

    /** Last update time. */
    private Timestamp lastUpdated;

    /**
     * Required for binary deserialization.
     */
    public Organization() {
        // No-op.
    }

    /**
     * @param name Organization name.
     */
    public Organization(String name) {
        id = ID_GEN.incrementAndGet();

        this.name = name;
    }

    /**
     * @param id Organization ID.
     * @param name Organization name.
     */
    public Organization(long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @param name Name.
     * @param addr Address.
     * @param type Type.
     * @param lastUpdated Last update time.
     */
    public Organization(String name, Address addr, OrganizationType type, Timestamp lastUpdated) {
        id = ID_GEN.incrementAndGet();

        this.name = name;
        this.addr = addr;
        this.type = type;

        this.lastUpdated = lastUpdated;
    }

    /**
     * @return Organization ID.
     */
    public Long id() {
        return id;
    }

    /**
     * @return Name.
     */
    public String name() {
        return name;
    }

    /**
     * @return Address.
     */
    public Address address() {
        return addr;
    }

    /**
     * @return Type.
     */
    public OrganizationType type() {
        return type;
    }

    /**
     * @return Last update time.
     */
    public Timestamp lastUpdated() {
        return lastUpdated;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "Organization [id=" + id +
                ", name=" + name +
                ", address=" + addr +
                ", type=" + type +
                ", lastUpdated=" + lastUpdated + ']';
    }

}
