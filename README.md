## DuckBox IoT System
![Input Image Description](pic/logo.PNG)

### **About:**

`DuckBox` is an intelligent IoT system consists of three main components: `DuckBox` as the main hardware board, `DuckBox Core` as the software running on `DuckBox`, and `DuckBox Helper` as the companion mobile application.

**Demo:**

As shown in the figure, the hardware system constructed on the breadboard on the left is `DuckBox`, and the mobile phone on the right is running the `DuckBox Helper` application.

- `STM32F103-BlackPill` as system motherboard
- `ESP32C3-DevKit` as WiFi communication
- `HC-08` as BLE communication
- `8x8 LED-Matrix` as Display

![a](pic/IMG_20230815_032151.jpg)

From left to right, the interfaces of `DuckBox Helper` include the device interface, function interface, and four modules: clock, weather, music, and games.

![b](pic/Screenshot_2023-08-16-00-40-54-580_com.max.blepro.jpg)

**APP Download:**

![Input Image Description](pic/apk.png)

**Hardware Schematic:**

![a](pic/hardware.jpg)

**`DuckBox Core`** Flowchart:

![d](pic/grgfd.jpg)