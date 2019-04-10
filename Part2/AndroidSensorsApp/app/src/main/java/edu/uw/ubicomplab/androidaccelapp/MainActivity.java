package edu.uw.ubicomplab.androidaccelapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;




public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // GLOBALS
    // Accelerometer
    private LineGraphSeries<DataPoint> timeAccelX = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeAccelY = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeAccelZ = new LineGraphSeries<>();

    // Gyroscope
    private LineGraphSeries<DataPoint> timeGyroX = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeGyroY = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> timeGyroZ = new LineGraphSeries<>();

    // Graph
    private GraphView graph;
    private int graphXBounds = 30;
    private int graphYBounds = 30;
    private int graphColor[] = {Color.argb(255,244,170,50),
            Color.argb(255, 60, 175, 240),
            Color.argb(225, 50, 220, 100)};
    private static final int MAX_DATA_POINTS_UI_IMU = 100; // Adjust to show more points on graph
    public int accelGraphXTime = 0;
    public int gyroGraphXTime = 0;
    public boolean isPlotting = false;

    // UI elements
    private TextView resultText;
    private TextView gesture1CountText, gesture2CountText, gesture3CountText, gesture4CountText;
    private Button gesture1Button,gesture2Button,gesture3Button,gesture4Button;

    // Machine learning
    private Model model;
    private boolean isRecording;
    private DescriptiveStatistics accelTime, accelX, accelY, accelZ;
    private DescriptiveStatistics gyroTime, gyroX, gyroY, gyroZ;
    private static final int GESTURE_DURATION_SECS = 2;

    boolean isAnyNewGestureRecorded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Get the UI elements
        resultText = findViewById(R.id.resultText);
        gesture1CountText = findViewById(R.id.gesture1TextView);
        gesture2CountText = findViewById(R.id.gesture2TextView);
        gesture3CountText = findViewById(R.id.gesture3TextView);
        gesture4CountText = findViewById(R.id.gesture4TextView);
        gesture1Button = findViewById(R.id.gesture1Button);
        gesture2Button = findViewById(R.id.gesture2Button);
        gesture3Button = findViewById(R.id.gesture3Button);
        gesture4Button = findViewById(R.id.gesture4Button);

        // Initialize the graphs
        initializeFilteredGraph();


        // Initialize data structures for gesture recording
        accelTime = new DescriptiveStatistics();
        accelX = new DescriptiveStatistics();
        accelY = new DescriptiveStatistics();
        accelZ = new DescriptiveStatistics();
        gyroTime = new DescriptiveStatistics();
        gyroX = new DescriptiveStatistics();
        gyroY = new DescriptiveStatistics();
        gyroZ = new DescriptiveStatistics();

        // Initialize the model
        model = new Model(this);


        //add text to the buttons
        gesture1Button.setText(model.outputClasses[0]);
        gesture2Button.setText(model.outputClasses[1]);
        gesture3Button.setText(model.outputClasses[2]);
        gesture4Button.setText(model.outputClasses[3]);

        // Get the sensors
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);

        // Check permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelGraphXTime += 1;

            // Get the data from the event
            long timestamp = event.timestamp;
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            // Add the original data to the graph
            DataPoint dataPointAccX = new DataPoint(accelGraphXTime, ax);
            DataPoint dataPointAccY = new DataPoint(accelGraphXTime, ay);
            DataPoint dataPointAccZ = new DataPoint(accelGraphXTime, az);
            timeAccelX.appendData(dataPointAccX, true, MAX_DATA_POINTS_UI_IMU);
            timeAccelY.appendData(dataPointAccY, true, MAX_DATA_POINTS_UI_IMU);
            timeAccelZ.appendData(dataPointAccZ, true, MAX_DATA_POINTS_UI_IMU);

            // Advance the graph
            if (isPlotting) {
                graph.getViewport().setMinX(accelGraphXTime - graphXBounds);
                graph.getViewport().setMaxX(accelGraphXTime);
            }

            // Add to gesture recorder, if applicable
            if (isRecording) {
                accelTime.addValue(timestamp);
                accelX.addValue(ax);
                accelY.addValue(ay);
                accelZ.addValue(az);
            }
        }
        else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroGraphXTime += 1;

            // Get the data from the event
            long timestamp = event.timestamp;
            float gx = event.values[0];
            float gy = event.values[1];
            float gz = event.values[2];

            // Add the original data to the graph
            DataPoint dataPointGyroX = new DataPoint(gyroGraphXTime, gx);
            DataPoint dataPointGyroY = new DataPoint(gyroGraphXTime, gy);
            DataPoint dataPointGyroZ = new DataPoint(gyroGraphXTime, gz);
            timeGyroX.appendData(dataPointGyroX, true, MAX_DATA_POINTS_UI_IMU);
            timeGyroY.appendData(dataPointGyroY, true, MAX_DATA_POINTS_UI_IMU);
            timeGyroZ.appendData(dataPointGyroZ, true, MAX_DATA_POINTS_UI_IMU);

            // Save to file, if applicable
            if (isRecording) {
                gyroTime.addValue(timestamp);
                gyroX.addValue(event.values[0]);
                gyroY.addValue(event.values[1]);
                gyroZ.addValue(event.values[2]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void computeFeaturesAndAddSamples(boolean isTraining, String label, View v2)
    {
        isAnyNewGestureRecorded = true;
        // Add the recent gesture to the train or test set
        isRecording = false;

        //TODO: Replace this function to receive features from Particle
        Double[] features = model.computeFeatures(accelTime, accelX, accelY, accelZ,
                gyroTime, gyroX, gyroY, gyroZ);

        if (isTraining)
            model.addTrainingSample(features,label);
        else
            model.assignTestSample(features);

        // Predict if the recent sample is for testing
        if (!isTraining) {
            String result = model.test();
            resultText.setText("Result: "+result);
        }

        // Update number of samples shown
        updateTrainDataCount();
        v2.setEnabled(true);
    }
    /**
     * Records a gesture that is GESTURE_DURATION_SECS long
     */
    public void recordGesture(View v) {
        final View v2 = v;

        // Figure out which button got pressed to determine label
        final String label;
        final boolean isTraining;
        switch (v.getId()) {
            case R.id.gesture1Button:
                label = model.outputClasses[0];
                isTraining = true;
                break;
            case R.id.gesture2Button:
                label = model.outputClasses[1];
                isTraining = true;
                break;
            case R.id.gesture3Button:
                label = model.outputClasses[2];
                isTraining = true;
                break;
            case R.id.gesture4Button:
                label = model.outputClasses[3];
                isTraining = true;
                break;
            default:
                label = "?";
                isTraining = false;
                break;
        }

        //TODO: When you stop using Android sensors, you might want to remove the timers and directly add features from Particle to the set
        // Create the timer to start data collection from the Android sensors
        Timer startTimer = new Timer();
        TimerTask startTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        accelTime.clear(); accelX.clear(); accelY.clear(); accelZ.clear();
                        gyroTime.clear(); gyroX.clear(); gyroY.clear(); gyroZ.clear();
                        isRecording = true;
                        v2.setEnabled(false);
                    }
                });
            }
        };

        // Create the timer to stop data collection
        Timer endTimer = new Timer();
        TimerTask endTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        computeFeaturesAndAddSamples(isTraining,label, v2);
                    }
                });
            }
        };

        // Start the timers
        startTimer.schedule(startTask, 0);
        endTimer.schedule(endTask, GESTURE_DURATION_SECS*1000);
    }

    /**
     * Trains the model as long as there is at least one sample per class
     */
    public void trainModel(View v) {
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + model.trainDataFilepath;
        File trainingFile = new File(fullFileName);
        if (trainingFile.exists() && !isAnyNewGestureRecorded)
        {
            Log.d("TAG","Training file exists: "+fullFileName);
            model.train(false);

        }
        else
        {
            Log.d("TAG","Need to create training file: "+fullFileName);
            model.train(true);
        }
    }

    /**
     * Resets the training data of the model
     */
    public void clearModel(View v) {
        model.resetTrainingData();
        updateTrainDataCount();
        resultText.setText("Result: ");
        isAnyNewGestureRecorded = false;
    }

    //Deletes the training file
    public void deleteTrainingFile (View v)
    {
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        String fullFileName = SDFile.getAbsolutePath() + File.separator + model.trainDataFilepath;
        File trainingFile = new File(fullFileName);
        trainingFile.delete();
    }

    /**
     * Initializes the graph that will show filtered data
     */
    public void initializeFilteredGraph() {
        graph = findViewById(R.id.graph);
        if (isPlotting) {
            graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
            graph.setBackgroundColor(Color.TRANSPARENT);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(graphXBounds);
            graph.getViewport().setMinY(-graphYBounds);
            graph.getViewport().setMaxY(graphYBounds);
            timeAccelX.setColor(graphColor[0]);
            timeAccelX.setThickness(5);
            graph.addSeries(timeAccelX);
            timeAccelY.setColor(graphColor[1]);
            timeAccelY.setThickness(5);
            graph.addSeries(timeAccelY);
            timeAccelZ.setColor(graphColor[2]);
            timeAccelZ.setThickness(5);
            graph.addSeries(timeAccelZ);
        }
        else
        {
            graph.setVisibility(View.INVISIBLE);
        }
    }

    public void updateTrainDataCount() {
        gesture1CountText.setText("Num samples: "+model.getNumTrainSamples(0));
        gesture2CountText.setText("Num samples: "+model.getNumTrainSamples(1));
        gesture3CountText.setText("Num samples: "+model.getNumTrainSamples(2));
        gesture4CountText.setText("Num samples: "+model.getNumTrainSamples(3));
    }
}
