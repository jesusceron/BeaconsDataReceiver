package com.example.test1;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class SaveDataToFile {

    static void main(String participant_ID, String type, StringBuffer data) {

        //Log.d("RSSI", Integer.toString(rssi));
        //Log.d("Estimote service data", serviceData_Hex);
        String file_name = "";
        //String header = "";

        switch (type) {
            case "a":
                file_name = participant_ID + "_acc_raw.csv";
                //header = "Timestamp,accX,accY,accZ"+System.lineSeparator();
                break;
            case "g":
                file_name = participant_ID + "_gyr_raw.csv";
                //header = "Timestamp,gyrX,gyrY,gyrZ"+System.lineSeparator();
                break;
            case "b":
                file_name = participant_ID + "_beacons_raw.csv";
                //header = "Timestamp,RSSI,Estimote TLM packet"+System.lineSeparator();
                break;
/*            case "p":
                file_name = participant_ID + "_pressure.txt";
                break;*/
        }

        try {

            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //handle case of no SDCARD present
                Log.e("error", "no SD");
            } else {
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dataset";
                //Log.d("dir", dir);

                File folder = new File(dir);

                if (folder.mkdirs()) {
                    System.out.println("Folder created successfully");
                } else {
                    System.out.println("Folder already created");
                }

                //create file if it has not been created so far
                File file = new File(dir, file_name);
                System.out.println("llega " + type);

/*                if (file.exists()) {

                }*/

                FileWriter filerWriter = new FileWriter(file, true);
                BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                bufWriter.append(data);
                bufWriter.close();
                filerWriter.close();



            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
