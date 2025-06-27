import java.awt.*;
import javax.swing.*;

public class PacMan extends JPanel {
    private int boardWidth;
    private int boardHeight;

    // Images du mur
    private Image wallImage;

    // Images des fantômes
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    // Images de Pac-Man selon la direction
    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    // Carte du jeu (21 lignes x 19 colonnes)
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X   X   X    X",
            "XXXX XXX X XXX XXXX",
            "0000 X0000000X 0000",
            "XXXX X XXrXX X XXXX",
            "X      XbpoX      X",
            "XXXX X XXXXX X XXXX",
            "0000 X0000000X 0000",
            "XXXX XXX X XXX XXXX",
            "X    X   X   X    X",
            "X XX X XXXXX X XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    // Classe interne Block
    class Block {
        int x, y, width, height;
        Image image;

        int startX, startY;
        char direction = 'U'; // U D L R
        int velocityX = 0, velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }
    }

    public PacMan(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);

        // Chargement des images
        loadImages();

        System.out.println("Carte du jeu chargée : " + tileMap.length + " lignes x " + tileMap[0].length() + " colonnes");
        System.out.println("Classe Block créée avec succès !");
    }

    private void loadImages() {
        // Chargement de l'image du mur
        wallImage = new ImageIcon(getClass().getResource("/images/wall.png")).getImage();

        // Chargement des images des fantômes
        blueGhostImage = new ImageIcon(getClass().getResource("/images/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/images/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/images/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/images/redGhost.png")).getImage();

        // Chargement des images de Pac-Man
        pacmanUpImage = new ImageIcon(getClass().getResource("/images/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/images/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/images/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/images/pacmanRight.png")).getImage();

        System.out.println("Images chargées avec succès !");
    }
}