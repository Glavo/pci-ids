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

import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.glavo.pci.ids.internal.ArgumentValidator;

/**
 * Java representation of a PCI device vendor. Each vendor has a unique 16 Bit ID, represented by
 * four hex-characters, and a mandatory name. The comment field is optional.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @since 0.1
 */
public final class Vendor implements Comparable<Vendor> {

    /**
     * Unique 16 Bit ID.
     */
    private final int id;
    private final String name;
    private final String comment;

    /**
     * Internal map of devices belonging to this vendor. Identified by their unique 16 Bit ID.
     */
    private final SortedMap<Integer, Device> devices;

    /**
     * Create a new Vendor database entry.
     *
     * @param id      Unique 16 Bit ID
     * @param name    Full name of the vendor
     * @param comment Optional comment, may be null
     */
    public Vendor(int id, String name, String comment) {
        ArgumentValidator.requireUnsignedShort(id, "Vendor ID");
        ArgumentValidator.requireNonBlank(name, "Vendor name");

        this.id = id;
        this.name = name;
        this.comment = comment;
        this.devices = new TreeMap<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    /**
     * Add a new device to the internal devices map.
     *
     * @param device Device to add
     */
    void addDevice(Device device) {
        ArgumentValidator.requireNonNull(device, "Vendor device");

        this.devices.put(device.getId(), device);
    }

    /**
     * Retrieve an unmodifiable view of the devices map.
     *
     * @return Unmodifiable map view
     */
    public SortedMap<Integer, Device> getDevices() {
        return Collections.unmodifiableSortedMap(devices);
    }

    /**
     * Compare this object to another {@link Vendor} object. Comparison will take place on the
     * integer representation of the unique ID.
     *
     * @param t Other object
     * @return Result of comparison
     */
    @Override
    public int compareTo(Vendor t) {
        return Integer.compare(this.id, t.id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vendor)) return false;
        Vendor vendor = (Vendor) o;
        return Objects.equals(id, vendor.id) && Objects.equals(name, vendor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return String.format("Vendor[id=%04x, name='%s']", id, name);
    }
}
