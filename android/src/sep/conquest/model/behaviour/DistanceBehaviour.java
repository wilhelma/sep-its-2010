package sep.conquest.model.behaviour;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sep.conquest.model.AStarPathFinder;
import sep.conquest.model.GraphNode;
import sep.conquest.model.PathNode;
import sep.conquest.model.Puck;
import sep.conquest.model.RobotStatus;
import sep.conquest.model.State;
import sep.conquest.util.Utility;

/**
 * DistanceBehaviour represents a behaviour to identify the next frontier-nodes
 * of a given map. It extends the Behaviour class for enabling a behaviour-
 * chain.
 * 
 * @author Andreas Wilhelm
 */
public final class DistanceBehaviour extends Behaviour {

  /**
   * The constructor enables chain-handling by calling the constructor of the
   * super-class (Behaviour).
   * 
   * @param stateLevel
   *          The level of the state.
   * @param next
   *          A reference to the next behaviour.
   */
  protected DistanceBehaviour(State stateLevel, IBehaviour next) {
    super(stateLevel, next);
  }

  /*
   * (non-Javadoc)
   * 
   * @see sep.conquest.model.IBehaviour#execute(java.util.Map)
   */
  public boolean execute(Map<Integer, Integer> map, Puck robot) {
    // Indicates whether behaviour changed map.
    boolean changed = false;

    List<GraphNode> toRemove = new ArrayList<GraphNode>();
    LinkedList<GraphNode> frontiers = robot.getMap().getFrontierList();
    for (GraphNode frontier : frontiers) {
      Integer key = Utility.makeKey(frontier.getXValue(), frontier.getYValue());
      if (!map.containsKey(key)) {
        toRemove.add(frontier);
      }
    }
    for (GraphNode node : toRemove)
      frontiers.remove(node);

    AStarPathFinder astar = new AStarPathFinder();
    RobotStatus status = robot.getRobotStatus().get(robot.getID());
    int[][] destinations = new int[frontiers.size()][2];
    int i = 0;
    for (GraphNode node : frontiers) {
      destinations[i][0] = node.getXValue();
      destinations[i][1] = node.getYValue();
      i++;
    }

    PathNode[] paths = astar.find(robot, status.getPosition(), destinations);

    if (paths.length > 0) {
      for (PathNode path : paths) {
        if (path != null) {
          map.put(Utility.makeKey(path.getPathNode().getXValue(), path
              .getPathNode().getYValue()), path.getPathCosts());
          changed = true;
        }
      }
    }
    return super.execute(map, robot) || changed;
  }
}