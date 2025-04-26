import java.util.ArrayList;
import java.util.List;

public class ChessModel {
    private static final int BOARD_SIZE = 8;
    private int[] backtrackingSolution;
    private int[] constrainedSolution;
    private int backtrackingSteps;
    private int constrainedAttempts;
    private String currentAlgorithm;
    private int currentStep;

    public ChessModel() {
        reset();
    }

    public void reset() {
        backtrackingSolution = null;
        constrainedSolution = null;
        backtrackingSteps = 0;
        constrainedAttempts = 0;
        currentAlgorithm = null;
        currentStep = 0;
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    // Getters and setters
    public int[] getBacktrackingSolution() { return backtrackingSolution; }
    public void setBacktrackingSolution(int[] solution) { this.backtrackingSolution = solution; }
    
    public int[] getConstrainedSolution() { return constrainedSolution; }
    public void setConstrainedSolution(int[] solution) { this.constrainedSolution = solution; }
    
    public int getBacktrackingSteps() { return backtrackingSteps; }
    public void incrementBacktrackingSteps() { backtrackingSteps++; }
    
    public int getConstrainedAttempts() { return constrainedAttempts; }
    public void incrementConstrainedAttempts() { constrainedAttempts++; }

    public String getCurrentAlgorithm() { return currentAlgorithm; }
    public void setCurrentAlgorithm(String algorithm) { this.currentAlgorithm = algorithm; }
    
    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int step) { this.currentStep = step; }

    public String getAlgorithmExplanation() {
        if (currentAlgorithm == null) {
            return "Select an algorithm to begin visualization.";
        }
        
        switch (currentAlgorithm) {
            case "Backtracking":
                return String.format("Backtracking Algorithm (Step %d):\n\n" +
                        "1. Places queens column by column\n" +
                        "2. For each column, tries every row\n" +
                        "3. If safe, proceeds to next column\n" +
                        "4. If no safe row, backtracks\n\n" +
                        "Steps taken: %d", currentStep, backtrackingSteps);
                        
            case "Las Vegas":
                return String.format("Las Vegas Algorithm (Attempt %d):\n\n" +
                        "1. Randomly places queens column by column\n" +
                        "2. For each column, selects a random safe row\n" +
                        "3. If no safe row, restarts\n" +
                        "4. Continues until solution found\n\n" +
                        "Attempts: %d", constrainedAttempts, constrainedAttempts);
                        
            default:
                return "Algorithm in progress...";
        }
    }

    public boolean isSafe(int row, int col, int[] solution) {
        for (int i = 0; i < col; i++) {
            if (solution[i] == row || Math.abs(solution[i] - row) == Math.abs(i - col)) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> getSafeRows(int col, int[] queens) {
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
}