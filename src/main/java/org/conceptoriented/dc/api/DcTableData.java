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

package org.conceptoriented.dc.api;

import org.conceptoriented.dc.ExprNode;

/**
 * Working with data in the table.
 *
 * @author savinov
 *
 */
public interface DcTableData {

    public int getLength();
    public void setLength(int length);

    public void setAutoIndex(boolean value);

    public boolean isIndexed();

    public void reindex();

    //
    // Value methods (convenience, probably should be removed and replaced by manual access to dimensions)
    //

    Object getValue(String name, int offset);
    void setValue(String name, int offset, Object value);

    //
    // Tuple (flat record) methods: append, insert, remove, read, write.
    //

    int find(DcColumn[] dims, Object[] values);
    int append(DcColumn[] dims, Object[] values);
    void remove(int input);

    //
    // Expression (nested record) methods: append, insert, remove, read, write.
    //

    int find(ExprNode expr);
    boolean canAppend(ExprNode expr);
    int append(ExprNode expr);
}
