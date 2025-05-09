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

import org.glavo.pci.ids.model.Device;
import org.glavo.pci.ids.model.DeviceClass;
import org.glavo.pci.ids.model.DeviceSubclass;
import org.glavo.pci.ids.model.ProgramInterface;
import org.glavo.pci.ids.model.Subsystem;
import org.glavo.pci.ids.model.Vendor;

public interface ModelAccess {
    void addDevice(Vendor vendor, Device device);

    void addSubsystem(Device device, Subsystem subsystem);

    void addSubclass(DeviceClass deviceClass, DeviceSubclass subclass);

    void addProgramInterface(DeviceSubclass subclass, ProgramInterface programInterface);

    final class Key {
        Key() {
        }
    }
}
