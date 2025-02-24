package chattcp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuariosDB {
    private static final String URL = "jdbc:sqlite:chatTCP/model/ChatDB.db";

    // Inicializa la conexión a la base de datos
    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return conn;
    }

    // Registra un usuario si no existe. Retorna true si se creó, false si ya existía.
    public synchronized static boolean registrarUsuario(String username, String password) {
        String sql = "INSERT INTO usuarios(nombre_usuario, contrasena) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true; // Usuario registrado
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false; // Ya existe el usuario
            }
            System.out.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    // Valida que el usuario exista y que la contraseña coincida.
    public synchronized static boolean validarUsuario(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ? AND contrasena = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Retorna true si se encontró el usuario con la contraseña correcta
        } catch (SQLException e) {
            System.out.println("Error al validar usuario: " + e.getMessage());
            return false;
        }
    }

    // Obtiene la lista de usuarios disponibles (excluyendo al usuario actual)
    public static List<String> obtenerUsuariosDisponibles(String usuarioActual) {
        List<String> usuarios = new ArrayList<>();
        String sql = "SELECT nombre_usuario FROM usuarios WHERE nombre_usuario != ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuarioActual);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                usuarios.add(rs.getString("nombre_usuario"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener usuarios disponibles: " + e.getMessage());
        }
        return usuarios;
    }

    // Agrega un contacto a la lista de contactos del usuario actual
    public static boolean agregarContacto(String usuarioActual, String nuevoContacto) {
        String sql = "INSERT INTO contactos (id_usuario, id_contacto) VALUES ((SELECT id FROM usuarios WHERE nombre_usuario = ?), (SELECT id FROM usuarios WHERE nombre_usuario = ?))";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuarioActual);
            pstmt.setString(2, nuevoContacto);
            pstmt.executeUpdate();
            return true; // Contacto agregado correctamente
        }
        catch (SQLException e) {
            System.out.println("Error al agregar contacto: " + e.getMessage());
            return false;
        }
    }
    public static List obtenerContactos(String usuarioActual) {
        List<String> contactos = new ArrayList<>();
        String sql = """
        SELECT u.nombre_usuario
        FROM contactos c
                 JOIN usuarios u ON c.id_contacto = u.id
        where id_usuario=(SELECT id FROM usuarios where nombre_usuario=?)
        
        """;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuarioActual);
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()) {
                contactos.add(rs.getString("nombre_usuario"));
            }
            return contactos;
        }

        catch (SQLException e) {
            System.out.println("Error al agregar contacto: " + e.getMessage());
            return contactos;
        }
    }
    public static boolean crearGrupo(String usuarioActual, String nuevoGrupo, List<String> contactosSeleccionados) {
        String sqlInsertGrupo = "INSERT INTO grupos (nombre) VALUES (?)";
        String sqlInsertGrupoUsuario = "INSERT INTO grupo_usuario (id_grupo, id_usuario) VALUES (?, (SELECT id FROM usuarios WHERE nombre_usuario = ?))";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false); // Para manejar la transacción

            // 1. Insertar el grupo y obtener su ID
            int idGrupo;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertGrupo, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, nuevoGrupo);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGrupo = rs.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID del grupo.");
                    }
                }
            }

            // 2. Insertar al usuario actual en grupo_usuario
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlInsertGrupoUsuario)) {
                pstmt2.setInt(1, idGrupo);
                pstmt2.setString(2, usuarioActual);
                pstmt2.executeUpdate();
            }

            // 3. Insertar todos los contactos seleccionados en grupo_usuario
            try (PreparedStatement pstmt3 = conn.prepareStatement(sqlInsertGrupoUsuario)) {
                for (String contacto : contactosSeleccionados) {
                    pstmt3.setInt(1, idGrupo);
                    pstmt3.setString(2, contacto);
                    pstmt3.executeUpdate();
                }
            }

            conn.commit(); // Confirmar transacción
            return true;
        } catch (SQLException e) {
            System.out.println("Error al crear el grupo: " + e.getMessage());
            return false;
        }
    }
    public static List obtenerGrupos(String usuarioActual) {
        List<String> grupos = new ArrayList<>();
        String sql = """
        
                SELECT DISTINCT nombre from grupo_usuario JOIN grupos g on g.id = grupo_usuario.id_grupo
                                                          JOIN usuarios u on u.id = grupo_usuario.id_usuario
                WHERE nombre_usuario=?
        
        """;
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuarioActual);
            ResultSet rs= pstmt.executeQuery();
            while (rs.next()) {
                grupos.add(rs.getString("nombre"));
            }
            return grupos;
        }

        catch (SQLException e) {
            System.out.println("Error al agregar contacto: " + e.getMessage());
            return grupos;
        }
    }

    public static boolean sonContactos(String usuario1, String usuario2) {
        String sql = "SELECT COUNT(*) FROM contactos WHERE " +
                "(id_usuario = (SELECT id FROM usuarios WHERE nombre_usuario = ?) AND " +
                "id_contacto = (SELECT id FROM usuarios WHERE nombre_usuario = ?)) OR " +
                "(id_usuario = (SELECT id FROM usuarios WHERE nombre_usuario = ?) AND " +
                "id_contacto = (SELECT id FROM usuarios WHERE nombre_usuario = ?))";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario1);
            pstmt.setString(2, usuario2);
            pstmt.setString(3, usuario2);
            pstmt.setString(4, usuario1);

            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean perteneceAlGrupo(String usuario, String grupo) {
        String sql = "SELECT COUNT(*) FROM grupo_usuario " +
                "WHERE id_grupo = (SELECT id FROM grupos WHERE nombre = ?) " +
                "AND id_usuario = (SELECT id FROM usuarios WHERE nombre_usuario = ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, grupo);
            pstmt.setString(2, usuario);

            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> obtenerMiembrosGrupo(String grupo) {
        List<String> miembros = new ArrayList<>();
        String sql = "SELECT u.nombre_usuario FROM grupo_usuario gu " +
                "JOIN usuarios u ON gu.id_usuario = u.id " +
                "WHERE gu.id_grupo = (SELECT id FROM grupos WHERE nombre = ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, grupo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                miembros.add(rs.getString("nombre_usuario"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return miembros;
    }
    public static List<MensajesChat> getChatHistory(String usuario1, String usuario2) {
        List<MensajesChat> messages = new ArrayList<>();
        String sql = """
        SELECT m.*, 
               u1.nombre_usuario as sender_name,
               u2.nombre_usuario as receiver_name,
               m.fecha
        FROM mensajes m
        JOIN usuarios u1 ON m.id_usuario = u1.id
        JOIN usuarios u2 ON m.id_destinatario = u2.id
        WHERE (m.id_usuario = (SELECT id FROM usuarios WHERE nombre_usuario = ?) 
              AND m.id_destinatario = (SELECT id FROM usuarios WHERE nombre_usuario = ?))
        OR (m.id_usuario = (SELECT id FROM usuarios WHERE nombre_usuario = ?) 
            AND m.id_destinatario = (SELECT id FROM usuarios WHERE nombre_usuario = ?))
        ORDER BY m.fecha ASC
    """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario1);
            pstmt.setString(2, usuario2);
            pstmt.setString(3, usuario2);
            pstmt.setString(4, usuario1);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String senderName = rs.getString("sender_name");
                String message = rs.getString("mensaje");
                Timestamp timestamp = rs.getTimestamp("fecha");
                messages.add(new MensajesChat(senderName, message, timestamp));
            }
        } catch (SQLException e) {
            System.out.println("Error getting chat history: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

}
