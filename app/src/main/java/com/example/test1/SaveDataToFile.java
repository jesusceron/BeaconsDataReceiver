package com.example.test1;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class SaveDataToFile {

    static void main(String participant_ID, String type, StringBuffer data){
        //Log.d("RSSI", Integer.toString(rssi));
        //Log.d("Estimote service data", serviceData_Hex);
        BufferedWriter bw = null;
        String file_name = "";
        switch (type){
            case "a":
                file_name = participant_ID+"_a.txt";
                break;
            case "g":
                file_name = participant_ID+"_g.txt";
                break;
            case "b":
                file_name = participant_ID+"_b.txt";
                break;
        }

        try {

            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //handle case of no SDCARD present
                Log.e("error", "no SD");
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

                File file = new File(dir, file_name);
                if (file.exists()) {
                    file.delete();
                }

                FileWriter fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                bw.write(data.toString());
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
/*    private void logErrorAndShowToast(String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.e("error", message);
    }*/
}