package sep.conquest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @class Connect
 * @brief 
 * @author Andreas Poxrucker
 * 
 * Searches for available e-puck robots and lists them. The user can select
 * robots and connect to them via Bluetooth.
 *
 */
public class Connect extends Activity {

  /**
   * Used to identify message to enable Bluetooth
   */
  private final int REQUEST_ENABLE_BLUETOOTH = 0;
  
  /**
   * Reference on BluetoothAdapter of the device
   */
  private BluetoothAdapter bluetoothAdapter;

  /**
   * Receives and handles broadcast messages
   */
  private BluetoothBroadcastReceiver bcReceiver;

  // Specifies the types of messages the bcReceiver should handle
  private IntentFilter messageFilter;

  // Saves Bluetooth devices found by the BluetoothAdapter
  private java.util.Map<String, BluetoothDevice> discoveredRobots;

  // Saves robots selected to open a Bluetooth connection
  // The key is the Bluetooth name of the device
  private java.util.Set<String> selectedRobots;

  // Used to display names of discovered devices in the ListView
  private ArrayAdapter<String> robotList;

  // Starts new search for available Bluetooth devices
  private Button btnSearch;

  // Displays the discovered devices
  private ListView lsRobots;

  // Indeterminate progress dialog shown during Bluetooth search and connet
  // action
  private ProgressDialog pDialog;

  /**
   * Overrides Activity.onCreate().
   * 
   * Initializes layout of Activity, its control elements, the BroadcastReceiver
   * and the Bluetooth-Adapter of the device.
   */
  public void onCreate(Bundle savedInstanceState) {

    // Call constructor of super class
    super.onCreate(savedInstanceState);

    // Set layout of Activity
    setContentView(R.layout.main_connect);

    // Set up BroadcastReceiver and message filter
    initializeBroadcastReceiver();

    // Get references on control elements and set EventListener
    initializeControlElements();

    // Set up Bluetooth
    initializeBluetooth();

    // Initialize fields saving the discovered devices and the selected
    discoveredRobots = new HashMap<String, BluetoothDevice>();
    selectedRobots = new HashSet<String>();
  }

  public void onResume() {
    super.onResume();
    registerReceiver(bcReceiver, messageFilter);
  }

  public void onPause() {
    unregisterReceiver(bcReceiver);
    super.onPause();
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
      if (resultCode == RESULT_CANCELED) {
        displayMessage("Error: Bluetooth has not been turned on. Bluetooth must be enabled to search for robots.");
      }
    }
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_connect, menu);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {

    // Intent message to start other Activities.
    Intent start = new Intent();

    switch (item.getItemId()) {

    // If "Connect" has been chosen, check if no robot is selected. In this case
    // display a hint message. Otherwise start connecting to robots one by one.
    // If all are successfully connected, start Map-Activity via Intent message.
    case R.id.mnu_connect:
      
      if (selectedRobots.size() > 0) {
        
        for (String device : selectedRobots) {
          ConnectHandler.connect(discoveredRobots.get(device));
        }

        start.putExtra(ApplicationMode.class.getName(),
            ApplicationMode.EXPLORATION);
        start.setComponent(new ComponentName(getApplicationContext()
            .getPackageName(), Map.class.getName()));
        startActivity(start);
      } else {
        displayMessage("No robot selected!");
      }
      break;

    // If "Simulation" has been chosen, start Simulation-Activity via Intent.
    case R.id.mnu_simulation:
      start.setComponent(new ComponentName(getApplicationContext()
          .getPackageName(), Simulation.class.getName()));
      startActivity(start);
      break;

    // If "Import" has been chosen, start Import-Activity via Intent.
    case R.id.mnu_import:
      start.setComponent(new ComponentName(getApplicationContext()
          .getPackageName(), Import.class.getName()));
      startActivity(start);
      break;
    }
    return true;
  }

  /**
   * @brief Initialize control elements of the activity and set the EventListener.
   */
  private void initializeControlElements() {

    // Get reference on "Search"-Button and set OnClickListener
    btnSearch = (Button) findViewById(R.id.btn_search);
    btnSearch.setOnClickListener(new mOnClickListener());

    // Get reference on ListView and set OnItemClickListener
    lsRobots = (ListView) findViewById(R.id.ls_robots);
    lsRobots.setOnItemClickListener(new mOnItemClickListener());

    // Initialize ArrayAdapter holding data for ListView
    robotList = new ArrayAdapter<String>(this, R.layout.list_item_connect);
  }

  /**
   * Initializes the BroadcastReceiver needed to receive Broadcast messages of
   * the Android System.
   */
  private void initializeBroadcastReceiver() {

    // Instantiate BroadcastReceiver
    bcReceiver = new BluetoothBroadcastReceiver();

    // Instantiate IntentFilter and set it's message filter
    messageFilter = new IntentFilter();
    messageFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    messageFilter.addAction(BluetoothDevice.ACTION_FOUND);
  }

  /**
   * Requests the Bluetooth-Adapter of the device and checks whether device
   * supports Bluetooth.
   */
  private void initializeBluetooth() {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // If bluetoothAdapter equals null then Smartphone does not support
    // Bluetooth.
    // In this case, display message and disable "Search"-Button.
    // Otherwise check, if Bluetooth is enabled.
    if (bluetoothAdapter == null) {
      displayMessage("Error: Device does not support Bluetooth.");
      btnSearch.setEnabled(false);
    } else if (!bluetoothAdapter.isEnabled()) {
      enableBluetooth();
    }
  }

  /**
   * Starts an Activity to enable the Bluetooth-Adapter of the Smartphone.
   */
  private void enableBluetooth() {

    // Set up an Intent message, set it's action to request enable Bluetooth
    Intent intent = new Intent();
    intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
  }

  private void displayMessage(String message) {
    Toast mtoast = Toast.makeText(this, message, Toast.LENGTH_LONG);
    mtoast.setGravity(Gravity.CENTER, 0, 0);
    mtoast.show();
  }

  private final class BluetoothBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        BluetoothDevice device = (BluetoothDevice) intent
            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        discoveredRobots.put(device.getName(), device);
        robotList.add(device.getName());

      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        lsRobots.setAdapter(robotList);
        pDialog.dismiss();
      }
    }
  }

  /**
   * ItemClickListener for ListView lsRobots.
   */
  private final class mOnItemClickListener implements OnItemClickListener {

    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {
      TextView txtDevice = (TextView) view;
      String deviceName = txtDevice.getText().toString();

      if (selectedRobots.contains(deviceName)) {
        selectedRobots.remove(deviceName);
      } else {
        selectedRobots.add(deviceName);
      }
    }
  }

  private final class mOnClickListener implements OnClickListener {

    public void onClick(View v) {
      int viewId = v.getId();

      switch (viewId) {

      // If btnSearch has been clicked, check, if Bluetooth is enabled.
      // If not, start Bluetooth enable activity, otherwise clear all fields
      // and start searching
      case R.id.btn_search:
        if (!bluetoothAdapter.isEnabled()) {
          enableBluetooth();
        } else {
          discoveredRobots.clear();
          robotList.clear();
          selectedRobots.clear();
          pDialog = ProgressDialog.show(Connect.this, "Searching...",
              "Searching for e-puck robots...", true);
          bluetoothAdapter.startDiscovery();
        }
      }
    }
  }
}