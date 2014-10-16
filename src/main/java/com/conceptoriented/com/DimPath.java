package com.conceptoriented.com;

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
