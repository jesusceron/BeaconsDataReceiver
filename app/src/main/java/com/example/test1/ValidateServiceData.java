package com.example.test1;

class ValidateServiceData {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    static boolean main(byte[] serviceData) {

        boolean answer = false;
        String serviceData_Hex = toHexString(serviceData);

        /* '22' for Estimote TLM packets and '00' for Estimote conectivity packets*/
        if ((serviceData_Hex.substring(0,2).equals("22"))||(serviceData_Hex.substring(0,2).equals("00"))){

            switch (serviceData_Hex.substring(2,18)) {

                case "46846e6187678448"://Coconut
                case "7897b2192cd1330e"://Mint x
                case "f32a65edd388bbd4"://Ice
                case "c7f00010b342cf9e"://Blueberry
                case "992074a3a75b01dd"://P2
                case "eeaf86657d2312d5"://P1
                case "7bb8ba833ded2db9"://B2
                case "3e03d2aaf4265aa5"://B1
                case "3c53d934182ed091"://G2
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

    private static String toHexString(byte[] bytes) {
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
