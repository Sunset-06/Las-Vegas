import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChessModel model = new ChessModel();
            ChessView view = new ChessView();
            new ChessController(model, view);
            view.setVisible(true);
        });
    }
}