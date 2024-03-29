#ifndef subs_calibration_h__
#define subs_calibration_h__

#include "common.h"


enum {
	SUBS_CALIBRATION_SELECTOR = 0, ///< Specifies the e-puck selector position which triggers a calibration.
	SUBS_CALIBRATION_DISTANCE = 300 ///< Specifies the distance to drive before measuring the white level.
};


bool subs_calibration_run( void);

void subs_calibration_reset( void);

#endif /* subs_calibration_h__ */
