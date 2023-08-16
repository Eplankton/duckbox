// 引入 wifi 模块，并实例化，不同的芯片这里的依赖可能不同
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
static WiFiClient espClient;

// 引入阿里云 IoT SDK
#include <AliyunIoTSDK.h>

// 设置产品和设备的信息，从阿里云设备信息里查看
#define PRODUCT_KEY "j1gcy835zBQ"
#define DEVICE_NAME "zwq-esp8266"
#define DEVICE_SECRET "e95200079644ac8bdfa40727dff736f8"
#define REGION_ID "cn-shanghai"

// 设置 wifi 信息
// #define WIFI_SSID "ZWQ's Redmi" // 开手机热点时候，要切换成2.4GHz频段
#define WIFI_SSID "zhao new"
#define WIFI_PASSWD "aa369258147"

void setup() {
  Serial.begin(9600);

  // 初始化 wifi
  wifiInit(WIFI_SSID, WIFI_PASSWD);

  // 初始化 iot，需传入 wifi 的 client，和设备产品信息
  AliyunIoTSDK::begin(espClient, PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET, REGION_ID);
}

struct env_info {
  float temp = 0, lux = 0;
  void send_info() {
    auto str = Serial.readString();
    if (sscanf(str.c_str(), "[%f, %f]", &temp, &lux) == 2) {
      AliyunIoTSDK::send((char *)"CurrentTemperature", temp);
      AliyunIoTSDK::send((char *)"LightLux", lux);
    }
  }
} Env;

void loop() {
  AliyunIoTSDK::loop();
  Env.send_info();
}

// 初始化 wifi 连接
void wifiInit(const char *ssid, const char *passphrase) {
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, passphrase);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    // Serial.println("WiFi not Connect");
  }
  // Serial.println("Connected to AP");
}

// 电源属性修改的回调函数
void powerCallback(JsonVariant p) {
  int PowerSwitch = p["PowerSwitch"];
  if (PowerSwitch == 1) {
    // 启动设备
  }
}