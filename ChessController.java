import javax.swing.*;
import java.util.List;
import java.util.Random;

public class ChessController {
    private ChessModel model;
    private ChessView view;
    private boolean isProcessing = false;

    public ChessController(ChessModel model, ChessView view) {
        this.model = model;
        this.view = view;
        initializeController();
    }

    private void initializeController() {
        view.drawEmptyBoard();
        view.updateExplanation(model.getAlgorithmExplanation());
        
        view.getSolveBacktrackingBtn().addActionListener(e -> {
            if (!isProcessing) {
                startProcessing("Backtracking");
                new Thread(this::solveWithBacktracking).start();
            }
        });

        view.getSolveConstrainedBtn().addActionListener(e -> {
            if (!isProcessing) {
                startProcessing("Las Vegas");
                new Thread(this::solveWithConstrained).start();
            }
        });

        view.getResetBtn().addActionListener(e -> {
            if (!isProcessing) {
                model.reset();
                view.drawEmptyBoard();
                view.getStatusLabel().setText("Ready");
                view.updateExplanation(model.getAlgorithmExplanation());
            }
        });
    }

    private void startProcessing(String algorithm) {
        isProcessing = true;
        model.setCurrentAlgorithm(algorithm);
        view.getSolveBacktrackingBtn().setEnabled(false);
        view.getSolveConstrainedBtn().setEnabled(false);
        view.getResetBtn().setEnabled(false);
        view.getStatusLabel().setText(algorithm + " running...");
        view.getAnimationTimer().start();
        view.updateExplanation(model.getAlgorithmExplanation());
    }

    private void endProcessing() {
        SwingUtilities.invokeLater(() -> {
            isProcessing = false;
            view.getSolveBacktrackingBtn().setEnabled(true);
            view.getSolveConstrainedBtn().setEnabled(true);
            view.getResetBtn().setEnabled(true);
            view.getAnimationTimer().stop();
            view.updateExplanation(model.getAlgorithmExplanation());
        });
    }

    private void solveWithBacktracking() {
        long startTime = System.currentTimeMillis();
        model.reset();
        model.setCurrentAlgorithm("Backtracking");
        model.setBacktrackingSolution(new int[model.getBoardSize()]);
        
        boolean solved = solveBacktracking(0);
        long duration = System.currentTimeMillis() - startTime;
        
        SwingUtilities.invokeLater(() -> {
            if (solved) {
                view.getStatusLabel().setText(String.format(
                    "Backtracking: %d steps in %d ms", 
                    model.getBacktrackingSteps(), duration
                ));
                view.drawSolution(model.getBacktrackingSolution(), "Backtracking");
            } else {
                view.getStatusLabel().setText("Backtracking failed (should never happen)");
            }
            view.updateExplanation(model.getAlgorithmExplanation());
            endProcessing();
        });
    }

    private boolean solveBacktracking(int col) {
        model.incrementBacktrackingSteps();
        model.setCurrentStep(col + 1);
        
        SwingUtilities.invokeLater(() -> {
            view.updateExplanation(model.getAlgorithmExplanation());
        });

        if (col >= model.getBoardSize()) return true;

        for (int row = 0; row < model.getBoardSize(); row++) {
            if (model.isSafe(row, col, model.getBacktrackingSolution())) {
                model.getBacktrackingSolution()[col] = row;
                
                final int[] partialSolution = new int[model.getBoardSize()];
                System.arraycopy(model.getBacktrackingSolution(), 0, partialSolution, 0, model.getBoardSize());
                for (int i = col + 1; i < model.getBoardSize(); i++) {
                    partialSolution[i] = -1;
                }
                
                SwingUtilities.invokeLater(() -> {
                    view.getStatusLabel().setText(String.format("Backtracking: Placing queen %d/%d", col+1, model.getBoardSize()));
                    view.drawSolution(partialSolution, "Backtracking");
                    view.updateExplanation(model.getAlgorithmExplanation());
                });

                try { Thread.sleep(50); } catch (InterruptedException e) { return false; }

                if (solveBacktracking(col + 1)) return true;
            }
        }
        return false;
    }

    private void solveWithConstrained() {
        long startTime = System.currentTimeMillis();
        model.incrementConstrainedAttempts();
        model.setCurrentAlgorithm("Las Vegas");
        model.setCurrentStep(model.getConstrainedAttempts());
        
        SwingUtilities.invokeLater(() -> {
            view.updateExplanation(model.getAlgorithmExplanation());
        });
        
        int[] solution = constrainedLasVegas();
        long duration = System.currentTimeMillis() - startTime;
        
        SwingUtilities.invokeLater(() -> {
            if (solution != null) {
                view.getStatusLabel().setText(String.format(
                    "Las Vegas: Solved in %d attempts (%d ms)",
                    model.getConstrainedAttempts(), duration
                ));
                model.setConstrainedSolution(solution);
                view.drawSolution(model.getConstrainedSolution(), "Las Vegas");
                view.updateExplanation(model.getAlgorithmExplanation());
                endProcessing();
            } else {
                view.getStatusLabel().setText(String.format(
                    "Las Vegas: Failed attempt %d, retrying...",
                    model.getConstrainedAttempts()
                ));
                view.updateExplanation(model.getAlgorithmExplanation());
                solveWithConstrained();
            }
        });
    }

    private int[] constrainedLasVegas() {
        Random rand = new Random();
        int[] queens = new int[model.getBoardSize()];
        for (int col = 0; col < model.getBoardSize(); col++) {
            List<Integer> safeRows = model.getSafeRows(col, queens);
            if (safeRows.isEmpty()) return null;
            queens[col] = safeRows.get(rand.nextInt(safeRows.size()));
            
            final int currentCol = col;
            SwingUtilities.invokeLater(() -> {
                view.getStatusLabel().setText(String.format("Las Vegas: Placing queen %d/%d", currentCol+1, model.getBoardSize()));
                view.updateExplanation(model.getAlgorithmExplanation());
            });
            try {Thread.sleep(50);} catch (InterruptedException e) {return null;}
        }
        return queens;
    }
}