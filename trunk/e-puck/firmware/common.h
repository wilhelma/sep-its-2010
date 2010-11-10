#ifndef comdef_h__
#define comdef_h__

#include <stddef.h>
#include <stdbool.h>
#include <stdint.h>

#ifndef FCY
#	warning "FCY (instruction cyle frequency) not defined; defaulting to 7.3728 MHz!"
#	define FCY 7372800
#endif

/*!
 * \brief
 * A marker to define denoting function parameters which are only read but not modified.
 * 
 * \see
 * OUT | INOUT
 */
#define IN

/*!
 * \brief
 * A marker to define denoting function parameters which are only modified but not read.
 * 
 * \see
 * IN | INOUT
 */
#define OUT

/*!
 * \brief
 * A marker to define denoting function parameters which are read and modified.
 * 
 * \see
 * IN | OUT
 */
#define INOUT


#define ISR					__attribute__( ( __interrupt__))
#define AUTO_PSV			__attribute__( ( auto_psv))
#define NO_AUTO_PSV			__attribute__( ( no_auto_psv))
#define USE_SHADOWING		__attribute__( ( shadow))


#endif /* comdef_h__ */
