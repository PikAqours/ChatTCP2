package chattcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Cliente extends javax.swing.JFrame implements Runnable {
    private Socket socket = null;
    private String nombre;
    private boolean repetir = true;
    private String destinatario;
    private DataInputStream fentrada;
    private DataOutputStream fsalida;
    private String serverIP;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long lastMessageTimestamp = 0;
    private final Color PRIMARY_COLOR = new Color(75, 0, 130);      // Índigo
    private final Color ACCENT_COLOR = new Color(147, 112, 219);    // Púrpura medio
    private final Color BG_COLOR = new Color(245, 245, 250);        // Fondo claro
    private final Color TEXT_COLOR = new Color(50, 50, 50);         // Texto oscuro
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font MESSAGE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);


    // Constructor for private chat
    public Cliente(Socket s, String nombre, String destinatario) {
        super("Chat Privado: " + nombre + " - " + destinatario);
        this.destinatario = destinatario;
        this.nombre = nombre;
        this.socket = s;
        this.serverIP = s.getInetAddress().getHostAddress();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (socket != null && !socket.isClosed()) {
                        fsalida.writeUTF("*");

                        socket.close();
                    }
                    dispose();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        initComponents();

        try {
            fentrada = new DataInputStream(s.getInputStream());
            fsalida = new DataOutputStream(s.getOutputStream());
            fsalida.writeUTF(nombre);

            // Load chat history after initializing components
            loadChatHistory();

            // Start the message receiving thread
            Thread messageThread = new Thread(this);
            messageThread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al iniciar el chat",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void loadChatHistory() {
        try (Socket historySocket = new Socket(serverIP, 44446)) {
            DataOutputStream salida = new DataOutputStream(historySocket.getOutputStream());
            DataInputStream entrada = new DataInputStream(historySocket.getInputStream());

            // Request chat history from server
            String comando = "OBTENER_HISTORIAL;" + nombre + ";" + destinatario;
            salida.writeUTF(comando);

            // Read response
            String response = entrada.readUTF();
            if (response.equals("HISTORY_START")) {
                SwingUtilities.invokeLater(() -> textArea1.setText("")); // Clear current chat
                while (!(response = entrada.readUTF()).equals("HISTORY_END")) {
                    appendMessage(response, false); // Pass false to indicate this is from history
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar el historial del chat",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {
        String texto = mensaje.getText().trim();
        if(texto.isEmpty()) return;

        try {
            String mensajePrivado = "/privado " + destinatario + " " + texto;
            fsalida.writeUTF(mensajePrivado);

            // Format message with timestamp and username consistently
            String formattedMessage = String.format("[%s] %s> %s",
                    sdf.format(new Timestamp(System.currentTimeMillis())),
                    nombre,
                    texto);
            appendMessage(formattedMessage, true); // Pass true for new messages

            mensaje.setText("");
            mensaje.requestFocus();
        } catch(IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al enviar el mensaje",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(500, 600));

        // Configurar el color de la barra de título
        getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Panel superior con información del chat
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        JLabel chatLabel = new JLabel("Chat con: " + destinatario, SwingConstants.CENTER);
        chatLabel.setFont(TITLE_FONT);
        chatLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(chatLabel, BorderLayout.CENTER);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Área de chat
        textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
        textArea1.setFont(MESSAGE_FONT);
        textArea1.setBackground(Color.WHITE);
        textArea1.setForeground(TEXT_COLOR);

        // Panel de desplazamiento con borde personalizado
        JScrollPane scrollPane = new JScrollPane(textArea1);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT_COLOR;
                this.trackColor = BG_COLOR;
            }
        });

        // Panel inferior para entrada de mensaje
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Campo de texto para mensaje
        mensaje = new JTextField();
        mensaje.setFont(MESSAGE_FONT);
        mensaje.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(BG_COLOR);

        // Botones estilizados
        btnEnviar = createStyledButton("Enviar", PRIMARY_COLOR);
        btnSalir = createStyledButton("Salir", new Color(220, 53, 69));

        buttonPanel.add(btnEnviar);
        buttonPanel.add(btnSalir);

        // Configurar panel inferior
        bottomPanel.add(mensaje, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Agregar componentes al panel principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Eventos
        mensaje.addActionListener(e -> btnEnviarActionPerformed(e));
        btnEnviar.addActionListener(e -> btnEnviarActionPerformed(e));
        btnSalir.addActionListener(e -> btnSalirActionPerformed(e));

        // Borde decorativo de la ventana
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));

        // Agregar panel principal al frame
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    /**
     * Crea un botón estilizado
     */
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setPreferredSize(new Dimension(100, 35));

        // Efectos al pasar el mouse
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (text.equals("Salir")) {
                    button.setBackground(new Color(200, 35, 51));
                } else {
                    button.setBackground(ACCENT_COLOR);
                }
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }


    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            fsalida.writeUTF("*");
            repetir = false;

            socket.close();
            dispose();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String text, boolean isNewMessage) {
        if (text != null && !text.isEmpty()) {
            // For new messages (not from history), check if we've already displayed this message
            if (isNewMessage) {
                // Extract timestamp from the message format [YYYY-MM-DD HH:mm:ss]
                try {
                    String timestampStr = text.substring(1, 20);
                    Timestamp msgTimestamp = Timestamp.valueOf(timestampStr);
                    if (msgTimestamp.getTime() <= lastMessageTimestamp) {
                        return; // Skip if we've already shown this message
                    }
                    lastMessageTimestamp = msgTimestamp.getTime();
                } catch (Exception e) {
                    // If there's any error parsing the timestamp, just show the message
                }
            }

            SwingUtilities.invokeLater(() -> {
                if (textArea1.getText().isEmpty()) {
                    textArea1.setText(text);
                } else {
                    textArea1.append("\n" + text);
                }
                textArea1.setCaretPosition(textArea1.getDocument().getLength());
            });
        }
    }


    private void handleUpdateNotification(String sender) {
        // Reload chat history when we receive an update notification
        try {
            fsalida.writeUTF("/historial " + sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(repetir) {
            try {
                String texto = fentrada.readUTF();

                if (texto.startsWith("/privado ")) {
                    String[] parts = texto.split(" ", 3);
                    if (parts.length >= 3) {
                        String sender = parts[1];
                        String content = parts[2];
                        // Format received message with timestamp
                        String formattedMessage = String.format("[%s] %s> %s",
                                sdf.format(new Timestamp(System.currentTimeMillis())),
                                sender,
                                content);
                        appendMessage(formattedMessage, true);

                        // Request chat history update after receiving a message
                        loadChatHistory();
                    }
                } else if (texto.startsWith("/actualizar ")) {
                    // Handle update notification
                    loadChatHistory();  // Directly load chat history instead of sending another request
                } else if (texto.equals("HISTORY_START")) {
                    SwingUtilities.invokeLater(() -> {
                        textArea1.setText(""); // Clear the chat area before loading history
                    });
                    while (!(texto = fentrada.readUTF()).equals("HISTORY_END")) {
                        if (texto.startsWith("HIST:")) {
                            if (texto.startsWith("HIST:")) {
                                appendMessage(texto.substring(5), false); // Pass false for history messages
                            }
                        }
                    }
                } else if (texto.equals("*")) {
                    repetir = false;
                    JOptionPane.showMessageDialog(this,
                            "El otro usuario ha cerrado la conversación",
                            "Chat finalizado",
                            JOptionPane.INFORMATION_MESSAGE);
                    socket.close();
                    dispose();
                }
            } catch(IOException e) {
                if (repetir) {
                    JOptionPane.showMessageDialog(this,
                            "Conexión con el servidor perdida",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                repetir = false;
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                dispose();
            }
        }
    }

    // Variables declaration
    private JButton btnEnviar;
    private JButton btnSalir;
    private JTextField mensaje;
    private JTextArea textArea1;
}