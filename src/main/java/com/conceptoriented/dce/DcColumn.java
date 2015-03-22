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

package com.conceptoriented.dce;


/**
 * Describes one column.
 *
 * @author savinov
 *
 */
public interface DcColumn {

    public String getName();
    public void setName(String name);

    //
    // Properties
    //
    public boolean isKey();
    void setKey(boolean isKey);

    public boolean isSuper();
    void setSuper(boolean isSuper);

    public boolean isPrimitive();

    //
    // Input and output
    //
    public DcTable getInput();
    public void setInput(DcTable input);

    public DcTable getOutput();
    public void setOutput(DcTable output);

    public void add();
    public void remove();

    //
    // Data and definition objects
    //
    public DcColumnData getData();
    public DcColumnDefinition getDefinition();
}

