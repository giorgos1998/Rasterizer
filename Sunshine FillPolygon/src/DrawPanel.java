/*
 * DrawPaneö.java
 *
 * Created on 1. September 2007, 13:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Sunshine
 */

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;


public class DrawPanel extends javax.swing.JPanel {
    
    private Vector<Point> polygon;      // hold polygon vertices
    
    private boolean isPolyClosed = false;   // finished drawing polygon?
    
    int mouseX = -10;           // current x-position of cursor on panel
    int mouseY = -10;           // current y-position of cursor on panel
    
    
    /** Creates a new instance of DrawPanel */
    public DrawPanel(int width, int height) 
    {
        this.setLayout(null);
        this.setBounds(0, 0, width, height);
        this.setBackground(new Color(255, 156, 50));//Color.YELLOW);
        
        polygon = new Vector<Point>();

        // test
        /*polygon.add(new Point(40, 40));
        polygon.add(new Point(140, 40));
        polygon.add(new Point(140, 90));
        polygon.add(new Point(100, 90));
        polygon.add(new Point(100, 60));
        polygon.add(new Point(60, 60));
        polygon.add(new Point(60, 120));
        polygon.add(new Point(40, 120));
        isPolyClosed = true;
        polygon.add(polygon.firstElement());*/
        
        this.addMouseMotionListener(new MouseAdapter()
        {
           public void mouseMoved(MouseEvent event)
           {
               mouseX = event.getX();
               mouseY = event.getY();
               if (!isPolyClosed)
                repaint();
           }
        });
        
        this.addMouseListener(new MouseAdapter()
        {
            public void mouseReleased(MouseEvent event)
            {
                // if we cannot add more points, bye
                if (isPolyClosed) return;
                
                // check if polygon should be closed
                if ( (polygon.size() > 2) && 
                     (Math.abs(event.getX() - polygon.firstElement().x) < 5) &&
                     (Math.abs(event.getY() - polygon.firstElement().y) < 5) )
                {
                    isPolyClosed = true;
                    polygon.add(polygon.firstElement());
                    System.out.println("Polygon closed");
                    /*if (checkConvexity())
                    {
                        System.out.println("Convex!");
                    }
                    else
                    {
                        System.out.println("Not Convex!");
                    }*/
                }
                else
                {
                    polygon.add(new Point(event.getX(), event.getY()));
                }
                repaint();
            }
        });
    }
    
    /*
     * Here goes all the painting
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
                
        // draw the point rectangles
        Point p;
        for (int i = 0; i < polygon.size(); i++)
        {
            p = polygon.get(i);
            g.fillRect(p.x - 2, p.y - 2, 5, 5);
        }
        
        // draw the connecting lines
        if (polygon.size() < 1) return;
        Point p2;
        for (int i = 0; i < polygon.size() - 1; i++)
        {
            p = polygon.get(i);
            p2 = polygon.get(i+1);
            g.drawLine(p.x, p.y, p2.x, p2.y);
        }
        
        
        // if polygon is completed, work is done
        if (this.isPolyClosed) return;
        
        // draw current line
        g.drawLine(polygon.lastElement().x, polygon.lastElement().y, mouseX, mouseY);
        
        // draw string to close line
        if ( (polygon.size() > 2) && 
             (Math.abs(mouseX - polygon.firstElement().x) < 5) &&
             (Math.abs(mouseY - polygon.firstElement().y) < 5) )
        {
            g.drawString("Closing polygon", mouseX + 5, mouseY + 5);
        }
    }
    
    /*
     * check if polygon is convex
     */
    public boolean checkConvexity()
    {
        if (polygon.size() < 3) return false;
        
        Point p;
        Point v;
        Point u;
        int res = 0;
        for (int i = 0; i < polygon.size() - 2; i++)
        {
            p = polygon.get(i);
            Point tmp = polygon.get(i+1);
            v = new Point();
            v.x = tmp.x - p.x;
            v.y = tmp.y - p.y;
            u = polygon.get(i+2);
           
            if (i == 0)
            {
                 res = u.x * v.y - u.y * v.x + v.x * p.y - v.y * p.x;
            }
            else
            {
                int newres = u.x * v.y - u.y * v.x + v.x * p.y - v.y * p.x;
                if ( (newres > 0 && res < 0) || (newres < 0 && res > 0) )
                    return false;
            }
        }
        return true;
    }
    
    
    /*
     * Create from the polygon vertices an array of edges.
     * Note that the first vertice of an edge is always the one with the smaller y coordinate one of both
     */
    public Edge[] createEdges()
    {
        Edge[] sortedEdges = new Edge[polygon.size()-1];
        for (int i = 0; i < polygon.size() - 1; i++)
        {
            //if (polygon.elementAt(i).y == polygon.elementAt(i+1).y) continue;
            if (polygon.elementAt(i).y < polygon.elementAt(i+1).y)
                sortedEdges[i] = new Edge(polygon.elementAt(i), polygon.elementAt(i+1));
            else
                sortedEdges[i] = new Edge(polygon.elementAt(i+1), polygon.elementAt(i));
        }
        return sortedEdges;
    }
    
    
    /*
     * Fill our polygon!
     */
    public void FillPolygon()
    {
        if (!this.isPolyClosed) return;
        // create edges array from polygon vertice vector
        // make sure that first vertice of an edge is the smaller one
        Edge[] sortedEdges = this.createEdges();
         
        // sort all edges by y coordinate, smallest one first, lousy bubblesort
        Edge tmp;
        
        for (int i = 0; i < sortedEdges.length - 1; i++)
            for (int j = 0; j < sortedEdges.length - 1; j++)
            {
                if (sortedEdges[j].p1.y > sortedEdges[j+1].p1.y) 
                {
                    // swap both edges
                    tmp = sortedEdges[j];
                    sortedEdges[j] = sortedEdges[j+1];
                    sortedEdges[j+1] = tmp;
                }  
            }
        
        // find biggest y-coord of all vertices
        int scanlineEnd = 0;
        for (int i = 0; i < sortedEdges.length; i++)
        {
            if (scanlineEnd < sortedEdges[i].p2.y)
                scanlineEnd = sortedEdges[i].p2.y;
        }
        
        // --- DEBUG ---
        /*
        System.out.println("==============");
        for (int i = 0; i < sortedEdges.length; i++)
            System.out.println("X: " + sortedEdges[i].p1.x + " Y: " + sortedEdges[i].p1.y + 
                   " --- " + "X: " + sortedEdges[i].p2.x + " Y: " + sortedEdges[i].p2.y);
        */
        
        
        // scanline starts at smallest y coordinate
        int scanline = sortedEdges[0].p1.y;
        
        // this list holds all cutpoints from current scanline with the polygon
        ArrayList<Integer> list = new ArrayList<Integer>();
        
        Graphics g = this.getGraphics();
        
        // move scanline step by step down to biggest one
        for (scanline = sortedEdges[0].p1.y; scanline <= scanlineEnd; scanline++)
        {
            //System.out.println("ScanLine: " + scanline); // DEBUG
            
            list.clear();
            
            // loop all edges to see which are cut by the scanline
            for (int i = 0; i < sortedEdges.length; i++)
            {   
                
                // here the scanline intersects the smaller vertice
                if (scanline == sortedEdges[i].p1.y) 
                {
                    if (scanline == sortedEdges[i].p2.y)
                    {
                        // the current edge is horizontal, so we add both vertices
                        sortedEdges[i].deactivate();
                        list.add((int)sortedEdges[i].curX);
                    }
                    else
                    {
                        sortedEdges[i].activate();
                        // we don't insert it in the list cause this vertice is also
                        // the (bigger) vertice of another edge and already handled
                    }
                }
                
                // here the scanline intersects the bigger vertice
                if (scanline == sortedEdges[i].p2.y)
                {
                    sortedEdges[i].deactivate();
                    list.add((int)sortedEdges[i].curX);
                }
                
                // here the scanline intersects the edge, so calc intersection point
                if (scanline > sortedEdges[i].p1.y && scanline < sortedEdges[i].p2.y)
                {
                    sortedEdges[i].update();
                    list.add((int)sortedEdges[i].curX);
                }
                
            }
            
            // now we have to sort our list with our x-coordinates, ascendend
            int swaptmp;
            for (int i = 0; i < list.size(); i++)
                for (int j = 0; j < list.size() - 1; j++)
                {
                    if (list.get(j) > list.get(j+1))
                    {
                        swaptmp = list.get(j);
                        list.set(j, list.get(j+1));
                        list.set(j+1, swaptmp);
                    }
                
                }
            
            g.setColor(Color.BLACK);

            if (list.size() < 2 || list.size() % 2 != 0) 
            {
                System.out.println("This should never happen!");
                continue;
            }
             
            // so draw all line segments on current scanline
            for (int i = 0; i < list.size(); i+=2)
            {
                g.drawLine(list.get(i), scanline, 
                           list.get(i+1), scanline);
            }
            
        }

    }

      
    /*
     * Clean up to handle a new polygon
     */
    public void Reset()
    {
        polygon.clear();
        isPolyClosed = false;
        this.repaint();
    }
    
    public boolean isPolygonClosed() { return this.isPolyClosed; }
}
