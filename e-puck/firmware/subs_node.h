#ifndef subs_node_h__
#define subs_node_h__

#include "common.h"
#include "subs_types.h"

enum {
	SUBS_NODE_LINE_THRESHOLD = 330, ///< Specifies the threshold for line-detection.
	SUBS_NODE_REQUIRED_MEASUREMENTS = 8, ///< Specifies the number of measurements, which have to provide data above a certain threshold for node-detection.
	SUBS_NODE_MOVE_ABOVE_CENTER = 230 ///< Specifies the number of steps which have to be performed by both motors to move the robot above the center of the node.
};

bool subs_node_run( void);

void subs_node_reset( void);

#endif /* subs_node_h__ */
