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
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    // The Eddystone Service UUID, 0xFEAA.
    private static final ParcelUuid ESTIMOTE_SERVICE_UUID = ParcelUuid.fromString("0000FE9A-0000-1000-8000-00805F9B34FB");
    //private static final ParcelUuid EDDYSTONE_SERVICE_UUID = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final ScanSettings SCAN_SETTINGS =
            new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0)
                    .build();

    //String str = "testing";
    final StringBuffer beacons_data = new StringBuffer();

    private List<ScanFilter> scanFilters;
    public BluetoothManager BTmanager;
    public BluetoothAdapter BTadapter;
    public BluetoothLeScanner BTscanner;
    public ScanCallback scanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(ESTIMOTE_SERVICE_UUID).build());
        //scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                ScanRecord scanRecord = result.getScanRecord();

                long millis = System.currentTimeMillis();

                if (scanRecord == null) {
                    return;
                }

                //Log.d("OJO", toHexString(scanRecord.getBytes())); mirar advertisment completo
                byte[] serviceData = result.getScanRecord().getServiceData(ESTIMOTE_SERVICE_UUID);

                if (serviceData!=null){

                    //validateServiceData(deviceAddress, serviceData);
                    String serviceData_Hex = toHexString(serviceData);

                    /* Choose only packets with accelerometer data (frame a)*/
                    if ((serviceData_Hex.substring(0,2).equals("22"))&(serviceData_Hex.substring(18,20).equals("00"))){
                        final int rssi = result.getRssi();
                        int id = 0;
                        switch (serviceData_Hex.substring(2,18)) {

                            case "46846e6187678448"://Coconut
                                id = 1;
                                break;
                            case "7897b2192cd1330e"://Mint
                                id = 2;
                                break;
                            case "f32a65edd388bbd4"://Ice
                                id = 3;
                                break;
                            case "c7f00010b342cf9e"://Blueberry
                                id = 4;
                                break;
                            case "992074a3a75b01dd"://P2
                                id = 5;
                                break;
                            case "eeaf86657d2312d5"://P1
                                id = 6;
                                break;
                            case "7bb8ba833ded2db9"://B2
                                id = 7;
                                break;
                            case "3e03d2aaf4265aa5"://B1
                                id = 8;
                                break;
                            case "3c53d934182ed091"://G2
                                id = 9;
                                break;
                            case "318da9517131bfab"://G1
                                id = 10;
                                break;
                        }

                        beacons_data.append(id+", "+millis+","+rssi+","+serviceData_Hex+"\n");
                        System.out.println(id+" "+rssi+","+serviceData_Hex);
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


    }

    public void onResume(){
        super.onResume();
        if (BTscanner != null) {
            BTscanner.startScan(scanFilters,SCAN_SETTINGS,scanCallback);
        }
    }

    public void onPause(){
        super.onPause();
        if (BTscanner != null) {
            BTscanner.stopScan(scanCallback);
        }

        SaveDataToFile.main(beacons_data);
    }

    // Attempts to create the scanner.
    private void init() {
        // New Android M+ permission check requirement.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
