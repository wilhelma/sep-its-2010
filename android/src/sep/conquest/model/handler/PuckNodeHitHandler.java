package sep.conquest.model.handler;

import java.util.UUID;

import android.webkit.WebView.HitTestResult;

import sep.conquest.model.IRequest;
import sep.conquest.model.LogicThread;
import sep.conquest.model.requests.MessageType;

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
  public PuckNodeHitHandler(Handler prev, LogicThread exec) {
    super(prev);
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
	  if(!(request.getKind() == MessageType.PUCK_HITNODE)){
		  return super.handleRequest(request);
	  } else {
		  //What to DO?!
		  return true;
	  }
  }

}
