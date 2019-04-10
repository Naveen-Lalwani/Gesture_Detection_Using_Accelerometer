package com.example.knightmare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

/**
 * @author Naveen Lalwani
 * AndrewID: naveenl
 * 17-722 Building User Focused Sensor Systems
 *
 * The following app "Knightmare" displays the value of gesture made by the
 * accelerometer connected to the Particle Photon. The app gives the ability to
 * any user to connect to the particle cloud and access their first photon (provided
 * the user has an account on the cloud).
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Text Field to view the state of the file and UP and DOWN.
     */
    static TextView state;
    /**
     * Text Field to enter username.
     */
    static TextView username;
    /*
     * Text Field to enter password.
     */
    static TextView password;
    /*
     * Button to start LOGIN to particle cloud.
     */
    Button loginButton;
    /*
     * Button to LOGOUT from the particle cloud.
     */
    Button logoutButton;
    /*
     * Button to get states from the accelerometer published on particle cloud.
     */
    Button stateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**
         * Setting up the Particle SDK Cloud.
         */
        ParticleCloudSDK.init(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        loginButton = (Button) findViewById(R.id.start);
        logoutButton = (Button) findViewById(R.id.stop);
        stateButton = (Button) findViewById(R.id.button1);
        state = (TextView) findViewById(R.id.stateText);
        state.setEnabled(false);
        username = (TextView) findViewById(R.id.username);
        password = (TextView) findViewById(R.id.pass);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Function to login to the cloud.
     * @param view Current view
     */
    public void startLogin(View view) {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            String email = String.valueOf(MainActivity.username.getText());
            String password = String.valueOf(MainActivity.password.getText());
            private ParticleDevice mDevice;
            /*
             * Function to login to the Particle cloud.
             */
            @Override
            public Object callApi(@NonNull ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                sparkCloud.logIn(email, password);
                sparkCloud.getDevices();
                try {
                    mDevice = sparkCloud.getDevices().get(0);
                } catch (IndexOutOfBoundsException iobEx) {
                    throw new RuntimeException("Your account must have at least one device for this example app to work");
                }
                return  -1;
            }
            /**
             * Function that will display message when the login is
             * successful.
             * @param value Value that is to be displayed on success
             */
            @Override
            public void onSuccess(@NonNull Object value) {
                MainActivity.state.setText("Successfully Logged In.");
                Toaster.l(MainActivity.this, "Logged In.");
            }
            /**
             * Function that will display message when the login is not
             * successful.
             * @param e Exception raised on unsuccessful login
             */
            @Override
            public void onFailure(@NonNull ParticleCloudException e) {
                MainActivity.state.setText("Unsuccessful. Try Again.");
                e.printStackTrace();
            }
        });
    }

    /**
     * Function to logout of the cloud and unsubscribe listening to events.
     * @param view Current View
     */
    public void startLogout(View view) {
        if(ParticleCloudSDK.getCloud().isLoggedIn()) {
            ParticleCloudSDK.getCloud().logOut();
            this.state.setText("Successfully Logged Out.");
            Toaster.l(MainActivity.this, "Logged Out.");
        } else {
            // In case, button is pressed without logging in first.
            this.state.setText("Please Login first");
        }
    }

    /**
     * Function to show the state of the gesture after getting it from the cloud.
     * @param view Current View
     */
    public void showState(View view) {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            private ParticleDevice mDevice;

            /**
             * Function that gets the value from the cloud from the given variable.
             * @return The value of the variable
             */
            @Override
            public Object callApi(@NonNull ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                try {
                    mDevice = sparkCloud.getDevices().get(0);
                } catch (IndexOutOfBoundsException iobEx) {
                    throw new RuntimeException("Your account must have at least one device for this example app to work");
                }
                Object obj;
                    try {
                        obj = mDevice.getVariable("data");
                        return obj;
                    } catch (ParticleDevice.VariableDoesNotExistException e) {
                        Toaster.s(MainActivity.this, "Error reading variable");
                    }
                return -1;
            }
            /**
             * Function that displays the value in the state text field.
             * @param value State that is published on the cloud
             */
            @Override
            public void onSuccess(@NonNull Object value) {
                state.setText(value.toString());
                if (ParticleCloudSDK.getCloud().isLoggedIn() == true) {
                    view.performClick();
                }
            }
            /**
             * Throws exception if couldn't get the variable or if the variable is
             * not present in the file.
             * @param e Exception on not getting the value
             */
            @Override
            public void onFailure(@NonNull ParticleCloudException e) {
                e.printStackTrace();
                Log.d("info", e.getBestMessage());
            }
        });
    }
}