package dev.daironh.swingsb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SwingAppApplication extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton openExcelButton;
    private JButton openTextButton;
    private JButton openNewWindowButton;
    private JButton processTableButton;
    private JTextPane textPane; // Replaced JTextArea with JTextPane for rich text formatting

    private String selectedValue = ""; // Store the selected value from the dropdown

    public SwingAppApplication() {
        // Set up the JFrame
        setTitle("Excel and Text File Reader");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table); // Scroll pane for the table

        textPane = new JTextPane(); // Use JTextPane for rich text formatting
        textPane.setEditable(false);
        JScrollPane textScrollPane = new JScrollPane(textPane); // Scroll pane for the text pane

        openExcelButton = new JButton("Open Excel File");
        openTextButton = new JButton("Open Text File");
        openNewWindowButton = new JButton("Open New Window");
        processTableButton = new JButton("Process Table");

        // Add action listeners
        openExcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openExcelFile();
            }
        });

        openTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openTextFile();
            }
        });

        openNewWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewWindow();
            }
        });

        processTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProcessDialog();
            }
        });

        // Create a split pane to divide the table and text pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, textScrollPane);
        splitPane.setResizeWeight(0.5); // Equal division of space

        // Add components to the JFrame
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openExcelButton);
        buttonPanel.add(openTextButton);
        buttonPanel.add(openNewWindowButton);
        buttonPanel.add(processTableButton);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER); // Add split pane to the center
        add(buttonPanel, BorderLayout.SOUTH);

    }

    private void openExcelFile() {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an Excel File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            readExcelFile(selectedFile);
        }
    }

    private void readExcelFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            // Create a workbook instance for the Excel file
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

            // Get the number of rows and columns
            int rowCount = sheet.getPhysicalNumberOfRows();
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getPhysicalNumberOfCells();

            // Read headers
            String[] headers = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                headers[i] = headerRow.getCell(i).getStringCellValue();
            }

            // Read all data
            Object[][] data = new Object[rowCount - 1][colCount];
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < colCount; j++) {
                    Cell cell = row.getCell(j);
                    switch (cell.getCellType()) {
                        case STRING:
                            data[i - 1][j] = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            data[i - 1][j] = cell.getNumericCellValue();
                            break;
                        case BOOLEAN:
                            data[i - 1][j] = cell.getBooleanCellValue();
                            break;
                        default:
                            data[i - 1][j] = "UNKNOWN";
                    }
                }
            }

            // Close the workbook
            workbook.close();

            // Set up the table model with headers and data
            tableModel.setColumnIdentifiers(headers);
            for (Object[] row : data) {
                tableModel.addRow(row);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading the Excel file: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTextFile() {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Text File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            readTextFile(selectedFile);
        }
    }

    private void readTextFile(File file) {
        try {
            // Read the entire text file into a string
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

            // Apply styling to text inside {{ }}
            StyledDocument doc = textPane.getStyledDocument();
            doc.remove(0, doc.getLength()); // Clear previous content

            // Define styles
            StyleContext context = new StyleContext();
            Style defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
            Style boldYellowStyle = context.addStyle("BoldYellow", null);
            StyleConstants.setBold(boldYellowStyle, true);
            StyleConstants.setBackground(boldYellowStyle, Color.YELLOW);

            // Insert text with styling
            int start = 0;
            while (true) {
                int openIndex = content.indexOf("{{", start);
                int closeIndex = content.indexOf("}}", openIndex);

                if (openIndex == -1 || closeIndex == -1) {
                    // No more {{ }} found, add the remaining text
                    doc.insertString(doc.getLength(), content.substring(start), defaultStyle);
                    break;
                }

                // Add text before {{
                doc.insertString(doc.getLength(), content.substring(start, openIndex), defaultStyle);

                // Add text inside {{ }} with bold and yellow background
                doc.insertString(doc.getLength(), "{{" + content.substring(openIndex + 2, closeIndex) + "}}",
                        boldYellowStyle);

                start = closeIndex + 2; // Move past the closing }}
            }
        } catch (IOException | BadLocationException ex) {
            JOptionPane.showMessageDialog(this, "Error reading the text file: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openNewWindow() {
        // Create a JDialog for the dropdown window
        JDialog dropdownDialog = new JDialog(this, "Dropdown Selection", true); // Modal dialog
        dropdownDialog.setSize(300, 150);
        dropdownDialog.setLocationRelativeTo(this);
        dropdownDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create components for the dropdown dialog
        String[] choices = { "Choice 1", "Choice 2", "Choice 3" };
        JComboBox<String> dropdown = new JComboBox<>(choices);
        JButton acceptButton = new JButton("Accept");

        // Add action listener for the accept button
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedValue = (String) dropdown.getSelectedItem(); // Store the selected value

                // If Choice 3 is selected, show a confirmation dialog
                if (selectedValue.equals("Choice 3")) {
                    int confirm = JOptionPane.showConfirmDialog(
                            dropdownDialog,
                            "Are you sure you want to select Choice 3?",
                            "Confirm Choice 3",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        JOptionPane.showMessageDialog(dropdownDialog, "Selected: " + selectedValue, "Selection",
                                JOptionPane.INFORMATION_MESSAGE);
                        dropdownDialog.dispose(); // Close the dialog
                    }
                } else {
                    JOptionPane.showMessageDialog(dropdownDialog, "Selected: " + selectedValue, "Selection",
                            JOptionPane.INFORMATION_MESSAGE);
                    dropdownDialog.dispose(); // Close the dialog
                }
            }
        });

        // Add components to the dropdown dialog
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(dropdown, BorderLayout.CENTER);
        panel.add(acceptButton, BorderLayout.SOUTH);

        dropdownDialog.add(panel);
        dropdownDialog.setVisible(true);
    }

    private void openProcessDialog() {
        // Create a JDialog for the processing window
        JDialog processDialog = new JDialog(this, "Process Table", true); // Modal dialog
        processDialog.setSize(500, 300);
        processDialog.setLocationRelativeTo(this);
        processDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Prevent closing during processing

        // Create components for the processing dialog
        JButton runButton = new JButton("Run");
        JProgressBar progressBar = new JProgressBar(0, tableModel.getRowCount());
        progressBar.setStringPainted(true); // Show progress percentage

        // Labels for record statistics
        JLabel totalRecordsLabel = new JLabel("Total Records: 0");
        JLabel okRecordsLabel = new JLabel("OK Records: 0");
        JLabel failedRecordsLabel = new JLabel("Failed Records: 0");

        // Toggle button for execution log
        JToggleButton toggleLogButton = new JToggleButton("Show Execution Log");
        JTextArea logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setVisible(false); // Initially hidden

        // Add action listener for the toggle button
        toggleLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logScrollPane.setVisible(toggleLogButton.isSelected());
                processDialog.pack(); // Resize dialog to fit content
            }
        });

        // Add action listener for the run button
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Disable the run button during processing
                runButton.setEnabled(false);

                // Simulate processing each record in the table
                new Thread(() -> {
                    int totalRecords = tableModel.getRowCount();
                    final int[] okRecords = {0};
                    final int[] failedRecords = {0};

                    // Clear the log file at the start of processing
                    try {
                        Files.write(Paths.get("execution.log"), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    for (int i = 0; i < totalRecords; i++) {
                        // Simulate processing delay
                        try {
                            Thread.sleep(500); // Simulate processing time
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        // Randomly simulate success or failure
                        boolean success = new Random().nextBoolean();
                        if (success) {
                            okRecords[0]++;
                        } else {
                            failedRecords[0]++;
                        }

                        // Log the result
                        String logMessage = "Record " + (i + 1) + ": " + (success ? "OK" : "Failed") + "\n";
                        try {
                            Files.write(Paths.get("execution.log"), logMessage.getBytes(), StandardOpenOption.APPEND);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        // Update progress bar and labels
                        final int progress = i + 1;
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(progress);
                            totalRecordsLabel.setText("Total Records: " + totalRecords);
                            okRecordsLabel.setText("OK Records: " + okRecords[0]);
                            failedRecordsLabel.setText("Failed Records: " + failedRecords[0]);

                            // Update log text area in real time
                            if (toggleLogButton.isSelected()) {
                                try {
                                    String logContent = new String(Files.readAllBytes(Paths.get("execution.log")));
                                    logTextArea.setText(logContent);
                                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength()); // Scroll to
                                                                                                         // bottom
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }

                    // Enable the run button after processing
                    SwingUtilities.invokeLater(() -> {
                        runButton.setEnabled(true);
                        JOptionPane.showMessageDialog(processDialog, "Processing complete!", "Complete",
                                JOptionPane.INFORMATION_MESSAGE);
                        processDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Allow closing after
                                                                                          // processing
                    });
                }).start();
            }
        });

        // Add components to the processing dialog
        JPanel panel = new JPanel(new BorderLayout());
        JPanel statsPanel = new JPanel(new GridLayout(3, 1));
        statsPanel.add(totalRecordsLabel);
        statsPanel.add(okRecordsLabel);
        statsPanel.add(failedRecordsLabel);

        panel.add(runButton, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.SOUTH);
        panel.add(toggleLogButton, BorderLayout.EAST);
        panel.add(logScrollPane, BorderLayout.AFTER_LAST_LINE);

        processDialog.add(panel);
        processDialog.setVisible(true);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SwingAppApplication.class, args);
        SwingUtilities.invokeLater(() -> {
            SwingAppApplication app = context.getBean(SwingAppApplication.class);
            app.setVisible(true);
        });
    }
}
