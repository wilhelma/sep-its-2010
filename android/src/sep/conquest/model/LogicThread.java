package sep.conquest.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import sep.conquest.model.behaviour.BehaviourFactory;
import sep.conquest.model.behaviour.IBehaviour;
import sep.conquest.model.handler.Handler;
import sep.conquest.model.handler.HandlerFactory;
import sep.conquest.model.requests.PuckRequest;
import sep.conquest.util.ConquestLog;
import sep.conquest.util.Utility;

/**
 * The LogicThread class will be used by Puck objects for navigation decisions.
 * 
 * @author Andreas Wilhelm
 * 
 */
public class LogicThread implements Runnable {

	/**
	 * A instance of the AStarPathFinder class for shortest-path-finding.
	 */
	private AStarPathFinder aStar;

	/**
	 * A reference to the robot.
	 */
	private Puck robot;

	/**
	 * The current behaviour.
	 */
	private IBehaviour stateBehaviour;

	/**
	 * First Handler to handle broadcast messages.
	 */
	private Handler bcHandler;

	/**
	 * First Handler to handle bluetooth-messages.
	 */
	private Handler btHandler;

	/**
	 * The queue for broadcast messages.
	 */
	private ConcurrentLinkedQueue<IRequest> bcQueue = new ConcurrentLinkedQueue<IRequest>();

	/**
	 * The awaiting turn command.
	 */
	private Orientation turn = Orientation.UNKNOWN;

	/**
	 * Indicates whether the thread should run.
	 */
	private boolean run = true;

	/**
	 * The constructor of LogicThread assigns the robot as well as its status to
	 * attributes.
	 * 
	 * @param robot
	 */
	public LogicThread(Puck robot) {
		this.robot = robot;
		State state = robot.getRobotStatus().get(robot.getID()).getState();
		stateBehaviour = BehaviourFactory.createBehaviourChain(state);
		bcHandler = HandlerFactory.getPuckBCChain(this);
		btHandler = HandlerFactory.getPuckBTChain(this);
	}

	/**
	 * Puts a request message of the broadcast-communication on the queue.
	 * 
	 * @param request
	 *            The new request message.
	 */
	public void addMessage(IRequest request) {
		bcQueue.add(request);
	}

	/**
	 * Change the behaviour due to the state change.
	 * 
	 * @param state
	 */
	public void changeBehaviour(State state) {
		BehaviourFactory.createBehaviourChain(state);
		stateBehaviour = BehaviourFactory.createBehaviourChain(state);
		ConquestLog.addMessage(this, "Behaviour changed to "
				+ stateBehaviour.getClass().toString());
	}

	/**
	 * Returns the current state of the robot.
	 * 
	 * @return The state.
	 */
	private RobotStatus getRobotState() {
		return robot.getRobotStatus().get(robot.getID());
	}

	/**
	 * Sets the next orientation of the robot.
	 * 
	 * @param ori
	 *            The orientation to set after ok message is received.
	 */
	public void setTurnOrientation(Orientation ori) {
		turn = ori;
	}

	/**
	 * Returns the orientation to set as soon as ok message is received.
	 * 
	 * @return The new orientation.
	 */
	public Orientation getTurnOrientation() {
		return turn;
	}

	/**
	 * Returns the robot of the logic thread.
	 * 
	 * @return The Puck.
	 */
	public Puck getRobot() {
		return robot;
	}

	/**
	 * Initialize a map with the map-information from the robot.
	 * 
	 * @return The initialized map.
	 */
	private Map<Integer, Integer> initMap() {
		Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
		for (GraphNode node : robot.getMap().getFrontierList()) {
			map.put(Utility.makeKey(node.getXValue(), node.getYValue()), 0);
		}
		return map;
	}

	/**
	 * Returns a map-node which has the most beneficial navigation costs.
	 * 
	 * @param map
	 *            The map.
	 * @return The best node.
	 */
	private int[] getBestNode(Map<Integer, Integer> map) {
		int bestValue = 0;
		int[] bestNode = { 0, 0 };
		Set<Integer> nodes = map.keySet();
		for (Integer node : nodes) {
			Integer cost = map.get(node);
			if (bestValue == 0 || cost < bestValue) {
				bestValue = cost;
				bestNode = Utility.extractCoordinates(node);
			}
		}
		return bestNode;
	}

	/**
	 * Drive to a specific node.
	 * 
	 * @param intentNode
	 *            The node.
	 */
	public void driveTo() {
		int[][] dest = { getRobotState().getIntentPosition() };

		// calculate best path to destination
		aStar = new AStarPathFinder();
		PathNode[] path = aStar
				.find(robot, getRobotState().getPosition(), dest);
		if (path == null)
			throw new IllegalStateException("Error! A-Star calculated wrong"
					+ "path from " + getRobotState().getPosition() + " to "
					+ dest);

		// If a path to an intended destination exists, send an appropriate
		// drive command. In case of turn-commands in order to drive to the
		// destination, ok-messages are handled to set the new orientation.
		if (path[0].getPathCosts() > 0) {
			MapNode nextMapNode = (MapNode) path[0].getNext().getNode();
			int[] node = { nextMapNode.getXValue(), nextMapNode.getYValue() };
			Orientation newDir = Orientation.getTurnedOrientation(
					getRobotState().getPosition(), node);

			int command = Orientation.addLocalDirection(getRobotState()
					.getOrientation(), newDir);

			turn = Orientation.getTurnedOrientation(command, getRobotState()
					.getOrientation());
			robot.driveCommand(command);
		}
	}

	/**
	 * Returns a bluetooth message if message-receiving is expected and the
	 * byte-length of the message on the socket has reached the desired length.
	 * 
	 * @return The message. If no message exists, an empty byte-array will be
	 *         returned.
	 */
	private IRequest getBTMessage() {
		byte[] message = robot.readSocket();
		if (message.length > 0) {
			return new PuckRequest(message, robot);
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		State currentState = getRobot().getRobotStatus()
				.get(getRobot().getID()).getState();

		while (run && (currentState != State.ERROR)) {
			boolean changed = false;

			// handle broadcast messages
			while (!bcQueue.isEmpty()) {
				IRequest req = bcQueue.poll();
				ConquestLog.addMessage(this, robot.getName() + " :"
						+ req.getKind().name());
				bcHandler.handleRequest(req);
				changed = true;
			}

			// handle bluetooth messages
			IRequest request = getBTMessage();
			if (request != null) {
				ConquestLog.addMessage(this, robot.getName() + " :"
						+ request.getKind().name());
				btHandler.handleRequest(request);
				changed = true;
			}

			// execute behaviours on changed state
			if (changed && !getRobot().isControlled()) {
				Map<Integer, Integer> map = initMap();
				if (stateBehaviour.execute(map, robot)) {
					getRobotState().setIntentPosition(getBestNode(map));
					driveTo();
				}

				// Update state.
				currentState = getRobot().getRobotStatus().get(
						getRobot().getID()).getState();
			} else {
				Thread.yield();
			}
		}
	}

	/**
	 * Sets flag that will end Thread before next execution.
	 */
	public void destroy() {
		run = false;
	}
}