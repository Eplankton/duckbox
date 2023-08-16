#ifndef _BLE_
#define _BLE_

#include <Stream.h>
#include "lib/draw.hpp"
#include "lib/potmtr.hpp"

extern volatile bool now_at_list;
extern POTMTR pmtr;

char global_ble_str[32];
static bool new_str = false;

char* recv_ble()
{
	uint8_t index = 0;// Index to keep track of the current position in the array

	while (Serial.available() > 0) {
		char c = (char) Serial.read();
		if (c == '\n') {                     // assuming '\n' as the end of string
			if (index > 0) {
				global_ble_str[index] = '\0';// Null terminate the string
				return global_ble_str;       // Return the address of the string
			}
		}
		else {
			if (index == 0) {                                     // 如果是新的字符
				memset(global_ble_str, 0, sizeof(global_ble_str));// 清空global_str
			}
			if (index < sizeof(global_ble_str) - 1) {             // Ensure we don't overflow the buffer
				global_ble_str[index++] = c;
			}
			delay(10);
		}
	}

	return NULL;// Return NULL if no string is received
}

void global_str_out(const char* tmp, uint32_t delay_ms = 40U)
{
	if (tmp != NULL) {
		scroll_str(tmp, delay_ms);
	}
	else if (global_ble_str[0] != '\0') {    // 如果global_str不为空
		scroll_str(global_ble_str, delay_ms);// 重复输出已经存在的字符串
	}
}

void ble_part()
{
	Serial.begin(9600);
	Serial.println("BLE @Eplkt --> Test");
	scroll_str("Test ", 25);
	clear_display();
	memset(global_ble_str, 0, sizeof(global_ble_str));// 清空global_str

	while (!now_at_list) {
		char* tmp = recv_ble();
		if (tmp != NULL) {
			Serial.print("Reply-> ");
			Serial.println(global_ble_str);
		}
		global_str_out(tmp, pmtr.read(30, 100));
	}
	Serial.end();
}

#endif