import java.awt.Point;
/*
 * Edge.java
 *
 * Created on 7. September 2007, 16:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Sunshine
 */

/*
 * Just a class for an edge.
 */
public class Edge {
    
    public Point p1;        // first vertice
    public Point p2;        // second vertice
    float m;                // slope
    
    float curX;             // x-coord of intersection with scanline
    
    /*
     * Create on edge out of two vertices
     */
    public Edge(Point a, Point b)
    {
        p1 = new Point(a);
        p2 = new Point(b);
        
        // m = dy / dx
        m = (float)((float)(p1.y - p2.y) / (float)(p1.x - p2.x));
    }
    
    /*
     * Called when scanline intersects the first vertice of this edge.
     * That simply means that the intersection point is this vertice.
     */
    public void activate()
    {
        curX = p1.x;
    }
    
    /*
     * Update the intersection point from the scanline and this edge.
     * Instead of explicitly calculate it we just increment with 1/m every time
     * it is intersected by the scanline.
     */
    public void update()
    {
        curX += (float)((float)1/(float)m);
    }
    
    /*
     * Called when scanline intersects the second vertice, 
     * so the intersection point is exactly this vertice and from now on 
     * we are done with this edge
     */
    public void deactivate()
    {
        curX = p2.x;
    }
    
}
