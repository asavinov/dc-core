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

import org.conceptoriented.dc.schema.*;

public class TableReader implements DcTableReader {

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
	public Object next() {
        if (rowid < table.getData().getLength()-1)
        {
            rowid++;
            return rowid;
        }
        else
        {
            return null;
        }
	}

    public TableReader(DcTable table)
    {
        this.table = table;
    }
}
