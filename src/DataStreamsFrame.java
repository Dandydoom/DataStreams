import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads a text file and uses a Java Stream with a lambda filter to show
 * only the lines that contain a search string.
 * The original file is on the left and the matching lines are on the right.
 *
 * @author Kirby Fortney
 */
public class DataStreamsFrame extends JFrame
{
    private Path filePath = null;

    private JTextField searchField;
    private JButton searchButton;
    private JLabel fileLabel;
    private JLabel resultLabel;
    private JTextArea originalArea;
    private JTextArea filteredArea;

    public DataStreamsFrame()
    {
        setTitle("Data Streams File Search");
        setLayout(new BorderLayout(5, 5));

        add(createSearchPanel(), BorderLayout.NORTH);
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JPanel createSearchPanel()
    {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));

        searchField = new JTextField(30);
        searchPanel.add(new JLabel("Search for:"));
        searchPanel.add(searchField);
        return searchPanel;
    }

    private JPanel createDisplayPanel()
    {
        JPanel displayPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // left side: the original file
        JPanel originalPanel = new JPanel(new BorderLayout());
        fileLabel = new JLabel("Original File: (none loaded)");
        originalArea = createTextArea();
        originalPanel.add(fileLabel, BorderLayout.NORTH);
        originalPanel.add(new JScrollPane(originalArea), BorderLayout.CENTER);

        // right side: the matching lines
        JPanel filteredPanel = new JPanel(new BorderLayout());
        resultLabel = new JLabel("Filtered Results:");
        filteredArea = createTextArea();
        filteredPanel.add(resultLabel, BorderLayout.NORTH);
        filteredPanel.add(new JScrollPane(filteredArea), BorderLayout.CENTER);

        displayPanel.add(originalPanel);
        displayPanel.add(filteredPanel);
        return displayPanel;
    }

    private JTextArea createTextArea()
    {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        return area;
    }

    private JPanel createButtonPanel()
    {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));

        JButton loadButton = new JButton("Load File");
        searchButton = new JButton("Search File");
        JButton quitButton = new JButton("Quit");

        searchButton.setEnabled(false); // only works after a file is loaded

        loadButton.addActionListener(ae -> loadFile());
        searchButton.addActionListener(ae -> searchFile());
        quitButton.addActionListener(ae -> quit());

        buttonPanel.add(loadButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(quitButton);
        return buttonPanel;
    }

    /**
     * Lets the user pick a file with JFileChooser and shows it on the left.
     */
    private void loadFile()
    {
        JFileChooser chooser = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return; // the user cancelled
        }

        Path path = chooser.getSelectedFile().toPath();

        try (Stream<String> lines = Files.lines(path))
        {
            String text = lines.collect(Collectors.joining("\n"));
            originalArea.setText(text);
            originalArea.setCaretPosition(0);

            filePath = path;
            fileLabel.setText("Original File: " + path.getFileName());
            filteredArea.setText("");
            resultLabel.setText("Filtered Results:");
            searchButton.setEnabled(true);
        }
        catch (IOException | UncheckedIOException e)
        {
            JOptionPane.showMessageDialog(this, "Could not read the file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Streams the file and keeps only the lines that contain the search string.
     */
    private void searchFile()
    {
        String searchString = searchField.getText();

        if (searchString.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Please enter a search string.",
                    "Missing Search String", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Stream<String> lines = Files.lines(Paths.get(filePath.toString())))
        {
            List<String> matches = lines
                    .filter(line -> line.contains(searchString))
                    .collect(Collectors.toList());

            filteredArea.setText(String.join("\n", matches));
            filteredArea.setCaretPosition(0);
            resultLabel.setText("Filtered Results: " + matches.size()
                    + " lines contain \"" + searchString + "\"");
        }
        catch (IOException | UncheckedIOException e)
        {
            JOptionPane.showMessageDialog(this, "Could not read the file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quit()
    {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?",
                "Quit", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }
}
