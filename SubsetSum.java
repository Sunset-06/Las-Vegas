/*This is not part of the Eight Queens simulation */
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubsetSum extends JFrame {
    private static final int MAX_NUMBERS = 10;
    private JPanel numbersPanel;
    private JLabel statusLabel;
    private JButton solveBtn, resetBtn, nextStepBtn, prevStepBtn;
    private JTextField targetField, arrayField;
    private JTextArea resultArea;
    private JProgressBar progressBar;
    private JLabel statsLabel;

    private int[] numbers;
    private int targetSum;
    private int backtrackSteps = 0;
    private Timer animationTimer;
    private List<List<Integer>> allSolutions;

    // Step-by-step state
    private List<BacktrackState> stateHistory = new ArrayList<>();
    private int currentStateIndex = -1;
    private boolean isSolving = false;

    class BacktrackState {
        int index;
        int currentSum;
        List<Integer> currentSolution;
        List<Integer> indexPath;
        String description;

        public BacktrackState(int index, int currentSum, List<Integer> currentSolution, 
                            List<Integer> indexPath, String description) {
            this.index = index;
            this.currentSum = currentSum;
            this.currentSolution = new ArrayList<>(currentSolution);
            this.indexPath = new ArrayList<>(indexPath);
            this.description = description;
        }
    }

    public SubsetSum() {
        setTitle("Subset Sum Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        initializeComponents();
        setupLayout();
        resetProblem();
    }

    private void initializeComponents() {
        numbersPanel = new JPanel(new GridLayout(1, MAX_NUMBERS, 10, 10));
        numbersPanel.setPreferredSize(new Dimension(800, 80));
        numbersPanel.setBorder(BorderFactory.createTitledBorder("Number Array"));

        Font font = new Font("Arial", Font.PLAIN, 16);

        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setFont(font);

        solveBtn = new JButton("Solve");
        resetBtn = new JButton("Add Array");
        nextStepBtn = new JButton("Next Step");
        prevStepBtn = new JButton("Previous Step");
        solveBtn.setFont(font);
        resetBtn.setFont(font);
        nextStepBtn.setFont(font);
        prevStepBtn.setFont(font);

        targetField = new JTextField("15", 5);
        arrayField = new JTextField("2,3,5,7,11", 30);
        targetField.setFont(font);
        arrayField.setFont(font);

        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createTitledBorder("Results"));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        statsLabel = new JLabel("Steps: 0 | Solutions: 0", SwingConstants.CENTER);
        statsLabel.setFont(font);

        animationTimer = new Timer(100, e -> {
            String text = statusLabel.getText();
            statusLabel.setText(text.endsWith("...") ? text.substring(0, text.length() - 3) : text + ".");
        });

        solveBtn.addActionListener(e -> solveSubsetSum());
        resetBtn.addActionListener(e -> resetProblem());
        nextStepBtn.addActionListener(e -> stepForward());
        prevStepBtn.addActionListener(e -> stepBackward());
    }

    private void setupLayout() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Target Sum:"));
        inputPanel.add(targetField);
        inputPanel.add(Box.createHorizontalStrut(20));
        inputPanel.add(new JLabel("Input Array:"));
        inputPanel.add(arrayField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(solveBtn);
        buttonPanel.add(prevStepBtn);
        buttonPanel.add(nextStepBtn);
        buttonPanel.add(resetBtn);

        controlPanel.add(inputPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(buttonPanel);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(numbersPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.add(progressBar, BorderLayout.NORTH);
        footerPanel.add(statsLabel, BorderLayout.CENTER);
        footerPanel.add(statusLabel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void resetProblem() {
        String input = arrayField.getText().trim();
        if (!input.isEmpty()) {
            try {
                String[] parts = input.split(",");
                numbers = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    numbers[i] = Integer.parseInt(parts[i].trim());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid array input. Using random array instead.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                generateRandomArray();
            }
        } else {
            generateRandomArray();
        }

        try {
            targetSum = Integer.parseInt(targetField.getText());
        } catch (NumberFormatException e) {
            targetSum = 15;
            targetField.setText("15");
        }

        backtrackSteps = 0;
        allSolutions = new ArrayList<>();
        stateHistory.clear();
        currentStateIndex = -1;
        isSolving = false;

        updateNumbersDisplay();
        resultArea.setText(getComplexityAnalysis());
        statusLabel.setText("Ready");
        statsLabel.setText("Steps: 0 | Solutions: 0");
        solveBtn.setEnabled(true);
        nextStepBtn.setEnabled(true);
        prevStepBtn.setEnabled(true);
        resetBtn.setEnabled(true);
    }

    private void generateRandomArray() {
        Random rand = new Random();
        numbers = new int[MAX_NUMBERS];
        for (int i = 0; i < MAX_NUMBERS; i++) {
            numbers[i] = rand.nextInt(20) + 1;
        }
        arrayField.setText(arrayToString());
    }

    private String arrayToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(numbers[i]);
        }
        return sb.toString();
    }

    private void updateNumbersDisplay() {
        numbersPanel.removeAll();
        for (int num : numbers) {
            JLabel numLabel = new JLabel(String.valueOf(num), SwingConstants.CENTER);
            numLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            numLabel.setOpaque(true);
            numLabel.setBackground(Color.WHITE);
            numLabel.setFont(new Font("Arial", Font.BOLD, 16));
            numbersPanel.add(numLabel);
        }
        numbersPanel.revalidate();
        numbersPanel.repaint();
    }

    private void solveSubsetSum() {
        isSolving = true;
        solveBtn.setEnabled(false);
        nextStepBtn.setEnabled(false);
        prevStepBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        animationTimer.start();
        statusLabel.setText("Solving...");
        allSolutions.clear();
        backtrackSteps = 0;
        stateHistory.clear();
        currentStateIndex = -1;

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            findAllSubsets(0, 0, new ArrayList<>(), new ArrayList<>());
            long duration = System.currentTimeMillis() - startTime;

            SwingUtilities.invokeLater(() -> {
                StringBuilder resultText = new StringBuilder();
                resultText.append(String.format("Found %d solutions in %d steps (%d ms)\n\n",
                        allSolutions.size(), backtrackSteps, duration));
                for (int i = 0; i < allSolutions.size(); i++) {
                    resultText.append(String.format("%d. %s\n", i + 1, allSolutions.get(i)));
                }
                //resultText.append("\n").append(getComplexityAnalysis());
                resultArea.setText(resultText.toString());

                solveBtn.setEnabled(true);
                nextStepBtn.setEnabled(true);
                prevStepBtn.setEnabled(true);
                resetBtn.setEnabled(true);
                progressBar.setVisible(false);
                animationTimer.stop();
                statusLabel.setText("Done");
                statsLabel.setText(String.format("Steps: %d | Solutions: %d", backtrackSteps, allSolutions.size()));
                colorAllNumbersCyan();
                isSolving = false;
                
                // Enable navigation through history
                if (!stateHistory.isEmpty()) {
                    currentStateIndex = 0;
                    showState(stateHistory.get(0));
                }
            });
        }).start();
    }

    private void findAllSubsets(int index, int sum, List<Integer> subset, List<Integer> indexPath) {
        // Save state before processing
        saveState(index, sum, subset, indexPath, "Exploring index " + index);
        
        backtrackSteps++;
        SwingUtilities.invokeLater(() -> {
            statsLabel.setText(String.format("Steps: %d | Solutions: %d", backtrackSteps, allSolutions.size()));
        });

        if (sum == targetSum) {
            allSolutions.add(new ArrayList<>(subset));
            saveState(index, sum, subset, indexPath, "Solution found!");
            return;
        }
        if (index >= numbers.length || sum > targetSum) {
            saveState(index, sum, subset, indexPath, "Backtracking from index " + index);
            return;
        }

        // Include current number
        subset.add(numbers[index]);
        indexPath.add(index);
        saveState(index, sum + numbers[index], subset, indexPath, 
                 "Including " + numbers[index] + " at index " + index);
        findAllSubsets(index + 1, sum + numbers[index], subset, indexPath);
        
        // Exclude current number
        subset.remove(subset.size() - 1);
        indexPath.remove(indexPath.size() - 1);
        saveState(index, sum, subset, indexPath, 
                 "Excluding " + numbers[index] + " at index " + index);
        findAllSubsets(index + 1, sum, subset, indexPath);
    }

    private void saveState(int index, int sum, List<Integer> subset, 
                         List<Integer> indexPath, String description) {
        stateHistory.add(new BacktrackState(index, sum, subset, indexPath, description));
    }

    private void stepForward() {
        if (isSolving) return;
        
        if (currentStateIndex < stateHistory.size() - 1) {
            currentStateIndex++;
            showState(stateHistory.get(currentStateIndex));
        } else {
            statusLabel.setText("Reached end of history");
        }
    }

    private void stepBackward() {
        if (isSolving) return;
        
        if (currentStateIndex > 0) {
            currentStateIndex--;
            showState(stateHistory.get(currentStateIndex));
        } else {
            statusLabel.setText("At initial state");
        }
    }

    private void showState(BacktrackState state) {
        // Update number highlighting
        highlightCurrentSelection(state.indexPath);
        
        // Update status
        statusLabel.setText(state.description);
        statsLabel.setText(String.format("Steps: %d | Solutions: %d", backtrackSteps, allSolutions.size()));
        
        // Update result area with current state info
        resultArea.setText(String.format(
            "Current State:\n" +
            "Index: %d\n" +
            "Current Sum: %d\n" +
            "Current Subset: %s\n" +
            "Target Sum: %d\n\n" +
            "%s",
            state.index, state.currentSum, state.currentSolution, targetSum
            //getComplexityAnalysis()
        ));
    }

    private void highlightCurrentSelection(List<Integer> indexPath) {
        Component[] components = numbersPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JLabel label) {
                label.setBackground(indexPath.contains(i) ? Color.GREEN : Color.WHITE);
            }
        }
    }

    private void colorAllNumbersCyan() {
        for (Component comp : numbersPanel.getComponents()) {
            if (comp instanceof JLabel label) {
                label.setBackground(Color.CYAN);
            }
        }
    }

    private String getComplexityAnalysis() {
        return "Complexity Analysis:\n" +
                "1. Worst Case: O(2^n) - Exponential time\n" +
                "2. Average Case: O(2^n) - Almost the same as worst case\n" +
                "3. Best Case: O(1) - No solutions\n" +
                "Space Complexity: O(n) - Recursion stack depth\n";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SubsetSum visualizer = new SubsetSum();
            visualizer.setVisible(true);
        });
    }
}