import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamUser;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;

public class MotorSteam {

    private boolean sessaoAtiva;

    public MotorSteam() {
        this.sessaoAtiva = false;
    }

    private void criarArquivoIdentificador(int idJogo) {
        try (FileWriter escritor = new FileWriter("steam_appid.txt")) {
            escritor.write(String.valueOf(idJogo));
        } catch (IOException e) {
            System.err.println("Erro: Falha ao criar arquivo de ID.");
        }
    }

    public static void processarCallbacks() {
        if (SteamAPI.isSteamRunning()) {
            try {
                SteamAPI.runCallbacks();
            } catch (Exception e) {
            }
        }
    }

    public static String obterSteamID() {
        if (SteamAPI.isSteamRunning()) {
            SteamUser user = new SteamUser(null);
            SteamID id = user.getSteamID();
            return String.valueOf(SteamID.getNativeHandle(id));
        }
        return null;
    }

    public boolean iniciarSessaoDeJogo(int appId) {
        if (sessaoAtiva) return true;

        criarArquivoIdentificador(appId);
        
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        try {
            PrintStream dummy = new PrintStream(new OutputStream() {
                public void write(int b) {}
            });

            System.setOut(dummy);
            System.setErr(dummy);

            SteamAPI.loadLibraries();
            
            if (!SteamAPI.init()) {
                System.setOut(originalOut);
                System.setErr(originalErr);
                System.out.println("Aviso: Falha ao conectar na Steam (AppID: " + appId + ")");
                return false; 
            }

            this.sessaoAtiva = true;
            return true;

        } catch (SteamException e) {
            System.setOut(originalOut);
            System.setErr(originalErr);
            return false;
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    public void encerrarSessao() {
        if (this.sessaoAtiva) {
            System.out.println("Encerrando conexão com Steam...");
            try {
                SteamAPI.shutdown();
            } catch (Exception e) {
                System.err.println("Erro ao fechar SteamAPI: " + e.getMessage());
            }
            this.sessaoAtiva = false;
        }
    }
}