#ifndef _BUZZER_
#define _BUZZER_

#include "pitches.h"
#define BUZZER_PIN 8

struct Mpo
{
	uint16_t m;
	uint16_t d;

	inline void sing() const
	{
		const int duration = 1000 / d;
		tone(BUZZER_PIN, m, duration);
		delay(duration * 1.30);
		noTone(BUZZER_PIN);
	}
};

struct Buzzer
{
	uint8_t pin;

	Buzzer(uint8_t _pin)
	    : pin(_pin)
	{
		init();
	}

	~Buzzer()
	{
		pinMode(pin, INPUT);
		digitalWrite(pin, LOW);
	}

	inline void init()
	{
		pinMode(pin, OUTPUT);// Turn it into input mode, so no noise
	}
};

#endif