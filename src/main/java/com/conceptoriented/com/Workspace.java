package com.conceptoriented.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Workspace {

	public List<ComSchema> schemas;

    public ComSchema getSchema(String name)
    {
		Optional<ComSchema> ret = schemas.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny();
		return ret.isPresent() ? ret.get() : null;
    }

    public ComSchema mashup;

    public Workspace()
    {
        schemas = new ArrayList<ComSchema>();
    }

}
