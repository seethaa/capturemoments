#include <SoftwareSerial.h>

#include <Servo.h> 
 
 
Servo myservo;  // create servo object to control a servo 
                // a maximum of eight servo objects can be created 
 
 int pos=0;    // variable to store the servo position 
 int output = 9;

int bluetoothTx = 1;
int bluetoothRx = 0;

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);
void setup() 
{ 
  myservo.attach(9);  // attaches the servo on pin 9 to the servo object 
  //Serial.begin(115200);
  
  bluetooth.begin(115200);  // The Bluetooth Mate defaults to 115200bps
  bluetooth.print("$");  // Enter command mode
  bluetooth.print("$"); 
  bluetooth.print("$"); 
  delay(100);  // Short delay, wait for the Mate to send back CMD
  bluetooth.println("U,9600,N");  // Temporarily Change the baudrate to 9600, no parity
  // 115200 can be too fast at times for NewSoftSerial to relay the data reliably
  bluetooth.begin(9600);  // Start bluetooth serial at 9600

} 
 
 char sensorVal;
void loop() 
{ 
   sensorVal = (char)bluetooth.read(); //digitalRead(2);
//   Serial.println("TEST outside");
    if(bluetooth.available())  // If the bluetooth sent any characters
    {
      // Send any characters the bluetooth prints to the serial monitor
      Serial.print((char)bluetooth.read());
       
    }
    if(Serial.available())  // If stuff was typed in the serial monitor
    {
      char c = (char) Serial.read();
       // Send any characters the Serial monitor prints to the bluetooth
      bluetooth.print(c); 
    }
    

      if (sensorVal=='1'){
          moveServo();  
        }
        else if (sensorVal=='0'){
          myservo.detach();
        }
        

  }
  
  void moveServo(){
    myservo.attach(9);
    for(pos = 0; pos <= 360; pos += 10){
      myservo.write(pos);             
      delay(2000);
      
   sensorVal = (char)bluetooth.read(); //digitalRead(2);
   if (sensorVal=='0'){
          return;
        }
    }
          
  }
    



