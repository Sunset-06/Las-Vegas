import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class ChessView extends JFrame {
    private JPanel chessBoard;
    private JPanel mainPanel;
    private JPanel sidePanel;
    private JTextArea explanationArea;
    private JButton solveBacktrackingBtn;
    private JButton solveConstrainedBtn;
    private JButton resetBtn;
    private JLabel statusLabel;
    private Timer animationTimer;

    // Chess colors
    private final Color lightSquare = new Color(240, 217, 181);
    private final Color darkSquare = new Color(181, 136, 99);
    //private final Color velvetColor = new Color(30, 30, 60);
    //private final Color highlightLight = new Color(247, 247, 105);
    //private final Color highlightDark = new Color(187, 187, 53);

    public ChessView() {
        setTitle("8-Queens Problem - Chess Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700); // Increased width for side panel
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        // Main chess board with external labels
        chessBoard = new JPanel(new GridLayout(8, 8, 0, 0));
        chessBoard.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));
        chessBoard.setPreferredSize(new Dimension(500, 500));
        chessBoard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 100), 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        // Side panel for explanations
        sidePanel = new JPanel(new BorderLayout());
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        sidePanel.setPreferredSize(new Dimension(300, 500));
        sidePanel.setBackground(new Color(245, 245, 245));

        explanationArea = new JTextArea();
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("Arial", Font.PLAIN, 14));
        explanationArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        explanationArea.setText("Welcome to the 8-Queens Visualizer!\n\n" +
                "This program demonstrates two algorithms:\n" +
                "1. Backtracking (systematic search)\n" +
                "2. Las Vegas (randomized)\n\n" +
                "Click a button to see them in action!");

        JScrollPane scrollPane = new JScrollPane(explanationArea);
        sidePanel.add(scrollPane, BorderLayout.CENTER);

        // Control buttons
        solveBacktrackingBtn = new JButton("Backtracking");
        solveConstrainedBtn = new JButton("Las Vegas");
        resetBtn = new JButton("Reset");

        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        solveBacktrackingBtn.setFont(buttonFont);
        solveConstrainedBtn.setFont(buttonFont);
        resetBtn.setFont(buttonFont);

        styleButton(solveBacktrackingBtn);
        styleButton(solveConstrainedBtn);
        styleButton(resetBtn);

        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(245, 245, 220));

        animationTimer = new Timer(100, e -> {
            String text = statusLabel.getText();
            if (text.contains("...")) {
                statusLabel.setText(text.substring(0, text.indexOf("...")));
            } else {
                statusLabel.setText(text + ".");
            }
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private void setupLayout() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        /* mainPanel.setOpaque(true);
        mainPanel.setBackground(velvetColor); */

        // Create a panel for the chessboard with external labels
        JPanel boardContainer = new JPanel(new BorderLayout());
        
        // Add column labels (A-H) at the bottom
        JPanel colLabelPanel = new JPanel(new GridLayout(1, 8));
        colLabelPanel.setBorder(new EmptyBorder(5, 40, 5, 20)); // Match left padding with chessboard
        for (int col = 0; col < 8; col++) {
            JLabel label = new JLabel(String.valueOf((char)('A' + col)), SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            colLabelPanel.add(label);
        }
        
        // Add row labels (1-8) on the left
        JPanel rowLabelPanel = new JPanel(new GridLayout(8, 1));
        rowLabelPanel.setPreferredSize(new Dimension(30, 0));
        for (int row = 0; row < 8; row++) {
            JLabel label = new JLabel(String.valueOf(8 - row), SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            rowLabelPanel.add(label);
        }

        boardContainer.add(colLabelPanel, BorderLayout.SOUTH);
        boardContainer.add(rowLabelPanel, BorderLayout.WEST);
        boardContainer.add(chessBoard, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(solveBacktrackingBtn);
        buttonPanel.add(solveConstrainedBtn);
        buttonPanel.add(resetBtn);

        // Main layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(boardContainer, BorderLayout.CENTER);
        centerPanel.add(sidePanel, BorderLayout.EAST);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(statusLabel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    public void updateExplanation(String text) {
        explanationArea.setText(text);
    }

    public void drawEmptyBoard() {
        chessBoard.removeAll();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel square = new JPanel(new BorderLayout());
                boolean isLight = (row + col) % 2 == 0;
                square.setBackground(isLight ? lightSquare : darkSquare);
                chessBoard.add(square);
            }
        }
        chessBoard.revalidate();
        chessBoard.repaint();
    }

    public void drawSolution(int[] solution, String algorithm) {
        chessBoard.removeAll();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel square = new JPanel(new BorderLayout());
                boolean isLight = (row + col) % 2 == 0;
                square.setBackground(isLight ? lightSquare : darkSquare);

                if (solution != null && col < solution.length && solution[col] == row) {
                    JLabel queen = new JLabel("♛", SwingConstants.CENTER);
                    queen.setFont(new Font("Serif", Font.PLAIN, 40));
                    queen.setForeground(algorithm.equals("Backtracking") ? new Color(200, 0, 0) : new Color(0, 100, 0));
                    square.add(queen);
                }
                chessBoard.add(square);
            }
        }
        chessBoard.revalidate();
        chessBoard.repaint();
    }

    // Getters for UI components
    public JButton getSolveBacktrackingBtn() { return solveBacktrackingBtn; }
    public JButton getSolveConstrainedBtn() { return solveConstrainedBtn; }
    public JButton getResetBtn() { return resetBtn; }
    public JLabel getStatusLabel() { return statusLabel; }
    public Timer getAnimationTimer() { return animationTimer; }
    public JTextArea getExplanationArea() { return explanationArea; }
}