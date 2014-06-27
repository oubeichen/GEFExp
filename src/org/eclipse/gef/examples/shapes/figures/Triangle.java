package org.eclipse.gef.examples.shapes.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

/**
 * A triangle that uses all of its bounds to draw an isosceles triangle in the
 * figure's bounds, like this:
 * 
 * ______ | /\ | | / \ | (bounds shown as surrounding rectangle). |/____\|
 * 
 * The implementation is based on the {@link org.eclipse.draw2d.Triangle}
 * implementation.
 * 
 * @author vainolo
 *
 */
public final class Triangle extends Shape {
	/** The points of the triangle. */
	protected PointList points = new PointList(3);

	public PointList getPoints() {
		return points;
	}

	public void setPoints(PointList points) {
		this.points = points;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void primTranslate(int dx, int dy) {
		super.primTranslate(dx, dy);
		points.translate(dx, dy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.drawPolygon(points);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void fillShape(Graphics graphics) {
		graphics.fillPolygon(points);
	}

	/**
	 * Validates the figure, drawing a vertical isosceles triangle filling the
	 * figure's bounds.
	 */
	@Override
	public void validate() {
		super.validate();
		bounds = getBounds().getCopy();
		Point top = new Point(bounds.x + bounds.width / 2, bounds.y);
		Point left = new Point(bounds.x, bounds.y + bounds.height);
		Point right = new Point(bounds.x + bounds.width, bounds.y
				+ bounds.height);
		points.removeAllPoints();
		points.addPoint(top);
		points.addPoint(left);
		points.addPoint(right);
	}
}
