#ifndef _DRAW_
#define _DRAW_

#include <avr/pgmspace.h>
#include "LedControl.h"
#include "binary.h"
#include "ascii_96.h"

#define SCROLL_SPEED 80
#define DISPLAY_INTENSITY 0

extern LedControl lc;
extern volatile bool now_at_list;

inline void display_init()
{
	// Power on
	lc.shutdown(0, false);

	// Set brightness to a medium value
	lc.setIntensity(0, DISPLAY_INTENSITY);

	// Clear the buffer
	lc.clearDisplay(0);
}

inline void clear_display()
{
	lc.clearDisplay(0);
}

inline void draw_by_row(const uint8_t pic[8])
{
	for (uint8_t i = 0; i < 8; i++) {
		lc.setRow(0, i, pic[i]);
	}
}

void scroll_sequence(const uint8_t* temp, uint32_t length, uint32_t delay_ms = SCROLL_SPEED / 2)
{
	uint8_t buffer[8] = {0};
	for (uint32_t i = 0; i < length; i += 8) {
		const uint8_t* bitmap = (length == 8) ? temp : &temp[i];
		for (uint8_t j = 0; j < 8; ++j) {
			if (now_at_list)
				return;

			// Shift the contents of the buffer to the left and set the rightmost column to the new character
			// Then update the LED matrix
			for (uint8_t k = 0; k < 8; ++k) {
				if (now_at_list)
					return;
				buffer[k] <<= 1;
				buffer[k] |= (bitmap[k] & (0x80 >> j)) >> (7 - j);
				lc.setRow(0, k, buffer[k]);
			}
			delay(delay_ms);
		}
	}
}

void scroll_icon(const uint8_t* icon, uint32_t delay_ms = SCROLL_SPEED)
{
	scroll_sequence(icon, 8, delay_ms);
}

void scroll_str(const char* text, uint32_t delay_ms = SCROLL_SPEED / 2)
{
	uint8_t temp[8 * strlen(text)];
	uint32_t len = 0;

	while (*text) {
		char ch = *text++;

		// 计算在ascii96数组中的索引
		const uint16_t index = ch - 32;

		// 添加图标到序列
		for (uint8_t i = 0; i < 8; i++) {
			temp[len++] = pgm_read_byte_near(&ascii_96[index][i]);
		}
	}

	// 滚动序列
	scroll_sequence(temp, len, delay_ms);
}

#endif