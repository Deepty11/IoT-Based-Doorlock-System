package com.jucse.doorlockfingerprintauthentication;

public class Logs {
    private String Date;
    private String IMEI;
    private String Time;

    public Logs(String date, String IMEI, String time) {
        Date = date;
        this.IMEI = IMEI;
        Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
