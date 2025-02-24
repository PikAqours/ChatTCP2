package chattcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
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
    private JButton btnConfig;
    private Socket socket;
    private DataInputStream fentrada;
    private DataOutputStream fsalida;
    private String usuario;
    private String grupo;
    private boolean repetir = true;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long lastMessageTimestamp = 0;
    private final Color PRIMARY_COLOR = new Color(75, 0, 130);      // Índigo
    private final Color ACCENT_COLOR = new Color(147, 112, 219);    // Púrpura medio
    private final Color BG_COLOR = new Color(245, 245, 250);        // Fondo claro
    private final Color TEXT_COLOR = new Color(50, 50, 50);         // Texto oscuro
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font MESSAGE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private boolean isAdmin = false;

    private JPanel mainPanel;
    private JPanel chatPanel;
    private JPanel configPanel;
    private ConfiguracionGrupo configuracionGrupo;

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
        setPreferredSize(new Dimension(500, 600));

        // Configurar el color de la barra de título
        getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Panel superior con información del grupo
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);

        JLabel groupLabel = new JLabel("Grupo: " + grupo, SwingConstants.CENTER);
        groupLabel.setFont(TITLE_FONT);
        groupLabel.setForeground(PRIMARY_COLOR);

        // Añadir botón de configuración
        ImageIcon configIconNormal = new ImageIcon(new ImageIcon("chatTCP/res/config_icon_2.png").getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));
        ImageIcon configIconDark = new ImageIcon(new ImageIcon("chatTCP/res/config_icon_2_dark.png").getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH));

        btnConfig = new JButton(configIconNormal);
        btnConfig.setContentAreaFilled(false);
        btnConfig.setBorderPainted(false);
        btnConfig.setFocusPainted(false);
        btnConfig.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfig.addActionListener(e -> abrirConfiguracion());

        btnConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnConfig.setIcon(configIconDark);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnConfig.setIcon(configIconNormal);
            }
        });

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.setBackground(BG_COLOR);
        topRightPanel.add(btnConfig);

        headerPanel.add(groupLabel, BorderLayout.CENTER);
        headerPanel.add(topRightPanel, BorderLayout.EAST);
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

        // Evento de cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                btnSalirActionPerformed(null);
            }
        });

        // Borde decorativo de la ventana
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));

        // Agregar panel principal al frame
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        // Añadir atajos de teclado
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enviar");
        actionMap.put("enviar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEnviarActionPerformed(e);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
        actionMap.put("salir", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSalirActionPerformed(e);
            }
        });
    }
    private boolean checkAdminStatus() {
        try {
            // Create new socket for DB connection
            Socket dbSocket = new Socket("localhost", 44446);
            DataOutputStream dbOut = new DataOutputStream(dbSocket.getOutputStream());
            DataInputStream dbIn = new DataInputStream(dbSocket.getInputStream());

            // Send command
            String comando = String.format("CHECK_ADMIN;%s;%s", grupo, usuario);
            dbOut.writeUTF(comando);

            // Read response

            boolean isAdmin = dbIn.readBoolean();


            // Close DB connection
            dbSocket.close();

            return isAdmin;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

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

    private void abrirConfiguracion() {
        if (checkAdminStatus()) {
            SwingUtilities.invokeLater(() -> {
                ConfiguracionGrupo configuracionGrupo = new ConfiguracionGrupo(socket, usuario, grupo);
                configuracionGrupo.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "No tienes permisos de administrador para configurar este grupo.",
                    "Acceso Denegado",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadGroupChatHistory() {
        List<MensajesChat> historial = GrupoMensajesDB.getGroupChatHistory(grupo);
        SwingUtilities.invokeLater(() -> {
            textArea1.setText("");
            for (MensajesChat msg : historial) {
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