package chattcp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class ClienteGrupo extends JFrame implements Runnable {
    private JTextArea textArea1;
    private JTextField mensaje;
    private JButton btnEnviar;
    private JButton btnSalir;
    private Socket socket;
    private DataInputStream fentrada;
    private DataOutputStream fsalida;
    private String usuario;
    private String grupo;
    private boolean repetir = true;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long lastMessageTimestamp = 0;

    public ClienteGrupo(Socket socket, String usuario, String grupo) {
        super("Chat de Grupo: " + grupo);
        this.socket = socket;
        this.usuario = usuario;
        this.grupo = grupo;

        try {
            fentrada = new DataInputStream(socket.getInputStream());
            fsalida = new DataOutputStream(socket.getOutputStream());
            fsalida.writeUTF(usuario); // Send username to server
        } catch (IOException e) {
            System.out.println("ERROR DE E/S");
            e.printStackTrace();
            System.exit(0);
        }

        initComponents();
        loadGroupChatHistory();
        new Thread(this).start();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        textArea1 = new JTextArea();
        textArea1.setEditable(false);
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea1);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        mensaje = new JTextField();
        btnEnviar = new JButton("Enviar");
        btnSalir = new JButton("Salir");

        btnEnviar.addActionListener(e -> btnEnviarActionPerformed(e));
        btnSalir.addActionListener(e -> btnSalirActionPerformed(e));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        buttonPanel.add(btnEnviar);
        buttonPanel.add(btnSalir);
        bottomPanel.add(mensaje, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mensaje.addActionListener(e -> btnEnviarActionPerformed(e));

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                btnSalirActionPerformed(null);
            }
        });
    }

    private void loadGroupChatHistory() {
        List<ChatMessage> historial = GrupoMensajesDB.getGroupChatHistory(grupo);
        SwingUtilities.invokeLater(() -> {
            textArea1.setText("");
            for (ChatMessage msg : historial) {
                appendMessage(msg.toString(), false);
            }
        });
    }

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {
        if (!mensaje.getText().trim().isEmpty()) {
            try {
                String texto = mensaje.getText();
                fsalida.writeUTF("/grupo " + grupo + " " + texto);
                mensaje.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            if (isNewMessage) {
                try {
                    String timestampStr = text.substring(1, 20);
                    Timestamp msgTimestamp = Timestamp.valueOf(timestampStr);
                    if (msgTimestamp.getTime() <= lastMessageTimestamp) {
                        return;
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

    @Override
    public void run() {
        while(repetir) {
            try {
                String texto = fentrada.readUTF();
                if (texto.startsWith("/grupo ")) {
                    String[] parts = texto.split(" ", 3);
                    if (parts.length >= 3) {
                        String sender = parts[1];
                        String content = parts[2];
                        String formattedMessage = String.format("[%s] %s> %s",
                                sdf.format(new Timestamp(System.currentTimeMillis())),
                                sender,
                                content);
                        appendMessage(formattedMessage, true);
                    }
                } else if (texto.startsWith("/actualizar_grupo ")) {
                    loadGroupChatHistory();
                }
            } catch(IOException e) {
                if (repetir) {
                    JOptionPane.showMessageDialog(this,
                            "Conexión con el servidor perdida",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    repetir = false;
                }
            }
        }
    }
}