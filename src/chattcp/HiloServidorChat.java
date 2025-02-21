package chattcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class HiloServidorChat extends Thread {

    DataInputStream fentrada;
    Socket socket = null;
    ComunHilos comun;
    private String usuarioActual;

    public HiloServidorChat(Socket s, ComunHilos comun) {
        this.socket = s;
        this.comun = comun;

        try {
            fentrada = new DataInputStream(socket.getInputStream());
        } catch(IOException e) {
            System.out.println("ERROR DE E/S");
            e.printStackTrace();
        }
    }

    private void enviarMensajePrivado(String destinatario, String contenido) {
        // Save message to database
        if (MensajesDB.saveMessage(usuarioActual, destinatario, contenido)) {
            // If message was saved successfully, send to online user if they're connected
            Socket socketDestino = comun.getSocketUsuario(destinatario);
            if (socketDestino != null && !socketDestino.isClosed()) {
                try {
                    DataOutputStream fsalida = new DataOutputStream(socketDestino.getOutputStream());
                    // Send the actual message
                    fsalida.writeUTF("/privado " + usuarioActual + " " + contenido);
                    // Send update notification
                    fsalida.writeUTF("/actualizar " + usuarioActual);
                } catch (IOException e) {
                    System.out.println("Error enviando mensaje a " + destinatario);
                }
            }
        }
    }

    private void enviarHistorialChat(String otroUsuario) {
        try {
            List<ChatMessage> historial = MensajesDB.getChatHistory(usuarioActual, otroUsuario);
            DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());

            fsalida.writeUTF("HISTORY_START");
            for (ChatMessage msg : historial) {
                fsalida.writeUTF("HIST:" + msg.toString());
            }
            fsalida.writeUTF("HISTORY_END");
        } catch (IOException e) {
            System.out.println("Error enviando historial a " + usuarioActual);
        }
    }

    @Override
    public void run() {
        try {
            this.usuarioActual = fentrada.readUTF();
            if (!comun.agregarUsuario(usuarioActual, socket)) {
                socket.close();
                return;
            }

            while (true) {
                String mensaje = fentrada.readUTF();

                if (mensaje.startsWith("/privado ")) {
                    String[] partes = mensaje.split(" ", 3);
                    if (partes.length >= 3) {
                        String destinatario = partes[1];
                        String contenido = partes[2];

                        if (UsuariosDB.sonContactos(usuarioActual, destinatario)) {
                            enviarMensajePrivado(destinatario, contenido);
                        }
                    }
                } else if (mensaje.startsWith("/historial ")) {
                    String contacto = mensaje.split(" ", 2)[1];
                    if (UsuariosDB.sonContactos(usuarioActual, contacto)) {
                        enviarHistorialChat(contacto);
                    }
                }
            }
        } catch (IOException e) {
            if (usuarioActual != null) {
                comun.eliminarUsuario(usuarioActual);
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {}
        }
    }
}