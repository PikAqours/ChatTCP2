package chattcp;


import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    private static final int WORKLOAD = 12; // Nivel de costo para BCrypt

    /**
     * Genera un hash de la contraseña usando BCrypt
     * @param passwordPlaintext La contraseña en texto plano
     * @return El hash de la contraseña
     */
    public static String hashPassword(String passwordPlaintext) {
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(passwordPlaintext, salt);
    }

    /**
     * Verifica si una contraseña coincide con su hash
     * @param passwordPlaintext La contraseña en texto plano
     * @param storedHash El hash almacenado
     * @return true si la contraseña coincide, false en caso contrario
     */
    public static boolean checkPassword(String passwordPlaintext, String storedHash) {
        if (null == storedHash || !storedHash.startsWith("$2a$")) {
            throw new IllegalArgumentException("Hash inválido");
        }
        return BCrypt.checkpw(passwordPlaintext, storedHash);
    }
}