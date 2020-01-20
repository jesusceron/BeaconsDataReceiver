package com.example.test1;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.test1.App.CHANNEL_ID;

public class MyService extends Service {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    // The Eddystone Service UUID, 0xFEAA.
    private static final ParcelUuid ESTIMOTE_SERVICE_UUID = ParcelUuid.fromString("0000FE9A-0000-1000-8000-00805F9B34FB");
    private static final ScanSettings SCAN_SETTINGS =
            new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();

    private StringBuffer beacons_data = new StringBuffer();
    private StringBuffer accelerometer_data = new StringBuffer();
    private StringBuffer gyroscope_data = new StringBuffer();
//    private StringBuffer pressure_data = new StringBuffer();

    public BluetoothManager BTmanager;
    public BluetoothAdapter BTadapter;
    public BluetoothLeScanner BTscanner;
    int count_beacons = 0;
    int count_beacons_total = 0;

    private String participant_ID;
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
                    Log.d("Bluetooth scan failed","SCAN_FAILED_ALREADY_STARTED");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.d("Bluetooth scan failed","SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.d("Bluetooth scan failed","SCAN_FAILED_FEATURE_UNSUPPORTED");
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    Log.d("Bluetooth scan failed","SCAN_FAILED_INTERNAL_ERROR");
                    break;
                default:
                    Log.d("Bluetooth scan failed","Scan failed, unknown error code");
                    break;
            }
        }
    };

    private SensorManager sensorManager;
    public Sensor accelerometer;
    public Sensor gyroscope;
//    public Sensor pressure;
    int count_acc =0;
    int count_gyr =0;
//    int count_pressure =0;
    int count_acc_total =0;
    int count_gyr_total =0;
//    int count_pressure_total =0;

    public SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            long sensorTimestampMillis = System.currentTimeMillis() -
                    SystemClock.elapsedRealtime() +
                    sensorEvent.timestamp / 1000000;

            switch(sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometer_data.append(sensorTimestampMillis).append(",")
                            .append(sensorEvent.values[0]).append(",")
                            .append(sensorEvent.values[1]).append(",")
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
                            .append(sensorEvent.values[0]).append(",")
                            .append(sensorEvent.values[1]).append(",")
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
/*                case Sensor.TYPE_PRESSURE:
                    pressure_data.append(sensorTimestampMillis).append(",")
                            .append(sensorEvent.values[0]).append(System.lineSeparator());
                    count_pressure++;
                    count_pressure_total++;
                    if (count_pressure>=4000) {
                        count_pressure = 0;
                        StringBuffer pressure_data_save = pressure_data;

                        System.out.println("Sale pressure");
                        SaveDataToFile.main(participant_ID, "p", pressure_data_save);

                        pressure_data.delete(0,pressure_data.length());
                    }

                    break;*/
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        participant_ID = intent.getStringExtra("inputExtra");

        accelerometer_data.append("Timestamp,accX,accY,accZ").append(System.lineSeparator());
        beacons_data.append("Timestamp,RSSI,Estimote TLM packet").append(System.lineSeparator());
        gyroscope_data.append("Timestamp,gyrX,gyrY,gyrZ").append(System.lineSeparator());

        BTmanager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BTadapter = BTmanager.getAdapter();
        BTscanner = BTadapter.getBluetoothLeScanner();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
//        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Collecting data from: ")
                .setContentText(participant_ID)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread

        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(ESTIMOTE_SERVICE_UUID).build());

        if (BTscanner != null) {

            sensorManager.registerListener(mSensorListener, accelerometer, 3907);// 1(second)/256(samples/second) = 3906.25
            sensorManager.registerListener(mSensorListener, gyroscope, 3907);
//            sensorManager.registerListener(mSensorListener, pressure, 5000);
            BTscanner.startScan(scanFilters,SCAN_SETTINGS,scanCallback);

        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (BTscanner != null) {
            BTscanner.stopScan(scanCallback);
        }
        sensorManager.unregisterListener(mSensorListener);
        System.out.println("Sale beacons");
        SaveDataToFile.main(participant_ID,"b", beacons_data);

        System.out.println("FINAL: "+count_beacons_total+" "+count_acc_total+" "+count_gyr_total);
        System.out.println("sale acc, gyr y pressure");
        SaveDataToFile.main(participant_ID,"a", accelerometer_data);
        SaveDataToFile.main(participant_ID,"g", gyroscope_data);
//        SaveDataToFile.main(participant_ID,"p", pressure_data);

        beacons_data.delete(0, beacons_data.length());
        accelerometer_data.delete(0, accelerometer_data.length());
        gyroscope_data.delete(0, gyroscope_data.length());
//        pressure_data.delete(0, pressure_data.length());

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
}