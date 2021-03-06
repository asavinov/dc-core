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

package org.conceptoriented.dc.utils;

import java.util.ArrayList;
import java.util.List;

import org.conceptoriented.dc.schema.*;

public class ColumnPath extends Column {

    protected List<DcColumn> segments;
    public List<DcColumn> getSegments() {
        return segments;
    }
    public void setSegments(List<DcColumn> segments) {
        this.segments = segments;
    }






    public ColumnPath() {
        segments = new ArrayList<DcColumn>();
    }

    public ColumnPath(DcTable tab) {
        this();
        setInput(tab);
        setOutput(tab);
    }

    public ColumnPath(String name) {
        super(name);
        segments = new ArrayList<DcColumn>();
    }

    public ColumnPath(DcColumn seg) {
        this();

        if (seg == null) return;

        segments.add(seg);
        setInput(segments.get(0).getInput());
        setOutput(segments.get(segments.size() - 1).getOutput());
    }

    public ColumnPath(List<DcColumn> segs) {
        this();

        if(segs == null || segs.size() == 0) return;

        segments.addAll(segs);
        setInput(segments.get(0).getInput());
        setOutput(segments.get(segments.size() - 1).getOutput());
    }

    public ColumnPath(ColumnPath path) {
        super(path);
        segments = new ArrayList<DcColumn>();
        segments.addAll(path.getSegments());
    }

    public ColumnPath(String name, DcTable input, DcTable output) {
        super(name, input, output);
        segments = new ArrayList<DcColumn>();
    }

}
