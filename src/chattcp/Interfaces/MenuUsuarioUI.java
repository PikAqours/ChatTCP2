    package chattcp.Interfaces;

    import chattcp.ServerConfig.Notificacion;
    import chattcp.ServerConfig.UsuariosDB;

    import javax.swing.*;
    import javax.swing.border.EmptyBorder;
    import javax.swing.plaf.basic.BasicScrollBarUI;
    import javax.swing.table.DefaultTableCellRenderer;
    import javax.swing.table.DefaultTableModel;
    import java.awt.*;
    import java.awt.event.*;
    import java.io.DataInputStream;
    import java.io.DataOutputStream;
    import java.io.IOException;
    import java.net.Socket;
    import java.util.ArrayList;
    import java.util.List;

    /**
     * Menú principal de la aplicación de chat
     */
    public class MenuUsuarioUI extends JFrame {
        // Componentes principales
        private String nombreUsuario;
        private String serverIP;

        // Colores y estilos (consistentes con otras interfaces)
        private final Color PRIMARY_COLOR = new Color(75, 0, 130);      // Índigo
        private final Color ACCENT_COLOR = new Color(147, 112, 219);    // Púrpura medio
        private final Color BG_COLOR = new Color(245, 245, 250);        // Fondo claro
        private final Color TEXT_COLOR = new Color(50, 50, 50);         // Texto oscuro
        private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
        private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
        // En la clase MenuUsuarioUI
        private List<String> chatsIndividualesAbiertos = new ArrayList<>();
        private List<String> chatsGrupalesAbiertos = new ArrayList<>();

        private Socket notificationSocket;
        private Thread notificationThread;
        private Notificacion notificacion;

        public MenuUsuarioUI(String nombreUsuario, String serverIP) {
            this.nombreUsuario = nombreUsuario;
            this.serverIP = serverIP;

            try {
                this.notificacion = Notificacion.getInstance();
            } catch (Exception e) {
                System.out.println("No se pudo inicializar el sistema de notificaciones");
            }

            // Inicializar el socket de notificaciones
            initNotificationSystem();

            inicializarUI();



        }

        private void initNotificationSystem() {
            try {
                notificationSocket = new Socket(serverIP, 44444);
                DataOutputStream salida = new DataOutputStream(notificationSocket.getOutputStream());
                salida.writeUTF("NOTIFY;" + nombreUsuario);  // Identificador especial para el servidor

                // Iniciar thread de escucha de notificaciones
                notificationThread = new Thread(() -> {
                    try {
                        DataInputStream entrada = new DataInputStream(notificationSocket.getInputStream());
                        while (true) {
                            String mensaje = entrada.readUTF();
                            if (mensaje.startsWith("/privado ")) {
                                String[] parts = mensaje.split(" ", 3);
                                if (parts.length >= 3) {
                                    String sender = parts[1];
                                    String content = parts[2];

                                    // Solo mostrar notificación si no hay un chat abierto con este usuario
                                    if (!chatsIndividualesAbiertos.contains(sender)) {
                                        notificacion.mostrarNotificacionPrivada(sender, content);
                                    }
                                }
                            } else if (mensaje.startsWith("/grupo ")) {
                                String[] parts = mensaje.split(" ", 3);
                                if (parts.length >= 3) {
                                    String sender = parts[1];
                                    String grupo = parts[2].split(":")[0];
                                    String content = parts[2].split(":", 2)[1];

                                    // Solo mostrar notificación si no hay un chat de grupo abierto
                                    if (!chatsGrupalesAbiertos.contains(grupo)) {
                                        notificacion.mostrarNotificacionGrupo(sender, grupo, content);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        if (!notificationSocket.isClosed()) {
                            System.out.println("Error en el sistema de notificaciones: " + e.getMessage());
                        }
                    }
                });
                notificationThread.start();

            } catch (IOException e) {
                System.out.println("No se pudo iniciar el sistema de notificaciones: " + e.getMessage());
            }
        }


        private void inicializarUI() {
            //Cerramos sesión en caso de que se interrumpa el programa
            agregarShutdownHook();
            // Configuración básica de la ventana
            setTitle("Menú Principal - " + nombreUsuario);
            setSize(450, 450);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    mostrarConfirmacionSalida();
                }
            });
            setLocationRelativeTo(null);
            setResizable(false);

            // Configura el color de la barra de título
            getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
            getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

            // Panel principal
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(BG_COLOR);
            mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

            // Panel superior con título de bienvenida
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BG_COLOR);
            JLabel welcomeLabel = new JLabel("Bienvenido, " + nombreUsuario, SwingConstants.CENTER);
            welcomeLabel.setFont(TITLE_FONT);
            welcomeLabel.setForeground(PRIMARY_COLOR);
            headerPanel.add(welcomeLabel);
            mainPanel.add(headerPanel, BorderLayout.NORTH);

            // Panel central que contendrá las secciones
            JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
            centerPanel.setBackground(BG_COLOR);

            // Sección de Contactos
            JPanel contactsSection = new JPanel(new GridLayout(2, 1, 5, 5));
            contactsSection.setBackground(BG_COLOR);
            contactsSection.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR),
                            "Contactos",
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            new Font("Segoe UI", Font.BOLD, 12),
                            PRIMARY_COLOR
                    ),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            JButton btnAgregarContacto = createStyledButton("Agregar Contacto");
            JButton btnChatear = createStyledButton("Chatear con Contacto");
            contactsSection.add(btnAgregarContacto);
            contactsSection.add(btnChatear);

            // Sección de Grupos
            JPanel groupsSection = new JPanel(new GridLayout(2, 1, 5, 5));
            groupsSection.setBackground(BG_COLOR);
            groupsSection.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR),
                            "Grupos",
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            new Font("Segoe UI", Font.BOLD, 12),
                            PRIMARY_COLOR
                    ),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            JButton btnCrearGrupo = createStyledButton("Crear Chat Grupal");
            JButton btnChatearGrupo = createStyledButton("Chatear en Grupo");
            groupsSection.add(btnCrearGrupo);
            groupsSection.add(btnChatearGrupo);

            // Panel que contiene ambas secciones
            JPanel sectionsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
            sectionsPanel.setBackground(BG_COLOR);
            sectionsPanel.add(contactsSection);
            sectionsPanel.add(groupsSection);

            centerPanel.add(sectionsPanel, BorderLayout.CENTER);

            // Botón de salir en panel separado
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.setBackground(BG_COLOR);
            JButton btnSalir = createStyledButton("Salir");
            btnSalir.setBackground(new Color(220, 53, 69)); // Color rojo para el botón Salir
            btnSalir.setPreferredSize(new Dimension(200, 40));
            bottomPanel.add(btnSalir);

            centerPanel.add(bottomPanel, BorderLayout.SOUTH);
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            // Agregar el panel principal al frame
            add(mainPanel);

            // Borde decorativo
            getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));

            // Configurar eventos (manteniendo el orden original de los parámetros)
            setupEvents(btnAgregarContacto, btnChatear, btnCrearGrupo, btnChatearGrupo, btnSalir);
            InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = mainPanel.getActionMap();

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "salir");
            actionMap.put("salir", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    btnSalir.doClick();
                }
            });
        }
        private void agregarShutdownHook() {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logout();
            }));
        }

        private void mostrarConfirmacionSalida() {
            // Personalizar botones de opción
            UIManager.put("OptionPane.background", BG_COLOR);
            UIManager.put("Panel.background", BG_COLOR);
            UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", BUTTON_FONT);
            UIManager.put("OptionPane.yesButtonText", "Sí");
            UIManager.put("OptionPane.noButtonText", "No");

            // Crear un JOptionPane personalizado
            JOptionPane optionPane = new JOptionPane(
                    "¿Estás seguro que deseas cerrar sesión?",
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.YES_NO_OPTION
            );


            // Crear un JDialog a partir del JOptionPane
            JDialog dialog = optionPane.createDialog(this, "Confirmar Salida");


            // Cambiar el color de la barra de título
            dialog.getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
            dialog.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
            dialog.setVisible(true);
            //Valor por defecto NO cerrar la ventana
            int confirmar= JOptionPane.NO_OPTION;
            // Obtener la opción seleccionada
            try {
                confirmar = (int) optionPane.getValue();
            }
            //Capturamos la posibilidad de que el usurio intente cerrar la ventana sin clicar una opción, da null en ese caso
            catch (NullPointerException e) {

            }

            if (confirmar == JOptionPane.YES_OPTION) {
                logout();
                dispose();
                new LoginForm(serverIP).setVisible(true);
            }
        }

        /**
         * Crea un botón estilizado
         */
        private JButton createStyledButton(String text) {
            JButton button = new JButton(text);
            button.setFont(BUTTON_FONT);
            button.setForeground(Color.WHITE);
            button.setBackground(PRIMARY_COLOR);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            // Efectos al pasar el mouse
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    if (text.equals("Salir")) {
                        button.setBackground(new Color(200, 35, 51)); // Rojo más oscuro para hover
                    } else {
                        button.setBackground(ACCENT_COLOR);
                    }
                    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                public void mouseExited(MouseEvent evt) {
                    if (text.equals("Salir")) {
                        button.setBackground(new Color(220, 53, 69)); // Rojo original
                    } else {
                        button.setBackground(PRIMARY_COLOR);
                    }
                    button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });

            return button;
        }
        /**
         * Configura los eventos de los botones
         */
        private void setupEvents(JButton btnAgregarContacto, JButton btnChatear,
                                 JButton btnCrearGrupo, JButton btnChatearGrupo, JButton btnSalir) {
            // Evento del botón Salir
            btnSalir.addActionListener(e -> {
                mostrarConfirmacionSalida();
            });


            btnAgregarContacto.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mostrarListaUsuarios();
                }
            });
            btnChatear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    abrirListaDeContactos();
                }
            });
            btnCrearGrupo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    abrirCrearGrupo();
                }
            });
            btnChatearGrupo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    abrirListaGrupos();
                }
            });

            setVisible(true);
        }
        private void logout() {
            try (Socket socket = new Socket(serverIP, 44445);
                 DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                 DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

                salida.writeUTF("LOGOUT;" + nombreUsuario);
                String response = entrada.readUTF();
                System.out.println("Logout response: " + response);

                if (notificationSocket != null && !notificationSocket.isClosed()) {
                    notificationSocket.close();
                }
                if (notificationThread != null) {
                    notificationThread.interrupt();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void mostrarListaUsuarios() {
            List<String> usuarios = obtenerUsuariosDisponibles();
            if (usuarios.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay usuarios disponibles para agregar.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Crear ventana de diálogo estilizada
            JFrame frame = new JFrame("Agregar Contacto");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(this);
            frame.setResizable(false);

            // Configurar el color de la barra de título
            frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
            frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

            // Panel principal
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(BG_COLOR);
            mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

            // Panel superior con título
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BG_COLOR);
            JLabel titleLabel = new JLabel("Selecciona un usuario para agregar", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(PRIMARY_COLOR);
            headerPanel.add(titleLabel, BorderLayout.CENTER);
            headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

            // Lista de usuarios
            DefaultListModel<String> listModel = new DefaultListModel<>();
            usuarios.forEach(listModel::addElement);
            JList<String> userList = new JList<>(listModel);
            userList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Personalizar el renderizado de las celdas
            userList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (isSelected) {
                        c.setBackground(ACCENT_COLOR);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                        c.setForeground(TEXT_COLOR);
                    }
                    return c;
                }
            });

            // Panel de desplazamiento con borde personalizado
            JScrollPane scrollPane = new JScrollPane(userList);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            scrollPane.setBackground(Color.WHITE);

            // Panel contenedor para la lista con padding
            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setBackground(BG_COLOR);
            listPanel.add(scrollPane, BorderLayout.CENTER);
            listPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

            // Panel de botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            buttonPanel.setBackground(BG_COLOR);

            JButton addButton = createStyledButton("Agregar");
            addButton.setPreferredSize(new Dimension(120, 35));

            JButton cancelButton = createStyledButton("Cancelar");
            cancelButton.setBackground(new Color(220, 53, 69));
            cancelButton.setPreferredSize(new Dimension(120, 35));

            buttonPanel.add(addButton);
            buttonPanel.add(cancelButton);

            // Agregar componentes al panel principal
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(listPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Agregar panel principal a la ventana
            frame.add(mainPanel);

            // Borde decorativo de la ventana
            frame.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));

            // Configurar eventos
            addButton.addActionListener(e -> {
                String seleccionado = userList.getSelectedValue();
                if (seleccionado != null) {
                    frame.dispose();
                    agregarContacto(seleccionado);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Por favor, selecciona un usuario de la lista",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                }
            });

            cancelButton.addActionListener(e -> frame.dispose());

            // Doble clic para seleccionar
            userList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String seleccionado = userList.getSelectedValue();
                        if (seleccionado != null) {
                            frame.dispose();
                            agregarContacto(seleccionado);
                        }
                    }
                }
            });


            JRootPane rootPane = frame.getRootPane();

            InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);


            ActionMap actionMap = rootPane.getActionMap();

            // Atajo para la tecla ENTER (Agregar Usuario)
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "agregarUsuario"); // Asocia ENTER a la acción "agregarUsuario"
            actionMap.put("agregarUsuario", new AbstractAction() { // Define la acción "agregarUsuario"
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Simula el clic en el botón "Agregar"
                    addButton.doClick();
                }
            });

            // Atajo para la tecla ESCAPE (Cancelar)
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelarDialogo"); // Asocia ESCAPE a la acción "cancelarDialogo"
            actionMap.put("cancelarDialogo", new AbstractAction() { // Define la acción "cancelarDialogo"
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Simula el clic en el botón "Cancelar"
                    cancelButton.doClick();
                }
            });
            // Mostrar la ventana
            frame.setVisible(true);
        }

        // Este método ya accede de forma remota
        private List<String> obtenerUsuariosDisponibles() {
            List<String> usuarios = new ArrayList<>();
            try (Socket socket = new Socket(serverIP, 44446);
                 DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                 DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

                // Enviar comando: "OBTENER_USUARIOS_DISPONIBLES;usuarioActual"
                String comando = "OBTENER_USUARIOS_DISPONIBLES;" + nombreUsuario;
                salida.writeUTF(comando);

                // Recibir respuesta (lista separada por comas)
                String resp = entrada.readUTF();
                if (!resp.isEmpty() && !resp.startsWith("ERROR")) {
                    String[] arr = resp.split(",");
                    for (String s : arr) {
                        if (!s.trim().isEmpty()) {
                            usuarios.add(s.trim());
                        }
                    }
                } else {
                    System.out.println("Respuesta del servidor: " + resp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return usuarios;
        }

        // Se actualiza para acceder de forma remota
        private void agregarContacto(String usuario) {
            try (Socket socket = new Socket(serverIP, 44446);
                 DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                 DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

                String comando = "AGREGAR_CONTACTO;" + nombreUsuario + ";" + usuario;
                salida.writeUTF(comando);
                String respuesta = entrada.readUTF();
                if (respuesta.equals("OK")) {
                    JOptionPane.showMessageDialog(this, usuario + " ha sido agregado a tus contactos.", "Contacto Agregado", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, usuario + " ya está en tu lista de contactos.", "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Método para obtener contactos de forma remota
        private List<String> obtenerContactosRemoto() {
            List<String> contactos = new ArrayList<>();
            try (Socket socket = new Socket(serverIP, 44446);
                 DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                 DataInputStream entrada = new DataInputStream(socket.getInputStream())) {
                String comando = "OBTENER_CONTACTOS;" + nombreUsuario;
                salida.writeUTF(comando);
                String respuesta = entrada.readUTF();
                if (!respuesta.isEmpty() && !respuesta.startsWith("ERROR")) {
                    String[] arr = respuesta.split(",");
                    for (String s : arr) {
                        if (!s.trim().isEmpty()) {
                            contactos.add(s.trim());
                        }
                    }
                } else {
                    System.out.println("Respuesta del servidor: " + respuesta);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return contactos;
        }

        /**
         * Abre una ventana con la lista de contactos del usuario
         */
        private void abrirListaDeContactos() {
            List<String> contactos = obtenerContactosRemoto();
            if (contactos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No tienes contactos agregados.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFrame frame = new JFrame("Lista de Contactos");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(this);
            frame.setResizable(false);

            frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
            frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(BG_COLOR);
            mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BG_COLOR);
            JLabel titleLabel = new JLabel("Tus Contactos", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(PRIMARY_COLOR);
            headerPanel.add(titleLabel, BorderLayout.CENTER);
            headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Contactos"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (String contacto : contactos) {
                boolean isOnline = verificarEstadoContacto(contacto);
                ImageIcon iconoEstado = getIconoEstado(isOnline);
                tableModel.addRow(new Object[]{new JLabel(contacto, iconoEstado, JLabel.LEFT)});
            }

            JTable table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowHeight(30);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setGridColor(new Color(230, 230, 230));
            table.setShowVerticalLines(false);
            table.setShowHorizontalLines(true);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setBackground(PRIMARY_COLOR);
            table.getTableHeader().setForeground(Color.WHITE);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) value;
                    if (isSelected) {
                        label.setBackground(ACCENT_COLOR);
                        label.setForeground(Color.WHITE);
                    } else {
                        label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                        label.setForeground(TEXT_COLOR);
                    }
                    label.setOpaque(true);
                    return label;
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            scrollPane.setBackground(Color.WHITE);

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBackground(BG_COLOR);
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            tablePanel.setBorder(new EmptyBorder(5, 0, 5, 0));

            JLabel infoLabel = new JLabel("Doble clic para abrir chat", SwingConstants.CENTER);
            infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            infoLabel.setForeground(TEXT_COLOR);
            infoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow != -1) {
                            JLabel label = (JLabel) tableModel.getValueAt(selectedRow, 0);
                            String contactoSeleccionado = label.getText();
                            frame.dispose();
                            abrirChat(contactoSeleccionado);
                        }
                    }
                }
            });

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(tablePanel, BorderLayout.CENTER);
            mainPanel.add(infoLabel, BorderLayout.SOUTH);

            frame.add(mainPanel);

            frame.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));

            frame.setVisible(true);
        }

        private boolean verificarEstadoContacto(String contacto) {

            try (Socket socket = new Socket(serverIP, 44445);
                 DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                 DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

                // Enviar comando para verificar estado
                salida.writeUTF("VERIFICAR_ESTADO;" + contacto);
                String respuesta = entrada.readUTF();
                return "ONLINE".equals(respuesta);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        /**
         * Abre una ventana para crear un nuevo grupo de chat
         */
        private void abrirCrearGrupo() {
            List<String> contactos = obtenerContactosRemoto();
            if (contactos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No tienes contactos, por favor agrega un contacto antes de crear un grupo.",
                        "Error",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Crear ventana
            JFrame frame = new JFrame("Crear Grupo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 450);
            frame.setLocationRelativeTo(this);
            frame.setResizable(false);

            // Configurar el color de la barra de título
            frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
            frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

            // Panel principal
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(BG_COLOR);
            mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

            // Panel superior con título
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BG_COLOR);
            JLabel titleLabel = new JLabel("Crear Nuevo Grupo", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(PRIMARY_COLOR);
            headerPanel.add(titleLabel, BorderLayout.CENTER);
            headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

            // Panel para el nombre del grupo
            JPanel groupNamePanel = new JPanel(new BorderLayout(5, 5));
            groupNamePanel.setBackground(BG_COLOR);
            groupNamePanel.setBorder(new EmptyBorder(10, 0, 10, 0));

            JLabel groupNameLabel = new JLabel("Nombre del Grupo:");
            groupNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            groupNameLabel.setForeground(TEXT_COLOR);

            JTextField groupNameField = new JTextField();
            groupNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            groupNameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            groupNamePanel.add(groupNameLabel, BorderLayout.NORTH);
            groupNamePanel.add(groupNameField, BorderLayout.CENTER);

            // Lista de contactos
            DefaultListModel<String> listModel = new DefaultListModel<>();
            contactos.forEach(listModel::addElement);

            JList<String> contactList = new JList<>(listModel);
            contactList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            contactList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            contactList.setBackground(Color.WHITE);

            // Personalizar el renderizado de la lista
            contactList.setCellRenderer(new DefaultListCellRenderer() {
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

            // Panel de desplazamiento con borde personalizado
            JScrollPane scrollPane = new JScrollPane(contactList);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            // Panel de la lista de contactos con título
            JPanel contactsPanel = new JPanel(new BorderLayout(5, 5));
            contactsPanel.setBackground(BG_COLOR);

            JLabel contactsLabel = new JLabel("Selecciona los miembros del grupo:", SwingConstants.LEFT);
            contactsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            contactsLabel.setForeground(TEXT_COLOR);
            contactsLabel.setBorder(new EmptyBorder(10, 0, 5, 0));

            contactsPanel.add(contactsLabel, BorderLayout.NORTH);
            contactsPanel.add(scrollPane, BorderLayout.CENTER);

            // Panel de botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            buttonPanel.setBackground(BG_COLOR);
            buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

            JButton createButton = createStyledButton("Crear Grupo");
            JButton cancelButton = createStyledButton("Cancelar");
            cancelButton.setBackground(new Color(220, 53, 69));

            createButton.setPreferredSize(new Dimension(120, 35));
            cancelButton.setPreferredSize(new Dimension(120, 35));

            buttonPanel.add(createButton);
            buttonPanel.add(cancelButton);

            // Eventos de los botones
            createButton.addActionListener(e -> {
                String nombreGrupo = groupNameField.getText().trim();
                List<String> contactosSeleccionados = contactList.getSelectedValuesList();

                if (nombreGrupo.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Por favor, ingresa un nombre para el grupo.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (contactosSeleccionados.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Por favor, selecciona al menos un contacto.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                crearGrupoRemoto(nombreGrupo, contactosSeleccionados);
                frame.dispose();
            });

            cancelButton.addActionListener(e -> frame.dispose());

            // Organizar paneles
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBackground(BG_COLOR);
            contentPanel.add(groupNamePanel, BorderLayout.NORTH);
            contentPanel.add(contactsPanel, BorderLayout.CENTER);

            // Agregar componentes al panel principal
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(contentPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Agregar panel principal a la ventana
            frame.add(mainPanel);

            // Borde decorativo de la ventana
            frame.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));

            // Obtener el RootPane del frame
            JRootPane rootPane = frame.getRootPane();

            // InputMap y ActionMap para el RootPane
            InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = rootPane.getActionMap();

            // Atajo para ENTER - Crear Grupo
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "crearGrupoAction");
            actionMap.put("crearGrupoAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createButton.doClick();
                }
            });

            // Atajo para ESCAPE - Cancelar
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelarCrearGrupoAction");
            actionMap.put("cancelarCrearGrupoAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelButton.doClick();
                }
            });
            frame.setVisible(true);
        }

        // Se actualiza para enviar el comando remoto de crear grupo
        private void crearGrupoRemoto(String nombreGrupo, List<String> contactosSeleccionados) {
            try (Socket socket = new Socket(serverIP, 44446);
                 DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                 DataInputStream entrada = new DataInputStream(socket.getInputStream())) {
                String contactosStr = String.join(",", contactosSeleccionados);
                String comando = "CREAR_GRUPO;" + nombreUsuario + ";" + nombreGrupo + ";" + contactosStr;
                salida.writeUTF(comando);
                String respuesta = entrada.readUTF();
                if (respuesta.equals("OK")) {
                    JOptionPane.showMessageDialog(this, "Grupo '" + nombreGrupo + "' creado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Ya existe un grupo con ese nombre. " + respuesta, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Método para obtener los grupos del usuario de forma remota.
        // Se asume que el servidorDB ha sido extendido para soportar el comando "OBTENER_GRUPOS"
        private List<String> obtenerGruposRemoto() {
            List<String> grupos = new ArrayList<>();
            try (Socket socket = new Socket(serverIP, 44446);
                 DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                 DataInputStream entrada = new DataInputStream(socket.getInputStream())) {
                String comando = "OBTENER_GRUPOS;" + nombreUsuario;
                salida.writeUTF(comando);
                String respuesta = entrada.readUTF();
                if (!respuesta.isEmpty() && !respuesta.startsWith("ERROR")) {
                    String[] arr = respuesta.split(",");
                    for (String s : arr) {
                        if (!s.trim().isEmpty()) {
                            grupos.add(s.trim());
                        }
                    }
                } else {
                    System.out.println("Respuesta del servidor: " + respuesta);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return grupos;
        }

        /**
         * Abre una ventana con la lista de grupos del usuario
         */
        private void abrirListaGrupos() {
            List<String> grupos = obtenerGruposRemoto();
            if (grupos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No estás en ningún grupo.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Crear ventana
            JFrame frame = new JFrame("Lista de Grupos");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(this);
            frame.setResizable(false);

            // Configurar el color de la barra de título
            frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", PRIMARY_COLOR);
            frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

            // Panel principal
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(BG_COLOR);
            mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

            // Panel superior con título
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BG_COLOR);
            JLabel titleLabel = new JLabel("Tus Grupos", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(PRIMARY_COLOR);
            headerPanel.add(titleLabel, BorderLayout.CENTER);
            headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

            // Crear el modelo de la tabla
            DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Grupos"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (String grupo : grupos) {
                tableModel.addRow(new Object[]{grupo});
            }

            // Configurar la tabla
            JTable table = new JTable(tableModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowHeight(30);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setGridColor(new Color(230, 230, 230));
            table.setShowVerticalLines(false);
            table.setShowHorizontalLines(true);
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setBackground(PRIMARY_COLOR);
            table.getTableHeader().setForeground(Color.WHITE);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

            // Personalizar el renderizado de las celdas
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);

                    if (isSelected) {
                        c.setBackground(ACCENT_COLOR);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                        c.setForeground(TEXT_COLOR);
                    }
                    return c;
                }
            });

            // Panel de desplazamiento con borde personalizado
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = ACCENT_COLOR;
                    this.trackColor = BG_COLOR;
                }
            });

            // Panel contenedor para la tabla
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBackground(BG_COLOR);
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            tablePanel.setBorder(new EmptyBorder(5, 0, 5, 0));

            // Texto informativo
            JLabel infoLabel = new JLabel("Doble clic para abrir el chat del grupo", SwingConstants.CENTER);
            infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            infoLabel.setForeground(TEXT_COLOR);
            infoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

            // Evento de doble clic
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow != -1) {
                            String grupoSeleccionado = (String) tableModel.getValueAt(selectedRow, 0);
                            frame.dispose();
                            abrirChatGrupo(grupoSeleccionado);
                        }
                    }
                }
            });

            // Agregar componentes al panel principal
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(tablePanel, BorderLayout.CENTER);
            mainPanel.add(infoLabel, BorderLayout.SOUTH);

            // Agregar panel principal a la ventana
            frame.add(mainPanel);

            // Borde decorativo de la ventana
            frame.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, PRIMARY_COLOR));

            frame.setVisible(true);
        }


        private void abrirChatGrupo(String grupo) {
            if (chatsGrupalesAbiertos.contains(grupo)) {
                JOptionPane.showMessageDialog(this,
                        "Ya tienes abierto el chat del grupo " + grupo,
                        "Chat ya abierto",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // Verify user belongs to the group
                if (!verificarPertenenciaGrupoRemoto(nombreUsuario, grupo)) {
                    JOptionPane.showMessageDialog(this,
                            "No puedes acceder a este grupo porque no eres miembro.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Socket socket = new Socket(serverIP, 44444);
                ClienteGrupo cliente = new ClienteGrupo(socket, nombreUsuario, grupo);
                cliente.setBounds(0, 0, 540, 400);
                cliente.setVisible(true);
                chatsGrupalesAbiertos.add(grupo);

                cliente.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        chatsGrupalesAbiertos.remove(grupo);
                    }
                });

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo conectar al servidor de chat",
                        "Error",
                         JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        // Método para verificar la pertenencia al grupo a través del servidor
        private boolean verificarPertenenciaGrupoRemoto(String usuario, String grupo) {
            try (Socket socket = new Socket(serverIP, 44446)) {
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                DataInputStream entrada = new DataInputStream(socket.getInputStream());

                // Enviar comando de verificación
                String comando = "VERIFICAR_PERTENENCIA_GRUPO;" + usuario + ";" + grupo;
                salida.writeUTF(comando);

                // Recibir respuesta
                String respuesta = entrada.readUTF();
                return "OK".equals(respuesta);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al verificar la pertenencia al grupo",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }
        // Método para abrir un chat con un contacto
        private void abrirChat(String contacto) {
            if (chatsIndividualesAbiertos.contains(contacto)) {
                JOptionPane.showMessageDialog(this,
                        "Ya tienes un chat abierto con " + contacto,
                        "Chat ya abierto",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                // First verify they are contacts through the server
                boolean sonContactos = verificarContactoRemoto(contacto);

                if (!sonContactos) {
                    JOptionPane.showMessageDialog(this,
                            "No puedes chatear con este usuario porque no es tu contacto.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Socket socket = new Socket(serverIP, 44444);
                Cliente cliente = new Cliente(socket, nombreUsuario, contacto);
                cliente.setBounds(0, 0, 540, 400);
                cliente.setVisible(true);
                chatsIndividualesAbiertos.add(contacto);

                cliente.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        chatsIndividualesAbiertos.remove(contacto);
                    }
                });
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo conectar al servidor de chat",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        private boolean verificarContactoRemoto(String contacto) {
            try (Socket socket = new Socket(serverIP, 44446)) {
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                DataInputStream entrada = new DataInputStream(socket.getInputStream());

                // Send verification command to server
                String comando = "VERIFICAR_CONTACTO;" + nombreUsuario + ";" + contacto;
                salida.writeUTF(comando);

                // Get response from server
                String respuesta = entrada.readUTF();
                return respuesta.equals("OK");

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error al verificar el estado de contacto",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }
        //Icono Online
        private ImageIcon getIconoEstado(boolean isOnline) {
            String iconPath = isOnline ? "chatTCP/res/online_icon.png" : "chatTCP/res/offline_icon.png";
            ImageIcon icon = new ImageIcon(iconPath);
            Image img = icon.getImage();
            Image newImg = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            return new ImageIcon(newImg);
        }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new MenuUsuarioUI("usuario1", "localhost");
            });
        }
    }