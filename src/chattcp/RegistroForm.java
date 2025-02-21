package chattcp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RegistroForm extends JFrame {
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private String serverIP;

    public RegistroForm(String serverIP) {
        this.serverIP = serverIP;
        setTitle("Crear Cuenta");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        JLabel userLabel = new JLabel("Nombre de usuario:");
        userField = new JTextField();
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordField = new JPasswordField();
        registerButton = new JButton("Registrar");

        add(userLabel);
        add(userField);
        add(passwordLabel);
        add(passwordField);
        add(new JLabel("")); // Espacio
        add(registerButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passwordField.getPassword());
                try {
                    Socket socket = new Socket(serverIP, 44445);
                    DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                    DataInputStream entrada = new DataInputStream(socket.getInputStream());
                    salida.writeUTF("REGISTER;" + username + ";" + password);
                    String respuesta = entrada.readUTF();
                    socket.close();

                    if (respuesta.equals("OK")) {
                        JOptionPane.showMessageDialog(null, "Cuenta creada exitosamente!");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, respuesta);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al conectar con el servidor");
                }
            }
        });

        setLocationRelativeTo(null);
    }
}
