#include <SoftwareSerial.h>
#include <Servo.h> 
 
 Servo myservo;  // created servo object 
 int pos = 0;    // variable to store the servo position 
 char sensorVal;
 int c = 0; 
 
void setup() 
 {    
   bluetooth.begin(115200);       // The Bluetooth Mate defaults to 115200bps
   bluetooth.print("$");          // Enter command mode
   bluetooth.print("$"); 
   bluetooth.print("$"); 
   delay(100);                    // Short delay, wait for the Mate to send back CMD
   bluetooth.println("U,9600,N"); // Change the baudrate to 9600,as 115200 can be too fast at times
   bluetooth.begin(9600);         // Start bluetooth serial at 9600
 } 
 
 
void loop() 
 { 
   sensorVal = (char)bluetooth.read();               // Reading the bluetooth data transmitted by android app
   
   if (sensorVal == '1')         c = 1;               
   else if (sensorVal == '0')    c = 0;

   if (c == 1)                   moveServo();       // If data received is '1', starting the motor  
   else if (c == 0)              myservo.detach();  // If data received is '0', stoping the motor  
 }

void moveServo()
 {
    myservo.attach(9);

    for(pos = 0; pos < 180; pos += 10)  // goes from 0 degrees to 180 degrees in steps of 10 degrees 
    {                                  
       myservo.write(pos);              // tell servo to go to position in variable 'pos' 
       delay(2000);                     // waits 2s for the servo to reach the position 
    } 
  
    for(pos = 180; pos>=1; pos-=1)      // goes from 180 degrees to 0 degrees in steps of 1 degree 
    {                                
       myservo.write(pos);              // tell servo to go to position in variable 'pos' 
       delay(15);                       // waits 15ms for the servo to reach the position 
    } 
 }
