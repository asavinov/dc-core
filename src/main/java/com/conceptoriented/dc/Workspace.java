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

package com.conceptoriented.dc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Workspace {

    public List<DcSchema> schemas;

    public DcSchema getSchema(String name)
    {
        Optional<DcSchema> ret = schemas.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny();
        return ret.isPresent() ? ret.get() : null;
    }

    public DcSchema mashup;

    public Workspace()
    {
        schemas = new ArrayList<DcSchema>();
    }

}
