/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.capstone.hotelmanagementsystem.objects.mobile;

/**
 *
 * @author Admin
 */

import com.capstone.hotelmanagementsystem.objects.GuestInfo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
    
    // Add pending guest
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
    
}
