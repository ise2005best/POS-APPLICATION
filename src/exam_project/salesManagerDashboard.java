/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package exam_project;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
/**
 *
 * @author iseoluwaariyibi
 */
public class salesManagerDashboard extends javax.swing.JFrame {

    int usersID = Integer.parseInt(Log_in.jTextField2.getText());
    notificationsFile emails = new notificationsFile();
    String usersName;
    int staffID = 0;

    /**
     * Creates new form salesManagerDashboard
     */
    public salesManagerDashboard() {
        initComponents();
        setIcon();
        setManagerName();

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
    
    private int getStaffID(String staffFirstName){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT staffID FROM users WHERE firstName = ? AND Position = \"Sales Person / Cashier\"");
            ps.setString(1, staffFirstName);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                staffID = rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return staffID;
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
                    }
                    notificationIndex++;
                    if (notificationIndex >= 2) {
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
   


    private void expiryDateNotification() {
        String usersEmail = emails.getUsersEmail(usersID);
        String usersName1 = getUsersName(usersID);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT productName, expiryDate FROM products WHERE expiryDate < DATE_ADD(CURDATE(), INTERVAL 14 DAY)");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String productsName = rs.getString(1);
                Date expiryDate = rs.getDate(2);
                sendNotifications("EXPIRY DATE NEAR", "The following product is about to reach its expiry date " + productsName + " It would be expiring on the " + expiryDate);
                emails.expiryDateNearEmail(usersEmail, usersName1, productsName, expiryDate);               
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
                    emails.happyBirthdayEmail(usersEmail, usersFirstName);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);

    private void setIcon() {
        String fileName = "/Users/iseoluwaariyibi/Downloads/user-sign-icon-person-symbol-human-avatar-isolated-on-white-backogrund-vector.jpg";
        Image img = Toolkit.getDefaultToolkit().createImage(fileName);
        img = img.getScaledInstance(jLabel21.getWidth(), jLabel21.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon ic = new ImageIcon(img);
        jLabel21.setIcon(ic);
    }

    private void setManagerName() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT FirstName FROM users WHERE StaffID = ?");
            ps.setInt(1, usersID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jLabel22.setText("Hello " + rs.getString(1));
            }
        } catch (Exception e) {

        }
    }

    public static void createTableHeader(PdfPTable table) {
        try {
            PdfPCell c1 = new PdfPCell(new Phrase("Product Name"));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase("Product Type"));
            c2.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Manufacturer"));
            c3.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c3);

            PdfPCell c4 = new PdfPCell(new Phrase("Total Quantity Sold"));
            c4.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c4);

            PdfPCell c5 = new PdfPCell(new Phrase("Price"));
            c5.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c5);

            PdfPCell c6 = new PdfPCell(new Phrase("Total Sales"));
            c6.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(c6);
            table.setHeaderRows(1);
        } catch (Exception e) {
        }
    }

    public static void createTableForReport(PdfPTable table, Document document, String ProductName, String ProductType, String Manufacturer, int TotalQuantitySold, Double Price, Double TotalSales) throws BadElementException {
        try {
            table.addCell(ProductName);
            table.addCell(ProductType);
            table.addCell(Manufacturer);
            table.addCell(Integer.toString(TotalQuantitySold));
            table.addCell(String.format("%.2f", Price));
            table.addCell(String.format("%.2f", TotalSales));
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
        jButton10 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jButton3 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton4 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton10.setText("LOG OUT");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jLabel22.setText("jLabel2");

        jLabel21.setOpaque(true);

        jLabel23.setFont(new java.awt.Font("Helvetica Neue", 0, 32)); // NOI18N
        jLabel23.setText("🔔");
        jLabel23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel23MouseClicked(evt);
            }
        });
        jLabel23.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jLabel23KeyPressed(evt);
            }
        });

        jLabel1.setText("Staff FirstName");

        jButton1.setText("Browse");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Generate Report");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(66, 66, 66)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(395, 395, 395)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(298, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(97, 97, 97)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(78, 78, 78)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(188, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cashier Performance", jPanel2);

        jLabel2.setFont(new java.awt.Font("Heiti TC", 0, 16)); // NOI18N
        jLabel2.setText("Generate Top Selling Item ");

        jLabel3.setText("Start Date");

        jLabel4.setText("End Date");

        jButton3.setText("Generate Report");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- SELECT PERIOD --", "Of The Day", "Of The Week", "Of The Month", "Of The Year" }));

        jButton4.setText("Generate");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Heiti TC", 0, 16)); // NOI18N
        jLabel5.setText("Generate Sales Report");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(113, 113, 113)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(79, 79, 79))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(159, 159, 159)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(87, 87, 87)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(159, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(380, 380, 380))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(413, 413, 413))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(397, 397, 397))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55))
        );

        jTabbedPane1.addTab("Sales Report", jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel23)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addComponent(jTabbedPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel23)))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(jTabbedPane1))
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

    private void jLabel23MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel23MouseClicked
        setNotifications();
    }//GEN-LAST:event_jLabel23MouseClicked

    private void jLabel23KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jLabel23KeyPressed
        
    }//GEN-LAST:event_jLabel23KeyPressed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Date startDate = jDateChooser1.getDate();
        Date endDate = jDateChooser2.getDate();
        java.sql.Date sqlStartDate = new java.sql.Date(startDate.getTime());
        java.sql.Date sqlEndDate = new java.sql.Date(endDate.getTime());
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT products.productName, products.productType, products.manufacturer, SUM(sales.quantity) AS totalQuantity, products.price, SUM(sales.totalPrice) AS totalSalesValue FROM sales LEFT JOIN products ON Sales.productID = products.productID WHERE sales.saleDate >= ? AND ? GROUP BY products.productName, products.productType, products.manufacturer, products.price ORDER BY totalQuantity DESC");
            ps.setDate(1, sqlStartDate);
            ps.setDate(2, sqlEndDate);
            ResultSet rs = ps.executeQuery();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("salesReport2.pdf"));
            PdfPTable table = new PdfPTable(6);
            document.open();
            createTableHeader(table);
            if (!rs.next()) {
                JOptionPane.showMessageDialog(rootPane, "No data found for the specified date range.");
            } else {

                do {
                    String productName = rs.getString(1);
                    String productType = rs.getString(2);
                    String manufacturer = rs.getString(3);
                    int TotalQuantitySold = rs.getInt(4);
                    Double price = rs.getDouble(5);
                    Double TotalSales = rs.getDouble(6);
                    createTableForReport(table, document, productName, productType, manufacturer, TotalQuantitySold, price, TotalSales);
                } while (rs.next());
            }
            document.add(table);
            JOptionPane.showMessageDialog(rootPane, "The Sales Report has been downloaded check your downloads to view");
            document.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        int datePeriod = jComboBox1.getSelectedIndex();
        java.sql.Date startDate;
        java.sql.Date endDate;
        long millis = System.currentTimeMillis();        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        startDate = new java.sql.Date(millis);
        // to generate top selling item for a particular period
        switch (datePeriod) {
            case 0:
                JOptionPane.showMessageDialog(rootPane, "Please select a timeframe");
                break;
            case 1: {
                calendar.setTimeInMillis(millis);
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                millis = calendar.getTimeInMillis();
                endDate = new java.sql.Date(millis);
                break;
            }
            case 2: {
                calendar.setTimeInMillis(millis);
                calendar.add(Calendar.WEEK_OF_MONTH, -1);
                millis = calendar.getTimeInMillis();
                endDate = new java.sql.Date(millis);
                break;
            }
            case 3: {
                calendar.setTimeInMillis(millis);
                calendar.add(Calendar.MONTH, -1);
                millis = calendar.getTimeInMillis();
                endDate = new java.sql.Date(millis);
                break;
            }
            case 4: {
                calendar.setTimeInMillis(millis);
                calendar.add(Calendar.YEAR, -1);
                millis = calendar.getTimeInMillis();
                endDate = new java.sql.Date(millis);
                break;
            }
            default:
                break;
        }
        endDate = new java.sql.Date(calendar.getTimeInMillis());
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT SUM(sales.quantity) AS totalQuantity, products.productName, SUM(sales.totalPrice) AS totalSalesValue FROM sales LEFT JOIN products ON Sales.productID = products.productID WHERE sales.saleDate >= ? AND ? GROUP BY products.productName ORDER BY totalQuantity DESC LIMIT 1;");
            ps.setDate(1, endDate);
            ps.setDate(2, startDate);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String productName = rs.getString(2);
                int productQuantity = rs.getInt(1);
                Double productTotalSales = rs.getDouble(3);
                System.out.println("got here");
                JOptionPane.showMessageDialog(rootPane, "The Top selling item is " + productName + " and it has been sold " + productQuantity + " with a total Sales of "+ productTotalSales + " " );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        int option = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to Log Out", "Log Out", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
        System.out.println(option);
        if(option == 0){
            new Log_in().setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       String staffFirstName = jTextField1.getText();
        int staffID = getStaffID(staffFirstName);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT SUM(totalPrice) as totalSalesPrice , count(quantity) as totalQuantity FROM sales WHERE StaffID = ?");
            ps.setInt(1, staffID);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Double totalSalesPrice = rs.getDouble(1);
                int totalQuantitySold = rs.getInt(2);
                String salesPrice = String.format("%.2f", totalSalesPrice);
                JOptionPane.showMessageDialog(rootPane, "" +staffFirstName+  " has sold over "+ totalQuantitySold + " quantity worth of products with a total sales price of " + salesPrice);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String staffFirstName = jTextField1.getText();
        int staffID = getStaffID(staffFirstName);
        if(staffID == 0){
            JOptionPane.showMessageDialog(rootPane, "User does not exist");
        }else{
            JOptionPane.showMessageDialog(rootPane, "User exists");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(salesManagerDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(salesManagerDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(salesManagerDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(salesManagerDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new salesManagerDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox<String> jComboBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
