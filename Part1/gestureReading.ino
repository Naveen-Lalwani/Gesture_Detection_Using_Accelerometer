/**
 * Authors: Naveen Lalwani, Karan Rawlani, Anmol Saxena, Sulashana Chakraborty
 * 
 * This project was done as a course project for EEE401 Electric Drives and Control
 * 
 * The following projects controls the speed and direction of a DC motor and 
 * a servo motor using IR Remote. The amount of speed is given as a percentage
 * of the total speed of the motor. For the servo motor, the increments are 
 * specified in the direction with 5 degrees and 10 degrees steps.
 * 
 * The techniqe used for controlling speed is PWM (Pulse Width Modulation) 
 * technique. 
 * 
 * We created our own H Bridge to connect the motors and the arduino crcuit board.
 * 
 */
#include <Servo.h>      
#include <IRremote.h>   

int IRpin = 11;         //IR Sensor control Pin 11
int x =0;
IRrecv irrecv(IRpin);     
decode_results results;

static int angle= 0;

Servo servoMotor;

int fT1 = 5 ; //Forward Direction Pin 5
int bT1 = 3 ; //Backward Direction Pin 3


void setup()   /*----( SETUP: RUNS ONCE )----*/
{
  Serial.begin(9600);
  irrecv.enableIRIn(); // Start the receiver
  servoMotor.attach(10);  //Servo Motor at pin 10

  //PWM pins below for H bridge
  pinMode(fT1, OUTPUT);    //input for Motor Driver control pin 1
  pinMode(bT1, OUTPUT);    //input for Motor Driver control pin 2
}


void loop()   /*----( LOOP: RUNS CONSTANTLY )----*/
{
  if(irrecv.decode(&results))     // RETURNS TRUE IF SIGNAL IS RECEIVED; receive the signal and decode and save it in results
  { if(results.value  == 0x1FE48B7 ) // POWER BUTTON
    { 
      exit1:                          //Label for goto command    
      Serial.println("POWER UP");
      delay(500);
      irrecv.resume();    //get the next signal 
      delay(500);

      //Loop for stalling IR receiver;  Makes the program to wait until the user gives an input 
      x=0;
      while(!(irrecv.decode(&results)))
      {
        x = x + 1;
      }
      
      //As soon as the user gives an input, it checks the value
      if(results.value == 0x1FE10EF )   //Geared DC Motor Selection; RPT Button
      {
        motorMode:                     //Label for GOTO command
        Serial.println("Geared Motor");
        delay(500);
     
        irrecv.resume(); 
        delay(300);
        
        x = 0;
        while(!(irrecv.decode(&results)))
       {
        x = x + 1;
       }
    
    
       if(results.value == 0x1FEC03F)    //FORWARD ; FastForward Button 
      {
       Serial.println("FORWARD");
       motorRPTSpeed:    // Label for GOTO command
       irrecv.resume();
       delay(500);
       x = 0;
       
       while(!(irrecv.decode(&results)))
       {
        x = x+1;
       }

      if(irrecv.decode(&results)){
      switch(results.value)
      {
        case (0x1FE50AF) :{ analogWrite(fT1, 26);      //pin 1 gets analog PWM of 10% duty cycle 
                            analogWrite (bT1, 0);       //Pin 2 set as 0 or DIGITAL 0 in all cases to rotate the motor in clockwise direction for Forward mode
                            Serial.println("10%");        
                            delay(300);S
                            irrecv.resume();
                            goto motorRPTSpeed ; }

        case (0x1FED827):{ analogWrite(fT1, 51);      //pin 1 gets analog PWM of 20% duty cycle
                           analogWrite (bT1, 0);
                           Serial.println("20%");  
                           delay(300);
                           goto motorRPTSpeed; }

        case (0x1FEF807):{ analogWrite(fT1,77);         //pin 1 gets analog PWM of 30% duty cycle
                           analogWrite (bT1,0 );
                           Serial.println("30%");
                           delay(300);
                           goto motorRPTSpeed; }

        case (0x1FE30CF):{ analogWrite(fT1,102);      //pin 1 gets analog PWM of 40% duty cycle
                           analogWrite (bT1,0);  
                           Serial.println("40%");
                           delay(300);
                           goto motorRPTSpeed; }
        
        case (0x1FEB04F):{ analogWrite(fT1, 128);       //pin 1 gets analog PWM of 50% duty cycle
                           analogWrite (bT1, 0);
                           Serial.println("50%");
                           delay(300);
                           goto motorRPTSpeed; }

        case (0x1FE708F):{ analogWrite(fT1, 154);       //pin 1 gets analog PWM of 60% duty cycle
                           analogWrite (bT1,0);
                           Serial.println("60%");  
                           delay(300);
                           goto motorRPTSpeed; }

        case (0x1FE00FF):{ analogWrite(fT1, 177);       //pin 1 gets analog PWM of 70% duty cycle
                           analogWrite (bT1,0); 
                           Serial.println("70%"); 
                           delay(300);
                           goto motorRPTSpeed; }
                           
        case (0x1FEF00F):{ analogWrite(fT1, 205);        //pin 1 gets analog PWM of 80% duty cycle
                           analogWrite (bT1, 0);  
                           Serial.println("80%");
                           delay(300);
                           goto motorRPTSpeed;}

        case (0x1FE9867):{ analogWrite(fT1,230);          //pin 1 gets analog PWM of 90% duty cycle
                           analogWrite (bT1, 0);
                           Serial.println("90%");  
                           delay(300);
                           goto motorRPTSpeed; }

        case (0x1FE10EF):{ digitalWrite(fT1,255);         //pin 1 gets analog PWM of 100% duty cycle
                           analogWrite (bT1,0);  
                           Serial.println("100%");
                           delay(300);
                           goto motorRPTSpeed; }

        case (0x1FE58A7):{ Serial.println("Select Mode: Forward or Backward");
                           delay(300);
                           goto motorMode; }   //To go to label motorMode to select the direction MODE:Forward or Backward; Mode Button

        case (0x1FE20DF):{  delay(300);
                            goto exit1; }     //To go to the beginning to select motor: Geared or Servo motor; EQ Button
          
        default: goto motorRPTSpeed;      //If incorrect value is sent then again enter speed
      }
    } 
 } else { 
  
  if(results.value == 0x1FE40BF )    //BACKWARD VALUE
  {
   
     Serial.println("BACKWARD");
     Repeat2:
     irrecv.resume();
     delay(500);
     x = 0;
    
  while(!(irrecv.decode(&results)))
  {
    x = x+1;
  }
  
       if(irrecv.decode(&results))
    {
      switch(results.value)
      {
        case (0x1FE50AF):{ analogWrite(bT1, 255);         //Pin 2 set as 255 or DIGITAL 1 in all cases to rotate the motor in anticlockwise direction for Backward mode
                           analogWrite(fT1, 230);         //pin 1 gets analog PWM of 10% duty cycle
                           Serial.println("10%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FED827):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 205);         //pin 1 gets analog PWM of 20% duty cycle
                           Serial.println("20%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FEF807):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 179);          //pin 1 gets analog PWM of 30% duty cycle
                           Serial.println("30%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FE30CF):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 154);           //pin 1 gets analog PWM of 40% duty cycle
                           Serial.println("40%");
                           delay(300);
                           goto Repeat2; }
        
        case (0x1FEB04F):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 128);           //pin 1 gets analog PWM of 50% duty cycle
                           Serial.println("50%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FE708F):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 102);            //pin 1 gets analog PWM of 60% duty cycle
                           Serial.println("60%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FE00FF):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 77);            //pin 1 gets analog PWM of 70% duty cycle
                           Serial.println("70%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FEF00F):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 51);             //pin 1 gets analog PWM of 80% duty cycle
                           Serial.println("80%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FE9867):{ analogWrite(bT1, 255);
                           analogWrite (fT1, 26);            //pin 1 gets analog PWM of 90% duty cycle
                           Serial.println("90%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FEE01F):{ analogWrite(bT1, 255);
                           analogWrite(fT1, 0);              //pin 1 gets analog PWM of 100% duty cycle
                           Serial.println("100%");
                           delay(300);
                           goto Repeat2; }

        case (0x1FE58A7):{ Serial.println("Select Mode: Forward or Backward");
                           delay(300);
                           goto motorMode; }                  //To go to label motorMode to select the direction MODE:Forward or Backward; Mode Button

        case (0x1FE20DF):{ delay(300);
                           goto exit1;}                       //To go to the beginning to select motor: Geared or Servo motor; EQ Button
          
        default: goto Repeat2; 
        }                                             //Switch Case Closing 
      }                                               //if(irrecv.decode(&results)) closing
  }                                                   //Backward closing
 }                                                 //else for Backward closing
}                                                 //Geared Motor

else                                            //Check for Servo Motor
 {
  if(results.value == 0x1FE906F)                  // U/SD Button
  {

    Serial.println("Servo Motor");
  
    servo1:
    delay(500);
    irrecv.resume(); 
    delay(500);
  
    x = 0;
    while(!(irrecv.decode(&results)))
    {
       x = x + 1;
    }

   if(irrecv.decode(&results)){
  switch(results.value)
      {
          case (0x1FE50AF): angle =20;
          break;
          case (0x1FED827): angle =40;
          break;                                                 
          case (0x1FEF807): angle =60;
          break;                 
          case (0x1FE30CF): angle =80;
          break;             
          case (0x1FEB04F): angle =100;
          break;                   
          case (0x1FE708F): angle =120;
          break;  
          case (0x1FE00FF): angle =140;
          break;
          case (0x1FEF00F) : angle = 160;
          break;
          case (0x1FE9867): angle =180;
          break;
          case (0x1FEE01F): angle =0;
          break;
          case (0x1FEC03F): angle = angle - 10;           //fastForward Button
          break;
          case (0x1FE40BF): angle = angle + 10;           //fastReverse Button
          break;
           case (0x1FEA05F): angle = angle - 5;           //Vol - Button
          break;
          case (0x1FE609F): angle = angle + 5;            //Vol + Button
          break;
          case (0x1FE20DF):{  delay(300);                 //To go to the beginning to select motor: Geared or Servo motor; EQ Button
                              goto exit1; }
        }
         
    if (angle <= 180 )
    {
      servoMotor.write(angle);
      Serial.println(angle);
      delay(1000);
    }
    goto servo1;            //Go to servo motor code beginning to change the value of angle again 
   }                      //if(irrecv.decode(&results))
}                        //if equal to servo closing
}                        //else for servo Closing; Geared or servo conditional
    
 

  }                     //Power up to start process if condition closing 
 }                     //if decode Closing
}   //void Loop() Closing
