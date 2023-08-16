#include "LedControl.h"
#include "ble_part.hpp"
#include "lib/draw.hpp"
#include "lib/icon.hpp"
#include "lib/lm35.hpp"
#include "lib/phtr.hpp"

extern LedControl lc;
extern volatile bool now_at_list;

LM35 lm35 {LM35_PIN};
PHTR phtr {PHTR_PIN};

inline void print_env_info()
{
	if (now_at_list)
		return;
	Serial.print("[");
	Serial.print(lm35.read());
	Serial.print(", ");
	Serial.print(phtr.read());
	Serial.println("]");
}

void weather_part()
{
	Serial.begin(9600);
	Serial.println("BLE @Eplkt --> Weather");
	scroll_str("Weather ", 15);
	clear_display();
	memset(global_ble_str, 0, sizeof(global_ble_str));// 清空global_str

	int16_t temp;
	uint8_t cdt;
	char temp_str[6] = "";

	auto slct = [&cdt](int spd) {
		switch (cdt)
		{
			case 0:
				scroll_str("Sunny ", spd);
				scroll_icon(sun_icon);
				break;
			case 1:
				scroll_str("Clear ", spd);
				scroll_icon(sun_icon);
				break;
			case 2:
			case 3:
				scroll_str("Clear ", spd);
				scroll_icon(sun_icon);
				break;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
				scroll_str("Cloudy ", spd);
				scroll_icon(cloud_icon);
				break;
			case 9:
				scroll_str("Overcast ", spd);
				scroll_icon(cloud_icon);
				break;
			default:
				scroll_str("Rainy ", spd);
				scroll_icon(rain_icon);
				break;
		}
	};

	while (!now_at_list) {
		auto spd = pmtr.read(30, 100);
		auto res = recv_ble();
		if (sscanf(res, "[%d,%d]", &temp, &cdt) == 2) {
			print_env_info();
			itoa(temp, temp_str, 10);
			strcat(temp_str, " ");
			scroll_str(temp_str, spd);
			slct(spd);
		}
	}

	Serial.end();
}