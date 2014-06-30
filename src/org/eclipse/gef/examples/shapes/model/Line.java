package org.eclipse.gef.examples.shapes.model;

/**
 * An line shape.
 * 
 * @author oubeichen
 */
public class Line extends ModelElement {
	
	private static final long serialVersionUID = 1;

	private ShapesDiagram parent;

    public void setSource(ShapesDiagram parent) {
        this.parent = parent;
    }

    /**
     * @param parent
     */
    public Line(ShapesDiagram parent) {
        this.parent = parent;
        parent.addLine(this);
    }
    
    public String toString()
    {
    	return "Line " + hashCode();
    }

	public ShapesDiagram getParent() {
		// TODO Auto-generated method stub
		return parent;
	}

}
