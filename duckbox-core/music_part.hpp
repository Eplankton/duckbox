#include <math.h>

#include "lib/draw.hpp"
#include "lib/icon.hpp"
#include "ble_part.hpp"
#include "lib/buzzer.hpp"

extern LedControl lc;
extern volatile bool now_at_list;

constexpr Mpo NokiaTune[] = {
        {NOTE_E5, 8},
        {NOTE_D5, 8},
        {NOTE_FS4, 4},
        {NOTE_GS4, 4},
        {NOTE_CS5, 8},
        {NOTE_B4, 8},
        {NOTE_D4, 4},
        {NOTE_E4, 4},
        {NOTE_B4, 8},
        {NOTE_A4, 8},
        {NOTE_CS4, 4},
        {NOTE_E4, 4},
        {NOTE_A4, 2},
};

constexpr uint8_t BASE_PATTERN[] = {
        0x07,// -----###
        0xFF,// ########
        0x0F,// -#######
        0x3F,// --######
        0x0F,// ----####
        0x7F,// -#######
        0x07,// -----###
        0x01 // -------#
};

inline uint8_t* magic(uint16_t note, uint8_t img[8])
{
	for (uint8_t i = 0; i < 8; i++) {
		if (now_at_list)
			return;
		// 使用正弦函数生成波浪形状
		uint8_t h;
		if (i % 2)
			h = (sin((note + i + BASE_PATTERN[i]) * M_PI / 3) + 1) * 4;
		else
			h = (cos((note - i + BASE_PATTERN[7 - i]) * M_PI / 3) + 1) * 4;
		// 生成柱状图
		img[i] = (1 << h) - 1;
	}

	return img;
}

void music_part()
{
	Serial.begin(9600);
	Serial.println("BLE @Eplkt --> Music");
	scroll_str("Music ", 25);
	clear_display();

	Buzzer bz {BUZZER_PIN};
	uint8_t img_buf[8] = "";
	Mpo note;

	while (!now_at_list) {
		auto tmp = recv_ble();
		if (sscanf(tmp, "[%d, %d]", &note.m, &note.d) == 2) {
			Icon_t {magic(note.m, img_buf)}.draw();
			note.sing();
		}
	}

	Serial.end();
}