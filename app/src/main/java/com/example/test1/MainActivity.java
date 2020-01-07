package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;
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
    int count_beacons = 0;
    int count_beacons_total = 0;

    boolean finish_data_saving;
    //scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());

    public ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord scanRecord = result.getScanRecord();
            final int rssi = result.getRssi();

            long btTimestampMillis = System.currentTimeMillis() -
                    SystemClock.elapsedRealtime() +
                    result.getTimestampNanos() / 1000000;

            if (scanRecord == null) {
                return;
            }

            //Log.d("OJO", toHexString(scanRecord.getBytes())); // see the complete advertisement packet
            byte[] serviceData = Objects.requireNonNull(result.getScanRecord()).getServiceData(ESTIMOTE_SERVICE_UUID);

            if (serviceData!=null){

                boolean answer = ValidateServiceData.main(serviceData);
                if (answer) {
                    beacons_data.append( btTimestampMillis ).append(",")
                            .append(rssi).append(",").append(toHexString(serviceData))
                            .append(System.lineSeparator());
                    count_beacons++;
                    count_beacons_total++;
                    if (count_beacons>=4000) {
                        count_beacons = 0;
                        StringBuffer beacons_data_save = beacons_data;

                        System.out.println("Sale beacons");
                        SaveDataToFile.main(participant_ID, "b", beacons_data_save);

                        beacons_data.delete(0,beacons_data.length());
                    }
                }

            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    logErrorAndShowToast("SCAN_FAILED_ALREADY_STARTED");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    logErrorAndShowToast("SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    logErrorAndShowToast("SCAN_FAILED_FEATURE_UNSUPPORTED");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    logErrorAndShowToast("SCAN_FAILED_INTERNAL_ERROR");
                    break;
                default:
                    logErrorAndShowToast("Scan failed, unknown error code");
                    break;
            }
        }
    };

    private SensorManager sensorManager;
    public Sensor accelerometer;
    public Sensor gyroscope;
    int count_acc =0;
    int count_gyr =0;
    int count_acc_total =0;
    int count_gyr_total =0;

    public SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            long sensorTimestampMillis = System.currentTimeMillis() -
                    SystemClock.elapsedRealtime() +
                    sensorEvent.timestamp / 1000000;

/*            if (finish_data_saving == true){

                SaveDataToFile.main(participant_ID, "a", accelerometer_data);
                SaveDataToFile.main(participant_ID, "g", gyroscope_data);
            }*/

            switch(sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometer_data.append(sensorTimestampMillis).append(",")
                            .append(sensorEvent.values[0]).append(", ")
                            .append(sensorEvent.values[1]).append(", ")
                            .append(sensorEvent.values[2]).append(System.lineSeparator());
                    count_acc++;
                    count_acc_total++;
                    if (count_acc>=4000) {
                        count_acc = 0;
                        StringBuffer accelerometer_data_save = accelerometer_data;

                        System.out.println("Sale acc");
                        SaveDataToFile.main(participant_ID, "a", accelerometer_data_save);

                        accelerometer_data.delete(0,accelerometer_data.length());
                    }

                    break;
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    gyroscope_data.append(sensorTimestampMillis).append(",")
                            .append(sensorEvent.values[0]).append(", ")
                            .append(sensorEvent.values[1]).append(", ")
                            .append(sensorEvent.values[2]).append(System.lineSeparator());
                    count_gyr++;
                    count_gyr_total++;
                    if (count_gyr>=4000) {
                        count_gyr = 0;
                        StringBuffer gyroscope_data_save = gyroscope_data;

                        System.out.println("Sale gyro");
                        SaveDataToFile.main(participant_ID, "g", gyroscope_data_save);

                        gyroscope_data.delete(0,gyroscope_data.length());
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

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

        //final String p_ID = "";
        init();

        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(ESTIMOTE_SERVICE_UUID).build());

    }

    private void startButtonClicked(){
        finish_data_saving = false;
        participant_ID = IDTextView.getText().toString();
        if (BTscanner != null) {

            sensorManager.registerListener(mSensorListener, accelerometer, 5000);
            sensorManager.registerListener(mSensorListener, gyroscope, 5000);
            BTscanner.startScan(scanFilters,SCAN_SETTINGS,scanCallback);

        }
        startButton.setVisibility(View.INVISIBLE);
        stopButton.setVisibility(View.VISIBLE);
    }
    private void stopButtonClicked(){

        finish_data_saving = true;

        if (BTscanner != null) {
            BTscanner.stopScan(scanCallback);
        }
        sensorManager.unregisterListener(mSensorListener);
        System.out.println("Sale b");
        SaveDataToFile.main(participant_ID,"b", beacons_data);


        System.out.println("FINAL: "+count_beacons_total+" "+count_acc_total+" "+count_gyr_total);
        System.out.println("sale acc y gyr");
        SaveDataToFile.main(participant_ID,"a", accelerometer_data);
        SaveDataToFile.main(participant_ID,"g", gyroscope_data);

        beacons_data.delete(0, beacons_data.length());
        accelerometer_data.delete(0, accelerometer_data.length());
        gyroscope_data.delete(0, gyroscope_data.length());

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
        } else {
            BTscanner = BTadapter.getBluetoothLeScanner();
        }


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            // fail! we dont have an accelerometer!
            showFinishingAlertDialog("Accelerometer Error", "Accelerometer not detected on device");
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED) != null) {
            // success! we have a gyroscope
            gyroscope= sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
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
                        finish();
                    }
                }).show();
    }

    static String toHexString(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        }
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int c = bytes[i] & 0xFF;
            chars[i * 2] = HEX[c >>> 4];
            chars[i * 2 + 1] = HEX[c & 0x0F];
        }
        return new String(chars).toLowerCase();
    }

    private void logErrorAndShowToast(String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.e("error", message);
    }
}