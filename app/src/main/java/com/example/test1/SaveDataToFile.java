package com.example.test1;

import android.util.Log;

public class SaveDataToFile {

    public static void main(int rssi, String serviceData_Hex){
        Log.d("RSSI", Integer.toString(rssi));
        Log.d("Estimote service data", serviceData_Hex);

    }
}
