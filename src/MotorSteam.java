import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class MotorSteam {

    private Process processoAtual;
    private long pidAtual = -1;
    private int appIdAtual = 0;

    public static void carregarBibliotecasGlobais() {
    }

    public static void processarCallbacks() {
    }

    public boolean iniciarSessaoDeJogo(int appId) {
        encerrarSessao();

        this.appIdAtual = appId;

        try {
            System.out.println("[MotorSteam] Iniciando instância para AppID: " + appId);

            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String libPath = System.getProperty("java.library.path");

            ProcessBuilder builder = new ProcessBuilder(
                javaBin,
                "-cp", classpath,
                "-Djava.library.path=" + libPath,
                "--enable-native-access=ALL-UNNAMED",
                "ProcessoFarm",
                String.valueOf(appId)
            );

            builder.redirectErrorStream(true);
            
            processoAtual = builder.start();
            pidAtual = processoAtual.pid();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(processoAtual.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[FANTASMA " + appId + "]: " + line);
                    }
                } catch (IOException e) {}
            }).start();

            Thread.sleep(1000);
            
            if (processoAtual.isAlive()) {
                System.out.println("[MotorSteam " + appId + "] Rodando PID: " + pidAtual);
                return true;
            } else {
                System.out.println("[MotorSteam " + appId + "] Morreu imediatamente.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void encerrarSessao() {
        if (processoAtual == null && pidAtual == -1) return;

        System.out.println("[MotorSteam " + appIdAtual + "] Encerrando PID: " + pidAtual);
        
        if (processoAtual != null) {
            try {
                if (processoAtual.getOutputStream() != null) {
                    processoAtual.getOutputStream().close();
                }
            } catch (IOException e) {}
        }

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        if (pidAtual > 0) {
            try {
                new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pidAtual)).start().waitFor();
            } catch (Exception e) {
                System.err.println("Erro no taskkill: " + e.getMessage());
            }
        }
        
        if (processoAtual != null) {
            processoAtual.destroyForcibly();
        }
        
        try {
            File f = new File("steam_appid.txt");
            if (f.exists()) f.delete();
        } catch (Exception e) {}

        processoAtual = null;
        pidAtual = -1;
        System.out.println("[MotorSteam " + appIdAtual + "] Encerrado.");
    }
}