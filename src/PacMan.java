import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;

public class PacMan extends JPanel implements ActionListener, KeyListener {
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

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();

            // Simulation du mouvement pour tester la collision
            int nextX = this.x + this.velocityX;
            int nextY = this.y + this.velocityY;

            // Créer un bloc temporaire pour tester la collision
            Block testBlock = new Block(this.image, nextX, nextY, this.width, this.height);

            // Vérifier s'il y aurait collision avec les murs
            for (Block wall : walls) {
                if (collision(testBlock, wall)) {
                    // Collision détectée : annuler le changement
                    this.direction = prevDirection;
                    updateVelocity();
                    return;
                }
            }
            // Si pas de collision, le changement est conservé
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
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

        // Boucle de jeu - Timer de 50ms = 20 FPS
        Timer gameLoop = new Timer(50, this);
        gameLoop.start();

        // Activation de l'écoute clavier
        addKeyListener(this);
        setFocusable(true);

        System.out.println("Carte du jeu chargée : " + tileMap.length + " lignes x " + tileMap[0].length() + " colonnes");
        System.out.println("Classe Block créée avec succès !");
        System.out.println("Structures de données initialisées !");
        System.out.println("Boucle de jeu démarrée (20 FPS) !");
        System.out.println("Contrôles clavier activés !");
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

    @Override
    public void actionPerformed(ActionEvent e) {
        // Cette méthode est appelée toutes les 50ms (20 FPS)
        move();
        repaint(); // Redessine l'écran
    }

    public void move() {
        // Mouvement de Pac-Man
        if (pacman != null) {
            // Sauvegarde de la position précédente
            int prevX = pacman.x;
            int prevY = pacman.y;

            // Mise à jour de la position
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;

            // Vérification des collisions avec les murs
            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    // Collision détectée : annuler le mouvement
                    pacman.x = prevX;
                    pacman.y = prevY;
                    break;
                }
            }

            // Gestion des bords (téléportation)
            // Bords gauche/droite
            if (pacman.x >= boardWidth) {
                pacman.x = -tileSize;
            }
            else if (pacman.x + tileSize < 0) {
                pacman.x = boardWidth;
            }

            // Bords haut/bas
            if (pacman.y >= boardHeight) {
                pacman.y = -tileSize;
            }
            else if (pacman.y + tileSize < 0) {
                pacman.y = boardHeight;
            }
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilisée pour les touches directionnelles
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Non utilisée dans ce projet
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (pacman == null) return;

        // Sauvegarder la direction actuelle
        char prevDirection = pacman.direction;

        // Gestion des touches directionnelles
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
            // Changer l'image seulement si la direction a vraiment changé
            if (pacman.direction == 'U') {
                pacman.image = pacmanUpImage;
                System.out.println("Pac-Man va vers le HAUT");
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
            if (pacman.direction == 'D') {
                pacman.image = pacmanDownImage;
                System.out.println("Pac-Man va vers le BAS");
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
            if (pacman.direction == 'L') {
                pacman.image = pacmanLeftImage;
                System.out.println("Pac-Man va vers la GAUCHE");
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
            if (pacman.direction == 'R') {
                pacman.image = pacmanRightImage;
                System.out.println("Pac-Man va vers la DROITE");
            }
        }
    }
}