package hucchu;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HotelManagementHomePage1 {

    private static JTable reservationsTable;
    private static DefaultTableModel tableModel;
    private static JFrame homePageFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createHomePage());
    }

    public static void createHomePage() {
        homePageFrame = new JFrame("Hotel Management Home Page");
        homePageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        homePageFrame.setSize(800, 400);
        homePageFrame.setLayout(new BorderLayout());
        
        // Set the background color of the home page frame to turquoise
        homePageFrame.getContentPane().setBackground(new Color(64, 224, 208)); // Turquoise color

        String[] columns = {"ID", "Name", "Email", "Phone", "Check-in", "Check-out", "Room Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        reservationsTable = new JTable(tableModel);

        // Set the table background color to dark green
        reservationsTable.setBackground(new Color(230, 230, 230)); // Dark green

        loadReservations();

        JScrollPane tableScrollPane = new JScrollPane(reservationsTable);
        homePageFrame.add(tableScrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        // Set the control panel background color to purple
        controlPanel.setBackground(new Color(200,200,255)); // Purple color

        JButton viewButton = new JButton("View Reservation");
        JButton updateButton = new JButton("Update Reservation");
        JButton removeButton = new JButton("Remove Reservation");
        JButton createButton = new JButton("Create New Reservation");

        controlPanel.add(viewButton);
        controlPanel.add(updateButton);
        controlPanel.add(removeButton);
        controlPanel.add(createButton);

        homePageFrame.add(controlPanel, BorderLayout.SOUTH);

        viewButton.addActionListener(e -> viewReservation());
        updateButton.addActionListener(e -> updateReservation());
        removeButton.addActionListener(e -> removeReservation());
        createButton.addActionListener(e -> createReservationForm());

        homePageFrame.setVisible(true);
    }

    private static void loadReservations() {
        String url = "jdbc:mysql://127.0.0.1:3306/hotel_reservation";
        String user = "root";
        String password = "root";
        String sql = "SELECT * FROM reservations";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getDate("check_in"),
                    rs.getDate("check_out"),
                    rs.getString("room_type"),
                    rs.getString("status")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(homePageFrame, "Error loading reservations: " + ex.getMessage());
        }
    }

    private static void viewReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(homePageFrame, "Please select a reservation to view.");
            return;
        }

        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        String url = "jdbc:mysql://127.0.0.1:3306/hotel_reservation";
        String user = "root";
        String password = "root";
        String sql = "SELECT * FROM reservations WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String reservationDetails = "ID: " + rs.getInt("id") + "\n"
                        + "Name: " + rs.getString("name") + "\n"
                        + "Email: " + rs.getString("email") + "\n"
                        + "Phone: " + rs.getString("phone") + "\n"
                        + "Check-in: " + rs.getDate("check_in") + "\n"
                        + "Check-out: " + rs.getDate("check_out") + "\n"
                        + "Room Type: " + rs.getString("room_type") + "\n"
                        + "Status: " + rs.getString("status");

                JOptionPane.showMessageDialog(homePageFrame, reservationDetails);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(homePageFrame, "Error fetching reservation: " + ex.getMessage());
        }
    }

    private static void updateReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(homePageFrame, "Please select a reservation to update.");
            return;
        }

        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        String newStatus = JOptionPane.showInputDialog(homePageFrame, "Enter new status (Pending/Confirmed):");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            JOptionPane.showMessageDialog(homePageFrame, "Invalid status entered.");
            return;
        }

        String url = "jdbc:mysql://127.0.0.1:3306/hotel_reservation";
        String user = "root";
        String password = "root";
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, reservationId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(homePageFrame, "Reservation updated successfully.");
                loadReservations();
            } else {
                JOptionPane.showMessageDialog(homePageFrame, "Failed to update reservation.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(homePageFrame, "Error updating reservation: " + ex.getMessage());
        }
    }

    private static void removeReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(homePageFrame, "Please select a reservation to remove.");
            return;
        }

        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirmation = JOptionPane.showConfirmDialog(homePageFrame,
                "Are you sure you want to delete this reservation?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            String url = "jdbc:mysql://127.0.0.1:3306/hotel_reservation";
            String user = "root";
            String password = "root";
            String sql = "DELETE FROM reservations WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, reservationId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(homePageFrame, "Reservation removed successfully.");
                    loadReservations();
                } else {
                    JOptionPane.showMessageDialog(homePageFrame, "Failed to remove reservation.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(homePageFrame, "Error removing reservation: " + ex.getMessage());
            }
        }
    }

    public static void createReservationForm() {
        JFrame frame = new JFrame("Create Reservation");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridLayout(8, 2));

        JLabel label1 = new JLabel("Name:");
        JTextField textField1 = new JTextField();
        JLabel label2 = new JLabel("Email:");
        JTextField textField2 = new JTextField();
        JLabel label3 = new JLabel("Phone:");
        JTextField textField3 = new JTextField();
        JLabel label4 = new JLabel("Check-in Date:");
        JTextField textField4 = new JTextField();
        JLabel label5 = new JLabel("Check-out Date:");
        JTextField textField5 = new JTextField();
        JLabel label6 = new JLabel("Room Type:");
        JComboBox<String> comboBox = new JComboBox<>(new String[] {"Single", "Double", "Suite"});
        JLabel label7 = new JLabel("Status:");
        JComboBox<String> statusComboBox = new JComboBox<>(new String[] {"Pending", "Confirmed"});

        JButton submitButton = new JButton("Submit Reservation");

        frame.add(label1);
        frame.add(textField1);
        frame.add(label2);
        frame.add(textField2);
        frame.add(label3);
        frame.add(textField3);
        frame.add(label4);
        frame.add(textField4);
        frame.add(label5);
        frame.add(textField5);
        frame.add(label6);
        frame.add(comboBox);
        frame.add(label7);
        frame.add(statusComboBox);
        frame.add(new JLabel());  // Empty space for layout
        frame.add(submitButton);

        submitButton.addActionListener(e -> {
            String name = textField1.getText();
            String email = textField2.getText();
            String phone = textField3.getText();
            String checkInDate = textField4.getText();
            String checkOutDate = textField5.getText();
            String roomType = (String) comboBox.getSelectedItem();
            String status = (String) statusComboBox.getSelectedItem();

            storeReservation(name, email, phone, checkInDate, checkOutDate, roomType, status);
            frame.dispose();  // Close the reservation form after submission
        });

        frame.setVisible(true);
    }

    public static void storeReservation(String name, String email, String phone, String checkInDate, String checkOutDate, String roomType, String status) {
        String url = "jdbc:mysql://127.0.0.1:3306/hotel_reservation";  
        String user = "root";  
        String password = "root";  
        String sql = "INSERT INTO reservations (name, email, phone, check_in, check_out, room_type, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, checkInDate);
            stmt.setString(5, checkOutDate);
            stmt.setString(6, roomType);
            stmt.setString(7, status);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Reservation made successfully!");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error making reservation: " + ex.getMessage());
        }
    }
}
