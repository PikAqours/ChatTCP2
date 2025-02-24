package chattcp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Formulario de registro de nuevos usuarios
 */
public class RegistroForm extends JFrame {
    // Componentes de la interfaz
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private String serverIP;

    // Colores y estilos (coinciden con los otros formularios)
    private final Color PRIMARY_COLOR = new Color(75, 0, 130);      // Color principal (índigo)
    private final Color ACCENT_COLOR = new Color(147, 112, 219);    // Color de acento (púrpura medio)
    private final Color BG_COLOR = new Color(245, 245, 250);        // Color de fondo
    private final Color TEXT_COLOR = new Color(50, 50, 50);         // Color del texto
    private final Color ERROR_COLOR = new Color(220, 53, 69);       // Color de error
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);

    public RegistroForm(String serverIP) {
        this.serverIP = serverIP;
        setupUI();
        setupEvents();
    }

    private void setupUI() {
        // Configuración básica de la ventana
        setTitle("Crear Cuenta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 280);
        setLocationRelativeTo(null);
        setResizable(false);

        // Configura el color de la barra de título
        getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Título
        JLabel titleLabel = new JLabel("Crear Cuenta", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo de usuario
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

        // Campo de contraseña
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

        // Panel del botón
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BG_COLOR);
        registerButton = createStyledButton("Crear Cuenta");
        bottomPanel.add(registerButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Borde decorativo
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));
        //Atajos
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "register");
        actionMap.put("register", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Simula el clic en el botón "Agregar"
                registerButton.doClick();
            }
        });
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
        registerButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, complete todos los campos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            registerButton.setEnabled(false);
            registerButton.setText("Creando cuenta...");

            // Realiza el registro en un hilo separado
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    try (Socket socket = new Socket(serverIP, 44445)) {
                        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                        DataInputStream entrada = new DataInputStream(socket.getInputStream());
                        salida.writeUTF("REGISTER;" + username + ";" + password);
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
                            JOptionPane.showMessageDialog(RegistroForm.this,
                                    "¡Cuenta creada exitosamente!",
                                    "Éxito",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(RegistroForm.this,
                                    respuesta.equals("ERROR") ?
                                            "Error al conectar con el servidor" : respuesta,
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(RegistroForm.this,
                                "Error al procesar la respuesta del servidor",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        registerButton.setEnabled(true);
                        registerButton.setText("Crear Cuenta");
                    }
                }
            }.execute();
        });
    }
}