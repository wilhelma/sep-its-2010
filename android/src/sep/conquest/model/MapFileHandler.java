package sep.conquest.model;

import java.io.File;

import android.os.Environment;

/**
 * Contains methods to open maps from the file system and to save maps.
 * 
 * @author Andreas Poxrucker
 * 
 */
public class MapFileHandler {

  /**
   * The path where the maps will be saved.
   */
  private static final File PATH = Environment.getDataDirectory()
      .getAbsoluteFile();

  /**
   * Opens a file containing a map of type GridMap a reconstructs the GridMap.
   * 
   * @return The GridMap extracted from the file or null, if file was invalid.
   */
  public static GridMap openMap() {
    return null;
  }

  /**
   * Saves a map as file in the file system.
   * 
   * @param map
   *          The map that has to be saved.
   * @return True, if map was successfully saved, false otherwise.
   */
  public boolean saveMap(GridMap map) {
    return true;
  }
}
