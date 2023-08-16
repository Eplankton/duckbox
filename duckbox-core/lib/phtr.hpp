#ifndef _PHTR_
#define _PHTR_

#define PHTR_PIN A2

struct PHTR// 光敏电阻
{
	uint8_t pin;

	PHTR(uint8_t _pin)
	    : pin(_pin) {}

	inline float read() const
	{
		// 生成光照率，0~100%
		return 100.0 - (analogRead(pin) / 10.0);
	};
};

#endif