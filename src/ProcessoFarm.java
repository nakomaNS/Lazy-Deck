import com.codedisaster.steamworks.SteamAPI;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ProcessoFarm {

    public static void main(String[] args) {
        if (args.length < 1) System.exit(1);

        int appId = Integer.parseInt(args[0]);
        System.out.println(">>> [FANTASMA] Iniciando AppID: " + appId);

        Thread suicida = new Thread(() -> {
            try {
                while (System.in.read() != -1) {
                }
            } catch (Exception e) {}
            System.err.println(">>> [FANTASMA] Pai desconectou. Encerrando...");
            System.exit(0);
        });
        suicida.setDaemon(true);
        suicida.start();

        try {
            File file = new File("steam_appid.txt");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(String.valueOf(appId).getBytes(StandardCharsets.UTF_8));
            }
            Thread.sleep(200);

            SteamAPI.loadLibraries();
            if (!SteamAPI.init()) {
                System.err.println(">>> [FANTASMA] Falha no Init!");
                System.exit(1);
            }

            System.out.println(">>> [FANTASMA] Conectado! Aguardando morte...");

            while (true) {
                if (SteamAPI.isSteamRunning()) {
                    SteamAPI.runCallbacks();
                }
                Thread.sleep(50);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}