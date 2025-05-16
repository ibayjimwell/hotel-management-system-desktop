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
import java.sql.Timestamp;
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
    
    // Load available room for checkin and booking
    public ResultSet LoadRooms(boolean checkinAndBooking, String type) {
        try {
            
            String query = "SELECT room_number FROM public.rooms WHERE room_type = ? AND status = ?";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);
            
            // Set the type
            ps.setString(1, type);
            ps.setString(2, "Available");

            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading available rooms", "Available rooms load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Checkin guest
    public boolean CheckinGuest(TransactionInfo info, String status) {
        try {
            String query = "INSERT INTO public.check_ins(room_number, guest_id, check_in_date, check_out_date, status, number_of_guests, type) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.room);
            ps.setInt(2, info.guest);
            ps.setTimestamp(3, new Timestamp(info.checkin.getTime()));   // <-- changed
            ps.setTimestamp(4, new Timestamp(info.checkout.getTime()));  // <-- changed
            ps.setString(5, status);
            ps.setInt(6, info.people);
            ps.setString(7, info.type);

            boolean result = ps.executeUpdate() > 0;

            if (result) {
                String roomQuery = "UPDATE public.rooms SET status = ? WHERE room_number = ?";
                PreparedStatement roomPs = con.prepareStatement(roomQuery);
                roomPs.setString(1, info.type.equalsIgnoreCase("booking") ? "Booked" : "Occupied");
                roomPs.setString(2, info.room);
                roomPs.executeUpdate();

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
    
    // Save transaction
    public boolean SaveTransaction(TransactionInfo info) {
        try {
            String query = "INSERT INTO public.transactions(type, guest, people, category, duration, checkin, room, checkout, downpayment, total, staff, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.type);
            ps.setInt(2, info.guest);
            ps.setInt(3, info.people);
            ps.setString(4, info.category);
            ps.setString(5, info.duration);
            ps.setTimestamp(6, new Timestamp(info.checkin.getTime()));   // <-- here
            ps.setString(7, info.room);
            ps.setTimestamp(8, new Timestamp(info.checkout.getTime()));  // <-- here
            ps.setDouble(9, info.downpayment);
            ps.setDouble(10, info.total);
            ps.setInt(11, info.staff);
            ps.setTimestamp(12, new Timestamp(info.date.getTime()));     // <-- here

            boolean result = ps.executeUpdate() > 0;
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in the database connection.", "Save Transaction Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
}
    
    // Load transactions
    public ResultSet LoadTransaction() {
        try {
            
            String query = "SELECT b.*, g.first_name AS guest_first_name, g.last_name AS guest_last_name, s.first_name AS staff_first_name, s.last_name AS staff_last_name FROM public.transactions b JOIN guests g ON b.guest = g.guest_id JOIN staffs s ON b.staff = s.id ORDER BY date DESC";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);

            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading transactions", "Transactions load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load staffs
    public ResultSet LoadStaffs() {
        try {
            
            String query = "SELECT * FROM public.staffs";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);

            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading staffs", "Staffs load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load staffs
    public ResultSet SearchGuest(Object search) {
        try {
            
            String query = "SELECT * FROM public.guests WHERE " +
               "CAST(guest_id AS TEXT) LIKE ? OR " +
               "first_name ILIKE ? OR " +
               "middle_name ILIKE ? OR " +
               "last_name ILIKE ? OR " +
               "email ILIKE ? OR " +
               "phone_number ILIKE ?";

            PreparedStatement ps = con.prepareStatement(query);
            String keyword = "%" + search.toString() + "%";

            // Set the parameters (6 total)
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, keyword);
            }

            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in searching guest", "Guest search failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load room types name 
    public ResultSet LoadRoomTypeName() {
        try {
            
            String query = "SELECT type FROM public.room_types";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);

            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading room type names", "Room type names load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load room prices 
    public ResultSet LoadRoomPrice(String type) {
        try {
            
            String query = "SELECT * FROM public.room_types WHERE type = ?";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);
            
            // Set the type 
            ps.setString(1, type);

            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading room type names", "Room type names load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load Bookings and Checkins
    public ResultSet LoadBookingsAndCheckin() {
        try {
            
            String query = "SELECT c.*, g.last_name, g.first_name, g.middle_name " +
               "FROM public.check_ins c " +
               "JOIN public.guests g ON c.guest_id = g.guest_id";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);

            // Execute the query 
            ResultSet result = ps.executeQuery();
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading bookings and checkins", "Bookings and Checkins load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load Bookings and Checkins
    public boolean ChangeCheckinStatus(int id, String newStatus) {
        try {
            
            String query = "UPDATE public.check_ins SET status = ? WHERE check_in_id = ?";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);
            
            // Set the data
            ps.setString(1, newStatus);
            ps.setInt(2, id);

            // Execute the query 
            boolean result = ps.executeUpdate() > 0;
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in changing checkins status", "Failed to change checkin status", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
}

// 
    