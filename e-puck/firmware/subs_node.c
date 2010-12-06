#include <string.h>
#include <p30f6014A.h>

#include "hal_led.h"
#include "hal_motors.h"
#include "com.h"
#include "sen_line.h"

#include "subs_node.h"

enum {
	NODE_DETECTION__REQUIRED_MEASUREMENTS = 3, ///< Specifies the number of measurements, which have to provide data above a certain threshold for node-detection.
};

enum {
	NODE_TYPE__TOP_LEFT_EDGE = 0,
	NODE_TYPE__TOP_RIGHT_EDGE = 1,
	NODE_TYPE__BOTTOM_LEFT_EDGE = 2,
	NODE_TYPE__BOTTOM_RIGHT_EDGE = 3,
	NODE_TYPE__TOP_T = 4,
	NODE_TYPE__RIGHT_T = 5,
	NODE_TYPE__BOTTOM_T = 6,
	NODE_TYPE__LEFT_T = 7,
	NODE_TYPE__CROSS = 8,
	NODE_TYPE__UNKNOWN = 9
};

uint8_t ui8NodeDetectionCounter = 0; ///< Number of ground-sensor-measurements in a row, which provided data below a certain threshold.
sen_line_SData_t podSensorData = {{0}}; ///< Holds data of the three ground-sensors.
uint16_t ui16AvgLeft = 0; ///< Stores values of the left ground-sensor, which are several times below the threshold for line-detection.
uint16_t ui16AvgRight = 0; ///< Stores values of the right ground-sensor, which are several times below the threshold for line-detection.

/*!
 * \brief
 * Analyzes if the robot is above a node.
 * 
 * \returns
 * True if a node has been detected, false otherwise.
 * 
 * Checks the ground-sensors for data. If the robot is currently moving and more than one sensor delivers several times
 * critical values the robot is supposed to be above a node and computes the nodes shape.
 * After that a message with the shape of the node is created and sent to the Smartphone via BlueTooth.
 * The robot will stop with its center above the node and visualize its state.
 */
bool subs_node_run( void) {
	bool nodeHit = false;	
	sen_line_read( &podSensorData);

	// node detection-measurement
	// TODO sollte man diese Multiplikation mit 2 in einer Konstante ablegen und besser dokumentieren?
 	if ((2 * (podSensorData.aui16Data[0]) < 1) || // 1 wird ersetzt durch EEPROM-Kalibrierwert f�r linken Sensor �ber Linie
 		(2 * (podSensorData.aui16Data[2]) < 1)) { // 1 wird ersetzt durch EEPROM-Kalibrierwert f�r rechten Sensor �ber Linie
		ui16AvgLeft += podSensorData.aui16Data[0];
		ui16AvgRight += podSensorData.aui16Data[2];
		ui8NodeDetectionCounter++;
	} else {
		hal_motors_setSteps(0);
		ui16AvgLeft = 0;
		ui16AvgRight = 0;
		ui8NodeDetectionCounter = 0;
	}
	bool hasMoved = (hal_motors_getStepsLeft() > 0) && (hal_motors_getStepsRight() > 0);
	
	// robot is above a node
	if( (ui8NodeDetectionCounter >= NODE_DETECTION__REQUIRED_MEASUREMENTS) && hasMoved) {
		hal_motors_setSpeed( 0, 0); // @TODO hier muss man eventuell noch ein bisschen fahren, so dass der e-puck genau �ber dem Knoten steht
		hal_motors_setSteps( 0);
		nodeHit = true;		
		ui16AvgLeft = ui16AvgLeft / ui8NodeDetectionCounter;
		ui16AvgRight = ui16AvgRight / ui8NodeDetectionCounter;		

		// analyze the shape of the node
		uint8_t ui8NodeType;
		
		if( (ui16AvgLeft < 1) &&( ui16AvgRight < 1) && (2 * podSensorData.aui16Data[1] < 1) ) { // 1 wird ersetzt durch den jeweiligen EEPROM-Kalibrierwert
			ui8NodeType = NODE_TYPE__CROSS;
		} else if( (ui16AvgLeft < 1) &&( ui16AvgRight < 1)) {
			ui8NodeType = NODE_TYPE__TOP_T;
		} else if( (ui16AvgLeft < 1) && (2 * podSensorData.aui16Data[1] < 1)) {
			ui8NodeType = NODE_TYPE__RIGHT_T;
		} else if( (ui16AvgRight < 1) && (2 * podSensorData.aui16Data[1] < 1)) {
			ui8NodeType = NODE_TYPE__LEFT_T;
		} else if( ui16AvgLeft < 1) {
			ui8NodeType = NODE_TYPE__TOP_RIGHT_EDGE;
		} else if( ui16AvgRight < 1) {
			ui8NodeType = NODE_TYPE__TOP_LEFT_EDGE;
		}
		ui16AvgLeft = 0;
		ui16AvgRight = 0;
		
		// inform smartphone about node-detection
		com_SMessage_t podHitNodeMessage = { COM_MESSAGE_TYPE__RESPONSE_HIT_NODE, {0}};
		podHitNodeMessage.aui8Data[0] = ui8NodeType;
		com_send( &podHitNodeMessage);
	}
	return nodeHit;
}

/*!
 * \brief
 * Resets all values which are used to detect and analyze a node.
 * 
 * Clears the node-detection-counter and the last ground-sensor-measurement.
 */
void subs_node_reset( void) {
	ui8NodeDetectionCounter = 0;
	ui16AvgLeft = 0;
	ui16AvgRight = 0;
	memset( podSensorData.aui16Data, 0, sizeof(podSensorData.aui16Data));
}
