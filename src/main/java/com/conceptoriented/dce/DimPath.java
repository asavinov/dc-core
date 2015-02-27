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

import java.util.ArrayList;
import java.util.List;

public class DimPath extends Dim {

    protected List<ComColumn> segments;
    public List<ComColumn> getSegments() {
        return segments;
    }
    public void setSegments(List<ComColumn> segments) {
        this.segments = segments;
    }






    public DimPath() {
        segments = new ArrayList<ComColumn>();
    }

    public DimPath(ComTable set) {
        this();
        setInput(set);
        setOutput(set);
    }

    public DimPath(String name) {
        super(name);
        segments = new ArrayList<ComColumn>();
    }

    public DimPath(ComColumn seg) {
        this();

        if (seg == null) return;

        segments.add(seg);
        setInput(segments.get(0).getInput());
        setOutput(segments.get(segments.size() - 1).getOutput());
    }

    public DimPath(List<ComColumn> segs) {
        this();

        if(segs == null || segs.size() == 0) return;

        segments.addAll(segs);
        setInput(segments.get(0).getInput());
        setOutput(segments.get(segments.size() - 1).getOutput());
    }

    public DimPath(DimPath path) {
        super(path);
        segments = new ArrayList<ComColumn>();
        segments.addAll(path.getSegments());
    }

    public DimPath(String name, ComTable input, ComTable output) {
        super(name, input, output);
        segments = new ArrayList<ComColumn>();
    }

}
