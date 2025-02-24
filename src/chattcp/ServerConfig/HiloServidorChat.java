package chattcp.ServerConfig;

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
    private boolean isNotificationSocket = false;

    public HiloServidorChat(Socket s, ComunHilos comun) {
        this.socket = s;
        this.comun = comun;

        try {
            fentrada = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("ERROR DE E/S");
            e.printStackTrace();
        }
    }

    private void enviarMensajePrivado(String destinatario, String contenido) {
        // Save message to database
        if (MensajesDB.saveMessage(usuarioActual, destinatario, contenido)) {
            // Si el mensaje se guardó correctamente, enviar al usuario y su socket de notificaciones
            Socket socketDestino = comun.getSocketUsuario(destinatario);
            Socket socketNotificacion = comun.getSocketNotificacion(destinatario);

            // Enviar al socket de chat si está disponible
            if (socketDestino != null && !socketDestino.isClosed()) {
                try {
                    DataOutputStream fsalida = new DataOutputStream(socketDestino.getOutputStream());
                    fsalida.writeUTF("/privado " + usuarioActual + " " + contenido);
                    fsalida.writeUTF("/actualizar " + usuarioActual);
                } catch (IOException e) {
                    System.out.println("Error enviando mensaje a " + destinatario);
                }
            }

            // Enviar al socket de notificaciones si está disponible
            if (socketNotificacion != null && !socketNotificacion.isClosed()) {
                try {
                    DataOutputStream fsalidaNotif = new DataOutputStream(socketNotificacion.getOutputStream());
                    fsalidaNotif.writeUTF("/privado " + usuarioActual + " " + contenido);
                } catch (IOException e) {
                    System.out.println("Error enviando notificación a " + destinatario);
                }
            }
        }
    }

    private void notificarCambioNombreGrupo(String grupoAntiguo, String grupoNuevo) {
        List<String> miembros = UsuariosDB.obtenerMiembrosGrupo(grupoNuevo);
        for (String miembro : miembros) {
            Socket socketDestino = comun.getSocketUsuario(miembro);
            if (socketDestino != null && !socketDestino.isClosed()) {
                try {
                    DataOutputStream fsalida = new DataOutputStream(socketDestino.getOutputStream());
                    fsalida.writeUTF("/cambio_nombre_grupo " + grupoAntiguo + " " + grupoNuevo);
                } catch (IOException e) {
                    System.out.println("Error notificando cambio de nombre a " + miembro);
                }
            }
        }
    }

    private void enviarHistorialChat(String otroUsuario) {
        try {
            List<MensajesChat> historial = MensajesDB.getChatHistory(usuarioActual, otroUsuario);
            DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());

            fsalida.writeUTF("HISTORY_START");
            for (MensajesChat msg : historial) {
                fsalida.writeUTF("HIST:" + msg.toString());
            }
            fsalida.writeUTF("HISTORY_END");
        } catch (IOException e) {
            System.out.println("Error enviando historial a " + usuarioActual);
        }
    }

    private void enviarMensajeGrupo(String grupo, String contenido) {
        if (MensajesDB.saveGroupMessage(usuarioActual, grupo, contenido)) {
            List<String> miembros = UsuariosDB.obtenerMiembrosGrupo(grupo);
            for (String miembro : miembros) {
                Socket socketDestino = comun.getSocketUsuario(miembro);
                Socket socketNotificacion = comun.getSocketNotificacion(miembro);

                // Enviar al socket de chat
                if (socketDestino != null && !socketDestino.isClosed()) {
                    try {
                        DataOutputStream fsalida = new DataOutputStream(socketDestino.getOutputStream());
                        fsalida.writeUTF("/grupo " + usuarioActual + " " + grupo + ": " + contenido);
                        fsalida.writeUTF("/actualizar_grupo " + grupo);
                    } catch (IOException e) {
                        System.out.println("Error enviando mensaje a " + miembro);
                    }
                }

                // Enviar notificación solo a los demás miembros
                if (!miembro.equals(usuarioActual) && socketNotificacion != null && !socketNotificacion.isClosed()) {
                    try {
                        DataOutputStream fsalidaNotif = new DataOutputStream(socketNotificacion.getOutputStream());
                        fsalidaNotif.writeUTF("/grupo " + usuarioActual + " " + grupo + ": " + contenido);
                    } catch (IOException e) {
                        System.out.println("Error enviando notificación a " + miembro);
                    }
                }
            }
        }

    }

    @Override
    public void run() {
        try {
            this.usuarioActual = fentrada.readUTF();


            if (usuarioActual.startsWith("NOTIFY;")) {
                this.usuarioActual = usuarioActual.substring(7); // Eliminar el prefijo "NOTIFY;"
                this.isNotificationSocket = true;
                comun.agregarSocketNotificacion(this.usuarioActual, socket);

                // Mantener la conexión abierta para notificaciones
                try {
                    while (true) {
                        Thread.sleep(1000); // Evitar consumo excesivo de CPU
                    }
                }
                catch (InterruptedException e) {
                    System.out.println("Thread interrumpido " + usuarioActual);
                }
            }
            // Si es un comando del servidor, procesarlo inmediatamente
            if ("SERVER_COMMAND".equals(usuarioActual)) {
                String comando = fentrada.readUTF();


                if (comando.startsWith("/cerrar_ventana_chat ")) {
                    String grupo = comando.split(" ", 2)[1];


                    List<String> miembros = UsuariosDB.obtenerMiembrosGrupo(grupo);


                    for (String miembro : miembros) {

                        Socket socketDestino = comun.getSocketUsuario(miembro);

                        if (socketDestino != null) {

                            if (!socketDestino.isClosed()) {
                                try {

                                    DataOutputStream fsalida = new DataOutputStream(socketDestino.getOutputStream());
                                    fsalida.writeUTF("/cerrar_ventana_chat");
                                    fsalida.flush();

                                } catch (IOException e) {

                                    e.printStackTrace();
                                }
                            } else {

                            }
                        } else {

                        }
                    }
                }
                return;
            }
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
                } else if (mensaje.startsWith("/grupo ")) {
                    String[] partes = mensaje.split(" ", 3);
                    if (partes.length >= 3) {
                        String grupo = partes[1];
                        String contenido = partes[2];

                        if (UsuariosDB.perteneceAlGrupo(usuarioActual, grupo)) {
                            enviarMensajeGrupo(grupo, contenido);
                        }
                    }

                } else if (mensaje.startsWith("/historial ")) {
                    String contacto = mensaje.split(" ", 2)[1];
                    if (UsuariosDB.sonContactos(usuarioActual, contacto)) {
                        enviarHistorialChat(contacto);
                    } else if (mensaje.startsWith("/notificar_cambio_grupo ")) {
                        // Nuevo caso para manejar la notificación de cambio de nombre
                        String[] partes = mensaje.split(" ", 3);
                        if (partes.length >= 3) {
                            String grupoAntiguo = partes[1];
                            String grupoNuevo = partes[2];
                            notificarCambioNombreGrupo(grupoAntiguo, grupoNuevo);
                        }
                    } else if (mensaje.startsWith("/cerrar_chat_grupo ")) {
                        String grupoCerrar = mensaje.split(" ", 2)[1];
                        List<String> miembros = UsuariosDB.obtenerMiembrosGrupo(grupoCerrar);
                        for (String miembro : miembros) {
                            Socket socketDestino = comun.getSocketUsuario(miembro);
                            if (socketDestino != null && !socketDestino.isClosed()) {
                                try {
                                    DataOutputStream fsalida = new DataOutputStream(socketDestino.getOutputStream());
                                    fsalida.writeUTF("/cerrar_ventana_chat");
                                } catch (IOException e) {
                                    System.out.println("Error enviando señal de cierre a " + miembro);
                                }
                            }
                        }
                    }

                }
            }
        } catch (IOException e) {
            if (usuarioActual != null) {
                if (isNotificationSocket) {
                    comun.eliminarSocketNotificacion(usuarioActual);
                } else {
                    comun.eliminarUsuario(usuarioActual);
                }
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }
}