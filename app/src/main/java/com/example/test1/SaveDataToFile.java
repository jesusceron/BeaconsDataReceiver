package com.example.test1;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveDataToFile {

    public static void main(int rssi, String serviceData_Hex){
        Log.d("RSSI", Integer.toString(rssi));
        Log.d("Estimote service data", serviceData_Hex);
        BufferedWriter bw = null;
        try {

            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //handle case of no SDCARD present
            } else {
                String dir = Environment.getExternalStorageDirectory()+File.separator+"myDirectory";
                //create folder
                Log.d("dir",dir);
                File folder = new File(dir); //folder name
                folder.mkdirs();

                //create file
                File file = new File(dir, "myfile.txt");


                /* This logic will make sure that the file
                 * gets created if it is not present at the
                 * specified location*/
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                bw.write(serviceData_Hex);
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
