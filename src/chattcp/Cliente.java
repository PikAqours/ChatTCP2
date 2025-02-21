package chattcp;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
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
                textArea1.setText(""); // Clear current chat
                while (!(response = entrada.readUTF()).equals("HISTORY_END")) {
                    appendMessage(response);
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
            appendMessage(formattedMessage);

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
        setPreferredSize(new Dimension(400, 500));

        // Initialize components
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Chat area
        textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea1);


        // Message input panel
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        mensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnSalir = new JButton("Salir");

        // Add action listeners
        btnEnviar.addActionListener(e -> btnEnviarActionPerformed(e));
        btnSalir.addActionListener(e -> btnSalirActionPerformed(e));

        // Layout bottom panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        buttonPanel.add(btnEnviar);
        buttonPanel.add(btnSalir);
        bottomPanel.add(mensaje, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Add components to main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mensaje.addActionListener(e -> btnEnviarActionPerformed(e));

        // Add main panel to frame
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
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

    private void appendMessage(String text) {
        if (text != null && !text.isEmpty()) {
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
                        appendMessage(formattedMessage);
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