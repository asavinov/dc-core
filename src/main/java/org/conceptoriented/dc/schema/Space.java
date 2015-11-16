 /*
 * Copyright 2013-2015 Alexandr Savinov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.conceptoriented.dc.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.conceptoriented.dc.schema.*;

public class Space implements DcSpace {

    protected List<DcSchema> schemas;
    @Override
    public List<DcSchema> getSchemas() {
        return schemas;
    }
    @Override
    public void addSchema(DcSchema schema) {
        schemas.add(schema);
    }
    @Override
    public void removeSchema(DcSchema schema) {
        // We have to ensure that inter-schema (import/export) columns are also deleted
        List<DcTable> allTables = schema.getAllSubTables();
        for (DcTable t : allTables)
        {
            if (t.isPrimitive()) continue;
            schema.deleteTable(t);
        }

        schemas.remove(schema);
    }

    @Override
    public DcSchema getSchema(String name) {
        Optional<DcSchema> ret = schemas.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny();
        return ret.isPresent() ? ret.get() : null;
    }

    protected DcSchema mashup;
    @Override
    public DcSchema getMashup() {
        return mashup;
    }
    @Override
    public void setMashup(DcSchema mashup) {
        this.mashup = mashup;
    }

    public Space() {
        schemas = new ArrayList<DcSchema>();
    }
}
