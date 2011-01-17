#include <string.h>
#include <stdlib.h>

#include "hal_rtc.h"
#include "hal_motors.h"
#include "hal_led.h"
#include "com.h"
#include "sen_line.h"
#include "sen_prox.h"
#include "subs_node.h"
#include "subs_abyss.h"
#include "subs_collision.h"
#include "subs.h"
#include "hal_uart1.h"

#include "conquest.h"

static const EType_t s_aaeMap[DIM_X][DIM_Y] = {
	{ TYPE__DOWN_RIGHT, TYPE__UP_T,		TYPE__UP_T,		TYPE__UP_T,		TYPE__DOWN_LEFT },
	{ TYPE__LEFT_T,		TYPE__CROSS,	TYPE__DOWN_T,	TYPE__CROSS,	TYPE__RIGHT_T },
	{ TYPE__LEFT_T,		TYPE__RIGHT_T,	TYPE__INVALID,	TYPE__LEFT_T,	TYPE__RIGHT_T },
	{ TYPE__LEFT_T,		TYPE__CROSS,	TYPE__UP_T,		TYPE__CROSS,	TYPE__RIGHT_T },
	{ TYPE__UP_RIGHT,	TYPE__DOWN_T,	TYPE__DOWN_T,	TYPE__DOWN_T,	TYPE__UP_LEFT }
};

static EDirection_t s_eDirection = DIRECTION_UP;
volatile uint16_t conquest_ui16Speed = INITIAL_SPEED;
static uint16_t s_ui16PosX = INITIAL_POS_X;
static uint16_t s_ui16PosY = INITIAL_POS_Y;

static bool cbHandleRequestStatus(
	IN const com_SMessage_t* const _lppodMessage
	);
static bool cbHandleRequestReset(
	IN const com_SMessage_t* const _lppodMessage
	);
static bool cbHandleRequestSetLED(
	IN const com_SMessage_t* const _lppodMessage
	);


static EType_t calculateRelativeNodeType( 
	IN const EType_t _eNodeType,
	IN const EDirection_t _eDirection
	);


EType_t calculateRelativeNodeType(
	IN const EType_t _eNodeType,
	IN const EDirection_t _eDirection
	) {

	EType_t eNode = _eNodeType;

	if( _eDirection != DIRECTION_UP) {

		// Rotate possible moving directions
		uint16_t ui16RawDirections = _eNodeType & 0xFF;
		if( _eDirection == DIRECTION_RIGHT) {
			ui16RawDirections <<= 6;
			ui16RawDirections |= ui16RawDirections >> 8;
		} else if( _eDirection == DIRECTION_DOWN) {
			ui16RawDirections <<= 4;
			ui16RawDirections |= ui16RawDirections >> 8;
		} else if( _eDirection == DIRECTION_LEFT) {
			ui16RawDirections <<= 2;
			ui16RawDirections |= ui16RawDirections >> 8;
		}

		// Reconstruct node type based on the possible moving directions
		switch( ui16RawDirections & 0xFF) {
			case DIRECTION_LEFT | DIRECTION_DOWN | DIRECTION_RIGHT: {
				eNode = TYPE__UP_T;
				break;
			}
			case DIRECTION_UP | DIRECTION_DOWN | DIRECTION_RIGHT: {
				eNode = TYPE__LEFT_T;
				break;
			}
			case DIRECTION_UP | DIRECTION_LEFT | DIRECTION_RIGHT: {
				eNode = TYPE__DOWN_T;
				break;
			}
			case DIRECTION_UP | DIRECTION_LEFT | DIRECTION_DOWN: {
				eNode = TYPE__RIGHT_T;
				break;
			}
			case DIRECTION_UP | DIRECTION_LEFT: {
				eNode = TYPE__UP_LEFT;
				break;
			}
			case DIRECTION_UP | DIRECTION_RIGHT: {
				eNode = TYPE__UP_RIGHT;
				break;
			}
			case DIRECTION_LEFT | DIRECTION_DOWN: {
				eNode = TYPE__DOWN_LEFT;
				break;
			}
			case DIRECTION_DOWN | DIRECTION_RIGHT: {
				eNode = TYPE__DOWN_RIGHT;
				break;
			}
			case DIRECTION_UP | DIRECTION_LEFT | DIRECTION_DOWN | DIRECTION_RIGHT: {
				eNode = TYPE__CROSS;
				break;
			}
			default: {
				eNode = TYPE__INVALID;
			}
		}
	}

	return eNode;
}


void conquest_init( void) {

	com_register( cbHandleRequestStatus);
	com_register( cbHandleRequestReset);
	com_register( cbHandleRequestSetLED);
}


bool cbDemoMessageHandler(
	IN const com_SMessage_t* _lppodMessage
	) {

	bool blAccepted = true;
	switch( _lppodMessage->ui16Type) {
		case CONQUEST_MESSAGE_TYPE__REQUEST_RESET: {
			s_eDirection = DIRECTION_UP;
			conquest_ui16Speed = INITIAL_SPEED;
			s_ui16PosX = INITIAL_POS_X;
			s_ui16PosY = INITIAL_POS_Y;
			hal_led_switchOff( TYPE__CROSS & 0xFF);
			hal_motors_setSpeed( 0, 0);

			com_SMessage_t podResponse;
			podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_OK;
			com_send( &podResponse);
			break;
		}
		case CONQUEST_MESSAGE_TYPE__REQUEST_STATUS: {
			com_SMessage_t podResponse;
			podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_STATUS;
			uint32_t ui32UpTime = hal_rtc_getSystemUpTime();
			memcpy( podResponse.aui8Data, &ui32UpTime, sizeof( ui32UpTime));
			memset( &podResponse.aui8Data[4], 0, 3 + 8);
			const EType_t eType = calculateRelativeNodeType( s_aaeMap[s_ui16PosY][s_ui16PosX], s_eDirection);
			podResponse.aui8Data[15] = eType >> 8;
			hal_led_set( eType & 0xFF);
			com_send( &podResponse);
			break;
		}
		case CONQUEST_MESSAGE_TYPE__REQUEST_TURN: {
			if( _lppodMessage->aui8Data[0]) {
				int16_t i16NewDir = ( ( ( (int8_t)_lppodMessage->aui8Data[0]) % 4) + 4) % 4;
				if( s_eDirection == DIRECTION_LEFT) {
					i16NewDir += 3;
				} else if( s_eDirection == DIRECTION_RIGHT) {
					i16NewDir++;
				} else if( s_eDirection == DIRECTION_DOWN) {
					i16NewDir += 2;
				}
				i16NewDir %= 4;
				if( !i16NewDir) {
					s_eDirection = DIRECTION_UP;
				} else if( i16NewDir == 1) {
					s_eDirection = DIRECTION_RIGHT;
				} else if( i16NewDir == 2) {
					s_eDirection = DIRECTION_DOWN;
				// i16NewDir == 3
				} else {
					s_eDirection = DIRECTION_LEFT;
				}

				for( uint16_t ui16 = 0; ui16 < abs( ( (int8_t)_lppodMessage->aui8Data[0]) % 4); ui16++) {
					hal_motors_setSpeed( 0, (int8_t)_lppodMessage->aui8Data[0] < 0 ? -conquest_ui16Speed : conquest_ui16Speed);
					hal_motors_setSteps( 0);
					while( hal_motors_getStepsLeft() < TURN_STEPS && hal_motors_getStepsRight() < TURN_STEPS)
						;
					hal_motors_setSpeed( 0, 0);
				}
			}

			hal_led_set( calculateRelativeNodeType( s_aaeMap[s_ui16PosY][s_ui16PosX], s_eDirection) & 0xFF);

			com_SMessage_t podResponse;
			podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_OK;
			com_send( &podResponse);
			break;
		}
		case CONQUEST_MESSAGE_TYPE__REQUEST_MOVE: {
			com_SMessage_t podResponse;
			if( s_aaeMap[s_ui16PosY][s_ui16PosX] & s_eDirection) {
				if( s_eDirection == DIRECTION_UP) {
					s_ui16PosY--;
				} else if( s_eDirection == DIRECTION_RIGHT) {
					s_ui16PosX++;
				} else if( s_eDirection == DIRECTION_DOWN) {
					s_ui16PosY++;

				// s_eDirection == DIRECTION_LEFT
				} else {
					s_ui16PosX--;
				}

//				hal_motors_setSpeed( conquest_ui16Speed, 0);
				hal_motors_accelerate( 200, 200, conquest_ui16Speed, conquest_ui16Speed);

				hal_motors_setSteps( 0);
				while( hal_motors_getStepsLeft() < FOWARD_STEPS && hal_motors_getStepsRight() < FOWARD_STEPS)
					;

//				hal_motors_setSpeed( 0, 0);
				hal_motors_accelerate( 200, 200, 0, 0);
				while( hal_motors_getSpeedLeft() && hal_motors_getSpeedRight())
					;

				podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_HIT_NODE;
				const EType_t eType = calculateRelativeNodeType( s_aaeMap[s_ui16PosY][s_ui16PosX], s_eDirection);
				podResponse.aui8Data[0] = eType >> 8;
				podResponse.aui8Data[1] = false;
				hal_led_set( eType & 0xFF);
			} else {
				podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_REJECTED;
			}
			com_send( &podResponse);
			break;
		}
		case CONQUEST_MESSAGE_TYPE__REQUEST_SET_SPEED: {
			com_SMessage_t podResponse;
			if( _lppodMessage->aui8Data[0] <= 100) {
				conquest_ui16Speed = 10 * _lppodMessage->aui8Data[0];
				podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_OK;
			} else {
				podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_REJECTED;
			}
			com_send( &podResponse);
			break;
		}
		case CONQUEST_MESSAGE_TYPE__REQUEST_SET_LED: {
			hal_led_set( _lppodMessage->aui8Data[0] | ( _lppodMessage->aui8Data[1] << 8));
			com_SMessage_t podResponse;
			podResponse.ui16Type = CONQUEST_MESSAGE_TYPE__RESPONSE_OK;
			com_send( &podResponse);
			break;
		}
		default: {
			blAccepted = false;
		}
	}

	return blAccepted;
}

/*!
 * \brief
 * Handles status-requests.
 * 
 * \param _lppodMessage
 * Specifies the message which has to be analyzed.
 * 
 * \returns
 * True, if a message has been handled by this function, false otherwise.
 *
 * Computes the type of the message by analyzing the first and second Byte of the message.
 * If this handler is responsible a message containing all current status-data will be created and sent to the smartphone.
 * 
 * \remarks
 * Handler-functions have to be registered.
 */
bool cbHandleRequestStatus(
	IN const com_SMessage_t* const _lppodMessage
	) {

	bool blHandledMessage = false;

	if( _lppodMessage->ui16Type == CONQUEST_MESSAGE_TYPE__REQUEST_STATUS) {
		com_SMessage_t podStatusResponse = { CONQUEST_MESSAGE_TYPE__RESPONSE_STATUS, { 0 } };

		// record system up-time in little endian format
		uint32_t ui32UpTime = hal_rtc_getSystemUpTime();
		memcpy( podStatusResponse.aui8Data, &ui32UpTime, sizeof( ui32UpTime));
		memset( &podStatusResponse.aui8Data[4], 0, 3 + 8);

		// record abyss status
		sen_line_SData_t podSensorData;
		sen_line_read( &podSensorData);
		sen_line_rescale( &podSensorData, &podSensorData);

		podStatusResponse.aui8Data[3 + SEN_LINE_SENSOR__LEFT] = podSensorData.aui16Data[SEN_LINE_SENSOR__LEFT] < SUBS_ABYSS_THRESHOLD;
		podStatusResponse.aui8Data[3 + SEN_LINE_SENSOR__MIDDLE] = podSensorData.aui16Data[SEN_LINE_SENSOR__MIDDLE] < SUBS_ABYSS_THRESHOLD;
		podStatusResponse.aui8Data[3 + SEN_LINE_SENSOR__RIGHT] = podSensorData.aui16Data[SEN_LINE_SENSOR__RIGHT] < SUBS_ABYSS_THRESHOLD;

		// record collision status
		sen_prox_SData_t podProxSensorData;
		sen_prox_getCurrent( &podProxSensorData);

		for( uint8_t i = 0; i < sizeof( podProxSensorData.aui8Data); i++) {
			if( podProxSensorData.aui8Data[i] > SUBS_COLLISION__PROX_THRESHOLD) {
				podStatusResponse.aui8Data[7 + i] = true;
			}
		}

		// record last visited node-type
		podStatusResponse.aui8Data[15] = subs_node_getCurrentNodeType();

		com_send( &podStatusResponse);
		hal_led_switchOn(1);

		blHandledMessage = true;
	}

	return blHandledMessage;
}

/*!
 * \brief
 * Handles reset-requests.
 * 
 * \param _lppodMessage
 * Specifies the message which has to be analyzed.
 * 
 * \returns
 * True, if a message has been handled by this function, false otherwise.
 *
 * Computes the type of the message by analyzing the first and second Byte of the message.
 * If this handler is responsible the robot will be reset to its default state.
 * 
 * \remarks
 * Handler-functions have to be registered during the reset function.
 */
bool cbHandleRequestReset(
	IN const com_SMessage_t* const _lppodMessage
	) {

	bool blHandledMessage = false;

	if( _lppodMessage->ui16Type == CONQUEST_MESSAGE_TYPE__REQUEST_STATUS) {
		subs_reset();
		hal_led_set( 0);

		const com_SMessage_t podResponseOk = { CONQUEST_MESSAGE_TYPE__RESPONSE_OK, { 0 } };
		com_send( &podResponseOk);
		blHandledMessage = true;
	}

	return blHandledMessage;
}

/*!
 * \brief
 * Handles SetLED-requests.
 * 
 * \param _lppodMessage
 * Specifies the message which has to be analyzed.
 * 
 * \returns
 * True, if a message has been handled by this function, false otherwise.
 *
 * Computes the type of the message by analyzing the first and second Byte of the message.
 * If this handler is responsible the LEDs which are specified in the first ten bytes of the message-data will be switched on, all other LEDs will be switched off.
 * 
 * \remarks
 * Handler-functions have to be registered during the reset function.
 */
bool cbHandleRequestSetLED(
	IN const com_SMessage_t* const _lppodMessage
	) {

	bool blHandledMessage = false;

	if( _lppodMessage->ui16Type == CONQUEST_MESSAGE_TYPE__REQUEST_SET_LED) {
		hal_led_set( _lppodMessage->aui8Data[0] | ( _lppodMessage->aui8Data[1] << 8));

		const com_SMessage_t podResponseOkMessage = { CONQUEST_MESSAGE_TYPE__RESPONSE_OK, { 0 } };
		com_send( &podResponseOkMessage);
		blHandledMessage = true;
	}

	return blHandledMessage;
}