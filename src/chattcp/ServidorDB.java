package chattcp;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

                        List<ChatMessage> history = UsuariosDB.getChatHistory(usuario1, usuario2);

                        salida.writeUTF("HISTORY_START");
                        for (ChatMessage msg : history) {
                            salida.writeUTF(msg.toString());
                        }
                        salida.writeUTF("HISTORY_END");
                    }
                    break; // FALTABA ESTE BREAK
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