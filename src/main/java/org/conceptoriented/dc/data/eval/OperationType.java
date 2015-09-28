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

package org.conceptoriented.dc.data.eval;

public enum OperationType
{
    // - (VALUE): do we need this? could be modeled by tuples with no children or by type. A primitive value (literal) represented here by-value
    VALUE, // this node stores a value. no computations

    // - TUPLE/DOWN/ROW_OP: access to the named function input given a combination of outputs.
    //   - name is a function from a (single) parent node set (input) to this node set (output) - but computed in inverse direction from this node to the parent when processing the parent (this node processes children's functions)
    //   - node type = input type of any child function (surrogate), child type(s) = output(s) type of the child function
    //   - TUPLE always means moving down in poset by propagating greater surrogates to lesser surrogates
    TUPLE, // children are tuple attributes corresponding to dimensions. this node de-projects their values and find the input for this node set

    // Operation type. What is in this node and how to interpret the name
    // - ACCESS/UP/COLUMN_OP: access to named function (variable, arithmetic, system procedure) output given intput(s) in children
    //   - name is a function from several children (inputs) to this node set (output) - computed when processing this node
    //   - node type = output type (surrogate), child type(s) = input(s) type
    //   - it means moving up in the poset from lesser values to greater values
    CALL, // this node processes children and produces output for this node. children are named arguments. the first argument is 'method'
}
