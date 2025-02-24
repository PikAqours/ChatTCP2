package chattcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorRegistro {
    public static void main(String[] args) {
        int puerto = 44445; // Puerto para registro y login
        try {
            ServerSocket serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor de registro iniciado en el puerto " + puerto);
            while (true) {
                Socket socket = serverSocket.accept();
                new RegistroHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class RegistroHandler extends Thread {
    private Socket socket;

    public RegistroHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

            // Leer el mensaje del cliente
            String mensaje = entrada.readUTF();
            System.out.println("Mensaje recibido: " + mensaje);

            // Procesar el mensaje según su tipo
            if (mensaje.equals("TEST")) {
                // Respuesta para el mensaje de prueba
                salida.writeUTF("OK");
            } else if (mensaje.startsWith("REGISTER;")) {
                // Procesar registro de usuario
                String[] partes = mensaje.split(";");
                if (partes.length == 3) {
                    String username = partes[1];
                    String password = partes[2];
                    boolean registrado = UsuariosDB.registrarUsuario(username, password);
                    if (registrado) {
                        salida.writeUTF("OK");
                    } else {
                        salida.writeUTF("ERROR: Usuario ya existe");
                    }
                } else {
                    salida.writeUTF("ERROR: Formato incorrecto");
                }
            }
            else if (mensaje.startsWith("VERIFICAR_ESTADO;")) {
                // Verificar estado del usuario
                String[] partes = mensaje.split(";");
                if (partes.length == 2) {
                    String username = partes[1];
                    boolean isActive = UsuariosActivos.isUserActive(username);
                    salida.writeUTF(isActive ? "ONLINE" : "OFFLINE");
                } else {
                    salida.writeUTF("ERROR: Formato incorrecto");
                }
            }
            else if (mensaje.startsWith("LOGIN;")) {
                // Procesar inicio de sesión
                String[] partes = mensaje.split(";");
                if (partes.length == 3) {
                    String username = partes[1];
                    String password = partes[2];

                    // First check if user is already active
                    if (UsuariosActivos.isUserActive(username)) {
                        salida.writeUTF("ERROR: Usuario ya está conectado");
                        return;
                    }

                    // Then validate credentials
                    boolean valid = UsuariosDB.validarUsuario(username, password);
                    if (valid) {
                        // Try to set user as active
                        if (UsuariosActivos.setUserActive(username)) {
                            salida.writeUTF("OK");

                            // Add shutdown hook to ensure user is marked as inactive when connection is lost
                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                UsuariosActivos.setUserInactive(username);
                            }));
                        } else {
                            salida.writeUTF("ERROR: Usuario ya está conectado");
                        }
                    } else {
                        salida.writeUTF("ERROR: Credenciales inválidas");
                    }
                } else {
                    salida.writeUTF("ERROR: Formato incorrecto");
                }
            } else if (mensaje.startsWith("LOGOUT;")) {
                // Add logout handling
                String[] partes = mensaje.split(";");
                if (partes.length == 2) {
                    String username = partes[1];
                    UsuariosActivos.setUserInactive(username);
                    salida.writeUTF("OK");
                } else {
                    salida.writeUTF("ERROR: Formato incorrecto");
                }
            } else {
                // Comando no reconocido
                salida.writeUTF("ERROR: Comando no reconocido");
            }

            // Cerrar la conexión
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
