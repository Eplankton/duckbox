#include <SoftwareSerial.h>
#include "lib/draw.hpp"
#include "lib/lm35.hpp"
#include "lib/phtr.hpp"

#define rxPin 5
#define txPin 6

// Set up a new SoftwareSerial object
static SoftwareSerial mySerial = SoftwareSerial {rxPin, txPin};
extern volatile bool now_at_list;
extern LM35 lm35;
extern PHTR phtr;

inline void soft_serial_setup()
{
	// Define pin modes for TX and RX
	pinMode(rxPin, INPUT);
	pinMode(txPin, OUTPUT);

	// Set the baud rate for the SoftwareSerial object
	mySerial.begin(9600);
}

inline void send_env_info()
{
	if (now_at_list)
		return;
	mySerial.print("[");
	mySerial.print(lm35.read());
	mySerial.print(", ");
	mySerial.print(phtr.read());
	mySerial.println("]");
}

void wifi_part()
{
	Serial.println("BLE @Eplkt --> Wifi");
	scroll_str("Wifi ", 25);

	soft_serial_setup();
	while (!now_at_list) {
		send_env_info();
		delay(1000);
	}
	mySerial.end();
}