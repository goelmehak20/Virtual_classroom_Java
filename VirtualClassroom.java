import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class VirtualClassroom extends JFrame {
    private JTextArea chatTextArea;
    private JTextField messageTextField;
    private JButton sendButton;
    private JButton uploadButton;
    private JButton timetableButton; // Button to view timetable
    private JButton startClassButton; // Button to start the class
    private PrintWriter writer;
    private boolean loggedIn;
    private String username;
    private HashMap<String, Boolean> attendanceMap;
    private HashMap<String, String> assignmentMap;
    private JLabel virtualClassroomImage; // Label to display the virtual classroom image

    public VirtualClassroom(String username) {
        this.username = username;
        setTitle("Virtual Classroom");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 1));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameTextField = new JTextField();
        usernameTextField.setText(username);
        usernameTextField.setEditable(false);
        JButton loginButton = new JButton("Logged in as " + username);
        loginButton.setEnabled(false);

        loginPanel.add(usernameLabel);
        loginPanel.add(usernameTextField);
        loginPanel.add(loginButton);

        add(loginPanel, BorderLayout.NORTH);

        // Chat panel
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatTextArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());

        messageTextField = new JTextField();
        messagePanel.add(messageTextField, BorderLayout.CENTER);

        // Create a KeyListener for the messageTextField
        messageTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        messagePanel.add(sendButton, BorderLayout.EAST);

        uploadButton = new JButton("Upload Attachments");
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadAttachments();
            }
        });
        messagePanel.add(uploadButton, BorderLayout.WEST);

        chatPanel.add(messagePanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        // Timetable button
        timetableButton = new JButton("View Timetable");
        timetableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTimetable();
            }
        });
        add(timetableButton, BorderLayout.SOUTH);

        // Start Class button
        startClassButton = new JButton("Start Class");
        startClassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startClass();
            }
        });
        add(startClassButton, BorderLayout.WEST);

        // Virtual Classroom image
        virtualClassroomImage = new JLabel();
        virtualClassroomImage.setHorizontalAlignment(JLabel.CENTER);
        add(virtualClassroomImage, BorderLayout.EAST);

        setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5000); // Change "localhost" to the server IP if running on a different machine
            writer = new PrintWriter(socket.getOutputStream());
            Thread readerThread = new Thread(new ServerReader(socket));
            readerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageTextField.getText();
        if (!message.isEmpty()) {
            writer.println(username + ": " + message);
            writer.flush();
            messageTextField.setText("");
        }
    }

    private void uploadAttachments() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // Process the selected file
            String filePath = selectedFile.getAbsolutePath();
            // You can handle the file path as required (e.g., send it to the server)
            writer.println(username + ": [Attachment] " + filePath);
            writer.flush();
        }
    }

    private void showTimetable() {
        ImageIcon timetableImage = new ImageIcon("Timetable.jpg");
        JOptionPane.showMessageDialog(this, timetableImage, "Timetable", JOptionPane.PLAIN_MESSAGE);
    }

    private void startClass() {
        ImageIcon classroomImage = new ImageIcon("VirtualClassroom.jpg");
        virtualClassroomImage.setIcon(classroomImage);
    }

    private class ServerReader implements Runnable {
        private Socket socket;
        private BufferedReader reader;

        public ServerReader(Socket socket) {
            this.socket = socket;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    String finalMessage = message;
                    SwingUtilities.invokeLater(() -> chatTextArea.append(finalMessage + "\n"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Prompt for username
            String username = JOptionPane.showInputDialog(null, "Enter your username:");
            if (username != null && !username.isEmpty()) {
                new VirtualClassroom(username);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username. Exiting...");
                System.exit(0);
            }
        });
    }
}
