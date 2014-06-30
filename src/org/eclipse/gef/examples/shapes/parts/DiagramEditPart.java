/*******************************************************************************
 * Copyright (c) 2004, 2005 Elias Volanakis and others.
?* All rights reserved. This program and the accompanying materials
?* are made available under the terms of the Eclipse Public License v1.0
?* which accompanies this distribution, and is available at
?* http://www.eclipse.org/legal/epl-v10.html
?*
?* Contributors:
?*????Elias Volanakis - initial API and implementation
?*******************************************************************************/
package org.eclipse.gef.examples.shapes.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gef.examples.shapes.model.EllipticalShape;
import org.eclipse.gef.examples.shapes.model.Line;
import org.eclipse.gef.examples.shapes.model.ModelElement;
import org.eclipse.gef.examples.shapes.model.RectangularShape;
import org.eclipse.gef.examples.shapes.model.Shape;
import org.eclipse.gef.examples.shapes.model.ShapesDiagram;
import org.eclipse.gef.examples.shapes.model.TriangularShape;
import org.eclipse.gef.examples.shapes.model.commands.LineCreateCommand;
import org.eclipse.gef.examples.shapes.model.commands.ShapeCreateCommand;
import org.eclipse.gef.examples.shapes.model.commands.ShapeSetConstraintCommand;

/**
 * EditPart for the a ShapesDiagram instance.
 * <p>
 * This edit part server as the main diagram container, the white area where
 * everything else is in. Also responsible for the container's layout (the way
 * the container rearanges is contents) and the container's capabilities (edit
 * policies).
 * </p>
 * <p>
 * This edit part must implement the PropertyChangeListener interface, so it can
 * be notified of property changes in the corresponding model element.
 * </p>
 * 
 * @author Elias Volanakis
 */
class DiagramEditPart extends AbstractGraphicalEditPart implements
		PropertyChangeListener, NodeEditPart {

	private XYAnchor sourceAnchor = null;
	
	private XYAnchor targetAnchor = null;

	/**
	 * Upon activation, attach to the model element as a property change
	 * listener.
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// disallows the removal of this edit part from its parent
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		// handles constraint changes (e.g. moving and/or resizing) of model
		// elements
		// and creation of new model elements
		installEditPolicy(EditPolicy.LAYOUT_ROLE,
				new ShapesXYLayoutEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
        	new GraphicalNodeEditPolicy() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
			 * getConnectionCompleteCommand
			 * (org.eclipse.gef.requests.CreateConnectionRequest)
			 */
			@Override
			protected Command getConnectionCompleteCommand(
					CreateConnectionRequest request) {
				LineCreateCommand cmd = (LineCreateCommand) request
						.getStartCommand();
				cmd.setParent((ShapesDiagram) getHost().getModel());
				return cmd;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy#
			 * getConnectionCreateCommand
			 * (org.eclipse.gef.requests.CreateConnectionRequest)
			 */
			@Override
			protected Command getConnectionCreateCommand(
					CreateConnectionRequest request) {
				LineCreateCommand cmd = new LineCreateCommand((ShapesDiagram) getHost().getModel());
				request.setStartCommand(cmd);
				return cmd;
			}

			@Override
			protected Command getReconnectTargetCommand(ReconnectRequest request) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected Command getReconnectSourceCommand(ReconnectRequest request) {
				// TODO Auto-generated method stub
				return null;
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());

		// Create the static router for the connection layer
		ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		connLayer.setConnectionRouter(new ShortestPathConnectionRouter(f));

		return f;
	}

	/**
	 * Upon deactivation, detach from the model element as a property change
	 * listener.
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((ModelElement) getModel()).removePropertyChangeListener(this);
		}
	}

	private ShapesDiagram getCastedModel() {
		return (ShapesDiagram) getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List getModelChildren() {
		return getCastedModel().getChildren(); // return a list of shapes
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		// these properties are fired when Shapes are added into or removed from
		// the ShapeDiagram instance and must cause a call of refreshChildren()
		// to update the diagram's contents.
		if (ShapesDiagram.CHILD_ADDED_PROP.equals(prop)
				|| ShapesDiagram.CHILD_REMOVED_PROP.equals(prop)) {
			refreshChildren();
		}
		if (ShapesDiagram.LINE_ADDED_PROP.equals(prop)
				|| ShapesDiagram.LINE_REMOVED_PROP.equals(prop)){
            refreshTargetConnections();
            refreshSourceConnections();
		}
	}

	/**
	 * EditPolicy for the Figure used by this edit part. Children of
	 * XYLayoutEditPolicy can be used in Figures with XYLayout.
	 * 
	 * @author Elias Volanakis
	 */
	private static class ShapesXYLayoutEditPolicy extends XYLayoutEditPolicy {

		/*
		 * (non-Javadoc)
		 * 
		 * @see ConstrainedLayoutEditPolicy#createChangeConstraintCommand(
		 * ChangeBoundsRequest, EditPart, Object)
		 */
		@Override
		protected Command createChangeConstraintCommand(
				ChangeBoundsRequest request, EditPart child, Object constraint) {
			if (child instanceof ShapeEditPart
					&& constraint instanceof Rectangle) {
				// return a command that can move and/or resize a Shape
				return new ShapeSetConstraintCommand((Shape) child.getModel(),
						request, (Rectangle) constraint);
			}
			return super.createChangeConstraintCommand(request, child,
					constraint);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * ConstrainedLayoutEditPolicy#createChangeConstraintCommand(EditPart,
		 * Object)
		 */
		@Override
		protected Command createChangeConstraintCommand(EditPart child,
				Object constraint) {
			// not used in this example
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see LayoutEditPolicy#getCreateCommand(CreateRequest)
		 */
		@Override
		protected Command getCreateCommand(CreateRequest request) {
			Object childClass = request.getNewObjectType();
			if (childClass == EllipticalShape.class
					|| childClass == RectangularShape.class
					|| childClass == TriangularShape.class) {
				// return a command that can add a Shape to a ShapesDiagram
				return new ShapeCreateCommand((Shape) request.getNewObject(),
						(ShapesDiagram) getHost().getModel(),
						(Rectangle) getConstraintFor(request));
			}
			if (childClass == Line.class){
				return new LineCreateCommand((ShapesDiagram)getHost().getModel());
			}
			return null;
		}

	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart arg0) {
		return sourceAnchor;
	}
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request req) {
		if(req instanceof CreateConnectionRequest)
			sourceAnchor = new XYAnchor(((CreateConnectionRequest) req).getLocation());
		else if(req instanceof ReconnectRequest)
			sourceAnchor = new XYAnchor(((ReconnectRequest) req).getLocation());
		return sourceAnchor;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart arg0) {
		// TODO Auto-generated method stub
		return targetAnchor;
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request req) {
		// TODO Auto-generated method stub
		if(req instanceof CreateConnectionRequest)
			targetAnchor = new XYAnchor(((CreateConnectionRequest) req).getLocation());
		else if(req instanceof ReconnectRequest)
			targetAnchor = new XYAnchor(((ReconnectRequest) req).getLocation());
		return targetAnchor;
	}

	@Override
	protected List getModelSourceConnections(){
		return ((ShapesDiagram) this.getModel()).getLines();
	}
	
	@Override
	protected List getModelTargetConnections(){
		return ((ShapesDiagram) this.getModel()).getLines();
	}
}