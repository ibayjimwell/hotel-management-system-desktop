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
public class TransactionInfo {
    public String type;
    public int guest;
    public int people;
    public String category;
    public String duration;
    public Date checkin;
    public String room;
    public Date checkout;
    public double downpayment;
    public double total;
    public int staff;
    public Date date;

    public TransactionInfo(String type, int guest, int people, String category, String duration, Date checkin, String room, Date checkout, double downpayment, double total, int staff, Date date) {
        this.type = type;
        this.guest = guest;
        this.people = people;
        this.category = category;
        this.duration = duration;
        this.checkin = checkin;
        this.room = room;
        this.checkout = checkout;
        this.downpayment = downpayment;
        this.total = total;
        this.staff = staff;
        this.date = date;
    }
    
}
