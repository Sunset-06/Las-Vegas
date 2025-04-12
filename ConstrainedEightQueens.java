import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ConstrainedEightQueens extends JFrame {
    private static final int BOARD_SIZE = 8;
    private JPanel chessBoard;
    private JTextArea outputArea;
    private JButton solveBacktrackingBtn;
    private JButton solveConstrainedBtn;
    private JButton resetBtn;
    private JLabel statsLabel;

    private int[] backtrackingSolution;
    private int[] constrainedSolution;
    private int backtrackingSteps = 0;
    private int constrainedAttempts = 0;

    public ConstrainedEightQueens() {
        setTitle("8 Queens: Backtracking vs Constrained Las Vegas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        initializeComponents();
        setupLayout();

        resetBoard();
    }

    private void initializeComponents() {
        chessBoard = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        chessBoard.setPreferredSize(new Dimension(400, 400));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        solveBacktrackingBtn = new JButton("Backtracking");
        solveConstrainedBtn = new JButton("Constrained LV");
        resetBtn = new JButton("Reset");

        statsLabel = new JLabel("Attempts: Backtracking - 0 | Constrained - 0");
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        solveBacktrackingBtn.addActionListener(e -> solveWithBacktracking());
        solveConstrainedBtn.addActionListener(e -> solveWithConstrained());
        resetBtn.addActionListener(e -> resetBoard());
    }

    private void setupLayout() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(solveBacktrackingBtn);
        buttonPanel.add(solveConstrainedBtn);
        buttonPanel.add(resetBtn);

        add(chessBoard, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.NORTH);
        add(statsLabel, BorderLayout.SOUTH);

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(new JLabel("Algorithm Output:"), BorderLayout.NORTH);
        eastPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(eastPanel, BorderLayout.EAST);
    }

    private void resetBoard() {
        backtrackingSolution = null;
        constrainedSolution = null;
        backtrackingSteps = 0;
        constrainedAttempts = 0;
        updateStatsLabel();
        outputArea.setText("");
        drawEmptyBoard();
    }

    private void drawEmptyBoard() {
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
    }

    private void drawSolution(int[] solution, String algorithm) {
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
    }

    private void solveWithBacktracking() {
        outputArea.append("Starting Backtracking...\n");
        solveBacktrackingBtn.setEnabled(false);
        solveConstrainedBtn.setEnabled(false);

        new BacktrackingWorker().execute();
    }

    private void solveWithConstrained() {
        outputArea.append("Starting Constrained Las Vegas...\n");
        solveBacktrackingBtn.setEnabled(false);
        solveConstrainedBtn.setEnabled(false);

        new ConstrainedWorker().execute();
    }

    private void updateStatsLabel() {
        statsLabel.setText(String.format(
            "Attempts: Backtracking - %d | Constrained - %d",
            backtrackingSteps, constrainedAttempts
        ));
    }

    private class BacktrackingWorker extends SwingWorker<Boolean, int[]> {
        private long startTime;
        private int[] solution;

        @Override
        protected Boolean doInBackground() {
            startTime = System.currentTimeMillis();
            solution = new int[BOARD_SIZE];
            backtrackingSteps = 0;
            return solveBacktracking(0);
        }

        private boolean solveBacktracking(int col) {
            backtrackingSteps++;
            if (col >= BOARD_SIZE) return true;

            for (int row = 0; row < BOARD_SIZE; row++) {
                if (isSafe(row, col, solution)) {
                    solution[col] = row;
                    int[] partial = new int[col + 1];
                    System.arraycopy(solution, 0, partial, 0, col + 1);
                    publish(partial);
                    
                    try { Thread.sleep(200); } 
                    catch (InterruptedException e) { return false; }

                    if (solveBacktracking(col + 1)) return true;
                }
            }
            return false;
        }

        @Override
        protected void process(List<int[]> chunks) {
            drawSolution(chunks.get(chunks.size() - 1), "Backtracking");
        }

        @Override
        protected void done() {
            try {
                boolean success = get();
                long duration = System.currentTimeMillis() - startTime;
                if (success) {
                    outputArea.append(String.format(
                        "Backtracking solved in %d steps (%d ms)\n", 
                        backtrackingSteps, duration
                    ));
                    backtrackingSolution = solution;
                    drawSolution(backtrackingSolution, "Backtracking");
                } else {
                    outputArea.append("Backtracking failed (should never happen)\n");
                }
            } catch (Exception e) {
                outputArea.append("Backtracking interrupted\n");
            } finally {
                enableButtons();
                updateStatsLabel();
            }
        }
    }

    private class ConstrainedWorker extends SwingWorker<int[], Void> {
        private long startTime;

        @Override
        protected int[] doInBackground() {
            startTime = System.currentTimeMillis();
            constrainedAttempts++;
            return constrainedLasVegas();
        }

        @Override
        protected void done() {
            try {
                int[] solution = get();
                long duration = System.currentTimeMillis() - startTime;
                if (solution != null && solution.length == BOARD_SIZE) {
                    outputArea.append(String.format(
                        "Constrained LV solved in %d attempts (%d ms)\n",
                        constrainedAttempts, duration
                    ));
                    constrainedSolution = solution;
                    drawSolution(constrainedSolution, "Constrained");
                } else {
                    outputArea.append(String.format(
                        "Constrained LV failed (attempt %d), retrying...\n",
                        constrainedAttempts
                    ));
                    solveWithConstrained();
                }
            } catch (Exception e) {
                outputArea.append("Constrained LV interrupted\n");
            } finally {
                updateStatsLabel();
            }
        }
    }

    private boolean isSafe(int row, int col, int[] solution) {
        for (int i = 0; i < col; i++) {
            if (solution[i] == row || 
                Math.abs(solution[i] - row) == Math.abs(i - col)) {
                return false;
            }
        }
        return true;
    }

    private int[] constrainedLasVegas() {
        Random rand = new Random();
        int[] queens = new int[BOARD_SIZE];
        for (int col = 0; col < BOARD_SIZE; col++) {
            List<Integer> safeRows = getSafeRows(col, queens);
            if (safeRows.isEmpty()) {
                return null;
            }
            queens[col] = safeRows.get(rand.nextInt(safeRows.size()));
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
            if (safe) {
                safeRows.add(row);
            }
        }
        return safeRows;
    }

    private void enableButtons() {
        solveBacktrackingBtn.setEnabled(true);
        solveConstrainedBtn.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConstrainedEightQueens visualizer = new ConstrainedEightQueens();
            visualizer.setVisible(true);
        });
    }
}