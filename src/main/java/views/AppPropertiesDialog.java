package views;

import utils.AppProperties;
import utils.Frames;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

// Вікно для керування властивостями додатку.
public class AppPropertiesDialog extends JDialog {
    private Dimension preferedPanelSize;
    private final JPanel mainPanel = new JPanel(); // головна панель вікна
    private final JTabbedPane tabbedPane = new JTabbedPane(); // табована панель, для івдображення різних типів налаштувань
    private final JTextField pathPdfFiles = new JTextField(30);
    private final JTextField pdfFileRegex = new JTextField(30);

    private final JTextField pathXLSXFile = new JTextField(30);
    private final JTextField dateFormat = new JTextField(30);
    private final JTextField showColumns = new JTextField(30);
    private final JTextField notNullColumn = new JTextField(30);
    private final JTextField headerRow = new JTextField(30);
    private final JTextField materialColumn = new JTextField(30);
    private final JTextField conNoColumn = new JTextField(30);
    private final JTextField materialRegex = new JTextField(30);
    //
    private final JTextField templatePermitProtocolFile = new JTextField(30);
    private final JTextField sheetInTemplatePermitProtocolFile = new JTextField(30);
    private final JTextField genProjectName = new JTextField(30);
    private final JTextField genMaterialNr = new JTextField(30);
    private final JTextField genSupplier = new JTextField(30);
    private final JTextField genArtNr = new JTextField(30);
    private final JTextField genPersonPPETS = new JTextField(30);
    private final JTextField genPersonPPE = new JTextField(30);
    private final JTextField genPersonPQM = new JTextField(30);
    private final JTextField genProtocolDate = new JTextField(30);

    private JTable dbTable;

    public AppPropertiesDialog(Frame owner) {
        super(owner, "Налаштування", true);
        setBounds(0, 0, 400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    // головний метод ініціалізації компонентів вікна
    private void initComponents() {
        // ініціалізуєм головну панель вікна
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(tabbedPane);
        this.getContentPane().add(mainPanel);
        initGeneralTab(); // ініціалізуєм панель з загальними налаштуваннями
        initControlButtons(); // ініціалізуєм кнопки дій - Зберегти/відмінити
        this.pack(); // підганяєм розміри вікна під панель з загальними налаштуваннями
        initGeneratorTab();
        initDBTab(); // ініціалізуєм панель з налаштуваннями баз даних
        revalidate();
        this.setResizable(false); // забороняєм зміну розміру вікна
        setBounds(Frames.getBoundsForCenteringFrame(this)); // робим вікно по центру монітора
    }

    // ініціалізує компоненти вкладки "Допуски тест. модулів"
    private void initGeneralTab() {
        // створюємо панель загальних налаштувань
        JPanel mainPrefPanel = new JPanel();
        mainPrefPanel.setLayout(new GridLayout(10, 1));
        // задаєм текст в поля з файлу налаштувань
        pathPdfFiles.setText(AppProperties.getProperty("pathPdfFiles"));
        pdfFileRegex.setText(AppProperties.getProperty("file_name_regex"));
        pathXLSXFile.setText(AppProperties.getProperty("pathXLSXFile"));
        dateFormat.setText(AppProperties.getProperty("dateFormat"));
        showColumns.setText(AppProperties.getProperty("showColumns"));
        notNullColumn.setText(AppProperties.getProperty("notNullColumn"));
        headerRow.setText(AppProperties.getProperty("headerRow"));
        materialColumn.setText(AppProperties.getProperty("materialColumn"));
        conNoColumn.setText(AppProperties.getProperty("conNoColumn"));
        materialRegex.setText(AppProperties.getProperty("material_regex"));
        // ініціалізація компонентів поля pathPdfFiles
        JPanel pathPdfFilesPanel = new JPanel();
        JLabel pathPdfFilesLabel = new JLabel("Шлях до теки з файлами допусків:");
        pathPdfFilesLabel.setPreferredSize(new Dimension(250, 20));
        pathPdfFilesPanel.add(pathPdfFilesLabel);
        pathPdfFilesPanel.add(pathPdfFiles);
        mainPrefPanel.add(pathPdfFilesPanel);
        // ініціалізація компонентів поля pathPdfFiles
        JPanel pdfFileRegexPanel = new JPanel();
        JLabel pdfFileRegexLabel = new JLabel("Regex для файлів допусків:");
        pdfFileRegexLabel.setPreferredSize(new Dimension(250, 20));
        pdfFileRegexPanel.add(pdfFileRegexLabel);
        pdfFileRegexPanel.add(pdfFileRegex);
        mainPrefPanel.add(pdfFileRegexPanel);
        // ініціалізація компонентів поля pathXLSXFile
        JPanel pathXLSXFilePanel = new JPanel();
        JLabel pathXLSXFileLabel = new JLabel("Шлях до таблиці модулів:");
        pathXLSXFileLabel.setPreferredSize(new Dimension(250, 20));
        pathXLSXFilePanel.add(pathXLSXFileLabel);
        pathXLSXFilePanel.add(pathXLSXFile);
        mainPrefPanel.add(pathXLSXFilePanel);
        // ініціалізація компонентів поля showColumns
        JPanel showColumnsPanel = new JPanel();
        JLabel showColumnsLabel = new JLabel("Стовпці для відображення:");
        showColumnsLabel.setPreferredSize(new Dimension(250, 20));
        showColumnsPanel.add(showColumnsLabel);
        showColumnsPanel.add(showColumns);
        mainPrefPanel.add(showColumnsPanel);
        // ініціалізація компонентів поля dateFormat
        JPanel dateFormatPanel = new JPanel();
        JLabel dateFormatLabel = new JLabel("Формат дати:");
        dateFormatLabel.setPreferredSize(new Dimension(250, 20));
        dateFormatPanel.add(dateFormatLabel);
        dateFormatPanel.add(dateFormat);
        mainPrefPanel.add(dateFormatPanel);
        // ініціалізація компонентів поля notNullColumn
        JPanel notNullColumnPanel = new JPanel();
        JLabel notNullColumnLabel = new JLabel("Назва стовпця в якому немає порожніх клітинок:");
        notNullColumnLabel.setPreferredSize(new Dimension(250, 20));
        notNullColumnPanel.add(notNullColumnLabel);
        notNullColumnPanel.add(notNullColumn);
        mainPrefPanel.add(notNullColumnPanel);
        // ініціалізація компонентів поля headerRow
        JPanel headerRowPanel = new JPanel();
        JLabel headerRowLabel = new JLabel("Номер рядка в якому знаходиться заголовок:");
        headerRowLabel.setPreferredSize(new Dimension(250, 20));
        headerRowPanel.add(headerRowLabel);
        headerRowPanel.add(headerRow);
        mainPrefPanel.add(headerRowPanel);
        // ініціалізація компонентів поля materialColumn
        JPanel materialColumnPanel = new JPanel();
        JLabel materialColumnLabel = new JLabel("Назва стовпця з матеріал номером:");
        materialColumnLabel.setPreferredSize(new Dimension(250, 20));
        materialColumnPanel.add(materialColumnLabel);
        materialColumnPanel.add(materialColumn);
        mainPrefPanel.add(materialColumnPanel);
        // ініціалізація компонентів поля conNoColumn
        JPanel conNoColumnPanel = new JPanel();
        JLabel conNoColumnLabel = new JLabel("Назва стовпця з конектор кодом:");
        conNoColumnLabel.setPreferredSize(new Dimension(250, 20));
        conNoColumnPanel.add(conNoColumnLabel);
        conNoColumnPanel.add(conNoColumn);
        mainPrefPanel.add(conNoColumnPanel);
        // ініціалізація компонентів поля materialColumn
        JPanel materialRegexPanel = new JPanel();
        JLabel materialRegexLabel = new JLabel("Patern матеріал номеру з таблиці:");
        materialRegexLabel.setPreferredSize(new Dimension(250, 20));
        materialRegexPanel.add(materialRegexLabel);
        materialRegexPanel.add(materialRegex);
        mainPrefPanel.add(materialRegexPanel);
        // додаєм вкладку в табовану панель
        tabbedPane.addTab("Допуски тест. модулів", mainPrefPanel);
    }

    // ініціалізує компоненти вкладки "Генератор протоколів"
    private void initGeneratorTab() {
        // створюємо панель загальних налаштувань
        JPanel generatorPanel = new JPanel();
        generatorPanel.setLayout(new GridLayout(10, 1));
        // задаєм текст в поля з файлу налаштувань
        templatePermitProtocolFile.setText(AppProperties.getProperty("gen.templatefile"));
        sheetInTemplatePermitProtocolFile.setText(AppProperties.getProperty("gen.templatefile.sheet"));
        genProjectName.setText(AppProperties.getProperty("gen.projectname"));
        genMaterialNr.setText(AppProperties.getProperty("gen.materialnr"));
        genSupplier.setText(AppProperties.getProperty("gen.supplier"));
        genArtNr.setText(AppProperties.getProperty("gen.artnr"));
        genPersonPPETS.setText(AppProperties.getProperty("gen.personppets"));
        genPersonPPE.setText(AppProperties.getProperty("gen.personppe"));
        genPersonPQM.setText(AppProperties.getProperty("gen.personpqm"));
        genProtocolDate.setText(AppProperties.getProperty("gen.protocoldate"));
        // ініціалізація компонентів поля templatePermitProtocolFile
        JPanel templatePermitProtocolFilePanel = new JPanel();
        JLabel templatePermitProtocolFileLabel = new JLabel("Файл шаблону допуску тест. модуля:");
        templatePermitProtocolFileLabel.setPreferredSize(new Dimension(250, 20));
        templatePermitProtocolFilePanel.add(templatePermitProtocolFileLabel);
        templatePermitProtocolFilePanel.add(templatePermitProtocolFile);
        generatorPanel.add(templatePermitProtocolFilePanel);
        // ініціалізація компонентів поля templatePermitProtocolFile
        JPanel sheetInTemplatePermitProtocolFilePanel = new JPanel();
        JLabel sheetInTtemplatePermitProtocolFileLabel = new JLabel("Назва листа:");
        sheetInTtemplatePermitProtocolFileLabel.setPreferredSize(new Dimension(250, 20));
        sheetInTemplatePermitProtocolFilePanel.add(sheetInTtemplatePermitProtocolFileLabel);
        sheetInTemplatePermitProtocolFilePanel.add(sheetInTemplatePermitProtocolFile);
        generatorPanel.add(sheetInTemplatePermitProtocolFilePanel);
        // ініціалізація компонентів поля genProjectName
        JPanel genProjectNamePanel = new JPanel();
        JLabel genProjectNameLabel = new JLabel("Комірка \"Назва проекту\":");
        genProjectNameLabel.setPreferredSize(new Dimension(250, 20));
        genProjectNamePanel.add(genProjectNameLabel);
        genProjectNamePanel.add(genProjectName);
        generatorPanel.add(genProjectNamePanel);
        // ініціалізація компонентів поля genMaterialNr
        JPanel genMaterialNrPanel = new JPanel();
        JLabel genMaterialNrLabel = new JLabel("Комірка \"Матеріал номер\":");
        genMaterialNrLabel.setPreferredSize(new Dimension(250, 20));
        genMaterialNrPanel.add(genMaterialNrLabel);
        genMaterialNrPanel.add(genMaterialNr);
        generatorPanel.add(genMaterialNrPanel);
        // ініціалізація компонентів поля genSupplier
        JPanel genSupplierPanel = new JPanel();
        JLabel genSupplierLabel = new JLabel("Комірка \"№ замовлення постачальника\":");
        genSupplierLabel.setPreferredSize(new Dimension(250, 20));
        genSupplierPanel.add(genSupplierLabel);
        genSupplierPanel.add(genSupplier);
        generatorPanel.add(genSupplierPanel);
        // ініціалізація компонентів поля genArtNr
        JPanel genArtNrPanel = new JPanel();
        JLabel genArtNrLabel = new JLabel("Комірка \"№ тест модуля\":");
        genArtNrLabel.setPreferredSize(new Dimension(250, 20));
        genArtNrPanel.add(genArtNrLabel);
        genArtNrPanel.add(genArtNr);
        generatorPanel.add(genArtNrPanel);
        // ініціалізація компонентів поля genPersonPPETS
        JPanel genPersonPPETSPanel = new JPanel();
        JLabel genPersonPPETSLabel = new JLabel("Комірка \"Прізвище працівника PPE-TS\":");
        genPersonPPETSLabel.setPreferredSize(new Dimension(250, 20));
        genPersonPPETSPanel.add(genPersonPPETSLabel);
        genPersonPPETSPanel.add(genPersonPPETS);
        generatorPanel.add(genPersonPPETSPanel);
        // ініціалізація компонентів поля genPersonPPE
        JPanel genPersonPPEPanel = new JPanel();
        JLabel genPersonPPELabel = new JLabel("Комірка \"Прізвище працівника PPE\":");
        genPersonPPELabel.setPreferredSize(new Dimension(250, 20));
        genPersonPPEPanel.add(genPersonPPELabel);
        genPersonPPEPanel.add(genPersonPPE);
        generatorPanel.add(genPersonPPEPanel);
        // ініціалізація компонентів поля headerRow
        JPanel genPersonPQMPanel = new JPanel();
        JLabel genPersonPQMLabel = new JLabel("Комірка \"Прізвище працівника PQM\":");
        genPersonPQMLabel.setPreferredSize(new Dimension(250, 20));
        genPersonPQMPanel.add(genPersonPQMLabel);
        genPersonPQMPanel.add(genPersonPQM);
        generatorPanel.add(genPersonPQMPanel);
        // ініціалізація компонентів поля genProtocolDate
        JPanel genProtocolDatePanel = new JPanel();
        JLabel genProtocolDateLabel = new JLabel("Комірка \"Дата допуску\":");
        genProtocolDateLabel.setPreferredSize(new Dimension(250, 20));
        genProtocolDatePanel.add(genProtocolDateLabel);
        genProtocolDatePanel.add(genProtocolDate);
        generatorPanel.add(genProtocolDatePanel);
        // додаєм вкладку в табовану панель
        tabbedPane.addTab("Генератор протоколів", generatorPanel);
    }

    // ініціалізує компоненти вкладки "Бази даних"
    private void initDBTab() {
        // створюємо панель загальних налаштувань
        JPanel dbPrefPanel = new JPanel();
        dbPrefPanel.setLayout(new BorderLayout());
        DefaultTableModel tableModel = new DefaultTableModel();
        dbTable = new JTable(tableModel);
        dbTable.getTableHeader().setReorderingAllowed(false); // забороняєм совати стовпці
        // створюєм загаловки таблиці
        tableModel.addColumn("Назва налаштування");
        tableModel.addColumn("Сервер БД");
        tableModel.addColumn("Назва БД");
        tableModel.addColumn("Користувач");
        tableModel.addColumn("Пароль");
        // отримуєм рядок всіх баз даних
        String dbs = AppProperties.getProperty("data_bases");
        // заповнюєм таблицю налаштуваннями баз даних
        if (dbs != null && !dbs.equals("")) {
            String[] dbArr = dbs.split("~");
            // формуємо налаштування до кожної бази даних
            for (String db : dbArr) {
                String dbServer = AppProperties.getProperty("db." + db + ".server");
                String dbName = AppProperties.getProperty("db." + db + ".name");
                String dbUser = AppProperties.getProperty("db." + db + ".user");
                String dbPass = AppProperties.getProperty("db." + db + ".pass");
                tableModel.addRow(new Object[]{db, dbServer, dbName, dbUser, dbPass});
            }
        }
        // додаєм порожній рядок для можливості додавання нового налаштування\
        tableModel.addRow(new Object[]{"", "", "", "", ""});
        // додаєм слухача на зміни в таблиці, для видалення порожній рядків, або для додавання нових рядків
        dbTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == -1)
                    return;
                // перевіряємо чи рядок не є порожнім, якщо весь рядок порожній то видаляємо його
                String cellValue = (String) tableModel.getValueAt(row, column);
                if ((row != tableModel.getRowCount() - 1) && cellValue.equals("")) {
                    String value1 = (String) tableModel.getValueAt(row, 0);
                    String value2 = (String) tableModel.getValueAt(row, 1);
                    String value3 = (String) tableModel.getValueAt(row, 2);
                    String value4 = (String) tableModel.getValueAt(row, 3);
                    String value5 = (String) tableModel.getValueAt(row, 4);
                    if (value1.equals("") && value2.equals("") && value3.equals("") && value4.equals("") && value5.equals("")) {
                        tableModel.removeRow(row);
                    }
                }
                // якщо всі рядки заповнені, додаємо новий - порожній
                if (tableModel.getRowCount() == 0 || (tableModel.getRowCount() - 1) == row && !cellValue.equals("")) {
                    tableModel.addRow(new Object[]{"", "", "", "", ""});
                }
            }
        });
        // додаєм вкладку в табовану панель
        JScrollPane scroll = new JScrollPane(dbTable);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dbPrefPanel.add(scroll);
        // в іншому випадку зсовує панель з кнопками (ok, cancel) вниз
        dbPrefPanel.setPreferredSize(new Dimension(dbPrefPanel.getSize().width, dbPrefPanel.getSize().height - 20));
        tabbedPane.addTab("Бази даних", dbPrefPanel);
    }

    // метод ініціалізації керуючих кнопок - "Зберегти" і "Скасувати"
    private void initControlButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        JButton okButton = new JButton("Save");
        okButton.addActionListener(e -> savePref());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);
    }

    // Метод збереження налаштувань, які задав користувач в формі.
    private synchronized void savePref() {
        //
        TableModel tableModel = dbTable.getModel();
        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();
        //очищуем поточні властивості
        AppProperties.clearProperties();
        // додаєм властивості з полів
        AppProperties.setProperty("pathPdfFiles", pathPdfFiles.getText());
        AppProperties.setProperty("file_name_regex", pdfFileRegex.getText());
        AppProperties.setProperty("pathXLSXFile", pathXLSXFile.getText());
        AppProperties.setProperty("dateFormat", dateFormat.getText());
        AppProperties.setProperty("showColumns", showColumns.getText());
        AppProperties.setProperty("notNullColumn", notNullColumn.getText());
        AppProperties.setProperty("headerRow", headerRow.getText());
        AppProperties.setProperty("materialColumn", materialColumn.getText());
        AppProperties.setProperty("conNoColumn", conNoColumn.getText());
        AppProperties.setProperty("material_regex", materialRegex.getText());
        // додаєм властивості генератора протоколів
        AppProperties.setProperty("gen.templatefile", templatePermitProtocolFile.getText());
        AppProperties.setProperty("gen.templatefile.sheet", sheetInTemplatePermitProtocolFile.getText());
        AppProperties.setProperty("gen.projectname", genProjectName.getText());
        AppProperties.setProperty("gen.materialnr", genMaterialNr.getText());
        AppProperties.setProperty("gen.supplier", genSupplier.getText());
        AppProperties.setProperty("gen.artnr", genArtNr.getText());
        AppProperties.setProperty("gen.personppets", genPersonPPETS.getText());
        AppProperties.setProperty("gen.personppe", genPersonPPE.getText());
        AppProperties.setProperty("gen.personpqm", genPersonPQM.getText());
        AppProperties.setProperty("gen.protocoldate", genProtocolDate.getText());
        // додаєм параметри підключення до баз даних
        String cellValue;
        StringBuilder allDB = new StringBuilder();
        String currentDB;
        // прохід по всіх рядкам таблиці
        for (int lineIndex = 0; lineIndex < rowCount - 1; lineIndex++) {
            currentDB = ((String) tableModel.getValueAt(lineIndex, 0)).trim();
            allDB.append(currentDB);
            allDB.append("~");
            // прохід по всіх стовпцях
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                Object value =  tableModel.getValueAt(lineIndex, columnIndex);
                if(value == null)
                    cellValue = null;
                else
                    cellValue = ((String) tableModel.getValueAt(lineIndex, columnIndex)).trim();
                if (cellValue == null || cellValue.length() == 0) {
                    Frames.showErrorMessage(this, "Налаштування не збережено!\nНе заповнені всі значення в налаштуваннях баз даних.");
                    AppProperties.loadPropertiesFromFile();
                    return;
                }
                // визначаєм назву властивості, через індекс стовпчика
                switch (columnIndex) {
                    case 1:
                        AppProperties.setProperty("db." + currentDB + ".server", cellValue.toString());
                        break;
                    case 2:
                        AppProperties.setProperty("db." + currentDB + ".name", cellValue.toString());
                        break;
                    case 3:
                        AppProperties.setProperty("db." + currentDB + ".user", cellValue.toString());
                        break;
                    case 4:
                        AppProperties.setProperty("db." + currentDB + ".pass", cellValue.toString());
                        break;
                }
            }
        }
        AppProperties.setProperty("data_bases", allDB.toString());
        // зберігаєм налаштування
        AppProperties.savePropertiesToFile();
        // якщо властивості програми не валідні, задаєм, що валідні, оскільки тільки що зберегли налаштування
        if(!AppProperties.isPropertiesOK())
            AppProperties.setPropertiesOK(true);
        this.dispose();
    }
}
