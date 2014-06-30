/*
 * Created on 2005-1-25
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.gef.examples.shapes.parts;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.examples.shapes.policies.LineEditPolicy;

/**
 * @author zhanghao
 *
 */
public class LineEditPart extends AbstractConnectionEditPart {

    @Override
	protected IFigure createFigure() {
        PolylineConnection line = new PolylineConnection();
    //    line.setTargetDecoration(new PolygonDecoration());
        line.setConnectionRouter(new BendpointConnectionRouter());
       
        return line;
    }

    @Override
	protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new LineEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
    }

    @Override
	protected void refreshVisuals() {
    }

    @Override
	public void setSelected(int value) {
        super.setSelected(value);
        if (value != EditPart.SELECTED_NONE)
            ((PolylineConnection) getFigure()).setLineWidth(2);
        else
            ((PolylineConnection) getFigure()).setLineWidth(1);
    }
}