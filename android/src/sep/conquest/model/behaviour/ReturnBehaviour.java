package sep.conquest.model.behaviour;

import java.util.Map;

import sep.conquest.model.Puck;
import sep.conquest.model.State;

/**
 * ReturnBehaviour represents a behaviour for robots that have to return to
 * their start-nodes. It extends the Behaviour class for enabling a
 * behaviour-chain.
 * 
 * @author Andreas Wilhelm
 */
public final class ReturnBehaviour extends Behaviour {

    /**
     * The constructor enables chain-handling by calling the constructor of
     * the super-class (Behaviour).
     * 
     * @param stateLevel The level of the state.
     * @param next A reference to the next behaviour.
     */
    protected ReturnBehaviour(State stateLevel, IBehaviour next) {
        super(stateLevel, next);
    }

    /* (non-Javadoc)
     * @see sep.conquest.model.IBehaviour#execute(java.util.Map)
     */
    public boolean execute(Map<Integer, Integer> map, Puck robot) {
    	
    	boolean changed = super.execute(map, robot);
    	
    	if (map.isEmpty()) {
    		map.put(LocalLocalizeBehaviour.startPositions.get(robot.getID()), 1);
    		changed = true;
    	} else{
    		robot.changeBehaviour(State.EXPLORE);
    	}
    	
        return changed;
    }
}
