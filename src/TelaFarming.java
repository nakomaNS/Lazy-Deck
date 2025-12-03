import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.concurrent.Worker;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelaFarming {

  private Stage stage;
  private StackPane rootStack;
  private TelaDashboard parentDashboard;
  private int appId;
  private String nomeJogo;
  private int cartasRestantes;
  private MotorSteam motor;
  private AtomicBoolean rodando = new AtomicBoolean(false);
  private Thread threadFarming;
  private long tempoCicloSegundos;
  private Label lblStatus;
  private Label lblTimer;
  private Label lblCartas;
  private Circle statusDot;
  private StackPane overlay;

  private static final String COR_VERDE_NEON = "#4cff00";
  private static final String COR_AMARELO = "#e3c800";
  private static final String COR_TEXTO_SECUNDARIO = "#8b949e";
  private static final String SVG_MINIMIZE = "M5 11h14v2H5z";
  private static final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";

  public TelaFarming(Stage stage, int appId, String nomeJogo, int cartasRestantes, TelaDashboard parent) {
    this.stage = stage;
    this.appId = appId;
    this.nomeJogo = nomeJogo;
    this.cartasRestantes = cartasRestantes;
    this.parentDashboard = parent;
    this.motor = new MotorSteam();

    int minutos = (cartasRestantes <= 2) ? GerenciadorConfig.getTempoMin() : GerenciadorConfig.getTempoMax();
    this.tempoCicloSegundos = minutos * 60;
  }

  public void mostrar(StackPane rootDashboard) {
    this.rootStack = rootDashboard;
    Node conteudoFundo = rootStack.getChildren().get(0);
    conteudoFundo.setEffect(new BoxBlur(20, 20, 3));

    overlay = new StackPane();
    overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

    VBox painel = new VBox(20);
    painel.setMaxWidth(450);
    painel.setMaxHeight(600);
    painel.setPadding(new Insets(20));
    painel.setAlignment(Pos.TOP_CENTER);

    painel.setStyle("-fx-background-color: #161b22; -fx-background-radius: 24; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 60, 0, 0, 15);");

    HBox header = new HBox(10);
    header.setAlignment(Pos.CENTER_RIGHT);

    Button btnMinimizar = new Button();
    Region iconMin = new Region();
    iconMin.setPrefSize(14, 2);
    iconMin.setMinSize(14, 2);
    iconMin.setMaxSize(14, 2);
    iconMin.setStyle("-fx-shape: \"" + SVG_MINIMIZE + "\"; -fx-background-color: rgba(255,255,255,0.7);");
    btnMinimizar.setGraphic(iconMin);
    btnMinimizar.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-cursor: hand;");

    btnMinimizar.setOnAction(e -> parentDashboard.minimizarFarming(overlay, appId, 0, 0));
    header.getChildren().add(btnMinimizar);

    String urlImagem = "https://cdn.akamai.steamstatic.com/steam/apps/" + appId + "/library_600x900.jpg";
    ImageView imgCapa = new ImageView(new Image(urlImagem, true));
    imgCapa.setFitHeight(280);
    imgCapa.setPreserveRatio(true);
    Rectangle clip = new Rectangle(186, 280);
    clip.setArcWidth(16);
    clip.setArcHeight(16);
    imgCapa.setClip(clip);
    StackPane imgContainer = new StackPane(imgCapa);
    imgContainer.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.5)));

    Label lblTitulo = new Label(nomeJogo);
    lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 22px; -fx-font-family: 'Segoe UI';");
    lblTitulo.setWrapText(true);
    lblTitulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

    HBox statusBox = new HBox(10);
    statusBox.setAlignment(Pos.CENTER);
    statusDot = new Circle(4);
    statusDot.setFill(Color.web(COR_VERDE_NEON));
    statusDot.setEffect(new DropShadow(10, Color.web(COR_VERDE_NEON)));
    lblStatus = new Label("Iniciando...");
    lblStatus.setStyle("-fx-text-fill: " + COR_VERDE_NEON + "; -fx-font-size: 13px; -fx-font-weight: bold;");
    statusBox.getChildren().addAll(statusDot, lblStatus);

    HBox timerBox = new HBox(5);
    timerBox.setAlignment(Pos.CENTER);
    lblTimer = new Label("Ciclo: --:--");
    lblTimer.setStyle("-fx-text-fill: " + COR_TEXTO_SECUNDARIO + "; -fx-font-size: 12px; -fx-font-family: 'Consolas', monospace; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 2 6; -fx-background-radius: 4;");

    Button btnEdit = new Button();
    Region iconEdit = new Region();
    double size = 11;
    iconEdit.setPrefSize(size, size);
    iconEdit.setMinSize(size, size);
    iconEdit.setMaxSize(size, size);
    iconEdit.setStyle("-fx-shape: \"" + SVG_EDIT + "\"; -fx-background-color: #8b949e; -fx-scale-shape: true;");
    btnEdit.setGraphic(iconEdit);
    btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2 4;");
    btnEdit.setOnAction(e -> mostrarModalEditarTempo(overlay));

    timerBox.getChildren().addAll(lblTimer, btnEdit);

    lblCartas = new Label(cartasRestantes + " cartas restantes");
    lblCartas.setStyle("-fx-text-fill: #66c0f4; -fx-font-weight: bold; -fx-font-size: 13px;");

    Button btnParar = new Button("INTERROMPER");
    btnParar.setStyle("-fx-background-color: rgba(218, 54, 51, 0.15); -fx-text-fill: #ff5f5c; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 12 35; -fx-cursor: hand; -fx-background-radius: 10;");
    btnParar.setOnAction(e -> fechar());

    painel.getChildren().addAll(header, imgContainer, lblTitulo, statusBox, timerBox, lblCartas, new Region(), btnParar);

    overlay.getChildren().add(painel);
    rootStack.getChildren().add(overlay);
    iniciarCicloDeFarm();
  }

  private void mostrarModalEditarTempo(StackPane parentOverlay) {
    StackPane modalOverlay = new StackPane();
    modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

    VBox painelEdit = new VBox(20);
    painelEdit.setMaxWidth(300);
    painelEdit.setStyle("-fx-background-color: #161b22; -fx-background-radius: 16; -fx-padding: 25;");

    Label lblInput = new Label("Minutos por ciclo:");
    lblInput.setStyle("-fx-text-fill: white;");

    TextField txtMinutos = new TextField(String.valueOf(tempoCicloSegundos / 60));
    txtMinutos.setStyle("-fx-background-color: #0d1117; -fx-text-fill: white;");

    Button btnConfirmar = new Button("Confirmar");
    btnConfirmar.setOnAction(e -> {
      try {
        long minutos = Long.parseLong(txtMinutos.getText());
        if (minutos > 0) tempoCicloSegundos = minutos * 60;
      } catch (Exception ex) {}
      parentOverlay.getChildren().remove(modalOverlay);
    });

    painelEdit.getChildren().addAll(lblInput, txtMinutos, btnConfirmar);
    modalOverlay.getChildren().add(painelEdit);
    parentOverlay.getChildren().add(modalOverlay);
  }

  private void iniciarCicloDeFarm() {
    rodando.set(true);
    threadFarming = new Thread(() -> {
      while (cartasRestantes > 0 && rodando.get()) {

        Platform.runLater(() -> {
          lblStatus.setText("Executando jogo");
          lblStatus.setStyle("-fx-text-fill: " + COR_VERDE_NEON + "; -fx-font-weight: bold;");
          statusDot.setFill(Color.web(COR_VERDE_NEON));
        });

        if (motor.iniciarSessaoDeJogo(appId)) {
          long countdown = tempoCicloSegundos;
          while (countdown > 0 && rodando.get()) {
            long s = countdown;
            Platform.runLater(() -> lblTimer.setText("Ciclo: " + formatarTempo(s)));
            try {
              Thread.sleep(1000);
            } catch (Exception e) {}
            countdown--;
          }

          if (!rodando.get()) break;

          Platform.runLater(() -> lblStatus.setText("Verificando..."));
          motor.encerrarSessao();

          try {
            Thread.sleep(5000);
          } catch (Exception e) {}

          int novosDrops = checarDropsWeb();
          if (novosDrops != 99) {
            cartasRestantes = novosDrops;
            Platform.runLater(() -> lblCartas.setText(cartasRestantes + " cartas restantes"));
          }

          if (cartasRestantes <= 0) {
            Platform.runLater(() -> {
              lblStatus.setText("Concluído!");
              lblStatus.setStyle("-fx-text-fill: #66c0f4;");
              statusDot.setFill(Color.web("#66c0f4"));
            });
            break;
          }
        } else {
          Platform.runLater(() -> lblStatus.setText("Erro ao iniciar"));
          try {
            Thread.sleep(3000);
          } catch (Exception e) {}
          break;
        }
      }
    });
    threadFarming.setDaemon(true);
    threadFarming.start();
  }

  private String formatarTempo(long segundos) {
    long min = segundos / 60;
    long sec = segundos % 60;
    return String.format("%02d:%02d", min, sec);
  }

  private int checarDropsWeb() {
    final int[] resultado = {
      -1
    };
    final Object lock = new Object();
    Platform.runLater(() -> {
      WebEngine engine = NavegadorGlobal.getEngine();
      ChangeListener < Worker.State > listener = new ChangeListener < > () {
        @Override
        public void changed(ObservableValue < ? extends Worker.State > ov, Worker.State oldState, Worker.State newState) {
          if (newState == Worker.State.SUCCEEDED) {
            String script = "(function() { var rows = document.getElementsByClassName('badge_row'); for (var i = 0; i < rows.length; i++) { var row = rows[i]; var link = row.querySelector('.badge_row_overlay'); if (link && link.href.includes('" + appId + "')) { var dropSpan = row.querySelector('.progress_info_bold'); if (dropSpan) return dropSpan.innerText.trim(); else return 'ZERO'; } } return 'NAO_ENCONTRADO'; })()";
            Object res = engine.executeScript(script);
            String resStr = (res != null) ? res.toString() : "";
            engine.getLoadWorker().stateProperty().removeListener(this);
            synchronized(lock) {
              if (resStr.equals("ZERO") || resStr.contains("sem cartas")) {
                resultado[0] = 0;
              } else if (resStr.equals("NAO_ENCONTRADO")) {
                resultado[0] = 99;
              } else {
                String num = resStr.replaceAll("[^0-9]", "");
                resultado[0] = num.isEmpty() ? 0 : Integer.parseInt(num);
              }
              lock.notify();
            }
          }
        }
      };
      engine.getLoadWorker().stateProperty().addListener(listener);
      engine.load("https://steamcommunity.com/my/badges?r=" + System.currentTimeMillis());
      new Thread(() -> {
        try {
          Thread.sleep(10000);
        } catch (Exception e) {}
        synchronized(lock) {
          lock.notify();
        }
      }).start();
    });
    synchronized(lock) {
      try {
        lock.wait();
      } catch (InterruptedException e) {}
    }
    return (resultado[0] == -1) ? 99 : resultado[0];
  }

  public void fechar() {
    rodando.set(false);
    motor.encerrarSessao();
    if (threadFarming != null) threadFarming.interrupt();

    NavegadorGlobal.limparListeners();
    parentDashboard.restaurarFarming(overlay);
    rootStack.getChildren().remove(overlay);
    rootStack.getChildren().get(0).setEffect(null);
  }
}