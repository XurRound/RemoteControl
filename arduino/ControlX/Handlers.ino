void handleRoot()
{
  server.send(200, "text/html", fileRead("/main.html"));
}

void handleUpdatePage()
{
  server.send(200, "text/html", fileRead("/update.html"));
}

void handleNotFound()
{
  String file = fileRead(server.uri());
  if (file != ",-12,1" && (server.uri().indexOf("ssid.dat") == -1) && server.uri().indexOf("pass.dat") == -1))
  {
    String mime = "text/plain";
    if (server.uri().endsWith(".html"))
      mime = "text/html";
    if (server.uri().endsWith(".css"))
      mime = "text/css";
    if (server.uri().endsWith(".js"))
      mime = "application/javascript";
    server.send(200, mime, file);
  }
  else
    server.send(404, "text/html", "Not found");
}

void handleNEC()
{
  digitalWrite(LED_PIN, HIGH);
  unsigned long code = strtoul(server.arg("code").c_str(), NULL, 16);
  irsend.sendNEC(code, 32);
  server.send(200, "text/html", String(code));
  digitalWrite(LED_PIN, LOW);
}

void handleSAMSUNG()
{
  digitalWrite(LED_PIN, HIGH);
  unsigned long code = strtoul(server.arg("code").c_str(), NULL, 16);
  irsend.sendSAMSUNG(code, 32);
  server.send(200, "text/html", String(code));
  digitalWrite(LED_PIN, LOW);
}

void handleLG()
{
  digitalWrite(LED_PIN, HIGH);
  unsigned long code = strtoul(server.arg("code").c_str(), NULL, 16);
  irsend.sendLG(code, 28);
  server.send(200, "text/html", String(code));
  digitalWrite(LED_PIN, LOW);
}

void handleRAW()
{
  if (!server.hasArg("freq") || !server.hasArg("code"))
  {
    server.send(200, "text/html", "Too few args");
    return;
  }
  digitalWrite(LED_PIN, HIGH);
  int freq = server.arg("freq").toInt() / 1000;
  String codeStr = server.arg("code");
  int count = 0;
  for (int i = 0; i < codeStr.length(); i++)
  {
    if (codeStr.charAt(i) == '|')
      count++;
  }
  unsigned int code[count];
  int r = 0;
  int t = 0;
  for (int i = 0; i < codeStr.length(); i++)
  {
    if (codeStr.charAt(i) == '|')
    {
      code[t] = codeStr.substring(r, i).toInt();
      r = i + 1;
      t++;
    }
  }
  String out = "";
  int codeSize = sizeof(code) / sizeof(int);
  for (int i = 0; i < codeSize; i++)
    out += String(code[i]);
  out += " " + String(freq);
  irsend.sendRaw(code, codeSize, freq);
  server.send(200, "text/html", out);
  digitalWrite(LED_PIN, LOW);
}

void handleSetup()
{
  if (!server.hasArg("ssid") || !server.hasArg("pass"))
  {
    server.send(200, "text/html", "Too few args");
    return;
  }
  fileWrite(SSID_DAT, server.arg("ssid"));
  fileWrite(PASS_DAT, server.arg("pass"));
  server.send(200, "text/html", "Setup complete");
  ESP.restart();
}

void handleUpdate()
{
  HTTPUpload& upload = server.upload();
  if(upload.status == UPLOAD_FILE_START)
  {
    digitalWrite(LED_PIN, HIGH);
    Serial.setDebugOutput(true);
    Serial.printf("Update: %s\n", upload.filename.c_str());
    uint32_t maxSketchSpace = (ESP.getFreeSketchSpace() - 0x1000) & 0xFFFFF000;
    if (!Update.begin(maxSketchSpace))
      Update.printError(Serial);
  }
  else if(upload.status == UPLOAD_FILE_WRITE)
  {
    if (Update.write(upload.buf, upload.currentSize) != upload.currentSize)
      Update.printError(Serial);
  }
  else if(upload.status == UPLOAD_FILE_END)
  {
    if (Update.end(true))
      Serial.printf("Update Success: %u\n", upload.totalSize);
    else
      Update.printError(Serial);
    Serial.setDebugOutput(false);
    digitalWrite(LED_PIN, LOW);
  }
  yield();
}
