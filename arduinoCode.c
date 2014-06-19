int led = 13;

//if UNO:
HardwareSerial serial = Serial;

//if LEONARDO:
//HardwareSerial serial = Serial1;

void setup() {  
  serial.begin(9600);
  pinMode(led, OUTPUT);
  
}
int readValue=0;

void loop() {
  
  readValue = analogRead(A1);
  serial.print(" ");
  serial.println(readValue,DEC);
  serial.flush();
  delay(1000);
}

void serialEvent() {
  while (serial.available()) {
    serial.read();
  }
  digitalWrite(led, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(50);               // wait for a second
  digitalWrite(led, LOW);    // turn the LED off by making the voltage LOW

}
