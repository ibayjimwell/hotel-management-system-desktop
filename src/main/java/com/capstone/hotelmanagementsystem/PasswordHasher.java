/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.capstone.hotelmanagementsystem;

/**
 *
 * @author Admin
 */
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    public static void Hash() {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement selectStmt = conn.prepareStatement("SELECT id, password FROM staffs");
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String currentPassword = rs.getString("password");

                // Skip if password already appears hashed (you can customize this condition)
                if (currentPassword.startsWith("$2a$") || currentPassword.startsWith("$2b$")) {
                    continue;
                }

                // Hash the plaintext password
                String hashedPassword = BCrypt.hashpw(currentPassword, BCrypt.gensalt());

                // Update the record
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE staffs SET password = ? WHERE id = ?");
                updateStmt.setString(1, hashedPassword);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();

                System.out.println("Hashed password for staff ID: " + id);
            }

            System.out.println("Password hashing completed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
