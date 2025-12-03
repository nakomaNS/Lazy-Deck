public class JogoComDrop {
  public int appid;
  public String nome;
  public int dropsRestantes;

  public JogoComDrop(int appid, String nome, int dropsRestantes) {
    this.appid = appid;
    this.nome = nome;
    this.dropsRestantes = dropsRestantes;
  }

  @Override
  public String toString() {
    return String.format("[%d] %s - Faltam %d cartas", appid, nome, dropsRestantes);
  }
}