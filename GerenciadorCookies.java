import java.io.*;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

public class GerenciadorCookies {

    private static final String ARQUIVO_COOKIES = "cookies_full.dat";

    public static void salvarTodosCookies(CookieManager manager) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_COOKIES))) {
            List<HttpCookie> cookies = manager.getCookieStore().getCookies();
            for (HttpCookie cookie : cookies) {
                writer.println(cookie.getName() + "|" + cookie.getValue() + "|" + cookie.getDomain() + "|" + cookie.getPath());
            }
            System.out.println("Cookies salvos em disco: " + cookies.size());
        } catch (IOException e) {
            System.out.println("Erro ao salvar cookies: " + e.getMessage());
        }
    }

    public static void carregarCookies(CookieManager manager) {
        File arquivo = new File(ARQUIVO_COOKIES);
        if (!arquivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int count = 0;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split("\\|");
                if (partes.length >= 4) {
                    String nome = partes[0];
                    String valor = partes[1];
                    String dominio = partes[2];
                    String path = partes[3];

                    HttpCookie cookie = new HttpCookie(nome, valor);
                    cookie.setDomain(dominio);
                    cookie.setPath(path);
                    cookie.setVersion(0);
                    

                    cookie.setMaxAge(31536000); 
                    cookie.setSecure(true);
                    cookie.setHttpOnly(true);

                    String dominioLimpo = dominio.startsWith(".") ? dominio.substring(1) : dominio;
                    URI uri = new URI("https://" + dominioLimpo);
                    
                    manager.getCookieStore().add(uri, cookie);
                    count++;
                }
            }
            System.out.println("Cookies restaurados do disco: " + count);
        } catch (Exception e) {
            System.out.println("Erro ao carregar cookies: " + e.getMessage());
        }
    }
}