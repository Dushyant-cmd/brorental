package com.brorental.brorental.models;

public class User {

    private String name, mobile, pin, totalRent, totalRide;
    private boolean termsCheck;

    public User(String name, String mobile, String pin, String totalRent, String totalRide, boolean termsCheck) {
        this.name = name;
        this.mobile = mobile;
        this.pin = pin;
        this.totalRent = totalRent;
        this.totalRide = totalRide;
        this.termsCheck = termsCheck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTotalRent() {
        return totalRent;
    }

    public void setTotalRent(String totalRent) {
        this.totalRent = totalRent;
    }

    public String getTotalRide() {
        return totalRide;
    }

    public void setTotalRide(String totalRide) {
        this.totalRide = totalRide;
    }

    public boolean isTermsCheck() {
        return termsCheck;
    }

    public void setTermsCheck(boolean termsCheck) {
        this.termsCheck = termsCheck;
    }
}
