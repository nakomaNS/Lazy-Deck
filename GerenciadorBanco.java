import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorBanco {

    private static final String URL_CONEXAO = "jdbc:sqlite:sdk/steam_games.db";
    private Connection conexao;

    public boolean conectar() {
        try {
            conexao = DriverManager.getConnection(URL_CONEXAO);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao conectar no banco: " + e.getMessage());
            return false;
        }
    }

    public void desconectar() {
        try {
            if (conexao != null) conexao.close();
        } catch (SQLException e) {}
    }

    public List<String> pesquisarPorNome(String termo) {
        List<String> resultados = new ArrayList<>();
        String sql = "SELECT appid, name FROM games WHERE name LIKE ? LIMIT 20";
        try (PreparedStatement pstmt = conexao.prepareStatement(sql)) {
            pstmt.setString(1, "%" + termo + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                resultados.add(rs.getInt("appid") + " ### " + rs.getString("name"));
            }
        } catch (SQLException e) {}
        return resultados;
    }
    
    public String obterNomePeloId(int appid) {
        String sql = "SELECT name FROM games WHERE appid = ?";
        try (PreparedStatement pstmt = conexao.prepareStatement(sql)) {
            pstmt.setInt(1, appid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("name");
        } catch (SQLException e) {}
        return "Desconhecido";
    }

    public int buscarIdPorNomeExato(String nomeJogo) {
        if (nomeJogo == null || nomeJogo.isEmpty()) return 0;

        String sql = "SELECT appid FROM games WHERE name = ? COLLATE NOCASE";
        try (PreparedStatement pstmt = conexao.prepareStatement(sql)) {
            pstmt.setString(1, nomeJogo.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("appid");
        } catch (SQLException e) {}
        
        String sqlStart = "SELECT appid FROM games WHERE name LIKE ? LIMIT 1";
        try (PreparedStatement pstmt = conexao.prepareStatement(sqlStart)) {
            pstmt.setString(1, nomeJogo.trim() + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("appid");
        } catch (SQLException e) {}
        return 0;
    }
}