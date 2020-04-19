package com.example.hellolocation;
//Assignment 2 Part 4.
//This section of the Application will measure the gait size, speed and symmetry of the user.
//It will use the location services and the step detector sensor to register when the user takes
//a step and to calculate the distance of this step. A timer is also implemented in the application.
//This measures the time that it takes for the user to make a step. Like this, if we know the time
//and the distance we can calculate the speed of the user.
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Declaring the variables and primitives required in the application.
    LocationManager locationManager;
    LocationListener locationListener;
    String bestprovider;
    Criteria criteria;
    double tlat;
    double tlong;
    int stepDetector = 0;
    double lastLocationLat;
    double lastLocationLong;
    double firstLocationLat;
    double firstLocationLong;
    double secondLocationLat;
    double secondLocationLong;
    public float distanceOne;
    public float distanceTwo;
    SharedPreferences pref;
    private SensorManager mSensorManager;
    private Chronometer mChronometer;
    double Timer;
    int intTimer;
    int dataBaseEntry;
    double timeAtCaseNull;
    double timeAtCaseOne;
    double timeAtCaseTwo;
    private Sensor mStep;
    double stepOne;
    double stepTwo;
    double speedOne;
    double speedTwo;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting an instance of the Firebase Database.
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Initialising the sensor and fetching the type step detector one.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStep = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //Initialising the Chronometer.
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        //At the beginning, we will initialize the Chronometer to the same base as the
        //System clock. This ensures that the counter starts from 0. We call the method
        //start() to begin the chronometer from the moment the app is created.
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();

        //Initialise the step detector to 0 at the beginning.
        stepDetector = 0;

        //Create the Shared Preferences. This is a power tool that will allow the application
        //To record the number of steps and will help saving the data in the database.
        pref = getPreferences(Context.MODE_PRIVATE);
        dataBaseEntry = pref.getInt("dataBaseEntry", -1);

        Log.d("step", "init" + stepDetector);

        //This method returns the handle to a system-level service by name. LOCATION_SERVICE
        //retrieves a LocationManager for controlling location.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Initialization of the location listener object.
        locationListener = new mylocationlistener();

        //Returns the current enabled/disabled status of the given provider
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Open GPS", Toast.LENGTH_LONG).show();
        }
        //To update our location, we will use the best provider out of the ones available.
        //These are: GPS Provider, Network Provider, Passive Provider. The minimum time
        //between updates is set to 0.1 seconds and the minimum distance to 1 meter.
        //This ensure accuracy while optimizing power consumption.
        bestprovider = locationManager.getBestProvider(getcriteria(), true);
        locationManager.requestLocationUpdates(bestprovider, 100, 1, locationListener);
    }
    //This class is created to save data into the Firebase Database. It saves three values:
    //A string that declares the step symmetrical or asymmetrical, the distance covered by the
    //user and the speed.
    @IgnoreExtraProperties
    public class User {

        public String symmetry;
        public double distance;
        public double speed;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String symmetry, double distance, double speed) {
            this.symmetry = symmetry;
            this.distance = distance;
            this.speed = speed;
        }

    }
    //This method will be called whenever the data has to be saved.
    private void writeNewUser(String userId, String symmetry, double distance, double speed) {
        User user = new User(symmetry, distance, speed);
        mDatabase.child("User Data").child(userId).setValue(user);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //Method implemented to keep track of the user's steps. The sensor used is Step_Detector.
    //This sensor triggers a 1.0 whenever it detects a user setting foot to the ground.
    //When this happens, we update the stepDetector by 1. This is how it adds steps. It is important
    //To mention that this sensor is less accurate in keeping the overall count of the steps than the
    //type STEP_COUNTER sensor, which was also investigated for this application. This second sensor
    //however, is updated less frequently than the STEP_DETECTOR. For this application we require a
    //sensor that updates in real time the steps taken by the user. Therefore, this was a better option.
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1) {
                stepDetector++;
            }

            //The step detector will not be allowed to reach a higher value than 4. In a later section
            //of the code we will see why this is like this.
            if (stepDetector >= 4) {
                stepDetector = 0;
            }
        }
    }

    //Location Listener class where the bulk of the code will be implemented
    class mylocationlistener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            //If the location is available
            if (location != null) {

                //Create a Timer that starts from 0 and counts in seconds.
                //The SystemClock.elapsedRealTime updates every millisecond,
                //So we have to firs subtract it to the chronometer base (to start
                //from 0) and then divide it by 1000 to count in seconds.
                Timer = (SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000;
                //Convert the value from double to integer
                intTimer = (int) Timer;

                //Defines three locations. Last location, first step location, second step location.
                Location lastLocation = new Location("lastLocation");
                Location firstLocation = new Location("firstLocation");
                Location secondLocation = new Location("secondLocation");

                //The variables tlat and tlong will return the latitude and longitude coordinates
                //of the user.
                tlat = location.getLatitude();
                tlong = location.getLongitude();

                //We define how the 3 locations are going to be updated.
                //These variables (lastlocationlat, lastlocationlong, etc, will be shown in a later
                //part of the code, where we will show how they update.
                lastLocation.setLatitude(lastLocationLat);
                lastLocation.setLongitude(lastLocationLong);
                firstLocation.setLatitude(firstLocationLat);
                firstLocation.setLongitude(firstLocationLong);
                secondLocation.setLatitude(secondLocationLat);
                secondLocation.setLongitude(secondLocationLong);

                //This is the bulk of the code. A switch statement is proposed, where we look at 3 possible
                //cases for the step counter. 0, 1, 2 and 3.
                //The idea is the following: 0 is a state where
                //nothing happens other than stopping the chronometer. This is the case in which the user is not
                //walking, therefore there is no need to keep track of the time or the location. It is done
                //like this so that we are not constantly requiring the location and wasting resources.
                switch (stepDetector) {
                    case 0:
                        //Timer is reset and stopped.
                        intTimer = 0;
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.stop();
                        break;

                    case 1:
                        //The user makes his first step. When it is detected by the sensor, we go into
                        //case 1. The chronometer base was set to the system clock on the previous case, so the timer is reset
                        //to 0. And the chronometer is started. The time spent in this state is measured
                        //and it is the one marked by the intTimer, since we have just reset it to 0.
                        //mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.start();
                        timeAtCaseOne = intTimer;

                        //If we spend less than 3 seconds in this state, this means the user will have taken
                        //a second step.The location is saved into lastLocation, as we saw earlier,
                        //by updating the two variables lastLocationLat/Long with tlat/tlong.
                        // Otherwise, 3 seconds is estimated to be the longest time an old person
                        // can take to make a step, and if we exceed this time, then we go back into the
                        //idle state (0).
                        if (intTimer <= 3) {
                            lastLocationLat = tlat;
                            lastLocationLong = tlong;

                        } else {

                            stepDetector = 0;
                        }
                        break;
                    case 2:
                        //This is Case 2. The user has taken another step. The principle is the same as with
                        //the previous case. We record the time spent here and we record the location and save
                        //it into firstLocationLat and firstLocationLong, which will update the first location.
                        //With the location from this case and the location from the previous case, we now have
                        //the distance the user has covered with this step. This will be calculated in case 3.
                        timeAtCaseTwo = intTimer - timeAtCaseOne;

                        if (intTimer <= 6) {
                            firstLocationLat = tlat;
                            firstLocationLong = tlong;
                        }
                        //If Timer is greater than 6, then this means the user has taken the step but then stopped.
                        //We have set it to 6 to allow a max
                        //of 3 seconds in the first case and 3 seconds
                        //in the second case.
                        //We will go back to the idle state (0) if this happens.
                        else {
                            stepDetector = 0;
                        }
                        break;
                    case 3:
                        //This is the final state, and marks the point where the user has taken the final step.
                        //We record this location into secondLocation. Now we have 3 locations, and we can therefore
                        //calculate the distance covered in two steps.
                        secondLocationLat = tlat;
                        secondLocationLong = tlong;

                        //First distance is done with the method distanceTo, and it takes the location
                        //from case two and calculates the distance to the location from case 1. The absolute
                        //value is taken, to ensure the distance is always a positive number. It is converted to
                        //a double, as we will have to store it into the FirebaseDatabase, which takes doubles.
                        distanceOne = firstLocation.distanceTo(lastLocation);
                        stepOne = abs((double) distanceOne);

                        //The same is done to calculate the distance from step 2, but this time with the location
                        //from case 3 and case 2.
                        distanceTwo = secondLocation.distanceTo(firstLocation);
                        stepTwo = abs((double) distanceTwo);

                        //The speed of the user is calculated by dividing the distance covered by the time it
                        //took the user to cover that distance.
                        speedOne = distanceOne / timeAtCaseOne;
                        speedTwo = distanceTwo / timeAtCaseTwo;

                        //Saving the data into the database. We will only save values which are real, as sometimes
                        //the step counter updates very quickly and yields missleading results. For the data to
                        //be real, the distance covered by a step cannot be greater than 2 meters and the time spent
                        //in a case cannot be smaller than 0 seconds.
                        if (distanceOne <= 2 && distanceTwo <= 2 && timeAtCaseOne > 0 && timeAtCaseTwo > 0) {

                            //This updates the name of the database Entry.
                            dataBaseEntry++;

                            //This method registers the data into shared preferences. Like this, we keep track of the
                            //last step saved and never overwrite data into the database entry.
                            saveStepValue("dataBaseEntry", dataBaseEntry);

                            //Defining symmetry: Walking symmetry is defined by the time the feet spend on the ground.
                            //If the user spends the same amount of time with his feet on the ground, the steps
                            //are symmetrical. If he doesn't, the steps are asymmetrical. The data is saved into the
                            //Firebase Database accordingly.
                            if (timeAtCaseOne == timeAtCaseTwo) {
                                writeNewUser("Step" + (2 * dataBaseEntry), "Symmetrical", stepOne, speedOne);
                                writeNewUser("Step" + (2 * dataBaseEntry + 1), "Symmetrical", stepTwo, speedTwo);
                            } else if (timeAtCaseOne != timeAtCaseTwo) {
                                writeNewUser("Step" + (2 * dataBaseEntry), "Asymmetrical", stepOne, speedOne);
                                writeNewUser("Step" + (2 * dataBaseEntry + 1), "Asymmetrical", stepTwo, speedTwo);
                            }
                        }
                        //When this has finished, we reset all the variables and go back to case
                        //0 to record further steps.
                        mChronometer.stop();
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.start();
                        timeAtCaseOne = 0;
                        timeAtCaseTwo = 0;
                        stepDetector = 0;
                        break;
                    default:
                        mChronometer.stop();
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.start();
                        timeAtCaseNull = 0;
                        timeAtCaseOne = 0;
                        timeAtCaseTwo = 0;
                        stepDetector = 0;
                }
            }
        }

        //This is the method used to save the data into the Shared Preferences.
        private void saveStepValue(String key, Integer value) {
            //Adding value into shared preferences, this is the actual steps value.
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(key, value);
            editor.apply();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    //Method that defines the criteria that the provider needs
    //to be matched to.
    private Criteria getcriteria() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    //We do not unregister the step detector or the location listener in onPause, so the activity
    //continues to run even when the user is not using the app. This is because we want to always
    //keep track of the steps.
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mStep, SensorManager.SENSOR_DELAY_FASTEST);
        locationManager.requestLocationUpdates(bestprovider, 0, 0, locationListener);
    }

}


