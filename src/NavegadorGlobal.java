import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.io.File;

public class NavegadorGlobal {

  private static WebView webView;
  private static final File PASTA_CACHE = new File("steam_cache");

  public static void inicializar() {
    if (webView == null) {
      webView = new WebView();
      WebEngine engine = webView.getEngine();

      engine.setUserDataDirectory(PASTA_CACHE);
      engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

      System.out.println("Navegador Global inicializado.");
    }
  }

  public static void limparListeners() {
    if (webView != null) {}
  }

  public static WebView getWebView() {
    if (webView == null) inicializar();
    return webView;
  }

  public static WebEngine getEngine() {
    return getWebView().getEngine();
  }
}