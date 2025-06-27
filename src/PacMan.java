import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;

public class PacMan extends JPanel {
    private int boardWidth;
    private int boardHeight;
    private int tileSize = 32;

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

    // Objets du jeu
    Block pacman;
    ArrayList<Block> walls;
    ArrayList<Block> ghosts;
    HashSet<Block> foods;

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
            "X       bpo       X",
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

        // Initialisation des structures de données
        walls = new ArrayList<Block>();
        ghosts = new ArrayList<Block>();
        foods = new HashSet<Block>();

        // Chargement de la carte
        loadMap();

        System.out.println("Carte du jeu chargée : " + tileMap.length + " lignes x " + tileMap[0].length() + " colonnes");
        System.out.println("Classe Block créée avec succès !");
        System.out.println("Structures de données initialisées !");
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

    private void loadMap() {
        for (int i = 0; i < tileMap.length; i++) {
            for (int j = 0; j < tileMap[i].length(); j++) {
                String row = tileMap[i];
                char tileMapChar = row.charAt(j);

                int x = j * tileSize;
                int y = i * tileSize;

                if (tileMapChar == 'X') { // Mur
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { // Fantôme bleu
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { // Fantôme orange
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { // Fantôme rose
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { // Fantôme rouge
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { // Pac-Man
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { // Nourriture
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
                // '0' = case vide, on l'ignore
            }
        }

        // Tests de vérification
        System.out.println("LoadMap terminé !");
        System.out.println("Murs créés : " + walls.size());
        System.out.println("Fantômes créés : " + ghosts.size());
        System.out.println("Nourriture créée : " + foods.size());
        System.out.println("Pac-Man créé : " + (pacman != null ? "Oui" : "Non"));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Dessiner Pac-Man
        if (pacman != null) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }

        // Dessiner les fantômes
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Dessiner les murs
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Dessiner la nourriture (petites pastilles jaunes)
        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillOval(food.x, food.y, food.width, food.height);
        }
    }
}