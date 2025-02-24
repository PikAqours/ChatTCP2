package chattcp;

import javax.swing.*;
import java.awt.*;

public class TestLabelIcon {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Prueba de JLabel con Icono");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 150);
            frame.setLocationRelativeTo(null);

            // Probamos con un icono online y texto
            boolean isOnline = true; // Cambia a false para probar el otro icono
            JLabel testLabel = getLabelConIcono("Usuario de Prueba", isOnline);

            JPanel panel = new JPanel();
            panel.add(testLabel);

            frame.add(panel);
            frame.setVisible(true);
        });
    }

    private static JLabel getLabelConIcono(String nombre, boolean isOnline) {
        String iconPath = isOnline ? "path/to/online.png" : "path/to/offline.png";
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(img);

        return new JLabel(nombre, scaledIcon, JLabel.LEFT);
    }
}
