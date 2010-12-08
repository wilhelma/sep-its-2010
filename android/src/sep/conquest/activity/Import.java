package sep.conquest.activity;

import sep.conquest.R;
import sep.conquest.model.MapFileHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Allows user to choose a map file form the file system and load it into Map
 * Activity or Simulation Activity.
 * 
 * @author Andreas Poxrucker
 * 
 */
public class Import extends Activity {

  /**
   * Used to send GridMaps in Intent messages.
   */
  public static final String EXTRA_FILE_PATH = "Path";

  /**
   * Used to list available maps.
   */
  private ListView lsMaps;

  /**
   * Used to display names of found map files.
   */
  private ArrayAdapter<String> fileList;

  /**
   * Used to identify last recently selected map.
   */
  private String selectedMap;

  /**
   * Called when Activity is initially created.
   * 
   * Initializes the layout of the Activity and its control elements.
   * 
   * @param savedInstanceState
   *          Bundle of state information, saved when Activity was executed
   *          before.
   */
  public void onCreate(Bundle savedInstanceState) {
    // Call constructor of super class
    super.onCreate(savedInstanceState);

    // Set layout of Activity
    setContentView(R.layout.import_main);

    // Get reference on ListView to display map files from the file system.
    lsMaps = (ListView) findViewById(R.id.lsMaps);
    lsMaps.setOnItemClickListener(new OnItemClickListener() {

      /**
       * Handles clicks on items of ListView containing the available maps.
       * 
       * If an item is selected, its text color changes.
       * 
       * @param parent
       *          AdapterView containing the list item.
       * @param view
       *          List item that has been clicked.
       * @param position
       *          Position of the list item within the AdapterView.
       */
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        TextView txtMap = (TextView) view;
        Resources res = getResources();
        selectedMap = txtMap.getText().toString();
        txtMap.setTextColor(res.getColor(R.color.list_item_not_selected));
      }
    });

    // Display the list of found map files from the file system in the ListView.
    displayMapFiles();
  }

  /**
   * Sets the menu of the Activity.
   * 
   * @param menu
   *          Reference on menu that has to be set.
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.import_menu, menu);
    return true;
  }

  /**
   * Handles selections of the menu of the Activity.
   * 
   * @param item
   *          The MenuItem that has been selected.
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.mnuOpen) {
      if (selectedMap != null) {
        // Determine, whether Activity has been started to display a map or
        // if it has been started to open a configuration for the simulator.
        ImportMode mode = (ImportMode) getIntent().getSerializableExtra(
            ImportMode.class.toString());

        // Intent message to start other Activities.
        Intent start = new Intent();

        switch (mode) {
        case SIMULATION_MAP:
          // If Import has been opened by Simulation put file name
          start.putExtra(EXTRA_FILE_PATH, selectedMap);
          setResult(RESULT_OK, start);
          finish();
          break;
        case IMPORT_MAP:
          start.putExtra(EXTRA_FILE_PATH, selectedMap);
          start.setComponent(new ComponentName(getApplicationContext()
              .getPackageName(), Map.class.getName()));
          startActivity(start);
          break;
        }
      } else {
        // If no map was selected, display note message.
        displayMessage(getString(R.string.MSG_NO_MAP_SELECTED), false);
      }
    }
    return true;
  }

  private void displayMapFiles() {
    String[] files = MapFileHandler.getFileList();

    // If files equals null then storage is not readable.
    if (files == null) {
      displayMessage(getString(R.string.ERR_MSG_STORAGE_NOT_READABLE), true);
    } else {
      fileList = new ArrayAdapter<String>(this, R.layout.list_item,
          MapFileHandler.getFileList());
      lsMaps.setAdapter(fileList);
    }
  }

  /**
   * Displays a message in a dialog box.
   * 
   * @param message
   *          The message to display.
   * @param isError
   *          Indicates, whether message should be displayed as error.
   */
  private void displayMessage(String message, boolean isError) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(message);
    builder.setCancelable(false);
    builder.setNeutralButton(getString(R.string.TXT_OK),
        new DialogInterface.OnClickListener() {

          /**
           * Handles click on "ok" button of dialog
           * Simply closes the dialog.
           */
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    AlertDialog alert = builder.create();
    alert.show();
  }
}