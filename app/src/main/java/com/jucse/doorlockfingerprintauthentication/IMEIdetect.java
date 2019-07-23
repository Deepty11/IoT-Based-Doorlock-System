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
            /*if (Character.isUpperCase(imei.charAt(i)))
            {
                char ch = (char)(((int)imei.charAt(i) +
                        s - 65) % 26 + 65);
                result.append(ch);
            }
            else if (Character.isUpperCase(imei.charAt(i)))
            {
                char ch = (char)(((int)imei.charAt(i) +
                        s - 97) % 26 + 97);
                result.append(ch);
            }
             else if (Character.isDigit(imei.charAt(i)))
            {
                char ch = (char)(((int)imei.charAt(i)+
                        s - 48) % 10 + 48);
                result.append(ch);
            }*/
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
