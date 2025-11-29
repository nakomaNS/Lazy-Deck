import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class MainApp extends Application {

    public static CookieManager globalCookieManager;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        Platform.setImplicitExit(false);

        globalCookieManager = new CookieManager();
        globalCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(globalCookieManager);

        NavegadorGlobal.inicializar();
        GerenciadorCookies.carregarCookies(globalCookieManager);

        try {
            String iconPath = "icons/app_icon.png"; 
            File iconFile = new File(iconPath);
            if (iconFile.exists()) {
                javafx.scene.image.Image icon = new javafx.scene.image.Image(iconFile.toURI().toString());
                stage.getIcons().add(icon);
            }
        } catch (Exception e) {
            System.out.println("Aviso: Ícone da janela não encontrado.");
        }

        configurarSystemTray();

        new TelaDashboard(stage).mostrar();
    }

    private void configurarSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray não suportado!");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            

            java.awt.Image image = toolkit.getImage("icons/app_icon.png"); 

            PopupMenu popup = new PopupMenu();

            MenuItem itemAbrir = new MenuItem("Abrir Lazy Deck");
            itemAbrir.addActionListener(e -> mostrarJanela());

            MenuItem itemSair = new MenuItem("Sair / Fechar");
            itemSair.addActionListener(e -> encerrarAplicacao());

            popup.add(itemAbrir);
            popup.addSeparator();
            popup.add(itemSair);

            TrayIcon trayIcon = new TrayIcon(image, "Lazy Deck", popup);
            trayIcon.setImageAutoSize(true);
            
            trayIcon.addActionListener(e -> mostrarJanela());

            tray.add(trayIcon);

        } catch (AWTException e) {
            System.out.println("Erro ao adicionar ícone na bandeja.");
        }
    }

    private void mostrarJanela() {
        Platform.runLater(() -> {
            if (primaryStage.isIconified()) {
                primaryStage.setIconified(false);
            }
            primaryStage.show();
            primaryStage.toFront();
        });
    }

    private void encerrarAplicacao() {
        SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[0]);
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}