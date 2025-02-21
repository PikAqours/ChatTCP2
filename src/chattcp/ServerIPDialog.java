package chattcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Diálogo para conectar con el servidor del chat
 */
public class ServerIPDialog extends JDialog {
    // Componentes de la interfaz
    private JTextField ipField;
    private JButton connectButton;
    private String serverIP;

    // Colores y estilos
    private final Color PRIMARY_COLOR = new Color(75, 0, 130);      // Color principal (índigo)
    private final Color ACCENT_COLOR = new Color(147, 112, 219);    // Color de acento (púrpura medio)
    private final Color BG_COLOR = new Color(245, 245, 250);        // Color de fondo
    private final Color TEXT_COLOR = new Color(50, 50, 50);         // Color del texto
    private final Color ERROR_COLOR = new Color(220, 53, 69);       // Color de error
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);

    public ServerIPDialog(JFrame parent) {
        super(parent, "Conectar al servidor", true);

        // Configura el color de la barra de título
        this.getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
        this.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

        setupUI();
        setupEvents();
    }

    private void setupUI() {
        // Configura el tamaño y posición de la ventana
        setSize(450, 250); // Altura reducida de 300 a 250
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // Reducido padding vertical de 30 a 20

        // Panel del título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(BG_COLOR);
        JLabel titleLabel = new JLabel("Conexión al Servidor");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titlePanel.add(titleLabel);

        // Panel del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 5, 15, 5); // Reducido padding vertical de 20 a 15
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta IP
        JLabel ipLabel = new JLabel("IP del servidor:");
        ipLabel.setFont(MAIN_FONT);
        ipLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(ipLabel, gbc);

        // Campo de texto IP
        ipField = createStyledTextField("localhost");
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(ipField, gbc);

        // Panel del botón
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        connectButton = createStyledButton("Conectar");
        buttonPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Reducido padding vertical de 10 a 5
        buttonPanel.add(connectButton);

        // Agrega los paneles al panel principal
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createVerticalStrut(10)); // Reducido de 15 a 10
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(10)); // Reducido de 15 a 10
        mainPanel.add(buttonPanel);

        // Agrega el panel principal al diálogo
        add(mainPanel);

        // Agrega el borde decorativo
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));
    }

    /**
     * Crea un campo de texto estilizado
     */
    private JTextField createStyledTextField(String defaultText) {
        JTextField field = new JTextField(defaultText);
        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(MAIN_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Efecto de resaltado al recibir el foco
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                        BorderFactory.createEmptyBorder(4, 9, 4, 9)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });

        return field;
    }

    /**
     * Crea un botón estilizado
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setPreferredSize(new Dimension(200, 40));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Efectos al pasar el mouse
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    /**
     * Muestra un mensaje de error
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Configura los eventos de la interfaz
     */
    private void setupEvents() {
        // Evento del botón conectar
        connectButton.addActionListener(e -> {
            serverIP = ipField.getText().trim();
            if (serverIP.isEmpty()) {
                showError("Por favor, ingrese una dirección IP válida");
                ipField.setBorder(BorderFactory.createLineBorder(ERROR_COLOR, 2));
                return;
            }

            connectButton.setEnabled(false);
            connectButton.setText("Conectando...");

            // Intenta la conexión en un hilo separado para no congelar la interfaz
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    try (Socket testSocket = new Socket(serverIP, 44445)) {
                        DataOutputStream salida = new DataOutputStream(testSocket.getOutputStream());
                        salida.writeUTF("TEST");
                        return true;
                    } catch (IOException ex) {
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            dispose();
                            new LoginForm(serverIP).setVisible(true);
                        } else {
                            showError("No se encontró servidor en " + serverIP);
                            connectButton.setEnabled(true);
                            connectButton.setText("Conectar");
                        }
                    } catch (Exception ex) {
                        showError("Error al conectar: " + ex.getMessage());
                        connectButton.setEnabled(true);
                        connectButton.setText("Conectar");
                    }
                }
            }.execute();
        });

        // Validación del campo IP
        ipField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validate(); }

            private void validate() {
                String text = ipField.getText().trim();
                if (text.isEmpty()) {
                    ipField.setBorder(BorderFactory.createLineBorder(ERROR_COLOR, 2));
                } else {
                    ipField.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR),
                            BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                }
            }
        });
    }
}