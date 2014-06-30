/*
 * Created on 2005-1-27
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.gef.examples.shapes.model.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.examples.shapes.model.ShapesDiagram;
import org.eclipse.gef.examples.shapes.model.Line;

/**
 * Delete a line
 * @author oubeichen
 */
public class LineDeleteCommand extends Command {

	public LineDeleteCommand(ShapesDiagram parent, Line line) {
		this.parent = parent;
		this.line = line;
	}
	
	ShapesDiagram parent;

    Line line;

    //Setters
    public void setLine(Line line) {
        this.line = line;
    }

    public void setParent(ShapesDiagram parent) {
        this.parent = parent;
    }

    @Override
	public void execute() {
        parent.removeLine(line);
        line.setSource(null);
    }

    @Override
	public String getLabel() {
        return "Delete Line";
    }

    @Override
	public void redo() {
        execute();
    }

    @Override
	public void undo() {
        line.setSource(parent);
        parent.addLine(line);
    }

}