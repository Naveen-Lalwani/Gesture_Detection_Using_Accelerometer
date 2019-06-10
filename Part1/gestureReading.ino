/**
 * @author: Naveen Lalwani
 * AndrewID: naveenl
 * 17-722 Building User Focused Sensor Systems
 * 
 * File to read to gestures of UP and DOWN from the accelerometer using 
 * Particle Photon. The values will be published on the Android App:
 * "Knightmare" showing the current state of the device.
 * 
 * Serial.print() has been commented everywhere on purpose for debugging 
 * later if required.
 */
#include "MPU9250.h"
#include <application.h>
using namespace std;

MPU9250 margSensor;
int16_t dataSensor[10]; //[NumSensors] ([0-2] Accelerometer [3] Temperature [4-6] Gyroscope [7-9] Magnetometer)

/**
 * Counter to calibrate the threshold value for stability
 */
int counter = 1;
/* Value to check the gesture peaks. Calculated during run time. 
*/
int threshold = 0;

/* Value that will passed on the Particle Cloud for displaying */
String data = "STABLE";

/**
 * detectDirection: Function to detect the direction of the and determine UP 
 *                  or DOWN gesture or if the device is STABLE.
 *                  Logic is less acceleration must be applied for UP gesture while
 *                  more acceleration for DOWN gesture. 
 *                  If the acceleration is more, it is DOWN, if less it will be UP. 
 */
void detectDirection() {
        if (abs(dataSensor[2] - threshold) > (threshold/7)) {
            if ((dataSensor[2] < (threshold))  && (dataSensor[2] < 10722 )) {
                data = "DOWN";
                //Serial.println(data); Serial.print("\t");Serial.print(threshold);
                delay(1700);
                counter = 1;
                return;
            } else {
                data = "UP";
                //Serial.println(data); Serial.print("\t"); Serial.print(threshold);
                delay(1700);
                counter = 1;
                return;
            }
        } else {
            data = "STABLE";
            threshold = ((threshold + dataSensor[2]) / 2);
            //Serial.print(threshold); Serial.print("\n");
        }
}

/**
 * setup: Function to setup the variables and the photon.
 */ 
void setup() {
    Particle.variable("data", data);
    Wire.setSpeed(CLOCK_SPEED_400KHZ);
    Wire.begin();
    // Baud Rate
    Serial.begin(9600);
    //Begin sensing
    margSensor.begin();
    //margSensor.setAccelOffsetZ(0);
    /* ============================
    Selection of scale for Acelerometer:
    0 = ±2g
    1 = ±4g
    2 = ±8g
    3 = ±16g
    ===============================*/
    margSensor.setAccelScale(0);	// +-4g
    margSensor.setAccelDLPF(3);
}

/** 
 * loop: Function that is to looped over and over again. It first calibrates the 
 *       threshold value for 50 values. When that is done, it checks the direction 
 *       of motion by calling detectDirection() function.
 */
void loop() {
    margSensor.Read9Axis(dataSensor);
   if (counter > 50) {
        delay(150);
        detectDirection();
        delay(150);
   } else {
       data = "STABLE";
       threshold = ((threshold + dataSensor[2]) / 2);
       counter++;
       //Serial.println("Threshold = "); Serial.print(threshold);
       delay(30);
   }
}
