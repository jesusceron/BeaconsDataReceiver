package com.example.test1;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveDataToFile {

    public static void main(StringBuffer beacons_data){
        //Log.d("RSSI", Integer.toString(rssi));
        //Log.d("Estimote service data", serviceData_Hex);
        BufferedWriter bw = null;
        try {

            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //handle case of no SDCARD present
            } else {
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/dataset";
                Log.d("dir",dir);

                File folder = new File(dir);

                if (folder.mkdirs()) {
                    System.out.println("Folder created successfully");
                } else {
                    System.out.println("Folder cannot be created");
                }

                //create file if it has not been created so far
                File file = new File(dir, "a.txt");
                if (file.exists()) {
                    file.delete();
                }

                FileWriter fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                bw.write(beacons_data.toString());
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
