package chattcp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MenuUsuarioUI extends JFrame {
    private String nombreUsuario;
    private String serverIP;

    public MenuUsuarioUI(String nombreUsuario, String serverIP) {
        this.nombreUsuario = nombreUsuario;
        this.serverIP = serverIP;
        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("Menú Principal - " + nombreUsuario);
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel userLabel = new JLabel("Bienvenido, " + nombreUsuario, SwingConstants.CENTER);
        userLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(userLabel, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(4, 1, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnAgregarContacto = new JButton("Agregar Contacto");
        JButton btnCrearGrupo = new JButton("Crear Chat Grupal");
        JButton btnChatear = new JButton("Chatear con Contacto");
        JButton btnChatearGrupo = new JButton("Chatear en Grupo");

        Font botonFont = new Font("Arial", Font.PLAIN, 16);
        btnAgregarContacto.setFont(botonFont);
        btnCrearGrupo.setFont(botonFont);
        btnChatear.setFont(botonFont);
        btnChatearGrupo.setFont(botonFont);

        panelBotones.add(btnAgregarContacto);
        panelBotones.add(btnChatear);
        panelBotones.add(btnCrearGrupo);
        panelBotones.add(btnChatearGrupo);

        add(panelBotones, BorderLayout.CENTER);

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

    private void mostrarListaUsuarios() {
        List<String> usuarios = obtenerUsuariosDisponibles();
        if (usuarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay usuarios disponibles para agregar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona un usuario para agregar a contactos:",
                "Agregar Contacto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                usuarios.toArray(),
                usuarios.get(0)
        );
        if (seleccionado != null) {
            agregarContacto(seleccionado);
        }
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

    private void abrirListaDeContactos() {
        List<String> contactos = obtenerContactosRemoto();
        if (contactos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No tienes contactos agregados.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear el modelo de la tabla con celdas no editables
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Contactos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String contacto : contactos) {
            tableModel.addRow(new Object[]{contacto});
        }

        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        String contactoSeleccionado = (String) tableModel.getValueAt(selectedRow, 0);
                        abrirChat(contactoSeleccionado);
                    }
                }
            }
        });

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JFrame frame = new JFrame("Lista de Contactos");
        // Change DISPOSE_ON_CLOSE to DO_NOTHING_ON_CLOSE and handle window closing manually
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose(); // Just close this window
            }
        });
        frame.setSize(400, 300);
        frame.add(panelPrincipal, BorderLayout.CENTER);
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    private void abrirCrearGrupo() {
        List<String> contactos = obtenerContactosRemoto();
        if (contactos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes contactos, por favor agrega un contacto antes de crear un grupo.", "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFrame frame = new JFrame("Crear Grupo");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JPanel panelNombreGrupo = new JPanel(new BorderLayout());
        JLabel nombreGrupoLabel = new JLabel("Nombre del Grupo:");
        JTextField nombreGrupoField = new JTextField();
        panelNombreGrupo.add(nombreGrupoLabel, BorderLayout.NORTH);
        panelNombreGrupo.add(nombreGrupoField, BorderLayout.CENTER);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String contacto : contactos) {
            listModel.addElement(contacto);
        }
        JList<String> contactoList = new JList<>(listModel);
        contactoList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(contactoList);

        JPanel panelCabecera = new JPanel(new BorderLayout());
        JLabel cabeceraLabel = new JLabel("Lista de Contactos");
        cabeceraLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panelCabecera.add(cabeceraLabel, BorderLayout.CENTER);
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelNombreGrupo, BorderLayout.NORTH);
        panelPrincipal.add(panelCabecera, BorderLayout.CENTER);
        panelPrincipal.add(scrollPane, BorderLayout.SOUTH);

        JButton agregarButton = new JButton("Agregar al Grupo");
        agregarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> contactosSeleccionados = contactoList.getSelectedValuesList();
                String nombreGrupo = nombreGrupoField.getText().trim();
                if (contactosSeleccionados.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Por favor, selecciona al menos un contacto.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (nombreGrupo.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Por favor, ingresa un nombre para el grupo.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                crearGrupoRemoto(nombreGrupo, contactosSeleccionados);
                frame.dispose();
            }
        });

        JPanel panelBoton = new JPanel(new BorderLayout());
        panelBoton.add(agregarButton, BorderLayout.CENTER);

        frame.add(panelPrincipal, BorderLayout.CENTER);
        frame.add(panelBoton, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(this);
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
                JOptionPane.showMessageDialog(this, "No se pudo crear el grupo: " + respuesta, "Error", JOptionPane.ERROR_MESSAGE);
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

    private void abrirListaGrupos() {
        List<String> grupos = obtenerGruposRemoto();
        if (grupos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No estás en ningún grupo.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Grupos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (String grupo : grupos) {
            tableModel.addRow(new Object[]{grupo});
        }
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        String grupoSeleccionado = (String) tableModel.getValueAt(selectedRow, 0);
                        abrirChatGrupo(grupoSeleccionado);
                    }
                }
            }
        });
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        JFrame frame = new JFrame("Lista de Grupos");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.add(panelPrincipal, BorderLayout.CENTER);
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    // Método para abrir el chat del grupo (a implementar según la lógica de tu aplicación)
    private void abrirChatGrupo(String grupo) {
        System.out.println("Abriendo chat del grupo: " + grupo);
        // Aquí va la lógica para abrir la interfaz de chat del grupo
    }

    // Método para abrir un chat con un contacto
    private void abrirChat(String contacto) {
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