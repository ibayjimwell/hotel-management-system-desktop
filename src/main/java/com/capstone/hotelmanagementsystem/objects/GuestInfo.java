/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.capstone.hotelmanagementsystem.objects;

import java.util.Date;

/**
 *
 * @author Admin
 */
public class GuestInfo {
    public String first_name;
    public String middle_name = "";
    public String last_name;
    public String gender;
    public Date birthday;
    public int age;
    public String phone_number;
    public String email;

    public GuestInfo(String first_name, String middle_name, String last_name, String gender, Date birthday, int age, String phone_number, String email) {
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.gender = gender;
        this.birthday = birthday;
        this.age = age;
        this.phone_number = phone_number;
        this.email = email;
    }
    
}
