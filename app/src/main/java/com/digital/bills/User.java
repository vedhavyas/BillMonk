package com.digital.bills;


import android.graphics.Bitmap;

/**
 * Authored by vedhavyas on 6/12/14.
 * Project My Bills Lite
 */
public class User {

    private int ID;
    private String objectID;
    private String name;
    private String email;

    public User() {
        //Empty Constructor
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }


    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
