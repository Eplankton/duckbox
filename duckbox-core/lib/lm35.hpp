#ifndef _LM35_
#define _LM35_

#define LM35_PIN A1

struct LM35// 温敏电阻
{
	uint8_t pin;

	LM35(uint8_t _pin)
	    : pin(_pin) {}

	inline float read() const
	{
		//使用浮点数存储温度数据，温度数据由电压值换算得到，温度范围 0~100℃
		return ((5.0 * analogRead(pin)) / 1024) * 100.0;
	};
};

#endif