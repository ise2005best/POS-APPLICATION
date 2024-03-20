## Pan-Atlantic University Cooperative Supermarket POS System (README.md)

### Project Description

This project outlines the development of a comprehensive Point-of-Sale (POS) system done for my CSC302 (Structured Programming) examinations. The system aims to automate supermarket operations, streamline workflows, and provide valuable insights for management.

### Requirements

#### User Management:

1. Register users (staff members) with details like name, date of birth, contact information, gender, and passport photograph.
2. Allow IT admins to upload passport photos via webcam or browsing.
Notifications:

3. Implement a system to notify staff (via pop-up or email) about birthdays.
#### Security:

4. Secure login authentication with role-based access control for sales, inventory, and IT departments.
5. Automatic username and password sent to users via email upon registration.
6. Enable IT admins to reset user passwords with email notification to the user.
#### Inventory Management:

7. Allow inventory staff to manage stock by adding items with details like product name, code, manufacturer, dates (manufacturing & expiry), quantity, and price.
8. Real-time stock tracking as items are sold.
9. Inventory manager can define low stock thresholds and receive alerts when reached.
10. Real-time low stock alerts via pop-up and email for cashier and inventory manager.
11. Implement real-time alerts for items nearing expiry date.
12. Notify salesperson, sales manager, and inventory manager of impending expiry dates (pop-up & email).
Prevent cashier sales of expired goods.
#### Sales Management:

13. Monitor individual cashier sales performance (accessible only to sales manager).
14. Generate sales reports for sales managers (daily, weekly, monthly, yearly).
15. Provide insights on top-selling items for sales managers.
#### Data Backup:

16. Allow IT manager to schedule automatic end-of-day database backups.
#### Cashier Interface:

17. Cashier-friendly alternative to barcode scanners: enable item search by product name.
18. Automatic calculation of total cost for items sold.
19. Automatic stock update upon purchase.
20. Generate receipts for each sale.
#### Budget Considerations:

21. Due to budget constraints, the system will utilize product name search as an alternative to barcode scanners.

### Development Status

This project is currently completed.

### Technologies Used
Programming Language: Java

Database : MySQL

Notifications: Java Tray Icons

Reports: Java Open PDF Library

User Interface: Graphical User Interface (GUI) library using Java Swing

Author

Ariyibi Joseph Iseoluwa

iseoluwaariyibi@gmail.com
