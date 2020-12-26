//#define DEBUG

#define LED_PIN 12

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <IRremoteESP8266.h>
#include <FS.h>

const char* selfSsid = "Control-X";
const char* selfPass = "controlx";

const String SSID_DAT = "ssid.dat";
const String PASS_DAT = "pass.dat";

ESP8266WebServer server(80);

IRsend irsend(14);

void setup()
{
  pinMode(LED_PIN, OUTPUT);

  digitalWrite(LED_PIN, HIGH);

  SPIFFS.begin();

  String ssid = fileRead(SSID_DAT);

  if (ssid == ",-12,1")
    ssid = "undefinedSSID";

  String pass = fileRead(PASS_DAT);

  if (pass == ",-12,1")
    pass = "undefinedPASS";
  
  #ifdef DEBUG
    Serial.begin(115200);
  #endif
  
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, pass);

  #ifdef DEBUG
    Serial.println();
    Serial.print("Connecting");
  #endif

  int counter = 0;
  
  while (WiFi.status() != WL_CONNECTED)
  {
    #ifdef DEBUG
      Serial.print(".");
    #endif
    delay(1000);
    counter++;
    if (counter >= 10)
      break;
  }

  if (counter >= 10)
  {
    WiFi.mode(WIFI_AP);
    WiFi.softAP(selfSsid, selfPass);

    server.on("/", HTTP_GET, handleSetupPage);
    server.on("/setup", HTTP_GET, handleSetupPage);
    server.on("/setup", HTTP_POST, handleSetup);
    server.onNotFound(handleNotFound);
    
    server.begin();
    
    return;
  }

  #ifdef DEBUG
    Serial.println();
    Serial.print("Connected to ");
    Serial.println(ssid);
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());
  #endif

  server.on("/", handleRoot);
  
  server.on("/samsung", handleSAMSUNG);
  server.on("/nec", handleNEC);
  server.on("/lg", handleLG);

  server.on("/raw", HTTP_POST, handleRAW);

  server.on("/update", HTTP_GET, handleUpdatePage);
  server.on("/update", HTTP_POST, []()
  {
    server.send(200, "text/plain", (Update.hasError()) ? "FAIL" : "OK");
    ESP.restart();
  }, handleUpdate);

  server.onNotFound(handleNotFound);

  irsend.begin();
  server.begin();

  #ifdef DEBUG
    Serial.println("Server started");
  #endif

  digitalWrite(LED_PIN, LOW);
}

void loop()
{
  server.handleClient();
}
