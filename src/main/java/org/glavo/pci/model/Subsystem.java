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
     * Unique 16 Bit ID.
     */
    private final int id;
    private final String name;
    private final String comment;

    /**
     * The unique 16 Bit subsystem vendor ID. This ID can be used as a link
     * to another {@link Vendor} object.
     */
    private final int vendorId;

    /**
     * Create a new Subsystem database entry.
     *
     * @param id       Device ID of this subsystem
     * @param name     Name of this subsystem
     * @param comment  Optional comment, may be null
     * @param vendorId Vendor ID of this subsystem's vendor
     */
    public Subsystem(int id, String name, String comment, int vendorId) {
        ArgumentValidator.requireUnsignedShort(vendorId, "Subsystem vendor ID");
        ArgumentValidator.requireUnsignedShort(id, "Subsystem ID");
        ArgumentValidator.requireNonBlank(name, "Subsystem name");

        this.id = id;
        this.name = name;
        this.comment = comment;
        this.vendorId = vendorId;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    public int getVendorId() {
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
        if (this.vendorId == t.vendorId) {
            return Integer.compare(this.id, t.id);
        }

        return Integer.compare(this.vendorId, t.vendorId);
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
        return String.format("Subsystem[id=%04x, name=%s, comment=%s, vendorId=%04x]", this.getId(), this.getName(), this.getComment(), this.getVendorId());
    }
}
