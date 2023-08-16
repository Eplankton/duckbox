#include "LedControl.h"
#include "ble_part.hpp"
#include "lib/potmtr.hpp"
#include "lib/buzzer.hpp"
#include "lib/icon.hpp"

#define PADDLE_HEIGHT 3
#define BALL_SPEED 75// The smaller the number, the faster the paddle moves

extern LedControl lc;
extern volatile bool now_at_list;
extern POTMTR pmtr;

struct Ball
{
	int8_t x, y;
	int8_t dx, dy;
};

static Ball ball {4, 3, -1, -1};
static char gameData[16];
int win_or_lose = 0;

void draw_paddle(int8_t position, int8_t column)
{
	for (int8_t i = 0; i < 8; ++i) {
		if (i >= position && i < position + PADDLE_HEIGHT) {
			lc.setLed(0, i, column, true);
		}
		else {
			lc.setLed(0, i, column, false);
		}
	}
}

inline void draw_ball()
{
	lc.setLed(0, ball.y, ball.x, true);
}

void move_ball()
{
	lc.setLed(0, ball.y, ball.x, false);
	ball.x += ball.dx;
	ball.y += ball.dy;

	if (ball.x == 0 || ball.x == 7) {
		ball.dx *= -1;
	}
	if (ball.y == 0 || ball.y == 7) {
		ball.dy *= -1;
	}
}

inline void send_game_info(int8_t bx, int8_t by, int8_t p1)
{
	constexpr auto size = sizeof(gameData);
	memset(gameData, '\0', size);
	sprintf(gameData, "[%d,%d,%d]\n", bx, by, p1);
	Serial.print(gameData);
}

bool handle_collision(int8_t player, int8_t player_number)
{
	constexpr Mpo pong {NOTE_C4, 16};

	auto fail_over = [player_number]() {
		constexpr Mpo failed_sounds {NOTE_E5, 2};
		if (player_number == 1) {// player1 failed
			draw_by_row(failed_icon);
			win_or_lose++;
		}
		else if (player_number == 2) {// player2 failed
			draw_by_row(hello_icon);
			win_or_lose--;
		}
		send_game_info(-1, -1, win_or_lose);
		failed_sounds.sing();
		delay(500);
	};

	if (ball.y < player || ball.y >= player + PADDLE_HEIGHT) {
		// Game over, call fail_over and return true to break the loop
		fail_over();
		return true;
	}
	else {
		// Change the direction of the ball depending on where it hits the paddle
		int8_t hit_position = ball.y - player;
		if (hit_position < PADDLE_HEIGHT / 2) {
			ball.dy *= -1;
		}
		else {
			ball.dy *= 1;
		}

		// Play the pong sound when the ball hits the paddle
		pong.sing();

		return false;
	}
}

void game_part()
{
	Serial.begin(9600);
	Serial.println("BLE @Eplkt --> Game");
	scroll_str("Game ", 25);

	Buzzer bz {BUZZER_PIN};
	win_or_lose = 0;

	while (!now_at_list)
	{
		// Reset the game state
		clear_display();
		ball = {4, 3, -1, -1};

		while (!now_at_list) {
			int8_t player1 = pmtr.read(0, 6);// Adjust the paddle position
			if (player1 > 5) {
				// 5 is the maximum value for the paddle position to ensure the paddle doesn't go off the screen
				player1 = 5;
			}

			// 添加第二个球拍的位置调整逻辑
			static int8_t player2 = 3;
			char* tmp = recv_ble();
			if (tmp != NULL) {
				player2 = 5 - (tmp[0] - 48);
				if (player2 > 5) {
					player2 = 5;
				}
			}

			draw_paddle(player1, 0);
			draw_paddle(player2, 7);// 第二个球拍在LED矩阵的另一边
			move_ball();
			draw_ball();

			send_game_info(ball.x, ball.y, player1);

			// Check if the ball hits the paddle
			if (ball.x == 0) {
				if (handle_collision(player1, 1))
					break;
			}
			else if (ball.x == 7) {
				if (handle_collision(player2, 2))
					break;
			}
			delay(BALL_SPEED);
		}
	}

	Serial.end();
}