package com.capstone.hotelmanagementsystem;

import java.sql.*;
import java.util.concurrent.*;

public class CheckinStatusUpdater {

    private static ScheduledExecutorService scheduler;

    public static void start(Connection connection) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> updateStatuses(connection), 0, 5, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private static void updateStatuses(Connection conn) {
        try {
            String sql = "SELECT check_in_id, check_in_date, check_out_date, status FROM check_ins WHERE status IN ('Booked', 'Active')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            long now = System.currentTimeMillis();

            while (rs.next()) {
                int id = rs.getInt("check_in_id");
                Timestamp checkin = rs.getTimestamp("check_in_date");
                Timestamp checkout = rs.getTimestamp("check_out_date");
                String status = rs.getString("status");

                if ("Booked".equals(status)) {
                    long millisUntilCheckin = checkin.getTime() - now;
                    long hoursUntilCheckin = millisUntilCheckin / (1000 * 60 * 60);
                    long daysUntilCheckin = hoursUntilCheckin / 24;

                    if ((daysUntilCheckin == 0 && hoursUntilCheckin <= 12) || millisUntilCheckin <= 0) {
                        Database.ChangeCheckinStatus(id, "Need to checkin");
                    }

                } else if ("Active".equals(status)) {
                    long millisRemaining = checkout.getTime() - now;
                    if (millisRemaining <= 0) {
                        Database.ChangeCheckinStatus(id, "Need to checkout");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Status Update Error: " + e.getMessage());
        }
    }
}