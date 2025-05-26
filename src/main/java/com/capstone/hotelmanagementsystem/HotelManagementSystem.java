/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.capstone.hotelmanagementsystem;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.UIManager;

/**
 *
 * @author Admin
 */
public class HotelManagementSystem {

    public static void main(String args[]) {
        /* Set the look and feel */
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // Initialize the Database once
        Database db = new Database(null); // No parent JFrame here
        
        // Start NotificationManager with shared connection
        NotificationManager.start(Database.getConnection());
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }
}
