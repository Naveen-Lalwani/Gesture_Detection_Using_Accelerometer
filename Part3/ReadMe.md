In this part, I used a MPU9250 accelerometer connected to the Particle Photon Board to make the gestures selected in part 2, which were 1, L, N and 2 and streamed the data of accelerometer readings from the particle photon to the android phone, where the random forest model would classfiy the gestures made.

The <B>AndroidAppPhoton</B> directory contains the Android App code with major changes in MainActivity.java file enabling Particle Cloud Connection and allowing recording of data from Particle Photon cloud.

The <B>particlePhotonGestureReading.ino</B> file contains the code to be flashed in Particle Photon board which allows to collect the data from the accelerometer and sent it to the Android App via particle Cloud.

The <B>trainData.arff</B> file contains the training data collected.

The <B>1LN2_CrossValidation.png</B> shows the cross validation accuracy for the training data.
