package com.example.test1;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveDataToFile {

    public static void main(String beacons_data){
        //Log.d("RSSI", Integer.toString(rssi));
        //Log.d("Estimote service data", serviceData_Hex);
        BufferedWriter bw = null;
        try {

            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //handle case of no SDCARD present
            } else {
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/dataset";
                Log.d("dir",dir);

                File folder = new File (dir);
                if (!folder.exists() && !folder.mkdirs()) {
                    // This should never happen - log handled exception!
                }

                //create file
                File file = new File(dir, "myData.txt");

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file, true);
                bw = new BufferedWriter(fw);
                bw.write(beacons_data);
                System.out.println("File written Successfully");

            }
        }catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally
        {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception ex){
                System.out.println("Error in closing the BufferedWriter"+ex);
            }
        }

    }
}
