import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelaFarmingMulti {

    private Stage stage;
    private StackPane rootStack;
    private VBox listaContainer;
    private Button btnCancelar;

    private List<JogoFila> filaJogos;
    private MotorSteam motor;
    private Thread threadFarming;
    private AtomicBoolean rodando = new AtomicBoolean(false);
    private Runnable onVoltarCallback; 

    private static final String COR_VERDE_SUAVE = "#57cb64"; 
    private static final String COR_AMARELO = "#e3c800";
    private static final String COR_TEXTO_SECUNDARIO = "#8b949e";
    private static final String COR_FUNDO_MODAL = "#0d1117";
    
    private static final String SVG_DECK_FALLBACK = "M21.47 4.35l-1.34-2.4c-0.39-0.69-1.26-0.94-1.96-0.55L7.5 7.5c-0.7 0.39-0.95 1.26-0.56 1.96l1.34 2.4c0.39 0.69 1.26 0.94 1.96 0.55l10.67-6.1c0.7-0.39 0.95-1.26 0.56-1.96zM18.29 18.55l-9.17 5.09c-0.69 0.39-1.56 0.14-1.95-0.56L1.73 13.3c-0.39-0.69-0.14-1.56 0.56-1.95l9.17-5.09c0.69-0.39 1.56-0.14 1.95 0.56l5.44 9.79c0.39 0.69 0.14 1.56-0.56 1.95z";

    public static class JogoFila {
        int appId;
        String nome;
        int dropsIniciais;
        int dropsAtuais;
        
        HBox container;
        Label lblStatus;
        Label lblCartas;
        Circle statusDot;
        Region iconDeck; 
        
        public JogoFila(int appId, String nome, int drops) {
            this.appId = appId;
            this.nome = nome;
            this.dropsIniciais = drops;
            this.dropsAtuais = drops;
        }
    }

    public TelaFarmingMulti(Stage stage, List<Node> cardsDoDashboard, Runnable onVoltar) {
        this.stage = stage;
        this.onVoltarCallback = onVoltar;
        this.motor = new MotorSteam();
        this.filaJogos = converterCardsParaFila(cardsDoDashboard);
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

    private List<JogoFila> converterCardsParaFila(List<Node> cards) {
        List<JogoFila> lista = new ArrayList<>();
        for (Node node : cards) {
            Object data = node.getUserData();
            if (data != null) {
                try {
                    Class<?> clazz = data.getClass();
                    Field fAppId = clazz.getField("appId");
                    Field fNome = clazz.getField("nome");
                    Field fDrops = clazz.getField("drops");
                    
                    int appId = fAppId.getInt(data);
                    String nome = (String) fNome.get(data);
                    int drops = fDrops.getInt(data);
                    
                    if (drops > 0) {
                        lista.add(new JogoFila(appId, nome, drops));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
        return lista;
    }

    public void mostrar(StackPane rootDoDashboard) {
        this.rootStack = rootDoDashboard;
        
        if (filaJogos.isEmpty()) return;

        Node conteudoFundo = rootDoDashboard.getChildren().get(0); 
        conteudoFundo.setEffect(new BoxBlur(20, 20, 3));

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); 
        
        VBox painel = new VBox(20);
        painel.setMaxWidth(650);
        painel.setMaxHeight(680);
        painel.setPadding(new Insets(40));
        painel.setAlignment(Pos.TOP_CENTER);
        
        painel.setStyle("-fx-background-color: #161b22; " +
                        "-fx-background-radius: 24; " +
                        "-fx-border-color: rgba(255,255,255,0.08); " +
                        "-fx-border-radius: 24; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 80, 0, 0, 20);");

        Label lblTitulo = new Label("Fila de Execução");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 24px; -fx-font-family: 'Segoe UI';");

        listaContainer = new VBox(12); 
        listaContainer.setPadding(new Insets(5));
        
        for (JogoFila jogo : filaJogos) {
            HBox item = criarItemVisual(jogo);
            jogo.container = item; 
            listaContainer.getChildren().add(item);
        }

        ScrollPane scroll = new ScrollPane(listaContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS); 

        btnCancelar = new Button("INTERROMPER");
        btnCancelar.setStyle("-fx-background-color: rgba(218, 54, 51, 0.15); -fx-text-fill: #ff5f5c; -fx-font-weight: 800; -fx-font-size: 13px; -fx-padding: 14 40; -fx-cursor: hand; -fx-background-radius: 12; -fx-border-color: rgba(218, 54, 51, 0.3); -fx-border-radius: 12;");
        
        btnCancelar.setOnMouseEntered(e -> btnCancelar.setStyle("-fx-background-color: #da3633; -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 13px; -fx-padding: 14 40; -fx-cursor: hand; -fx-background-radius: 12; -fx-border-color: #da3633; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(218, 54, 51, 0.4), 15, 0, 0, 0);"));
        
        btnCancelar.setOnMouseExited(e -> btnCancelar.setStyle("-fx-background-color: rgba(218, 54, 51, 0.15); -fx-text-fill: #ff5f5c; -fx-font-weight: 800; -fx-font-size: 13px; -fx-padding: 14 40; -fx-cursor: hand; -fx-background-radius: 12; -fx-border-color: rgba(218, 54, 51, 0.3); -fx-border-radius: 12;"));

        btnCancelar.setOnAction(e -> pararEVoltar(overlay));

        painel.getChildren().addAll(lblTitulo, scroll, btnCancelar);
        overlay.getChildren().add(painel);
        
        rootStack.getChildren().add(overlay);
        
        iniciarCicloDeFarm();
    }

    private HBox criarItemVisual(JogoFila jogo) {
        HBox row = new HBox(20); 
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12; -fx-border-color: transparent; -fx-border-radius: 12; -fx-border-width: 1;");

        String urlHeader = "https://cdn.akamai.steamstatic.com/steam/apps/" + jogo.appId + "/header.jpg";
        ImageView imgHeader = new ImageView(new Image(urlHeader, true));
        imgHeader.setFitWidth(180); 
        imgHeader.setPreserveRatio(true);
        Rectangle clip = new Rectangle(180, 84); 
        clip.setArcWidth(8); clip.setArcHeight(8);
        imgHeader.setClip(clip);

        VBox infoCol = new VBox(8);
        infoCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoCol, Priority.ALWAYS);

        Label lblNome = new Label(jogo.nome);
        lblNome.setStyle("-fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 14px;");

        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        
        jogo.statusDot = new Circle(3.5);
        jogo.statusDot.setFill(Color.web("#555"));
        jogo.statusDot.setEffect(null);

        jogo.lblStatus = new Label("Aguardando na fila");
        jogo.lblStatus.setStyle("-fx-text-fill: " + COR_TEXTO_SECUNDARIO + "; -fx-font-size: 12px; -fx-font-weight: 500;");

        statusRow.getChildren().addAll(jogo.statusDot, jogo.lblStatus);

        HBox dropsRow = new HBox(8);
        dropsRow.setAlignment(Pos.CENTER_LEFT);
        
        jogo.iconDeck = new Region();
        jogo.iconDeck.setPrefSize(16, 16);
        
        String svgContent = lerSvgArquivo("deck.svg");
        jogo.iconDeck.setStyle("-fx-shape: \"" + svgContent + "\"; -fx-background-color: #66c0f4; -fx-scale-shape: true;");
        
        jogo.lblCartas = new Label(jogo.dropsAtuais + " drops restantes");
        jogo.lblCartas.setStyle("-fx-text-fill: #66c0f4; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        dropsRow.getChildren().addAll(jogo.iconDeck, jogo.lblCartas);

        infoCol.getChildren().addAll(lblNome, statusRow, dropsRow);
        
        row.getChildren().addAll(imgHeader, infoCol);
        return row;
    }

    private void iniciarCicloDeFarm() {
        rodando.set(true);
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int totalJogos = filaJogos.size();
                
                for (int i = 0; i < totalJogos; i++) {
                    if (!rodando.get()) break;
                    
                    JogoFila jogoAtual = filaJogos.get(i);
                    
                    while (jogoAtual.dropsAtuais > 0 && rodando.get()) {
                        
                        int dropsObtidos = jogoAtual.dropsIniciais - jogoAtual.dropsAtuais;
                        boolean metadeObtido = dropsObtidos >= (jogoAtual.dropsIniciais / 2.0);
                        int minutosEspera = metadeObtido ? 5 : 15; 
                        long millisEspera = minutosEspera * 60 * 1000;

                        atualizarStatusJogo(jogoAtual, true, "Executando (Próxima verificação em " + minutosEspera + " min)");

                        if (motor.iniciarSessaoDeJogo(jogoAtual.appId)) {
                            long fimDoTimer = System.currentTimeMillis() + millisEspera;
                            while (System.currentTimeMillis() < fimDoTimer && rodando.get()) {
                                long faltante = fimDoTimer - System.currentTimeMillis();
                                long min = (faltante / 1000) / 60;
                                long seg = (faltante / 1000) % 60;
                                atualizarTextoStatus(jogoAtual, String.format("Executando... Verificação em %02d:%02d", min, seg));
                                Thread.sleep(1000);
                            }
                            motor.encerrarSessao();
                        } else {
                            atualizarStatusJogo(jogoAtual, false, "Erro ao iniciar. Pulando...");
                            Thread.sleep(3000);
                            break; 
                        }

                        if (!rodando.get()) break;

                        atualizarTextoStatus(jogoAtual, "Sincronizando inventário...");
                        int novosDrops = checarDropsWeb(jogoAtual.appId);
                        
                        if (novosDrops < jogoAtual.dropsAtuais) {
                            jogoAtual.dropsAtuais = novosDrops;
                            Platform.runLater(() -> jogoAtual.lblCartas.setText(novosDrops + " drops restantes"));
                        }
                        
                        if (novosDrops == 0 || novosDrops > 90) { 
                            atualizarStatusJogo(jogoAtual, false, "Concluído");
                            break; 
                        }
                    }
                    if (rodando.get()) atualizarStatusJogo(jogoAtual, false, "Concluído");
                }
                
                Platform.runLater(() -> {
                    btnCancelar.setText("VOLTAR AO DASHBOARD");
                    btnCancelar.setStyle("-fx-background-color: #2da44e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14 40; -fx-cursor: hand; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(45, 164, 78, 0.4), 15, 0, 0, 0);");
                });
                return null;
            }
        };

        threadFarming = new Thread(task);
        threadFarming.setDaemon(true);
        threadFarming.start();
    }

    private void atualizarStatusJogo(JogoFila jogo, boolean ativo, String texto) {
        Platform.runLater(() -> {
            jogo.lblStatus.setText(texto);
            
            if (ativo) {
                jogo.lblStatus.setStyle("-fx-text-fill: " + COR_VERDE_SUAVE + "; -fx-font-weight: bold; -fx-font-size: 12px;");
                
                jogo.statusDot.setFill(Color.web(COR_VERDE_SUAVE));
                jogo.statusDot.setEffect(new DropShadow(5, Color.web(COR_VERDE_SUAVE))); 
                String svgContent = lerSvgArquivo("deck.svg");
                jogo.iconDeck.setStyle("-fx-shape: \"" + svgContent + "\"; -fx-background-color: " + COR_VERDE_SUAVE + "; -fx-scale-shape: true;");
                jogo.lblCartas.setStyle("-fx-text-fill: " + COR_VERDE_SUAVE + "; -fx-font-size: 11px; -fx-font-weight: bold;");

                jogo.container.setStyle(
                    "-fx-background-color: rgba(87, 203, 100, 0.05); " + 
                    "-fx-background-radius: 10; " +
                    "-fx-border-color: rgba(87, 203, 100, 0.2); " + 
                    "-fx-border-radius: 10; " +
                    "-fx-border-width: 1;"
                );
            } else {
                if (texto.equals("Concluído")) {
                    jogo.lblStatus.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
                    jogo.statusDot.setFill(Color.web("#333"));
                    jogo.statusDot.setEffect(null);
                    jogo.container.setOpacity(0.4);
                } else {
                    jogo.lblStatus.setStyle("-fx-text-fill: " + COR_AMARELO + "; -fx-font-size: 12px;");
                    jogo.statusDot.setFill(Color.web(COR_AMARELO));
                    jogo.statusDot.setEffect(null);
                }
                
                String svgContent = lerSvgArquivo("deck.svg");
                jogo.iconDeck.setStyle("-fx-shape: \"" + svgContent + "\"; -fx-background-color: " + COR_TEXTO_SECUNDARIO + "; -fx-scale-shape: true;");
                jogo.lblCartas.setStyle("-fx-text-fill: " + COR_TEXTO_SECUNDARIO + "; -fx-font-size: 11px; -fx-font-weight: bold;");
                
                jogo.container.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 10; -fx-border-color: transparent;");
            }
        });
    }

    private void atualizarTextoStatus(JogoFila jogo, String texto) {
        Platform.runLater(() -> jogo.lblStatus.setText(texto));
    }

    private void atualizarProgressoGeral(double progresso, String status) {
    }

    private int checarDropsWeb(int appId) {
        final int[] resultado = { -1 }; 
        final Object lock = new Object(); 

        Platform.runLater(() -> {
            WebEngine engine = NavegadorGlobal.getEngine();
            
            ChangeListener<Worker.State> listener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
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

                        Object res = engine.executeScript(script);
                        String resStr = (res != null) ? res.toString() : "";
                        
                        engine.getLoadWorker().stateProperty().removeListener(this);

                        synchronized (lock) {
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
                try { Thread.sleep(10000); } catch (Exception e) {}
                synchronized (lock) { lock.notify(); } 
            }).start();
        });

        synchronized (lock) {
            try { lock.wait(); } catch (InterruptedException e) {}
        }
        
        return (resultado[0] == -1) ? 99 : resultado[0];
    }

    private void pararEVoltar(StackPane overlay) {
        rodando.set(false);
        motor.encerrarSessao();
        if (threadFarming != null) threadFarming.interrupt();
        rootStack.getChildren().remove(overlay);
        rootStack.getChildren().get(0).setEffect(null);
        if (onVoltarCallback != null) onVoltarCallback.run();
    }
}