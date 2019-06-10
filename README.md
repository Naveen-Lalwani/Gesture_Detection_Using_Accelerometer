# Gesture Detection using Accelerometer

This project was done with the purpose of using accelerometer as a sensor for gesture detection. <BR><BR>
The project was done in three stages:
* ## Utilizaing MPU 9250 accelerometer to detect simple UP and DOWN gestures.
  I had used MPU 9250 IMU in conjunction with a particle photon board because of its wifi capabilities.
  For this I had employed alternate sensing, which was faster acceleration would mean DOWN gesture while slower gesture would mean UP         getsure. The result of the gesture was displayed by an Android App "KnightMare" which allows you to connect to the particle cloud using     your username and password.
  
* ## Utilizing Android Phone accelerometer to recognize hadnwritten characters drawn in air.
  For this part, I trained a random forest classifier using features like mean, max, 90 percentile and variance of the accelerometer data     along the X, Y and Z axis of the Android Phone's accelerometer to recognize the gestures. We got a cross validation accuracy of 98%.
  
* ## Utilizing the MPU 9250 and the particle photon and the android app.
  For this using the same features as above, I streamed the data of accelerometer readings from the particle photon to the android phone,     where the random forest model would classfiy the gestures made using MPU9250 accelerometer connected to the Particle Photon Board. 
