import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    private int boardWidth;
    private int boardHeight;
    private int tileSize = 32;

    // Directions possibles pour les fantômes
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();

    // Images du mur
    private Image wallImage;

    // Images des fantômes
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image scaredGhostImage; // Fantôme effrayé

    // Images de Pac-Man selon la direction
    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    // Images des bonus
    private Image cherryImage;
    private Image superCherryImage;

    // Objets du jeu
    Block pacman;
    ArrayList<Block> walls;
    ArrayList<Block> ghosts;
    HashSet<Block> foods;

    // Collections pour les bonus
    ArrayList<Block> cherries;      // Petites cerises (50 points)
    ArrayList<Block> superCherries; // Grandes cerises (100 points)
    ArrayList<Block> powerPellets;  // Super pastilles

    // Variables de jeu
    int score = 0;           // Score du joueur
    int lives = 3;           // Nombre de vies de Pac-Man
    boolean gameOver = false; // Indique si le jeu est terminé

    // NOUVEAU: Variables pour le système de meilleur score
    int bestScore = 0;              // Meilleur score enregistré
    boolean isNewRecord = false;    // Indique si on a battu le record
    String bestScoreFilePath = "bestScore.txt"; // Fichier de sauvegarde

    // Variables pour les effets spéciaux
    boolean powerMode = false;      // Mode où les fantômes sont vulnérables
    int powerModeTimer = 0;        // Durée restante du mode power
    int ghostEatenScore = 200;     // Score pour manger un fantôme (200, 400, 800, 1600)

    // Timer pour spawn des cerises
    int cherrySpawnTimer = 0;
    int cherrySpawnInterval = 600; // Apparition toutes les 30 secondes (600 frames à 20 FPS)

    // Variables des cerises
    private int cherryLifetime = 600; // Durée de vie des cerises (30 secondes)
    private ArrayList<Integer> cherryTimers; // Timer pour chaque cerise
    private ArrayList<Integer> superCherryTimers; // Timer pour chaque super cerise

    // Effets spéciaux des super cerises
    private boolean invincibility = false;
    private int invincibilityTimer = 0;

    // Timer de jeu
    Timer gameLoop;

    // Carte du jeu (21 lignes x 19 colonnes) - SANS CERISES FIXES
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "XS       X       SX", // S = Super pastille
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "000X X       X X000",
            "XXXX X XXrXX X XXXX",
            "        bpo        ",
            "XXXX X XXXXX X XXXX",
            "000X X       X X000",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "XS               SX", // S = Super pastille
            "XXXXXXXXXXXXXXXXXXX"
    };

    // Classe interne Block avec amélioration pour Scared Mode
    class Block {
        int x, y, width, height;
        Image image;
        Image originalImage; // NOUVEAU: pour sauvegarder l'image originale

        int startX, startY;
        char direction = 'U'; // U D L R
        int velocityX = 0, velocityY = 0;

        // NOUVEAU: Variables pour le Scared Mode
        boolean isScared = false;
        int normalSpeed = 8; // Vitesse normale (tileSize/4)
        int scaredSpeed = 4; // Vitesse réduite quand effrayé

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.originalImage = image; // NOUVEAU: sauvegarder l'image originale
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
            // MODIFIÉ: Utiliser la vitesse appropriée selon l'état scared
            int speed = isScared ? scaredSpeed : normalSpeed;

            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -speed;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = speed;
            }
            else if (this.direction == 'L') {
                this.velocityX = -speed;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = speed;
                this.velocityY = 0;
            }
        }

        // NOUVEAU: Méthode pour activer le mode scared
        void setScared(boolean scared) {
            this.isScared = scared;
            if (scared) {
                this.image = scaredGhostImage;
            } else {
                this.image = this.originalImage;
            }
            updateVelocity(); // Mettre à jour la vitesse
        }

        // NOUVEAU: Méthode pour obtenir la direction opposée (fuite)
        char getOppositeDirection() {
            switch (this.direction) {
                case 'U': return 'D';
                case 'D': return 'U';
                case 'L': return 'R';
                case 'R': return 'L';
                default: return 'U';
            }
        }

        // NOUVEAU: Méthode pour fuir Pac-Man
        void fleeFromPacman() {
            if (!isScared || pacman == null) return;

            // Calculer la direction pour s'éloigner de Pac-Man
            int deltaX = this.x - pacman.x;
            int deltaY = this.y - pacman.y;

            char fleeDirection = this.direction; // Direction par défaut

            // Déterminer la meilleure direction pour fuir
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                // Fuir horizontalement
                fleeDirection = (deltaX > 0) ? 'R' : 'L';
            } else {
                // Fuir verticalement
                fleeDirection = (deltaY > 0) ? 'D' : 'U';
            }

            // Essayer la direction de fuite, sinon direction aléatoire
            char prevDirection = this.direction;
            updateDirection(fleeDirection);

            // Si la direction de fuite mène à un mur, essayer une direction aléatoire
            if (this.direction == prevDirection) {
                // La direction n'a pas changé, probablement à cause d'un mur
                char randomDirection = directions[random.nextInt(4)];
                updateDirection(randomDirection);
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
            setScared(false); // MODIFIÉ: Reset du scared mode
        }
    }

    public PacMan(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);

        // NOUVEAU: Charger le meilleur score au démarrage
        loadBestScore();

        // Chargement des images
        loadImages();

        // Initialisation des structures de données
        walls = new ArrayList<Block>();
        ghosts = new ArrayList<Block>();
        foods = new HashSet<Block>();

        // Initialisation des nouvelles collections
        cherries = new ArrayList<Block>();
        superCherries = new ArrayList<Block>();
        powerPellets = new ArrayList<Block>();

        // Initialisations des timers
        cherryTimers = new ArrayList<Integer>();
        superCherryTimers = new ArrayList<Integer>();

        // Chargement de la carte
        loadMap();

        // Initialiser les directions des fantômes
        for (Block ghost : ghosts) {
            char randomDirection = directions[random.nextInt(4)];
            ghost.updateDirection(randomDirection);
        }

        // Boucle de jeu - Timer de 50ms = 20 FPS
        gameLoop = new Timer(50, this);
        gameLoop.start();

        // Activation de l'écoute clavier
        addKeyListener(this);
        setFocusable(true);

        System.out.println("Système de meilleur score initialisé!");
        System.out.println("Meilleur score actuel : " + bestScore);
        System.out.println("Scared Mode implémenté avec succès!");
        System.out.println("Carte du jeu chargée : " + tileMap.length + " lignes x " + tileMap[0].length() + " colonnes");
        System.out.println("Classe Block créée avec succès !");
        System.out.println("Structures de données initialisées !");
        System.out.println("Boucle de jeu démarrée (20 FPS) !");
        System.out.println("Contrôles clavier activés !");
    }

    // NOUVELLE MÉTHODE: Charger le meilleur score depuis le fichier
    private void loadBestScore() {
        try {
            if (Files.exists(Paths.get(bestScoreFilePath))) {
                String content = new String(Files.readAllBytes(Paths.get(bestScoreFilePath)));
                bestScore = Integer.parseInt(content.trim());
                System.out.println("Meilleur score chargé: " + bestScore);
            } else {
                bestScore = 0;
                System.out.println("Aucun fichier de score trouvé, meilleur score initialisé à 0");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du meilleur score: " + e.getMessage());
            bestScore = 0;
        } catch (NumberFormatException e) {
            System.err.println("Format de score invalide dans le fichier, remis à 0");
            bestScore = 0;
        }
    }

    // NOUVELLE MÉTHODE: Sauvegarder le meilleur score dans le fichier
    private void saveBestScore() {
        try {
            Files.write(Paths.get(bestScoreFilePath), String.valueOf(bestScore).getBytes());
            System.out.println("Nouveau meilleur score sauvegardé: " + bestScore);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du meilleur score: " + e.getMessage());
        }
    }

    // NOUVELLE MÉTHODE: Vérifier et mettre à jour le meilleur score
    private void checkAndUpdateBestScore() {
        if (score > bestScore) {
            bestScore = score;
            isNewRecord = true;
            saveBestScore();
            System.out.println("🏆 NOUVEAU RECORD! Score: " + score);
        } else {
            isNewRecord = false;
        }
    }

    private void loadImages() {
        // Chargement de l'image du mur
        wallImage = new ImageIcon(getClass().getResource("/images/wall.png")).getImage();

        // Chargement des images des fantômes
        blueGhostImage = new ImageIcon(getClass().getResource("/images/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/images/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/images/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/images/redGhost.png")).getImage();

        // Utiliser une image existante ou null pour le fantôme effrayé
        try {
            scaredGhostImage = new ImageIcon(getClass().getResource("/images/scaredGhost.png")).getImage();
        } catch (Exception e) {
            // Si l'image n'existe pas, utiliser l'image du fantôme bleu
            scaredGhostImage = blueGhostImage;
            System.out.println("Image scaredGhost.png non trouvée, utilisation de l'image bleue");
        }

        // Chargement des images de Pac-Man
        pacmanUpImage = new ImageIcon(getClass().getResource("/images/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/images/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/images/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/images/pacmanRight.png")).getImage();

        // Chargement des images de bonus avec gestion d'erreur
        try {
            cherryImage = new ImageIcon(getClass().getResource("/images/cherry.png")).getImage();
        } catch (Exception e) {
            cherryImage = null; // Sera dessiné comme un cercle rouge
            System.out.println("Image cherry.png non trouvée, affichage par défaut");
        }

        try {
            superCherryImage = new ImageIcon(getClass().getResource("/images/superCherry.png")).getImage();
        } catch (Exception e) {
            superCherryImage = null; // Sera dessiné comme un cercle rouge plus gros
            System.out.println("Image superCherry.png non trouvée, affichage par défaut");
        }

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
                else if (tileMapChar == ' ') { // Nourriture normale
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
                else if (tileMapChar == 'S') { // Super pastille
                    Block powerPellet = new Block(null, x + 8, y + 8, 16, 16);
                    powerPellets.add(powerPellet);
                }
                // '0' = case vide, on l'ignore
                // Les cerises seront générées aléatoirement par spawnCherry()
            }
        }

        // Tests de vérification
        System.out.println("LoadMap terminé !");
        System.out.println("Murs créés : " + walls.size());
        System.out.println("Fantômes créés : " + ghosts.size());
        System.out.println("Nourriture créée : " + foods.size());
        System.out.println("Super pastilles créées : " + powerPellets.size());
        System.out.println("Pac-Man créé : " + (pacman != null ? "Oui" : "Non"));

        // Spawn initial de quelques cerises aléatoires
        spawnInitialCherries();
    }

    // NOUVELLE MÉTHODE pour spawn initial de cerises aléatoires
    private void spawnInitialCherries() {
        // Spawn 5-8 cerises normales au début
        int cherryCount = 5 + random.nextInt(4); // Entre 5 et 8 cerises
        for (int i = 0; i < cherryCount; i++) {
            spawnRandomCherry(false); // false = cerise normale
        }

        // Spawn 2-3 super cerises au début
        int superCherryCount = 2 + random.nextInt(2); // Entre 2 et 3 super cerises
        for (int i = 0; i < superCherryCount; i++) {
            spawnRandomCherry(true); // true = super cerise
        }

        System.out.println("Cerises initiales spawned : " + cherries.size() + " normales, " + superCherries.size() + " super");
    }

    // MÉTHODE AMÉLIORÉE pour spawn aléatoire d'une cerise
    private void spawnRandomCherry(boolean isSuperCherry) {
        // Essayer de trouver une position libre
        for (int attempts = 0; attempts < 50; attempts++) {
            int randomX = random.nextInt(boardWidth / tileSize) * tileSize;
            int randomY = random.nextInt(boardHeight / tileSize) * tileSize;

            // Vérifier que la position est libre
            if (isPositionFree(randomX, randomY)) {
                if (isSuperCherry) {
                    Block superCherry = new Block(superCherryImage, randomX + 4, randomY + 4, 24, 24);
                    superCherries.add(superCherry);
                    superCherryTimers.add(cherryLifetime);
                } else {
                    Block cherry = new Block(cherryImage, randomX + 8, randomY + 8, 16, 16);
                    cherries.add(cherry);
                    cherryTimers.add(cherryLifetime);
                }
                break;
            }
        }
    }

    // NOUVELLE MÉTHODE pour vérifier si une position est libre
    private boolean isPositionFree(int x, int y) {
        // Vérifier les murs
        for (Block wall : walls) {
            if (wall.x == x && wall.y == y) {
                return false;
            }
        }

        // Vérifier les fantômes (zone de sécurité)
        for (Block ghost : ghosts) {
            if (Math.abs(ghost.x - x) < tileSize * 2 && Math.abs(ghost.y - y) < tileSize * 2) {
                return false;
            }
        }

        // Vérifier pac-man (zone de sécurité)
        if (pacman != null) {
            if (Math.abs(pacman.x - x) < tileSize * 3 && Math.abs(pacman.y - y) < tileSize * 3) {
                return false;
            }
        }

        // Vérifier les super pastilles existantes
        for (Block powerPellet : powerPellets) {
            if (Math.abs(powerPellet.x - (x + 8)) < tileSize && Math.abs(powerPellet.y - (y + 8)) < tileSize) {
                return false;
            }
        }

        // Vérifier les autres cerises (éviter le clustering)
        for (Block cherry : cherries) {
            if (Math.abs(cherry.x - (x + 8)) < tileSize && Math.abs(cherry.y - (y + 8)) < tileSize) {
                return false;
            }
        }

        for (Block superCherry : superCherries) {
            if (Math.abs(superCherry.x - (x + 4)) < tileSize && Math.abs(superCherry.y - (y + 4)) < tileSize) {
                return false;
            }
        }

        return true;
    }

    // MÉTHODE AMÉLIORÉE pour spawn périodique des cerises
    private void spawnCherry() {
        // Limiter le nombre de cerises simultanées
        if (cherries.size() + superCherries.size() >= 12) {
            return;
        }

        // 20% de chance d'avoir une super cerise
        boolean isSuperCherry = random.nextInt(100) < 20;
        spawnRandomCherry(isSuperCherry);

        System.out.println((isSuperCherry ? "Super cerise" : "Cerise normale") + " spawned périodiquement!");
    }

    // NOUVELLE MÉTHODE pour gérer la durée de vie des cerises
    private void updateCherryLifetimes() {
        // Gérer les cerises normales (toutes sont maintenant temporaires)
        for (int i = cherryTimers.size() - 1; i >= 0; i--) {
            int currentTime = cherryTimers.get(i);
            cherryTimers.set(i, currentTime - 1);
            if (cherryTimers.get(i) <= 0) {
                cherries.remove(i);
                cherryTimers.remove(i);
                System.out.println("Cerise expirée");
            }
        }

        // Gérer les super cerises (toutes sont maintenant temporaires)
        for (int i = superCherryTimers.size() - 1; i >= 0; i--) {
            int currentTime = superCherryTimers.get(i);
            superCherryTimers.set(i, currentTime - 1);
            if (superCherryTimers.get(i) <= 0) {
                superCherries.remove(i);
                superCherryTimers.remove(i);
                System.out.println("Super cerise expirée");
            }
        }
    }

    // NOUVELLE MÉTHODE pour activer les effets spéciaux des super cerises - SANS SPEED BOOST
    private void activateSuperCherryEffect() {
        int effect = random.nextInt(2); // 2 effets possibles (retiré speed boost)

        switch (effect) {
            case 0: // Invincibilité courte
                invincibility = true;
                invincibilityTimer = 200; // 10 secondes
                System.out.println("INVINCIBILITÉ activée!");
                break;
            case 1: // Double points temporaire (sera géré dans move())
                // Cet effet sera appliqué directement lors de la collecte
                System.out.println("DOUBLE POINTS pour cette super cerise!");
                break;
        }
    }

    // NOUVELLE MÉTHODE pour aligner Pac-Man sur la grille si nécessaire
    private void alignToGrid() {
        // Aligner Pac-Man sur la grille la plus proche
        int gridX = Math.round((float)pacman.x / tileSize) * tileSize;
        int gridY = Math.round((float)pacman.y / tileSize) * tileSize;

        // Seulement aligner si on est très proche (éviter les sauts brusques)
        if (Math.abs(pacman.x - gridX) <= 4) {
            pacman.x = gridX;
        }
        if (Math.abs(pacman.y - gridY) <= 4) {
            pacman.y = gridY;
        }
    }

    // Méthode resetPositions() modifiée pour le Scared Mode
    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;

        // Reset du power mode
        powerMode = false;
        powerModeTimer = 0;
        ghostEatenScore = 200;

        // Reset des effets spéciaux
        invincibility = false;
        invincibilityTimer = 0;

        // MODIFIÉ: Reset des fantômes avec gestion du Scared Mode
        for (int i = 0; i < ghosts.size(); i++) {
            Block ghost = ghosts.get(i);
            ghost.reset(); // Ceci va automatiquement désactiver le scared mode

            // Remettre les images originales
            if (i == 0) ghost.originalImage = blueGhostImage;
            else if (i == 1) ghost.originalImage = orangeGhostImage;
            else if (i == 2) ghost.originalImage = pinkGhostImage;
            else if (i == 3) ghost.originalImage = redGhostImage;

            ghost.image = ghost.originalImage;

            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);

        // MODIFIÉ: Affichage du score, des vies et du meilleur score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        if (gameOver) {
            // Affichage Game Over avec informations de score
            g.drawString("Game Over: " + score, tileSize/2, tileSize/2);

            // NOUVEAU: Afficher le meilleur score et si c'est un nouveau record
            if (isNewRecord) {
                g.setColor(Color.YELLOW);
                g.drawString("🏆 NOUVEAU RECORD! 🏆", tileSize/2, tileSize);
                g.setColor(Color.WHITE);
                g.drawString("Ancien record: " + (bestScore == score ? 0 : bestScore), tileSize/2, tileSize + 25);
            } else {
                g.drawString("Meilleur Score: " + bestScore, tileSize/2, tileSize);
                if (bestScore > 0) {
                    int difference = bestScore - score;
                    g.setColor(Color.CYAN);
                    g.drawString("Il vous manquait " + difference + " points!", tileSize/2, tileSize + 25);
                    g.setColor(Color.WHITE);
                }
            }

            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("Appuyez sur une touche pour rejouer", tileSize/2, tileSize * 2);

        } else {
            // Affichage pendant le jeu
            g.drawString("x" + lives + " Score: " + score, tileSize/2, tileSize/2);

            // NOUVEAU: Afficher le meilleur score en cours de partie
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("Record: " + bestScore, tileSize/2, tileSize/2 + 20);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));

            // Afficher le power mode
            if (powerMode) {
                g.setColor(Color.CYAN);
                g.drawString("POWER MODE: " + (powerModeTimer / 20), tileSize/2, tileSize);
            }
            // Afficher les effets spéciaux
            if (invincibility) {
                g.setColor(Color.MAGENTA);
                g.drawString("INVINCIBLE: " + (invincibilityTimer / 20), tileSize/2, tileSize * 3/2);
            }

            // NOUVEAU: Indicateur si on dépasse le record - EN HAUT BIEN VISIBLE
            if (score > bestScore && bestScore > 0) {
                // Effet clignotant pour le nouveau record
                if (System.currentTimeMillis() % 600 < 300) {
                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(new Color(255, 215, 0)); // Couleur or (RGB)
                }
                g.setFont(new Font("Arial", Font.BOLD, 16));

                // Centrer le texte
                String recordText = "🏆 NOUVEAU RECORD EN COURS! 🏆";
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(recordText);
                int centerX = (boardWidth - textWidth) / 2;

                g.drawString(recordText, centerX, tileSize * 2 + 10); // En haut, sous les infos de base

                // Ajouter un effet de fond
                g.setColor(new Color(255, 215, 0, 50)); // Or semi-transparent
                g.fillRect(centerX - 10, tileSize * 2 - 10, textWidth + 20, 25);
            }
            // NOUVEAU: Indicateur visuel si on approche du record
            else if (score > bestScore * 0.8 && bestScore > 0 && score < bestScore) {
                g.setColor(Color.ORANGE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                int remaining = bestScore - score;
                g.drawString("Record proche! Plus que " + remaining + " pts", tileSize/2, boardHeight - 30);
            }
        }
    }

    public void draw(Graphics g) {
        // Dessiner Pac-Man avec effet clignotant si invincible
        if (pacman != null) {
            if (invincibility && System.currentTimeMillis() % 200 < 100) {
                // Effet clignotant pendant l'invincibilité
                g.setColor(Color.YELLOW);
                g.fillOval(pacman.x + 2, pacman.y + 2, pacman.width - 4, pacman.height - 4);
            } else {
                g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
            }
        }

        // MODIFIÉ: Dessiner les fantômes avec effet clignotant en Scared Mode
        for (Block ghost : ghosts) {
            if (ghost.isScared && powerModeTimer < 100 && System.currentTimeMillis() % 200 < 100) {
                // Effet clignotant quand le power mode va finir
                // Alterner entre l'image scared et l'image normale
                g.drawImage(ghost.originalImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }

        // Dessiner les murs
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Dessiner la nourriture normale
        g.setColor(Color.YELLOW);
        for (Block food : foods) {
            g.fillOval(food.x, food.y, food.width, food.height);
        }

        // Dessiner les super pastilles (plus grosses et clignotantes)
        if (System.currentTimeMillis() % 500 < 250) { // Effet clignotant
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.YELLOW);
        }
        for (Block powerPellet : powerPellets) {
            g.fillOval(powerPellet.x, powerPellet.y, powerPellet.width, powerPellet.height);
        }

        // Dessiner les cerises avec clignotement avant expiration
        for (int i = 0; i < cherries.size(); i++) {
            Block cherry = cherries.get(i);
            int timeLeft = (i < cherryTimers.size()) ? cherryTimers.get(i) : cherryLifetime;

            // Faire clignoter les cerises qui vont expirer
            boolean shouldDraw = true;
            if (timeLeft < 100) { // Clignoter dans les 5 dernières secondes
                shouldDraw = (System.currentTimeMillis() % 400 < 200);
            }

            if (shouldDraw) {
                if (cherry.image != null) {
                    g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
                } else {
                    g.setColor(Color.RED);
                    g.fillOval(cherry.x, cherry.y, cherry.width, cherry.height);
                }
            }
        }

        // Dessiner les super cerises avec effets spéciaux
        for (int i = 0; i < superCherries.size(); i++) {
            Block superCherry = superCherries.get(i);
            int timeLeft = (i < superCherryTimers.size()) ? superCherryTimers.get(i) : cherryLifetime;

            // Faire clignoter les super cerises qui vont expirer
            boolean shouldDraw = true;
            if (timeLeft < 100) {
                shouldDraw = (System.currentTimeMillis() % 300 < 150); // Clignotement plus rapide
            }

            if (shouldDraw) {
                if (superCherry.image != null) {
                    g.drawImage(superCherry.image, superCherry.x, superCherry.y, superCherry.width, superCherry.height, null);
                } else {
                    // Affichage par défaut avec effet spécial
                    g.setColor(Color.RED);
                    g.fillOval(superCherry.x, superCherry.y, superCherry.width, superCherry.height);
                    g.setColor(Color.YELLOW);
                    g.drawOval(superCherry.x, superCherry.y, superCherry.width, superCherry.height);
                    // Ajouter une étoile au centre
                    g.setColor(Color.WHITE);
                    g.fillOval(superCherry.x + superCherry.width/2 - 2, superCherry.y + superCherry.height/2 - 2, 4, 4);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            gameLoop.stop();
        } else {
            move();
        }
        repaint();
    }

    public void move() {
        // GESTION DU TIMER POWER MODE - MODIFIÉ pour Scared Mode
        if (powerMode) {
            powerModeTimer--;
            if (powerModeTimer <= 0) {
                powerMode = false;
                ghostEatenScore = 200; // Reset du score fantôme

                // NOUVEAU: Désactiver le scared mode pour tous les fantômes
                for (Block ghost : ghosts) {
                    ghost.setScared(false);
                }
                System.out.println("Power Mode terminé - fantômes redevenus normaux");
            }
        }

        // GESTION DU SPAWN DES CERISES
        cherrySpawnTimer++;
        if (cherrySpawnTimer >= cherrySpawnInterval) {
            spawnCherry();
            cherrySpawnTimer = 0;
        }

        // Gestion de la durée de vie des cerises
        updateCherryLifetimes();

        // Gérer les effets spéciaux
        if (invincibility) {
            invincibilityTimer--;
            if (invincibilityTimer <= 0) {
                invincibility = false;
                System.out.println("Invincibilité terminée");
            }
        }

        // MOUVEMENT DE PAC-MAN - VERSION NORMALE (sans speed boost)
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Gestion du passage aux bords opposés (téléportation)
        if (pacman.x < 0) {
            pacman.x = boardWidth - tileSize;
        } else if (pacman.x >= boardWidth) {
            pacman.x = 0;
        }

        if (pacman.y < 0) {
            pacman.y = boardHeight - tileSize;
        } else if (pacman.y >= boardHeight) {
            pacman.y = 0;
        }

        // Vérification des collisions avec les murs - VERSION SIMPLIFIÉE
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                // Reculer du mouvement normal
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // COLLISION AVEC LA NOURRITURE NORMALE
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
                break;
            }
        }
        if (foodEaten != null) {
            foods.remove(foodEaten);
        }

        // COLLISION AVEC LES SUPER PASTILLES - MODIFIÉ pour Scared Mode
        Block powerPelletEaten = null;
        for (Block powerPellet : powerPellets) {
            if (collision(pacman, powerPellet)) {
                powerPelletEaten = powerPellet;
                score += 50;
                // Activer le mode power
                powerMode = true;
                powerModeTimer = 300; // 15 secondes à 20 FPS
                ghostEatenScore = 200; // Reset du score fantôme

                // NOUVEAU: Activer le scared mode pour tous les fantômes
                for (Block ghost : ghosts) {
                    ghost.setScared(true);
                }
                System.out.println("POWER MODE activé - Fantômes effrayés!");
                break;
            }
        }
        if (powerPelletEaten != null) {
            powerPellets.remove(powerPelletEaten);
        }

        // COLLISION AVEC LES CERISES
        Block cherryEaten = null;
        int cherryIndex = -1;
        for (int i = 0; i < cherries.size(); i++) {
            Block cherry = cherries.get(i);
            if (collision(pacman, cherry)) {
                cherryEaten = cherry;
                cherryIndex = i;
                score += 50;
                break;
            }
        }
        if (cherryEaten != null) {
            cherries.remove(cherryEaten);
            if (cherryIndex < cherryTimers.size()) {
                cherryTimers.remove(cherryIndex);
            }
        }

        // COLLISION AVEC LES SUPER CERISES - EFFETS SIMPLIFIÉS
        Block superCherryEaten = null;
        int superCherryIndex = -1;
        for (int i = 0; i < superCherries.size(); i++) {
            Block superCherry = superCherries.get(i);
            if (collision(pacman, superCherry)) {
                superCherryEaten = superCherry;
                superCherryIndex = i;

                // Effet double points (effet #1)
                if (random.nextInt(2) == 1) {
                    score += 200; // Double points
                    System.out.println("DOUBLE POINTS! +200");
                } else {
                    score += 100;
                    activateSuperCherryEffect(); // Invincibilité uniquement
                }
                break;
            }
        }
        if (superCherryEaten != null) {
            superCherries.remove(superCherryEaten);
            if (superCherryIndex < superCherryTimers.size()) {
                superCherryTimers.remove(superCherryIndex);
            }
        }

        // Vérification fin de niveau (toute nourriture + super pastilles mangées OU tous les fantômes éliminés)
        if ((foods.isEmpty() && powerPellets.isEmpty()) || ghosts.isEmpty()) {
            if (ghosts.isEmpty()) {
                System.out.println("Tous les fantômes éliminés! Passage au niveau suivant.");
                score += 1000; // Bonus pour avoir éliminé tous les fantômes
            }
            loadMap();
            resetPositions();
            // Vider les bonus temporaires
            cherries.clear();
            superCherries.clear();
            cherryTimers.clear();
            superCherryTimers.clear();
        }

        // MOUVEMENT DES FANTÔMES - MODIFIÉ pour Scared Mode
        for (Block ghost : ghosts) {
            // NOUVEAU: Comportement différent selon l'état scared
            if (ghost.isScared) {
                // Tous les 10 frames, essayer de fuir Pac-Man
                if (random.nextInt(10) == 0) {
                    ghost.fleeFromPacman();
                }
            } else {
                // Comportement normal: changement de direction aléatoire occasionnel
                if (random.nextInt(50) == 0) { // Changement moins fréquent
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            // Empêcher les fantômes de sortir de l'écran
            if (ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                ghost.velocityX = -ghost.velocityX;
            }

            // Empêcher les fantômes de rester bloqués sur la ligne centrale
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }

            // Vérification des collisions avec les murs
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;

                    // MODIFIÉ: Comportement différent selon scared mode
                    if (ghost.isScared) {
                        // En scared mode, essayer de fuir même après collision
                        ghost.fleeFromPacman();
                    } else {
                        // Comportement normal
                        char newDirection = directions[random.nextInt(4)];
                        ghost.updateDirection(newDirection);
                    }
                    break;
                }
            }
        }

        // COLLISION AVEC LES FANTÔMES - MODIFIÉ pour suppression définitive
        for (int i = ghosts.size() - 1; i >= 0; i--) { // Parcourir à l'envers pour éviter les problèmes d'index
            Block ghost = ghosts.get(i);
            if (collision(pacman, ghost)) {
                if (ghost.isScared) {
                    // NOUVEAU: Pac-Man mange le fantôme effrayé - SUPPRESSION DÉFINITIVE
                    score += ghostEatenScore;
                    ghostEatenScore *= 2; // Double le score pour le prochain fantôme (200, 400, 800, 1600)

                    // Supprimer définitivement le fantôme de la liste
                    ghosts.remove(i);

                    System.out.println("Fantôme supprimé définitivement! +" + (ghostEatenScore/2) + " points. Fantômes restants: " + ghosts.size());
                } else if (invincibility) {
                    // Si seulement invincible, pas de dégâts mais pas de points
                    // Rien ne se passe
                } else {
                    // Comportement normal : perte de vie
                    lives--;
                    if (lives == 0) {
                        gameOver = true;
                        // NOUVEAU: Vérifier et mettre à jour le meilleur score quand le jeu se termine
                        checkAndUpdateBestScore();
                        return;
                    } else {
                        resetPositions();
                    }
                }
                break;
            }
        }

        // Appliquer l'alignement sur la grille si nécessaire (optionnel)
        // alignToGrid(); // Décommentez cette ligne si vous voulez forcer l'alignement
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Réinitialisation du jeu si Game Over
        if (gameOver) {
            walls.clear();
            ghosts.clear();
            foods.clear();
            powerPellets.clear();
            cherries.clear();
            superCherries.clear();
            cherryTimers.clear();
            superCherryTimers.clear();

            loadMap();
            resetPositions();
            score = 0;
            lives = 3;
            gameOver = false;
            powerMode = false;
            powerModeTimer = 0;
            cherrySpawnTimer = 0;
            invincibility = false;
            invincibilityTimer = 0;
            // NOUVEAU: Reset du flag nouveau record
            isNewRecord = false;
            gameLoop.start();
            return;
        }

        if (pacman == null) return;

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
            if (pacman.direction == 'U') {
                pacman.image = pacmanUpImage;
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
            if (pacman.direction == 'D') {
                pacman.image = pacmanDownImage;
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
            if (pacman.direction == 'L') {
                pacman.image = pacmanLeftImage;
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
            if (pacman.direction == 'R') {
                pacman.image = pacmanRightImage;
            }
        }
    }
}