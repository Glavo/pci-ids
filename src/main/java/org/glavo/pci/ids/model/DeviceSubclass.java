/*
 * Copyright 2025 Glavo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glavo.pci.ids.model;

import org.glavo.pci.ids.internal.ArgumentValidator;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Java representation of a PCI device subclass. Each subclass has an 8 Bit ID, which is unique in
 * the scope of its device class, represented by two hex-characters, and a mandatory name. The
 * comment field is optional.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @since 0.3
 */
public final class DeviceSubclass implements Comparable<DeviceSubclass> {

    /**
     * Unique 8 Bit ID.
     */
    private final Integer id;
    private final String name;
    private final String comment;

    /**
     * Internal set of program interfaces belonging to this device class.
     */
    private final SortedMap<Integer, ProgramInterface> programInterfaces;

    /**
     * Create a new Device Subclass database entry.
     *
     * @param id      Unique 8 Bit ID
     * @param name    Full name of the device subclass
     * @param comment Optional comment, may be null
     */
    public DeviceSubclass(int id, String name, String comment) {
        ArgumentValidator.requireUnsignedByte(id, "Device subclass ID");
        ArgumentValidator.requireNonBlank(name, "Device subclass name");

        this.id = id;
        this.name = name;
        this.comment = comment;
        this.programInterfaces = new TreeMap<>();
    }

    /**
     * Add a new program interface to the internal interfaces map.
     *
     * @param iface Program interface to add
     */
    void addProgramInterface(ProgramInterface iface) {
        ArgumentValidator.requireNonNull(iface, "Device subclass program interface");

        this.programInterfaces.put(iface.getId(), iface);
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

    /**
     * Retrieve an unmodifiable view of the internal interfaces map.
     *
     * @return Unmodifiable map view
     */
    public SortedMap<Integer, ProgramInterface> getProgramInterfaces() {
        return Collections.unmodifiableSortedMap(this.programInterfaces);
    }

    /**
     * Compare this object to another {@link DeviceSubclass} object. Comparison will take place on
     * the integer representation of the unique ID.
     *
     * @param t Other object
     * @return Result of comparison
     */
    @Override
    public int compareTo(DeviceSubclass t) {
        return Integer.compare(this.id, t.id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceSubclass)) return false;
        DeviceSubclass that = (DeviceSubclass) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public String toString() {
        return String.format("DeviceSubclass[id=%02x, name='%s']", this.getId(), this.getName());
    }
}
