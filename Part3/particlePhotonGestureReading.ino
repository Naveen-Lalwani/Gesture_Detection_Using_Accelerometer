/**
 * @author: Naveen Lalwani
 * AndrewID: naveenl
 * 17-722 Building User Focused Sensor Systems
 * 
 * File to read to gestures of P, L, N, 2 from the accelerometer using 
 * Particle Photon. The values will be published on the Android App:
 * "NeoGesture" showing the current gesture.
 * 
 */
#include "MPU9250.h"
#include <application.h>
using namespace std;

MPU9250 margSensor;
int16_t dataSensor[10]; //[NumSensors] ([0-2] Accelerometer [3] Temperature [4-6] Gyroscope [7-9] Magnetometer)
/*
 * Variable to measure acceleration along X axis.
 */
double ax;
/*
 * Variable to measure acceleration along Z axis.
 */
double az;
/**
 * setup: Function to setup the variables and the photon.
 */ 
void setup() {
    /*
     * Sending the variables to the android app for processing.
     */
    Particle.variable("ax", &ax, DOUBLE);
    Particle.variable("az", &az, DOUBLE);
    Wire.setSpeed(CLOCK_SPEED_400KHZ);
    Wire.begin();
    // Baud Rate
    Serial.begin(19200);
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
 * loop: Function that is to be looped over and over again. It sends 
 * acceleration data along X axis and Z axis to the android app. 
 */
void loop() {
    margSensor.Read9Axis(dataSensor);
    ax = dataSensor[0];
    az = dataSensor[2];
}