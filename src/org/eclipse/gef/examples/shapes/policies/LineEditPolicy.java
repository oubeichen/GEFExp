/*
 * Created on 2005-1-27
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.gef.examples.shapes.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.examples.shapes.model.Line;
import org.eclipse.gef.examples.shapes.model.commands.LineDeleteCommand;
import org.eclipse.gef.requests.GroupRequest;

/**
 * @author zhanghao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LineEditPolicy extends ComponentEditPolicy{

    @Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
        Line line=(Line)getHost().getModel();
        LineDeleteCommand cmd=new LineDeleteCommand(line.getParent(), line);
        cmd.setLine(line);
        return cmd;
    }
}
