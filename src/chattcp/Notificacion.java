package chattcp;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class Notificacion {
    private TrayIcon trayIcon;

    public Notificacion() {
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("El system tray no es soportado en este sistema.");
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icono.png");
        trayIcon = new TrayIcon(image, "Notificación");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException("Error al agregar el icono en el system tray.", e);
        }
    }

    public void mostrarMensaje(String mensaje) {
        trayIcon.displayMessage("Mensaje", mensaje, MessageType.INFO);
    }

    public static void main(String[] args) {
        Notificacion notificacion = new Notificacion();
        notificacion.mostrarMensaje("Este es un mensaje de prueba.");
    }
}
