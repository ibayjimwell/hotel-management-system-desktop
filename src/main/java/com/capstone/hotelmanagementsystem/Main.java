/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.capstone.hotelmanagementsystem;

import com.capstone.hotelmanagementsystem.objects.GuestInfo;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author Admin
 */
public class Main extends javax.swing.JFrame {
    Database db = null;
    int staff_id;
    boolean isAdmin = false;
    String fullname = "";
    LocalTime StaffIn = LocalTime.now();
    LocalTime StaffOut;

    /**
     * Creates new form Main
     */
    
    // CONSTRUCTOR
    public Main(int staff_id, boolean isAdmin, String fullanme) {
        this.StaffIn = LocalTime.now();
        
        initComponents();
        
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Trigger the logout confirmation dialog
                LogoutTabActionPerformed(null);  // You can pass null since evt isn't used
            }
        });
        
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen when It's start
        this.db = new Database(this); // Connect to the database
        this.staff_id = staff_id;
        this.isAdmin = isAdmin;
        this.fullname = fullanme;
        
        // Change the status text value color
        applyStatusColumnRenderer(BookingTable, "BookingTable", /* status column index */ 8);
        applyStatusColumnRenderer(CheckinTable, "CheckinTable", /* status column index */ 8);
        applyStatusColumnRenderer(RoomsTable, "RoomTable", /* status column index */ 2);
        
        // Center Tables text
        centerTableText(BookingTable);
        centerTableText(CheckinTable);
        centerTableText(PendingGuestTable);
        centerTableText(RoomsTable);
        centerTableText(TransactionsTable);
        centerTableText(StaffsTable);
        centerTableText(LogsTable);
        
        // Load the date and time today
        this.loadDateAndTimeToday();
        // Load the transactions
        this.LoadTransactions();
        // Set the SearchList default model
        SearchList.setModel(new DefaultListModel<>());
        // Load The Analytics
        this.LoadAnalyticsCharts();
        
        if (this.isAdmin) {
            TransactionLabel.setText("All Transactions Today:");
            StaffFullnameLabel.setText("Full Name: " + this.fullname + " - Admin");
            StaffIDLabel.setText("ID: " + this.staff_id);
        } else {
            TransactionLabel.setText("Your Transactions Today:");
            StaffFullnameLabel.setText("Full Name: " + this.fullname + " - Staff");
            StaffIDLabel.setText("ID: " + this.staff_id);
        }
     
    }
    
    public void applyStatusColumnRenderer(JTable table, String tableType, int statusColumnIndex) {
        TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);

        table.getColumnModel().getColumn(statusColumnIndex).setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> {
            Component comp = defaultRenderer.getTableCellRendererComponent(table1, value, isSelected, hasFocus, row, column);

            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setOpaque(true); // Critical to see background color

                String status = value != null ? value.toString() : "";
                Color bgColor = Color.WHITE;
                Color fgColor = Color.BLACK;

                switch (tableType) {
                    case "BookingTable":
                        if (status.equalsIgnoreCase("Booked")) {
                            bgColor = new Color(22, 163, 74);
                            fgColor = Color.WHITE;
                        } else if (status.equalsIgnoreCase("Need to checkin")) {
                            bgColor = new Color(239, 68, 68);
                            fgColor = Color.WHITE;
                        }
                        break;

                    case "CheckinTable":
                        if (status.equalsIgnoreCase("Active")) {
                            bgColor = new Color(22, 163, 74);
                            fgColor = Color.WHITE;
                        } else if (status.equalsIgnoreCase("Need to checkout")) {
                            bgColor = new Color(239, 68, 68);
                            fgColor = Color.WHITE;
                        }
                        break;

                    case "RoomTable":
                        if (status.equalsIgnoreCase("Available")) {
                            bgColor = new Color(22, 163, 74);
                            fgColor = Color.WHITE;
                        } else if (status.equalsIgnoreCase("Occupied")) {
                            bgColor = new Color(59, 130, 246);
                            fgColor = Color.WHITE;
                        }
                        break;
                }

                if (isSelected) {
                    label.setBackground(table1.getSelectionBackground());
                    label.setForeground(table1.getSelectionForeground());
                } else {
                    label.setBackground(bgColor);
                    label.setForeground(fgColor);
                }

                return label;
            }

            return comp;
        });
}
    
    // Center Tables
    public void centerTableText(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(JLabel.CENTER);
    }
    
    void LoadAnalyticsCharts() {
        DefaultPieDataset categoryDataset = new DefaultPieDataset();           // For ChartPanelOne
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();      // For ChartPanelTwo2

        int bookingCount = 0;
        int checkinCount = 0;
        Map<LocalDate, int[]> dailyCounts = new TreeMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        try {
            // Fetch transactions and process them
            ResultSet data = db.LoadTransaction(isAdmin, staff_id, true);
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(6);

            while (data.next()) {
                String type = data.getString("type");
                Timestamp ts = data.getTimestamp("date");
                String category = data.getString("category");

                if (ts == null) continue;

                LocalDate createdDate = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                // Count category usage
                if (category != null && !category.isEmpty()) {
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                }

                if ("booking".equalsIgnoreCase(type)) bookingCount++;
                else if ("checkin".equalsIgnoreCase(type)) checkinCount++;

                if (!createdDate.isBefore(weekAgo) && !createdDate.isAfter(today)) {
                    int[] counts = dailyCounts.getOrDefault(createdDate, new int[]{0, 0});
                    if ("booking".equalsIgnoreCase(type)) counts[0]++;
                    else if ("checkin".equalsIgnoreCase(type)) counts[1]++;
                    dailyCounts.put(createdDate, counts);
                }
            }

            // Load room categories to ensure consistent labeling
            ResultSet categories = db.LoadRoomTypeName();
            while (categories.next()) {
                String categoryName = categories.getString("type");
                if (!categoryCounts.containsKey(categoryName)) {
                    categoryCounts.put(categoryName, 0);
                }
            }

            // Populate pie chart
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                categoryDataset.setValue(entry.getKey(), entry.getValue());
            }

            JFreeChart pieChart = ChartFactory.createPieChart(
                "Room Category Trends",
                categoryDataset,
                true, true, false);

            // Populate bar chart
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
            for (LocalDate date : dailyCounts.keySet()) {
                int[] counts = dailyCounts.get(date);
                barDataset.addValue(counts[0], "Bookings", date.format(formatter));
                barDataset.addValue(counts[1], "Check-ins", date.format(formatter));
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                "Bookings and Check-ins (Last 7 Days)",
                "Date",
                "Count",
                barDataset,
                PlotOrientation.VERTICAL,
                true, true, false);

            // Apply charts with fixed sizes
            SwingUtilities.invokeLater(() -> {
                Dimension pieSize = ChartPanelOne.getSize();
                Dimension barSize = ChartPanelTwo2.getSize();

                ChartPanel piePanel = new ChartPanel(pieChart, false);
                piePanel.setPreferredSize(pieSize);

                ChartPanel barPanel = new ChartPanel(barChart, false);
                barPanel.setPreferredSize(barSize);

                // ChartPanelOne - Pie
                ChartPanelOne.removeAll();
                ChartPanelOne.setLayout(new BorderLayout());
                ChartPanelOne.add(piePanel, BorderLayout.CENTER);
                ChartPanelOne.revalidate();
                ChartPanelOne.repaint();

                // ChartPanelTwo2 - Bar
                ChartPanelTwo2.removeAll();
                ChartPanelTwo2.setLayout(new BorderLayout());
                ChartPanelTwo2.add(barPanel, BorderLayout.CENTER);
                ChartPanelTwo2.revalidate();
                ChartPanelTwo2.repaint();
            });

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading analytics data", "Analytics Load Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean PromptAndCheckPassword() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(
            this,
            passwordField,
            "Enter your password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String inputPassword = new String(passwordField.getPassword());
            boolean isCorrect = db.CheckPassword(this.staff_id, inputPassword);

            if (!isCorrect) {
                JOptionPane.showMessageDialog(this, "Incorrect password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }

            return isCorrect;
        }

        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        GenderButtonGroup = new javax.swing.ButtonGroup();
        TopPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        SidePanel = new javax.swing.JPanel();
        UserTab = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10), new java.awt.Dimension(Short.MAX_VALUE, 5));
        DashboardTab = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10), new java.awt.Dimension(Short.MAX_VALUE, 5));
        BookingTab = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10), new java.awt.Dimension(Short.MAX_VALUE, 5));
        GuestTab = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10), new java.awt.Dimension(Short.MAX_VALUE, 5));
        RoomTab = new javax.swing.JButton();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        LogoutTab = new javax.swing.JButton();
        MainPanel = new javax.swing.JPanel();
        DashboardPanel = new javax.swing.JPanel();
        TopPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        ChartPanelTwo2 = new javax.swing.JPanel();
        ChartPanelOne = new javax.swing.JPanel();
        DayOfWeek = new javax.swing.JLabel();
        Time = new javax.swing.JLabel();
        Date = new javax.swing.JLabel();
        TransactionLabel = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        TransactionsTable = new javax.swing.JTable();
        StaffFullnameLabel = new javax.swing.JLabel();
        StaffIDLabel = new javax.swing.JLabel();
        UserPanel = new javax.swing.JPanel();
        TopPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        TopPanel8 = new javax.swing.JPanel();
        AddStaffButton = new javax.swing.JButton();
        RemoveStaff = new javax.swing.JButton();
        StaffRefreah = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        StaffsTable = new javax.swing.JTable();
        jLabel30 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        LogsTable = new javax.swing.JTable();
        BookingPanel = new javax.swing.JPanel();
        TopPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        TopPanel7 = new javax.swing.JPanel();
        CheckinBook = new javax.swing.JButton();
        CancelBook = new javax.swing.JButton();
        CheckoutButton = new javax.swing.JButton();
        BookingCheckinRefresh = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        BookingsToolBar = new javax.swing.JToolBar();
        jScrollPane4 = new javax.swing.JScrollPane();
        BookingTable = new javax.swing.JTable();
        CheckinsToolBar = new javax.swing.JToolBar();
        jScrollPane5 = new javax.swing.JScrollPane();
        CheckinTable = new javax.swing.JTable();
        GuestPanel = new javax.swing.JPanel();
        TopPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        GridPanel = new javax.swing.JPanel();
        GuestFormPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        FirstNameTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        MiddleNameTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        LastNameTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        DayComboBox = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        MonthComboBox = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        YearComboBox = new javax.swing.JComboBox<>();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        MaleRadio = new javax.swing.JRadioButton();
        FemaleRadio = new javax.swing.JRadioButton();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        EmailTextField = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        PhoneTextField = new javax.swing.JTextField();
        ClearButton = new javax.swing.JButton();
        SubmitButton = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        AgeTextField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        PendingGuestTable = new javax.swing.JTable();
        CheckinButton = new javax.swing.JButton();
        BookingButton = new javax.swing.JButton();
        DeclineButton = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        SearchList = new javax.swing.JList<>();
        SearchTextField = new javax.swing.JTextField();
        ClearSearchButton = new javax.swing.JButton();
        SearchSubmitButton = new javax.swing.JButton();
        SubmitButton2 = new javax.swing.JButton();
        SubmitButton3 = new javax.swing.JButton();
        RefreshButton = new javax.swing.JButton();
        RoomPanel = new javax.swing.JPanel();
        TopPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        TopPanel6 = new javax.swing.JPanel();
        AddRoomType = new javax.swing.JButton();
        RemoveRoomType = new javax.swing.JButton();
        AddRoom = new javax.swing.JButton();
        RemoveRoomButton = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        RoomsTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        TopPanel.setBackground(new java.awt.Color(55, 48, 163));
        TopPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel.setPreferredSize(new java.awt.Dimension(1009, 50));

        jLabel1.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(249, 250, 251));
        jLabel1.setText("ADC - HOTEL MANAGEMENT SYSTEM");
        TopPanel.add(jLabel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.1;
        getContentPane().add(TopPanel, gridBagConstraints);

        SidePanel.setBackground(new java.awt.Color(67, 56, 202));
        SidePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        SidePanel.setPreferredSize(new java.awt.Dimension(300, 702));
        SidePanel.setLayout(new javax.swing.BoxLayout(SidePanel, javax.swing.BoxLayout.Y_AXIS));

        UserTab.setBackground(new java.awt.Color(249, 250, 253));
        UserTab.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        UserTab.setForeground(new java.awt.Color(3, 7, 18));
        UserTab.setIcon(new javax.swing.ImageIcon("C:\\Users\\Admin\\Downloads\\icons\\management.png")); // NOI18N
        UserTab.setText("Staff");
        UserTab.setIconTextGap(6);
        UserTab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        UserTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UserTabActionPerformed(evt);
            }
        });
        SidePanel.add(UserTab);
        SidePanel.add(filler1);

        DashboardTab.setBackground(new java.awt.Color(249, 250, 253));
        DashboardTab.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        DashboardTab.setForeground(new java.awt.Color(3, 7, 18));
        DashboardTab.setIcon(new javax.swing.ImageIcon("C:\\Users\\Admin\\Downloads\\icons\\statisctics.png")); // NOI18N
        DashboardTab.setText("Analytics");
        DashboardTab.setIconTextGap(6);
        DashboardTab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        DashboardTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DashboardTabActionPerformed(evt);
            }
        });
        SidePanel.add(DashboardTab);
        SidePanel.add(filler2);

        BookingTab.setBackground(new java.awt.Color(249, 250, 253));
        BookingTab.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        BookingTab.setForeground(new java.awt.Color(3, 7, 18));
        BookingTab.setIcon(new javax.swing.ImageIcon("C:\\Users\\Admin\\Downloads\\icons\\booking.png")); // NOI18N
        BookingTab.setText("Booking & Check-in");
        BookingTab.setIconTextGap(6);
        BookingTab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        BookingTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BookingTabActionPerformed(evt);
            }
        });
        SidePanel.add(BookingTab);
        SidePanel.add(filler3);

        GuestTab.setBackground(new java.awt.Color(249, 250, 253));
        GuestTab.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        GuestTab.setForeground(new java.awt.Color(3, 7, 18));
        GuestTab.setIcon(new javax.swing.ImageIcon("C:\\Users\\Admin\\Downloads\\icons\\guest.png")); // NOI18N
        GuestTab.setText("Guest");
        GuestTab.setIconTextGap(6);
        GuestTab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        GuestTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GuestTabActionPerformed(evt);
            }
        });
        SidePanel.add(GuestTab);
        SidePanel.add(filler4);

        RoomTab.setBackground(new java.awt.Color(249, 250, 253));
        RoomTab.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        RoomTab.setForeground(new java.awt.Color(3, 7, 18));
        RoomTab.setIcon(new javax.swing.ImageIcon("C:\\Users\\Admin\\Downloads\\icons\\mattress.png")); // NOI18N
        RoomTab.setText("Room");
        RoomTab.setIconTextGap(6);
        RoomTab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        RoomTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RoomTabActionPerformed(evt);
            }
        });
        SidePanel.add(RoomTab);
        SidePanel.add(filler6);

        LogoutTab.setBackground(new java.awt.Color(249, 250, 253));
        LogoutTab.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        LogoutTab.setForeground(new java.awt.Color(3, 7, 18));
        LogoutTab.setIcon(new javax.swing.ImageIcon("C:\\Users\\Admin\\Downloads\\icons\\logout.png")); // NOI18N
        LogoutTab.setText("Logout");
        LogoutTab.setIconTextGap(6);
        LogoutTab.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        LogoutTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogoutTabActionPerformed(evt);
            }
        });
        SidePanel.add(LogoutTab);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.1;
        getContentPane().add(SidePanel, gridBagConstraints);

        MainPanel.setBackground(new java.awt.Color(243, 244, 246));
        MainPanel.setForeground(new java.awt.Color(243, 244, 246));
        MainPanel.setLayout(new java.awt.CardLayout());

        DashboardPanel.setBackground(new java.awt.Color(243, 244, 246));
        DashboardPanel.setForeground(new java.awt.Color(243, 244, 246));
        DashboardPanel.setLayout(new java.awt.BorderLayout());

        TopPanel2.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel2.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel3.setFont(new java.awt.Font("Arial Black", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(249, 250, 251));
        jLabel3.setText("    ANALYTICS");
        TopPanel2.add(jLabel3);

        DashboardPanel.add(TopPanel2, java.awt.BorderLayout.PAGE_START);

        ChartPanelTwo2.setBackground(new java.awt.Color(153, 153, 153));
        ChartPanelTwo2.setMaximumSize(new java.awt.Dimension(543, 435));

        javax.swing.GroupLayout ChartPanelTwo2Layout = new javax.swing.GroupLayout(ChartPanelTwo2);
        ChartPanelTwo2.setLayout(ChartPanelTwo2Layout);
        ChartPanelTwo2Layout.setHorizontalGroup(
            ChartPanelTwo2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        ChartPanelTwo2Layout.setVerticalGroup(
            ChartPanelTwo2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 435, Short.MAX_VALUE)
        );

        ChartPanelOne.setBackground(new java.awt.Color(153, 153, 153));
        ChartPanelOne.setMaximumSize(new java.awt.Dimension(543, 442));

        javax.swing.GroupLayout ChartPanelOneLayout = new javax.swing.GroupLayout(ChartPanelOne);
        ChartPanelOne.setLayout(ChartPanelOneLayout);
        ChartPanelOneLayout.setHorizontalGroup(
            ChartPanelOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 847, Short.MAX_VALUE)
        );
        ChartPanelOneLayout.setVerticalGroup(
            ChartPanelOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 442, Short.MAX_VALUE)
        );

        DayOfWeek.setFont(new java.awt.Font("Arial Black", 0, 20)); // NOI18N
        DayOfWeek.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DayOfWeek.setText("Monday");

        Time.setFont(new java.awt.Font("Arial Black", 0, 100)); // NOI18N
        Time.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        Time.setText("12:30 AM");
        Time.setToolTipText("");

        Date.setFont(new java.awt.Font("Arial Black", 0, 40)); // NOI18N
        Date.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        Date.setText("September 03, 2025");

        TransactionLabel.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        TransactionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        TransactionLabel.setText("Your Transactions Today:");

        TransactionsTable.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        TransactionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Type", "Guest", "People", "Category", "Duration", "Checkins", "Room", "Checkout", "Downpayment", "Total", "Staff", "Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TransactionsTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        TransactionsTable.setSelectionBackground(new java.awt.Color(55, 48, 163));
        TransactionsTable.setSelectionForeground(new java.awt.Color(249, 250, 253));
        TransactionsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        TransactionsTable.setShowGrid(true);
        jScrollPane6.setViewportView(TransactionsTable);

        StaffFullnameLabel.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        StaffFullnameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        StaffFullnameLabel.setText("Full Name:");

        StaffIDLabel.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        StaffIDLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        StaffIDLabel.setText("ID:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(139, 139, 139)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(Time)
                            .addComponent(Date)
                            .addComponent(DayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 173, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(TransactionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
                                    .addComponent(StaffFullnameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(StaffIDLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ChartPanelOne, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ChartPanelTwo2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(618, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(0, 126, Short.MAX_VALUE)
                        .addComponent(ChartPanelOne, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(ChartPanelTwo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addComponent(DayOfWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Time, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Date)
                        .addGap(84, 84, 84)
                        .addComponent(TransactionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(StaffFullnameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(StaffIDLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(19, 19, 19))
        );

        DashboardPanel.add(jPanel4, java.awt.BorderLayout.CENTER);

        MainPanel.add(DashboardPanel, "DashboardCard");

        UserPanel.setBackground(new java.awt.Color(243, 244, 246));
        UserPanel.setForeground(new java.awt.Color(243, 244, 246));
        UserPanel.setLayout(new java.awt.BorderLayout());

        TopPanel1.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel1.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setFont(new java.awt.Font("Arial Black", 0, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(249, 250, 251));
        jLabel2.setText("    STAFF");
        TopPanel1.add(jLabel2);

        UserPanel.add(TopPanel1, java.awt.BorderLayout.PAGE_START);

        TopPanel8.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel8.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel8.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        AddStaffButton.setBackground(new java.awt.Color(22, 163, 74));
        AddStaffButton.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        AddStaffButton.setForeground(new java.awt.Color(3, 7, 18));
        AddStaffButton.setText("Add Staff");
        AddStaffButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddStaffButtonActionPerformed(evt);
            }
        });
        TopPanel8.add(AddStaffButton);

        RemoveStaff.setBackground(new java.awt.Color(239, 68, 68));
        RemoveStaff.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RemoveStaff.setForeground(new java.awt.Color(3, 7, 18));
        RemoveStaff.setText("Remove Staff");
        RemoveStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveStaffActionPerformed(evt);
            }
        });
        TopPanel8.add(RemoveStaff);

        StaffRefreah.setBackground(new java.awt.Color(59, 130, 246));
        StaffRefreah.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        StaffRefreah.setForeground(new java.awt.Color(3, 7, 18));
        StaffRefreah.setText("Refresh");
        StaffRefreah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StaffRefreahActionPerformed(evt);
            }
        });
        TopPanel8.add(StaffRefreah);

        UserPanel.add(TopPanel8, java.awt.BorderLayout.SOUTH);

        jLabel29.setFont(new java.awt.Font("Arial Black", 0, 20)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText("Staffs");

        StaffsTable.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        StaffsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Last Name", "First Name", "Middle Name", "Gender", "Email", "Phone", "Username", "Password"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        StaffsTable.setSelectionBackground(new java.awt.Color(55, 48, 163));
        StaffsTable.setSelectionForeground(new java.awt.Color(249, 250, 253));
        StaffsTable.setShowGrid(true);
        jScrollPane7.setViewportView(StaffsTable);

        jLabel30.setFont(new java.awt.Font("Arial Black", 0, 20)); // NOI18N
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("Logs");

        LogsTable.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        LogsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Staff ID", "In", "Out", "Working Hours", "Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        LogsTable.setSelectionBackground(new java.awt.Color(55, 48, 163));
        LogsTable.setSelectionForeground(new java.awt.Color(249, 250, 253));
        LogsTable.setShowGrid(true);
        jScrollPane8.setViewportView(LogsTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 990, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(687, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(515, Short.MAX_VALUE))
        );

        UserPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

        MainPanel.add(UserPanel, "UserCard");

        BookingPanel.setBackground(new java.awt.Color(243, 244, 246));
        BookingPanel.setForeground(new java.awt.Color(243, 244, 246));
        BookingPanel.setLayout(new java.awt.BorderLayout());

        TopPanel3.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel3.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel4.setFont(new java.awt.Font("Arial Black", 0, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(249, 250, 251));
        jLabel4.setText("    BOOKING & CHECK-IN");
        TopPanel3.add(jLabel4);

        BookingPanel.add(TopPanel3, java.awt.BorderLayout.NORTH);

        TopPanel7.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel7.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        CheckinBook.setBackground(new java.awt.Color(22, 163, 74));
        CheckinBook.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        CheckinBook.setForeground(new java.awt.Color(3, 7, 18));
        CheckinBook.setText("Checkin Book");
        CheckinBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckinBookActionPerformed(evt);
            }
        });
        TopPanel7.add(CheckinBook);

        CancelBook.setBackground(new java.awt.Color(239, 68, 68));
        CancelBook.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        CancelBook.setForeground(new java.awt.Color(3, 7, 18));
        CancelBook.setText("Cancel Book");
        CancelBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelBookActionPerformed(evt);
            }
        });
        TopPanel7.add(CancelBook);

        CheckoutButton.setBackground(new java.awt.Color(22, 163, 74));
        CheckoutButton.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        CheckoutButton.setForeground(new java.awt.Color(3, 7, 18));
        CheckoutButton.setText("Checkout");
        CheckoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckoutButtonActionPerformed(evt);
            }
        });
        TopPanel7.add(CheckoutButton);

        BookingCheckinRefresh.setBackground(new java.awt.Color(59, 130, 246));
        BookingCheckinRefresh.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        BookingCheckinRefresh.setForeground(new java.awt.Color(3, 7, 18));
        BookingCheckinRefresh.setText("Refresh");
        BookingCheckinRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BookingCheckinRefreshActionPerformed(evt);
            }
        });
        TopPanel7.add(BookingCheckinRefresh);

        BookingPanel.add(TopPanel7, java.awt.BorderLayout.SOUTH);

        BookingsToolBar.setRollover(true);
        BookingsToolBar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N

        BookingTable.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        BookingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Guest ID", "Guest", "Checkin", "Checkout", "People", "Room Number", "Time Left", "Status", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        BookingTable.setSelectionBackground(new java.awt.Color(55, 48, 163));
        BookingTable.setSelectionForeground(new java.awt.Color(249, 250, 253));
        BookingTable.setShowGrid(true);
        jScrollPane4.setViewportView(BookingTable);

        BookingsToolBar.add(jScrollPane4);

        jTabbedPane1.addTab("Bookings", BookingsToolBar);

        CheckinsToolBar.setRollover(true);
        CheckinsToolBar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N

        CheckinTable.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        CheckinTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Guest ID", "Guest", "Checkin", "Checkout", "People", "Room Number", "Time Remaining", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        CheckinTable.setSelectionBackground(new java.awt.Color(55, 48, 163));
        CheckinTable.setSelectionForeground(new java.awt.Color(249, 250, 253));
        CheckinTable.setShowGrid(true);
        jScrollPane5.setViewportView(CheckinTable);

        CheckinsToolBar.add(jScrollPane5);

        jTabbedPane1.addTab("Checkins", CheckinsToolBar);

        BookingPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        MainPanel.add(BookingPanel, "BookingCard");

        GuestPanel.setBackground(new java.awt.Color(243, 244, 246));
        GuestPanel.setForeground(new java.awt.Color(243, 244, 246));
        GuestPanel.setLayout(new java.awt.BorderLayout());

        TopPanel4.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel4.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel5.setFont(new java.awt.Font("Arial Black", 0, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(249, 250, 251));
        jLabel5.setText("    GUEST");
        TopPanel4.add(jLabel5);

        GuestPanel.add(TopPanel4, java.awt.BorderLayout.PAGE_START);

        GuestFormPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(80, 82, 84)));

        jLabel7.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel7.setText("Guest Form");

        jLabel8.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel8.setText("Fullname:");

        jLabel9.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel9.setText("First Name: ");

        FirstNameTextField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        FirstNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FirstNameTextFieldActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel10.setText("Middle Name: ");

        MiddleNameTextField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        MiddleNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MiddleNameTextFieldActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel11.setText("Last Name: ");

        LastNameTextField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        LastNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LastNameTextFieldActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel12.setText("Birthday:");

        jLabel13.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel13.setText("Day: ");

        DayComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        DayComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CalculateAgeActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel14.setText("Month:");

        MonthComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        MonthComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CalculateAgeActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel15.setText("Year:");

        YearComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1900", "1901", "1902", "1903", "1904", "1905", "1906", "1907", "1908", "1909", "1910", "1911", "1912", "1913", "1914", "1915", "1916", "1917", "1918", "1919", "1920", "1921", "1922", "1923", "1924", "1925", "1926", "1927", "1928", "1929", "1930", "1931", "1932", "1933", "1934", "1935", "1936", "1937", "1938", "1939", "1940", "1941", "1942", "1943", "1944", "1945", "1946", "1947", "1948", "1949", "1950", "1951", "1952", "1953", "1954", "1955", "1956", "1957", "1958", "1959", "1960", "1961", "1962", "1963", "1964", "1965", "1966", "1967", "1968", "1969", "1970", "1971", "1972", "1973", "1974", "1975", "1976", "1977", "1978", "1979", "1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992", "1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025" }));
        YearComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CalculateAgeActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel16.setText("Gender:");

        GenderButtonGroup.add(MaleRadio);
        MaleRadio.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        MaleRadio.setText("Male");
        MaleRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MaleRadioActionPerformed(evt);
            }
        });

        GenderButtonGroup.add(FemaleRadio);
        FemaleRadio.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        FemaleRadio.setText("Female");

        jLabel17.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        jLabel17.setText("Contact:");

        jLabel18.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel18.setText("Email Address:");

        jLabel19.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel19.setText("Phone Number:");

        ClearButton.setBackground(new java.awt.Color(249, 250, 253));
        ClearButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        ClearButton.setForeground(new java.awt.Color(3, 7, 18));
        ClearButton.setText("Clear");
        ClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearButtonActionPerformed(evt);
            }
        });

        SubmitButton.setBackground(new java.awt.Color(55, 48, 163));
        SubmitButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        SubmitButton.setForeground(new java.awt.Color(249, 250, 253));
        SubmitButton.setText("Submit");
        SubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SubmitButtonActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel21.setText("Age:");

        AgeTextField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        AgeTextField.setText("0");
        AgeTextField.setEnabled(false);

        javax.swing.GroupLayout GuestFormPanelLayout = new javax.swing.GroupLayout(GuestFormPanel);
        GuestFormPanel.setLayout(GuestFormPanelLayout);
        GuestFormPanelLayout.setHorizontalGroup(
            GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GuestFormPanelLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MiddleNameTextField)
                    .addComponent(FirstNameTextField)
                    .addComponent(LastNameTextField))
                .addGap(36, 36, 36))
            .addComponent(jSeparator1)
            .addComponent(jSeparator2)
            .addComponent(jSeparator3)
            .addComponent(jSeparator4)
            .addGroup(GuestFormPanelLayout.createSequentialGroup()
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GuestFormPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(GuestFormPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ClearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SubmitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(GuestFormPanelLayout.createSequentialGroup()
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(GuestFormPanelLayout.createSequentialGroup()
                            .addComponent(jLabel14)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(MonthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 392, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GuestFormPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(GuestFormPanelLayout.createSequentialGroup()
                                .addGap(95, 95, 95)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(DayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 392, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(GuestFormPanelLayout.createSequentialGroup()
                            .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel21)
                                .addComponent(jLabel15))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(YearComboBox, 0, 392, Short.MAX_VALUE)
                                .addComponent(AgeTextField))))
                    .addGroup(GuestFormPanelLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(GuestFormPanelLayout.createSequentialGroup()
                        .addGap(92, 92, 92)
                        .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FemaleRadio)
                            .addComponent(MaleRadio)))
                    .addGroup(GuestFormPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(GuestFormPanelLayout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PhoneTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(GuestFormPanelLayout.createSequentialGroup()
                                    .addGap(14, 14, 14)
                                    .addComponent(jLabel18)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(EmailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        GuestFormPanelLayout.setVerticalGroup(
            GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GuestFormPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(FirstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(MiddleNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(LastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MonthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(YearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(AgeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MaleRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FemaleRadio)
                .addGap(18, 18, 18)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(EmailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(PhoneTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(GuestFormPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ClearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SubmitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        PendingGuestTable.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        PendingGuestTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Last Name", "First Name", "Age", "Gender", "Email", "Phone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        PendingGuestTable.setSelectionBackground(new java.awt.Color(55, 48, 163));
        PendingGuestTable.setSelectionForeground(new java.awt.Color(249, 250, 253));
        PendingGuestTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        PendingGuestTable.setShowGrid(true);
        jScrollPane2.setViewportView(PendingGuestTable);
        if (PendingGuestTable.getColumnModel().getColumnCount() > 0) {
            PendingGuestTable.getColumnModel().getColumn(0).setResizable(false);
            PendingGuestTable.getColumnModel().getColumn(1).setResizable(false);
            PendingGuestTable.getColumnModel().getColumn(2).setResizable(false);
            PendingGuestTable.getColumnModel().getColumn(3).setResizable(false);
            PendingGuestTable.getColumnModel().getColumn(4).setResizable(false);
        }

        CheckinButton.setBackground(new java.awt.Color(22, 163, 74));
        CheckinButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        CheckinButton.setForeground(new java.awt.Color(249, 250, 253));
        CheckinButton.setText("Checkin");
        CheckinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckinButtonActionPerformed(evt);
            }
        });

        BookingButton.setBackground(new java.awt.Color(22, 163, 74));
        BookingButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        BookingButton.setForeground(new java.awt.Color(249, 250, 253));
        BookingButton.setText("Booking");
        BookingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BookingButtonActionPerformed(evt);
            }
        });

        DeclineButton.setBackground(new java.awt.Color(239, 68, 68));
        DeclineButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        DeclineButton.setForeground(new java.awt.Color(249, 250, 253));
        DeclineButton.setText("Decline");
        DeclineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeclineButtonActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel20.setText("Pending Guest:");

        SearchList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Search..." };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        SearchList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(SearchList);

        ClearSearchButton.setBackground(new java.awt.Color(249, 250, 253));
        ClearSearchButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        ClearSearchButton.setForeground(new java.awt.Color(3, 7, 18));
        ClearSearchButton.setText("Clear");
        ClearSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearSearchButtonActionPerformed(evt);
            }
        });

        SearchSubmitButton.setBackground(new java.awt.Color(55, 48, 163));
        SearchSubmitButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        SearchSubmitButton.setForeground(new java.awt.Color(249, 250, 253));
        SearchSubmitButton.setText("Search");
        SearchSubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchSubmitButtonActionPerformed(evt);
            }
        });

        SubmitButton2.setBackground(new java.awt.Color(22, 163, 74));
        SubmitButton2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        SubmitButton2.setForeground(new java.awt.Color(249, 250, 253));
        SubmitButton2.setText("Checkin");
        SubmitButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchCheckinButtonActionPerformed(evt);
            }
        });

        SubmitButton3.setBackground(new java.awt.Color(22, 163, 74));
        SubmitButton3.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        SubmitButton3.setForeground(new java.awt.Color(249, 250, 253));
        SubmitButton3.setText("Booking");
        SubmitButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchBookingButtonActionPerformed(evt);
            }
        });

        RefreshButton.setBackground(new java.awt.Color(55, 48, 163));
        RefreshButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        RefreshButton.setForeground(new java.awt.Color(249, 250, 253));
        RefreshButton.setText("Refresh");
        RefreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RefreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout GridPanelLayout = new javax.swing.GroupLayout(GridPanel);
        GridPanel.setLayout(GridPanelLayout);
        GridPanelLayout.setHorizontalGroup(
            GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GridPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(GuestFormPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(GridPanelLayout.createSequentialGroup()
                        .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GridPanelLayout.createSequentialGroup()
                                .addComponent(SearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ClearSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SearchSubmitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SubmitButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SubmitButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GridPanelLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GridPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1411, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, GridPanelLayout.createSequentialGroup()
                                .addGap(0, 42, Short.MAX_VALUE)
                                .addComponent(DeclineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(GridPanelLayout.createSequentialGroup()
                                .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(BookingButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                        .addComponent(CheckinButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(RefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        GridPanelLayout.setVerticalGroup(
            GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GridPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(GridPanelLayout.createSequentialGroup()
                        .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GridPanelLayout.createSequentialGroup()
                                .addComponent(CheckinButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BookingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RefreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(DeclineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(GuestFormPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(GridPanelLayout.createSequentialGroup()
                                .addComponent(SubmitButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SubmitButton3)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(GridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(SearchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SearchSubmitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ClearSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane2))
                .addContainerGap(134, Short.MAX_VALUE))
        );

        GuestPanel.add(GridPanel, java.awt.BorderLayout.CENTER);

        MainPanel.add(GuestPanel, "GuestCard");

        RoomPanel.setBackground(new java.awt.Color(243, 244, 246));
        RoomPanel.setForeground(new java.awt.Color(243, 244, 246));
        RoomPanel.setLayout(new java.awt.BorderLayout());

        TopPanel5.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel5.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel6.setFont(new java.awt.Font("Arial Black", 0, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(249, 250, 251));
        jLabel6.setText("    ROOM");
        TopPanel5.add(jLabel6);

        RoomPanel.add(TopPanel5, java.awt.BorderLayout.PAGE_START);

        TopPanel6.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel6.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        AddRoomType.setBackground(new java.awt.Color(22, 163, 74));
        AddRoomType.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        AddRoomType.setForeground(new java.awt.Color(3, 7, 18));
        AddRoomType.setText("Add Room Type");
        AddRoomType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddRoomTypeActionPerformed(evt);
            }
        });
        TopPanel6.add(AddRoomType);

        RemoveRoomType.setBackground(new java.awt.Color(239, 68, 68));
        RemoveRoomType.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RemoveRoomType.setForeground(new java.awt.Color(3, 7, 18));
        RemoveRoomType.setText("Remove Room Type");
        RemoveRoomType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveRoomTypeActionPerformed(evt);
            }
        });
        TopPanel6.add(RemoveRoomType);

        AddRoom.setBackground(new java.awt.Color(22, 163, 74));
        AddRoom.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        AddRoom.setForeground(new java.awt.Color(3, 7, 18));
        AddRoom.setText("Add Room");
        AddRoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddRoomActionPerformed(evt);
            }
        });
        TopPanel6.add(AddRoom);

        RemoveRoomButton.setBackground(new java.awt.Color(239, 68, 68));
        RemoveRoomButton.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        RemoveRoomButton.setForeground(new java.awt.Color(3, 7, 18));
        RemoveRoomButton.setText("Remove Room");
        RemoveRoomButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveRoomButtonActionPerformed(evt);
            }
        });
        TopPanel6.add(RemoveRoomButton);

        jButton6.setBackground(new java.awt.Color(59, 130, 246));
        jButton6.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(3, 7, 18));
        jButton6.setText("Refresh");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        TopPanel6.add(jButton6);

        RoomPanel.add(TopPanel6, java.awt.BorderLayout.SOUTH);

        RoomsTable.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        RoomsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Room Number", "Type", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        RoomsTable.setSelectionBackground(new java.awt.Color(55, 48, 163));
        RoomsTable.setSelectionForeground(new java.awt.Color(249, 250, 253));
        RoomsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        RoomsTable.setShowGrid(true);
        jScrollPane1.setViewportView(RoomsTable);

        RoomPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        MainPanel.add(RoomPanel, "RoomCard");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.1;
        gridBagConstraints.weighty = 1.1;
        getContentPane().add(MainPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Showing the card layout card method
    void ShowCard(String cardName) {
        CardLayout cardLayout = (CardLayout) MainPanel.getLayout();
        cardLayout.show(MainPanel, cardName + "Card");
    }
    
    // Showing pending guest method
    void LoadPendingGuest() {
        
        // Create table model
        DefaultTableModel tableModel = (DefaultTableModel) PendingGuestTable.getModel();
        tableModel.setRowCount(0);
        
        // Execute Database method for that
        ResultSet data = db.LoadGuests("pending");
        
        // Setting the data for pending guest table
        try {
            
            while (data.next()) {
                Object[] row = {
                    data.getInt("guest_id"),
                    data.getString("last_name"),
                    data.getString("first_name"),
                    data.getInt("age"),
                    data.getString("gender"),
                    data.getString("email"),
                    data.getString("phone_number")
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error on setting guest for pending guest table.", "Pending Guest Table Load Failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    // Showing the rooms
    void LoadRooms() {
        
        // Create table model
        DefaultTableModel tableModel = (DefaultTableModel) RoomsTable.getModel();
        tableModel.setRowCount(0);
        
        // Execute Database method for that
        ResultSet data = db.LoadRooms("All");
        
        // Setting the data for rooms table
        try {
            
            while (data.next()) {
                Object[] row = {
                    data.getString("room_number"),
                    data.getString("room_type"),
                    data.getString("status"),
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error on setting room for rooms table.", "Rooms Table Load Failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    // Showing the staffs
    void LoadStaffs() {
        
        // Create table model
        DefaultTableModel tableModel = (DefaultTableModel) StaffsTable.getModel();
        tableModel.setRowCount(0);
        
        // Execute Database method for that
        ResultSet data = db.LoadStaffs();
        
        // Setting the data for rooms table
        try {
            
            while (data.next()) {
                Object[] row = {
                    data.getInt("id"),
                    data.getString("last_name"),
                    data.getString("first_name"),
                    data.getString("middle_name"),
                    data.getString("gender"),
                    data.getString("email_address"),
                    data.getString("phone_number"),
                    data.getString("username"),
                    data.getString("password")
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error on setting staff for staffs table.", "Staffs Load Failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    // Showing the staffs
    void LoadLogs() {
        
        // Create table model
        DefaultTableModel tableModel = (DefaultTableModel) LogsTable.getModel();
        tableModel.setRowCount(0);
        
        // Execute Database method for that
        ResultSet data = db.LoadLogs();
        
        // Setting the data for rooms table
        try {
            
            while (data.next()) {
                Object[] row = {
                    data.getInt("staff_id"),
                    data.getTime("time_in"),
                    data.getTime("time_out"),
                    data.getInt("working_hours"),
                    data.getDate("log_date")
                        
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error on setting log for logs table.", "Logs Load Failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    // Showing the bookings and checkins
    void LoadTransactions() {
        
        // Create table model
        DefaultTableModel tableModel = (DefaultTableModel) TransactionsTable.getModel();
        tableModel.setRowCount(0);
        
        // Execute Database method for that
        ResultSet data = db.LoadTransaction(isAdmin, staff_id, false);
        
        // Setting the data for rooms table
        try {
            
            while (data.next()) {
                Object[] row = {
                    data.getString("type"),
                    data.getString("guest_last_name") + " " + data.getString("guest_first_name"),
                    data.getInt("people"),
                    data.getString("category"),
                    data.getString("duration"),
                    data.getTimestamp("checkin"),
                    data.getString("room"),
                    data.getTimestamp("checkout"),
                    data.getDouble("downpayment"),
                    data.getDouble("total"),
                    data.getString("staff_last_name") + " " + data.getString("staff_first_name"),
                    data.getDate("date"),
                };
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error on loading transactions table.", "Transactions Table Load Failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    // Showing the bookings and checkins
    void LoadBookingsAndCheckins() {
        // Create table model
        DefaultTableModel BookingTableModel = (DefaultTableModel) BookingTable.getModel();
        BookingTableModel.setRowCount(0);

        DefaultTableModel CheckinTableModel = (DefaultTableModel) CheckinTable.getModel();
        CheckinTableModel.setRowCount(0);

        // Execute Database method for that
        ResultSet data = db.LoadBookingsAndCheckin();

        try {
            while (data.next()) {
                int checkInId = data.getInt("check_in_id");
                int guestId = data.getInt("guest_id");
                String fullName = data.getString("last_name") + " " + data.getString("first_name") + " " + data.getString("middle_name");
                Timestamp checkin = data.getTimestamp("check_in_date");
                Timestamp checkout = data.getTimestamp("check_out_date");
                int numberOfGuests = data.getInt("number_of_guests");
                String roomNumber = data.getString("room_number");
                String status = data.getString("status");
                double total = data.getDouble("total");

                long now = System.currentTimeMillis();

                if (status.equals("Booked") || status.equals("Need to checkin")) {
                    long millisUntilCheckin = checkin.getTime() - now;
                    long hoursUntilCheckin = millisUntilCheckin / (1000 * 60 * 60);
                    long daysUntilCheckin = hoursUntilCheckin / 24;

                    // Check if guest is arriving today (0 days), with remaining hours or already past
                    if ((daysUntilCheckin == 0 && hoursUntilCheckin <= 12) || millisUntilCheckin <= 0) {
                        if (!status.equals("Need to checkin")) {
                            db.ChangeCheckinStatus(checkInId, "Need to checkin");
                            status = "Need to checkin";
                        }
                    }

                    Object[] row = {
                        checkInId,
                        guestId,
                        fullName,
                        checkin,
                        checkout,
                        numberOfGuests,
                        roomNumber,
                        daysUntilCheckin + " days / " + hoursUntilCheckin + " hours",
                        status,
                        total
                    };
                    BookingTableModel.addRow(row);

                } else if (status.equals("Active") || status.equals("Need to checkout")) {
                    long millisRemaining = checkout.getTime() - now;
                    long hoursRemaining = millisRemaining / (1000 * 60 * 60);

                    if (millisRemaining <= 0 && !status.equals("Need to checkout")) {
                        db.ChangeCheckinStatus(checkInId, "Need to checkout");
                        status = "Need to checkout";
                    }

                    Object[] row = {
                        checkInId,
                        guestId,
                        fullName,
                        checkin,
                        checkout,
                        numberOfGuests,
                        roomNumber,
                        hoursRemaining + " hours",
                        status
                    };
                    CheckinTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error on loading bookings and checkins.", "Booking or Checkin Table Load Failed", JOptionPane.ERROR_MESSAGE);
        }
}

    
    // Convert raw birthday data into Date object
    Date getBirthdayObject() {
       int day = Integer.parseInt(DayComboBox.getSelectedItem().toString());
       int month = MonthComboBox.getSelectedIndex();
       int year = Integer.parseInt(YearComboBox.getSelectedItem().toString());
       
       Calendar calendar = Calendar.getInstance();
       calendar.set(year, month, day);
       Date birthday = calendar.getTime();
       
       return birthday;
    }
    
    // Age Calculator
    int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;

        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

        // Adjust if birthday hasn't occurred yet this year
        if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH) ||
           (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) &&
            today.get(Calendar.DAY_OF_MONTH) < birthCal.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }
    
    Object getSelectedGuestFromTable() {
        
        int selectedRow = PendingGuestTable.getSelectedRow(); // Get selected row index
        if (selectedRow != -1) { // Make sure a row is selected
            int value = Integer.parseInt(PendingGuestTable.getValueAt(selectedRow, 0).toString()); 
            return value;
        } else {
            JOptionPane.showMessageDialog(this, "Make sure to select a guest from the pending guest table.", "Select a guest", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        
    }
    
    Object getSelectedGuestFromTable(boolean itsfullname) {
        
        int selectedRow = PendingGuestTable.getSelectedRow(); // Get selected row index
        if (selectedRow != -1) { // Make sure a row is selected
            String lastname = PendingGuestTable.getValueAt(selectedRow, 1).toString(); 
            String firstname = PendingGuestTable.getValueAt(selectedRow, 2).toString(); 
            String fullname = lastname + ", " + firstname;
            return fullname;
        } else {
            return null;
        }
        
    }
    
    void loadDateAndTimeToday() {
        
        // Set the today date
        LocalDate today = LocalDate.now();
        
        DateTimeFormatter todayFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String formattedDate = today.format(todayFormatter);
        Date.setText(formattedDate);
        
        // Set the now time
        LocalTime now = LocalTime.now();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        String formattedTime = now.format(timeFormatter);
        Time.setText(formattedTime);
        
        // Set day of the week
        String dayOfWeek = today.getDayOfWeek().toString();
        DayOfWeek.setText(dayOfWeek);
        
    }
    
    private void UserTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UserTabActionPerformed
        if (isAdmin) {
            
            // Prompt for password
            if (!PromptAndCheckPassword()) {
                return; // Stop if password incorrect or cancelled
            }
            
            ShowCard("User");
            this.LoadStaffs();
            this.LoadLogs();
        } else {
            JOptionPane.showMessageDialog(this, "Make sure to login as admin to access this features.", "Only Admin can access this features", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_UserTabActionPerformed

    private void DashboardTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DashboardTabActionPerformed
        ShowCard("Dashboard");
        this.loadDateAndTimeToday();
        this.LoadTransactions();
        this.LoadAnalyticsCharts();
        StaffFullnameLabel.setText("Full Name: " + this.fullname);
        StaffIDLabel.setText("ID: " + this.staff_id);
    }//GEN-LAST:event_DashboardTabActionPerformed

    private void BookingTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BookingTabActionPerformed
        ShowCard("Booking");
        this.LoadBookingsAndCheckins();
    }//GEN-LAST:event_BookingTabActionPerformed

    private void GuestTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GuestTabActionPerformed
        // Show the Guest section
        ShowCard("Guest");
        // Calculate the age text field
        this.CalculateAgeActionPerformed(evt);
        // Load the pending guest table data
        this.LoadPendingGuest();
    }//GEN-LAST:event_GuestTabActionPerformed

    private void RoomTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RoomTabActionPerformed
        ShowCard("Room");
        this.LoadRooms();
    }//GEN-LAST:event_RoomTabActionPerformed

    private void LogoutTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogoutTabActionPerformed
        int response = JOptionPane.showConfirmDialog(
            this,                             
            "Are you sure you want to Logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            // User clicked YES
            
            StaffOut = LocalTime.now();
            db.SaveLog(this.staff_id, this.StaffIn, StaffOut);
            
            Login login = new Login();
            login.setVisible(true);
            this.dispose();
        } else if (response == JOptionPane.NO_OPTION) {
            // User clicked NO
           
        }
    }//GEN-LAST:event_LogoutTabActionPerformed

    private void FirstNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FirstNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FirstNameTextFieldActionPerformed

    private void MiddleNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MiddleNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MiddleNameTextFieldActionPerformed

    private void LastNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LastNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LastNameTextFieldActionPerformed

    private void MaleRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MaleRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MaleRadioActionPerformed

    private void SubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SubmitButtonActionPerformed
       // SUBMIT ACTION EVENT ( GUEST FORM )
       
       boolean isFormValid = true;
       
       if (FirstNameTextField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Double check the credentials make sure to fill-up all required field.", "The First Name is required.", JOptionPane.ERROR_MESSAGE);
            isFormValid = false;
        }
       
       if (LastNameTextField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Double check the credentials make sure to fill-up all required field.", "The Last Name is required.", JOptionPane.ERROR_MESSAGE);
            isFormValid = false;
        }
       
       if (!MaleRadio.isSelected() && !FemaleRadio.isSelected()) {
           JOptionPane.showMessageDialog(this, "Double check the credentials make sure to select a gender.", "The Gender is required.", JOptionPane.ERROR_MESSAGE);
           isFormValid = false;
       }
       
       if (EmailTextField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Double check the credentials make sure to fill-up all required field.", "The Email Address is required.", JOptionPane.ERROR_MESSAGE);
            isFormValid = false;
        }
       
       if (PhoneTextField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Double check the credentials make sure to fill-up all required field.", "The Phone Number is required.", JOptionPane.ERROR_MESSAGE);
            isFormValid = false;
        }
       
       if (isFormValid) {
           
           // Get if a Male or Female
            String gender = MaleRadio.isSelected() ? "Male" : "Female";

            // Convert raw Date data into Date object
            Date birthday = this.getBirthdayObject();

            // Create a guest object info
            GuestInfo guest = new GuestInfo(
                    FirstNameTextField.getText(),
                    MiddleNameTextField.getText(),
                    LastNameTextField.getText(),
                    gender,
                    birthday,
                    Integer.parseInt(AgeTextField.getText()),
                    PhoneTextField.getText(),
                    EmailTextField.getText()
            );

            // Execute Databse method for that
            boolean addGuest = db.AddGuest(guest);

            if (addGuest) {
                JOptionPane.showMessageDialog(this, "Added Guest " + guest.last_name + " " + guest.first_name + " as pending guest.", "Guest Added Success", JOptionPane.INFORMATION_MESSAGE);
                this.LoadPendingGuest();
                this.ClearButtonActionPerformed(evt);
            }
           
       }
         
    }//GEN-LAST:event_SubmitButtonActionPerformed

    private void SearchSubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchSubmitButtonActionPerformed
        if (SearchTextField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "If you need to search something provide a keyword to the search text field.", "The search field is empty.", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                // Call the search method
                ResultSet result = db.SearchGuest(SearchTextField.getText());

                // Clear the current list
                DefaultListModel<String> model = (DefaultListModel<String>) SearchList.getModel();
                model.clear();

                boolean hasResult = false;

                // Loop through result set and format entries
                while (result.next()) {
                    int id = result.getInt("guest_id");
                    String first = result.getString("first_name");
                    String last = result.getString("last_name");
                    String email = result.getString("email");
                    String phone = result.getString("phone_number");

                    String entry = id + " | " + last + " " + first + " | " + email + " | " + phone;
                    model.addElement(entry);

                    hasResult = true;
                }

                if (!hasResult) {
                    model.addElement("No item found.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Something went wrong while retrieving guest data.", "Search Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_SearchSubmitButtonActionPerformed

    private void CheckinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckinButtonActionPerformed
        Object selectedGuest = this.getSelectedGuestFromTable();
        
        if (selectedGuest != null) {
            CheckinAndBooking checkin = new CheckinAndBooking(false, Integer.parseInt(selectedGuest.toString()), this.getSelectedGuestFromTable(true).toString(), this.staff_id);
            checkin.setVisible(true);
            this.LoadPendingGuest();
        }
           
    }//GEN-LAST:event_CheckinButtonActionPerformed

    private void CalculateAgeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CalculateAgeActionPerformed
        AgeTextField.setText(String.valueOf(this.calculateAge(this.getBirthdayObject())));
    }//GEN-LAST:event_CalculateAgeActionPerformed

    private void BookingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BookingButtonActionPerformed
        Object selectedGuest = this.getSelectedGuestFromTable();
        
        if (selectedGuest != null) {
            CheckinAndBooking booking = new CheckinAndBooking(true, Integer.parseInt(selectedGuest.toString()), this.getSelectedGuestFromTable(true).toString(), this.staff_id);
            booking.setVisible(true);
            this.LoadPendingGuest();
        }
    }//GEN-LAST:event_BookingButtonActionPerformed

    private void SearchCheckinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchCheckinButtonActionPerformed
        String selectedValue = SearchList.getSelectedValue();
        
        if (selectedValue != null) {
            
            String[] parts = selectedValue.split("\\|");
            String id = parts[0].trim();          
            String fullname = parts[1].trim();
            
            CheckinAndBooking checkin = new CheckinAndBooking(false, Integer.parseInt(id), fullname, this.staff_id);
            checkin.setVisible(true);
        }
        
    }//GEN-LAST:event_SearchCheckinButtonActionPerformed

    private void SearchBookingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchBookingButtonActionPerformed
        String selectedValue = SearchList.getSelectedValue();
        
        if (selectedValue != null) {
            
            String[] parts = selectedValue.split("\\|");
            String id = parts[0].trim();          
            String fullname = parts[1].trim();
            
            CheckinAndBooking booking = new CheckinAndBooking(true, Integer.parseInt(id), fullname, this.staff_id);
            booking.setVisible(true);
        }
    }//GEN-LAST:event_SearchBookingButtonActionPerformed

    private void ClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearButtonActionPerformed
        FirstNameTextField.setText("");
        MiddleNameTextField.setText("");
        LastNameTextField.setText("");
        GenderButtonGroup.clearSelection();
        EmailTextField.setText("");
        PhoneTextField.setText("");
    }//GEN-LAST:event_ClearButtonActionPerformed

    private void RefreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshButtonActionPerformed
        this.LoadPendingGuest();
    }//GEN-LAST:event_RefreshButtonActionPerformed

    private void ClearSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearSearchButtonActionPerformed
         // Clear the text field
        SearchTextField.setText("");

        // Clear the list
        DefaultListModel<String> model = (DefaultListModel<String>) SearchList.getModel();
        model.clear();
    }//GEN-LAST:event_ClearSearchButtonActionPerformed

    private void DeclineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclineButtonActionPerformed
        int selectedGuest = Integer.parseInt(this.getSelectedGuestFromTable().toString());
        if (db.DeclineGuest(selectedGuest)) {
            JOptionPane.showMessageDialog(this, "Decline guests successful It's now deleted to the record.", "Guest has been decline", JOptionPane.INFORMATION_MESSAGE);
            this.LoadPendingGuest();
        }
    }//GEN-LAST:event_DeclineButtonActionPerformed

    private void RemoveRoomButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveRoomButtonActionPerformed
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "Make sure to login as admin to access this feature.", "Only Admin Can Access", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prompt for password
        if (!PromptAndCheckPassword()) {
            return; // Stop if password is incorrect or cancelled
        }

        int selectedRow = RoomsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get room ID from first column
        Object roomIdObj = RoomsTable.getValueAt(selectedRow, 0);
        if (roomIdObj == null) {
            JOptionPane.showMessageDialog(this, "Selected room ID is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String roomId = roomIdObj.toString();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove room ID: " + roomId + "?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean removed = db.RemoveRoom(roomId);
            if (removed) {
                JOptionPane.showMessageDialog(this, "Room removed successfully.");
                // Optional: refresh the table here if needed
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove room.", "Remove Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        this.LoadRooms();
    }//GEN-LAST:event_RemoveRoomButtonActionPerformed

    private void CheckinBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckinBookActionPerformed
        int selectedRow = BookingTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = BookingTable.getValueAt(selectedRow, 8).toString(); // 9th column (index 8)
        if (!status.equalsIgnoreCase("Need to checkin")) {
            JOptionPane.showMessageDialog(this, "The selected booking is not eligible for check-in.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get total amount and ask for confirmation
        String totalAmountStr = BookingTable.getValueAt(selectedRow, 9).toString(); // 10th column (index 9)
        int confirm = JOptionPane.showConfirmDialog(this,
            "Guest needs to pay a total of ₱" + totalAmountStr + ".\nProceed with check-in and confirm payment?",
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Proceed with check-in
        int checkInId = Integer.parseInt(BookingTable.getValueAt(selectedRow, 0).toString()); // 1st column (index 0)
        boolean success = db.CheckinBook(checkInId);

        if (success) {
            JOptionPane.showMessageDialog(this, "Guest successfully checked in.", "Check-in Complete", JOptionPane.INFORMATION_MESSAGE);
            // Optionally refresh the booking table here
        }
        
         this.LoadBookingsAndCheckins();
    }//GEN-LAST:event_CheckinBookActionPerformed

    private void AddRoomTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddRoomTypeActionPerformed
        if (isAdmin) {
            AddRoomType addRoomType = new AddRoomType(this.staff_id);
            addRoomType.setVisible(true);
            this.LoadRooms();
        } else {
            JOptionPane.showMessageDialog(this, "Make sure to login as admin to access this features.", "Only Admin can access this features", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_AddRoomTypeActionPerformed

    private void AddRoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddRoomActionPerformed
        if (isAdmin) {
            AddRoom addRoom = new AddRoom(this.staff_id);
            addRoom.setVisible(true);
            this.LoadRooms();
        } else {
            JOptionPane.showMessageDialog(this, "Make sure to login as admin to access this features.", "Only Admin can access this features", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_AddRoomActionPerformed

    private void RemoveRoomTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveRoomTypeActionPerformed
         if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "Make sure to login as admin to access this feature.", "Only Admin Can Access", JOptionPane.ERROR_MESSAGE);
            return;
            }

            // Prompt for password
            if (!PromptAndCheckPassword()) {
                return; // Stop if password incorrect or cancelled
            }

            // Load room types from DB
            ResultSet rs = db.LoadRoomTypeName();
            if (rs == null) {
                return; // Already handled with an error message in db method
            }

            JComboBox<String> roomTypeComboBox = new JComboBox<>();

            try {
                while (rs.next()) {
                    roomTypeComboBox.addItem(rs.getString("type"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load room types.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(
                this,
                roomTypeComboBox,
                "Select Room Type to Remove",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String selectedType = (String) roomTypeComboBox.getSelectedItem();

                if (selectedType == null || selectedType.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No room type selected.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to remove room type: " + selectedType + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean removed = db.RemoveRoomType(selectedType);
                    if (removed) {
                        JOptionPane.showMessageDialog(this, "Room type removed successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove room type.", "Remove Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
           this.LoadRooms();
    }//GEN-LAST:event_RemoveRoomTypeActionPerformed

    private void CancelBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelBookActionPerformed
        int selectedRow = BookingTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int checkInId = Integer.parseInt(BookingTable.getValueAt(selectedRow, 0).toString()); // 1st column (index 0)

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this booking?",
            "Confirm Cancel Booking",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = db.CheckoutBook(checkInId);

        if (success) {
            JOptionPane.showMessageDialog(this, "Booking successfully cancelled.", "Cancel Success", JOptionPane.INFORMATION_MESSAGE);
            // Optionally refresh BookingTable here
        }
        
         this.LoadBookingsAndCheckins();
    }//GEN-LAST:event_CancelBookActionPerformed

    private void CheckoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckoutButtonActionPerformed
       int selectedRow = CheckinTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a check-in record to checkout.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = CheckinTable.getValueAt(selectedRow, 8).toString(); // Assuming 9th column is status (index 8)

        if (!status.equalsIgnoreCase("Need to checkout")) {
            JOptionPane.showMessageDialog(this, "Only records with status 'Need to checkout' can be checked out.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int checkInId = Integer.parseInt(CheckinTable.getValueAt(selectedRow, 0).toString()); // 1st column (index 0)

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to checkout this guest?",
            "Confirm Checkout",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = db.CheckoutBook(checkInId);

        if (success) {
            JOptionPane.showMessageDialog(this, "Checkout successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Optionally refresh CheckinTable here
        }
        
         this.LoadBookingsAndCheckins();
    }//GEN-LAST:event_CheckoutButtonActionPerformed

    private void RemoveStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveStaffActionPerformed
         if (isAdmin) {
            int selectedRow = StaffsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a staff to remove.", "No Staff Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!PromptAndCheckPassword()) {
                return;
            }

            // Get the staff ID from the first column
            Object idObject = StaffsTable.getValueAt(selectedRow, 0);
            if (idObject == null) {
                JOptionPane.showMessageDialog(this, "Invalid staff ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idObject.toString());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Staff ID is not a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this staff?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                
                if (db.RemoveStaff(id)) { 
                    JOptionPane.showMessageDialog(this, "Staff removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                
                this.LoadStaffs();
            }

        } else {
            JOptionPane.showMessageDialog(this, "Make sure to login as admin to access this feature.", "Only Admin Can Access", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_RemoveStaffActionPerformed

    private void AddStaffButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddStaffButtonActionPerformed
        if (isAdmin) {
            AddStaff addStaff = new AddStaff(this.staff_id);
            addStaff.setVisible(true);
            this.LoadStaffs();
            this.LoadLogs();
        } else {
            JOptionPane.showMessageDialog(this, "Make sure to login as admin to access this features.", "Only Admin can access this features", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_AddStaffButtonActionPerformed

    private void BookingCheckinRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BookingCheckinRefreshActionPerformed
        this.LoadBookingsAndCheckins();
    }//GEN-LAST:event_BookingCheckinRefreshActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        this.LoadRooms();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void StaffRefreahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StaffRefreahActionPerformed
        this.LoadStaffs();
        this.LoadLogs();
    }//GEN-LAST:event_StaffRefreahActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddRoom;
    private javax.swing.JButton AddRoomType;
    private javax.swing.JButton AddStaffButton;
    private javax.swing.JTextField AgeTextField;
    private javax.swing.JButton BookingButton;
    private javax.swing.JButton BookingCheckinRefresh;
    private javax.swing.JPanel BookingPanel;
    private javax.swing.JButton BookingTab;
    private javax.swing.JTable BookingTable;
    private javax.swing.JToolBar BookingsToolBar;
    private javax.swing.JButton CancelBook;
    private javax.swing.JPanel ChartPanelOne;
    private javax.swing.JPanel ChartPanelTwo2;
    private javax.swing.JButton CheckinBook;
    private javax.swing.JButton CheckinButton;
    private javax.swing.JTable CheckinTable;
    private javax.swing.JToolBar CheckinsToolBar;
    private javax.swing.JButton CheckoutButton;
    private javax.swing.JButton ClearButton;
    private javax.swing.JButton ClearSearchButton;
    private javax.swing.JPanel DashboardPanel;
    private javax.swing.JButton DashboardTab;
    private javax.swing.JLabel Date;
    private javax.swing.JComboBox<String> DayComboBox;
    private javax.swing.JLabel DayOfWeek;
    private javax.swing.JButton DeclineButton;
    private javax.swing.JTextField EmailTextField;
    private javax.swing.JRadioButton FemaleRadio;
    private javax.swing.JTextField FirstNameTextField;
    private javax.swing.ButtonGroup GenderButtonGroup;
    private javax.swing.JPanel GridPanel;
    private javax.swing.JPanel GuestFormPanel;
    private javax.swing.JPanel GuestPanel;
    private javax.swing.JButton GuestTab;
    private javax.swing.JTextField LastNameTextField;
    private javax.swing.JButton LogoutTab;
    private javax.swing.JTable LogsTable;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JRadioButton MaleRadio;
    private javax.swing.JTextField MiddleNameTextField;
    private javax.swing.JComboBox<String> MonthComboBox;
    private javax.swing.JTable PendingGuestTable;
    private javax.swing.JTextField PhoneTextField;
    private javax.swing.JButton RefreshButton;
    private javax.swing.JButton RemoveRoomButton;
    private javax.swing.JButton RemoveRoomType;
    private javax.swing.JButton RemoveStaff;
    private javax.swing.JPanel RoomPanel;
    private javax.swing.JButton RoomTab;
    private javax.swing.JTable RoomsTable;
    private javax.swing.JList<String> SearchList;
    private javax.swing.JButton SearchSubmitButton;
    private javax.swing.JTextField SearchTextField;
    private javax.swing.JPanel SidePanel;
    private javax.swing.JLabel StaffFullnameLabel;
    private javax.swing.JLabel StaffIDLabel;
    private javax.swing.JButton StaffRefreah;
    private javax.swing.JTable StaffsTable;
    private javax.swing.JButton SubmitButton;
    private javax.swing.JButton SubmitButton2;
    private javax.swing.JButton SubmitButton3;
    private javax.swing.JLabel Time;
    private javax.swing.JPanel TopPanel;
    private javax.swing.JPanel TopPanel1;
    private javax.swing.JPanel TopPanel2;
    private javax.swing.JPanel TopPanel3;
    private javax.swing.JPanel TopPanel4;
    private javax.swing.JPanel TopPanel5;
    private javax.swing.JPanel TopPanel6;
    private javax.swing.JPanel TopPanel7;
    private javax.swing.JPanel TopPanel8;
    private javax.swing.JLabel TransactionLabel;
    private javax.swing.JTable TransactionsTable;
    private javax.swing.JPanel UserPanel;
    private javax.swing.JButton UserTab;
    private javax.swing.JComboBox<String> YearComboBox;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
