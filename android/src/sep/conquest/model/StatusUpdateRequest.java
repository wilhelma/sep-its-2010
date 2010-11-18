package sep.conquest.model;

import java.util.UUID;


/**
 * The StatusUpdateRequest represents message objects that are sent by 
 * IComClient clients. It contains a position update information of a specific
 * robot as well as its current speed and orientation.
 * 
 * @author Andreas Wilhelm
 *
 */
public class StatusUpdateRequest extends Request {
    
    /**
     * The state of the specific robot that has sent the message.
     */
    private RobotStatus robotStatus;
    
    /**
     * The constructor expects a sender, a list of receiver as well as the
     * corresponding RobotStatus instance.
     * 
     * @param receiver The IDs of the receiver.
     * @param robotStatus The corresponding state of the robot.
     */
    public StatusUpdateRequest(UUID[] receiver, 
    		RobotStatus robotStatus) {
    	super(receiver);    	
        this.robotStatus = robotStatus;
    }

    /* (non-Javadoc)
     * @see sep.conquest.model.IRequest#getKind()
     */
    public MessageType getKind() {
        return MessageType.STATUS_UPDATE;
    }
    
    /**
     * getStatus returns the status of the robot (sender).
     * 
     * @return The drive-command.
     */
    public RobotStatus getStatus() {
        return robotStatus;
    }
}
