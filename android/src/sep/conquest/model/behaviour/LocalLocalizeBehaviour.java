package sep.conquest.model.behaviour;

import java.util.Map;
import java.util.UUID;

import sep.conquest.model.NodeType;
import sep.conquest.model.Orientation;
import sep.conquest.model.Puck;
import sep.conquest.model.RobotStatus;
import sep.conquest.model.State;
import sep.conquest.util.Utility;

/**
 * LocalLocalizeBehaviour represents a behaviour for localizing adjacent robots.
 * It extends the Behaviour class for enabling a behaviour-chain.
 * 
 * @author Andreas Wilhelm
 */
public final class LocalLocalizeBehaviour extends Behaviour {

  /**
   * Used to save relative start positions for simplification.
   */
  public static Map<UUID, Integer> startPositions;

  /**
   * Used to save relative start orientations for simplification.
   */
  public static Map<UUID, Orientation> startOrientations;

  private boolean statusRequested = false;

  /**
   * The constructor enables chain-handling by calling the constructor of the
   * super-class (Behaviour).
   * 
   * @param stateLevel
   *          The level of the state.
   * @param next
   *          A reference to the next behaviour.
   */
  protected LocalLocalizeBehaviour(State stateLevel, IBehaviour next) {
    super(stateLevel, next);
  }

  /*
   * (non-Javadoc)
   * 
   * @see sep.conquest.model.IBehaviour#execute(java.util.Map)
   */
  public boolean execute(Map<Integer, Integer> map, Puck robot) {

    if (!robot.isMessageExpected()) {
      if (!statusRequested) {
        // If Puck has not requested its own status so far send status request.
        robot.requestStatus();
        statusRequested = true;
      } else {
        RobotStatus status = robot.getRobotStatus().get(robot.getID());
        
        // Get and set start orientations from static field.
        Orientation pOri = startOrientations.get(robot.getID());
        status.setOrientation(pOri);
        
        // Get and set start position and related node type.
        status.setPosition(Utility.extractCoordinates(startPositions.get(robot
            .getID())));
        NodeType nType = Utility.calculateNodeTypesToRealWorld(status
            .getNodeType(), pOri);
        status.setNodeType(nType);
        
        // Add start node to local map.
        robot.getMap().addNode(status.getPosition()[0],
            status.getPosition()[1], nType);
        
        // Finally changed behaviour to EXPLORE.
        robot.changeBehaviour(State.EXPLORE);
      }
    }
    return super.execute(map, robot);
  }
}
