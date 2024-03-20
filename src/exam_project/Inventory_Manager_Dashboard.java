/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package exam_project;

import java.awt.Color;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.table.DefaultTableModel;
import raven.glasspanepopup.DefaultOption;
import raven.glasspanepopup.GlassPanePopup;

/**
 *
 * @author iseoluwaariyibi
 */
public class Inventory_Manager_Dashboard extends javax.swing.JFrame {

    int productID = 0;
    String usersName;
    int usersID = Integer.parseInt(Log_in.jTextField2.getText());
    notificationsFile emails = new notificationsFile();
    String usersEmail = emails.getUsersEmail(usersID);
    

    /**
     * Creates new form Inventory_Manager_Dashboard
     */
    public Inventory_Manager_Dashboard() {
        initComponents();
        setIcon();
        setManagerName();
        displayInventory();

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
        String usersEmail1 = emails.getUsersEmail(usersID);
        String usersName1 = getUsersName(usersID);
        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT quantity, productName FROM products WHERE quantity <= 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int quantity = rs.getInt(1);
                String productName = rs.getString(2);
                sendNotifications("LOW STOCK ALERT !!!", "There are only " + quantity + " pieces of " + productName + " please kindly stock up or Contact manufacturer for new products");
                emails.lowStockLevelsEmail(usersEmail1, usersName1, productName, quantity);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
            System.err.println(e);
        }
    }

    private void expiryDateNotification() {
        String usersEmail1 = emails.getUsersEmail(usersID);
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
                emails.expiryDateNearEmail(usersEmail1, usersName1, productsName, expiryDate);
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
    
   

    private void setIcon() {
        String fileName = "/Users/iseoluwaariyibi/Downloads/user-sign-icon-person-symbol-human-avatar-isolated-on-white-backogrund-vector.jpg";
        Image img = Toolkit.getDefaultToolkit().createImage(fileName);
        img = img.getScaledInstance(jLabel1.getWidth(), jLabel1.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon ic = new ImageIcon(img);
        jLabel1.setIcon(ic);
    }

    private int checkIfProductExists(int productSerialNumber) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT productID FROM products WHERE productSerialNumber = ?");
            ps.setInt(1, productSerialNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                productID = rs.getInt(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
        return productID;
    }

    private void setManagerName() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            PreparedStatement ps = con.prepareStatement("SELECT FirstName FROM users WHERE StaffID = ?");
            ps.setInt(1, usersID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                jLabel2.setText("Hello " + rs.getString(1));
            }
        } catch (Exception e) {

        }
    }

    private void displayInventory() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
            String selectedFilter = jComboBox2.getSelectedItem().toString();
            String query = "";
            switch (selectedFilter) {
                case "Price (Lowest to Highest)":
                    query = "price";
                    break;
                case "Quantity":
                    query = "quantity";
                    break;
                case "Expiry Date":
                    query = "expiryDate";
                    break;
                case "Name (A-Z)":
                    query = "productName";
                    break;
                default:
                    query = "productID";
                    break;
            }
            PreparedStatement ps = con.prepareStatement("SELECT * FROM products ORDER BY " + query + " ASC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String productName = rs.getString(2);
                String productSn = rs.getString(3);
                String productType = rs.getString(4);
                String manufacturer = rs.getString(5);
                Date manufacturingDate = rs.getDate(6);
                Date expiryDate = rs.getDate(7);
                int quantity = rs.getInt(8);
                Double price = rs.getDouble(9);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedManufacturingDate = dateFormat.format(manufacturingDate);
                String formattedExpiryDate = dateFormat.format(expiryDate);
                DefaultTableModel td = (DefaultTableModel) jTable1.getModel();
                Object[] rowData = {productName, productSn, productType, manufacturer, formattedManufacturingDate, formattedExpiryDate, quantity, price};
                td.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e);
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel7 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel8 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setOpaque(true);

        jLabel2.setText("jLabel2");

        jLabel3.setText("Product Name");

        jLabel4.setText("Product Serial Number");

        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField2KeyPressed(evt);
            }
        });

        jLabel5.setText("Manufacturer");

        jLabel6.setText("Manufacturing Date");
        jLabel6.setLocation(new java.awt.Point(-32698, -32370));

        jDateChooser1.setMaxSelectableDate(new Date (System.currentTimeMillis() - 24 * 60 * 60 * 100));

        jLabel7.setText("Expiry Date");

        jDateChooser2.setMinSelectableDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));

        jLabel8.setText("Quantity");

        jLabel9.setText("Price");

        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField4KeyPressed(evt);
            }
        });

        jButton2.setText("INSERT");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel10.setText("Product Type");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- SELECT A PRODUCT TYPE --", "Fruits ", "Vegetables", "Bakery", "Appliances", "Electronics", "Beverages", "Dairy Products", "Poultry", "Sea Food", "Alcohol and Tobacco", "Personal Care" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(364, 364, 364))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(87, 87, 87)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1)
                    .addComponent(jTextField2)
                    .addComponent(jTextField3)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField4)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(422, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Add Inventory", jPanel2);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product Name", "Product Serial Number", "Product Type", "Manufaturer", "Manufacturing Date", "Expiry Date", "Quantity", "Price"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel11.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        jLabel11.setText("🔄");
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel11MouseClicked(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Helvetica Neue", 0, 16)); // NOI18N
        jLabel12.setText("Refresh");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Filter By --", "Price (Lowest to Highest)", "Quantity", "Expiry Date", "Name (A-Z)" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 926, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(31, 31, 31)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Inspect Inventory", jPanel3);

        jButton1.setText("LOG OUT");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Helvetica Neue", 0, 32)); // NOI18N
        jButton3.setText("🔔");
        jButton3.setBorder(null);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane1)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int productSerialNumber = Integer.parseInt(jTextField2.getText());
        int productID = checkIfProductExists(productSerialNumber);
        String productName = jTextField1.getText();
        String manufacturer = jTextField3.getText();
        Date manufacturingDate = jDateChooser1.getDate();
        java.sql.Date SqlManufacturingDate = new java.sql.Date(manufacturingDate.getTime());
        Date expiryDate = jDateChooser2.getDate();
        java.sql.Date SqlexpiringDate = new java.sql.Date(expiryDate.getTime());
        int quantity = Integer.parseInt(jSpinner1.getValue().toString());
        Double price = Double.parseDouble(jTextField4.getText());
        String productType = jComboBox1.getSelectedItem().toString();
        if (productID != 0) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
                PreparedStatement ps = con.prepareStatement("UPDATE products SET quantity = quantity + ? WHERE productID = ? ");
                ps.setInt(1, quantity);
                ps.setInt(2, productID);
                int rs = ps.executeUpdate();
                JOptionPane.showMessageDialog(rootPane, " " + productName + " already exists but the quantity has been increased");
                jTextField1.setText("");
                jTextField2.setText("");
                jTextField3.setText("");
                jTextField4.setText("");
                jComboBox1.setSelectedIndex(0);
                jDateChooser1.cleanup();
                jDateChooser2.cleanup();
                jSpinner1.setValue(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        } else {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306" + "/POS_EXAM", "root", "damilOlamide14$");
                PreparedStatement ps = con.prepareStatement("INSERT INTO products (productName, productSerialNumber, productType, manufacturer, manufacturingDate, expiryDate, quantity, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, productName);
                ps.setInt(2, productSerialNumber);
                ps.setString(3, productType);
                ps.setString(4, manufacturer);
                ps.setDate(5, SqlManufacturingDate);
                ps.setDate(6, SqlexpiringDate);
                ps.setInt(7, quantity);
                ps.setDouble(8, price);
                int rs = ps.executeUpdate();
                JOptionPane.showMessageDialog(rootPane, "Product Registered");
                jTextField1.setText("");
                jTextField2.setText("");
                jTextField3.setText("");
                jTextField4.setText("");
                jComboBox1.setSelectedIndex(0);
                jDateChooser1.cleanup();
                jDateChooser2.cleanup();
                jSpinner1.setValue(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(rootPane, e);
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyPressed
        String field = jTextField2.getText();
        int length = field.length();
        // ensure only the word only contains digits
        if (evt.getKeyChar() >= '0' && evt.getKeyChar() <= '9') {
            jTextField2.setEditable(true);
        } else {
            // allows the user to delete a digit
            if (evt.getKeyCode() == KeyEvent.VK_DELETE || evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                jTextField2.setEditable(true);
            } else {
                jTextField2.setEditable(false);
            }
        }
    }//GEN-LAST:event_jTextField2KeyPressed

    private void jTextField4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField4KeyPressed
        String field = jTextField4.getText();
        int length = field.length();
        // ensure only the word only contains digits
        if ((evt.getKeyChar() >= '0' && evt.getKeyChar() <= '9')
                || // Allow only one decimal point
                (evt.getKeyChar() == '.' && !field.contains("."))) {
            jTextField4.setEditable(true);
        } else {
            // allows the user to delete a digit
            if (evt.getKeyCode() == KeyEvent.VK_DELETE || evt.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                jTextField4.setEditable(true);
            } else {
                jTextField4.setEditable(false);
            }
        }
    }//GEN-LAST:event_jTextField4KeyPressed

    private void jLabel11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseClicked
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        displayInventory();
    }//GEN-LAST:event_jLabel11MouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        setNotifications();

//GlassPanePopup.showPopup(new notifications());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        displayInventory();
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int option = JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to Log Out", "Log Out", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
        if(option == 0){
            new Log_in().setVisible(true);
            dispose();
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
            java.util.logging.Logger.getLogger(Inventory_Manager_Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Inventory_Manager_Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Inventory_Manager_Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Inventory_Manager_Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Inventory_Manager_Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
