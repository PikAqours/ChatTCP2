package chattcp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GrupoMensajesDB {
    private static final String URL = "jdbc:sqlite:C:\\DAM\\chatTCP\\chatTCP\\model\\ChatDB.db";

    // Save a group message
    public static boolean saveGroupMessage(String fromUser, String groupName, String message) {
        String sql = "INSERT INTO mensajes_grupo (id_usuario, id_grupo, mensaje) VALUES (" +
                "(SELECT id FROM usuarios WHERE nombre_usuario = ?), " +
                "(SELECT id FROM grupos WHERE nombre = ?), ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fromUser);
            pstmt.setString(2, groupName);
            pstmt.setString(3, message);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error saving group message: " + e.getMessage());
            return false;
        }
    }

    // Get chat history for a group
    public static List<ChatMessage> getGroupChatHistory(String groupName) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = """
            SELECT m.*, 
                   u.nombre_usuario as sender_name,
                   m.mensaje,
                   m.fecha
            FROM mensajes_grupo m
            JOIN usuarios u ON m.id_usuario = u.id
            JOIN grupos g ON m.id_grupo = g.id
            WHERE g.nombre = ?
            ORDER BY m.fecha ASC
        """;

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String senderName = rs.getString("sender_name");
                String mensaje = rs.getString("mensaje");
                Timestamp fecha = rs.getTimestamp("fecha");

                messages.add(new ChatMessage(senderName, mensaje, fecha));
            }
        } catch (SQLException e) {
            System.out.println("Error getting group chat history: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }
}