package com.example.test1;

import static com.example.test1.MainActivity.toHexString;

public class ValidateServiceData {

    public static boolean main(byte[] serviceData) {

        boolean answer = false;
        String serviceData_Hex = toHexString(serviceData);

        /* Choose only packets with accelerometer data (frame a)*/
        if ((serviceData_Hex.substring(0,2).equals("22"))&(serviceData_Hex.substring(18,20).equals("00"))){

            switch (serviceData_Hex.substring(2,18)) {

                case "46846e6187678448"://Coconut
                    answer = true;
                    break;
                case "7897b2192cd1330e"://Mint
                    answer = true;
                    break;
                case "f32a65edd388bbd4"://Ice
                    answer = true;
                    break;
                case "c7f00010b342cf9e"://Blueberry
                    answer = true;
                    break;
                case "992074a3a75b01dd"://P2
                    answer = true;
                    break;
                case "eeaf86657d2312d5"://P1
                    answer = true;
                    break;
                case "7bb8ba833ded2db9"://B2
                    answer = true;
                    break;
                case "3e03d2aaf4265aa5"://B1
                    answer = true;
                    break;
                case "3c53d934182ed091"://G2
                    answer = true;
                    break;
                case "318da9517131bfab"://G1
                    answer = true;
                    break;
                default:
                    answer = false;
                    break;
            }

        }

        return answer;
    }
}
