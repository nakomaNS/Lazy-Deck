import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TelaDashboard {

    // --- ÍCONES VETORIAIS ---
    private static final String SVG_DECK_FALLBACK = "M21.47 4.35l-1.34-2.4c-0.39-0.69-1.26-0.94-1.96-0.55L7.5 7.5c-0.7 0.39-0.95 1.26-0.56 1.96l1.34 2.4c0.39 0.69 1.26 0.94 1.96 0.55l10.67-6.1c0.7-0.39 0.95-1.26 0.56-1.96zM18.29 18.55l-9.17 5.09c-0.69 0.39-1.56 0.14-1.95-0.56L1.73 13.3c-0.39-0.69-0.14-1.56 0.56-1.95l9.17-5.09c0.69-0.39 1.56-0.14 1.95 0.56l5.44 9.79c0.39 0.69 0.14 1.56-0.56 1.95z";
    private static final String SVG_LIB_FALLBACK = "M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9H9V9h10v2zm-4 4H9v-2h6v2zm4-8H9V5h10v2z";
    private static final String SVG_FILTER = "M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z";
    private static final String SVG_SEARCH = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";
    
    private static final String SVG_CLOCK = "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z";
    private static final String SVG_GAMEPAD = "M21 6H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h18c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-10 7H8v3H6v-3H3v-2h3V8h2v3h3v2zm4.5 2c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm4-3c-.83 0-1.5-.67-1.5-1.5S18.67 9 19.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z";

    private static final String SVG_EYE_OPEN = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
    private static final String SVG_EYE_SLASH = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

    // --- ÍCONES DO LOGIN ---
    private static final String SVG_STEAM_OFFICIAL = "M18.102 12.129c0-0 0-0 0-0.001 0-1.564 1.268-2.831 2.831-2.831s2.831 1.268 2.831 2.831c0 1.564-1.267 2.831-2.831 2.831-0 0-0 0-0.001 0h0c-0 0-0 0-0.001 0-1.563 0-2.83-1.267-2.83-2.83 0-0 0-0 0-0.001v0zM24.691 12.135c0-2.081-1.687-3.768-3.768-3.768s-3.768 1.687-3.768 3.768c0 2.081 1.687 3.768 3.768 3.768v0c2.080-0.003 3.765-1.688 3.768-3.767v-0zM10.427 23.76l-1.841-0.762c0.524 1.078 1.611 1.808 2.868 1.808 1.317 0 2.448-0.801 2.93-1.943l0.008-0.021c0.155-0.362 0.246-0.784 0.246-1.226 0-1.757-1.424-3.181-3.181-3.181-0.405 0-0.792 0.076-1.148 0.213l0.022-0.007 1.903 0.787c0.852 0.364 1.439 1.196 1.439 2.164 0 1.296-1.051 2.347-2.347 2.347-0.324 0-0.632-0.066-0.913-0.184l0.015 0.006zM15.974 1.004c-7.857 0.001-14.301 6.046-14.938 13.738l-0.004 0.054 8.038 3.322c0.668-0.462 1.495-0.737 2.387-0.737 0.001 0 0.002 0 0.002 0h-0c0.079 0 0.156 0.005 0.235 0.008l3.575-5.176v-0.074c0.003-3.12 2.533-5.648 5.653-5.648 3.122 0 5.653 2.531 5.653 5.653s-2.531 5.653-5.653 5.653h-0.131l-5.094 3.638c0 0.065 0.005 0.131 0.005 0.199 0 0.001 0 0.002 0 0.003 0 2.342-1.899 4.241-4.241 4.241-2.047 0-3.756-1.451-4.153-3.38l-0.005-0.027-5.755-2.383c1.841 6.345 7.601 10.905 14.425 10.905 8.281 0 14.994-6.713 14.994-14.994s-6.713-14.994-14.994-14.994c-0 0-0.001 0-0.001 0h0z";
    private static final String SVG_LOCK_SOLID = "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z";

    private Stage stage;
    private StackPane rootStack; 
    private BorderPane contentLayout; 
    private VBox containerNavegador; 
    private StackPane overlayLogin; 
    private StackPane overlayLoading; 
    private HBox windowControls;
    
    private TilePane gridJogos;
    private VBox emptyState;
    private List<Node> todosOsCards = new ArrayList<>();
    private Set<Integer> blacklist = new HashSet<>();
    
    private Label lblCartasRestantesSidebar;
    private Label lblTempoEstimado;
    private Label lblSaldoCarteira; 
    private Label lblTotalJogos;
    
    private Label lblUserName;
    private Label lblUserLevel;
    private ImageView imgAvatar;
    private Label lblStatus;

    private double xOffset = 0;
    private double yOffset = 0;
    
    private GerenciadorBanco banco;
    
    private ChangeListener<Worker.State> listenerCargaScan;
    private ChangeListener<Worker.State> listenerCargaLogin;
    
    private boolean carregamentoEmAndamento = false;

    public static class DadosCard {
        public int appId;
        public String nome;
        public int drops;
        public DadosCard(int appId, String nome, int drops) {
            this.appId = appId;
            this.nome = nome;
            this.drops = drops;
        }
    }

    public TelaDashboard(Stage stage) {
        this.stage = stage;
        this.banco = new GerenciadorBanco();
        carregarBlacklist();
    }

    public void mostrar() {
        banco.conectar();

        try {
            if (stage.getStyle() != StageStyle.TRANSPARENT) {
                 stage.initStyle(StageStyle.TRANSPARENT);
            }
        } catch (Exception e) {}
        
        stage.setWidth(1080);
        stage.setHeight(720);

        rootStack = new StackPane();
        rootStack.getStylesheets().add("file:style.css");
        rootStack.getStyleClass().add("main-container");

        contentLayout = new BorderPane();
        
        rootStack.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        rootStack.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(260); 
        sidebar.getStyleClass().add("sidebar");
        sidebar.setAlignment(Pos.TOP_CENTER);

        imgAvatar = new ImageView(new Image("https://cdn.akamai.steamstatic.com/steamcommunity/public/images/avatars/fe/fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb_full.jpg"));
        imgAvatar.setFitWidth(100);
        imgAvatar.setFitHeight(100);
        imgAvatar.setSmooth(true);
        Circle clipAvatar = new Circle(50, 50, 50);
        imgAvatar.setClip(clipAvatar);
        imgAvatar.getStyleClass().add("user-avatar");

        lblUserName = new Label("Detectando...");
        lblUserName.getStyleClass().add("user-name");
        lblUserName.setWrapText(true);
        lblUserName.setMaxWidth(220);

        lblUserLevel = new Label("Lv. --");
        lblUserLevel.getStyleClass().add("level-badge");

        lblSaldoCarteira = new Label("Saldo Steam: R$ --");
        lblSaldoCarteira.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        HBox saldoContainer = new HBox(lblSaldoCarteira);
        saldoContainer.setAlignment(Pos.CENTER);
        saldoContainer.setPadding(new Insets(4, 12, 4, 12)); 
        saldoContainer.setMaxWidth(Region.USE_PREF_SIZE);
        saldoContainer.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.08); " + 
            "-fx-background-radius: 20; " + 
            "-fx-border-color: rgba(255, 255, 255, 0.15); " + 
            "-fx-border-radius: 20;"
        );

        VBox perfilBox = new VBox(12); 
        perfilBox.setAlignment(Pos.CENTER);
        perfilBox.getChildren().addAll(lblUserName, saldoContainer, lblUserLevel);

        VBox boxCartas = criarStatBox("CARTAS RESTANTES", "0", null);
        lblCartasRestantesSidebar = (Label) boxCartas.getChildren().get(0);
        lblCartasRestantesSidebar.setStyle("-fx-text-fill: #00efff; -fx-font-size: 20px; -fx-font-weight: 800;");

        VBox boxTempo = criarStatBox("TEMPO ESTIMADO", "--", SVG_CLOCK);
        lblTempoEstimado = (Label) ((HBox)boxTempo.getChildren().get(0)).getChildren().get(1);
        lblTempoEstimado.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 20px; -fx-font-weight: 800;");
        

        VBox boxJogos = criarStatBoxPersonalizado("FILA DE JOGOS", "0", SVG_GAMEPAD, 22, 12);
        lblTotalJogos = (Label) ((HBox)boxJogos.getChildren().get(0)).getChildren().get(1);
        lblTotalJogos.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: 800;");

        Button btnFarmAll = new Button("RESGATAR CARTAS");
        btnFarmAll.getStyleClass().add("button-farm-all");
        btnFarmAll.setMaxWidth(Double.MAX_VALUE);
        btnFarmAll.setOnAction(e -> {
             new TelaFarmingMulti(stage, todosOsCards, this::iniciarScan).mostrar(rootStack);
        });
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(imgAvatar, perfilBox, new Region(), boxCartas, boxTempo, boxJogos, spacer, btnFarmAll);
        contentLayout.setLeft(sidebar);

        VBox centerArea = new VBox(15);
        centerArea.setPadding(new Insets(25, 30, 20, 30));

        HBox topBar = new HBox(20); 
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 120, 0, 0)); 
        
        VBox titleBlock = new VBox(6); 
        titleBlock.setAlignment(Pos.CENTER_LEFT);
        titleBlock.setMinWidth(200);
        
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        String svgLibData = lerSvgArquivo("library.svg", SVG_LIB_FALLBACK);
        Region iconLib = criarIcone(svgLibData, 22, 22, "white");
        
        Label lblTitulo = new Label("MINHA BIBLIOTECA");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 18px;");
        lblTitulo.setMinWidth(Region.USE_PREF_SIZE);
        
        titleRow.getChildren().addAll(iconLib, lblTitulo);
        
        lblStatus = new Label("Carregando biblioteca Steam...");
        lblStatus.setStyle("-fx-text-fill: #66c0f4; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        titleBlock.getChildren().addAll(titleRow, lblStatus);
        titleBlock.setCursor(Cursor.HAND);
        titleBlock.setOnMouseClicked(e -> iniciarScan());

        HBox searchContainer = new HBox();
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 20; -fx-padding: 5 10;");
        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        searchContainer.setMaxWidth(Double.MAX_VALUE);

        Region iconSearch = criarIcone(SVG_SEARCH, 16, 16, "#8b949e");
        
        TextField txtBusca = new TextField();
        txtBusca.setPromptText("Buscar jogo...");
        txtBusca.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-prompt-text-fill: #8b949e;");
        HBox.setHgrow(txtBusca, Priority.ALWAYS); 
        txtBusca.textProperty().addListener((obs, oldVal, newVal) -> filtrarJogos(newVal));

        Button btnFilter = new Button();
        Region iconFilter = criarIcone(SVG_FILTER, 16, 16, "white");
        btnFilter.setGraphic(iconFilter);
        btnFilter.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        
        RotateTransition rotateFilter = new RotateTransition(Duration.millis(300), iconFilter);
        
        ContextMenu menuFilter = new ContextMenu();
        menuFilter.setStyle("-fx-background-color: #1b2838; -fx-text-fill: white; -fx-base: #1b2838; -fx-control-inner-background: #1b2838;");
        
        MenuItem itemSortMore = new MenuItem("Maior Recompensa");
        itemSortMore.setStyle("-fx-text-fill: white;");
        itemSortMore.setOnAction(e -> ordenarCards("MAIOR"));
        MenuItem itemSortLess = new MenuItem("Menor Recompensa");
        itemSortLess.setStyle("-fx-text-fill: white;");
        itemSortLess.setOnAction(e -> ordenarCards("MENOR"));
        MenuItem itemSortName = new MenuItem("Nome (A-Z)");
        itemSortName.setStyle("-fx-text-fill: white;");
        itemSortName.setOnAction(e -> ordenarCards("AZ"));
        
        menuFilter.getItems().addAll(itemSortMore, itemSortLess, itemSortName);
        
        menuFilter.setOnShowing(e -> {
            rotateFilter.setToAngle(90); 
            rotateFilter.play();
            iconFilter.setStyle("-fx-shape: \"" + SVG_FILTER + "\"; -fx-background-color: #00efff; -fx-scale-shape: true;");
        });

        menuFilter.setOnHidden(e -> {
            rotateFilter.setToAngle(0);
            rotateFilter.play();
            iconFilter.setStyle("-fx-shape: \"" + SVG_FILTER + "\"; -fx-background-color: white; -fx-scale-shape: true;");
        });
        
        btnFilter.setOnAction(e -> menuFilter.show(btnFilter, javafx.geometry.Side.BOTTOM, 0, 0));

        searchContainer.getChildren().addAll(iconSearch, txtBusca, btnFilter);

        topBar.getChildren().addAll(titleBlock, searchContainer, new Label(" "));

        StackPane contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        gridJogos = new TilePane();
        gridJogos.setOpacity(0);
        
        gridJogos.setHgap(25);
        gridJogos.setVgap(25);
        gridJogos.setPrefColumns(3); 
        gridJogos.setAlignment(Pos.TOP_CENTER);
        gridJogos.setPadding(new Insets(30, 20, 20, 20)); 
        gridJogos.setClip(null);

        ScrollPane scroll = new ScrollPane(gridJogos);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setVisible(false);
        Label lblEmpty = new Label("Tudo limpo! Nenhum jogo aqui.");
        lblEmpty.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 16px; -fx-font-weight: bold; -fx-opacity: 0.7;");
        emptyState.getChildren().add(lblEmpty);

        contentStack.getChildren().addAll(emptyState, scroll);

        centerArea.getChildren().addAll(topBar, contentStack);
        contentLayout.setCenter(centerArea);

        containerNavegador = criarInterfaceNavegador();
        containerNavegador.setVisible(false);

        AnchorPane uiOverlay = new AnchorPane();
        uiOverlay.setPickOnBounds(false); 
        
        HBox windowControls = criarControlesJanela();
        uiOverlay.getChildren().add(windowControls);
        AnchorPane.setTopAnchor(windowControls, 10.0);
        AnchorPane.setRightAnchor(windowControls, 10.0);

        rootStack.getChildren().addAll(contentLayout, containerNavegador, uiOverlay);

        Scene scene = new Scene(rootStack);
        scene.setFill(Color.TRANSPARENT); 
        
        stage.setTitle("Lazy Deck");
        stage.setScene(scene);
        stage.show();

        verificarSessao();
    }

    private void iniciarScan() {
        if (carregamentoEmAndamento) return;

        gridJogos.getChildren().clear();
        todosOsCards.clear();
        lblStatus.setText("Carregando biblioteca Steam...");
        WebEngine engine = NavegadorGlobal.getEngine();
        NavegadorGlobal.limparListeners();
        
        carregamentoEmAndamento = true;
        
        listenerCargaScan = new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED && engine.getLocation().contains("/badges")) {
                    engine.getLoadWorker().stateProperty().removeListener(this);
                    rodarExtratorJavascript(engine);
                }
            }
        };
        
        engine.getLoadWorker().stateProperty().addListener(listenerCargaScan);
        engine.load("https://steamcommunity.com/my/badges");
    }

    private void mostrarLoadingInicial() {
        if (overlayLoading == null) {
            overlayLoading = new StackPane();
            overlayLoading.setStyle("-fx-background-color: transparent;"); 
            
            VBox box = new VBox(15);
            box.setAlignment(Pos.CENTER);
            
            Region loadingIcon = criarIcone(SVG_STEAM_OFFICIAL, 64, 64, "#66c0f4");
            
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(0.8), loadingIcon);
            pulse.setFromX(1.0); pulse.setToX(1.15);
            pulse.setFromY(1.0); pulse.setToY(1.15);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();
            
            Label lbl = new Label("Carregando biblioteca Steam...");
            lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            box.getChildren().addAll(loadingIcon, lbl);
            overlayLoading.getChildren().add(box);
            
            int indexUi = rootStack.getChildren().indexOf(containerNavegador);
            rootStack.getChildren().add(indexUi, overlayLoading);
            
            BoxBlur blur = new BoxBlur(15, 15, 3);
            contentLayout.setEffect(blur);
        }
        overlayLoading.setVisible(true);
    }

    private void ocultarLoading() {
        Platform.runLater(() -> {
            if (overlayLoading != null) {
                overlayLoading.setVisible(false);
                rootStack.getChildren().remove(overlayLoading);
                overlayLoading = null;
            }
            contentLayout.setEffect(null);
            carregamentoEmAndamento = false; 
        });
    }

    private void mostrarBloqueioLogin() {
        Platform.runLater(() -> {
            BoxBlur blur = new BoxBlur(15, 15, 3);
            contentLayout.setEffect(blur);

            overlayLogin = new StackPane();
            overlayLogin.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); 
            
            VBox modal = new VBox(20);
            modal.setMaxWidth(380);
            modal.setMaxHeight(320); 
            modal.setAlignment(Pos.CENTER);
            modal.setPadding(new Insets(40));
            modal.setStyle("-fx-background-color: #171a21; " +
                           "-fx-background-radius: 12; " +
                           "-fx-border-radius: 12; " +
                           "-fx-border-color: rgba(255,255,255,0.1); " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 10);");
            
            Label lblAviso = new Label("Conectar Steam");
            lblAviso.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 22px;");
            
            Label lblDesc = new Label("Sincronize sua biblioteca para gerenciar seus jogos e conseguir suas cartas.");
            lblDesc.setWrapText(true);
            lblDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            lblDesc.setStyle("-fx-text-fill: #8f98a0; -fx-font-size: 13px; -fx-line-spacing: 3;");
            
            Button btnLogin = new Button("ENTRAR COM STEAM");
            Region steamIcon = criarIcone(SVG_STEAM_OFFICIAL, 20, 20, "white");
            btnLogin.setGraphic(steamIcon);
            btnLogin.setGraphicTextGap(12);
            btnLogin.setStyle("-fx-background-color: #1769ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 4; -fx-padding: 12 24; -fx-cursor: hand;");
            
            btnLogin.setOnMouseEntered(e -> btnLogin.setStyle("-fx-background-color: #06bfff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 4; -fx-padding: 12 24; -fx-cursor: hand;"));
            btnLogin.setOnMouseExited(e -> btnLogin.setStyle("-fx-background-color: #1769ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 4; -fx-padding: 12 24; -fx-cursor: hand;"));
            
            btnLogin.setOnAction(e -> {
                overlayLogin.setVisible(false); 
                containerNavegador.setVisible(true);
                NavegadorGlobal.getEngine().load("https://steamcommunity.com/login/home/?goto=my/badges");
            });

            VBox footerBox = new VBox(5);
            footerBox.setAlignment(Pos.CENTER);

            HBox secureRow = new HBox(8);
            secureRow.setAlignment(Pos.CENTER);
            Region lockIcon = criarIcone(SVG_LOCK_SOLID, 10, 12, "#4cff00");
            Label lblSecure = new Label("Login seguro via site oficial da Steam");
            lblSecure.setStyle("-fx-text-fill: #56585a; -fx-font-size: 11px; -fx-font-weight: bold;");
            secureRow.getChildren().addAll(lockIcon, lblSecure);

            Label lblReason = new Label("Necessário para verificar quais jogos possuem cartas.");
            lblReason.setStyle("-fx-text-fill: #3a3d42; -fx-font-size: 10px;");

            footerBox.getChildren().addAll(secureRow, lblReason);

            modal.getChildren().addAll(lblAviso, lblDesc, new Region(), btnLogin, footerBox);
            overlayLogin.getChildren().add(modal);
            
            int indexNav = rootStack.getChildren().indexOf(containerNavegador);
            rootStack.getChildren().add(indexNav, overlayLogin);
        });
    }

    private HBox criarControlesJanela() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Button btnMin = new Button("─");
        btnMin.getStyleClass().add("window-btn");
        btnMin.setMinWidth(40);
        btnMin.setOnAction(e -> stage.setIconified(true));

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().addAll("window-btn", "window-btn-close");
        btnClose.setMinWidth(40);
        btnClose.setOnAction(e -> System.exit(0));

        controls.getChildren().addAll(btnMin, btnClose);
        return controls;
    }

    private VBox criarInterfaceNavegador() {
        VBox container = new VBox();
        container.setStyle("-fx-background-color: #0d1117;");
        container.setAlignment(Pos.TOP_CENTER);

        HBox addressBar = new HBox(10);
        addressBar.setAlignment(Pos.CENTER_LEFT);
        addressBar.setPadding(new Insets(15, 20, 15, 20)); 
        addressBar.setStyle("-fx-background-color: #0d1117;");

        Button btnVoltar = new Button("Cancelar");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-cursor: hand; -fx-font-weight: bold;");
        
        btnVoltar.setOnAction(e -> {
            NavegadorGlobal.getEngine().load("about:blank");
            containerNavegador.setVisible(false);
            if(overlayLogin != null) overlayLogin.setVisible(true);
        });

        HBox urlBox = new HBox(10);
        urlBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(urlBox, Priority.ALWAYS);
        urlBox.setStyle("-fx-background-color: #161b22; -fx-background-radius: 8; -fx-padding: 8 15; -fx-border-color: #30363d; -fx-border-radius: 8;");
        
        Region iconLock = criarIcone(SVG_LOCK_SOLID, 10, 12, "#4cff00");
        Label lblUrl = new Label("https://steamcommunity.com/login");
        lblUrl.setStyle("-fx-text-fill: #8b949e; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px;");

        urlBox.getChildren().addAll(iconLock, lblUrl);
        HBox.setMargin(urlBox, new Insets(0, 100, 0, 0));

        addressBar.getChildren().addAll(btnVoltar, urlBox);

        WebView webView = NavegadorGlobal.getWebView();
        VBox.setVgrow(webView, Priority.ALWAYS);
        
        WebEngine engine = NavegadorGlobal.getEngine();

        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null) {
                String cleanUrl = newUrl.replace("https://", "").replace("http://", "");
                if (cleanUrl.length() > 60) cleanUrl = cleanUrl.substring(0, 60) + "...";
                lblUrl.setText(cleanUrl);
            }
        });

        listenerCargaLogin = new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    String url = engine.getLocation();
                    if (url.contains("/id/") || url.contains("/profiles/") || url.contains("/my/badges")) {
                        engine.getLoadWorker().stateProperty().removeListener(this);
                        
                        System.out.println("Login detectado com sucesso!");
                        GerenciadorCookies.salvarTodosCookies(MainApp.globalCookieManager);
                        
                        Platform.runLater(() -> {
                            containerNavegador.setVisible(false);
                            if (overlayLogin != null) {
                                rootStack.getChildren().remove(overlayLogin);
                                overlayLogin = null;
                            }
                            mostrarLoadingInicial();
                            iniciarScan();
                        });
                    }
                }
            }
        };
        
        engine.getLoadWorker().stateProperty().addListener(listenerCargaLogin);

        container.getChildren().addAll(addressBar, webView);
        return container;
    }

    private void verificarSessao() {
        if (MainApp.globalCookieManager.getCookieStore().getCookies().isEmpty()) {
            mostrarBloqueioLogin();
        } else {
            mostrarLoadingInicial(); 
            Platform.runLater(this::iniciarScan);
        }
    }

    private void carregarBlacklist() {
        try {
            if (Files.exists(Paths.get("blacklist.txt"))) {
                List<String> lines = Files.readAllLines(Paths.get("blacklist.txt"));
                for (String line : lines) {
                    try { blacklist.add(Integer.parseInt(line.trim())); } catch (Exception e) {}
                }
            }
        } catch (Exception e) { System.out.println("Erro ao carregar blacklist"); }
    }

    private void alternarBlacklist(int appId, VBox card) {
        boolean estavaNaLista = blacklist.contains(appId);
        if (estavaNaLista) { blacklist.remove(appId); card.setOpacity(1.0); } 
        else { blacklist.add(appId); card.setOpacity(0.4); }
        
        try {
            String conteudo = blacklist.stream().map(String::valueOf).collect(Collectors.joining("\n"));
            Files.writeString(Paths.get("blacklist.txt"), conteudo);
        } catch (IOException e) { e.printStackTrace(); }
        atualizarIconeOlho(card, appId, true);
        recalcularTotais();
    }
    
    private void recalcularTotais() {
        int novosDrops = 0;
        int jogosAtivos = 0;
        for(Node n : todosOsCards) {
            DadosCard d = (DadosCard) n.getUserData();
            if (!blacklist.contains(d.appId)) {
                novosDrops += d.drops;
                jogosAtivos++;
            }
        }
        atualizarEstimativas(novosDrops, jogosAtivos);
    }

    private void filtrarJogos(String termo) {
        gridJogos.getChildren().clear();
        if (termo == null || termo.isEmpty()) {
            gridJogos.getChildren().addAll(todosOsCards);
        } else {
            String termoLower = termo.toLowerCase();
            List<Node> filtrados = todosOsCards.stream()
                .filter(node -> {
                    DadosCard dados = (DadosCard) node.getUserData();
                    return dados.nome.toLowerCase().contains(termoLower);
                })
                .collect(Collectors.toList());
            gridJogos.getChildren().addAll(filtrados);
        }
        checarEmptyState();
    }
    
    private void checarEmptyState() {
        boolean vazio = gridJogos.getChildren().isEmpty();
        emptyState.setVisible(vazio);
        if (vazio) emptyState.toFront();
        else emptyState.toBack();
    }

    private void ordenarCards(String criterio) {
        Comparator<Node> comparator = (n1, n2) -> {
            DadosCard d1 = (DadosCard) n1.getUserData();
            DadosCard d2 = (DadosCard) n2.getUserData();
            switch (criterio) {
                case "MAIOR": return Integer.compare(d2.drops, d1.drops);
                case "MENOR": return Integer.compare(d1.drops, d2.drops);
                case "AZ": return d1.nome.compareToIgnoreCase(d2.nome);
                default: return 0;
            }
        };
        todosOsCards.sort(comparator);
        filtrarJogos("");
    }

    private void atualizarEstimativas(int totalDrops, int totalJogos) {
        Platform.runLater(() -> {
            int totalMinutos = totalDrops * 20;
            String tempoStr = (totalMinutos < 60) ? totalMinutos + " min" : (totalMinutos/60) + "h " + (totalMinutos%60) + "m";
            lblTempoEstimado.setText(tempoStr);
            
            lblTotalJogos.setText(String.valueOf(totalJogos));
            lblCartasRestantesSidebar.setText(totalDrops + " CARTAS"); 
            lblStatus.setText("Biblioteca sincronizada e ordenada.");
        });
    }

    private Region criarIcone(String svgContent, double w, double h, String corHex) {
        Region icon = new Region();
        icon.setPrefSize(w, h);
        icon.setMinSize(w, h);
        icon.setMaxSize(w, h);
        icon.setStyle("-fx-shape: \"" + svgContent + "\"; -fx-background-color: " + corHex + "; -fx-scale-shape: true;");
        return icon;
    }

    private String lerSvgArquivo(String nomeArquivo, String fallback) {
        try {
            String caminho = "icons/" + nomeArquivo;
            String conteudo = Files.readString(Paths.get(caminho));
            Pattern pattern = Pattern.compile("d=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(conteudo);
            if (matcher.find()) return matcher.group(1);
        } catch (Exception e) {}
        return fallback;
    }

    private VBox criarStatBox(String titulo, String valorInicial, String svgIcone) {
        return criarStatBoxPersonalizado(titulo, valorInicial, svgIcone, 18, 18);
    }

    private VBox criarStatBoxPersonalizado(String titulo, String valorInicial, String svgIcone, double w, double h) {
        VBox box = new VBox(2);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-padding: 10; -fx-alignment: center;");
        Label lblValor = new Label(valorInicial);
        
        if (svgIcone != null) {
            HBox linha = new HBox(8);
            linha.setAlignment(Pos.CENTER);
            Region icon = criarIcone(svgIcone, w, h, "#8b949e");
            linha.getChildren().addAll(icon, lblValor);
            box.getChildren().add(linha);
        } else {
            box.getChildren().add(lblValor);
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 9px; -fx-font-weight: bold;");
        box.getChildren().add(lblTitulo);
        return box;
    }

    private String limparNomeNuclear(String sujo) {
        if (sujo == null) return "";
        String limpo = sujo.replace("Ver detalhes", "").replace("View details", "");
        limpo = limpo.replace("\n", " ").replace("\r", "").replace("\t", " ");
        limpo = limpo.replace("\u00A0", " ");
        return limpo.replaceAll("\\s+", " ").trim();
    }

    private void rodarExtratorJavascript(WebEngine engine) {
        String script = 
            "(function() {" +
            "   var output = '';" +
            "   var accPull = document.getElementById('account_pulldown');" +
            "   var userName = accPull ? accPull.innerText.trim() : null;" +
            "   if (!userName) {" +
            "       var nameElem = document.querySelector('.profile_small_header_name .whiteLink') || " +
            "                      document.querySelector('.actual_persona_name');" +
            "       userName = nameElem ? nameElem.innerText : 'Steam User';" +
            "   }" +
            "   var avatarImg = document.querySelector('.playerAvatar img');" +
            "   var avatarUrl = avatarImg ? avatarImg.src : 'NULL';" +
            "   var lvlSpan = document.querySelector('.friendPlayerLevelNum');" +
            "   var userLvl = lvlSpan ? lvlSpan.innerText : '??';" +
            "   var walletElem = document.getElementById('header_wallet_balance');" +
            "   var wallet = walletElem ? walletElem.innerText : 'R$ --';" +
            "   output += 'USER_DATA###' + userName + '###' + avatarUrl + '###' + userLvl + '###' + wallet + '|||';" +
            "   var rows = document.getElementsByClassName('badge_row');" +
            "   for (var i = 0; i < rows.length; i++) {" +
            "       var row = rows[i];" +
            "       var dropSpan = row.querySelector('.progress_info_bold');" +
            "       if (dropSpan) {" +
            "           var drops = dropSpan.innerText.trim();" +
            "           var fullText = (row.innerText || row.textContent).toLowerCase();" +
            "           if (fullText.indexOf('drop') !== -1 || fullText.indexOf('dar mais') !== -1 || fullText.indexOf('resta') !== -1 || fullText.indexOf('card') !== -1) {" +
            "               var titleDiv = row.querySelector('.badge_title');" +
            "               var name = 'Jogo';" +
            "               if (titleDiv) {" +
            "                   var clone = titleDiv.cloneNode(true);" +
            "                   var links = clone.querySelectorAll('a');" +
            "                   for (var k=0; k<links.length; k++) links[k].remove();" +
            "                   name = clone.innerText;" +
            "                   if(name.length < 1) name = titleDiv.innerText.split('\\n')[0];" +
            "               }" +
            "               output += name + '###' + drops + '|||';" +
            "           }" +
            "       }" +
            "   }" +
            "   return output;" +
            "})()";

        try {
            Object resposta = engine.executeScript(script);
            if (resposta == null || resposta.toString().isEmpty()) return;

            String[] linhas = resposta.toString().split("\\|\\|\\|");
            List<Node> novosCards = new ArrayList<>();
            Set<Integer> idsProcessadosNesteLote = new HashSet<>();

            for (String linha : linhas) {
                String[] partes = linha.split("###");
                if (partes[0].equals("USER_DATA") && partes.length >= 5) {
                    final String uName = partes[1];
                    final String uAvatar = partes[2];
                    final String uLvl = partes[3];
                    final String uWallet = partes[4];
                    Platform.runLater(() -> {
                        lblUserName.setText(uName);
                        lblUserLevel.setText("Lv. " + uLvl);
                        lblSaldoCarteira.setText("Saldo Steam: " + uWallet);
                        if (!uAvatar.equals("NULL")) imgAvatar.setImage(new Image(uAvatar, true));
                    });
                    continue;
                }
                if (partes.length >= 2) {
                    String nomeBruto = partes[0]; 
                    String dropsStr = partes[1].replaceAll("[^0-9]", "");
                    String nomeLimpo = limparNomeNuclear(nomeBruto);
                    int qtd = dropsStr.isEmpty() ? 0 : Integer.parseInt(dropsStr);
                    int appId = banco.buscarIdPorNomeExato(nomeLimpo);

                    if (appId > 0 && !idsProcessadosNesteLote.contains(appId)) {
                        idsProcessadosNesteLote.add(appId);
                        Node card = criarCardJogo(appId, nomeLimpo, qtd);
                        if (card != null) novosCards.add(card);
                    }
                }
            }
            
            Platform.runLater(() -> {
                gridJogos.setOpacity(0);
                todosOsCards.clear(); 
                todosOsCards.addAll(novosCards);
                ordenarCards("MAIOR"); 
                recalcularTotais();
                checarEmptyState();
                lblStatus.setText("Sincronização concluída.");
                ocultarLoading();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(800), gridJogos);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void atualizarIconeOlho(Node card, int appId, boolean forceShow) {
        StackPane stack = (StackPane) ((VBox)card).getChildren().get(0);
        Node eyeBox = stack.getChildren().stream()
            .filter(n -> n instanceof HBox && StackPane.getAlignment(n) == Pos.TOP_LEFT)
            .findFirst().orElse(null);
            
        if (eyeBox != null) {
            Region icon = (Region) ((HBox)eyeBox).getChildren().get(0);
            boolean ignorado = blacklist.contains(appId);
            estilizarIconeOlho(icon, ignorado);
            if (ignorado) eyeBox.setVisible(true);
            else eyeBox.setVisible(forceShow);
        }
    }

    private void estilizarIconeOlho(Region icon, boolean bloqueado) {
        icon.setStyle(null); 
        if (bloqueado) {
            icon.setPrefSize(13, 13);
            icon.setMinSize(13, 13);
            icon.setMaxSize(13, 13);
            icon.setStyle("-fx-shape: \"" + SVG_EYE_SLASH + "\"; -fx-background-color: #ff4444; -fx-scale-shape: true;");
        } else {
            icon.setPrefSize(13, 9);
            icon.setMinSize(13, 9);
            icon.setMaxSize(13, 9);
            icon.setStyle("-fx-shape: \"" + SVG_EYE_OPEN + "\"; -fx-background-color: white; -fx-scale-shape: true;");
        }
    }

    private VBox criarCardJogo(int appId, String nome, int drops) {
        VBox card = new VBox();
        card.setPrefSize(180, 270);
        card.setMaxSize(180, 270);
        card.getStyleClass().add("game-card");
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setUserData(new DadosCard(appId, nome, drops));

        if (blacklist.contains(appId)) card.setOpacity(0.4);

        String urlImg = "https://cdn.akamai.steamstatic.com/steam/apps/" + appId + "/library_600x900.jpg";
        ImageView imgView = new ImageView(new Image(urlImg, true)); 
        imgView.setFitWidth(180);
        imgView.setFitHeight(270);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);
        
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(180, 270);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imgView.setClip(clip);

        VBox overlayName = new VBox();
        overlayName.setAlignment(Pos.CENTER);
        overlayName.setStyle("-fx-background-color: linear-gradient(to top, rgba(0,0,0,1) 0%, rgba(0,0,0,0) 100%); -fx-padding: 10; -fx-background-radius: 0 0 12 12;");
        overlayName.setPrefHeight(60);
        overlayName.setMaxHeight(60);
        
        Label lblNome = new Label(nome);
        lblNome.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
        lblNome.setWrapText(false);
        lblNome.setMaxWidth(160);
        overlayName.getChildren().add(lblNome);

        HBox badge = new HBox(5);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 15; -fx-padding: 4 8;");
        badge.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        String svgCardsData = lerSvgArquivo("deck.svg", SVG_DECK_FALLBACK);
        Region iconDeckSmall = criarIcone(svgCardsData, 12, 12, "white");
        
        Label lblDrops = new Label(drops + " Cartas");
        lblDrops.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        badge.getChildren().addAll(iconDeckSmall, lblDrops);
        
        HBox eyeBox = new HBox();
        eyeBox.setAlignment(Pos.CENTER);
        eyeBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 50; -fx-padding: 6;");
        eyeBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        eyeBox.setCursor(Cursor.HAND);
        
        Region iconEye = new Region();
        estilizarIconeOlho(iconEye, blacklist.contains(appId));
        
        eyeBox.getChildren().add(iconEye);
        
        if (blacklist.contains(appId)) {
            eyeBox.setVisible(true);
        } else {
            eyeBox.setVisible(false);
        }
        
        final VBox cardRef = card; 
        eyeBox.setOnMouseClicked(e -> {
            e.consume();
            alternarBlacklist(appId, cardRef);
        });

        StackPane stack = new StackPane();
        stack.getChildren().addAll(imgView, overlayName, badge, eyeBox);
        
        StackPane.setAlignment(overlayName, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setAlignment(eyeBox, Pos.TOP_LEFT);
        
        StackPane.setMargin(badge, new Insets(8)); 
        StackPane.setMargin(eyeBox, new Insets(8)); 

        card.getChildren().add(stack);

        card.setCache(true);
        card.setCacheHint(javafx.scene.CacheHint.QUALITY);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), card);
        scaleIn.setToX(1.10); scaleIn.setToY(1.10);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), card);
        scaleOut.setToX(1.0); scaleOut.setToY(1.0);
        DropShadow glowEffect = new DropShadow();
        glowEffect.setColor(Color.rgb(0, 239, 255, 0.25)); 
        glowEffect.setRadius(30); glowEffect.setSpread(0.15); 
        glowEffect.setOffsetX(0); glowEffect.setOffsetY(0);

        card.setOnMouseEntered(e -> {
            card.setViewOrder(-100.0);
            scaleIn.playFromStart();
            card.setEffect(glowEffect);
            if (!blacklist.contains(appId)) eyeBox.setVisible(true);
        });

        card.setOnMouseExited(e -> {
            scaleOut.playFromStart();
            card.setEffect(null); 
            new java.util.Timer().schedule(new java.util.TimerTask(){
                @Override public void run() { Platform.runLater(() -> card.setViewOrder(0.0)); }
            }, 150);
            if (!blacklist.contains(appId)) eyeBox.setVisible(false);
        });

        card.setOnMouseClicked(e -> {
             if (e.getButton() == MouseButton.PRIMARY && !blacklist.contains(appId)) {
                 new TelaFarming(stage, appId, nome, drops).mostrar(rootStack);
             }
        });

        return card;
    }
}