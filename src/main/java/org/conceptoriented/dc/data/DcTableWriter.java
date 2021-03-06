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

import org.conceptoriented.dc.schema.DcColumn;

public interface DcTableWriter {
	public void open();
    public void close();

    public int find(ExprNode expr);
    public boolean canAppend(ExprNode expr);
    public int append(ExprNode expr);

    //
    // Tuple (flat record) methods: append, insert, remove, read, write.
    //

    int find(DcColumn[] dims, Object[] values);
    int append(DcColumn[] dims, Object[] values);
    void remove(int input);
}
