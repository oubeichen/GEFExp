package com.oubeichen.gefexp.model;

import org.eclipse.swt.graphics.Image;

/**
 * An triangle shape.
 * 
 * @author oubeichen
 */
public class TriangularShape extends Shape {

	/** A 16x16 pictogram of an triangular shape. */
	private static final Image TRIANGLE_ICON = createImage("icons/triangle16.gif");

	private static final long serialVersionUID = 1;

	@Override
	public Image getIcon() {
		return TRIANGLE_ICON;
	}

	@Override
	public String toString() {
		return "Triangle " + hashCode();
	}
}
