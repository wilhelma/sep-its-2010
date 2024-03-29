package sep.conquest.model;

import java.io.Serializable;

/**
 * The class MapNode represents a node on the pitch. It saves the coordinates, 
 * the type of the node and how often an e-puck roboter visited the node.
 * 
 * @author Florian Lorenz
 */
public class MapNode implements Serializable {
	
	/**
	 * Left upper corner
	 */
	public static final int TOPLEFTEDGE = 0;
	  
	/**
	 * Right upper corner
	 */
	public static final int TOPRIGHTEDGE = 1;
	
	
	/**
	 * Left bottom corner.
	 */
	public static final int BOTTOMLEFTEDGE = 2;
	
	/**
	 * Right lower corner.
	 */
	public static final int BOTTOMRIGHTEDGE = 3;
	
	
	/**
	 * T-Cross
	 */
	public static final int TOPT = 4;
	
	/**
	 * T-Cross turned 90 degrees
	 */
	public static final int RIGHTT = 5;
	
	/**
	 * T-Cross turned 180 degrees.
	 */
	public static final int BOTTOMT = 6;
	
	/**
	 * T-Cross turned 270 degrees
	 */
	public static final int LEFTT = 7;
	
	/**
	 * Cross of two lines
	 */
	public static final int CROSS = 8;
	
	
	
	/**
	 * An unique serialVersionUID to identify the class.
	 */
	private static final long serialVersionUID = 8344046024055788574L;

	/**
	 * Saves the x-coordinate of the node.
	 */
	private int xValue;
	
	/**
	 * Saves the y-coordinate of the node.
	 */
	private int yValue;
	
	/**
	 * Saves how often an e-puck robot visited the node.
	 */
	private int visitCounter;
	
	/**
	 * Saves the type of the node, e.g. cross, unvisited..
	 */
	private NodeType nodeState;
	
	/**
	 * Constructor creates an instance of the class MapNode and sets the
	 * coordinates and the NodeType.
	 * 
	 * @param newXValue Is the x-coordinate of the new MapNode.
	 * @param newYValue Is the y-coordinate of the new MapNode.
	 * @param newnodeState Is the NodeType of the new MapNode.
	 */
	public MapNode(int newXValue, int newYValue,NodeType newNodeState){
		this.xValue = newXValue;
		this.yValue = newYValue;
		this.nodeState = newNodeState;
	}
	
	/**
	 * This method returns the x-coordinate of the node.
	 * 
	 * @return Returns the attribute xValue.
	 */
	public int getXValue(){
	  return this.xValue;
	}
	
	/**
	 * This method returns the y-coordinate of the node.
	 * 
	 * @return Returns the attribute yValue.
	 */
	public int getYValue(){
	  return this.yValue;	
	}
	
	/**
	 * This method returns the type of the node.
	 * 
	 * @return Returns the attribute nodeType.
	 */
	public NodeType getNodeType(){
	  return this.nodeState;
	}
	
	/**
	 * This method returns how often an e-puck visited the node.
	 * 
	 * @return Returns the attribute.
	 */
	public int getVisitCounter(){
	  return this.visitCounter;
	}
	
	/**
	 * This method changes the x-coordinate of the node.
	 * 
	 * @param newXValue Is the new x-coordinate.
	 */
	public void setXValue(int newXValue){
		this.xValue = newXValue;
	}
	
	/**
	 * This method changes the y-coordinate of the node.
	 * 
	 * @param newYValue Is the new y-coordinate.
	 */
	public void setYValue(int newYValue){
		this.yValue = newYValue;
	}
	
	/**
	 * This method changes the type of the node.
	 * 
	 * @param newNodeType Is the new nodeType.
	 */
	public void setNodeType(NodeType newNodeState){
		this.nodeState = newNodeState;
	}
	
	/**
	 * This method is called when an e-puck drives over the node. It increases 
	 * the attribute visitCounter.
	 */
	public void increaseVisitCounter(){
		this.visitCounter++;
	}
}
