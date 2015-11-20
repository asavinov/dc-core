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

package org.conceptoriented.dc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.conceptoriented.dc.schema.*;

public class TableWriter implements DcTableWriter {

    DcTable table;
    int rowid = -1;

    @Override
	public void open() {
    	rowid = -1;
	}

	@Override
	public void close() {
		rowid = table.getData().getLength();
	}

    @Override
    public int find(ExprNode expr) {
        int[] result = java.util.stream.IntStream.range(0, table.getData().getLength()).toArray(); // All elements of this set (can be quite long)

        boolean hasBeenRestricted = false; // For the case where the Length==1, and no key columns are really provided, so we get at the end result.Length==1 which is misleading. Also, this fixes the problem of having no key dimensions.

        List<DcColumn> dims = new ArrayList<DcColumn>();
        dims.addAll(table.getColumns().stream().filter(x -> x.isKey()).collect(Collectors.toList()));
        dims.addAll(table.getColumns().stream().filter(x -> !x.isKey()).collect(Collectors.toList()));

        for (DcColumn dim : dims) // OPTIMIZE: the order of dimensions matters (use statistics, first dimensins with better filtering). Also, first identity dimensions.
        {
            ExprNode childExpr = expr.getChild(dim.getName());
            if (childExpr != null)
            {
                Object val = null;
                val = childExpr.getOutputVariable().getValue();

                hasBeenRestricted = true;
                int[] range = dim.getData().deproject(val); // Deproject the value
                result = Utils.intersect(result, range); // Intersect with previous de-projections
                // OPTIMIZE: Write our own implementation for intersection and other operations. Assume that they are ordered.
                // OPTIMIZE: Remember the position for the case this value will have to be inserted so we do not have again search for this positin during insertion (optimization)

                if (result.length == 0) break; // Not found
            }
        }

        if (result.length == 0) // Not found
        {
            return -1;
        }
        else if (result.length == 1) // Found single element - return its offset
        {
            if (hasBeenRestricted) return result[0];
            else return -result.length;
        }
        else // Many elements satisfy these properties (non-unique identities). Use other methods for getting these records (like de-projection)
        {
            return -result.length;
        }
    }

    @Override
    public boolean canAppend(ExprNode expr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int append(ExprNode expr) {

        for (DcColumn dim : table.getColumns()) // We must append one value to ALL greater dimensions (possibly null)
        {
            ExprNode childExpr = expr.getChild(dim.getName()); // TODO: replace by accessor by dimension reference (has to be resolved in the tuple)
            Object val = null;
            if (childExpr != null) // A tuple contains a subset of all dimensions
            {
                val = childExpr.getOutputVariable().getValue();
            }
            dim.getData().append(val);
        }

        table.getData().setLength(table.getData().getLength() + 1);
        return table.getData().getLength() - 1;
    }

    // Tuple methods

    @Override
    public int find(DcColumn[] dims, Object[] values) {
        int[] result = java.util.stream.IntStream.range(0, table.getData().getLength()).toArray(); // All elements of this set (can be quite long)

        boolean hasBeenRestricted = false; // For the case where the Length==1, and no key columns are really provided, so we get at the end result.Length==1 which is misleading. Also, this fixes the problem of having no key dimensions.
        for (int i = 0; i < dims.length; i++)
        {
            hasBeenRestricted = true;
            int[] range = dims[i].getData().deproject(values[i]); // Deproject one value
            result = Utils.intersect(result, range);
            // OPTIMIZE: Write our own implementation for various operations (intersection etc.). Use the fact that they are ordered.
            // OPTIMIZE: Use statistics for column distribution to choose best order of de-projections. Alternatively, the order of dimensions can be set by the external procedure taking into account statistics. Say, there could be a special utility method like SortDimensionsAccordingDiscriminationFactor or SortDimsForFinding tuples.
            // OPTIMIZE: Remember the position for the case this value will have to be inserted so we do not have again search for this positin during insertion. Maybe store it in a static field as part of last operation.

            if (result.length == 0) break; // Not found
        }

        if (result.length == 0) // Not found
        {
            return -1;
        }
        else if (result.length == 1) // Found single element - return its offset
        {
            if (hasBeenRestricted) return result[0];
            else return -result.length;
        }
        else // Many elements satisfy these properties (non-unique identities). Use other methods for getting these records (like de-projection)
        {
            return -result.length;
        }
    }
    @Override
    public int append(DcColumn[] dims, Object[] values) {
        for (int i = 0; i < dims.length; i++)
        {
            dims[i].getData().append(values[i]);
        }

        table.getData().setLength(table.getData().getLength() + 1);
        return table.getData().getLength() - 1;
    }
    @Override
    public void remove(int input) {
        for (DcColumn col : table.getColumns())
        {
            col.getData().remove(input);
        }

        table.getData().setLength(table.getData().getLength() - 1);
    }

    public TableWriter(DcTable table)
    {
        this.table = table;
    }
}
