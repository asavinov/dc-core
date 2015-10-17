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

public enum TableDefinitionType // Specific types of table formula
{
    FREE, // No definition for the table (and cannot be defined). Example: manually created table with primitive dimensions.
    PRODUCT, // Table contains all combinations of its greater (key) sets satisfying the constraints
    PROJECTION, // Table gets its elements from (unique) outputs of some function
    FILTER, // Tables contains a subset of elements from its super-set
    ANY, // Arbitrary formula without constraints can be provided with a mix of various expression types
}
