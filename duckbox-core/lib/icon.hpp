#ifndef _ICON_
#define _ICON_

#include "draw.hpp"

struct Icon_t
{
	using IconPtr = const uint8_t*;
	const IconPtr icon;

	inline void draw() const
	{
		if (icon != NULL)
			draw_by_row(icon);
	}

	inline void scroll() const
	{
		if (icon != NULL)
			scroll_icon(icon);
	}
};

// App icon
constexpr uint8_t wthr_icon[] = {0x38, 0x46, 0x81, 0x81, 0x7E, 0x54, 0x00, 0x54};
constexpr uint8_t game_icon[] = {0x7F, 0xFF, 0xC0, 0xCF, 0xCF, 0xC3, 0xFF, 0x7F};
constexpr uint8_t clk_icon[] = {0x7E, 0x89, 0xA9, 0x99, 0x89, 0x81, 0x81, 0x7E};
constexpr uint8_t test_icon[] = {0xFF, 0xFF, 0x99, 0x18, 0x18, 0x18, 0x18, 0x3C};
constexpr uint8_t music_icon[] = {0x3F, 0x7F, 0x63, 0x63, 0xE3, 0xE7, 0xE7, 0x07};
constexpr uint8_t wifi_icon[] = {0x40, 0xE0, 0x40, 0xE1, 0x45, 0x55, 0x55, 0x55};
constexpr uint8_t off_icon[] = {0x18, 0x5A, 0x99, 0x99, 0x99, 0x81, 0x42, 0x3C};

constexpr uint8_t failed_icon[] = {0x00, 0x42, 0x24, 0x18, 0x18, 0x24, 0x42, 0x00};
constexpr uint8_t hello_icon[] = {0x00, 0x52, 0x52, 0x10, 0x18, 0x42, 0x3C, 0x00};
constexpr uint8_t bye_icon[] = {0x18, 0x28, 0xE8, 0x87, 0x80, 0x80, 0xFF, 0x00};

// weather icon
constexpr uint8_t rain_icon[] = {0x38, 0x7E, 0xFF, 0xFF, 0x7E, 0x54, 0x00, 0x54};
constexpr uint8_t cloud_icon[] = {0x00, 0x38, 0x46, 0x81, 0x81, 0x7E, 0x00, 0x00};
constexpr uint8_t sun_icon[] = {0x91, 0x42, 0x18, 0x3D, 0xBC, 0x18, 0x42, 0x89};

#endif