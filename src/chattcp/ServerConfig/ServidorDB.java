package chattcp.ServerConfig;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServidorDB {
    public static void main(String[] args) {
        int puerto = 44446; // Puerto para peticiones de BD
        try {
            ServerSocket serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor de Base de Datos iniciado en el puerto " + puerto);
            while (true) {
                Socket socket = serverSocket.accept();
                new DBHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DBHandler extends Thread {
    private Socket socket;

    public DBHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

            // Se espera un comando en formato: "COMANDO;param1;param2;..."
            String comandoCompleto = entrada.readUTF();
            System.out.println("Comando recibido: " + comandoCompleto);
            String[] partes = comandoCompleto.split(";");
            String comando = partes[0];
            String respuesta = "";

            switch (comando) {
                case "OBTENER_USUARIOS_DISPONIBLES": {
                    // Formato: "OBTENER_USUARIOS_DISPONIBLES;usuarioActual"
                    if (partes.length >= 2) {
                        String usuarioActual = partes[1];
                        List<String> usuarios = UsuariosDB.obtenerUsuariosDisponibles(usuarioActual);
                        // Serializamos la lista en una cadena separada por comas
                        respuesta = String.join(",", usuarios);
                    } else {
                        respuesta = "ERROR: Parámetros insuficientes";
                    }
                    break;
                }
                case "OBTENER_CONTACTOS": {
                    // Formato: "OBTENER_CONTACTOS;usuarioActual"
                    if (partes.length >= 2) {
                        String usuarioActual = partes[1];
                        List<String> contactos = UsuariosDB.obtenerContactos(usuarioActual);
                        respuesta = String.join(",", contactos);
                    } else {
                        respuesta = "ERROR: Parámetros insuficientes";
                    }
                    break;
                }
                case "AGREGAR_CONTACTO": {
                    // Formato: "AGREGAR_CONTACTO;usuarioActual;nuevoContacto"
                    if (partes.length >= 3) {
                        String usuarioActual = partes[1];
                        String nuevoContacto = partes[2];
                        boolean exito = UsuariosDB.agregarContacto(usuarioActual, nuevoContacto);
                        respuesta = exito ? "OK" : "ERROR: No se pudo agregar";
                    } else {
                        respuesta = "ERROR: Parámetros insuficientes";
                    }
                    break;
                }
                case "OBTENER_GRUPOS": {
                    // Formato: "OBTENER_GRUPOS;usuarioActual
                    if (partes.length >= 2) {
                        String usuarioActual = partes[1];

                        List<String> grupos = UsuariosDB.obtenerGrupos(usuarioActual);
                        respuesta = String.join(",", grupos);
                    } else {
                        respuesta = "ERROR: Parámetros insuficientes";
                    }
                    break;
                }
                case "CREAR_GRUPO": {
                    // Formato: "CREAR_GRUPO;usuarioActual;nombreGrupo;contacto1,contacto2,contacto3"
                    if (partes.length >= 4) {
                        String usuarioActual = partes[1];
                        String nombreGrupo = partes[2];
                        String contactosStr = partes[3];
                        String[] arrContactos = contactosStr.split(",");
                        List<String> contactosSeleccionados = new ArrayList<>();
                        for (String c : arrContactos) {
                            if (!c.trim().isEmpty()) {
                                contactosSeleccionados.add(c.trim());
                            }
                        }
                        boolean exito = UsuariosDB.crearGrupo(usuarioActual, nombreGrupo, contactosSeleccionados);
                        respuesta = exito ? "OK" : "ERROR: No se pudo crear el grupo";
                    } else {
                        respuesta = "ERROR: Parámetros insuficientes";
                    }
                    break;
                }
                case "VERIFICAR_CONTACTO": {
                    if (partes.length == 3) {
                        String usuario1 = partes[1];
                        String usuario2 = partes[2];
                        boolean sonContactos = UsuariosDB.sonContactos(usuario1, usuario2);
                        salida.writeUTF(sonContactos ? "OK" : "ERROR");
                    } else {
                        salida.writeUTF("ERROR");
                    }
                    break;
                }
                case "OBTENER_HISTORIAL": {

                    if (partes.length == 3) {
                        String usuario1 = partes[1];
                        String usuario2 = partes[2];

                        List<MensajesChat> history = UsuariosDB.getChatHistory(usuario1, usuario2);

                        salida.writeUTF("HISTORY_START");
                        for (MensajesChat msg : history) {
                            salida.writeUTF(msg.toString());
                        }
                        salida.writeUTF("HISTORY_END");
                    }
                    break;
                }
                case "CHECK_ADMIN": {
                        // Formato: "CHECK_ADMIN;nombreGrupo;nombreUsuario"

                        if (partes.length >= 3) {
                            String nombreGrupo = partes[1];
                            String nombreUsuario = partes[2];

                            boolean isAdmin = UsuariosDB.isUserGroupAdmin(nombreUsuario, nombreGrupo);
                            salida.writeBoolean(isAdmin);  // Using writeBoolean instead of writeUTF for boolean value
                        }
                        else {
                            salida.writeBoolean(false);  // If parameters are insufficient, return false
                        }
                        break;

                }
                case "OBTENER_USUARIOS_GRUPO": {
                    // Format: OBTENER_USUARIOS_GRUPO;nombreGrupo
                    if (partes.length >= 2) {
                        String nombreGrupo = partes[1];
                        List<String> usuarios = UsuariosDB.obtenerMiembrosGrupo(nombreGrupo);
                        respuesta = String.join(",", usuarios);
                    } else {
                        respuesta = "ERROR";
                    }
                    break;
                }

                case "OBTENER_USUARIOS_DISPONIBLES_GRUPO": {
                    // Format: OBTENER_USUARIOS_DISPONIBLES_GRUPO;nombreGrupo
                    if (partes.length >= 2) {
                        String nombreGrupo = partes[1];
                        List<String> usuarios = UsuariosDB.obtenerUsuariosDisponiblesGrupo(nombreGrupo);
                        respuesta = usuarios.isEmpty() ? "ERROR" : String.join(",", usuarios);
                    } else {
                        respuesta = "ERROR";
                    }
                    System.out.println(respuesta);
                    salida.writeUTF(respuesta);
                    break;
                }

                case "ACTUALIZAR_GRUPO": {
                    if (partes.length >= 5) {
                        String grupoActual = partes[1];
                        String nuevoNombre = partes[2];
                        String[] usuariosActuales = partes[3].split(",");
                        String[] usuariosEliminados = partes[4].split(",");

                        System.out.println("Actualizando grupo: " + grupoActual + " -> " + nuevoNombre);
                        System.out.println("Usuarios actuales: " + String.join(", ", usuariosActuales));

                        boolean exito = UsuariosDB.actualizarGrupo(grupoActual, nuevoNombre,
                                Arrays.asList(usuariosActuales),
                                Arrays.asList(usuariosEliminados));

                        if (exito) {
                            try (Socket chatSocket = new Socket("localhost", 44444);
                                 DataOutputStream chatOut = new DataOutputStream(chatSocket.getOutputStream())) {

                                // Importante: Usar el nombre actual del grupo, no el nuevo

                                chatOut.writeUTF("SERVER_COMMAND");
                                chatOut.writeUTF("/cerrar_ventana_chat " + nuevoNombre);
                                chatOut.flush();
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                            respuesta = "OK";
                        } else {
                            respuesta = "ERROR";
                        }
                    }
                    break;
                }

                case "PROMOVER_ADMIN": {
                    // Format: PROMOVER_ADMIN;nombreGrupo;nombreUsuario
                    if (partes.length >= 3) {
                        String nombreGrupo = partes[1];
                        String nombreUsuario = partes[2];
                        boolean exito = UsuariosDB.promoverAdmin(nombreGrupo, nombreUsuario);
                        respuesta = exito ? "OK" : "ERROR";
                    } else {
                        respuesta = "ERROR";
                    }
                    break;
                }
                default: {
                    respuesta = "ERROR: Comando no reconocido";
                    break;
                }
            }

            salida.writeUTF(respuesta);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}