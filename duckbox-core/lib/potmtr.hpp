#ifndef _PMTR_
#define _PMTR_

#include "Arduino.h"
#include <stdint.h>

struct POTMTR
{
	uint8_t pin;

	POTMTR(uint8_t _pin)
	    : pin(_pin) {}

	inline int read(long out_min = 0, long out_max = 1023) const
	{
		return map(analogRead(pin), 0, 1023, out_min, out_max);
	};
};

#endif