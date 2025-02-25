package chattcp.ServerConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;


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



    public synchronized static boolean registrarUsuario(String username, String password) {
        String sql = "INSERT INTO usuarios(nombre_usuario, contrasena) VALUES(?, ?)";
        // Hashear la contraseña con salt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true; // Usuario registrado
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false; // Usuario ya existe
            }
            System.out.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }


    // Valida que el usuario exista y que la contraseña coincida.
    public synchronized static boolean validarUsuario(String username, String password) {
        String sql = "SELECT contrasena FROM usuarios WHERE nombre_usuario = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Obtén el hash almacenado en la base de datos
                String storedHash = rs.getString("contrasena");
                // Comprueba que la contraseña ingresada coincida con el hash
                return BCrypt.checkpw(password, storedHash);
            }
            return false;
        } catch (Exception e) {
            System.out.println("Nombre de usuario y contraseña incorrectos");
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
        String sqlInsertGrupoUsuario = "INSERT INTO grupo_usuario (id_grupo, id_usuario, es_admin) VALUES (?, (SELECT id FROM usuarios WHERE nombre_usuario = ?), ?)";

        Connection conn = null;
        try {
            conn = connect();
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

            // 2. Insertar al usuario actual en grupo_usuario como administrador (es_admin = 1)
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlInsertGrupoUsuario)) {
                pstmt2.setInt(1, idGrupo);
                pstmt2.setString(2, usuarioActual);
                pstmt2.setInt(3, 1); // Establecer como administrador
                pstmt2.executeUpdate();
            }

            // 3. Insertar todos los contactos seleccionados en grupo_usuario como usuarios normales (es_admin = 0)
            try (PreparedStatement pstmt3 = conn.prepareStatement(sqlInsertGrupoUsuario)) {
                for (String contacto : contactosSeleccionados) {
                    pstmt3.setInt(1, idGrupo);
                    pstmt3.setString(2, contacto);
                    pstmt3.setInt(3, 0); // Usuario normal
                    pstmt3.executeUpdate();
                }
            }

            conn.commit(); // Confirmar transacción
            return true;
        } catch (SQLException e) {
            System.out.println("Error al crear el grupo: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Deshacer la transacción en caso de error
                } catch (SQLException ex) {
                    System.out.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
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
    public static boolean isUserGroupAdmin(String nombreUsuario, String nombreGrupo) {
        String query = """
        SELECT gu.es_admin 
        FROM grupo_usuario gu 
        JOIN usuarios u ON gu.id_usuario = u.id 
        JOIN grupos g ON gu.id_grupo = g.id 
        WHERE u.nombre_usuario = ? AND g.nombre = ?
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, nombreGrupo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("es_admin") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public static List<String> obtenerUsuariosDisponiblesGrupo(String nombreGrupo) {
        List<String> usuariosDisponibles = new ArrayList<>();
        String query = """
        SELECT u.nombre_usuario 
        FROM usuarios u 
        WHERE u.id NOT IN (
            SELECT gu.id_usuario 
            FROM grupo_usuario gu 
            JOIN grupos g ON g.id = gu.id_grupo 
            WHERE g.nombre = ?
        )
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nombreGrupo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                usuariosDisponibles.add(rs.getString("nombre_usuario"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuariosDisponibles;
    }

    public static boolean actualizarGrupo(String grupoActual, String nuevoNombre,
                                          List<String> usuariosActuales,
                                          List<String> usuariosEliminados) {
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            // 1. First get the group ID using the CURRENT name
            int groupId;
            String getGroupId = "SELECT id FROM grupos WHERE nombre = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(getGroupId)) {
                pstmt.setString(1, grupoActual);  // Use current name, not new name
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                groupId = rs.getInt("id");
            }

            // 2. Update group name if it changed
            if (!grupoActual.equals(nuevoNombre)) {
                String updateGroupName = "UPDATE grupos SET nombre = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateGroupName)) {
                    pstmt.setString(1, nuevoNombre);
                    pstmt.setInt(2, groupId);     // Use ID instead of name for update
                    pstmt.executeUpdate();
                }
            }

            // 3. Remove specified users from group
            if (!usuariosEliminados.isEmpty()) {
                String deleteUsers = """
                DELETE FROM grupo_usuario 
                WHERE id_grupo = ? 
                AND id_usuario IN (
                    SELECT id FROM usuarios WHERE nombre_usuario IN (
                    """ + String.join(",", Collections.nCopies(usuariosEliminados.size(), "?")) + "))";

                try (PreparedStatement pstmt = conn.prepareStatement(deleteUsers)) {
                    pstmt.setInt(1, groupId);
                    for (int i = 0; i < usuariosEliminados.size(); i++) {
                        pstmt.setString(i + 2, usuariosEliminados.get(i));
                    }
                    pstmt.executeUpdate();
                }
            }

            // 4. Add new users that aren't in the group yet
            if (!usuariosActuales.isEmpty()) {
                String addUser = """
                INSERT INTO grupo_usuario (id_grupo, id_usuario, es_admin) 
                SELECT ?, u.id, 0 
                FROM usuarios u 
                WHERE u.nombre_usuario = ? 
                AND NOT EXISTS (
                    SELECT 1 FROM grupo_usuario 
                    WHERE id_grupo = ? 
                    AND id_usuario = u.id
                )
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(addUser)) {
                    for (String usuario : usuariosActuales) {
                        pstmt.setInt(1, groupId);
                        pstmt.setString(2, usuario);
                        pstmt.setInt(3, groupId);
                        pstmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean promoverAdmin(String nombreGrupo, String nombreUsuario) {
        String query = """
        UPDATE grupo_usuario 
        SET es_admin = 1 
        WHERE id_grupo = (SELECT id FROM grupos WHERE nombre = ?) 
        AND id_usuario = (SELECT id FROM usuarios WHERE nombre_usuario = ?)
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nombreGrupo);
            pstmt.setString(2, nombreUsuario);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean borrarContacto(String usuarioActual, String contactoAEliminar) {
        String sql = "DELETE FROM contactos " +
                "WHERE id_usuario = (SELECT id FROM usuarios WHERE nombre_usuario = ?) " +
                "AND id_contacto = (SELECT id FROM usuarios WHERE nombre_usuario = ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuarioActual);
            pstmt.setString(2, contactoAEliminar);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.out.println("Error al borrar contacto: " + e.getMessage());
            return false;
        }
    }
    public static boolean eliminarGrupo(String nombreGrupo) {
        Connection conn = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            // 1. Obtener el ID del grupo a partir de su nombre
            int idGrupo;
            String queryId = "SELECT id FROM grupos WHERE nombre = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(queryId)) {
                pstmt.setString(1, nombreGrupo);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    idGrupo = rs.getInt("id");
                } else {
                    conn.rollback();
                    return false; // El grupo no existe
                }
            }

            // 2. Eliminar los mensajes asociados al grupo (tabla "mensajes" y columna "id_grupo")
            String deleteMensajes = "DELETE FROM mensajes WHERE id_grupo = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteMensajes)) {
                pstmt.setInt(1, idGrupo);
                pstmt.executeUpdate();
            }

            // 3. Eliminar las relaciones de usuarios con el grupo (tabla "grupo_usuario")
            String deleteUsuarios = "DELETE FROM grupo_usuario WHERE id_grupo = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUsuarios)) {
                pstmt.setInt(1, idGrupo);
                pstmt.executeUpdate();
            }

            // 4. Eliminar el grupo de la tabla "grupos"
            String deleteGrupo = "DELETE FROM grupos WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteGrupo)) {
                pstmt.setInt(1, idGrupo);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Error al eliminar grupo: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}





