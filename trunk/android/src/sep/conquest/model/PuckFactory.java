package sep.conquest.model;

import java.util.Collection;
import java.util.UUID;

import android.bluetooth.BluetoothSocket;

/**
 * Contains methods to create a set of RealPucks or VirtualPucks. Created Pucks
 * are registered at the ComManager.
 * 
 * @author Andreas Poxrucker
 * 
 */
public class PuckFactory {

  /**
   * Creates a new RealPuck instance for each BluetoothDevice of the set and
   * registers it to the ComManager. If creation fails (for example when device
   * is not available) creation is aborted and false is returned.
   * 
   * As soon as all Pucks are successfully created method return true.
   * 
   * If empty set or null is passed method returns false.
   * 
   * @param robots
   *          Set of BluetoothDevices identified as e-puck robots.
   * @return True, if creation and registration was successful, false otherwise.
   */
  public static boolean createRealPucks(Collection<BluetoothSocket> robots) {

    // Get instance of ComManager to add created Pucks.
    ComManager man = ComManager.getInstance();

    // In case set is empty or null return false.
    // Otherwise start creating RealPucks.
    if (robots == null || robots.isEmpty()) {

      // Evtl. IllegalArgumentException besser?!
      return false;
    } else {

      // Iterate over set and create RealPuck for each device.
      for (BluetoothSocket robot : robots) {
        UUID newUUID = UUID.randomUUID();
        Puck newPuck = new RealPuck(robot, newUUID);
        man.addClient(newUUID, newPuck);
      }
      return true;
    }
  }

  /**
   * Creates number times a new VirtualPuck instance and registers it to the
   * ComManager.
   * 
   * As soon as all Pucks are created and connected to the ComManager return
   * true.
   * 
   * @param number
   *          The number of VirtualPucks to create.
   * @return True, if creation and registration was successful, false otherwise.
   */
  public static boolean createVirtualPucks(int number) {

    
    // Get instance of ComManager to add created Pucks.
    ComManager man = ComManager.getInstance();

    // If number is greater than zero start creation.
    // Otherwise return false.
    if (number > 0) {
      for (int i = 0; i < number; i++) {
        UUID newUUID = UUID.randomUUID();
        Puck newPuck = new VirtualPuck(UUID.randomUUID());
        man.addClient(newUUID, newPuck);
      }
      return true;
    } else {

      // Evtl. IllegalArgumentException besser?!
      return false;
    }
  }
}