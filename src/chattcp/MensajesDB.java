package chattcp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MensajesDB {
    private static final String URL = "jdbc:sqlite:C:\\DAM\\chatTCP\\chatTCP\\model\\ChatDB.db";

    // Get user ID by username
    public static int getUserId(String username) {
        String sql = "SELECT id FROM usuarios WHERE nombre_usuario = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting user ID: " + e.getMessage());
        }
        return -1;
    }

    // Save a private message
    public static boolean saveMessage(String fromUser, String toUser, String message) {
        String sql = "INSERT INTO mensajes (id_usuario, id_destinatario, mensaje) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int fromUserId = getUserId(fromUser);
            int toUserId = getUserId(toUser);

            if (fromUserId == -1 || toUserId == -1) {
                return false;
            }

            pstmt.setInt(1, fromUserId);
            pstmt.setInt(2, toUserId);
            pstmt.setString(3, message);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error saving message: " + e.getMessage());
            return false;
        }
    }

    // Get chat history between two users
    public static List<ChatMessage> getChatHistory(String user1, String user2) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = """
        SELECT m.*, 
               u1.nombre_usuario as sender_name,
               u2.nombre_usuario as receiver_name,
               m.mensaje,
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

        try (Connection conn =  DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the usernames directly in the query
            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String senderName = rs.getString("sender_name");
                String mensaje = rs.getString("mensaje");
                Timestamp fecha = rs.getTimestamp("fecha");

                messages.add(new ChatMessage(senderName, mensaje, fecha));
            }
        } catch (SQLException e) {
            System.out.println("Error getting chat history: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }
}