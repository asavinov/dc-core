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

public enum ColumnDefinitionType // Specific types of column formula
{
    FREE, // No definition for the column (and cannot be defined). Example: key columns of a product table
    ANY, // Arbitrary formula without constraints which can mix many other types of expressions
    ARITHMETIC, // Column uses only other columns or paths of this same table as well as operations
    LINK, // Column is defined via a mapping represented as a tuple with paths as leaves
    AGGREGATION, // Column is defined via an updater (accumulator) function which is fed by facts using grouping and measure paths
    CASE,
}

