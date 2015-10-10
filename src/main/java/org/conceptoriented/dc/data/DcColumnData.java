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

/**
 * Storage methods for working with function data like reading and writing function output values for the specified inputs.
 *
 * @author savinov
 *
 */
public interface DcColumnData {

    public int getLength();
    public void setLength(int length);

    public boolean isAutoIndex();
    public void setAutoIndex(boolean value);

    public boolean isIndexed();

    public void reindex();

    //
    // Untyped methods. Default conversion will be done according to the function type.
    //
    public boolean isNull(int input);

    public Object getValue(int input);
    public void setValue(int input, Object value);
    public void setValue(Object value);

    public void nullify();

    public void append(Object value);

    public void insert(int input, Object value);

    public void remove(int input);

    //
    // Project/de-project
    //

    Object project(int[] offsets);
    int[] deproject(Object value);

}