package chattcp.Interfaces;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracionGrupo extends JFrame {
    // Constants for colors and fonts
    private static final Color PRIMARY_COLOR = new Color(75, 0, 130);      // Índigo
    private static final Color ACCENT_COLOR = new Color(147, 112, 219);    // Púrpura medio
    private static final Color BG_COLOR = new Color(245, 245, 250);        // Fondo claro
    private static final Color TEXT_COLOR = new Color(50, 50, 50);         // Texto oscuro
    private static final Color DELETE_COLOR = new Color(220, 53, 69);      // Rojo para eliminar
    private static final Color CANCEL_COLOR = new Color(108, 117, 125);    // Gris para cancelar

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font MESSAGE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private static final Dimension MAIN_LIST_SIZE = new Dimension(200, 120); // Size for main list
    private static final Dimension ADD_LIST_SIZE = new Dimension(200, 100);  // Size for add users list
    // Instance variables
    private final Socket socket;
    private final String usuario;
    private final String grupo;
    private final String serverIP;

    // UI Components
    private JTextField txtNombreGrupo;
    private JList<String> listaUsuarios;
    private DefaultListModel<String> listModelUsuarios;
    private JTextField txtAgregarUsuario;
    private JButton btnAgregarUsuario;
    private JButton btnEliminarUsuario;
    private JButton btnPromoverAdmin;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private JList<String> listaUsuariosDisponibles;
    private DefaultListModel<String> listModelUsuariosDisponibles;

    public ConfiguracionGrupo(Socket socket, String usuario, String grupo) {
        super("Configuración del Grupo: " + grupo);
        this.socket = socket;
        this.usuario = usuario;
        this.grupo = grupo;
        this.serverIP = socket.getInetAddress().getHostAddress();

        initComponents();
        cargarUsuariosDelGrupo();
    }

    private void initComponents() {
        setupWindow();
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void setupWindow() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 600));
        setResizable(false);
        getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        headerPanel.setBackground(BG_COLOR);

        JLabel titleLabel = new JLabel("Configuración del Grupo", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Grupo nombre section
        addGrupoNombreSection(contentPanel, gbc);

        // Usuarios list section
        addUsuariosListSection(contentPanel, gbc);

        // Agregar usuario section
        addAgregarUsuarioSection(contentPanel, gbc);

        return contentPanel;
    }

    private void addGrupoNombreSection(JPanel panel, GridBagConstraints gbc) {
        JLabel lblNombreGrupo = createLabel("Nombre del Grupo:");
        txtNombreGrupo = createTextField(grupo);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(lblNombreGrupo, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(txtNombreGrupo, gbc);
    }

    private void addUsuariosListSection(JPanel panel, GridBagConstraints gbc) {
        JLabel lblUsuarios = createLabel("Usuarios del Grupo:");

        // Panel para lista y botones
        JPanel listPanel = new JPanel(new BorderLayout(5, 0));
        listPanel.setBackground(BG_COLOR);

        // Lista de usuarios
        listModelUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(listModelUsuarios);
        listaUsuarios.setFont(MESSAGE_FONT);
        listaUsuarios.setBackground(Color.WHITE);
        listaUsuarios.setForeground(TEXT_COLOR);
        listaUsuarios.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Personalizar el renderizado de la lista
        listaUsuarios.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(ACCENT_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                    setForeground(TEXT_COLOR);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });

        JScrollPane scrollPane = createStyledScrollPane(listaUsuarios);
        scrollPane.setPreferredSize(MAIN_LIST_SIZE);


        // Panel para los botones a la derecha de la lista
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        buttonsPanel.setBackground(BG_COLOR);
        buttonsPanel.setBorder(new EmptyBorder(0, 5, 0, 0));

        btnEliminarUsuario = createStyledButton("Eliminar Usuario", DELETE_COLOR);
        btnPromoverAdmin = createStyledButton("Promover a Admin", PRIMARY_COLOR);

        buttonsPanel.add(btnEliminarUsuario);
        buttonsPanel.add(btnPromoverAdmin);

        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(buttonsPanel, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0; gbc.weighty = 0;
        panel.add(lblUsuarios, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(listPanel, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 0;
    }

    private void addAgregarUsuarioSection(JPanel panel, GridBagConstraints gbc) {
        JLabel lblAgregarUsuario = createLabel("Contactos disponibles:");

        JPanel addUserPanel = new JPanel(new BorderLayout(5, 0));
        addUserPanel.setBackground(BG_COLOR);

        // Lista de usuarios disponibles
        listModelUsuariosDisponibles = new DefaultListModel<>();
        listaUsuariosDisponibles = new JList<>(listModelUsuariosDisponibles);
        listaUsuariosDisponibles.setFont(MESSAGE_FONT);
        listaUsuariosDisponibles.setBackground(Color.WHITE);
        listaUsuariosDisponibles.setForeground(TEXT_COLOR);
        listaUsuariosDisponibles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Personalizar el renderizado de la lista de usuarios disponibles
        listaUsuariosDisponibles.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(ACCENT_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                    setForeground(TEXT_COLOR);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });

        JScrollPane scrollPane = createStyledScrollPane(listaUsuariosDisponibles);
        scrollPane.setPreferredSize(ADD_LIST_SIZE);

        // Panel para el botón con GridBagLayout para centrarlo verticalmente
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(BG_COLOR);

        btnAgregarUsuario = createStyledButton("Añadir al grupo", PRIMARY_COLOR);

        // Centrar el botón verticalmente
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 0;
        buttonConstraints.weighty = 1.0;
        buttonConstraints.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(btnAgregarUsuario, buttonConstraints);

        // Añadir componentes al panel principal
        addUserPanel.add(scrollPane, BorderLayout.CENTER);
        addUserPanel.add(buttonPanel, BorderLayout.EAST);
        buttonPanel.setBorder(new EmptyBorder(0, 5, 0, 0));

        // Añadir al panel principal con GridBagLayout
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        panel.add(lblAgregarUsuario, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        panel.add(addUserPanel, gbc);

        gbc.gridwidth = 1;
    }
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_COLOR);

        // Create a panel for the buttons with FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(10, 5, 10, 0));


        btnGuardar = createStyledButton("Guardar", PRIMARY_COLOR);
        btnCancelar = createStyledButton("Cancelar", CANCEL_COLOR);

        // Make buttons the same size as other buttons
        btnGuardar.setPreferredSize(new Dimension(160, 50));
        btnCancelar.setPreferredSize(new Dimension(160, 50));

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);




        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        setupButtonActions();

        return bottomPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MESSAGE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JTextField createTextField(String initialText) {
        JTextField textField = new JTextField(initialText);
        textField.setFont(MESSAGE_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return textField;
    }

    private JScrollPane createStyledScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.setPreferredSize(new Dimension(200, 120));

        // Personalizar la barra de desplazamiento
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ACCENT_COLOR;
                this.trackColor = BG_COLOR;
            }
        });
        return scrollPane;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // Reduced horizontal padding
        // Increased width from 150 to 160 to accommodate longer text
        button.setPreferredSize(new Dimension(160, 50)); // Reduced height from 35 to 30

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                Color hoverColor = getHoverColor(text, backgroundColor);
                button.setBackground(hoverColor);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    private Color getHoverColor(String buttonText, Color baseColor) {
        return switch (buttonText) {
            case "Cancelar" -> new Color(148, 157, 165);
            case "Eliminar Usuario" -> new Color(200, 35, 51);
            default -> ACCENT_COLOR;
        };
    }

    private void setupButtonActions() {
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarConfiguracion());
        btnAgregarUsuario.addActionListener(e -> agregarUsuario());
        btnEliminarUsuario.addActionListener(e -> eliminarUsuarioSeleccionado());
        btnPromoverAdmin.addActionListener(e -> promoverUsuarioAdmin());
    }

    private void cargarUsuariosDelGrupo() {
        try {
            // Create DB connection
            Socket dbSocket = new Socket(serverIP, 44446);
            Socket dbSocket2 = new Socket(serverIP, 44446);
            DataOutputStream dbOut = new DataOutputStream(dbSocket.getOutputStream());
            DataOutputStream dbOut2 = new DataOutputStream(dbSocket2.getOutputStream());
            DataInputStream dbIn = new DataInputStream(dbSocket.getInputStream());
            DataInputStream dbIn2 = new DataInputStream(dbSocket2.getInputStream());

            // Get group users
            dbOut.writeUTF("OBTENER_USUARIOS_GRUPO;" + grupo);
            String usuariosGrupo = dbIn.readUTF();
            dbSocket.close();

            // Get available users

            dbOut2.writeUTF("OBTENER_USUARIOS_DISPONIBLES_GRUPO;" + grupo);
            String usuariosDisponibles = dbIn2.readUTF();
            dbSocket2.close();




            // Update UI
            SwingUtilities.invokeLater(() -> {
                // Load group users
                listModelUsuarios.clear();
                if (!usuariosGrupo.equals("ERROR")) {
                    String[] usuarios = usuariosGrupo.split(",");
                    for (String usuario : usuarios) {
                        if (!usuario.trim().isEmpty()) {
                            listModelUsuarios.addElement(usuario);
                        }
                    }
                }

                // Load available users
                listModelUsuariosDisponibles.clear();
                if (!usuariosDisponibles.equals("ERROR")) {
                    String[] usuarios = usuariosDisponibles.split(",");
                    for (String usuario : usuarios) {
                        if (!usuario.trim().isEmpty()) {
                            listModelUsuariosDisponibles.addElement(usuario);
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los usuarios",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void guardarConfiguracion() {
        try {
            Socket dbSocket = new Socket(serverIP, 44446);
            DataOutputStream dbOut = new DataOutputStream(dbSocket.getOutputStream());
            DataInputStream dbIn = new DataInputStream(dbSocket.getInputStream());

            // Collect all changes
            String nuevoNombre = txtNombreGrupo.getText();

            // Get current users in the list
            List<String> usuariosActuales = new ArrayList<>();
            for (int i = 0; i < listModelUsuarios.size(); i++) {
                usuariosActuales.add(listModelUsuarios.getElementAt(i));
            }

            // Get users available (these were removed from the group)
            List<String> usuariosEliminados = new ArrayList<>();
            for (int i = 0; i < listModelUsuariosDisponibles.size(); i++) {
                String usuario = listModelUsuariosDisponibles.getElementAt(i);
                usuariosEliminados.add(usuario);
            }

            // Send update command with both lists
            String comando = String.format("ACTUALIZAR_GRUPO;%s;%s;%s;%s",
                    grupo,
                    nuevoNombre,
                    String.join(",", usuariosActuales),
                    String.join(",", usuariosEliminados));

            dbOut.writeUTF(comando);
            String respuesta = dbIn.readUTF();

            if (respuesta.equals("OK")) {
                // Enviar señal de cierre de chat a todos los usuarios
                try {
                    // Usar el socket que ya tenemos en la clase
                    DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());
                    fsalida.writeUTF("/cerrar_chat_grupo " + grupo);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JOptionPane.showMessageDialog(this,
                        "Configuración guardada correctamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "El nombre de grupo "+ nuevoNombre + " ya existe" ,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            dbSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al guardar la configuración",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void agregarUsuario() {
        List<String> usuariosSeleccionados = listaUsuariosDisponibles.getSelectedValuesList();
        if (!usuariosSeleccionados.isEmpty()) {
            for (String usuario : usuariosSeleccionados) {
                listModelUsuarios.addElement(usuario);
                listModelUsuariosDisponibles.removeElement(usuario);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Selecciona uno o más usuarios para añadir.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarUsuarioSeleccionado() {
        List<String> usuariosSeleccionados = listaUsuarios.getSelectedValuesList();
        if (!usuariosSeleccionados.isEmpty()) {
            for (String usuario : usuariosSeleccionados) {
                listModelUsuarios.removeElement(usuario);
                listModelUsuariosDisponibles.addElement(usuario);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Selecciona uno o más usuarios para eliminar.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void promoverUsuarioAdmin() {
        String usuarioSeleccionado = listaUsuarios.getSelectedValue();
        if (usuarioSeleccionado != null) {
            try {
                Socket dbSocket = new Socket(serverIP, 44446);
                DataOutputStream dbOut = new DataOutputStream(dbSocket.getOutputStream());
                DataInputStream dbIn = new DataInputStream(dbSocket.getInputStream());

                dbOut.writeUTF("PROMOVER_ADMIN;" + grupo + ";" + usuarioSeleccionado);
                String respuesta = dbIn.readUTF();

                dbSocket.close();

                if (respuesta.equals("OK")) {
                    JOptionPane.showMessageDialog(this,
                            "Usuario promovido a administrador correctamente",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error al promover usuario",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error al promover usuario",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un usuario para promover a administrador",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ConfiguracionGrupo frame = new ConfiguracionGrupo(null, "UsuarioDePrueba", "GrupoDePrueba");
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}