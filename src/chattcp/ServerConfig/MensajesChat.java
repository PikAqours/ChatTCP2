package chattcp.ServerConfig;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class MensajesChat {
    private final String sender;
    private final String message;
    private final Timestamp timestamp;

    public MensajesChat(String sender, String message, Timestamp timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s> %s",
                sdf.format(timestamp),
                sender,
                message);
    }
}