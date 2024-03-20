/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package exam_project;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author iseoluwaariyibi
 */
public class cashierDashboard extends javax.swing.JFrame {
int usersID = Integer.parseInt(Log_in.jTextField2.getText());
String usersName;
 notificationsFile emails = new notificationsFile();
    /**
     * Creates new form cashierDashboard
     */
    public cashierDashboard() {
        initComponents();
        setIcon();
        setCashierName();
    }

       private void setIcon() {
        String fileName = "/Users/iseoluwaariyibi/Downloads/user-sign-icon-person-symbol-human-avatar-isolated-on-white-backogrund-vector.jpg";
        Image img = Toolkit.getDefaultToolkit().createImage(fileName);
        img = img.getScaledInstance(jLabel1.getWidth(), jLabel1.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon ic = new ImageIcon(img);
        jLabel2.setIcon(ic);
    }
    
    private void setCashierName() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT FirstName FROM users WHERE StaffID = ?");
            ps.setInt(1, usersID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jLabel3.setText("Hello " + rs.getString(1));
            }
        } catch (Exception e) {

        }
    }
    
     private String getUsersName(int usersID) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT FirstName FROM users WHERE StaffID = ?");
            ps.setInt(1, usersID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                usersName = rs.getString("FirstName");
            }
        } catch (Exception e) {
        }
        return usersName;
    }

    public void setNotifications() {
        Timer timer = new Timer();
        LocalDate today = LocalDate.now();
        String usersName = getUsersName(usersID);
        TimerTask notificationTask = new TimerTask() {
        int notificationIndex = 0;
            @Override
            public void run() {
                try {
                    switch (notificationIndex) {
                        case 0:
                            birthdayNotification(today, usersName);
                            break;
                        case 1:
                            expiryDateNotification();
                            break;
                        case 2:
                            lowStockNotification();
                            break;
                    }
                    notificationIndex++;
                    if (notificationIndex >= 3) {
                        timer.cancel();
                    }
                } catch (Exception e) {
                }
            }
        };
        timer.schedule(notificationTask, 0, 5000);
    }
    
    private void sendNotifications(String title, String Message) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("/Users/iseoluwaariyibi/Downloads/bell.png");
            TrayIcon trayIcon = new TrayIcon(image, "Notification");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            trayIcon.displayMessage(title, Message, TrayIcon.MessageType.INFO);
        } catch (Exception e) {
        }
    }

    private void lowStockNotification() {
        try {
            String usersEmail = emails.getUsersEmail(usersID);
            String usersName = getUsersName(usersID);
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT quantity, productName FROM products WHERE quantity <= 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int quantity = rs.getInt(1);
                String productName = rs.getString(2);
                sendNotifications("LOW STOCK ALERT !!!", "There are only " + quantity + " pieces of " + productName + " please kindly stock up or Contact manufacturer for new products");
                emails.lowStockLevelsEmail(usersEmail, usersName, productName, quantity);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
            System.err.println(e);
        }
    }

    private void expiryDateNotification() {
        try {
            String usersEmail = emails.getUsersEmail(usersID);
            String usersName = getUsersName(usersID);
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT productName, expiryDate FROM products WHERE expiryDate < DATE_ADD(CURDATE(), INTERVAL 14 DAY)");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String productsName = rs.getString(1);
                Date expiryDate = rs.getDate(2);
                sendNotifications("EXPIRY DATE NEAR", "The following product is about to reach its expiry date " + productsName + " It would be expiring on the " + expiryDate);
                emails.expiryDateNearEmail(usersEmail, usersName, productsName, expiryDate);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
            System.err.println(e);
        }
    }

    private void birthdayNotification(LocalDate today, String usersName) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT DateOfBirth, FirstName, Email FROM users WHERE StaffID = ?");
            ps.setInt(1, usersID);
            ResultSet rs = ps.executeQuery();
            while ((rs.next())) {
                String usersFirstName = rs.getString(2);
                String usersEmail = rs.getString(3);
                LocalDate usersDob = rs.getDate(1).toLocalDate();  // Convert Date to LocalDate
                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
                int usersDayOfBirth = Integer.parseInt(dayFormatter.format(usersDob));
                int usersMonthOfBirth = Integer.parseInt(monthFormatter.format(usersDob));
                if (usersDayOfBirth == today.getDayOfMonth() && usersMonthOfBirth == today.getMonthValue()) {
                    sendNotifications("HAPPY BIRTHDAYYYY", "Happy birthday " + usersName + "! We want you to know how much we appreciate you and the work you do. Thank you for being a part of ISESEN Supermarket! ");
                    new notificationsFile().happyBirthdayEmail(usersEmail, usersFirstName);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 0, 40)); // NOI18N
        jLabel1.setText("🔔");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        jLabel2.setOpaque(true);

        jLabel3.setText("jLabel2");

        jButton1.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        jButton1.setText("CLICK HERE TO BEGIN YOUR DAY");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("LOG OUT");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 834, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(266, 266, 266))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)))
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(221, 221, 221)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(288, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        setNotifications();
    }//GEN-LAST:event_jLabel1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       new cashier_POS().setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int option = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to Log Out", "Log Out", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
        if(option == 0){
            new Log_in().setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(cashierDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(cashierDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(cashierDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(cashierDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new cashierDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
