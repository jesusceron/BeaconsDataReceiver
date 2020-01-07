package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    // The Eddystone Service UUID, 0xFEAA.
    private static final ParcelUuid ESTIMOTE_SERVICE_UUID = ParcelUuid.fromString("0000FE9A-0000-1000-8000-00805F9B34FB");
    //private static final ParcelUuid EDDYSTONE_SERVICE_UUID = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final ScanSettings SCAN_SETTINGS =
            new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();

    private StringBuffer beacons_data = new StringBuffer();
    private StringBuffer accelerometer_data = new StringBuffer();
    private StringBuffer gyroscope_data = new StringBuffer();

    private List<ScanFilter> scanFilters;
    public BluetoothManager BTmanager;
    public BluetoothAdapter BTadapter;
    public BluetoothLeScanner BTscanner;
    private SensorManager sensorManager;
    int count_beacons = 0;
    int count_beacons_total = 0;
    //scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());

    private Button startButton;
    private Button stopButton;
    private TextView IDTextView;
    private String participant_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IDTextView = findViewById(R.id.participant_ID);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        startButton.setOnClickListener(new buttonClick());
        stopButton.setOnClickListener(new buttonClick());

        init();

/*        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(ESTIMOTE_SERVICE_UUID).build());*/

    }

    private void startButtonClicked(){

        participant_ID = IDTextView.getText().toString();

        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.putExtra("inputExtra", participant_ID);

        ContextCompat.startForegroundService(this, serviceIntent);
/*        if (BTscanner != null) {

            sensorManager.registerListener(mSensorListener, accelerometer, 5000);
            sensorManager.registerListener(mSensorListener, gyroscope, 5000);
            BTscanner.startScan(scanFilters,SCAN_SETTINGS,scanCallback);

        }*/
        startButton.setVisibility(View.INVISIBLE);
        stopButton.setVisibility(View.VISIBLE);
    }
    private void stopButtonClicked(){

        Intent serviceIntent = new Intent(this, MyService.class);
        stopService(serviceIntent);

        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.INVISIBLE);

    }

    class buttonClick implements View.OnClickListener{
        @Override
        public void onClick(View v){
            switch (v.getId()) {
                case R.id.start_button:
                    startButtonClicked();
                    break;
                case R.id.stop_button:
                    stopButtonClicked();
                    break;
            }
        }
    }


    public void onResume(){
        super.onResume();

    }

    public void onPause(){
        super.onPause();

    }


    // Attempts to create the scanner.
    private void init() {
        // New Android M+ permission check requirement.

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs coarse location access");
            builder.setMessage("Please grant coarse location access so this app can scan for beacons");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs external storage access");
            builder.setMessage("Please grant external storage access so this app can save the data collected");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });
            builder.show();
        }


        BTmanager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BTadapter = BTmanager.getAdapter();
        if (BTadapter == null) {
            showFinishingAlertDialog("Bluetooth Error", "Bluetooth not detected on device");
        } else if (!BTadapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        } /*else {
            BTscanner = BTadapter.getBluetoothLeScanner();
        }*/


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            //accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            // fail! we dont have an accelerometer!
            showFinishingAlertDialog("Accelerometer Error", "Accelerometer not detected on device");
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED) != null) {
            // success! we have a gyroscope
            //gyroscope= sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        } else {
            // fail! we dont have a gyroscope!
            showFinishingAlertDialog("Gyroscope Error", "Gyroscope not detected on device");
        }

    }

    // Pops an AlertDialog that quits the app on OK.
    private void showFinishingAlertDialog(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //finish();
                    }
                }).show();
    }



    private void logErrorAndShowToast(String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.e("error", message);
    }

}