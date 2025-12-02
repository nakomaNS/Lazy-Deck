import java.io.*;
import java.util.Properties;

public class GerenciadorConfig {
    private static final String ARQUIVO_CONFIG = "config.properties";
    private static Properties props = new Properties();

    static {
        carregar();
    }

    private static void carregar() {
        try (InputStream input = new FileInputStream(ARQUIVO_CONFIG)) {
            props.load(input);
        } catch (IOException ex) {
            System.out.println("Configuração nova ou não encontrada. Usando padrões.");
        }
    }

    public static int getTempoMin() {
        return Integer.parseInt(props.getProperty("tempoMin", "5"));
    }

    public static int getTempoMax() {
        return Integer.parseInt(props.getProperty("tempoMax", "15"));
    }

    public static boolean isMinimizarParaTray() {
        return Boolean.parseBoolean(props.getProperty("minimizarTray", "false"));
    }

    public static void salvarConfig(int min, int max, boolean tray) {
        props.setProperty("tempoMin", String.valueOf(min));
        props.setProperty("tempoMax", String.valueOf(max));
        props.setProperty("minimizarTray", String.valueOf(tray));
        
        try (OutputStream output = new FileOutputStream(ARQUIVO_CONFIG)) {
            props.store(output, "Configuracoes LazyDeck");
            System.out.println("Configurações salvas: Min=" + min + ", Max=" + max + ", Tray=" + tray);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void restaurarPadrao() {
        salvarConfig(5, 15, false);
    }
}