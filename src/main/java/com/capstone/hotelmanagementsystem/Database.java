/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.capstone.hotelmanagementsystem;

/**
 *
 * @author Admin
 */
import com.capstone.hotelmanagementsystem.objects.GuestInfo;
import com.capstone.hotelmanagementsystem.objects.TransactionInfo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Admin
 */

// Class that's responsible for manipulating data in the database
public class Database {
    
    // Connection global variable
    // This variables is can access everywhere in this class 
    Connection con;
    Statement statement;
    JFrame parent;
    
    public Database (JFrame parent) {
        this.parent = parent;
        // try catch to catch the error and the program is not terminate if erro's happend
        try {
            
            // Connection info that's need of the postgresql
            // host:                                   port, database 
            String host = System.getenv("DB_HOST_HMS");
            
            // Database username
            String user = System.getenv("DB_USER_HMS");
            
            // Database password
            String password = System.getenv("DB_PASSWORD_HMS");           
            
            // Connect to the database
            con = DriverManager.getConnection(host, user, password);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error connecting to the database.", "Database Connection", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    // Add guest
    public boolean AddGuest(GuestInfo info) {
        try {
            
            String query = "INSERT INTO public.guests(first_name, middle_name, last_name, gender, birthday, age, phone_number, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);

            // Set the data
            ps.setString(1, info.first_name);
            ps.setString(2, info.middle_name);
            ps.setString(3, info.last_name);
            ps.setString(4, info.gender);
            ps.setDate(5, new Date(info.birthday.getTime()));
            ps.setInt(6, info.age);
            ps.setString(7, info.phone_number);
            ps.setString(8, info.email);
            
            // Execute the query 
            boolean result = ps.executeUpdate() > 0 ? true : false;
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Double check the credentials and make sure to add a unique guest that's not already exist.", "Guest is already exist", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
    }
    
    // Load guests
    public ResultSet LoadGuests(String status) {
        try {
            
            String query = "SELECT * FROM public.guests WHERE status = ?";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);

            // Set the data
            ps.setString(1, status);
            
            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading guests", "Guest load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load rooms
    public ResultSet LoadRooms(String status) {
        try {
            
            String query = null;

            if (status.equalsIgnoreCase("All")) {
                query = "SELECT * FROM public.rooms";
                PreparedStatement ps = con.prepareStatement(query);
                return ps.executeQuery();
            } else {
                query = "SELECT * FROM public.rooms WHERE status = ?";
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, status); // sets 'Available' safely
                return ps.executeQuery();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading rooms", "Rooms load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Add pending guest
    public boolean CheckinGuest(TransactionInfo info, String status) {
        try {
            
            String query = "INSERT INTO public.check_ins(room_number, guest_id, check_in_date, check_out_date, status, number_of_guests, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);

            // Set the data
            ps.setString(1, info.room);
            ps.setInt(2, info.guest);
            ps.setDate(3, new Date(info.checkin.getTime()));
            ps.setDate(4, new Date(info.checkout.getTime()));
            ps.setString(5, status);
            ps.setInt(6, info.people);
            ps.setString(7, info.type);
            
            // Execute the query 
            boolean result = ps.executeUpdate() > 0 ? true : false;
            
            if (result) {
                
                // Change room status
                String roomQuery = "UPDATE public.rooms SET status = ? WHERE room_number = ?";
                PreparedStatement roomPs = con.prepareStatement(roomQuery);
                
                roomPs.setString(1, info.type.equalsIgnoreCase("booking") ? "Booked" : "Occupied");
                roomPs.setString(2, info.room);
                roomPs.executeUpdate();
                
                // Change guest status
                String guestQuery = "UPDATE public.guests SET status = ? WHERE guest_id = ?";
                PreparedStatement guestPs = con.prepareStatement(guestQuery);
                
                guestPs.setString(1, info.type.equalsIgnoreCase("booking") ? "Book" : "Checkin");
                guestPs.setInt(2, info.guest);
                guestPs.executeUpdate();
                
            }
            
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in the database connection.", "Booking or Checkin Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
    }
    
}
    