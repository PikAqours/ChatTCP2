package chattcp.ServerConfig;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComunHilos {

    private int maximo;  // Máximo de conexiones permitidas
    private String mensajes; // Mensajes del Chat

    private final List<Socket> conexiones = new ArrayList<>(); // Lista de sockets conectados
    private final Map<String, Socket> usuariosConectados = new HashMap<>();
    private final Map<String, List<String>> grupos = new HashMap<>();
    private HashMap<String, Socket> notificationSockets = new HashMap<>();

    public ComunHilos(int maximo) {
        this.maximo = maximo;
        this.mensajes = "";
    }
    public synchronized boolean puedeAceptarConexion() {
        return conexiones.size() < maximo;
    }

    // Agregar usuario al mapa de usuarios conectados
    public synchronized boolean agregarUsuario(String usuario, Socket socket) {
        if (puedeAceptarConexion() && !conexiones.contains(socket)) {
            usuariosConectados.put(usuario, socket);
            conexiones.add(socket);
            return true;
        }
        return false;
    }
    public synchronized void agregarSocketNotificacion(String usuario, Socket socket) {
        notificationSockets.put(usuario, socket);
    }

    public synchronized void eliminarSocketNotificacion(String usuario) {
        notificationSockets.remove(usuario);
    }

    public synchronized Socket getSocketNotificacion(String usuario) {
        return notificationSockets.get(usuario);
    }

    // Eliminar usuario del mapa y de la lista de conexiones
    public synchronized void eliminarUsuario(String usuario) {
        Socket socket = usuariosConectados.remove(usuario);
        if (socket != null) {
            conexiones.remove(socket);
        }
    }

    // Obtener el socket de un usuario específico
    public synchronized Socket getSocketUsuario(String usuario) {
        return usuariosConectados.get(usuario);
    }

    // Obtener miembros de un grupo
    public synchronized List<String> getMiembrosGrupo(String nombreGrupo) {
        return grupos.getOrDefault(nombreGrupo, new ArrayList<>());
    }

    // Agregar un grupo con sus miembros
    public synchronized void agregarGrupo(String nombre, List<String> miembros) {
        grupos.put(nombre, miembros);
    }

    public int getMaximo() {
        return maximo;
    }

    public void setMaximo(int maximo) {
        this.maximo = maximo;
    }

    public synchronized String getMensajes() {
        return mensajes;
    }

    public synchronized void setMensajes(String mensajes) {
        this.mensajes = mensajes;
    }



    // Eliminar un socket desconectado
    public synchronized void eliminarSocket(Socket s) {
        conexiones.remove(s);

        usuariosConectados.entrySet().removeIf(entry -> entry.getValue().equals(s));
    }

    // Obtener el número actual de conexiones activas
    public synchronized int getConexiones() {
        return conexiones.size();
    }

    // Obtener un socket en un índice específico
    public synchronized Socket getElementoTabla(int i) {
        if (i >= 0 && i < conexiones.size()) {
            return conexiones.get(i);
        }
        return null;
    }
}