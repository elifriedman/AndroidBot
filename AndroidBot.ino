#include <Servo.h>
#include <SoftwareSerial.h>


SoftwareSerial bluetooth(12, 11);//(transmit, receive)
int Left = 9, Right = 10;
                // Backward ...... Forward
Servo myservoL; // 180      ...... 0
Servo myservoR; // 0        ...... 180

void setup() {
  Serial.begin(9600);
   //Setup bluetooth to arduino serial connection
     bluetooth.begin(9600);
  attachRL(Left,Right);
  writeSpeed(0);
}
void loop() {
  if(bluetooth.available())
     {
          //Data organized as one byte:
          //0bABXXXXXX
          // 'A' chooses the axis to control:
          // 1 = forward / reverse, 0 = left / right
          //
          // 'B' chooses the direction: 
          // 1 = forward / right, 0 = back / left
          // 'X's choose how fast to go.
          byte data = bluetooth.read();
          int direc = data & 0x40 ? 1 : -1;
          int speed = direc * map(data & 0x3F,0,0x3F,0,90);
          
          if(data & 0x80) { //then this is the y axis
               writeSpeed(speed);
               Serial.print("Y: ");
          } else {
               turn(speed);
               Serial.print("X: ");
          }
          Serial.println(speed);
     }
  
}
void turn(int deg) {//90 deg = Turn Right, -90 deg = Turn Left
  if(deg != 0) {
    attachRL(Left,Right);
  }
  int velL = myservoL.read();
  int velR = myservoR.read();
  myservoL.write(constrain(velL-deg,0,180));
  myservoR.write(constrain(velR-deg,0,180));
}
void writeSpeed(int vel) { //From -90 to 90

  if(vel == 0) { //Don't want motors to make noise when not in use
    myservoL.detach();
    myservoR.detach();
  } 
  else {
    attachRL(Left,Right);
    myservoL.write(90-vel);
    myservoR.write(90+vel);
  }
}

void attachRL(int L, int R) {
  if(!myservoL.attached())
    myservoL.attach(L);
  if(!myservoR.attached())
    myservoR.attach(R); 
}


