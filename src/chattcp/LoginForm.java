package chattcp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class LoginForm extends JFrame {
    private JTextField ipField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel registerLabel;
    private String serverIP;

    public LoginForm(String serverIP) {
        this.serverIP = serverIP;
        setTitle("Autenticación de Usuario");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Se usan 5 filas y 2 columnas para incluir el campo de IP
        setLayout(new GridLayout(5, 2));


        JLabel userLabel = new JLabel("Nombre de usuario:");
        userField = new JTextField();
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordField = new JPasswordField();
        loginButton = new JButton("Iniciar sesión");

        // Label con estilo de enlace para crear cuenta
        registerLabel = new JLabel("<html><a href=''>Crear cuenta</a></html>");
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));


        add(userLabel);
        add(userField);
        add(passwordLabel);
        add(passwordField);
        add(new JLabel("")); // Espacio vacío
        add(loginButton);
        add(new JLabel("")); // Espacio vacío
        add(registerLabel);

        // Acción de login: se conecta al servidor de registro para validar credenciales.
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                String username = userField.getText();
                String password = new String(passwordField.getPassword());
                try {
                    // Conexión al servidor de registro en el puerto 44445
                    Socket socket = new Socket(serverIP, 44445);
                    DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                    DataInputStream entrada = new DataInputStream(socket.getInputStream());
                    salida.writeUTF("LOGIN;" + username + ";" + password);
                    String respuesta = entrada.readUTF();
                    socket.close();


                    if (respuesta.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "Acceso exitoso!");
                        dispose();
                        // Abrir MenuUsuarioUI en lugar del cliente de chat directamente
                        new MenuUsuarioUI(username, serverIP).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, respuesta);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al conectar con el servidor");
                }
            }
        });

        // Al hacer clic en "Crear cuenta" se abre la ventana de registro
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegistroForm(serverIP).setVisible(true);
            }
        });

        setLocationRelativeTo(null);
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
