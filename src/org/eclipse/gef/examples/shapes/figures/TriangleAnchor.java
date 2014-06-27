package org.eclipse.gef.examples.shapes.figures;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
/**
 * @author xmind
 * modified by oubeichen
 */
public class TriangleAnchor extends ChopboxAnchor {
	public TriangleAnchor(IFigure iFigure) {  
        super(iFigure);  
    }  
      
    public Point getLocation(Point reference) {  

		Rectangle bounds = getBox().getCopy();
		Point head = new Point(bounds.x + bounds.width / 2, bounds.y);
		Point p2 = new Point(bounds.x, bounds.y + bounds.height);
		Point p3 = new Point(bounds.x + bounds.width, bounds.y
				+ bounds.height);
        Point p;
        double d1, d2, d3;
        d1 = distance(reference,head);  
        d2 = distance(reference,p2);  
        d3 = distance(reference,p3);  
          
        p = head;  
        if(d1<d2 && d1<d3) {  
            p = head;  
        }  
        if(d2<d1 && d2<d3) {  
            p = p2;  
        }  
        if(d3<d1 && d3<d2) {  
            p = p3;  
        }  
          
        return p;  
    }  
  
    private double distance(Point p1, Point p2) {  
        return Math.abs(p1.x-p2.x) * Math.abs(p1.x-p2.x) + Math.abs(p1.y-p2.y) * Math.abs(p1.y-p2.y);  
    }  
}
