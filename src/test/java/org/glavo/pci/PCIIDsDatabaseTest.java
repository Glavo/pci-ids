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

import org.glavo.pci.model.Device;
import org.glavo.pci.model.Vendor;
import org.junit.jupiter.api.Test;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Glavo
 */
public final class PCIIDsDatabaseTest {

    private static final int LOONGSON_ID = 0x0014;
    private static final int NVIDIA_ID = 0x10de;

    PCIIDsDatabase database;

    {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new XZInputStream(this.getClass().getResourceAsStream("pci.ids.xz")), StandardCharsets.UTF_8))) {
            database = PCIIDsDatabase.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void testFindAllVendors() {
        List<Vendor> allVendors = database.findAllVendors();

        List<Vendor> loongson = allVendors.stream().filter(it -> it.getId() == LOONGSON_ID).collect(Collectors.toList());
        assertEquals(1, loongson.size());
        assertEquals("Loongson Technology LLC", loongson.get(0).getName());
    }

    @Test
    public void testFindVendor() throws Exception {
        Vendor loongson = database.findVendor(LOONGSON_ID);
        assertEquals("Loongson Technology LLC", loongson.getName());

        Vendor nvidia = database.findVendor(NVIDIA_ID);
        assertEquals("NVIDIA Corporation", nvidia.getName());

        assertNull(database.findVendor(0x0005));
        assertNull(database.findVendor(0xffff + 1));
        assertNull(database.findVendor(-1));
    }

    @Test
    public void testFindAllDevices() throws Exception {
        List<Device> loongsonDevices = database.findAllDevices(LOONGSON_ID);
        List<Device> pch = loongsonDevices.stream().filter(device -> device.getName().equals("7A2000 PCH I2S Controller")).collect(Collectors.toList());
        assertEquals(1, pch.size());
        assertEquals(0x7a27, pch.get(0).getId());
    }

    @Test
    public void testFindDevice() throws Exception {
        assertEquals("7A2000 PCH I2S Controller", database.findDevice(LOONGSON_ID, 0x7a27).getName());
    }
}
