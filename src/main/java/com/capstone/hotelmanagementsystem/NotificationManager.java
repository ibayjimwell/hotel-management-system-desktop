package com.capstone.hotelmanagementsystem;

import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class NotificationManager {

    private static NotificationManager instance;
    private ScheduledExecutorService scheduler;
    private Connection connection;

    private int lastCheckin = 0;
    private int lastCheckout = 0;
    private int lastPending = 0;

    private NotificationManager(Connection connection) {
        this.connection = connection;
        startPolling();
    }

    public static void start(Connection connection) {
        if (instance == null) {
            instance = new NotificationManager(connection);
        }
    }

    public static void stop() {
        if (instance != null) {
            instance.scheduler.shutdownNow();
        }
    }

    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkNotifications, 0, 5, TimeUnit.SECONDS);
    }

    private void checkNotifications() {
        try {
            LocalDate today = LocalDate.now();

            String checkinSQL = "SELECT COUNT(*) FROM check_ins WHERE status = 'Need to checkin'";
            PreparedStatement ps1 = connection.prepareStatement(checkinSQL);
//            ps1.setDate(1, Date.valueOf(today));
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int currentCheckin = rs1.getInt(1);

            String checkoutSQL = "SELECT COUNT(*) FROM check_ins WHERE status = 'Need to checkout'";
            PreparedStatement ps2 = connection.prepareStatement(checkoutSQL);
//            ps2.setDate(1, Date.valueOf(today));
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            int currentCheckout = rs2.getInt(1);

            String pendingSQL = "SELECT COUNT(*) FROM guests WHERE status = 'pending'";
            PreparedStatement ps3 = connection.prepareStatement(pendingSQL);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            int currentPending = rs3.getInt(1);

             boolean hasChanged =
            (currentCheckin != lastCheckin) ||
            (currentCheckout != lastCheckout) ||
            (currentPending != lastPending);
                    
            if (hasChanged) {
                int oldCheckin = lastCheckin;
                int oldCheckout = lastCheckout;
                int oldPending = lastPending;

                // Show notification with previous values
                showNotification(currentCheckin, currentCheckout, currentPending, oldCheckin, oldCheckout, oldPending);
                playSound();

                // Only update AFTER notification
                lastCheckin = currentCheckin;
                lastCheckout = currentCheckout;
                lastPending = currentPending;
            }

        } catch (Exception e) {
            System.err.println("Notification Error: " + e.getMessage());
        }
    }

    private void showNotification(int currentCheckin, int currentCheckout, int currentPending,
                              int oldCheckin, int oldCheckout, int oldPending) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder message = new StringBuilder("⚠ Notifications:\n");

            if (currentCheckin > oldCheckin) {
                message.append("- New booking need to check-in! Total: ").append(currentCheckin).append("\n");
            }

            if (currentCheckout > oldCheckout) {
                message.append("- New guest need to check out! Total: ").append(currentCheckout).append("\n");
            }

            if (currentPending > oldPending) {
                message.append("- New pending guest added! Total: ").append(currentPending).append("\n");
            }

            if (message.toString().equals("⚠ Notifications:\n")) {
                // Nothing new
                return;
            }

            JOptionPane.showMessageDialog(null, message.toString(), "Hotel Alerts", JOptionPane.WARNING_MESSAGE);
        });
    }

    private void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }
}