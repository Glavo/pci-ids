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
 * Java representation of a PCI device subclass program interface. Each interface has an 8 Bit ID,
 * which is unique in the scope of its device subclass, represented by two hex-characters, and a
 * mandatory name. The comment field is optional.
 *
 * @author Thomas Rix (thomasrix@exodus-project.net)
 * @since 0.3
 */
public final class ProgramInterface implements Comparable<ProgramInterface> {

    /**
     * Unique 8 Bit ID.
     */
    private final int id;
    private final String name;
    private final String comment;

    /**
     * Create a new Progrmam Interface database entry.
     *
     * @param id      Unique 8 Bit ID
     * @param name    Full name of the program interface
     * @param comment Optional comment, may be null
     */
    public ProgramInterface(int id, String name, String comment) {
        ArgumentValidator.requireUnsignedByte(id, "Program interface ID");
        ArgumentValidator.requireNonBlank(name, "Program interface name");

        this.id = id;
        this.name = name;
        this.comment = comment;
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
     * Compare this object to another {@link ProgramInterface} object. Comparison will take place on
     * the integer representation of the unique ID.
     *
     * @param t Other object
     * @return Result of comparison
     */
    @Override
    public int compareTo(ProgramInterface t) {
        return Integer.compare(this.id, t.id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProgramInterface)) return false;
        ProgramInterface that = (ProgramInterface) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public String toString() {
        return String.format("ProgramInterface[id=%02x, name='%s']", this.getId(), this.getName());
    }
}
