package com.graduationproject.zakerly.core.models;

public class OpinionModel {

    private String id;
    private String imageStudent;
    private String opinion;
    private float numStarRating;
    private int imageStudentInt;
    private String date;

    public OpinionModel() {
    }


    public OpinionModel(String imageStudent, String opinion, float numStarRating, int imageStudentInt, String date) {
        this.imageStudent = imageStudent;
        this.opinion = opinion;
        this.numStarRating = numStarRating;
        this.imageStudentInt = imageStudentInt;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public int getImageStudentInt() {
        return imageStudentInt;
    }

    public void setImageStudentInt(int imageStudentInt) {
        this.imageStudentInt = imageStudentInt;
    }

    public String getImageStudent() {
        return imageStudent;
    }

    public void setImageStudent(String imageStudent) {
        this.imageStudent = imageStudent;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public float getNumStarRating() {
        return numStarRating;
    }

    public void setNumStarRating(float numStarRating) {
        this.numStarRating = numStarRating;
    }
}

