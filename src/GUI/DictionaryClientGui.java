package GUI;

import javax.swing.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.List;

public class DictionaryClientGui {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private final Logger logger;
    private final ClientUtil clientUtil;

    public DictionaryClientGui(Logger logger, ClientUtil clientUtil) {
        this.logger = logger;
        this.clientUtil = clientUtil;
        initializeUI();
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Best Dictionary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
        frame.add(mainPanel);

        JPanel sidebar = new JPanel(new GridLayout(4, 1, 0, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ToggleButton queryButton = new ToggleButton("Query");
        ToggleButton addButton = new ToggleButton("Add");
        ToggleButton removeButton = new ToggleButton("Remove");
        ToggleButton updateButton = new ToggleButton("Update");

        sidebar.add(queryButton);
        sidebar.add(addButton);
        sidebar.add(removeButton);
        sidebar.add(updateButton);

        mainPanel.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel welcomePanel = createWelcomeCard();
        cardPanel.add(welcomePanel, "Welcome");

        cardPanel.add(createQueryCard(), "Query");
        cardPanel.add(createAddCard(), "Add");
        cardPanel.add(createRemoveCard(), "Remove");
        cardPanel.add(createUpdateCard(), "Update");

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setupCardSwitching(queryButton, addButton, removeButton, updateButton);

        frame.setVisible(true);

    }

    private JPanel createQueryCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel for user input
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Search for a Word"));

        JTextField wordInput = new JTextField();
        wordInput.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Box inputBox = Box.createHorizontalBox();
        inputBox.add(wordInput);
        inputPanel.add(inputBox, BorderLayout.CENTER);

        JButton queryButton = new JButton("Search");
        inputPanel.add(queryButton, BorderLayout.EAST);

        card.add(inputPanel, BorderLayout.NORTH);

        // Text area for displaying the results
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Results"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        card.add(scrollPane, BorderLayout.CENTER);

        // Button action to perform the search
        queryButton.addActionListener(e -> {
            String word = wordInput.getText().trim();
            if (!word.isEmpty()) {
                String definition = clientUtil.queryServer(word);
                SwingUtilities.invokeLater(() -> resultArea.setText("Results for \"" + word + "\":\n" + definition));
            } else {
                resultArea.setText("Please enter a word to query.");
            }
        });

        return card;
    }

    private JPanel createAddCard() {
        JPanel addCard = new JPanel();
        addCard.setLayout(new BoxLayout(addCard, BoxLayout.Y_AXIS));
        addCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Word input panel with a titled border
        JPanel wordPanel = new JPanel();
        wordPanel.setLayout(new BoxLayout(wordPanel, BoxLayout.Y_AXIS));
        wordPanel.setBorder(BorderFactory.createTitledBorder("Word"));

        // Input field for the word
        JTextField wordInput = new JTextField();
        wordInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, wordInput.getPreferredSize().height));
        wordPanel.add(wordInput);

        addCard.add(wordPanel);
        addCard.add(Box.createVerticalStrut(10));

        // Meanings panel with a titled border
        JPanel meaningsPanel = new JPanel(new BorderLayout());
        meaningsPanel.setBorder(BorderFactory.createTitledBorder("Meanings"));

        // List panel for the meaning fields
        JPanel meaningsListPanel = new JPanel();
        meaningsListPanel.setLayout(new BoxLayout(meaningsListPanel, BoxLayout.Y_AXIS));
        meaningsPanel.add(new JScrollPane(meaningsListPanel), BorderLayout.CENTER);

        // Bottom panel for the buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear");
        JButton addMeaningButton = new JButton("+ Add Meaning");
        JButton submitButton = new JButton("Submit");
        bottomPanel.add(clearButton);
        bottomPanel.add(addMeaningButton);
        bottomPanel.add(submitButton);
        meaningsPanel.add(bottomPanel, BorderLayout.SOUTH);

        // List to keep track of all meaning fields
        List<JTextField> meaningFields = new ArrayList<>();

        // Add Meaning button logic
        addMeaningButton.addActionListener(e -> {
            JTextField newMeaningField = new JTextField();
            JButton removeMeaningButton = new JButton("-");

            Dimension buttonDimension = removeMeaningButton.getPreferredSize();
            newMeaningField.setPreferredSize(new Dimension(newMeaningField.getPreferredSize().width, buttonDimension.height));
            newMeaningField.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonDimension.height));

            removeMeaningButton.addActionListener(ev -> {
                meaningsListPanel.remove(newMeaningField.getParent());
                meaningFields.remove(newMeaningField);
                meaningsListPanel.revalidate();
                meaningsListPanel.repaint();
            });

            JPanel meaningFieldPanel = new JPanel();
            meaningFieldPanel.setLayout(new BoxLayout(meaningFieldPanel, BoxLayout.X_AXIS));
            meaningFieldPanel.add(newMeaningField);
            meaningFieldPanel.add(removeMeaningButton);
            meaningFields.add(newMeaningField);

            meaningsListPanel.add(meaningFieldPanel);
            meaningsListPanel.revalidate();
            meaningsListPanel.repaint();
        });

        addCard.add(meaningsPanel);

        // Status area with a scroll pane
        JTextArea statusArea = new JTextArea(4, 20);
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
        addCard.add(statusScrollPane);

        clearButton.addActionListener(e -> {
            meaningFields.clear();
            meaningsListPanel.removeAll();
            meaningsListPanel.revalidate();
            meaningsListPanel.repaint();
            statusArea.setText("");
            wordInput.setText("");
        });

        // Submit button logic
        submitButton.addActionListener(e -> {
            String word = wordInput.getText().trim();
            logger.info("receive word: " + word);
            List<String> meanings = meaningFields.stream()
                                                 .map(JTextField::getText)
                                                 .filter(text -> !text.trim().isEmpty())
                                                 .toList();
            logger.info("receive meaning: " + meanings);

            if (!word.isEmpty() && !meanings.isEmpty()) {
                String response = clientUtil.sendAddToServer(word, meanings);
                statusArea.setText(response);
            } else {
                statusArea.setText("Please enter a word and at least one meaning before submitting.");
            }
        });

        return addCard;
    }


    private JPanel createRemoveCard() {
        JPanel card = new JPanel(new BorderLayout());
        // Add components specific to the Remove functionality
        return card;
    }

    private JPanel createUpdateCard() {
        JPanel card = new JPanel(new BorderLayout());
        // Add components specific to the Update functionality
        return card;
    }

    private JPanel createWelcomeCard() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to the Best Dictionary", JLabel.CENTER);
        welcomePanel.add(welcomeLabel);
        return welcomePanel;
    }

    private void setupCardSwitching(ToggleButton queryButton, ToggleButton addButton,
                                    ToggleButton removeButton, ToggleButton updateButton) {
        queryButton.addActionListener(e -> {
            queryButton.setSelected(true);
            addButton.setSelected(false);
            removeButton.setSelected(false);
            updateButton.setSelected(false);
            cardLayout.show(cardPanel, "Query");
        });
        addButton.addActionListener(e -> {
            queryButton.setSelected(false);
            addButton.setSelected(true);
            removeButton.setSelected(false);
            updateButton.setSelected(false);
            cardLayout.show(cardPanel, "Add");
        });
        removeButton.addActionListener(e -> {
            queryButton.setSelected(false);
            addButton.setSelected(false);
            removeButton.setSelected(true);
            updateButton.setSelected(false);
            cardLayout.show(cardPanel, "Remove");
        });
        updateButton.addActionListener(e -> {
            queryButton.setSelected(false);
            addButton.setSelected(false);
            removeButton.setSelected(false);
            updateButton.setSelected(true);
            cardLayout.show(cardPanel, "Update");
        });
    }
}
