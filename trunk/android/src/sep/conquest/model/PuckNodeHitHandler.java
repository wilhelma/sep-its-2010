package sep.conquest.model;

import java.util.UUID;

/**
 * Handles PuckNodeHit messages coming from the Bluetooth Adapter.
 * 
 * @author Andreas Poxrucker
 *
 */
public class PuckNodeHitHandler extends Handler {

  /**
   * The LogicThread that executes the content.
   */
  private LogicThread executor;
  
  /**
   * Constructor calling constructor of super class.
   */
  public PuckNodeHitHandler(LogicThread exec) {
    super();
    executor = exec;
  }
  
  /**
   * Handles PuckNodeHit messages.
   * 
   * Returns true, if request was handled. If class is not responsible,
   * call handleRequest of next handler. If next handler is null return
   * false.
   * 
   * @param request The request to handle.
   * @return True, if request was handled, false otherwise.
   */
  @Override
  public boolean handleRequest(IRequest request) {
    // TODO Auto-generated method stub
    return false;
  }

}
