package com.jucse.doorlockfingerprintauthentication;



public class IMEIdetect {
    private String imei;

    public IMEIdetect(String imei) {
        this.imei = imei;
    }

    public String getImei() {
        return imei;
    }
    public String encryptImei(){

        StringBuffer result= new StringBuffer();
        int s=5;

        for (int i=0; i<imei.length(); i++)
        {

            int ch1 = imei.charAt(i);
            char ch2 = (char)((ch1 + s) % 128);
            result.append(ch2);
        }
        return result.toString();
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}
