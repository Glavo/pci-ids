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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Java representation of a PCI device class. Each class has an 8 Bit unique ID, represented by two
 * hex-characters, and a mandatory name. The comment field is optional.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @since 0.3
 */
public final class DeviceClass implements Comparable<DeviceClass> {

    /**
     * String representation of the unique 8 Bit ID.
     */
    private final String id;
    private final String name;
    private final String comment;

    /**
     * Internal map of subclasses belonging to this device class. Identified by their unique 8 Bit
     * ID.
     */
    private final Map<String, DeviceSubclass> subclasses;

    /**
     * Integer representation of the unique 8 Bit ID. For internal use only.
     */
    private final int numericId;

    /**
     * Create a new Device Class database entry.
     *
     * @param id      Unique 8 Bit ID
     * @param name    Full name of the device class
     * @param comment Optional comment, may be null
     */
    public DeviceClass(String id, String name, String comment) {
        ArgumentValidator.requireStringLength(id, 2, ArgumentValidator.NumberCompare.EQUAL, "Device class ID");
        ArgumentValidator.requireNonBlank(name, "Device class name");

        this.id = id;
        this.name = name;
        this.comment = comment;
        this.subclasses = new HashMap<>();

        this.numericId = Integer.parseInt(id, 16);
    }

    /**
     * Add a new device subclass to the internal subclasses map.
     *
     * @param subclass Subclass to add
     */
    public void addSubclass(DeviceSubclass subclass) {
        ArgumentValidator.requireNonNull(subclass, "Device subclass");

        this.subclasses.put(subclass.getId(), subclass);
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

    /**
     * Retrieve an unmodifiable view of the device subclasses map.
     *
     * @return Unmodifiable map view
     */
    public Map<String, DeviceSubclass> getSubclasses() {
        return Collections.unmodifiableMap(this.subclasses);
    }

    /**
     * Compare this object to another {@link DeviceClass} object. Comparison will take place on the
     * integer representation of the unique ID.
     *
     * @param t Other object
     * @return Result of comparison
     */
    @Override
    public int compareTo(DeviceClass t) {
        return Integer.compare(this.numericId, t.numericId);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceClass)) return false;
        DeviceClass that = (DeviceClass) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public String toString() {
        return String.format("DeviceClass[id=%s, name=%s, comment=%s, subclasses=%s, numericId=%s]",
                this.getId(), this.getName(), this.getComment(), this.getSubclasses(), this.numericId);
    }
}
