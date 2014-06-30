/*
 * Created on 2005-1-25
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.gef.examples.shapes.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.examples.shapes.model.Line;
import org.eclipse.gef.examples.shapes.model.ShapesDiagram;

/**
 * Create a Line
 * @author oubeichen
 */
public class LineCreateCommand extends Command {

    public LineCreateCommand(ShapesDiagram parent) {
		this.parent = parent;
	}

	protected Line line;

    protected ShapesDiagram parent;

    public void setParent(ShapesDiagram parent) {
        this.parent = parent;
    }

    public void setLine(Line Line) {
        this.line = Line;
    }

    //------------------------------------------------------------------------
    // Overridden from Command

    @Override
	public void execute() {
        line = new Line(parent);
    }

    @Override
	public String getLabel() {
        return "Create Line";
    }

    @Override
	public void redo() {
        this.parent.addLine(this.line);
    }

    @Override
	public void undo() {
        this.parent.removeLine(this.line);
    }

    @Override
	public boolean canExecute() {

        return true;
    }
}