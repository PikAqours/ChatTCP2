package chattcp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Formulario de inicio de sesión para la aplicación de chat
 */
public class LoginForm extends JFrame {
    // Componentes de la interfaz
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel registerLabel;
    private String serverIP;

    // Colores y estilos (coinciden con ServerIPDialog)
    private final Color PRIMARY_COLOR = new Color(75, 0, 130);      // Color principal (índigo)
    private final Color ACCENT_COLOR = new Color(147, 112, 219);    // Color de acento (púrpura medio)
    private final Color BG_COLOR = new Color(245, 245, 250);        // Color de fondo
    private final Color TEXT_COLOR = new Color(50, 50, 50);         // Color del texto
    private final Color ERROR_COLOR = new Color(220, 53, 69);       // Color de error
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);

    public LoginForm(String serverIP) {
        this.serverIP = serverIP;
        setupUI();
        setupEvents();
    }

    private void setupUI() {
        // Configuración básica de la ventana
        setTitle("Autenticación de Usuario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        // Configura el color de la barra de título
        getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Título
        JLabel titleLabel = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos del formulario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(MAIN_FONT);
        userLabel.setForeground(TEXT_COLOR);
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        userField = createStyledTextField();
        formPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setFont(MAIN_FONT);
        passLabel.setForeground(TEXT_COLOR);
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField = createStyledPasswordField();
        formPanel.add(passwordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel inferior
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(BG_COLOR);

        // Botón de inicio de sesión
        loginButton = createStyledButton("Iniciar Sesión");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.add(loginButton);
        bottomPanel.add(buttonPanel);

        // Enlace de registro
        registerLabel = new JLabel("<html><div style='text-align: center;'>¿No tienes cuenta? " +
                "<span style='color: " + String.format("#%02x%02x%02x",
                PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue()) +
                "'>Crea una aquí</span></div></html>");
        registerLabel.setFont(MAIN_FONT);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(registerLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Borde decorativo
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));
    }

    /**
     * Crea un campo de texto estilizado
     */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(MAIN_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Efecto de foco
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                        BorderFactory.createEmptyBorder(4, 9, 4, 9)
                ));
            }
            public void focusLost(FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        return field;
    }

    /**
     * Crea un campo de contraseña estilizado
     */
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(MAIN_FONT);
        field.setForeground(TEXT_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Efecto de foco
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                        BorderFactory.createEmptyBorder(4, 9, 4, 9)
                ));
            }
            public void focusLost(FocusEvent evt) {
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
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    /**
     * Configura los eventos de la interfaz
     */
    private void setupEvents() {
        // Evento de inicio de sesión
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passwordField.getPassword());

            if (username.trim().isEmpty() || password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, complete todos los campos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            loginButton.setEnabled(false);
            loginButton.setText("Iniciando sesión...");

            // Realiza el inicio de sesión en un hilo separado
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    try (Socket socket = new Socket(serverIP, 44445)) {
                        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                        DataInputStream entrada = new DataInputStream(socket.getInputStream());
                        salida.writeUTF("LOGIN;" + username + ";" + password);
                        return entrada.readUTF();
                    } catch (IOException ex) {
                        return "ERROR";
                    }
                }

                @Override
                protected void done() {
                    try {
                        String respuesta = get();
                        if (respuesta.equals("OK")) {
                            dispose();
                            new MenuUsuarioUI(username, serverIP).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(LoginForm.this,
                                    respuesta.equals("ERROR") ?
                                            "Error al conectar con el servidor" : respuesta,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "Error al procesar la respuesta del servidor",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        loginButton.setEnabled(true);
                        loginButton.setText("Iniciar Sesión");
                    }
                }
            }.execute();
        });

        // Evento para abrir el formulario de registro
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegistroForm(serverIP).setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginForm("localhost").setVisible(true);
        });
    }
}