package chattcp;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerIPDialog extends JDialog {
    private JTextField ipField;
    private JButton connectButton;
    private String serverIP;

    public ServerIPDialog(JFrame parent) {
        super(parent, "Conectar al servidor", true);
        setupUI();
        setupEvents();
    }

    private void setupUI() {
        setLayout(new GridLayout(2, 2));
        ipField = new JTextField("localhost");
        connectButton = new JButton("Conectar");

        add(new JLabel("IP del servidor:"));
        add(ipField);
        add(new JLabel(""));
        add(connectButton);

        setSize(300, 100);
        setLocationRelativeTo(null);
    }

    private void setupEvents() {
        connectButton.addActionListener(e -> {
            serverIP = ipField.getText().trim();
            if (serverIP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa una dirección IP válida");
                return;
            }

            // Intentar conexión al puerto de registro
            try (Socket testSocket = new Socket(serverIP, 44445)) {
                // Enviar un mensaje de prueba al servidor
                DataOutputStream salida = new DataOutputStream(testSocket.getOutputStream());
                salida.writeUTF("TEST"); // Mensaje de prueba

                // Si la conexión y el mensaje se envían correctamente, abrir el LoginForm
                dispose();
                new LoginForm(serverIP).setVisible(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró servidor en " + serverIP,
                        "Error de conexión",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}