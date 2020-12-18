String fileRead(String filename)
{
  if (!SPIFFS.exists(filename))
    return ",-12,1";
  File file = SPIFFS.open(filename, "r");
  String result = "";
  while (file.available())
    result += (char)file.read();
  file.close();
  return result;
}

void fileWrite(String filename, String data)
{
  File file = SPIFFS.open(filename, "w");
  file.print(data);
  file.close();
}
