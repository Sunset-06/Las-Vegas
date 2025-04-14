import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubsetSum extends JFrame {
    private static final int MAX_NUMBERS = 10;
    private JPanel numbersPanel;
    private JLabel statusLabel;
    private JButton solveBtn;
    private JButton resetBtn;
    private JTextField targetField;
    private JTextField arrayField;
    private JTextArea resultArea;
    private JProgressBar progressBar;

    private int[] numbers;
    private int targetSum;
    private int backtrackSteps = 0;
    private Timer animationTimer;
    private List<List<Integer>> allSolutions;

    public SubsetSum() {
        setTitle("Subset Sum Problem Visualizer - All Solutions");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
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
        resetBtn = new JButton("Add Input");
        solveBtn.setFont(font);
        resetBtn.setFont(font);

        targetField = new JTextField("15", 5);
        arrayField = new JTextField(30);
        targetField.setFont(font);
        arrayField.setFont(font);

        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createTitledBorder("Results"));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        animationTimer = new Timer(100, e -> {
            String text = statusLabel.getText();
            statusLabel.setText(text.endsWith("...") ? text.substring(0, text.length() - 3) : text + ".");
        });

        solveBtn.addActionListener(e -> solveSubsetSum());
        resetBtn.addActionListener(e -> resetProblem());
    }

    private void setupLayout() {
        // Top Controls
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
        buttonPanel.add(resetBtn);

        controlPanel.add(inputPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(buttonPanel);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(numbersPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.add(progressBar, BorderLayout.NORTH);
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
        updateNumbersDisplay();
        resultArea.setText(getComplexityAnalysis());
        statusLabel.setText("Ready");
    }

    private void generateRandomArray() {
        Random rand = new Random();
        numbers = new int[MAX_NUMBERS];
        for (int i = 0; i < MAX_NUMBERS; i++) {
            numbers[i] = rand.nextInt(20) + 1;
        }
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
        solveBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        animationTimer.start();
        statusLabel.setText("Solving...");

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            findAllSubsets(0, 0, new ArrayList<>(), new ArrayList<>());
            long duration = System.currentTimeMillis() - startTime;

            SwingUtilities.invokeLater(() -> {
                StringBuilder resultText = new StringBuilder();
                resultText.append(String.format("Found %d solutions in %d steps (%d ms)\n\n",
                        allSolutions.size(), backtrackSteps, duration));

                if (!allSolutions.isEmpty()) {
                    resultText.append("Solutions:\n");
                    for (int i = 0; i < allSolutions.size(); i++) {
                        resultText.append(String.format("%d. %s\n", i + 1, allSolutions.get(i)));
                    }
                }

                resultText.append("\n").append(getComplexityAnalysis());
                resultArea.setText(resultText.toString());

                solveBtn.setEnabled(true);
                resetBtn.setEnabled(true);
                progressBar.setVisible(false);
                animationTimer.stop();
                statusLabel.setText(allSolutions.isEmpty() ? "No solutions found" :
                        String.format("Found %d solutions", allSolutions.size()));
                colorAllNumbersCyan();
            });
        }).start();
    }

    private void findAllSubsets(int index, int currentSum, List<Integer> currentSolution, List<Integer> indexPath) {
        backtrackSteps++;

        if (backtrackSteps % 10 == 0) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(String.format("Step %d: Found %d solutions",
                        backtrackSteps, allSolutions.size()));
                highlightCurrentSelection(indexPath);
            });
            try {Thread.sleep(50);} catch (InterruptedException e) {return;}
        }

        if (currentSum == targetSum) {
            allSolutions.add(new ArrayList<>(currentSolution));
            return;
        }

        if (index >= numbers.length || currentSum > targetSum) {
            return;
        }

        // Include current number
        currentSolution.add(numbers[index]);
        indexPath.add(index);
        findAllSubsets(index + 1, currentSum + numbers[index], currentSolution, indexPath);

        // Exclude current number
        currentSolution.remove(currentSolution.size() - 1);
        indexPath.remove(indexPath.size() - 1);
        findAllSubsets(index + 1, currentSum, currentSolution, indexPath);
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
