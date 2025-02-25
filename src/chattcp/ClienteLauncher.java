package chattcp;

import java.io.IOException;
import java.net.Socket;
import javax.swing.*;
import javax.swing.UIManager; // Importa UIManager

import chattcp.Interfaces.Cliente;
import chattcp.Interfaces.ServerIPDialog;
import com.formdev.flatlaf.FlatLightLaf; // Importa FlatLightLaf
import javax.swing.UnsupportedLookAndFeelException; // Importa esta excepción

public class ClienteLauncher {
    // Método para iniciar el chat
    public void iniciarCliente(String username, String serverIP, String destinatario) {
        int puerto = 44444;

        // **APLICA EL LOOK AND FEEL AQUÍ, ANTES DE CREAR Cliente**
        try {
            UIManager.setLookAndFeel( new FlatLightLaf() ); // Aplica FlatLaf
        } catch( UnsupportedLookAndFeelException ex ) {
            System.err.println( "Failed to initialize LaF" ); // Imprime error si falla
        }

        try {
            Socket s = new Socket(serverIP, puerto);
            Cliente cliente = new Cliente(s, username, destinatario);
            cliente.setBounds(0, 0, 540, 400);
            cliente.setVisible(true);
            new Thread(cliente).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al conectar con el servidor en " + serverIP);
        }
    }

    // Cliente inicia desde aquí
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel( new FlatLightLaf() ); // Aplica FlatLaf
            } catch( UnsupportedLookAndFeelException ex ) {
                System.err.println( "Failed to initialize LaF en main de ClienteLauncher" ); // Imprime error si falla
            }
            ServerIPDialog ipDialog = new ServerIPDialog(null);
            ipDialog.setVisible(true);
        });
    }
}