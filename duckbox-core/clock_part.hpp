#ifndef _CLOCK_
#define _CLOCK_

#include "LedControl.h"
#include "lib/draw.hpp"
#include "ble_part.hpp"

extern LedControl lc;
extern volatile bool now_at_list;

inline void print_runtime()
{
	Serial.print("[");
	Serial.print(millis());
	Serial.println("]");
}

void clock_part()
{
	// BLE HC-08 --> Serial TX/RX
	Serial.begin(9600);
	Serial.println("BLE @Eplkt --> Clock");
	scroll_str("Clock ", 25);

	clear_display();
	memset(global_ble_str, 0, sizeof(global_ble_str));// 清空global_str

	while (!now_at_list) {
		print_runtime();
		char* tmp = recv_ble();
		global_str_out(tmp, pmtr.read(30, 100));
	}
	Serial.end();
}

#endif