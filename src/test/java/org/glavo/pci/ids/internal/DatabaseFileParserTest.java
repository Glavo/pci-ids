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

import org.glavo.pci.ids.model.Device;
import org.glavo.pci.ids.model.DeviceClass;
import org.glavo.pci.ids.model.DeviceSubclass;
import org.glavo.pci.ids.model.ProgramInterface;
import org.glavo.pci.ids.model.Subsystem;
import org.glavo.pci.ids.model.Vendor;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.tukaani.xz.XZInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Rix (thomasrix@exodus-project.net)
 */
public class DatabaseFileParserTest {

    @Test
    public void testParseSimpleDatabaseFile() throws Exception {
        final Map<Integer, Vendor> vendorDatabase = new TreeMap<>();
        final Map<Integer, DeviceClass> deviceClassDatabase = new TreeMap<>();

        DatabaseFileParser instance = new DatabaseFileParser();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(this.getClass().getResourceAsStream("../simple.pci.ids"), StandardCharsets.UTF_8))) {
            instance.parseDatabaseFile(reader, vendorDatabase, deviceClassDatabase);
        }

        // Vendor database
        assertEquals(1, vendorDatabase.size());

        Vendor v = vendorDatabase.get(0x001c);
        assertNotNull(v);
        assertEquals(0x001c, v.getId());
        assertEquals("PEAK-System Technik GmbH", v.getName());
        assertEquals("Comment on vendor", v.getComment());

        Device d = v.getDevices().get(0x0001);
        assertNotNull(d);
        assertEquals(0x0001, d.getId());
        assertEquals("PCAN-PCI CAN-Bus controller", d.getName());
        assertEquals("Comment on device", d.getComment());

        Subsystem s1 = d.getSubsystems().stream()
                .filter(s -> s.getVendorId() == 0x001c && s.getId() == 0x0004)
                .findFirst()
                .get();

        assertEquals(0x0004, s1.getId());
        assertEquals(0x001c, s1.getVendorId());
        assertEquals("2 Channel CAN Bus SJC1000", s1.getName());
        assertEquals("Comment on subsystem", s1.getComment());

        Subsystem s2 = d.getSubsystems().stream()
                .filter(s -> s.getVendorId() == 0x001c && s.getId() == 0x0005)
                .findFirst()
                .get();

        assertEquals(0x0005, s2.getId());
        assertEquals(0x001c, s2.getVendorId());
        assertEquals("2 Channel CAN Bus SJC1000 MOCK", s2.getName());
        assertNull(s2.getComment());

        // Device classes database
        assertEquals(1, deviceClassDatabase.size());

        DeviceClass dc = deviceClassDatabase.get(0x01);
        assertNotNull(dc);
        assertEquals(0x01, dc.getId());
        assertEquals("Mass storage controller", dc.getName());
        assertEquals("Comment on device class", dc.getComment());

        DeviceSubclass dsc = dc.getSubclasses().get(0x05);
        assertNotNull(dsc);
        assertEquals(0x05, dsc.getId());
        assertEquals("ATA controller", dsc.getName());
        assertEquals("Comment on device subclass", dsc.getComment());

        ProgramInterface pi1 = dsc.getProgramInterfaces().get(0x20);
        assertNotNull(pi1);
        assertEquals(0x20, pi1.getId());
        assertEquals("ADMA single stepping", pi1.getName());
        assertEquals("Comment on program interface", pi1.getComment());

        ProgramInterface pi2 = dsc.getProgramInterfaces().get(0x30);
        assertNotNull(pi2);
        assertEquals(0x30, pi2.getId());
        assertEquals("ADMA continuous operation", pi2.getName());
        assertNull(pi2.getComment());
    }

    @Test
    public void testParseRealWorldDatabaseFile() throws Exception {
        final Map<Integer, Vendor> vendorDatabase = new TreeMap<>();
        final Map<Integer, DeviceClass> deviceClassDatabase = new TreeMap<>();

        DatabaseFileParser instance = new DatabaseFileParser();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new XZInputStream(this.getClass().getResourceAsStream("../pci.ids.xz")), StandardCharsets.UTF_8))) {
            instance.parseDatabaseFile(reader, vendorDatabase, deviceClassDatabase);
        }

        // TODO
    }

    //
    // PARSE LINE: VENDOR
    //
    @Test
    public void testParseVendorLine() throws Exception {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        Vendor result = instance.parseVendorLine(line);

        assertEquals(0x0059, result.getId());
        assertEquals("Tiger Jet Network Inc. (Wrong ID)", result.getName());
    }

    @Test
    public void testParseVendorLine_malformedIdShort() {
        String line = "005  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseVendorLine(line));
    }

    @Test
    public void testParseVendorLine_malformedIdLong() {
        String line = "00590  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseVendorLine(line));
    }

    @Test
    public void testParseVendorLine_malformedPrefix() {
        String line = "\t0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseVendorLine(line));
    }

    //
    // PARSE LINE: DEVICE
    //
    @Test
    public void testParseDeviceLine() throws Exception {
        String line = "\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        Device result = instance.parseDeviceLine(line);

        assertEquals(0x7801, result.getId());
        assertEquals("WinTV HVR-1800 MCE", result.getName());
    }

    @Test
    public void testParseDeviceLine_malformedIdShort() {
        String line = "\t780  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceLine(line));
    }

    @Test
    public void testParseDeviceLine_malformedIdLong() {
        String line = "\t78010 WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceLine(line));
    }

    @Test
    public void testParseDeviceLine_malformedPrefix1() {
        String line = "\t\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceLine(line));
    }

    @Test
    public void testParseDeviceLine_malformedPrefix2() {
        String line = "7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceLine(line));
    }

    //
    // PARSE LINE: SUBSYSTEM
    //
    @Test
    public void testParseSubsystemLine() throws Exception {
        String line = "\t\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        Subsystem result = instance.parseSubsystemLine(line);

        assertEquals(0x001c, result.getVendorId());
        assertEquals(0x0004, result.getId());
        assertEquals("2 Channel CAN Bus SJC1000", result.getName());
    }

    @Test
    public void testParseSubsystemLine_malformedIdShort() {
        String line = "\t\t001c 000  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseSubsystemLine(line));
    }

    @Test
    public void testParseSubsystemLine_malformedIdLong() {
        String line = "\t\t001c 00040  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseSubsystemLine(line));
    }

    @Test
    public void testParseSubsystemLine_malformedVendorIdShort() {
        String line = "\t\t001 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseSubsystemLine(line));
    }

    @Test
    public void testParseSubsystemLine_malformedVendorIdLong() {
        String line = "\t\t001c0 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseSubsystemLine(line));
    }

    @Test
    public void testParseSubsystemLine_malformedPrefix1() {
        String line = "\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseSubsystemLine(line));
    }

    @Test
    public void testParseSubsystemLine_malformedPrefix2() {
        String line = "001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseSubsystemLine(line));
    }

    //
    // PARSE LINE: DEVICE_CLASS
    //
    @Test
    public void testParseDeviceClassLine() throws Exception {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DeviceClass result = instance.parseDeviceClassLine(line);

        assertEquals(0x00, result.getId());
        assertEquals("Unclassified device", result.getName());
        assertNull(result.getComment());
    }

    @Test
    public void testParseDeviceClassLine_malformedIdShort() {
        String line = "C 0  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceClassLine(line));
    }

    @Test
    public void testParseDeviceClassLine_malformedIdLong() {
        String line = "C 000  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceClassLine(line));
    }

    @Test
    public void testParseDeviceClassLine_malformedPrefix1() {
        String line = "\tC 000  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceClassLine(line));
    }

    @Test
    public void testParseDeviceClassLine_malformedPrefix2() {
        String line = "000  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceClassLine(line));
    }

    //
    // PARSE LINE: DEVICE_SUBCLASS
    //
    @Test
    public void testParseDeviceSubclassLine() throws Exception {
        String line = "\t00  Non-VGA unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DeviceSubclass result = instance.parseDeviceSubclassLine(line);

        assertEquals(0x00, result.getId());
        assertEquals("Non-VGA unclassified device", result.getName());
        assertNull(result.getComment());
    }

    @Test
    public void testParseDeviceSubclassLine_malformedIdShort() {
        String line = "\t0  Non-VGA unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceSubclassLine(line));
    }

    @Test
    public void testParseDeviceSubclassLine_malformedIdLong() {
        String line = "\t000  Non-VGA unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceSubclassLine(line));
    }

    @Test
    public void testParseDeviceSubclassLine_malformedPrefix1() {
        String line = "00  Non-VGA unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceSubclassLine(line));
    }

    @Test
    public void testParseDeviceSubclassLine_malformedPrefix2() {
        String line = "\t\t00  Non-VGA unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseDeviceSubclassLine(line));
    }

    //
    // PARSE LINE: PROGRAM_INTERFACE
    //
    @Test
    public void testParseProgramInterfaceLine() throws Exception {
        String line = "\t\t20  ADMA single stepping";

        DatabaseFileParser instance = new DatabaseFileParser();
        ProgramInterface result = instance.parseProgramInterfaceLine(line);

        assertEquals(0x20, result.getId());
        assertEquals("ADMA single stepping", result.getName());
        assertNull(result.getComment());
    }

    @Test
    public void testParseProgramInterfaceLine_malformedIdShort() throws Exception {
        String line = "\t\t2  ADMA single stepping";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseProgramInterfaceLine(line));
    }

    @Test
    public void testParseProgramInterfaceLine_malformedIdLong() throws Exception {
        String line = "\t\t200  ADMA single stepping";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseProgramInterfaceLine(line));
    }

    @Test
    public void testParseProgramInterfaceLine_malformedPrefix1() throws Exception {
        String line = "\t20  ADMA single stepping";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseProgramInterfaceLine(line));
    }

    @Test
    public void testParseProgramInterfaceLine_malformedPrefix2() throws Exception {
        String line = "20  ADMA single stepping";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IOException.class, () -> instance.parseProgramInterfaceLine(line));
    }

    //
    // LINE TYPE: COMMENT
    //
    @Test
    public void testDetermineLineType_COMMENT_prevNull() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, null);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    @Test
    public void testDetermineLineType_COMMENT_prevCOMMENT() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.COMMENT);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    @Test
    public void testDetermineLineType_COMMENT_prevDEVICE() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    @Test
    public void testDetermineLineType_COMMENT_prevDEVICE_CLASS() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_CLASS);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    @Test
    public void testDetermineLineType_COMMENT_prevDEVICE_SUBCLASS() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_SUBCLASS);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    @Test
    public void testDetermineLineType_COMMENT_prevPROGRAM_INTERFACE() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.PROGRAM_INTERFACE);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    @Test
    public void testDetermineLineType_COMMENT_prevSUBSYSTEM() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.SUBSYSTEM);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    @Test
    public void testDetermineLineType_COMMENT_prevVENDOR() {
        String line = "# This is a comment";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.VENDOR);

        assertEquals(DatabaseFileParser.LineType.COMMENT, result);
    }

    //
    // LINE TYPE: VENDOR
    //
    @Test
    public void testDetermineLineType_VENDOR_prevNull() {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, null);

        assertEquals(DatabaseFileParser.LineType.VENDOR, result);
    }

    @Test
    public void testDetermineLineType_VENDOR_prevDEVICE() {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE);

        assertEquals(DatabaseFileParser.LineType.VENDOR, result);
    }

    @Test
    public void testDetermineLineType_VENDOR_prevDEVICE_CLASS() {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_CLASS);

        assertEquals(DatabaseFileParser.LineType.VENDOR, result);
    }

    @Test
    public void testDetermineLineType_VENDOR_prevDEVICE_SUBCLASS() {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_SUBCLASS);

        assertEquals(DatabaseFileParser.LineType.VENDOR, result);
    }

    @Test
    public void testDetermineLineType_VENDOR_prevPROGRAM_INTERFACE() {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.PROGRAM_INTERFACE);

        assertEquals(DatabaseFileParser.LineType.VENDOR, result);
    }

    @Test
    public void testDetermineLineType_VENDOR_prevSUBSYSTEM() {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.SUBSYSTEM);

        assertEquals(DatabaseFileParser.LineType.VENDOR, result);
    }

    @Test
    public void testDetermineLineType_VENDOR_prevVENDOR() {
        String line = "0059  Tiger Jet Network Inc. (Wrong ID)";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.VENDOR);

        assertEquals(DatabaseFileParser.LineType.VENDOR, result);
    }

    //
    // LINE TYPE: DEVICE_CLASS
    //
    @Test
    public void testDetermineLineType_CLASS_prevNull() {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, null);

        assertEquals(DatabaseFileParser.LineType.DEVICE_CLASS, result);
    }

    @Test
    public void testDetermineLineType_CLASS_prevDEVICE() {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE);

        assertEquals(DatabaseFileParser.LineType.DEVICE_CLASS, result);
    }

    @Test
    public void testDetermineLineType_CLASS_prevDEVICE_CLASS() {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_CLASS);

        assertEquals(DatabaseFileParser.LineType.DEVICE_CLASS, result);
    }

    @Test
    public void testDetermineLineType_CLASS_prevDEVICE_SUBCLASS() {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_SUBCLASS);

        assertEquals(DatabaseFileParser.LineType.DEVICE_CLASS, result);
    }

    @Test
    public void testDetermineLineType_CLASS_prevPROGRAM_INTERFACE() {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.PROGRAM_INTERFACE);

        assertEquals(DatabaseFileParser.LineType.DEVICE_CLASS, result);
    }

    @Test
    public void testDetermineLineType_CLASS_prevSUBSYSTEM() {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.SUBSYSTEM);

        assertEquals(DatabaseFileParser.LineType.DEVICE_CLASS, result);
    }

    @Test
    public void testDetermineLineType_CLASS_prevVENDOR() {
        String line = "C 00  Unclassified device";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.VENDOR);

        assertEquals(DatabaseFileParser.LineType.DEVICE_CLASS, result);
    }

    //
    // LINE TYPE: ONE TAB
    //
    @Test
    public void testDetermineLineType_ONETAB_prevDEVICE() {
        String line = "\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE);

        assertEquals(DatabaseFileParser.LineType.DEVICE, result);
    }

    @Test
    public void testDetermineLineType_ONETAB_prevSUBSYSTEM() {
        String line = "\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.SUBSYSTEM);

        assertEquals(DatabaseFileParser.LineType.DEVICE, result);
    }

    @Test
    public void testDetermineLineType_ONETAB_prevVENDOR() {
        String line = "\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.VENDOR);

        assertEquals(DatabaseFileParser.LineType.DEVICE, result);
    }

    @Test
    public void testDetermineLineType_ONETAB_prevDEVICE_CLASS() {
        String line = "\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_CLASS);

        assertEquals(DatabaseFileParser.LineType.DEVICE_SUBCLASS, result);
    }

    @Test
    public void testDetermineLineType_ONETAB_prevDEVICE_SUBCLASS() {
        String line = "\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_SUBCLASS);

        assertEquals(DatabaseFileParser.LineType.DEVICE_SUBCLASS, result);
    }

    @Test
    public void testDetermineLineType_ONETAB_prevPROGRAM_INTERFACE() {
        String line = "\t7801  WinTV HVR-1800 MCE";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.PROGRAM_INTERFACE);

        assertEquals(DatabaseFileParser.LineType.DEVICE_SUBCLASS, result);
    }

    //
    // LINE TYPE: TWO TABS
    //
    @Test
    public void testDetermineLineType_TWOTABS_prevDEVICE() {
        String line = "\t\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE);

        assertEquals(DatabaseFileParser.LineType.SUBSYSTEM, result);
    }

    @Test
    public void testDetermineLineType_TWOTABS_prevSUBSYSTEM() {
        String line = "\t\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.SUBSYSTEM);

        assertEquals(DatabaseFileParser.LineType.SUBSYSTEM, result);
    }

    @Test
    public void testDetermineLineType_TWOTABS_prevDEVICE_CLASS() {
        String line = "\t\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        assertThrows(IllegalArgumentException.class, () -> instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_CLASS));
    }

    @Test
    public void testDetermineLineType_TWOTABS_prevDEVICE_SUBCLASS() {
        String line = "\t\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.DEVICE_SUBCLASS);

        assertEquals(DatabaseFileParser.LineType.PROGRAM_INTERFACE, result);
    }

    @Test
    public void testDetermineLineType_TWOTABS_prevPROGRAM_INTERFACE() {
        String line = "\t\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();
        DatabaseFileParser.LineType result = instance.determineLineType(line, DatabaseFileParser.LineType.PROGRAM_INTERFACE);

        assertEquals(DatabaseFileParser.LineType.PROGRAM_INTERFACE, result);
    }

    @Test
    public void testDetermineLineType_TWOTABS_prevVENDOR() {
        String line = "\t\t001c 0004  2 Channel CAN Bus SJC1000";

        DatabaseFileParser instance = new DatabaseFileParser();

        assertThrows(IllegalArgumentException.class, () -> instance.determineLineType(line, DatabaseFileParser.LineType.VENDOR));
    }
}