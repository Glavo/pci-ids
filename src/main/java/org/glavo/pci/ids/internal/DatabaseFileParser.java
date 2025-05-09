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
package org.glavo.pci.ids.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glavo.pci.ids.model.Device;
import org.glavo.pci.ids.model.DeviceClass;
import org.glavo.pci.ids.model.DeviceSubclass;
import org.glavo.pci.ids.model.ModelAccessImpl;
import org.glavo.pci.ids.model.ProgramInterface;
import org.glavo.pci.ids.model.Subsystem;
import org.glavo.pci.ids.model.Vendor;

import java.util.Map;

/**
 * Parser class for contents of the PCI IDs file.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @since 0.1
 */
public final class DatabaseFileParser {

    private static final ModelAccess ACCESS = new ModelAccessImpl(new ModelAccess.Key());

    /**
     * RegEx pattern used to parse database file lines of type {@link LineType#VENDOR VENDOR}.
     */
    private final Pattern VENDOR_PATTERN = Pattern.compile("^([0-9a-f]{4})\\s+(.+)$");

    /**
     * RegEx pattern used to parse database file lines of type {@link LineType#DEVICE DEVICE}.
     */
    private final Pattern DEVICE_PATTERN = Pattern.compile("^\\t([0-9a-f]{4})\\s+(.+)$");

    /**
     * RegEx pattern used to parse database file lines of type {@link LineType#SUBSYSTEM SUBSYSTEM}.
     */
    private final Pattern SUBSYS_PATTERN = Pattern.compile("^\\t\\t([0-9a-f]{4})\\s([0-9a-f]{4})\\s+(.+)$");

    /**
     * RegEx pattern used to parse database file lines of type
     * {@link LineType#DEVICE_CLASS DEVICE_CLASS}.
     */
    private final Pattern DEVCLASS_PATTERN = Pattern.compile("^C\\s([0-9a-f]{2})\\s+(.+)$");

    /**
     * RegEx pattern used to parse database file lines of type
     * {@link LineType#DEVICE_SUBCLASS DEVICE_SUBCLASS}.
     */
    private final Pattern SUBCLASS_PATTERN = Pattern.compile("^\\t([0-9a-f]{2})\\s+(.+)$");

    /**
     * RegEx pattern used to parse database file lines of type
     * {@link LineType#PROGRAM_INTERFACE PROGRAM_INTERFACE}.
     */
    private final Pattern PROGIFACE_PATTERN = Pattern.compile("^\\t\\t([0-9a-f]{2})\\s+(.+)$");

    /**
     * Current comment from database file will be stored here and applied to the next created entry.
     */
    private String currentComment = null;

    /**
     * Process the contents of the PCI IDs database file. The complete file is read and parsed The
     * resulting objects are filled into the corresponding database maps provided in
     * <tt>vendorDb</tt> and <tt>deviceClassDb</tt>. Each {@link Vendor} object is filled with the
     * corresponding {@link Device} and {@link Subsystem} objects. Each {@link DeviceClass} object
     * is filled with the corresponding {@link DeviceSubclass} and {@link ProgramInterface} objects.
     *
     * @param reader        Reader holding the database file contents
     * @param vendorDb      Database map to fill with Vendor objects
     * @param deviceClassDb Database map to fill with DeviceClass objects
     * @throws IOException if the database file is malformed or otherwise unprocessable
     */
    public void parseDatabaseFile(BufferedReader reader, final Map<Integer, Vendor> vendorDb, Map<Integer, DeviceClass> deviceClassDb) throws IOException {
        ArgumentValidator.requireNonNull(reader, "PCI IDs database InputStream");

        try {
            Vendor currentVendor = null;
            Device currentDevice = null;
            DeviceClass currentClass = null;
            DeviceSubclass currentSubclass = null;

            String line = reader.readLine();
            LineType previousLineType = null;
            while (line != null) {
                // If current line is blank, discard previous comments and continue
                // This is used to discard the file header
                if (line.trim().isEmpty()) {
                    this.currentComment = null;
                    line = reader.readLine();
                    continue;
                }

                switch (this.determineLineType(line, previousLineType)) {
                    case COMMENT:
                        if (this.currentComment != null) {
                            // This is a multiline comment, append the current comment to the previous line
                            this.currentComment += "\n" + line.substring(1).trim();
                        } else {
                            this.currentComment = line.substring(1).trim();
                        }
                        break;
                    case DEVICE:
                        previousLineType = LineType.DEVICE;

                        // Handle previous device
                        if (currentDevice != null) {
                            if (currentVendor == null) {
                                throw new IOException("Encountered unexpected device line");
                            }
                            // Add the previous device to the current vendor
                            ACCESS.addDevice(currentVendor, currentDevice);
                        }
                        currentDevice = this.parseDeviceLine(line);
                        break;
                    case SUBSYSTEM:
                        previousLineType = LineType.SUBSYSTEM;

                        if (currentDevice == null) {
                            throw new IOException("Encountered unexpected subsystem line");
                        }

                        ACCESS.addSubsystem(currentDevice, this.parseSubsystemLine(line));
                        break;
                    case VENDOR:
                        previousLineType = LineType.VENDOR;

                        // Handle previous vendor
                        if (currentVendor != null) {
                            // Is there a device left to be added to the previous vendor?
                            if (currentDevice != null) {
                                ACCESS.addDevice(currentVendor, currentDevice);
                                currentDevice = null;
                            }
                            // Add the previous vendor to the return list
                            vendorDb.put(currentVendor.getId(), currentVendor);
                        }
                        currentVendor = this.parseVendorLine(line);
                        break;
                    case DEVICE_CLASS:
                        previousLineType = LineType.DEVICE_CLASS;

                        // Handle previous device class
                        if (currentClass != null) {
                            // Is there a subclass left to be added to the previous device class?
                            if (currentSubclass != null) {
                                ACCESS.addSubclass(currentClass, currentSubclass);
                                currentSubclass = null;
                            }
                            // Add the previous device class to the return list
                            deviceClassDb.put(currentClass.getId(), currentClass);
                        }
                        currentClass = this.parseDeviceClassLine(line);
                        break;
                    case DEVICE_SUBCLASS:
                        previousLineType = LineType.DEVICE_SUBCLASS;

                        // Handle previous device subclass
                        if (currentSubclass != null) {
                            if (currentClass == null) {
                                throw new IOException("Encountered unexpected device subclass line");
                            }
                            // Add the previous subclass to the current device class
                            ACCESS.addSubclass(currentClass, currentSubclass);
                        }
                        currentSubclass = this.parseDeviceSubclassLine(line);
                        break;
                    case PROGRAM_INTERFACE:
                        previousLineType = LineType.PROGRAM_INTERFACE;

                        if (currentSubclass == null) {
                            throw new IOException("Encountered unexpected program interface line");
                        }
                        ACCESS.addProgramInterface(currentSubclass, this.parseProgramInterfaceLine(line));
                        break;
                    default:
                        throw new IOException("Encountered invalid line format in database file");
                }

                line = reader.readLine();
            }

            if (currentVendor != null) {
                // Is there a device left to be added to the previous vendor?
                if (currentDevice != null) {
                    ACCESS.addDevice(currentVendor, currentDevice);
                }
                vendorDb.put(currentVendor.getId(), currentVendor);
            }
            if (currentClass != null) {
                // Is there a subclass left to be added to the previous device class?
                if (currentSubclass != null) {
                    ACCESS.addSubclass(currentClass, currentSubclass);
                }
                // Add the previous device class to the return list
                deviceClassDb.put(currentClass.getId(), currentClass);
            }
        } catch (IOException | RuntimeException ex) {
            throw new IOException("Error while parsing database file", ex);
        }
    }

    /**
     * Verify syntax of the current line to be a vendor entry and parse contents into a
     * {@link Vendor} object.
     *
     * @param line Current line from database file
     * @return Vendor object filled with info from line
     * @throws IOException if line is malformed or otherwise unprocessable
     */
    Vendor parseVendorLine(String line) throws IOException {
        Matcher m = this.VENDOR_PATTERN.matcher(line);

        if (m.matches()) {
            int id = Integer.parseInt(m.group(1), 16);
            String name = m.group(2);

            Vendor rv = new Vendor(id, name, this.currentComment);
            this.currentComment = null;

            return rv;
        } else {
            throw new IOException("Unable to process vendor line: [" + line + "]");
        }
    }

    /**
     * Verify syntax of the current line to be a device entry and parse contents into a
     * {@link Device} object.
     *
     * @param line Current line from database file
     * @return Device object filled with info from line
     * @throws IOException if line is malformed or otherwise unprocessable
     */
    Device parseDeviceLine(String line) throws IOException {
        Matcher m = this.DEVICE_PATTERN.matcher(line);

        if (m.matches()) {
            int id = Integer.parseInt(m.group(1), 16);
            String name = m.group(2);

            Device rv = new Device(id, name, this.currentComment);
            this.currentComment = null;

            return rv;
        } else {
            throw new IOException("Unable to process device line: [" + line + "]");
        }
    }

    /**
     * Verify syntax of the current line to be a subsystem entry and parse contents into a
     * {@link Subsystem} object.
     *
     * @param line Current line from database file
     * @return Subsystem object filled with info from line
     * @throws IOException if line is malformed or otherwise unprocessable
     */
    Subsystem parseSubsystemLine(String line) throws IOException {
        Matcher m = this.SUBSYS_PATTERN.matcher(line);

        if (m.matches()) {
            int id = Integer.parseInt(m.group(2), 16);
            String name = m.group(3);
            int vendorId = Integer.parseInt(m.group(1), 16);

            Subsystem rv = new Subsystem(id, name, this.currentComment, vendorId);
            this.currentComment = null;

            return rv;
        } else {
            throw new IOException("Unable to process subsystem line: [" + line + "]");
        }
    }

    /**
     * Verify syntax of the current line to be a device class entry and parse contents into a
     * {@link DeviceClass} object.
     *
     * @param line Current line from database file
     * @return DeviceClass object filled with info from line
     * @throws IOException if line is malformed or otherwise unprocessable
     */
    DeviceClass parseDeviceClassLine(String line) throws IOException {
        Matcher m = this.DEVCLASS_PATTERN.matcher(line);

        if (m.matches()) {
            int id = Integer.parseInt(m.group(1), 16);
            String name = m.group(2);

            DeviceClass rv = new DeviceClass(id, name, this.currentComment);
            this.currentComment = null;

            return rv;
        } else {
            throw new IOException("Unable to process device class line: [" + line + "]");
        }
    }

    /**
     * Verify syntax of the current line to be a device subclass entry and parse contents into a
     * {@link DeviceSubclass} object.
     *
     * @param line Current line from database file
     * @return DeviceSubclass object filled with info from line
     * @throws IOException if line is malformed or otherwise unprocessable
     */
    DeviceSubclass parseDeviceSubclassLine(String line) throws IOException {
        Matcher m = this.SUBCLASS_PATTERN.matcher(line);

        if (m.matches()) {
            int id = Integer.parseInt(m.group(1), 16);
            String name = m.group(2);

            DeviceSubclass rv = new DeviceSubclass(id, name, this.currentComment);
            this.currentComment = null;

            return rv;
        } else {
            throw new IOException("Unable to process device subclass line: [" + line + "]");
        }
    }

    /**
     * Verify syntax of the current line to be a program interface entry and parse contents into a
     * {@link ProgramInterface} object.
     *
     * @param line Current line from database file
     * @return ProgramInterface object filled with info from line
     * @throws IOException if line is malformed or otherwise unprocessable
     */
    ProgramInterface parseProgramInterfaceLine(String line) throws IOException {
        Matcher m = this.PROGIFACE_PATTERN.matcher(line);

        if (m.matches()) {
            String id = m.group(1);
            String name = m.group(2);

            ProgramInterface rv = new ProgramInterface(Integer.parseInt(id, 16), name, this.currentComment);
            this.currentComment = null;

            return rv;
        } else {
            throw new IOException("Unable to process program interface line: [" + line + "]");
        }
    }

    /**
     * Determine the type of the current line. Possible return values are:
     * <p>
     * <ul>
     * <li>COMMENT: Starts with a <tt>#</tt> character</li>
     * <li>DEVICE: Starts with exactly one tabulator character and previous line was VENDOR, DEVICE,
     * or SUBSYSTEM</li>
     * <li>SUBSYSTEM: Starts with exactly two tabulator character and previous line was DEVICE or
     * SUBSYSTEM</li>
     * <li>VENDOR: Has no prefix</li>
     * <li>DEVICE_CLASS: Starts with a <tt>C</tt> character</li>
     * <li>DEVICE_SUBCLASS: Starts with exactly one tabulator character and previous line was
     * DEVICE_CLASS, DEVICE_SUBCLASS, or PROGRAM_INTERFACE</li>
     * <li>PROGRAM_INTERFACE: Starts with exactly two tabulator character and previous line was
     * DEVICE_SUBCLASS or PROGRAM_INTERFACE</li>
     * </ul>
     *
     * @param line     Current line from database file
     * @param previous Line type of previous line, is null at first call
     * @return Type of the current line determined by line prefix
     */
    LineType determineLineType(String line, LineType previous) {
        if (line.startsWith("#")) {
            return LineType.COMMENT;
        } else if (line.startsWith("C")) {
            return LineType.DEVICE_CLASS;
        } else if (line.startsWith("\t\t")) {
            if (previous == LineType.DEVICE || previous == LineType.SUBSYSTEM) {
                return LineType.SUBSYSTEM;
            } else if (previous == LineType.DEVICE_SUBCLASS || previous == LineType.PROGRAM_INTERFACE) {
                return LineType.PROGRAM_INTERFACE;
            } else {
                throw new IllegalArgumentException("Unexpected previous line type: " + previous);
            }
        } else if (line.startsWith("\t")) {
            if (previous == LineType.VENDOR || previous == LineType.DEVICE || previous == LineType.SUBSYSTEM) {
                return LineType.DEVICE;
            } else if (previous == LineType.DEVICE_CLASS || previous == LineType.DEVICE_SUBCLASS || previous == LineType.PROGRAM_INTERFACE) {
                return LineType.DEVICE_SUBCLASS;
            } else {
                throw new IllegalArgumentException("Unexpected previous line type: " + previous);
            }
        } else {
            return LineType.VENDOR;
        }
    }

    /**
     * Possible line types found in the PCI IDs database file.
     */
    enum LineType {
        COMMENT,
        DEVICE,
        DEVICE_CLASS,
        DEVICE_SUBCLASS,
        PROGRAM_INTERFACE,
        SUBSYSTEM,
        VENDOR;
    }
}
