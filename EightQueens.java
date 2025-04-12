import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EightQueens extends JFrame {
    private static final int BOARD_SIZE = 8;
    private JPanel chessBoard;
    private JButton solveBacktrackingBtn;
    private JButton solveConstrainedBtn;
    private JButton resetBtn;
    private JLabel statusLabel;
    private Timer animationTimer;
    private boolean isProcessing = false;

    private int[] backtrackingSolution;
    private int[] constrainedSolution;
    private int backtrackingSteps = 0;
    private int constrainedAttempts = 0;

    public EightQueens() {
        setTitle("8-Queens Problem");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600); 
        setLayout(new BorderLayout());

        initializeComponents();
        setupLayout();
        resetBoard();
    }

    private void initializeComponents() {
        chessBoard = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        chessBoard.setPreferredSize(new Dimension(400, 400));

        solveBacktrackingBtn = new JButton("Backtracking");
        solveConstrainedBtn = new JButton("Las Vegas");
        resetBtn = new JButton("Reset");

        statusLabel = new JLabel("Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        animationTimer = new Timer(100, e -> {
            String text = statusLabel.getText();
            if (text.contains("...")) {
                statusLabel.setText(text.substring(0, text.indexOf("...")));
            } else {
                statusLabel.setText(text + ".");
            }
        });

        solveBacktrackingBtn.addActionListener(e -> {
            if (!isProcessing) {
                startProcessing("Backtracking");
                new Thread(this::solveWithBacktracking).start();
            }
        });

        solveConstrainedBtn.addActionListener(e -> {
            if (!isProcessing) {
                startProcessing("Las Vegas");
                new Thread(this::solveWithConstrained).start();
            }
        });

        resetBtn.addActionListener(e -> {
            if (!isProcessing) resetBoard();
        });
    }

    private void startProcessing(String algorithm) {
        isProcessing = true;
        solveBacktrackingBtn.setEnabled(false);
        solveConstrainedBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        statusLabel.setText(algorithm + " running...");
        animationTimer.start();
    }

    private void endProcessing() {
        SwingUtilities.invokeLater(() -> {
            isProcessing = false;
            solveBacktrackingBtn.setEnabled(true);
            solveConstrainedBtn.setEnabled(true);
            resetBtn.setEnabled(true);
            animationTimer.stop();
        });
    }

    private void setupLayout() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(solveBacktrackingBtn);
        buttonPanel.add(solveConstrainedBtn);
        buttonPanel.add(resetBtn);

        add(chessBoard, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void resetBoard() {
        backtrackingSolution = null;
        constrainedSolution = null;
        backtrackingSteps = 0;
        constrainedAttempts = 0;
        statusLabel.setText("Ready");
        drawEmptyBoard();
    }

    private void drawEmptyBoard() {
        SwingUtilities.invokeLater(() -> {
            chessBoard.removeAll();
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    JPanel square = new JPanel(new BorderLayout());
                    square.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY);
                    chessBoard.add(square);
                }
            }
            chessBoard.revalidate();
            chessBoard.repaint();
        });
    }

    private void drawSolution(int[] solution, String algorithm) {
        SwingUtilities.invokeLater(() -> {
            chessBoard.removeAll();
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    JPanel square = new JPanel(new BorderLayout());
                    square.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY);

                    if (solution != null && col < solution.length && solution[col] == row) {
                        JLabel queen = new JLabel("♕", SwingConstants.CENTER);
                        queen.setFont(new Font("Serif", Font.PLAIN, 30));
                        queen.setForeground(algorithm.equals("Backtracking") ? Color.RED : new Color(0, 150, 0));
                        square.add(queen);
                    }
                    chessBoard.add(square);
                }
            }
            chessBoard.revalidate();
            chessBoard.repaint();
        });
    }

    private void solveWithBacktracking() {
        long startTime = System.currentTimeMillis();
        backtrackingSolution = new int[BOARD_SIZE];
        backtrackingSteps = 0;
        
        boolean solved = solveBacktracking(0);
        long duration = System.currentTimeMillis() - startTime;
        
        SwingUtilities.invokeLater(() -> {
            if (solved) {
                statusLabel.setText(String.format(
                    "Backtracking: %d steps in %d ms", 
                    backtrackingSteps, duration
                ));
                drawSolution(backtrackingSolution, "Backtracking");
            } else {
                statusLabel.setText("Backtracking failed (should never happen)");
            }
            endProcessing();
        });
    }

    // ACTUAL ALGORITHM HERE - Recursive Backtracking, checks all possible squares and
    // backtracks if none are found in the current column
    private boolean solveBacktracking(int col) {
        backtrackingSteps++;
        if (col >= BOARD_SIZE) return true;

        for (int row = 0; row < BOARD_SIZE; row++) {
            if (isSafe(row, col, backtrackingSolution)) {
                backtrackingSolution[col] = row;
                
                final int[] partialSolution = new int[col + 1];
                System.arraycopy(backtrackingSolution, 0, partialSolution, 0, col + 1);
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(String.format("Backtracking: Placing queen %d/%d", col+1, BOARD_SIZE));
                    drawSolution(partialSolution, "Backtracking");
                });

                try { Thread.sleep(50); } catch (InterruptedException e) { return false; }

                if (solveBacktracking(col + 1)) return true;
            }
        }
        return false;
    }

    private void solveWithConstrained() {
        long startTime = System.currentTimeMillis();
        constrainedAttempts++;
        
        int[] solution = constrainedLasVegas();
        long duration = System.currentTimeMillis() - startTime;
        
        SwingUtilities.invokeLater(() -> {
            if (solution != null) {
                statusLabel.setText(String.format(
                    "Las Vegas: Solved in %d attempts (%d ms)",
                    constrainedAttempts, duration
                ));
                constrainedSolution = solution;
                drawSolution(constrainedSolution, "Las Vegas");
            } else {
                statusLabel.setText(String.format(
                    "Las Vegas: Failed attempt %d, retrying...",
                    constrainedAttempts
                ));
                solveWithConstrained();
                return;
            }
            endProcessing();
        });
    }

    // Checks if current block is safe
    private boolean isSafe(int row, int col, int[] solution) {
        for (int i = 0; i < col; i++) {
            if (solution[i] == row || Math.abs(solution[i] - row) == Math.abs(i - col)) {
                return false;
            }
        }
        return true;
    }

    // ACTUAL ALGORITHM HERE - Non-Deterministic, randomly puts a queen on any
    // safe block of current column
    private int[] constrainedLasVegas() {
        Random rand = new Random();
        int[] queens = new int[BOARD_SIZE];
        for (int col = 0; col < BOARD_SIZE; col++) {
            List<Integer> safeRows = getSafeRows(col, queens);
            if (safeRows.isEmpty()) return null;
            queens[col] = safeRows.get(rand.nextInt(safeRows.size()));
            
            final int currentCol = col;
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(String.format("Las Vegas: Placing queen %d/%d", currentCol+1, BOARD_SIZE));
            });
            try {Thread.sleep(50);} catch (InterruptedException e) {return null;}
        }
        return queens;
    }

    private List<Integer> getSafeRows(int col, int[] queens) {
        List<Integer> safeRows = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            boolean safe = true;
            for (int i = 0; i < col; i++) {
                if (queens[i] == row || Math.abs(queens[i] - row) == Math.abs(i - col)) {
                    safe = false;
                    break;
                }
            }
            if (safe) safeRows.add(row);
        }
        return safeRows;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EightQueens visualizer = new EightQueens();
            visualizer.setVisible(true);
        });
    }
}