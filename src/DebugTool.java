import javafx.scene.web.WebEngine;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class DebugTool {

    public static void log(String area, String msg) {
        System.out.println(String.format("[DEBUG][%s] %s", area, msg));
    }


    public static void imprimirCofreJava(CookieManager manager) {
        System.out.println("\n=== RAIO-X: COFRE DE COOKIES (JAVA) ===");
        try {
            URI uri = new URI("https://steamcommunity.com");
            Map<String, List<String>> headers = manager.get(uri, Map.of());
            
            if (headers.isEmpty() || !headers.containsKey("Cookie")) {
                System.out.println(">> O cofre Java parece VAZIO para steamcommunity.com!");
            } else {
                List<String> cookies = headers.get("Cookie");
                for (String c : cookies) {
                    System.out.println(">> HEADER: " + c);
                }
            }
            
            System.out.println("--- Detalhes (CookieStore) ---");
            List<HttpCookie> storeCookies = manager.getCookieStore().getCookies();
            for (HttpCookie c : storeCookies) {
                System.out.printf("Nome: %-20s | Dominio: %-25s | Secure: %-5s | HttpOnly: %-5s\n", 
                    c.getName(), c.getDomain(), c.getSecure(), c.isHttpOnly());
                
                if (c.getName().equals("steamLoginSecure")) {
                    System.out.println("   -> Valor (inicio): " + c.getValue().substring(0, Math.min(15, c.getValue().length())) + "...");
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao ler cofre: " + e.getMessage());
        }
        System.out.println("=======================================\n");
    }

    public static void imprimirNavegador(WebEngine engine) {
        System.out.println("\n=== RAIO-X: NAVEGADOR (WEBVIEW) ===");
        try {
            String url = engine.getLocation();
            System.out.println("URL Atual: " + url);
            
            String cookiesJS = (String) engine.executeScript("document.cookie");
            if (cookiesJS == null || cookiesJS.isBlank()) {
                System.out.println(">> O navegador diz que NAO TEM COOKIES (document.cookie vazio)!");
            } else {
                System.out.println(">> Cookies visiveis via JS: " + cookiesJS);
                if (!cookiesJS.contains("steamLoginSecure")) {
                    System.out.println(">> ALERTA: 'steamLoginSecure' nao aparece no JS (Isso é normal se for HttpOnly, mas o Java tem que ter enviado!)");
                }
            }
        } catch (Exception e) {
            System.out.println("Nao foi possivel ler o navegador (talvez pagina nao carregada).");
        }
        System.out.println("===================================\n");
    }
}