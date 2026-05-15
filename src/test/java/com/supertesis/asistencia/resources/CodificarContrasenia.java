public class CodificarContrasenia {

    public static void main(String[] args) {
        String password = codificar("admin123");
        System.out.println(password);
    }

    public static String codificar(String contrasenia) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(contrasenia);
    }
}