package chattcp;

import chattcp.ServerConfig.ComunHilos;
import chattcp.ServerConfig.HiloServidorChat;
import chattcp.ServerConfig.ServidorDB;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    static final int maximo = 10; // Máximo de Conexiones Permitidas

    public static void main(String[] args) throws IOException {
        // Iniciar el servidor de base de datos en un hilo aparte
        new Thread(() -> {
            ServidorDB.main(new String[0]);
        }).start();

        // Puertos para los servidores de registro y chat
        int puertoRegistro = 44445;
        int puertoChat = 44444;

        // Servidor de registro
        ServerSocket servidorRegistro = new ServerSocket(puertoRegistro);
        System.out.println("Servidor de registro iniciado en el puerto " + puertoRegistro);

        // Servidor de chat
        ServerSocket servidorChat = new ServerSocket(puertoChat);
        System.out.println("Servidor de chat iniciado en el puerto " + puertoChat);

        // Control de clientes para el servidor de chat
        ComunHilos comun = new ComunHilos(maximo);

        // Hilo para manejar conexiones de registro
        Thread registroThread = new Thread(() -> {
            try {
                while (true) {
                    Socket socket = servidorRegistro.accept();
                    new RegistroHandler(socket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        registroThread.start(); // Ejecuta el servidor de registro en un hilo separado

        // Hilos para manejar conexiones de chat
        while (true) {
            Socket socket = servidorChat.accept(); // Esperando conexión del cliente de chat

            if (comun.getConexiones() < maximo) {
                // Create and start the chat thread immediately
                // The username will be handled in HiloServidorChat
                HiloServidorChat hilo = new HiloServidorChat(socket, comun);
                hilo.start();
            } else {
                System.out.println("Servidor lleno, rechazando conexión...");
                socket.close(); // Rechaza la conexión si está lleno
            }
        }
    }
}