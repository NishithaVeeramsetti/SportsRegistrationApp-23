import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;

public class SportsRegistrationApp {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe"; // Update with your DB details
    private static final String USER = "SYSTEM"; // Your Oracle username
    private static final String PASSWORD = "nishitha"; // Your Oracle password

    private JFrame frame;
    private JTextField studentIdField;
    private JTextField studentNameField;
    private JComboBox<String> sportComboBox;
    private JComboBox<String> participationComboBox;

    public SportsRegistrationApp() {
        // Set up the main frame
        frame = new JFrame("Sports Registration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(7, 2));

        // Initialize form fields
        studentIdField = new JTextField();
        studentNameField = new JTextField();
        sportComboBox = new JComboBox<>(new String[]{"Basketball", "Volleyball", "Badminton", "Tennis", "Table Tennis"});
        participationComboBox = new JComboBox<>(new String[]{"Single", "Double"});

        // Add components to the frame
        frame.add(new JLabel("Student ID:"));
        frame.add(studentIdField);
        frame.add(new JLabel("Student Name:"));
        frame.add(studentNameField);
        frame.add(new JLabel("Sport:"));
        frame.add(sportComboBox);
        frame.add(new JLabel("Participation Type:"));
        frame.add(participationComboBox);

        JButton addStudentButton = new JButton("Add Student");
        addStudentButton.addActionListener(new AddStudentAction());
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new RegisterAction());
        JButton deregisterButton = new JButton("Deregister");
        deregisterButton.addActionListener(new DeregisterAction());

        frame.add(addStudentButton);
        frame.add(registerButton);
        frame.add(deregisterButton);

        frame.setVisible(true);
    }

    // Method to get sport ID by name
    public int getSportIdByName(String sportName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT SPORT_ID FROM Sports WHERE SPORT_NAME = ?")) {
            stmt.setString(1, sportName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("SPORT_ID");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error retrieving sport ID: " + ex.getMessage());
        }
        return -1; // Return -1 if sport not found
    }

    // Method to check if student ID and name exist in the database
    public boolean isStudentIdValid(int studentId, String studentName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Students WHERE STUDENT_ID = ? AND STUDENT_NAME = ?")) {
            stmt.setInt(1, studentId);
            stmt.setString(2, studentName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Return true if student ID and name exist
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error checking student ID: " + ex.getMessage());
        }
        return false; // Default to false if any error occurs
    }

    // Action for adding a new student
    private class AddStudentAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                int studentID = Integer.parseInt(studentIdField.getText().trim());
                String studentName = studentNameField.getText().trim();

                // Input validation
                if (studentName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter the student name.");
                    return;
                }

                // Check for existing student ID
                if (isStudentIdAlreadyExists(studentID)) {
                    JOptionPane.showMessageDialog(frame, "Student ID already exists. Please use a different ID.");
                    return;
                }

                // Proceed with adding the new student
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                    String sql = "INSERT INTO Students (STUDENT_ID, STUDENT_NAME) VALUES (?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, studentID);
                        stmt.setString(2, studentName);
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(frame, "Student added successfully!");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Failed to add student. Please try again.");
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Database error while adding student: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid Student ID. Please enter a number.");
            }
        }

        private boolean isStudentIdAlreadyExists(int studentId) {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Students WHERE STUDENT_ID = ?")) {
                stmt.setInt(1, studentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Return true if student ID already exists
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error checking student ID: " + ex.getMessage());
            }
            return false; // Default to false if any error occurs
        }
    }

    // Action for registering
    private class RegisterAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                int studentID = Integer.parseInt(studentIdField.getText().trim());
                String studentName = studentNameField.getText().trim();
                String sport = (String) sportComboBox.getSelectedItem();
                String participationType = (String) participationComboBox.getSelectedItem();

                // Input validation
                if (studentName.isEmpty() || sport == null || participationType == null) {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
                    return;
                }

                // Map the sport name to its corresponding ID
                int sportId = getSportIdByName(sport);

                // Check if sport ID is valid
                if (sportId == -1) {
                    JOptionPane.showMessageDialog(frame, "Invalid sport selected. Please choose a valid sport.");
                    return;
                }

                // Check if student ID and name exist
                if (!isStudentIdValid(studentID, studentName)) {
                    JOptionPane.showMessageDialog(frame, "Invalid Student ID or Name. Please check your input.");
                    return;
                }

                // Check for duplicate registration
                if (isAlreadyRegistered(studentID, sportId, participationType)) {
                    JOptionPane.showMessageDialog(frame, "You are already registered for this sport with the same participation type.");
                    return;
                }

                // Proceed with registration
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                    String sql = "INSERT INTO Registrations (student_id, student_name, sport_id, participation_type) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, studentID);
                        stmt.setString(2, studentName);
                        stmt.setInt(3, sportId);
                        stmt.setString(4, participationType);

                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(frame, "Registered successfully!");

                            // Clear input fields after successful registration
                            studentIdField.setText("");
                            studentNameField.setText("");
                            sportComboBox.setSelectedIndex(0);
                            participationComboBox.setSelectedIndex(0);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Registration failed. Please try again.");
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid Student ID. Please enter a number.");
            } catch (SQLException ex) {
                String errorMessage;
                switch (ex.getErrorCode()) {
                    case 1: // Unique constraint violation
                        errorMessage = "Duplicate entry for student ID. Please use a unique ID.";
                        break;
                    case 2291: // Foreign key constraint violation
                        errorMessage = "Foreign key constraint violated. Please check the references.";
                        break;
                    default:
                        errorMessage = "Database error: " + ex.getMessage();
                }
                JOptionPane.showMessageDialog(frame, errorMessage);
            }
        }

        // Method to check if the student is already registered for the same sport
        private boolean isAlreadyRegistered(int studentId, int sportId, String participationType) {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Registrations WHERE student_id = ? AND sport_id = ? AND participation_type = ?")) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, sportId);
                stmt.setString(3, participationType);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Return true if already registered
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error checking registration: " + ex.getMessage());
            }
            return false; // Default to false if any error occurs
        }
    }

    // Action for deregistering
    private class DeregisterAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                int studentID = Integer.parseInt(studentIdField.getText().trim());
                String studentName = studentNameField.getText().trim();
                String sport = (String) sportComboBox.getSelectedItem();

                // Input validation
                if (studentName.isEmpty() || sport == null) {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
                    return;
                }

                // Get sport ID by name
                int sportId = getSportIdByName(sport);
                if (sportId == -1) {
                    JOptionPane.showMessageDialog(frame, "Invalid sport selected. Please choose a valid sport.");
                    return;
                }

                // Check if student ID and name exist
                if (!isStudentIdValid(studentID, studentName)) {
                    JOptionPane.showMessageDialog(frame, "Invalid Student ID or Name. Please check your input.");
                    return;
                }

                // Proceed with deregistration
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                    String sql = "DELETE FROM Registrations WHERE student_id = ? AND sport_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, studentID);
                        stmt.setInt(2, sportId);
                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(frame, "Deregistered successfully!");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Deregistration failed. Please check if you are registered.");
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid Student ID. Please enter a number.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Database error while deregistering: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new SportsRegistrationApp();
    }
}