#include "hal_motors.h"
#include "hal_led.h"
#include "com.h"
#include "com_types.h"

#include "subs_movement.h"

com_EMessageType_t podCurrentMessageType; ///< Specifies the last smartphone-message-type.
int16_t i16CurrentLineSpeed = 600; ///< Stores the current movement-speed of the robot. Default: 600 steps per second.
int16_t i16CurrentAngularSpeed = 0; ///< Stores angular-speed for turning in left or right direction.

static bool cbHandleRequestMove( IN const com_SMessage_t* const _lppodMessage);
static bool cbHandleRequestTurn( IN const com_SMessage_t* const _lppodMessage);
static bool cbHandleRequestSetSpeed( IN const com_SMessage_t* const _lppodMessage);

/*!
 * \brief
 * Executes movement-commands sent by the smartphone.
 * 
 * \returns
 * True if a movement-command is going to be performed, false otherwise.
 *
 * Checks if the bluetooth-message-queue contains a message for moving or turning.
 * If there is one the robot starts to perform the demanded movement and sends an acknowledgment to the smartphone and deletes this message.
 */
bool subs_movement_run( void) {
	bool blMovementChanged = false;
	com_SMessage_t podOkMessage = { COM_MESSAGE_TYPE__RESPONSE_OK, {0}};

	switch( podCurrentMessageType) {
		case COM_MESSAGE_TYPE__REQUEST_TURN: {
			hal_motors_setSpeed( i16CurrentLineSpeed, i16CurrentAngularSpeed);
			// bis Liniensensoren "x-mal" ausgeschlagen haben
			blMovementChanged = true;
			com_send( &podOkMessage);
			break;
		}
		case COM_MESSAGE_TYPE__REQUEST_MOVE: {
			hal_motors_setSpeed( i16CurrentLineSpeed, i16CurrentAngularSpeed);
			blMovementChanged = true;
			com_send( &podOkMessage);
			break;
		}
		case COM_MESSAGE_TYPE__REQUEST_SET_SPEED: {
			blMovementChanged = true;
			com_send( &podOkMessage);
			break;
		}
		default: {
			
		}
	}
	return blMovementChanged;
}

/*!
 * \brief
 * Handles turn-requests.
 * 
 * \param _podMessage
 * Specifies the message which has to be analyzed.
 * 
 * \returns
 * True, if a message has been handled by this function, false otherwise.
 *
 * Computes the type of the message by analyzing the first and second Byte of the message.
 * If this handler is responsible the number of turns is analyzed. Here the first Byte of the transmitted data is a signed Integer.
 * A positive Integer indicates the number of 90�-turns in clockwise-rotation, negative Integer indicate the number of 90�-turns in counterclockwise-rotation.
 * 
 * \remarks
 * Handler-functions have to be registered during the reset function.
 *
 * \see
 * cbHandleRequestMove | cbHandleRequestSetSpeed
 */
bool cbHandleRequestTurn(
	IN const com_SMessage_t* const _lppodMessage
	) {
	bool blHandledMessage = false;
	
	if( _lppodMessage->eType == COM_MESSAGE_TYPE__REQUEST_TURN) {
		podCurrentMessageType = COM_MESSAGE_TYPE__REQUEST_TURN;
		//int8_t i8NumberOfTurns = _lppodMessage->aui8Data[0];
		//i16CurrentAngularSpeed = ;
		i16CurrentLineSpeed = 0;
		blHandledMessage = true;
	}
	return blHandledMessage;
}

/*!
 * \brief
 * Handles move-requests.
 * 
 * \param _podMessage
 * Specifies the message which has to be analyzed.
 * 
 * \returns
 * True, if a message has been handled by this function, false otherwise.
 *
 * Computes the type of the message by analyzing the first and second Byte of the message.
 * If this handler is responsible 
 * 
 * \remarks
 * Handler-functions have to be registered during the reset function.
 *
 * \see
 * cbHandleRequestTurn | cbHandleRequestSetSpeed
 */
bool cbHandleRequestMove(
	IN const com_SMessage_t* const _lppodMessage
	) {
	bool blHandledMessage = false;

	if( _lppodMessage->eType == COM_MESSAGE_TYPE__REQUEST_MOVE) {
		podCurrentMessageType = COM_MESSAGE_TYPE__REQUEST_MOVE;
		blHandledMessage = true;
	}
	return blHandledMessage;
}

/*!
 * \brief
 * Handles setSpeed-requests.
 * 
 * \param _podMessage
 * Specifies the message which has to be analyzed.
 * 
 * \returns
 * True, if a message has been handled by this function, false otherwise.
 *
 * Computes the type of the message by analyzing the first and second Byte of the message.
 * If this handler is responsible the first 8 bit of the transmitted data represent an Integer which delivers the new line-speed after it is multiplied with 10.
 * 
 * \remarks
 * Handler-functions have to be registered during the reset function.
 *
 * \see
 * cbHandleRequestMove | cbHandleRequestTurn | cbHandleRequestSetLED
 */
bool cbHandleRequestSetSpeed(
	IN const com_SMessage_t* const _lppodMessage
	) {
	bool blHandledMessage = false;

	if( _lppodMessage->eType == COM_MESSAGE_TYPE__REQUEST_SET_SPEED) {
		podCurrentMessageType = COM_MESSAGE_TYPE__REQUEST_SET_SPEED;
		i16CurrentLineSpeed = _lppodMessage->aui8Data[0] * 10;
		i16CurrentAngularSpeed = 0;
		blHandledMessage = true;
	}
	return blHandledMessage;
}

// /*!
//  * \brief
//  * Handles setLED-requests.
//  * 
//  * \param _podMessage
//  * Specifies the message which has to be analyzed.
//  * 
//  * \returns
//  * True, if a message has been handled by this function, false otherwise.
//  *
//  * Computes the type of the message by analyzing the first and second Byte of the message.
//  * If this handler is responsible the movement is performed.
//  * 
//  * \remarks
//  * Handler-functions have to be registered during the reset function.
//  *
//  * \see
//  * cbHandleRequestMove | cbHandleRequestTurn | cbHandleRequestSetSpeed
//  */
// bool cbHandleRequestSetLED( com_SMessage_t* _lppodMessage) {
// 	bool handledMessage = false;
// 
// 	if( _lppodMessage.eType == COM_MESSAGE_TYPE__REQUEST_SET_LED) {
// 
// 	}
// 	return handledMessage;
// }


/*!
 * \brief
 * Resets all movement data.
 * 
 * Registers all handler of this subsumption-layer for the Chain-of-Responsibility pattern.
 */
void subs_movement_reset( void) {
	i16CurrentLineSpeed = 600;
	i16CurrentAngularSpeed = 0;
	podCurrentMessageType = 0;
	com_register( cbHandleRequestTurn);
	com_register( cbHandleRequestMove);
	com_register( cbHandleRequestSetSpeed);
}
