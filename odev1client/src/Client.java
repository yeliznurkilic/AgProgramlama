import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client extends JFrame {
    private JTextField ipField, portField, messageField;
    private JButton connectButton, sendButton, disconnectButton;
    private JTextArea chatArea;
    private JTextArea logArea;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Client() {
        setTitle("Client ekranı:");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        // Üst panel: Sunucu IP, port girişi ve bağlantı butonları
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Sunucu IP:"));
        ipField = new JTextField("", 10);
        topPanel.add(ipField);
        topPanel.add(new JLabel("Port:"));
        portField = new JTextField("", 10);
        topPanel.add(portField);
        connectButton = new JButton("Bağlan");
        topPanel.add(connectButton);
        disconnectButton = new JButton("Kapat");
        disconnectButton.setEnabled(false);
        topPanel.add(disconnectButton);

        chatArea = new JTextArea(10, 40);
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Gönder");
        sendButton.setEnabled(false);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        logArea = new JTextArea(5, 40);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(chatScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Bağlan butonu
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ip = ipField.getText();
                int port = Integer.parseInt(portField.getText());
                connectToServer(ip, port);
            }
        });

        // Gönder butonu
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Kapat butonu
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });
    }

    // Sunucuya bağlanma işlemi
    private void connectToServer(String ip, int port) {
        try {
            // Sunucuya bağlanmayı dene
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log("Sunucuya bağlanıldı: " + ip + ":" + port);
            connectButton.setEnabled(false);
            sendButton.setEnabled(true);
            disconnectButton.setEnabled(true);
            // Sunucudan gelecek mesajları dinlemek için yeni bir thread başlat
            new Thread(() -> listenForMessages()).start();
        } catch (UnknownHostException ex) {
            log("Hata: Bilinen bir host bulunamadı. " + ex.getMessage());
        } catch (ConnectException ex) {
            log("Hata: Bağlantı kurulamadı. Port doğru yaz. " + ex.getMessage());
        } catch (IOException ex) {
            log("Bağlantı hatası: " + ex.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                chatArea.append("[" + timeStamp + "] " + "Sunucu: " + line + "\n");
            }
        } catch (IOException ex) {
            log("Mesaj dinleme hatası: " + ex.getMessage());
        }
    }

    private void sendMessage() {
        String msg = messageField.getText();
        if (msg.isEmpty()) return;
        out.println(msg);
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        chatArea.append("[" + timeStamp + "] " + "Ben: " + msg + "\n");
        messageField.setText("");
    }

    private void disconnect() {
        try {
            out.println("Kapat");
            socket.close();
            log("Bağlantı kesildi.");
        } catch (IOException ex) {
            log("Bağlantı kesme hatası: " + ex.getMessage());
        } finally {
            connectButton.setEnabled(true);
            sendButton.setEnabled(false);
            disconnectButton.setEnabled(false);
        }
    }

    private void log(String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append("[" + timeStamp + "] " + message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Client().setVisible(true);
        });
    }
}