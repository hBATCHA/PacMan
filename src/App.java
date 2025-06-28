import javax.swing.JFrame;

public class App {
    public static void main(String[] args) {
        // Définition de la carte selon le document
        int rowCount = 21;        // Nombre de lignes (indexées de 0 à 20)
        int columnCount = 19;     // Nombre de colonnes (indexées de 0 à 18)
        int tileSize = 32;        // Taille d'une tuile : 32 x 32 pixels

        // Calcul de la taille de la fenêtre
        int boardWidth = columnCount * tileSize;   // 19 × 32 = 608 pixels
        int boardHeight = rowCount * tileSize;     // 21 × 32 = 672 pixels

        // Création de la fenêtre JFrame
        JFrame frame = new JFrame("Pac-Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);  // Centre la fenêtre sur l'écran
        frame.setResizable(false);          // Empêche de redimensionner
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ferme le programme

        // Création et ajout du panneau de jeu
        PacMan game = new PacMan(boardWidth, boardHeight);
        frame.add(game);

        frame.setVisible(true);             // Affiche la fenêtre

        // Force le focus sur le panneau pour les contrôles clavier
        game.requestFocus();

        System.out.println("Étape 3 : Panneau de jeu créé et intégré !");
    }
}