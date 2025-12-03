import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class MainApp extends Application {

  public static CookieManager globalCookieManager;
  private Stage primaryStage;
  private static TelaDashboard dashboardRef;
  private static TrayIcon trayIcon;

  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println(">>> [SHUTDOWN HOOK] Detectado encerramento da JVM. Faxina iniciada...");
      new MotorSteam().encerrarSessao();
    }));

    Platform.setImplicitExit(false);

    MotorSteam.carregarBibliotecasGlobais();

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

    dashboardRef = new TelaDashboard(stage);
    dashboardRef.mostrar();
  }

  private void configurarSystemTray() {
    if (!SystemTray.isSupported()) return;

    try {
      SystemTray tray = SystemTray.getSystemTray();
      Toolkit toolkit = Toolkit.getDefaultToolkit();

      java.awt.Image image = null;
      try {
        image = toolkit.getImage("icons/app_icon.png");
      } catch (Exception e) {}

      if (image == null) {
        image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
      }

      trayIcon = new TrayIcon(image, "Lazy Deck - Aguardando...");
      trayIcon.setImageAutoSize(true);

      trayIcon.addActionListener(e -> mostrarJanela());

      trayIcon.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            Platform.runLater(() -> mostrarMenuTray(e.getX(), e.getY()));
          }
        }
      });

      tray.add(trayIcon);

    } catch (AWTException e) {
      System.out.println("Erro ao adicionar ícone na bandeja.");
    }
  }

  private void mostrarMenuTray(int mouseXRaw, int mouseYRaw) {
    Stage menuStage = new Stage();
    menuStage.initStyle(StageStyle.TRANSPARENT);
    menuStage.setAlwaysOnTop(true);

    VBox root = new VBox(4);
    root.getStyleClass().add("tray-menu-container");
    root.setStyle("-fx-background-color: #161b22; -fx-padding: 6; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 4);");

    Button btnIniciar = criarItemMenu("Iniciar / Interromper");
    btnIniciar.setOnAction(e -> {
      mostrarJanela();
      if (dashboardRef != null) dashboardRef.alternarEstadoFarm(false);
      menuStage.close();
    });

    Button btnAbrir = criarItemMenu("Abrir Lazy Deck");
    btnAbrir.setOnAction(e -> {
      mostrarJanela();
      menuStage.close();
    });

    Region sep = new Region();
    sep.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-min-height: 1; -fx-max-height: 1; -fx-margin: 4 0;");

    Button btnSair = criarItemMenu("Sair / Fechar");
    String styleSairNormal = "-fx-background-color: transparent; -fx-text-fill: #ff5f5c; -fx-alignment: center-left; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 6;";
    String styleSairHover = "-fx-background-color: rgba(218, 54, 51, 0.2); -fx-text-fill: #ff5f5c; -fx-alignment: center-left; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 6;";
    btnSair.setStyle(styleSairNormal);
    btnSair.setOnMouseEntered(ev -> btnSair.setStyle(styleSairHover));
    btnSair.setOnMouseExited(ev -> btnSair.setStyle(styleSairNormal));
    btnSair.setOnAction(e -> {
      menuStage.close();
      encerrarAplicacao();
    });

    root.getChildren().addAll(btnIniciar, btnAbrir, sep, btnSair);

    Scene scene = new Scene(root);
    scene.setFill(Color.TRANSPARENT);
    try {
      scene.getStylesheets().add("file:style.css");
    } catch (Exception ex) {}
    menuStage.setScene(scene);

    Screen screen = Screen.getPrimary();
    double scaleX = screen.getOutputScaleX();
    double scaleY = screen.getOutputScaleY();

    double finalX = mouseXRaw / scaleX;
    double finalY = mouseYRaw / scaleY;

    double menuWidth = 180;
    double menuHeight = 150;

    Rectangle2D bounds = screen.getVisualBounds();

    if (finalX + menuWidth > bounds.getMaxX()) {
      finalX = (mouseXRaw / scaleX) - menuWidth;
    }

    if (finalY + menuHeight > bounds.getMaxY()) {
      finalY = (mouseYRaw / scaleY) - menuHeight;
    }

    menuStage.setX(finalX);
    menuStage.setY(finalY);

    menuStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) menuStage.close();
    });

    menuStage.show();
    menuStage.requestFocus();
  }

  private Button criarItemMenu(String texto) {
    Button btn = new Button(texto);
    btn.setMaxWidth(Double.MAX_VALUE);
    String baseStyle = "-fx-background-color: transparent; -fx-text-fill: #c9d1d9; -fx-alignment: center-left; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 6;";
    String hoverStyle = "-fx-background-color: #00efff; -fx-text-fill: #0d1117; -fx-alignment: center-left; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 6;";
    btn.setStyle(baseStyle);
    btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
    btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    return btn;
  }

  public static void atualizarTooltipTray(String texto) {
    if (trayIcon != null) {
      trayIcon.setToolTip(texto);
    }
  }

  private void mostrarJanela() {
    Platform.runLater(() -> {
      if (primaryStage.isIconified()) {
        primaryStage.setIconified(false);
      }
      if (!primaryStage.isShowing()) {
        primaryStage.show();
      }
      primaryStage.toFront();
      primaryStage.requestFocus();
    });
  }

  private void encerrarAplicacao() {
    new MotorSteam().encerrarSessao();
    if (SystemTray.isSupported() && trayIcon != null) {
      SystemTray.getSystemTray().remove(trayIcon);
    }
    Platform.exit();
    System.exit(0);
  }

  public static void main(String[] args) {
    launch(args);
  }
}