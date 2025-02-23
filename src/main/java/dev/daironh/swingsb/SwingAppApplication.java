package dev.daironh.swingsb;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

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
    private JTextArea textArea;

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

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane textScrollPane = new JScrollPane(textArea); // Scroll pane for the text area

        openExcelButton = new JButton("Open Excel File");
        openTextButton = new JButton("Open Text File");
        openNewWindowButton = new JButton("Open New Window");

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

        // Create a split pane to divide the table and text area
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, textScrollPane);
        splitPane.setResizeWeight(0.5); // Equal division of space

        // Add components to the JFrame
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openExcelButton);
        buttonPanel.add(openTextButton);
        buttonPanel.add(openNewWindowButton);

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
            textArea.setText(content); // Display the content in the text area
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading the text file: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private void openNewWindow() {
        // Create a new JFrame for the dropdown window
        JFrame dropdownWindow = new JFrame("Dropdown Selection");
        dropdownWindow.setSize(300, 150);
        dropdownWindow.setLocationRelativeTo(this);
        dropdownWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create components for the dropdown window
        String[] choices = {"Choice 1", "Choice 2", "Choice 3"};
        JComboBox<String> dropdown = new JComboBox<>(choices);
        JButton acceptButton = new JButton("Accept");

        // Add action listener for the accept button
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedValue = (String) dropdown.getSelectedItem(); // Store the selected value
                JOptionPane.showMessageDialog(dropdownWindow, "Selected: " + selectedValue, "Selection", JOptionPane.INFORMATION_MESSAGE);
                dropdownWindow.dispose(); // Close the window
            }
        });

        // Add components to the dropdown window
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(dropdown, BorderLayout.CENTER);
        panel.add(acceptButton, BorderLayout.SOUTH);

        dropdownWindow.add(panel);
        dropdownWindow.setVisible(true);
    }


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SwingAppApplication.class, args);
        SwingUtilities.invokeLater(() -> {
            SwingAppApplication app = context.getBean(SwingAppApplication.class);
            app.setVisible(true);
        });
    }
}
