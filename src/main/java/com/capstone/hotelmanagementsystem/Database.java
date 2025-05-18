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
import java.time.Duration;
import java.time.LocalTime;
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
            
            String query = "SELECT room_number FROM public.rooms WHERE room_type = ? AND status IN (?, ?)";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);
            
            // Set the type
            ps.setString(1, type);
            ps.setString(2, "Available");
            ps.setString(3, "Booked");

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
            String query = "INSERT INTO public.check_ins(room_number, guest_id, check_in_date, check_out_date, status, number_of_guests, type, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, info.room);
            ps.setInt(2, info.guest);
            ps.setTimestamp(3, new Timestamp(info.checkin.getTime()));   // <-- changed
            ps.setTimestamp(4, new Timestamp(info.checkout.getTime()));  // <-- changed
            ps.setString(5, status);
            ps.setInt(6, info.people);
            ps.setString(7, info.type);
            ps.setDouble(8, info.total);

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
    // Load Transactions
    public ResultSet LoadTransaction(boolean isAdmin, int staff, boolean isForAnalytics) {
        try {
            String query;
            PreparedStatement ps;

            if (isForAnalytics) {
                if (isAdmin) {
                    // Admin sees all transactions regardless of date
                    query = "SELECT b.*, g.first_name AS guest_first_name, g.last_name AS guest_last_name, " +
                            "s.first_name AS staff_first_name, s.last_name AS staff_last_name " +
                            "FROM public.transactions b " +
                            "JOIN guests g ON b.guest = g.guest_id " +
                            "JOIN staffs s ON b.staff = s.id " +
                            "ORDER BY b.date DESC";
                    ps = con.prepareStatement(query);
                } else {
                    // Staff sees only their own transactions regardless of date
                    query = "SELECT b.*, g.first_name AS guest_first_name, g.last_name AS guest_last_name, " +
                            "s.first_name AS staff_first_name, s.last_name AS staff_last_name " +
                            "FROM public.transactions b " +
                            "JOIN guests g ON b.guest = g.guest_id " +
                            "JOIN staffs s ON b.staff = s.id " +
                            "WHERE b.staff = ? " +
                            "ORDER BY b.date DESC";
                    ps = con.prepareStatement(query);
                    ps.setInt(1, staff);
                }
            } else {
                if (isAdmin) {
                    // Admin sees today's transactions
                    query = "SELECT b.*, g.first_name AS guest_first_name, g.last_name AS guest_last_name, " +
                            "s.first_name AS staff_first_name, s.last_name AS staff_last_name " +
                            "FROM public.transactions b " +
                            "JOIN guests g ON b.guest = g.guest_id " +
                            "JOIN staffs s ON b.staff = s.id " +
                            "WHERE DATE(b.date) = CURRENT_DATE " +
                            "ORDER BY b.date DESC";
                    ps = con.prepareStatement(query);
                } else {
                    // Staff sees only their own transactions today
                    query = "SELECT b.*, g.first_name AS guest_first_name, g.last_name AS guest_last_name, " +
                            "s.first_name AS staff_first_name, s.last_name AS staff_last_name " +
                            "FROM public.transactions b " +
                            "JOIN guests g ON b.guest = g.guest_id " +
                            "JOIN staffs s ON b.staff = s.id " +
                            "WHERE DATE(b.date) = CURRENT_DATE AND b.staff = ? " +
                            "ORDER BY b.date DESC";
                    ps = con.prepareStatement(query);
                    ps.setInt(1, staff);
                }
            }

            return ps.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading transactions", "Transactions load failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Load staffs
    public ResultSet LoadStaffs() {
        try {
            
            String query = "SELECT * FROM public.staffs WHERE admin = false";
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
    
    // Decline guest
    public boolean DeclineGuest(int guest_id) {
        try {
            
            String query = "DELETE FROM public.guests WHERE guest_id = ?";
            // Prepare the query
            PreparedStatement ps = con.prepareStatement(query);
            
            // Set the data
            ps.setInt(1, guest_id);

            // Execute the query 
            boolean result = ps.executeUpdate() > 0;
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in declining guest", "Failed to decline guest", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
   // Login Staff
    public ResultSet LoginStaff(String username, String password) {
        try {
            
            String query = "SELECT id, admin, last_name, first_name, middle_name FROM public.staffs WHERE username = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(query);
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            return ps.executeQuery();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in processing login", "Failed to process login", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Saving logs
    public boolean SaveLog(int staffId, LocalTime timeIn, LocalTime timeOut) {
        try {
            String query = "INSERT INTO public.logs(staff_id, time_in, time_out, working_hours, log_date) " +
                           "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, staffId);
            ps.setTime(2, java.sql.Time.valueOf(timeIn));  // Convert LocalTime to SQL Time
            ps.setTime(3, java.sql.Time.valueOf(timeOut)); // Convert LocalTime to SQL Time

            // Calculate working hours as whole hours
            long hours = Duration.between(timeIn, timeOut).toHours();

            ps.setLong(4, hours);
            ps.setDate(5, new java.sql.Date(System.currentTimeMillis()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in saving log.", "Failed to save log", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Load logs
    public ResultSet LoadLogs() {
        try {
            String query = "SELECT * FROM public.logs ORDER BY log_date DESC";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet result = ps.executeQuery();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong in loading logs", "Logs Load Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Checkin Book
    public boolean CheckinBook(int check_in_id) {
        try {
            // Start transaction
            con.setAutoCommit(false);

            // Step 1: Update check_in status
            String updateCheckIn = "UPDATE public.check_ins SET status = 'Active' WHERE check_in_id = ?";
            PreparedStatement psCheckIn = con.prepareStatement(updateCheckIn);
            psCheckIn.setInt(1, check_in_id);
            psCheckIn.executeUpdate();

            // Step 2: Get guest_id and room_number from check_ins
            String selectCheckIn = "SELECT guest_id, room_number FROM public.check_ins WHERE check_in_id = ?";
            PreparedStatement psSelect = con.prepareStatement(selectCheckIn);
            psSelect.setInt(1, check_in_id);
            ResultSet rs = psSelect.executeQuery();

            if (rs.next()) {
                int guestId = rs.getInt("guest_id");
                String roomNumber = rs.getString("room_number");

                // Step 3: Update guest status
                String updateGuest = "UPDATE public.guests SET status = 'Checkin' WHERE guest_id = ?";
                PreparedStatement psGuest = con.prepareStatement(updateGuest);
                psGuest.setInt(1, guestId);
                psGuest.executeUpdate();

                // Step 4: Update room status
                String updateRoom = "UPDATE public.rooms SET status = 'Occupied' WHERE room_number = ?";
                PreparedStatement psRoom = con.prepareStatement(updateRoom);
                psRoom.setString(1, roomNumber);
                psRoom.executeUpdate();
            } else {
                con.rollback();
                JOptionPane.showMessageDialog(parent, "No matching check-in found.", "Check-in Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Commit transaction
            con.commit();
            con.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(parent, "Something went wrong during check-in.", "Failed to check-in", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean CheckoutBook(int check_in_id) {
        try {
            // Start transaction
            con.setAutoCommit(false);

            // Step 1: Get guest_id and room_number from check_ins
            String selectCheckIn = "SELECT guest_id, room_number FROM public.check_ins WHERE check_in_id = ?";
            PreparedStatement psSelect = con.prepareStatement(selectCheckIn);
            psSelect.setInt(1, check_in_id);
            ResultSet rs = psSelect.executeQuery();

            if (rs.next()) {
                int guestId = rs.getInt("guest_id");
                String roomNumber = rs.getString("room_number");

                // Step 2: Update guest status to 'Guest'
                String updateGuest = "UPDATE public.guests SET status = 'Guest' WHERE guest_id = ?";
                PreparedStatement psGuest = con.prepareStatement(updateGuest);
                psGuest.setInt(1, guestId);
                psGuest.executeUpdate();

                // Step 3: Update room status to 'Available'
                String updateRoom = "UPDATE public.rooms SET status = 'Available' WHERE room_number = ?";
                PreparedStatement psRoom = con.prepareStatement(updateRoom);
                psRoom.setString(1, roomNumber);
                psRoom.executeUpdate();

                // Step 4: Delete check-in record
                String deleteCheckin = "DELETE FROM public.check_ins WHERE check_in_id = ?";
                PreparedStatement psDelete = con.prepareStatement(deleteCheckin);
                psDelete.setInt(1, check_in_id);
                psDelete.executeUpdate();
            } else {
                con.rollback();
                JOptionPane.showMessageDialog(parent, "No matching check-in found.", "Checkout Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Commit transaction
            con.commit();
            con.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(parent, "Something went wrong during checkout.", "Failed to checkout", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean AddRoomType(String type, double hr3_price, double hr6_price, double hr10_price, double hr12_price, double hr24_price) {
        try {
            String query = "INSERT INTO public.room_types(type, hr3_price, hr6_price, hr10_price, hr12_price, hr24_price) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, type);
            ps.setDouble(2, hr3_price);
            ps.setDouble(3, hr6_price);
            ps.setDouble(4, hr10_price);
            ps.setDouble(5, hr12_price);
            ps.setDouble(6, hr24_price);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong while adding the room type.", "Add Room Type Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean RemoveRoomType(String type) {
        try {
            String query = "DELETE FROM public.room_types WHERE type = ?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, type);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong while removing the room type.", "Remove Room Type Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean AddRoom(String roomNumber, String roomType) {
        try {
            String query = "INSERT INTO public.rooms(room_number, room_type, status) VALUES (?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, roomNumber);
            ps.setString(2, roomType);
            ps.setString(3, "Available");

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong while adding the room.", "Add Room Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
   public boolean RemoveRoom(String room_number) {
        try {
            String query = "DELETE FROM public.rooms WHERE room_number = ?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, room_number);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong while removing the room.", "Remove Room Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
   
   public boolean AddStaff(String firstName, String middleName, String lastName, String gender,
                        String email, String phone, boolean isAdmin, String username, String password) {
        try {
            String query = "INSERT INTO public.staffs(" +
                           "first_name, middle_name, last_name, gender, email_address, phone_number, admin, username, password" +
                           ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, firstName);
            ps.setString(2, middleName);
            ps.setString(3, lastName);
            ps.setString(4, gender);
            ps.setString(5, email);
            ps.setString(6, phone);
            ps.setBoolean(7, isAdmin);
            ps.setString(8, username);
            ps.setString(9, password); // Consider hashing passwords before storing them

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong while adding the staff.", "Add Staff Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
   
   public boolean RemoveStaff(int id) {
        try {
            String query = "DELETE FROM public.staffs WHERE id = ?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Something went wrong while removing the staff.", "Remove Staff Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
   }
   
   public boolean CheckPassword(int staff_id, String password) {
        try {
            String query = "SELECT password FROM public.staffs WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, staff_id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Failed to verify password.", "Password Check Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
}

// 
    