package com.mycookcode.bigData.ignite.model;

import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;

/**
 * Created by zhaolu on 2018/1/29.
 */
public class Address {

    /** Street. */
    private String street;

    /** ZIP code. */
    private int zip;

    /**
     * Required for binary deserialization.
     */
    public Address() {
        // No-op.
    }

    /**
     * @param street Street.
     * @param zip ZIP code.
     */
    public Address(String street, int zip) {
        this.street = street;
        this.zip = zip;
    }

    /** {@inheritDoc} */

    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString("street", street);
        writer.writeInt("zip", zip);
    }

    /** {@inheritDoc} */

    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        street = reader.readString("street");
        zip = reader.readInt("zip");
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "Address [street=" + street +
                ", zip=" + zip + ']';
    }
}
