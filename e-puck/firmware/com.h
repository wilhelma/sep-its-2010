#ifndef com_h__
#define com_h__

#include "common.h"

#include "com_types.h"


enum {
	COM_MAX_HANDLERS = 16 ///< Specifies the maximal amount of message handler callbacks without the default handler.
};


void com_init(
	IN OPT const com_fnConnectionEvent_t _fnConnectionEvent
	);

void com_processIncoming( void);

void com_send(
	IN const com_SMessage_t* const _lppodMessage
	);

bool com_register(
	IN const com_fnMessageHandler_t _fnHandler
	);

void com_setDefault(
	IN OPT const com_fnMessageHandler_t _fnDefaultHandler
	);

void com_unregister(
	IN const com_fnMessageHandler_t _fnHandler
	);

#endif /* com_h__ */
