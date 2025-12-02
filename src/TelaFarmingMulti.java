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
    private TelaDashboard parentDashboard; 
    private VBox listaContainer;
    private Button btnCancelar;
    private Label lblResumo;
    private List<JogoFila> filaJogos;
    private List<MotorSteam> motoresAtivos = new ArrayList<>();
    private Thread threadFarming;
    private AtomicBoolean rodando = new AtomicBoolean(false);
    private Runnable onVoltarCallback;
    private Runnable onCloseCallback;
    private boolean modoTurbo = false; 
    private static final String COR_VERDE_SUAVE = "#57cb64"; 
    private static final String COR_AMARELO = "#e3c800";
    private static final String COR_TEXTO_SECUNDARIO = "#8b949e";
    private static final String SVG_DECK_FALLBACK = "M21.47 4.35l-1.34-2.4c-0.39-0.69-1.26-0.94-1.96-0.55L7.5 7.5c-0.7 0.39-0.95 1.26-0.56 1.96l1.34 2.4c0.39 0.69 1.26 0.94 1.96 0.55l10.67-6.1c0.7-0.39 0.95-1.26 0.56-1.96zM18.29 18.55l-9.17 5.09c-0.69 0.39-1.56 0.14-1.95-0.56L1.73 13.3c-0.39-0.69-0.14-1.56 0.56-1.95l9.17-5.09c0.69-0.39 1.56-0.14 1.95 0.56l5.44 9.79c0.39 0.69 0.14 1.56-0.56 1.95z";
    private static final String SVG_MINIMIZE = "M5 11h14v2H5z";
    private static final String SVG_EDIT = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";

    public static class JogoFila {
        int appId;
        String nome;
        int dropsIniciais;
        int dropsAtuais;
        long tempoCicloSegundos;
        
        HBox container;
        Label lblStatus;
        Label lblCartas;
        Label lblTimer;
        Circle statusDot;
        Region iconDeck; 
        
        public JogoFila(int appId, String nome, int drops) {
            this.appId = appId;
            this.nome = nome;
            this.dropsIniciais = drops;
            this.dropsAtuais = drops;
            int minutos = (drops <= 2) ? GerenciadorConfig.getTempoMin() : GerenciadorConfig.getTempoMax();
            this.tempoCicloSegundos = minutos * 60;
        }
    }

    public TelaFarmingMulti(Stage stage, List<Node> cardsDoDashboard, Runnable onVoltar, TelaDashboard parent, boolean modoTurbo) {
        this.stage = stage;
        this.onVoltarCallback = onVoltar;
        this.parentDashboard = parent;
        this.modoTurbo = modoTurbo;
        this.filaJogos = converterCardsParaFila(cardsDoDashboard);
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public void pararExterno() {
        pararEVoltar(null); 
    }
    
    private String lerSvgArquivo(String nomeArquivo) {
        try { String caminho = "icons/" + nomeArquivo;
            if (Files.exists(Paths.get(caminho))) {
                String conteudo = Files.readString(Paths.get(caminho));
                Pattern pattern = Pattern.compile("d=\"([^\"]+)\""); Matcher matcher = pattern.matcher(conteudo);
                if (matcher.find()) return matcher.group(1); return conteudo; }
        } catch (Exception e) {} return SVG_DECK_FALLBACK;
    }

    private List<JogoFila> converterCardsParaFila(List<Node> cards) {
        List<JogoFila> lista = new ArrayList<>();
        for (Node node : cards) { Object data = node.getUserData(); if (data != null) { try {
                    Class<?> clazz = data.getClass(); Field fAppId = clazz.getField("appId"); Field fNome = clazz.getField("nome"); Field fDrops = clazz.getField("drops");
                    int appId = fAppId.getInt(data); String nome = (String) fNome.get(data); int drops = fDrops.getInt(data);
                    if (drops > 0) { lista.add(new JogoFila(appId, nome, drops)); }
                } catch (Exception e) { e.printStackTrace(); } } } return lista;
    }

    public void mostrar(StackPane rootDoDashboard) {
        this.rootStack = rootDoDashboard;
        if (filaJogos.isEmpty()) return;

        Node conteudoFundo = rootDoDashboard.getChildren().get(0); 
        conteudoFundo.setEffect(new BoxBlur(20, 20, 3));

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);"); 
        
        VBox painel = new VBox(15);
        painel.setMaxWidth(650);
        painel.setMaxHeight(700);
        painel.setPadding(new Insets(20));
        painel.setAlignment(Pos.TOP_CENTER);
        painel.setStyle("-fx-background-color: #161b22; -fx-background-radius: 24; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 80, 0, 0, 20);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnMinimizar = new Button();
        Region iconMin = new Region();
        iconMin.setPrefSize(14, 2); iconMin.setMinSize(14, 2); iconMin.setMaxSize(14, 2);
        iconMin.setStyle("-fx-shape: \"" + SVG_MINIMIZE + "\"; -fx-background-color: rgba(255,255,255,0.7);");
        btnMinimizar.setGraphic(iconMin);
        btnMinimizar.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-cursor: hand;");
        btnMinimizar.setOnAction(e -> {

            int c=0,n=0,t=0;
            if(!filaJogos.isEmpty()) c = filaJogos.get(0).appId;
            if(filaJogos.size()>1) n = filaJogos.get(1).appId;
            if(filaJogos.size()>2) t = filaJogos.get(2).appId;
            parentDashboard.minimizarFarming(overlay, c, n, t);
        });
        header.getChildren().add(btnMinimizar);

        Label lblTitulo = new Label(modoTurbo ? "Fila Turbo (3x)" : "Fila de Execução");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 24px;");
        if(modoTurbo) lblTitulo.setTextFill(Color.web("#ff5f5c"));
        
        lblResumo = new Label(filaJogos.size() + " jogos na fila");
        lblResumo.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 12px; -fx-font-weight: bold;");

        listaContainer = new VBox(12); listaContainer.setPadding(new Insets(5));
        for (JogoFila jogo : filaJogos) { HBox item = criarItemVisual(jogo, overlay); jogo.container = item; listaContainer.getChildren().add(item); }

        ScrollPane scroll = new ScrollPane(listaContainer);
        scroll.setFitToWidth(true); scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS); 

        btnCancelar = new Button("INTERROMPER");
        btnCancelar.setStyle("-fx-background-color: rgba(218, 54, 51, 0.15); -fx-text-fill: #ff5f5c; -fx-font-weight: 800; -fx-font-size: 13px; -fx-padding: 14 40; -fx-cursor: hand; -fx-background-radius: 12;");
        btnCancelar.setOnAction(e -> pararEVoltar(overlay));

        painel.getChildren().addAll(header, lblTitulo, lblResumo, scroll, btnCancelar);
        overlay.getChildren().add(painel);
        rootStack.getChildren().add(overlay);
        
        iniciarCicloDeFarm();
    }

    private HBox criarItemVisual(JogoFila jogo, StackPane parentOverlay) {
        HBox row = new HBox(20); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12;");
        
        String urlHeader = "https://cdn.akamai.steamstatic.com/steam/apps/" + jogo.appId + "/header.jpg";
        ImageView imgHeader = new ImageView(new Image(urlHeader, true)); 
        imgHeader.setFitWidth(180); imgHeader.setPreserveRatio(true);
        Rectangle clip = new Rectangle(180, 84); clip.setArcWidth(8); clip.setArcHeight(8); imgHeader.setClip(clip);
        
        VBox infoCol = new VBox(8); infoCol.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(infoCol, Priority.ALWAYS);
        
        Label lblNome = new Label(jogo.nome); lblNome.setStyle("-fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 14px;");
        
        jogo.lblTimer = new Label("Ciclo: " + formatarTempo(jogo.tempoCicloSegundos));
        jogo.lblTimer.setStyle("-fx-text-fill: #8b949e; -fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        HBox statusRow = new HBox(10); statusRow.setAlignment(Pos.CENTER_LEFT);
        jogo.statusDot = new Circle(3.5); jogo.statusDot.setFill(Color.web("#555"));
        jogo.lblStatus = new Label("Aguardando"); jogo.lblStatus.setStyle("-fx-text-fill: " + COR_TEXTO_SECUNDARIO + "; -fx-font-size: 12px;");
        statusRow.getChildren().addAll(jogo.statusDot, jogo.lblStatus);
        
        jogo.lblCartas = new Label(jogo.dropsAtuais + " drops"); jogo.lblCartas.setStyle("-fx-text-fill: #66c0f4; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        infoCol.getChildren().addAll(lblNome, jogo.lblTimer, statusRow, jogo.lblCartas);
        row.getChildren().addAll(imgHeader, infoCol);
        return row;
    }
    
    private void mostrarModalEditarTempo(JogoFila jogo, StackPane parentOverlay) {}

    private void iniciarCicloDeFarm() {
        rodando.set(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int batchSize = modoTurbo ? 3 : 1;

                while (!filaJogos.isEmpty() && rodando.get()) {
                    
                    List<JogoFila> loteAtual = new ArrayList<>();
                    for (int i = 0; i < batchSize && i < filaJogos.size(); i++) {
                        loteAtual.add(filaJogos.get(i));
                    }
                    
                    Platform.runLater(() -> {
                        int c = loteAtual.size() > 0 ? loteAtual.get(0).appId : 0;
                        int n = loteAtual.size() > 1 ? loteAtual.get(1).appId : 0;
                        int t = loteAtual.size() > 2 ? loteAtual.get(2).appId : 0;
                        parentDashboard.animarTrocaCard(c, n, t);
                        String statusMsg = modoTurbo ? "Farmando 3 jogos simultâneos" : "Farmando: " + loteAtual.get(0).nome;
                        MainApp.atualizarTooltipTray(statusMsg);
                    });

                    motoresAtivos.clear();
                    for (JogoFila jogo : loteAtual) {
                        MotorSteam m = new MotorSteam();
                        motoresAtivos.add(m);
                        
                        atualizarStatusJogo(jogo, true, "Iniciando...");
                        if (!m.iniciarSessaoDeJogo(jogo.appId)) {
                            atualizarStatusJogo(jogo, false, "Erro ao iniciar");
                            jogo.dropsAtuais = 0; 
                        } else {
                            atualizarStatusJogo(jogo, true, "Executando...");
                        }
                        Thread.sleep(500);
                    }

                    long tempoCiclo = loteAtual.stream().mapToLong(j -> j.tempoCicloSegundos).max().orElse(300);
                    long countdown = tempoCiclo;
                    
                    while (countdown > 0 && rodando.get()) {
                        long s = countdown;
                        for (JogoFila j : loteAtual) {
                            if(j.dropsAtuais > 0) {
                                Platform.runLater(() -> j.lblTimer.setText("Check: " + formatarTempo(s)));
                            }
                        }
                        Thread.sleep(1000);
                        countdown--;
                    }

                    if (!rodando.get()) break;

                    for (JogoFila j : loteAtual) atualizarTextoStatus(j, "Verificando...");
                    for (MotorSteam m : motoresAtivos) m.encerrarSessao();
                    motoresAtivos.clear();

                    Thread.sleep(5000);

                    List<JogoFila> concluidos = new ArrayList<>();
                    
                    for (JogoFila jogo : loteAtual) {
                        int novosDrops = checarDropsWeb(jogo.appId);
                        if (novosDrops != 99) {
                            jogo.dropsAtuais = novosDrops;
                            Platform.runLater(() -> jogo.lblCartas.setText(novosDrops + " drops restantes"));
                        }
                        
                        if (jogo.dropsAtuais <= 0) {
                            atualizarStatusJogo(jogo, false, "Concluído");
                            concluidos.add(jogo);
                        } else {
                            atualizarTextoStatus(jogo, "Reiniciando...");
                        }
                    }


                    filaJogos.removeAll(concluidos);
                    
                    
                    Thread.sleep(2000);
                }
                
                Platform.runLater(() -> {
                    btnCancelar.setText("VOLTAR AO DASHBOARD");
                    btnCancelar.setStyle("-fx-background-color: #2da44e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14 40;");
                    MainApp.atualizarTooltipTray("Lazy Deck - Finalizado");
                    if (onCloseCallback != null) onCloseCallback.run();
                });
                return null;
            }
        };
        threadFarming = new Thread(task);
        threadFarming.setDaemon(true);
        threadFarming.start();
    }
    
    private String formatarTempo(long segundos) {
        long min = segundos / 60;
        long sec = segundos % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void atualizarStatusJogo(JogoFila jogo, boolean ativo, String texto) {
        Platform.runLater(() -> {
            jogo.lblStatus.setText(texto);
            if (ativo) {
                jogo.lblStatus.setStyle("-fx-text-fill: " + COR_VERDE_SUAVE + "; -fx-font-weight: bold; -fx-font-size: 12px;");
                jogo.statusDot.setFill(Color.web(COR_VERDE_SUAVE));
            } else {
                jogo.lblStatus.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
                jogo.statusDot.setFill(Color.web("#555"));
                if (texto.equals("Concluído")) jogo.container.setOpacity(0.4);
            }
        });
    }

    private void atualizarTextoStatus(JogoFila jogo, String texto) { Platform.runLater(() -> jogo.lblStatus.setText(texto)); }

    private int checarDropsWeb(int appId) {
        final int[] resultado = { -1 }; final Object lock = new Object(); 
        Platform.runLater(() -> {
            WebEngine engine = NavegadorGlobal.getEngine();
            ChangeListener<Worker.State> listener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
                        String script = "(function() { var rows = document.getElementsByClassName('badge_row'); for (var i = 0; i < rows.length; i++) { var row = rows[i]; var link = row.querySelector('.badge_row_overlay'); if (link && link.href.includes('" + appId + "')) { var dropSpan = row.querySelector('.progress_info_bold'); if (dropSpan) return dropSpan.innerText.trim(); else return 'ZERO'; } } return 'NAO_ENCONTRADO'; })()";
                        Object res = engine.executeScript(script); String resStr = (res != null) ? res.toString() : "";
                        engine.getLoadWorker().stateProperty().removeListener(this);
                        synchronized (lock) { if (resStr.equals("ZERO") || resStr.contains("sem cartas")) { resultado[0] = 0; } else if (resStr.equals("NAO_ENCONTRADO")) { resultado[0] = 99; } else { String num = resStr.replaceAll("[^0-9]", ""); resultado[0] = num.isEmpty() ? 0 : Integer.parseInt(num); } lock.notify(); }
                    }
                }
            };
            engine.getLoadWorker().stateProperty().addListener(listener);
            engine.load("https://steamcommunity.com/my/badges?r=" + System.currentTimeMillis());
            new Thread(() -> { try { Thread.sleep(10000); } catch (Exception e) {} synchronized (lock) { lock.notify(); } }).start();
        });
        synchronized (lock) { try { lock.wait(); } catch (InterruptedException e) {} }
        return (resultado[0] == -1) ? 99 : resultado[0];
    }

    private void pararEVoltar(StackPane overlay) {
        rodando.set(false);
        for (MotorSteam m : motoresAtivos) m.encerrarSessao();
        motoresAtivos.clear();
        
        if (threadFarming != null) threadFarming.interrupt();
        NavegadorGlobal.limparListeners();
        parentDashboard.restaurarFarming(overlay);
        
        if (overlay != null) rootStack.getChildren().remove(overlay);
        rootStack.getChildren().get(0).setEffect(null); 
        
        if (onVoltarCallback != null) onVoltarCallback.run();
        if (onCloseCallback != null) onCloseCallback.run(); 
        MainApp.atualizarTooltipTray("Lazy Deck - Parado");
    }
}