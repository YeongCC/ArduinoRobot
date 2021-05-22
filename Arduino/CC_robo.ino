#include <SoftwareSerial.h>
#include <NewPing.h>  //ultrasonic library
#include <Servo.h>    //servo library
#define RightmotorF 7 //digital pin 7 for right motor forward
#define RightmotorB 8 //digital pin 8 for right motor Backward
#define LeftmotorF 5  //digitalpin 5 for left motor forward
#define LeftmotorB 6  //digital pin 6 for right motor Backward
#define ControlRight 1
#define ControlLeft 0
Servo myservo;
int LeftDistance = 0;
int RightDistance = 0;
int distance = 0;
int val;
String message = "";
long duration;
int chkdistance;
int Speed = 150;
int FrontDistance = 0;
NewPing sonar(2, 3, 400);  //(trig,echo,maxdistance)
SoftwareSerial BT(10, 11); //Assigning arduino's (RXD,TXD)
void setup()
{
  // Setup LED
  pinMode(RightmotorF, OUTPUT); //declaring these pins as output to control them
  pinMode(RightmotorB, OUTPUT);
  pinMode(LeftmotorF, OUTPUT);
  pinMode(LeftmotorB, OUTPUT);
  pinMode(ControlRight, OUTPUT);
  pinMode(ControlLeft, OUTPUT);
  pinMode(3, OUTPUT); // Sets the trigPin as an OUTPUT
  pinMode(2, INPUT);
  Serial.begin(9600);
  myservo.attach(12); //telling the code that servo is at digital pin 12
  BT.begin(115200);
  BT.print("$$$"); //Bluetooth stuff dont change
  delay(100);
  BT.println("U,9600,N");
  BT.begin(9600);
  serv(512); //setting the servo at initial position (Change this accordingly)
}
void scan()
{
  int uS = sonar.ping();
  distance = (uS / US_ROUNDTRIP_CM);
  delay(500);
}

void loop()
{
  if (BT.available())
  {

    char GetBT = (char)BT.read();
    Serial.print(GetBT);
    switch (GetBT)
    {
    case 'f':
      Forward();
      break;
    case 'b':
      Backward();
      break;
    case 'l':
      Left();
      break;
    case 'r':
      Right();
      break;
    case 's':
      Movestop();
      break;
    case 'o':
      AutoMove();
      break;
    case 'k':
      Checkdistance();
      break;
    }
  }
}

int distanceCm()
{
  digitalWrite(3, LOW);
  delayMicroseconds(5);
  digitalWrite(3, HIGH);
  delayMicroseconds(10);
  digitalWrite(3, LOW);
  duration = pulseIn(2, HIGH);
  FrontDistance = duration * 0.034 / 2;
  return FrontDistance;
}

void Checkdistance()
{
  distanceCm();
  BT.print(FrontDistance);
  Serial.print(FrontDistance);
}

void AutoMove()
{
  while (1)
  {
    distanceCm();
    delay(500);
    if (FrontDistance < 10)
    {
      Movestop();
      navigate();
    }
    else
    {
      Forward();
    }

    if (BT.available())
    {
      char newBT = (char)BT.read();
      if (newBT == 't')
      {
        Movestop();
        break;
      }
    }
  }
}

void serv(int a)
{
  val = map(a, 0, 1023, 0, 179);
  myservo.write(val);
  delay(1000);
}
void Forward()
{
  digitalWrite(RightmotorF, LOW);
  digitalWrite(RightmotorB, HIGH);
  digitalWrite(LeftmotorF, HIGH);
  digitalWrite(LeftmotorB, LOW);
}
void Backward()
{
  digitalWrite(RightmotorF, HIGH);
  digitalWrite(RightmotorB, LOW);
  digitalWrite(LeftmotorF, LOW);
  digitalWrite(LeftmotorB, HIGH);
}
void Left()
{
  digitalWrite(RightmotorF, HIGH);
  digitalWrite(RightmotorB, LOW);
  digitalWrite(LeftmotorF, HIGH);
  digitalWrite(LeftmotorB, LOW);
}
void Right()
{
  digitalWrite(RightmotorF, LOW);
  digitalWrite(RightmotorB, HIGH);
  digitalWrite(LeftmotorF, LOW);
  digitalWrite(LeftmotorB, HIGH);
}
void Movestop()
{
  digitalWrite(RightmotorF, LOW);
  digitalWrite(RightmotorB, LOW);
  digitalWrite(LeftmotorF, LOW);
  digitalWrite(LeftmotorB, LOW);
}

void navigate()
{
  serv(1023);               //Move the servo to the left (my little servos didn't like going to 180 so I played around with the value until it worked nicely)
                            //Wait half a second for the servo to get there
  scan();                   //Go to the scan function
  LeftDistance = distance;  //Set the variable LeftDistance to the distance on the left
  serv(10);                 //Move the servo to the right
                            //Wait half a second for the servo to get there
  scan();                   //Go to the scan function
  RightDistance = distance; //Set the variable RightDistance to the distance on the right
  if (abs(RightDistance - LeftDistance) < 5)
  {
    Backward(); //Go to the moveBackward function
    delay(200); //Pause the program for 200 milliseconds to let the robot reverse
    Right();    //Go to the moveRight function
    delay(100); //Pause the program for 200 milliseconds to let the robot turn right
    serv(512);
  }
  else if (RightDistance < LeftDistance) //If the distance on the right is less than that on the left then...
  {
    Left();     //Go to the moveLeft function
    delay(100); //Pause the program for half a second to let the robot turn
    serv(512);
  }
  else if (LeftDistance < RightDistance) //Else if the distance on the left is less than that on the right then...
  {
    Right();    //Go to the moveRight function
    delay(100); //Pause the program for half a second to let the robot turn
    serv(512);
  }
}
