package com.example.hellolocation;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView textLat;
    TextView textLong;
    TextView speed;
    TextView stepDetected;
    TextView distanceBetween;
    TextView dispCurrentLong;
    TextView dispCurrentLat;
    TextView dispPastLong;
    TextView dispPastLat;
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
    float[] results;
    SharedPreferences pref;
    float lastLocationLatSaved;
    float lastLocationLongSaved;
    float firstLocationLatSaved;
    float firstLocationLongSaved;

    private SensorManager mSensorManager;
    private Chronometer mChronometer;
    double Timer;
    double AccurateTimer;
    double deciSeconds;
    int intTimer;
    int dataBaseEntry;
    double timeAtCaseNull;
    double timeAtCaseOne;
    double timeAtCaseTwo;
    private Sensor mStep;
    double stepOne;
    double stepTwo;
    float gSpeedOne;
    float gSpeedTwo;
    double speedOne;
    double speedTwo;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //writeNewUser("","",1);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStep = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        textLat = (TextView) findViewById(R.id.textView2);
        textLong = (TextView) findViewById(R.id.textView4);
        speed = (TextView) findViewById(R.id.textView6);
        stepDetected = (TextView) findViewById(R.id.textView8);
        distanceBetween = (TextView) findViewById(R.id.textView10);
        dispCurrentLong = (TextView) findViewById(R.id.textView13);
        dispCurrentLat = (TextView) findViewById(R.id.textView14);
        dispPastLong = (TextView) findViewById(R.id.textView11);
        dispPastLat = (TextView) findViewById(R.id.textView12);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        stepDetector = 0;
        pref = getPreferences(Context.MODE_PRIVATE);
        dataBaseEntry = pref.getInt("dataBaseEntry", -1);

        Log.d("step", "init" + stepDetector);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new mylocationlistener();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Open GPS", Toast.LENGTH_LONG).show();
        }
        bestprovider = locationManager.getBestProvider(getcriteria(), true);
        locationManager.requestLocationUpdates(bestprovider, 1000, 5, locationListener);
    }

    @IgnoreExtraProperties
    public class User {

        public String username;
        public double distance;
        public double speed;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String username, double distance, double speed) {
            this.username = username;
            this.distance = distance;
            this.speed = speed;
        }

    }

    private void writeNewUser(String userId, String name, double distance, double speed) {
        User user = new User(name, distance, speed);
        mDatabase.child("Sois").child(userId).setValue(user);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    class mylocationlistener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
//                Timer = (SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000;
                Timer = (SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000;
                intTimer = (int) Timer;
                Location lastLocation = new Location("lastLocation");
                Location firstLocation = new Location("firstLocation");
                Location secondLocation = new Location("secondLocation");
                tlat = location.getLatitude();
                tlong = location.getLongitude();
                //textLat.setText(Double.toString(tlat));
                //textLong.setText(Double.toString(tlong));
                //float gspeed = location.getSpeed();
                //speed.setText(Double.toString(gspeed));
                //firstLocation.setLatitude(lastLocationLat);
                //firstLocation.setLongitude(lastLocationLong);
//                lastLocation.setLatitude(lastLocationLatSaved);
//                lastLocation.setLongitude(lastLocationLongSaved);
//                firstLocation.setLatitude(firstLocationLatSaved);
//                firstLocation.setLongitude(firstLocationLongSaved);
//                secondLocation.setLatitude(secondLocationLat);
//                secondLocation.setLongitude(secondLocationLong);
                lastLocation.setLatitude(lastLocationLat);
                lastLocation.setLongitude(lastLocationLong);
                firstLocation.setLatitude(firstLocationLat);
                firstLocation.setLongitude(firstLocationLong);
                secondLocation.setLatitude(secondLocationLat);
                secondLocation.setLongitude(secondLocationLong);


                switch (stepDetector) {
                    case 0:
                        timeAtCaseNull = intTimer;
                        Log.d("0", "We are in State = " + stepDetector);
                        Log.d("E", "Time at State " + stepDetector + "=" + timeAtCaseNull);
                        Log.d("Timer", "Timer is " + intTimer);
                        if (intTimer >= 3) {
                            mChronometer.stop();
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                            mChronometer.start();
                            stepDetector = 0;
                        }
                        break;
                    case 1:
                        timeAtCaseOne = intTimer - timeAtCaseNull;
                        Log.d("1", "We are in State = " + stepDetector);
                        Log.d("E", "Time at State " + stepDetector + "=" + timeAtCaseOne);
                        Log.d("Timer", "Timer is " + intTimer);
                        if (intTimer <= 6) {
                            lastLocationLat = tlat;
                            lastLocationLong = tlong;
                            Log.d("A", "lastLocationLat = " + lastLocationLat);
                            Log.d("B", "lastLocationLong = " + lastLocationLong);
                        } else {
                            timeAtCaseNull = 0;
                            mChronometer.stop();
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                            mChronometer.start();
                            stepDetector = 0;
                        }
                        break;
                    case 2:
                        timeAtCaseTwo = intTimer - (timeAtCaseOne + timeAtCaseNull);
                        Log.d("2", "We are in State = " + stepDetector);
                        Log.d("E", "Time at State " + stepDetector + "=" + timeAtCaseTwo);
                        Log.d("Timer", "Timer is " + intTimer);
                        if (intTimer <= 9) {
                            firstLocationLat = tlat;
                            firstLocationLong = tlong;
                            Log.d("C", "firstLocationLat = " + firstLocationLat);
                            Log.d("D", "firstLocationLong = " + firstLocationLong);
                        } else {
                            timeAtCaseOne = 0;
                            mChronometer.stop();
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                            mChronometer.start();
                            stepDetector = 0;
                        }
                        break;
                    case 3:
                        Log.d("3", "We are in State = " + stepDetector);
                        secondLocationLat = tlat;
                        secondLocationLong = tlong;
                        distanceOne = firstLocation.distanceTo(lastLocation);
                        stepOne = abs((double) distanceOne);
                        distanceTwo = secondLocation.distanceTo(firstLocation);
                        stepTwo = abs((double) distanceTwo);
                        speedOne = distanceOne / timeAtCaseOne;
                        speedTwo = distanceTwo / timeAtCaseTwo;
                        if (distanceOne <= 2 && distanceTwo <= 2 && timeAtCaseOne > 0 && timeAtCaseTwo > 0) {
                            dataBaseEntry++;
                            saveStepValue("dataBaseEntry", dataBaseEntry);
                            if (timeAtCaseOne == timeAtCaseTwo) {
                                writeNewUser("Paso" + (2 * dataBaseEntry), "Symmetrical", stepOne, speedOne);
                                writeNewUser("Paso" + (2 * dataBaseEntry + 1), "Symmetrical", stepTwo, speedTwo);
                            } else if (timeAtCaseOne != timeAtCaseTwo) {
                                writeNewUser("Paso" + (2 * dataBaseEntry), "Asymmetrical", stepOne, speedOne);
                                writeNewUser("Paso" + (2 * dataBaseEntry + 1), "Asymmetrical", stepTwo, speedTwo);
                            }
                        }
                        Log.d("A", "lastLocationLat = " + lastLocationLat);
                        Log.d("B", "lastLocationLong = " + lastLocationLong);
                        Log.d("C", "firstLocationLat = " + firstLocationLat);
                        Log.d("D", "firstLocationLong = " + firstLocationLong);
                        Log.d("E", "secondLocationLat = " + secondLocationLat);
                        Log.d("F", "secondLocationLong = " + secondLocationLong);
                        Log.d("G", "FirstStep distance is = " + distanceOne);
                        Log.d("H", "SecondStep distance is = " + distanceTwo);
                        mChronometer.stop();
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.start();
                        timeAtCaseNull = 0;
                        timeAtCaseOne = 0;
                        timeAtCaseTwo = 0;
                        stepDetector = 0;
                        Log.d("W", "success = " + timeAtCaseNull);
                        Log.d("W", "success = " + timeAtCaseOne);
                        Log.d("W", "success = " + timeAtCaseTwo);
                        Log.d("W", "success = " + stepDetector);
                        Log.d("X", "Restart Process Successfull");
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
        private void saveStepValue(String key, Integer value) {
            //Adding value into shared preferences, this is the actual steps value and apply
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


    private Criteria getcriteria() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1) {
                stepDetector++;
            }
            if (stepDetector >= 4) {
                stepDetector = 0;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates(locationListener);
        //mSensorManager.unregisterListener(this);
//        mSensorManager.registerListener(this, mStep, SensorManager.SENSOR_DELAY_FASTEST);
//        locationManager.requestLocationUpdates(bestprovider, 0, 0, locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mStep, SensorManager.SENSOR_DELAY_FASTEST);
        locationManager.requestLocationUpdates(bestprovider, 0, 0, locationListener);
    }

}


