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

package com.conceptoriented.com;

interface ComEvaluator {
	public Workspace getWorkspace();
	public void setWorkspace(Workspace workspace);

	public boolean next(); // True if there exists a next element
    public boolean first(); // True if there exists a first element (if the set is not empty)
    public boolean last(); // True if there exists a last element (if the set is not empty)

    public Object evaluate(); // Compute output for the specified intput and write it

    public Object getResult();
}
