package sep.conquest.model;

/**
 * The RobotState represents the current status of a specific Puck. It contains
 * the position, the orientation, the indicator whether the robot is moving as
 * well as the current state.
 * 
 * @author Andreas Wilhelm
 * 
 */
public class RobotStatus {

	/**
	 * The current position of the robot within the map.
	 */
	private int[] position;

	/**
	 * The type of the current node.
	 */
	private NodeType nodeType;

	/**
	 * The current orientation of the robot within the map.
	 */
	private Orientation orientation;

	/**
	 * Indicates if the robot is currently moving.
	 */
	private boolean moving;

	/**
	 * The current state of the robot.
	 */
	private State state;
	
	/**
	 * The intended destination.
	 */
	private int[] intentPosition;

	/**
	 * The constructor takes every needed parameter to fill the ModelData
	 * Container.
	 * 
	 * @param pos
	 *            The position of the robot.
	 * @param orientation
	 *            The Orientation of the robot.
	 * @param moving
	 *            Indicates if the robot is moving.
	 * @param state
	 *            The current state of the robot.
	 * @param nodeType
	 *            The type of the node.
	 * @param intentPosition
	 *            The intended destination node.
	 */
	public RobotStatus(int[] position, Orientation orientation, boolean moving,
			State state, NodeType nodeType, int[] intentPosition) {
		this.position = position;
		this.orientation = orientation;
		this.moving = moving;
		this.state = state;
		this.nodeType = nodeType;
		this.intentPosition = intentPosition;
	}

	/**
	 * The constructor with initial values.
	 */
	public RobotStatus() {
		position = new int[2];
		position[0] = position[1] = 0;
		orientation = Orientation.UNKNOWN;
		moving = false;
		state = State.IDLE;
	}

	/**
	 * Returns the current position of the robot.
	 * 
	 * @return An int-array with x and y position.
	 */
	public int[] getPosition() {
		return position;
	}

	/**
	 * Returns the current orientation of the robot.
	 * 
	 * @return The orientation data.
	 */
	public Orientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Returns the type of the current node.
	 * 
	 * @return A NodeType.
	 */
	public NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * Indicates if the robot is currently moving.
	 * 
	 * @return A boolean value which indicates the moving state of the robot.
	 */
	public boolean isMoving() {
		return moving;
	}

	/**
	 * Returns the current state of the robot.
	 * 
	 * @return The state value.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the current destination position.
	 * 
	 * @return The destination.
	 */
	public int[] getIntentPosition() {
		return intentPosition;
	}	
	
	/**
	 * Sets the position of the robot.
	 * 
	 * @param position
	 *            The position (x and y value).
	 */
	public void setPosition(int[] position) {
		this.position = position;
	}

	/**
	 * Sets the orientation of the robot.
	 * 
	 * @param orientation
	 *            The orientation.
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	/**
	 * Sets the state of the robot.
	 * 
	 * @param state
	 *            The state.
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * Sets the moving-state of the robot.
	 * 
	 * @param moving
	 *            Indicates if the robot is moving.
	 */
	public void setMoving(boolean moving) {
		this.moving = moving;
	}
	
	/**
	 * Sets the type of the Node.
	 * 
	 * @param nodeType The NodeType.
	 */
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * Sets the destination node.
	 * 
	 * @param intentPosition The intended destination.
	 */
	public void setIntentPosition(int[] intentPosition) {
		this.intentPosition = intentPosition;
	}	
	
	/**
	 * Sets the complete status of the robot.
	 * 
	 * @param status
	 *            The RobotStatus.
	 */
	public void setRobotStatus(RobotStatus status) {
		this.position = status.getPosition();
		this.orientation = status.getOrientation();
		this.moving = status.isMoving();
		this.state = status.getState();
		this.nodeType = status.getNodeType();
		this.intentPosition = status.getIntentPosition();
	}
}
