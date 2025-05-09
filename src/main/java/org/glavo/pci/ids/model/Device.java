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
import java.util.SortedSet;
import java.util.TreeSet;

import org.glavo.pci.ids.internal.ArgumentValidator;

/**
 * Java representation of a PCI device. Each device has a 16 Bit ID, which is unique in the scope of
 * its vendor, represented by four hex-characters, and a mandatory name. The comment field is
 * optional.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @since 0.1
 */
public final class Device implements Comparable<Device> {

    /**
     * Unique 16 Bit ID
     */
    private final int id;
    private final String name;
    private final String comment;

    /**
     * Internal set of subsystems belonging to this device.
     */
    private final SortedSet<Subsystem> subsystems;

    /**
     * Create a new Device database entry.
     *
     * @param id      Unique 16 Bit ID
     * @param name    Full name of the device
     * @param comment Optional comment, may be null
     */
    public Device(int id, String name, String comment) {
        ArgumentValidator.requireUnsignedShort(id, "Device ID");
        ArgumentValidator.requireNonBlank(name, "Device name");

        this.id = id;
        this.name = name;
        this.comment = comment;
        this.subsystems = new TreeSet<>();
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
     * Add a new subsystem to the internal subsystems set.
     *
     * @param subsys Subsystem to add
     */
    void addSubsystem(Subsystem subsys) {
        ArgumentValidator.requireNonNull(subsys, "Device subsystem");

        this.subsystems.add(subsys);
    }

    /**
     * Retrieve an unmodifiable view of the subsystems set.
     *
     * @return Unmodifiable set view
     */
    public SortedSet<Subsystem> getSubsystems() {
        return Collections.unmodifiableSortedSet(this.subsystems);
    }

    /**
     * Compare this object to another {@link Device} object. Comparison will take place on the
     * integer representation of the unique ID.
     *
     * @param t Other object
     * @return Result of comparison
     */
    @Override
    public int compareTo(Device t) {
        return Integer.compare(this.id, t.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Device)) return false;
        Device device = (Device) o;
        return Objects.equals(id, device.id) && Objects.equals(name, device.name);
    }

    @Override
    public String toString() {
        return String.format("Device[id=%04x, name='%s']", id, name);
    }
}
