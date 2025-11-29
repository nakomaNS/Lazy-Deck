import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelaFarming {

    private Stage stage;
    private StackPane rootStack;
    private int appId;
    private String nomeJogo;
    private int cartasRestantes;
    
    private MotorSteam motor;
    private boolean rodando = false;
    private Timer timer;
    private long tempoInicio;

    private int contadorSegundos = 0;
    private int proximaChecagem = 900; 

    private Label lblStatus;
    private Label lblTimer;
    private Label lblCartas;
    private Circle statusDot;
    private Region iconDeck;
    private StackPane overlay; 

    private static final String COR_VERDE_NEON = "#4cff00";
    private static final String COR_AMARELO = "#e3c800";
    private static final String COR_TEXTO_SECUNDARIO = "#8b949e";
    
    private static final String SVG_DECK_FALLBACK = "M2 2h20v20H2z"; 

    public TelaFarming(Stage stage, int appId, String nomeJogo, int cartasRestantes) {
        this.stage = stage;
        this.appId = appId;
        this.nomeJogo = nomeJogo;
        this.cartasRestantes = cartasRestantes;
        this.motor = new MotorSteam();
        ajustarIntervaloChecagem();
    }

    private void ajustarIntervaloChecagem() {
        if (cartasRestantes <= 2) {
            proximaChecagem = 300; 
        } else {
            proximaChecagem = 900;
        }
    }
    
    private String lerSvgArquivo(String nomeArquivo) {
        try {
            String caminho = "icons/" + nomeArquivo;
            if (Files.exists(Paths.get(caminho))) {
                String conteudo = Files.readString(Paths.get(caminho));
                Pattern pattern = Pattern.compile("d=\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(conteudo);
                if (matcher.find()) return matcher.group(1);
                return conteudo;
            }
        } catch (Exception e) {}
        return SVG_DECK_FALLBACK;
    }

    public void mostrar(StackPane rootDashboard) {
        this.rootStack = rootDashboard;

        Node conteudoFundo = rootStack.getChildren().get(0);
        conteudoFundo.setEffect(new BoxBlur(20, 20, 3));

        overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        VBox painel = new VBox(25);
        painel.setMaxWidth(450);
        painel.setMaxHeight(600);
        painel.setPadding(new Insets(40));
        painel.setAlignment(Pos.TOP_CENTER);
        
        painel.setStyle("-fx-background-color: #161b22; " +
                        "-fx-background-radius: 24; " +
                        "-fx-border-color: rgba(255,255,255,0.08); " +
                        "-fx-border-radius: 24; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 60, 0, 0, 15);");


        String urlImagem = "https://cdn.akamai.steamstatic.com/steam/apps/" + appId + "/library_600x900.jpg";
        ImageView imgCapa = new ImageView(new Image(urlImagem, true));
        imgCapa.setFitHeight(280); 
        imgCapa.setPreserveRatio(true);
        
        Rectangle clip = new Rectangle(186, 280);
        clip.setArcWidth(16); clip.setArcHeight(16);
        imgCapa.setClip(clip);
        
        StackPane imgContainer = new StackPane(imgCapa);
        imgContainer.setEffect(new DropShadow(30, Color.rgb(0,0,0,0.5)));

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

        lblTimer = new Label("--:--");
        lblTimer.setStyle("-fx-text-fill: " + COR_TEXTO_SECUNDARIO + "; -fx-font-size: 12px;");

        HBox cartasBox = new HBox(8);
        cartasBox.setAlignment(Pos.CENTER);
        cartasBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-padding: 8 16; -fx-background-radius: 20;");
        cartasBox.setMaxWidth(Region.USE_PREF_SIZE);

        iconDeck = new Region();
        iconDeck.setPrefSize(16, 16);
        String svgContent = lerSvgArquivo("deck.svg");
        iconDeck.setStyle("-fx-shape: \"" + svgContent + "\"; -fx-background-color: #66c0f4; -fx-scale-shape: true;");

        lblCartas = new Label(cartasRestantes + " cartas restantes");
        lblCartas.setStyle("-fx-text-fill: #66c0f4; -fx-font-weight: bold; -fx-font-size: 13px;");

        cartasBox.getChildren().addAll(iconDeck, lblCartas);

        Button btnParar = new Button("INTERROMPER");
        btnParar.setStyle("-fx-background-color: rgba(218, 54, 51, 0.15); -fx-text-fill: #ff5f5c; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 12 35; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: rgba(218, 54, 51, 0.3); -fx-border-radius: 10;");
        
        btnParar.setOnMouseEntered(e -> btnParar.setStyle("-fx-background-color: #da3633; -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 12 35; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: #da3633; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(218, 54, 51, 0.4), 15, 0, 0, 0);"));
        btnParar.setOnMouseExited(e -> btnParar.setStyle("-fx-background-color: rgba(218, 54, 51, 0.15); -fx-text-fill: #ff5f5c; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 12 35; -fx-cursor: hand; -fx-background-radius: 10; -fx-border-color: rgba(218, 54, 51, 0.3); -fx-border-radius: 10;"));
        
        btnParar.setOnAction(e -> pararFarm());

        painel.getChildren().addAll(imgContainer, lblTitulo, statusBox, lblTimer, cartasBox, new Region(), btnParar);
        
        overlay.getChildren().add(painel);
        rootStack.getChildren().add(overlay);
        
        iniciarMotor();
    }

    private void iniciarMotor() {
        new Thread(() -> {
            if (motor.iniciarSessaoDeJogo(appId)) {
                rodando = true;
                tempoInicio = System.currentTimeMillis();
                
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (!rodando) return;

                        long diff = System.currentTimeMillis() - tempoInicio;
                        long s = (diff / 1000) % 60;
                        long m = (diff / (1000 * 60)) % 60;
                        long h = (diff / (1000 * 60 * 60));
                        
                        contadorSegundos++;
                        int segundosFaltantes = proximaChecagem - contadorSegundos;
                        
                        if (contadorSegundos >= proximaChecagem) {
                            contadorSegundos = 0;
                            Platform.runLater(() -> {
                                lblStatus.setText("Verificando drops...");
                                lblStatus.setStyle("-fx-text-fill: #66c0f4; -fx-font-size: 13px; -fx-font-weight: bold;");
                                statusDot.setFill(Color.web("#66c0f4"));
                                verificarDropsNaSteam();
                            });
                        }

                        Platform.runLater(() -> {
                            lblTimer.setText(String.format("%02d:%02d:%02d (Próx. check em %ds)", h, m, s, segundosFaltantes));
                            MotorSteam.processarCallbacks();
                        });
                    }
                }, 1000, 1000);
                
                Platform.runLater(() -> {
                   lblStatus.setText("Executando jogo"); 
                });
                
            } else {
                Platform.runLater(() -> {
                    lblStatus.setText("Erro ao iniciar (Steam não detectada)");
                    lblStatus.setStyle("-fx-text-fill: " + COR_AMARELO + ";");
                    statusDot.setFill(Color.web(COR_AMARELO));
                });
            }
        }).start();
    }

    private void verificarDropsNaSteam() {
        WebEngine engine = NavegadorGlobal.getEngine();
        
        engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && engine.getLocation().contains("/badges")) {
                
                String script = 
                    "(function() {" +
                    "   var rows = document.getElementsByClassName('badge_row');" +
                    "   for (var i = 0; i < rows.length; i++) {" +
                    "       var row = rows[i];" +
                    "       var link = row.querySelector('.badge_row_overlay');" +
                    "       if (link && link.href.includes('" + appId + "')) {" +
                    "           var dropSpan = row.querySelector('.progress_info_bold');" +
                    "           if (dropSpan) return dropSpan.innerText.trim();" +
                    "           else return 'ZERO';" + 
                    "       }" +
                    "   }" +
                    "   return 'NAO_ENCONTRADO';" +
                    "})()";

                Object resultado = engine.executeScript(script);
                String resStr = (resultado != null) ? resultado.toString() : "";

                Platform.runLater(() -> {
                    if (resStr.equals("ZERO") || resStr.contains("sem cartas")) {
                        cartasRestantes = 0;
                        lblCartas.setText("0 cartas restantes");
                        lblStatus.setText("Farm Concluído!");
                        lblStatus.setStyle("-fx-text-fill: " + COR_VERDE_NEON + ";");
                        statusDot.setFill(Color.web(COR_VERDE_NEON));
                        pararFarm();
                    } else if (!resStr.equals("NAO_ENCONTRADO") && !resStr.isEmpty()) {
                        String num = resStr.replaceAll("[^0-9]", "");
                        if (!num.isEmpty()) {
                            cartasRestantes = Integer.parseInt(num);
                            lblCartas.setText(cartasRestantes + " cartas restantes");
                            ajustarIntervaloChecagem();
                            
                            lblStatus.setText("Executando jogo");
                            lblStatus.setStyle("-fx-text-fill: " + COR_VERDE_NEON + "; -fx-font-size: 13px; -fx-font-weight: bold;");
                            statusDot.setFill(Color.web(COR_VERDE_NEON));
                        }
                    }
                });
            }
        });

        engine.load("https://steamcommunity.com/my/badges");
    }

    private void pararFarm() {
        if (rodando) {
            rodando = false;
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
            motor.encerrarSessao();
        }
        
        NavegadorGlobal.limparListeners(); 
        
        rootStack.getChildren().remove(overlay);
        rootStack.getChildren().get(0).setEffect(null);
    }
}