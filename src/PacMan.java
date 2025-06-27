import java.awt.*;
import javax.swing.*;

public class PacMan extends JPanel {
    private int boardWidth;
    private int boardHeight;

    public PacMan(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
    }
}