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
package org.glavo.pci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.glavo.pci.internal.ArgumentValidator;
import org.glavo.pci.internal.DatabaseFileParser;
import org.glavo.pci.model.Device;
import org.glavo.pci.model.DeviceClass;
import org.glavo.pci.model.DeviceSubclass;
import org.glavo.pci.model.ProgramInterface;
import org.glavo.pci.model.Subsystem;
import org.glavo.pci.model.Vendor;

/**
 * Main entry point into the PCI IDs database library.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @see <a href="https://pci-ids.ucw.cz/">The PCI ID Repository</a>
 * @since 0.1
 */
public final class PCIIDsDatabase {

    public static PCIIDsDatabase load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return load(reader);
        }
    }

    public static PCIIDsDatabase load(InputStream inputStream) throws IOException {
        return load(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
    }

    public static PCIIDsDatabase load(BufferedReader reader) throws IOException {
        ArgumentValidator.requireNonNull(reader, "PCI IDs database reader");
        PCIIDsDatabase database = new PCIIDsDatabase();
        DatabaseFileParser parser = new DatabaseFileParser();
        parser.parseDatabaseFile(reader, database.vendorDatabase, database.deviceClassDatabase);
        return database;
    }

    private final SortedMap<Integer, Vendor> vendorDatabase;
    private final SortedMap<Integer, DeviceClass> deviceClassDatabase;

    /**
     * Create a new empty database
     */
    private PCIIDsDatabase() {
        this.vendorDatabase = new TreeMap<>();
        this.deviceClassDatabase = new TreeMap<>();
    }

    /**
     * Retrieve a list of all vendors found in the database. If the database is empty, the returned
     * list is empty as well.
     *
     * @return List of vendors
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.1
     */
    public List<Vendor> findAllVendors() {
        return new ArrayList<>(this.vendorDatabase.values());
    }

    /**
     * Retrieve a specific vendor from the database. If the vendor ID does not exist, the return
     * value is <tt>null</tt>.
     *
     * @param vendorId Vendor ID to search for
     * @return Requested vendor object or null
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.1
     */
    public Vendor findVendor(final int vendorId) {
        return this.vendorDatabase.get(vendorId);
    }

    /**
     * Retrieve a list of all known devices for a specific vendor. If the vendor does not exist or
     * no devices are known for this vendor, the returned list is empty.
     *
     * @param vendorId Vendor ID to search for
     * @return List of vendor's devices
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.1
     */
    public List<Device> findAllDevices(final int vendorId) {
        Vendor v = this.vendorDatabase.get(vendorId);
        if (v == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(v.getDevices().values());
    }

    /**
     * Retrieve a specific device for a specific vendor. If the vendor or device does not exist, the
     * return value is <tt>null</tt>.
     *
     * @param vendorId Vendor ID to search for
     * @param deviceId Device ID to search for
     * @return Requested device object or null
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.1
     */
    public Device findDevice(final int vendorId, final int deviceId) {
        Vendor v = this.vendorDatabase.get(vendorId);
        if (v == null) {
            return null;
        }

        return v.getDevices().get(deviceId);
    }

    /**
     * Retrieve a list of all known subsystems for a specific device. If the vendor or device does
     * not exist or no subsystems are known for this device, the returned list is empty. The result
     * list is sorted by subvendor ID and subsystem ID.
     *
     * @param vendorId Vendor ID to search for
     * @param deviceId Device ID to search for
     * @return List of device's subsystems
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.1
     */
    public List<Subsystem> findAllSubsystems(final int vendorId, final int deviceId) {
        Vendor v = this.vendorDatabase.get(vendorId);
        if (v == null) {
            return Collections.emptyList();
        }

        Device d = v.getDevices().get(deviceId);
        if (d == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(d.getSubsystems());
    }

    /**
     * Retrieve a list of all known subsystems for a specific device that share a specific vendor.
     * If the vendor, device, or subvendor does not exist or no subsystems are known for this
     * device, the returned list is empty. The result list is sorted by subvendor ID and subsystem
     * ID.
     *
     * @param vendorId    Vendor ID to search for
     * @param deviceId    Device ID to search for
     * @param subvendorId Subsystem vendor ID to search for
     * @return List of device's subsystems
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.1
     */
    public List<Subsystem> findAllSubsystemsWithVendor(final int vendorId, final int deviceId, final int subvendorId) {
        Vendor v = this.vendorDatabase.get(vendorId);
        if (v == null) {
            return Collections.emptyList();
        }

        Device d = v.getDevices().get(deviceId);
        if (d == null) {
            return Collections.emptyList();
        }

        return d.getSubsystems().stream()
                .filter(s -> s.getVendorId() == subvendorId)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a list of all device classes found in the database. If the database is empty, the
     * returned list is empty as well.
     *
     * @return List of device classes
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.3
     */
    public List<DeviceClass> findAllDeviceClasses() {
        return new ArrayList<>(this.deviceClassDatabase.values());
    }

    /**
     * Retrieve a specific device class from the database. If the vendor ID does not exist, the
     * return value is <tt>null</tt>.
     *
     * @param classId Device class ID to search for
     * @return Requested device class object or null
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.3
     */
    public DeviceClass findDeviceClass(final int classId) {
        return this.deviceClassDatabase.get(classId);
    }

    /**
     * Retrieve a list of all known subclasses for a specific device class. If the device class does
     * not exist or no subclasses are known for this device class, the returned list is empty.
     *
     * @param classId Device class ID to search for
     * @return List of device class' subclasses
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.3
     */
    public List<DeviceSubclass> findAllDeviceSubclasses(final int classId) {
        DeviceClass d = this.deviceClassDatabase.get(classId);
        if (d == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(d.getSubclasses().values());
    }

    /**
     * Retrieve a specific subclass for a specific device class. If the device class or subclass
     * does not exist, the return value is <tt>null</tt>.
     *
     * @param classId    Device class ID to search for
     * @param subclassId Subclass ID to search for
     * @return Requested subclass object or null
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.3
     */
    public DeviceSubclass findDeviceSubclass(final int classId, final int subclassId) {
        DeviceClass d = this.deviceClassDatabase.get(classId);
        return d != null ? d.getSubclasses().get(subclassId) : null;
    }

    /**
     * Retrieve a list of all known program interfaces for a specific device subclass. If the device
     * subclass does not exist or no program interfaces are known for this device subclass, the
     * returned list is empty.
     *
     * @param classId    Device class ID to search for
     * @param subclassId Subclass ID to search for
     * @return List of device subclass' program interfaces
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.3
     */
    public List<ProgramInterface> findAllProgramInterfaces(final int classId, final int subclassId) {
        DeviceClass d = this.deviceClassDatabase.get(classId);
        if (d == null) {
            return Collections.emptyList();
        }

        DeviceSubclass s = d.getSubclasses().get(subclassId);
        if (s == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(s.getProgramInterfaces().values());
    }

    /**
     * Retrieve a specific program interface for a specific device subclass. If the device class,
     * subclass, or program interface does not exist, the return value is <tt>null</tt>.
     *
     * @param classId    Device class ID to search for
     * @param subclassId Subclass ID to search for
     * @param ifaceId    Program interface ID to search for
     * @return Requested program interface object or null
     * @throws IllegalStateException if database is not ready, i.e. no database file was loaded
     * @since 0.3
     */
    public ProgramInterface findProgramInterface(final int classId, final int subclassId, final int ifaceId) {
        DeviceClass d = this.deviceClassDatabase.get(classId);
        if (d == null) {
            return null;
        }

        DeviceSubclass s = d.getSubclasses().get(subclassId);
        if (s == null) {
            return null;
        }

        return s.getProgramInterfaces().get(ifaceId);
    }

}
