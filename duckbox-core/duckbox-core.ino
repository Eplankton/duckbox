#include "lib/draw.hpp"
#include "lib/icon.hpp"
#include "lib/task.hpp"
#include "game_part.hpp"
#include "weather_part.hpp"
#include "clock_part.hpp"
#include "ble_part.hpp"
#include "music_part.hpp"
#include "wifi_part.hpp"

#define DIN 11
#define CLK 13
#define CS 10
#define KEY1 2

LedControl lc {DIN, CLK, CS, 0};// 8x8 LED Matrix
POTMTR pmtr {A0};               // 旋钮
volatile int pmtr_res = 0;
volatile bool now_at_list = true;

struct App_t
{
	const Task_t tk;
	const Icon_t ic;

	inline void run() const { tk.run(); }
	inline void draw() const { ic.draw(); }
};

inline void K1_ISR()
{
	now_at_list = !now_at_list;
}

inline void home_btn_init()
{
	pinMode(KEY1, INPUT_PULLUP);
	attachInterrupt(digitalPinToInterrupt(KEY1), K1_ISR, RISING);
}

inline void boot_info()
{
	scroll_str("Hi ", 20);
	scroll_icon(hello_icon);

	Buzzer bz {BUZZER_PIN};
	for (auto& n: NokiaTune) {
		if (now_at_list)
			return;
		n.sing();
	}
}

void off_part()
{
	lc.shutdown(0, true);
	while (!now_at_list)// Do nothing
		;
	display_init();
}

void setup()
{
	home_btn_init();// K1 interrupt
	display_init(); // 8x8 LED Display Init

	now_at_list = false;
	boot_info();
	now_at_list = true;// Enter Menu
}

constexpr App_t apps[] = {
        {weather_part, wthr_icon},
        {game_part, game_icon},
        {clock_part, clk_icon},
        {music_part, music_icon},
        {wifi_part, wifi_icon},
        {off_part, off_icon},
        {off_part, off_icon},
        {NULL, NULL},
};

void loop()
{
	constexpr auto app_cnt = sizeof(apps) / sizeof(App_t) - 1;
	pmtr_res = pmtr.read(0, app_cnt);
	delay(50);
	if (now_at_list) {
		apps[pmtr_res].draw();
	}
	else {
		apps[pmtr_res].run();
	}
}