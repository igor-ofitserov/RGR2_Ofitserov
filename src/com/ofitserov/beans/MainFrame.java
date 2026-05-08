package com.ofitserov.beans;

import javax.swing.*;
import java.awt.*;
import java.io.File;

// Бібліотеки для роботи з XML
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MainFrame extends JFrame {
    private DataSheetTable tablePanel;
    private DataSheetGraph graphPanel;
    private JFileChooser fileChooser;

    public MainFrame() {
        setTitle("Обробка експериментальних даних (РГР 2)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        tablePanel = new DataSheetTable();
        graphPanel = new DataSheetGraph();

        // Зв'язуємо таблицю та графік
        tablePanel.addDataSheetChangeListener(graphPanel);
        graphPanel.setDataSheet(tablePanel.getDataSheet());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, graphPanel);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        setupMenu();

        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem openItem = new JMenuItem("Відкрити XML...");
        JMenuItem saveItem = new JMenuItem("Зберегти XML...");
        JMenuItem exitItem = new JMenuItem("Вихід");

        openItem.addActionListener(e -> openXML());
        saveItem.addActionListener(e -> saveXML());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu dataMenu = new JMenu("Дані");
        JMenuItem addItem = new JMenuItem("Додати порожню точку");
        JMenuItem clearItem = new JMenuItem("Очистити дані");

        addItem.addActionListener(e -> {
            tablePanel.getDataSheet().addData(new Data());
            tablePanel.setDataSheet(tablePanel.getDataSheet());
        });

        clearItem.addActionListener(e -> {
            tablePanel.getDataSheet().clear();
            tablePanel.setDataSheet(tablePanel.getDataSheet());
        });

        dataMenu.add(addItem);
        dataMenu.add(clearItem);
        menuBar.add(dataMenu);

        setJMenuBar(menuBar);
    }

    // --- Читання XML за допомогою SAX-парсера ---
    private void openXML() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                DefaultHandler handler = new DefaultHandler() {
                    DataSheet tempSheet = new DataSheet();
                    Data tempData = null;
                    String currentTag = "";

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) {
                        currentTag = qName;
                        if (qName.equals("data")) {
                            tempData = new Data();
                        }
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) {
                        String value = new String(ch, start, length).trim();
                        if (value.isEmpty() || tempData == null) return;

                        try {
                            if (currentTag.equals("x")) {
                                tempData.setX(Double.parseDouble(value));
                            } else if (currentTag.equals("y")) {
                                tempData.setY(Double.parseDouble(value));
                            }
                        } catch (NumberFormatException ignored) {}
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) {
                        if (qName.equals("data") && tempData != null) {
                            tempSheet.addData(tempData);
                            tempData = null;
                        }
                        currentTag = "";
                    }

                    // Передаємо зібрані дані в таблицю після завершення читання документа
                    @Override
                    public void endDocument() {
                        tablePanel.setDataSheet(tempSheet);
                        graphPanel.setDataSheet(tempSheet);
                    }
                };

                parser.parse(file, handler);
                JOptionPane.showMessageDialog(this, "Дані успішно завантажено!", "Успіх", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка читання XML: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Запис XML за допомогою DOM-об'єкта ---
    private void saveXML() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xml")) {
                file = new File(file.getParentFile(), file.getName() + ".xml"); // Автоматично додаємо розширення .xml
            }

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.newDocument();

                // Створюємо кореневий елемент
                Element rootElement = doc.createElement("datasheet");
                doc.appendChild(rootElement);

                DataSheet currentData = tablePanel.getDataSheet();

                // Проходимося по всіх точках і створюємо вузли
                for (int i = 0; i < currentData.size(); i++) {
                    Data d = currentData.getDataItem(i);

                    Element dataElement = doc.createElement("data");

                    Element xElement = doc.createElement("x");
                    xElement.appendChild(doc.createTextNode(String.valueOf(d.getX())));

                    Element yElement = doc.createElement("y");
                    yElement.appendChild(doc.createTextNode(String.valueOf(d.getY())));

                    dataElement.appendChild(xElement);
                    dataElement.appendChild(yElement);
                    rootElement.appendChild(dataElement);
                }

                // Зберігаємо DOM-дерево у файл
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);

                JOptionPane.showMessageDialog(this, "Дані успішно збережено у XML!", "Успіх", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка збереження XML: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}