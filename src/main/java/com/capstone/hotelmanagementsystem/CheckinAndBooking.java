/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.capstone.hotelmanagementsystem;

import com.capstone.hotelmanagementsystem.objects.TransactionInfo;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import org.jdesktop.swingx.JXMonthView;

/**
 *
 * @author Admin
 */
public class CheckinAndBooking extends javax.swing.JFrame {
    boolean isForBooking;
    int guest;
    int staff;
    double price;
    double downpayment;

    /**
     * Creates new form CheckinAndBooking
     */
    
    // Showing the rooms
//    void LoadAvailabeRooms() {
//        
//        // Execute Database method for that
//        ResultSet data = db.LoadRooms("Available");
//        
//        // Setting the data for room combobox
//        try {
//            
//            while (data.next()) {
//                RoomComboBox.addItem(data.getString("room_number"));
//            }
//            
//        } catch (SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Error on loading available room for combobox.", "Available Room Load Failed", JOptionPane.ERROR_MESSAGE);
//        }
//        
//    }
    
    // Set room type names
    void LoadRomsTypes() {
        
        // Execute Database method for that
        ResultSet data = Database.LoadRoomTypeName();
        
        // Setting the data for room combobox
        try {
            
            while (data.next()) {
                CategoryComboBox.addItem(data.getString("type"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error on loading room types for combobox.", "Category of Room Load Failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    private void updateCheckoutDateTimeFromDuration() {
        Date checkinDate = CheckinDatePicker.getDate();
        Date checkinTime = (Date) CheckinSpinner.getValue();

        if (checkinDate == null || checkinTime == null) return;

        // Combine checkin date and time
        Calendar checkinCal = Calendar.getInstance();
        checkinCal.setTime(checkinDate);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(checkinTime);
        checkinCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        checkinCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        checkinCal.set(Calendar.SECOND, 0);
        checkinCal.set(Calendar.MILLISECOND, 0);

        // Get selected duration
        Object selected = DurationComboBox.getSelectedItem();
        if (selected != null) {
            try {
                // Assume format like "3 Hours: 485.0"
                String selectedStr = selected.toString();
                String[] parts = selectedStr.split(" ");
                int hours = Integer.parseInt(parts[0]); // "3"

                checkinCal.add(Calendar.HOUR_OF_DAY, hours);

                // Set to checkout pickers
                Date checkout = checkinCal.getTime();
                CheckoutDatePicker.setDate(checkout);
                CheckoutSpinner.setValue(checkout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
    private Date combineDateAndTime(Date date, JSpinner timeSpinner) {
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);

        Calendar calTime = Calendar.getInstance();
        calTime.setTime((Date) timeSpinner.getValue());

        calDate.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
        calDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
        calDate.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
        calDate.set(Calendar.MILLISECOND, calTime.get(Calendar.MILLISECOND));

        return calDate.getTime();
    }
    
    // CONSTRUCTOR
   public CheckinAndBooking(boolean isForBooking, int guest, String fullname, int staff) {
        this.isForBooking = isForBooking;
        this.guest = guest;
        this.staff = staff;
        
        initComponents();
        
        Database.parent = this;

        PeopleSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
        PeopleSpinner.setValue(1);

        CheckinSpinner.setModel(new SpinnerDateModel());
        CheckinSpinner.setEditor(new JSpinner.DateEditor(CheckinSpinner, "hh:mm a"));

        CheckoutSpinner.setModel(new SpinnerDateModel());
        CheckoutSpinner.setEditor(new JSpinner.DateEditor(CheckoutSpinner, "hh:mm a"));

        Date now = new Date();

        if (this.isForBooking) {
            Title.setText("Booking");
            SubmitButton.setText("Book");

            CheckinDatePicker.setDate(now);
            CheckoutDatePicker.setDate(now);
            CheckinSpinner.setValue(now);
            CheckoutSpinner.setValue(now);
            
            CheckoutDatePicker.setEditable(false);
            CheckoutSpinner.setEnabled(false);


        } else {
            Title.setText("Checkin");
            SubmitButton.setText("Checkin");

            CheckinDatePicker.setDate(now);
            CheckinDatePicker.setEditable(false);

            CheckinSpinner.setValue(now);
            CheckinSpinner.setEnabled(false);

            Date defaultCheckout = new Date(now.getTime() + 3600 * 1000); // +1 hour
            CheckoutDatePicker.setDate(defaultCheckout);
            CheckoutSpinner.setValue(defaultCheckout);
            
            CheckoutDatePicker.setEditable(false);
            CheckoutSpinner.setEnabled(false);
        }

//        this.LoadAvailabeRooms();
        
//        if (RoomComboBox.getItemCount() > 0) {
//            RoomComboBox.setSelectedIndex(0);
//        }

        GuestTextField.setText(fullname);
        
        // Duration
        this.LoadRomsTypes();
        
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TopPanel4 = new javax.swing.JPanel();
        Title = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        GuestTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Title1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        Title2 = new javax.swing.JLabel();
        CategoryLabel = new javax.swing.JLabel();
        DurationLabel = new javax.swing.JLabel();
        DownpaymentLabel = new javax.swing.JLabel();
        PriceLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        SubmitButton = new javax.swing.JButton();
        PeopleSpinner = new javax.swing.JSpinner();
        CategoryComboBox = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        DurationComboBox = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        CheckinDatePicker = new org.jdesktop.swingx.JXDatePicker();
        jLabel12 = new javax.swing.JLabel();
        RoomComboBox = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        CheckoutDatePicker = new org.jdesktop.swingx.JXDatePicker();
        CheckoutSpinner = new javax.swing.JSpinner();
        CheckinSpinner = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setLocation(new java.awt.Point(0, 0));
        setResizable(false);
        setType(java.awt.Window.Type.POPUP);

        TopPanel4.setBackground(new java.awt.Color(49, 46, 129));
        TopPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        TopPanel4.setPreferredSize(new java.awt.Dimension(1009, 40));
        TopPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        Title.setFont(new java.awt.Font("Arial Black", 0, 16)); // NOI18N
        Title.setForeground(new java.awt.Color(249, 250, 251));
        Title.setText("   TEXT");
        TopPanel4.add(Title);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Guest:");

        GuestTextField.setEnabled(false);

        jLabel2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel2.setText("People:");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel3.setText("Category:");

        Title1.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        Title1.setText("Transaction Information");

        Title2.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        Title2.setText("Payment");

        CategoryLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        CategoryLabel.setText("Category: ");

        DurationLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        DurationLabel.setText("Duration: ");

        DownpaymentLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        DownpaymentLabel.setText("Down payment: ");

        PriceLabel.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        PriceLabel.setText("Price: ");

        SubmitButton.setBackground(new java.awt.Color(49, 46, 129));
        SubmitButton.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        SubmitButton.setForeground(new java.awt.Color(249, 250, 251));
        SubmitButton.setText("Text");
        SubmitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SubmitButtonActionPerformed(evt);
            }
        });

        CategoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CategoryComboBoxActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel4.setText("Duration:");

        DurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DurationComboBoxActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel11.setText("Checkin:");

        CheckinDatePicker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckinDatePickerActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel12.setText("Room:");

        RoomComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));
        RoomComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RoomComboBoxActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel13.setText("Checkout:");

        CheckinSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                CheckinSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(TopPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(2, 2, 2))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jSeparator1)
                .addContainerGap())
            .addComponent(jSeparator2)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Title2)
                            .addComponent(CategoryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DurationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DownpaymentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGap(36, 36, 36)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(Title1)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(PeopleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(GuestTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(CategoryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(DurationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel11)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(CheckinSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(CheckinDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(52, 52, 52)
                                    .addComponent(jLabel12)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel13)
                                    .addGap(8, 8, 8)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(CheckoutDatePicker, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                                .addComponent(RoomComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(CheckoutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE))))))
                .addContainerGap(28, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SubmitButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PriceLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(TopPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(Title1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(GuestTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(PeopleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(CategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(DurationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CheckinDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(3, 3, 3)
                .addComponent(CheckinSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(RoomComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CheckoutDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CheckoutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Title2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CategoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DurationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DownpaymentLabel)
                .addGap(18, 18, 18)
                .addComponent(PriceLabel)
                .addGap(13, 13, 13)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SubmitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SubmitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SubmitButtonActionPerformed
        
        Date checkinDateTime = combineDateAndTime(CheckinDatePicker.getDate(), CheckinSpinner);
        Date checkoutDateTime = combineDateAndTime(CheckoutDatePicker.getDate(), CheckoutSpinner);

        TransactionInfo transaction = new TransactionInfo(
            isForBooking ? "booking" : "checkin",
            this.guest,
            Integer.parseInt(PeopleSpinner.getValue().toString()),
            CategoryComboBox.getSelectedItem().toString(),
            DurationComboBox.getSelectedItem().toString(),
            checkinDateTime,
            RoomComboBox.getSelectedItem().toString(),
            checkoutDateTime,
            this.downpayment,
            this.price,
            this.staff,
            new Date() // transaction date (you can also use combineDateAndTime here if needed)
        );
        
        // Execute the database method for that
        boolean checkinOrBooked = Database.CheckinGuest(transaction, isForBooking ? "Booked" : "Active");
        
        if (checkinOrBooked) {
            JOptionPane.showMessageDialog(this, "Congratulations the transaction is successfull.", "Transaction Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Save the transaction
            boolean transact = Database.SaveTransaction(transaction);
           
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Something went wrong double check the credentials.", "Transaction Failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_SubmitButtonActionPerformed

    private void CategoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CategoryComboBoxActionPerformed
        String selectedCategory = CategoryComboBox.getSelectedItem().toString();
        CategoryLabel.setText("Category: " + selectedCategory);

        DurationComboBox.removeAllItems();
        RoomComboBox.removeAllItems(); // Clear previous rooms

        try {
            ResultSet data = Database.LoadRoomPrice(selectedCategory);
            while (data.next()) {
                DurationComboBox.addItem("3 Hours: " + data.getDouble("hr3_price"));
                DurationComboBox.addItem("6 Hours: " + data.getDouble("hr6_price"));
                DurationComboBox.addItem("10 Hours: " + data.getDouble("hr10_price"));
                DurationComboBox.addItem("12 Hours: " + data.getDouble("hr12_price"));
                DurationComboBox.addItem("24 Hours: " + data.getDouble("hr24_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading room prices.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_CategoryComboBoxActionPerformed

    private void DurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DurationComboBoxActionPerformed
        
        String selectedItem = DurationComboBox.getSelectedItem().toString();
        DurationLabel.setText("Duration: " + selectedItem);

        String[] splitItem = selectedItem.split(":");
        price = Double.parseDouble(splitItem[1].trim());
        downpayment = 0;

        if (isForBooking) {
            downpayment = price * 0.20;
            price = price - downpayment;
        }

        DownpaymentLabel.setText("Downpayment: " + downpayment);
        PriceLabel.setText("Price: " + price);

        this.updateCheckoutDateTimeFromDuration(); // still useful

        // ✅ Load available rooms here
        RoomComboBox.removeAllItems(); // Clear to prevent duplication

        String selectedCategory = CategoryComboBox.getSelectedItem().toString();
        Date checkinDate = CheckinDatePicker.getDate();
        Date checkinDateTime = combineDateAndTime(checkinDate, CheckinSpinner);

        try {
            String[] parts = selectedItem.split(" ");
            int hours = Integer.parseInt(parts[0]);

            Calendar cal = Calendar.getInstance();
            cal.setTime(checkinDateTime);
            cal.add(Calendar.HOUR_OF_DAY, hours);
            Date checkoutDateTime = cal.getTime();

            ResultSet roomData = Database.LoadAvailableRooms(checkinDateTime, checkoutDateTime, selectedCategory);
            while (roomData.next()) {
                RoomComboBox.addItem(roomData.getString("room_number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load available rooms.", "Room Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_DurationComboBoxActionPerformed

    private void HighlightAndBlockUnavailableDates(List<Date> dates) {
        JXMonthView monthView = CheckinDatePicker.getMonthView();

        // Highlight (flag) the unavailable dates in blue
        monthView.clearFlaggedDates();
        monthView.setFlaggedDates(dates.toArray(new Date[0]));
        monthView.setFlaggedDayForeground(Color.BLUE);

        // Disable (make unselectable) the unavailable dates
        monthView.setUnselectableDates(dates.toArray(new Date[0]));
    }

    // Helper to check if two dates are the same day
    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
            && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
    
    private void CheckinDatePickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckinDatePickerActionPerformed
       this.updateCheckoutDateTimeFromDuration();
    }//GEN-LAST:event_CheckinDatePickerActionPerformed

    private void CheckinSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_CheckinSpinnerStateChanged
        this.updateCheckoutDateTimeFromDuration();
    }//GEN-LAST:event_CheckinSpinnerStateChanged

    private void RoomComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RoomComboBoxActionPerformed
        // Prevent execution during combo box population or if nothing meaningful is selected
        Object selectedItem = RoomComboBox.getSelectedItem();

        if (selectedItem == null || selectedItem.toString().trim().isEmpty() || selectedItem.toString().equalsIgnoreCase("Select a room")) {
            return; // Don't show the dialog here, just return silently
        }

        String roomNumber = selectedItem.toString();

        // Proceed with loading and highlighting unavailable dates
        List<Date> unavailableDates = Database.LoadUnavailableCheckinDates(roomNumber);
        HighlightAndBlockUnavailableDates(unavailableDates);
        
    }//GEN-LAST:event_RoomComboBoxActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> CategoryComboBox;
    private javax.swing.JLabel CategoryLabel;
    private org.jdesktop.swingx.JXDatePicker CheckinDatePicker;
    private javax.swing.JSpinner CheckinSpinner;
    private org.jdesktop.swingx.JXDatePicker CheckoutDatePicker;
    private javax.swing.JSpinner CheckoutSpinner;
    private javax.swing.JLabel DownpaymentLabel;
    private javax.swing.JComboBox<String> DurationComboBox;
    private javax.swing.JLabel DurationLabel;
    private javax.swing.JTextField GuestTextField;
    private javax.swing.JSpinner PeopleSpinner;
    private javax.swing.JLabel PriceLabel;
    private javax.swing.JComboBox<String> RoomComboBox;
    private javax.swing.JButton SubmitButton;
    private javax.swing.JLabel Title;
    private javax.swing.JLabel Title1;
    private javax.swing.JLabel Title2;
    private javax.swing.JPanel TopPanel4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
}
