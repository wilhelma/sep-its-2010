package sep.conquest.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

import sep.conquest.R;
import sep.conquest.controller.Controller;
import sep.conquest.model.ConquestUpdate;
import sep.conquest.model.GridMap;
import sep.conquest.model.MapFileHandler;
import sep.conquest.model.MapNode;
import sep.conquest.model.Orientation;
import sep.conquest.model.PuckFactory;
import sep.conquest.model.State;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


/**
 * The Map class extends from Activity and is responsible for the representation
 * of the map and the selection of e-pucks.
 *
 * @author Florian Buerchner
 *
 */
public class Map extends Activity implements Observer {
	
	private static final int UPDATE_MESSAGE = 0;
	
	/**
	 * The Spinner to select a robot in the map. For manual control, e.g.
	 */
	private Spinner mRobotSelect;
	
	/**
	 * Saves the values for the spinner.
	 */
	private ArrayAdapter < String > mRobotAdapter;
	
	/**
	 * Saves the positions of the robots.
	 */
	private LinkedList < EpuckPosition > mPositions;
	
	/**
	 * Saves the map mode, whether the spinner is enabled or not.
	 */
	private MapMode mMode;
	
	/**
	 * Saves the ids of the e-pucks.
	 */
	private LinkedList<String> mIds;
	
	/**
	 * Dialog is shown when robots are synchronizing.
	 */
	private ProgressDialog pd;
	
	/**
	 * Once the simulator is started and the activity is returned, it shouldn't
	 * be started a second time.
	 */
	private boolean first = true;
	
	/**
	 * Used to update the View from update-method.
	 */
	private Handler updateHandler;
	

    /**
     * Is implemented by Activity and is called when the map class is accessed.
     * It initializes the spinner control element for the selection of the
     * e-pucks and calculates the dimensions of the display. They must be
     * available for the MapSurfaceView, but this class itself can't get this
     * information, because they are only available in Activity classes.
     *
     * @param savedInstanceState Bundle of state information, saved when
     *                           Activity was executed before.
     */
    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map_main);

        mPositions = new LinkedList < EpuckPosition >();
        mIds = new LinkedList<String>();
        
        mRobotSelect = (Spinner) findViewById(R.id.epuck_selector);
        final MapSurfaceView map = (MapSurfaceView) findViewById(R.id.map_view);
        
        mRobotAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mRobotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //initially set
        mRobotAdapter.add("none");

        mRobotSelect.setAdapter(mRobotAdapter);
        mRobotSelect.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(final AdapterView < ? > parent,
                    final View v, final int position, final long id) {
                String s = (String) parent.getItemAtPosition(position);
                if (s.equals("none")) {
                    map.setSelectedRobot(null);
                } else {
                    map.setSelectedRobot(s);
                }

            }

            public void onNothingSelected(final AdapterView < ? > parent) { }

        });
        
        
        
        mMode = (MapMode) getIntent().getSerializableExtra(MapMode.class.toString());
        if (mMode == MapMode.IMPORT) {
        	MapSurfaceView view = (MapSurfaceView) findViewById(R.id.map_view);
        	view.setMode(mMode);
        	mRobotSelect.setEnabled(false);
        	loadMap();
        } else if (mMode == MapMode.EXPLORATION) {
        	setSpinner();
        	setPD();       	
        } else {
        	mRobotSelect.setEnabled(false);
        	setPD();
        }
    }
    
    public void onResume() {
    	super.onResume();
    	if (mMode != MapMode.IMPORT) {
    	Controller.getInstance().getEnv().addObserver(this);
    	}
    	//automatischer start
    	if (mMode == MapMode.SIMULATION && first) {
    		PuckFactory.getSimulator().start();
    		first = false;
    	}
    	
    }
    
    public void onPause() {
    	super.onPause();
    	if (mMode != MapMode.IMPORT) {
        	Controller.getInstance().getEnv().deleteObserver(this);
        	}
    }

    /**
     * Sets the menu of the Activity.
     *
     * @param menu Reference on menu that has to be set.
     * @return Returns true when the correct menu is displayed.
     */
    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        MapMode mode = (MapMode) getIntent().
                       getSerializableExtra(MapMode.class.toString());
        mMode = mode;

        switch(mode) {
        case EXPLORATION: inflater.inflate(R.menu.map_exp_menu, menu); break;
        case SIMULATION: inflater.inflate(R.menu.map_sim_menu, menu); break;
        case IMPORT: break;
        default: break;
        }

        return true;
    }

    /**
     * Handles selections of menu items.
     *
     * @param item The menu item that has been selected.
     * @return Returns true when the correct intent is started.
     */
    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.mnuStatistics:
            Intent stats = new Intent();
            stats.setComponent(new ComponentName(getApplicationContext()
                    .getPackageName(), Statistics.class.getName()));
            startActivity(stats);
            break;

        case R.id.mnuControl:
            Intent steer = new Intent();
            steer.setComponent(new ComponentName(getApplicationContext()
                    .getPackageName(), Steer.class.getName()));
            startActivity(steer);
            break;

        case R.id.mnuExport:
            Intent export = new Intent();
            export.setComponent(new ComponentName(getApplicationContext()
                    .getPackageName(), Export.class.getName()));
            startActivity(export);
            break;
            
        case R.id.mnuStart:
        	PuckFactory.getSimulator().start();
        	break;
        	
        case R.id.mnuPause:
        	PuckFactory.getSimulator().pause();
        	break;

        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    /**
     * Gets the file name from the intent and loads the map for preview.
     */
    private void loadMap() {
    	String mapName = getIntent().getStringExtra(Import.EXTRA_FILE_PATH);
    	try {
			GridMap container = MapFileHandler.openMap(mapName);
			drawMap(container.getMapAsList(), container.getMapBorders());
		} catch (FileNotFoundException e) {
			displayMessage(getString(R.string.ERR_MSG_FILE_NOT_FOUND), true);
			return;
		} catch (IOException e) {
			displayMessage(getString(R.string.ERR_MSG_INVALID_FILE), true);
			return;
		}
    }
    
    /**
     * Makes the progress dialog appear.
     */
    private void setPD() {
    	pd = ProgressDialog.show(this, getString(R.string.TXT_SYNC), getString(R.string.TXT_LOCALIZING));
    }
    
    /**
     * Draws a preview of the selected map.
     * @param map chosen map
     * @param borders map borders
     */
    private void drawMap(List<MapNode> map, int[] borders) {
    	MapSurfaceView draw = (MapSurfaceView) findViewById(R.id.map_view);
		
		draw.setMode(MapMode.IMPORT);
		draw.setMap(map, borders);
    }
    
    /**
     * Sets the spinner into the surface view to make it possible to
     * select a e-puck out of the map by a touch event.
     */
    private void setSpinner() {
    	MapSurfaceView map = (MapSurfaceView) findViewById(R.id.map_view);
    	map.setSpinner(mRobotSelect);
        
    	// Initialize message handler to deal with update messages.
        updateHandler = new Handler() {

          public void handleMessage(final Message msg) {
            if (msg.what == UPDATE_MESSAGE) {
              mRobotAdapter.clear();

              mRobotAdapter.add("none");
              for (final String names : mIds) {
                mRobotAdapter.add(names);
              }
            }
          }
        };
    }

    /**
     * The ConquestUpdate provides the data of the robots. E.g. explored
     * nodes and new positions.
     */
	public void update(Observable obs, Object data) {
		synchronized(data) {
		ConquestUpdate cu = (ConquestUpdate) data;
		MapSurfaceView draw = (MapSurfaceView) findViewById(R.id.map_view);
		
		Set<UUID> id = cu.getRobotStatus().keySet();
		mPositions.clear();
		mIds.clear();
		//mRobotAdapter.clear();
		//mRobotAdapter.add("none");
		for (UUID key : id) {
			int[] position = cu.getRobotStatus().get(key).getPosition();
			String name = cu.getRobotName(key);
			Orientation ori = cu.getRobotStatus().get(key).getOrientation();
			
			State state = cu.getRobotStatus(key).getState();
			if (state == State.EXPLORE || state == State.FINISH || state == State.RETURN || state == State.BLOCKED) {
				if (pd.isShowing()) {
					pd.dismiss();
				}
				mPositions.add(new EpuckPosition(position[0], position[1], name, ori));
				mIds.add(name);
			}
			
			
			//mRobotAdapter.add(name);
			
			
		}
		
		
		draw.setMode(mMode);
		draw.setMap(cu.getMapList(), cu.getBorders());
		draw.setRobotPosition(mPositions);
			
		if (updateHandler != null) {
			updateHandler.obtainMessage(UPDATE_MESSAGE).sendToTarget();
		}
		}
	}
	
	  /**
	   * Displays a message on top of the Activity.
	   * 
	   * @param message
	   *          The message to display.
	   */
	  private void displayMessage(String message, boolean isError) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(message);
	    builder.setCancelable(false);
	    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	      
	      public void onClick(DialogInterface dialog, int which) {
	        dialog.dismiss();
	        
	      }
	    });
	    AlertDialog alert = builder.create();
	    alert.show();
	  }

}
