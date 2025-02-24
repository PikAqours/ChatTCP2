package chattcp;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class Notificacion {
    private static Notificacion instancia;
    private TrayIcon trayIcon;
    private static final String ICON_PATH = "chatTCP/res/notification_icon.png"; // Ajusta la ruta según tu estructura

    private Notificacion() {
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("El system tray no es soportado en este sistema.");
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(ICON_PATH);
            if (image == null) {
                image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/res/notification_icon.png"));
            }

            trayIcon = new TrayIcon(image, "Chat TCP");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException("Error al agregar el icono en el system tray.", e);
        }
    }

    public static synchronized Notificacion getInstance() {
        if (instancia == null) {
            instancia = new Notificacion();
        }
        return instancia;
    }

    public void mostrarNotificacionPrivada(String remitente, String mensaje) {
        String textoNotificacion = String.format("Nuevo mensaje de %s:\n%s",
                remitente,
                mensaje.length() > 50 ? mensaje.substring(0, 47) + "..." : mensaje);

        trayIcon.displayMessage("Mensaje Privado", textoNotificacion, MessageType.INFO);
    }

    public void mostrarNotificacionGrupo(String remitente, String grupo, String mensaje) {
        String textoNotificacion = String.format("%s en %s:\n%s",
                remitente,
                grupo,
                mensaje.length() > 50 ? mensaje.substring(0, 47) + "..." : mensaje);

        trayIcon.displayMessage("Mensaje de Grupo", textoNotificacion, MessageType.INFO);
    }

    public void mostrarNotificacionSistema(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, MessageType.NONE);
    }
}