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

public enum DcDataType
{
    // Built-in types in C#: http://msdn.microsoft.com/en-us/library/vstudio/ya5y69ds.aspx
    Void, // Null, Nothing, Empty no value. Can be equivalent to Top.
    Top, // Maybe equivalent to Void
    Bottom, // The most specific type but introduced formally. This guarantees that any set has a lesser set.
    Root, // It is surrogate or reference
    Integer,
    Double,
    Decimal,
    String,
    Boolean,
    DateTime,
    Set, // User-defined. It is any set that is not root (non-primititve type). Arbitrary user-defined name.
}
