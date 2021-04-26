package com.graduationproject.zakerly.core.models;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Instructor extends RealmObject {

    private User user;
    private double pricePerHour;
    private double rate;

    public Instructor() {
    }

    public Instructor(User user, double pricePerHour) {
        this.user = user;
        this.pricePerHour = pricePerHour;
        this.rate = 5.0;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return "Instructor{" +
                "user=" + user +
                ", pricePerHour=" + pricePerHour +
                ", rate=" + rate +
                '}';
    }
}
