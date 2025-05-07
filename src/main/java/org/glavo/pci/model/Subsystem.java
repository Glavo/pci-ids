/*
 * Copyright 2017 Thomas Rix.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glavo.pci.model;

import org.glavo.pci.internal.ArgumentValidator;

import java.util.Objects;

/**
 * Java representation of a PCI device subsystem. Each subsystem is identified by the unique
 * combination of the 16 Bit ID of its vendor and its own 16 Bit ID. The name field is mandatory,
 * the comment field is optional.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @since 0.1
 */
public final class Subsystem implements Comparable<Subsystem> {

    /**
     * String representation of the unique 16 Bit ID.
     */
    private final String id;
    private final String name;
    private final String comment;

    /**
     * String representation of the unique 16 Bit subsystem vendor ID. This ID can be used as a link
     * to another {@link Vendor} object.
     */
    private final String vendorId;

    /**
     * Integer representation of the unique 16 Bit ID. For internal use only.
     */
    private final int numericId;

    /**
     * Integer representation of the unique 16 Bit subsystem vendor ID. For internal use only.
     */
    private final int numericVendorId;

    /**
     * Create a new Subsystem database entry.
     *
     * @param id       Device ID of this subsystem
     * @param name     Name of this subsystem
     * @param comment  Optional comment, may be null
     * @param vendorId Vendor ID of this subsystem's vendor
     */
    public Subsystem(String id, String name, String comment, String vendorId) {
        ArgumentValidator.requireStringLength(vendorId, 4, ArgumentValidator.NumberCompare.EQUAL, "Subsystem vendor ID");
        ArgumentValidator.requireStringLength(id, 4, ArgumentValidator.NumberCompare.EQUAL, "Subsystem ID");
        ArgumentValidator.requireNonBlank(name, "Subsystem name");

        this.id = id;
        this.name = name;
        this.comment = comment;
        this.vendorId = vendorId;

        this.numericId = Integer.parseInt(id, 16);
        this.numericVendorId = Integer.parseInt(vendorId, 16);
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    public String getVendorId() {
        return this.vendorId;
    }

    /**
     * Compare this object to another {@link Device} object. First comparison will take place on the
     * integer representation of the subsystem vendor unique ID. If these are equal, the integer
     * representation of the subsystem ID will be used for comparison.
     *
     * @param t Other object
     * @return Result of comparison
     */
    @Override
    public int compareTo(Subsystem t) {
        if (this.numericVendorId == t.numericVendorId) {
            return Integer.compare(this.numericId, t.numericId);
        }

        return Integer.compare(this.numericVendorId, t.numericVendorId);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Subsystem)) return false;
        Subsystem subsystem = (Subsystem) o;
        return Objects.equals(id, subsystem.id) && Objects.equals(name, subsystem.name) && Objects.equals(vendorId, subsystem.vendorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, vendorId);
    }

    public String toString() {
        return String.format("Subsystem[id=%s, name=%s, comment=%s, vendorId=%s, numericId=%d, numericVendorId=%d]",
                this.getId(), this.getName(), this.getComment(), this.getVendorId(), this.numericId, this.numericVendorId);
    }
}
