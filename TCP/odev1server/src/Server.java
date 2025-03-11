import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server extends JFrame {
    private JTextField portField;
    private JButton startButton;
    private JTextArea messageArea;
    private JTextArea logArea;
    private ServerSocket serverSocket;

    public Server() {
        setTitle("Server ekranı:");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Port:"));
        portField = new JTextField("", 10);
        topPanel.add(portField);
        startButton = new JButton("Başlat");
        topPanel.add(startButton);

        messageArea = new JTextArea(10, 40);
        messageArea.setEditable(false);
        JScrollPane messageScroll = new JScrollPane(messageArea);

        logArea = new JTextArea(5, 40);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(messageScroll, BorderLayout.CENTER);
        add(logScroll, BorderLayout.SOUTH);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int port = Integer.parseInt(portField.getText());
                startServer(port);
            }
        });
    }

    private void startServer(int port) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log("Sunucu localhost:" + port + " portunda başlatıldı.");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    String clientInfo = clientSocket.getInetAddress().getHostAddress();
                    log("İstemci bağlandı: " + clientInfo);
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (IOException ex) {
                log("Hata: sunucu başlatılmadı. " + ex.getMessage());
            }
        }).start();
    }
    private void log(String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append("[" + timeStamp + "] " + message + "\n");

    }
    private class ClientHandler implements Runnable {
        private Socket socket;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                    BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    String clientIP = socket.getInetAddress().getHostAddress();
                    String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    if (!line.equalsIgnoreCase("Kapat")) {
                        messageArea.append("[" + timeStamp + "] " + clientIP + ": " + line + "\n");
                    }
                }
            } catch (IOException ex) {
                log("İstemci bağlantı hatası: " + ex.getMessage());
            } finally {
                try {
                    socket.close();
                    log("İstemci bağlantısı kapatıldı.");
                } catch (IOException ex) {
                    log("Kapatma hatası: " + ex.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Server().setVisible(true);
        });
    }
}