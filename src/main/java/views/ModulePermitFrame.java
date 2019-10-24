package views;

import models.Observer;
import models.testmodule.ModulesPermitTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.AppProperties;
import utils.Frames;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ModulePermitFrame extends JFrame implements Observer {
    private static final Logger LOGGER = LogManager.getLogger(ModulePermitFrame.class.getName());
    private ModulesPermitTableModel tableModel;
    private JTable jTableModules;
    private JPanel mainPanel;

    // конструктор, приймає модель таблиці, розширену від AbstractTableModel
    public ModulePermitFrame(ModulesPermitTableModel tableModel) {
        super("Допуски тест. модулів");
        this.tableModel = tableModel;
        initFrame();    // ініціалізує фрейм
        initComponents();// ініціалізуєм компоненти на фреймі
    }

    // ініціалізуєм фрейм
    private void initFrame() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setBounds(0, 0, 1000, 800);
        this.setBounds(Frames.getBoundsForCenteringFrame(this));
        this.setMinimumSize(new Dimension(800, 600));
        this.setLayout(new BorderLayout());
    }

    // ініціалізуєм компоненти на фреймі
    private void initComponents() {
        // чекаєм ініціалізації/завантаження моделі(таблиці) модулів
        Frames.showProgressWindow(this, tableModel);
        // перевіряємо чи модель таблиці готова, не готова - закриваєм це вікно
        if (!tableModel.isReady()) {
            Frames.showErrorMessage((JFrame) null, tableModel.getStopException().getMessage());
            this.dispose();
            return;
        }
        // ініціалізуєм таблицю для відображення
        jTableModules = new JTable(tableModel);
        jTableModules.getTableHeader().setReorderingAllowed(false); // забороняєм совати стовпці
        // додаємо слухача на клік по заголовку талиці, для виклику сортувального вікна
        jTableModules.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = jTableModules.columnAtPoint(e.getPoint());
                String name = jTableModules.getColumnName(col);
                new FilterFrame(ModulePermitFrame.this, e.getXOnScreen(), e.getYOnScreen(), col, tableModel);
                //System.out.println("Column index selected " + col + " " + name);
            }
        });
        JScrollPane jScrollPane = new JScrollPane(jTableModules); // додаєм таблицю на скрол
        // mainPanel - панель таблиці результатів
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(jScrollPane); // додаєм скрол на панель
        // додаєм панель на форму
        this.add(mainPanel, BorderLayout.CENTER);
        this.setVisible(true);
        this.revalidate();
        jTableModules.getColumnModel().getColumn(0).setPreferredWidth(40);
        // викликаєм метод ініціалізації контекстного меню таблиці
        initTablePopUpMenu();
    }

    // метод ініціалізації поп-ап меню для таблиці
    private void initTablePopUpMenu() {
        // create popupMenu
        JPopupMenu popupMenu = new JPopupMenu();
        // пункт меню для збереження таблиці в файл
        JMenuItem saveToFileMenuItem = new JMenuItem("Save table to file");
        saveToFileMenuItem.addActionListener(e -> saveTableModel());
        popupMenu.add(saveToFileMenuItem);
        // пункт меню для перегляду файлу допуску для конкретного модуля(рядка)
        JMenuItem viewPermitFileMenuItem = new JMenuItem("View");
        viewPermitFileMenuItem.addActionListener(e -> {
            Frames.showSelectingAndExecutingFileFrame(this, tableModel.getPermitFilesForModuleByIndex(jTableModules.getSelectedRow()));
        });
        // пункт меню для перегляду файлу допуску для конкретного модуля(рядка)
        JMenuItem generatePermitFileMenuItem = new JMenuItem("Generate permit file");
        generatePermitFileMenuItem.addActionListener(e -> {
            generatePermitFiles(jTableModules.getSelectedRows());
        });
        // додаєм слухача мишки для таблиці
        jTableModules.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(jTableModules.getSelectedRows().length > 0)
                    popupMenu.add(generatePermitFileMenuItem, 0);
                else
                    popupMenu.remove(generatePermitFileMenuItem);
                // якщо є виділений 1 або 0 рядків тоді натисканням будь-якої кнопки миші виділяється рядок
                if (jTableModules.getSelectedRows().length == 1 && tableModel.getPermitFilesForModuleByIndex(jTableModules.getSelectedRow()) != null) {
                    popupMenu.add(viewPermitFileMenuItem);
                    int r = jTableModules.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < jTableModules.getRowCount()) {
                        jTableModules.setRowSelectionInterval(r, r);
                    } else {
                        jTableModules.clearSelection();
                    }
                    int rowindex = jTableModules.getSelectedRow();
                    if (rowindex < 0)
                        return;
                } else { // якщо виділено більше одного рядка
                    popupMenu.remove(viewPermitFileMenuItem); // видаляєм пункт перегляду
                }
            }
        });
        jTableModules.setComponentPopupMenu(popupMenu); // додаєм поп-ап меню таблиці
    }

    private Map<String, String> valuesForPermitFile;
    private boolean willGenerate; // прапор чи генеруватимуться протоколи допуску, за допомогою нього визначається, що ми натиснули кнопку ОК

    // метод генерації протоколів допуску тест модулів
    private void generatePermitFiles(int[] selectedRows) {
        XSSFWorkbook excelBook;
        XSSFSheet excelSheet;
        Pattern pattern = Pattern.compile("^(\\w+)", Pattern.CASE_INSENSITIVE);
        String templatePermitProtocolFile = AppProperties.getProperty("gen.templatefile");
        new GeneratePermitFileEditParametersDialog();
        if(willGenerate == false) {
            return;
        }
        // отримуєм координати куди вставляти значення в файлі шаблоні протоколу допуску
        Map<String, Integer[]> coordinatesValuesInTemplateFile = getCoordinatesValuesInTemplateFile();
        if (coordinatesValuesInTemplateFile == null) {
            Frames.showErrorMessage(this,  "Хибно задані параметри в налаштуваннях генерації протоколів допуску.");
            return;
        }
        for(int rowId : selectedRows) {
            try {
                excelBook = new XSSFWorkbook(new FileInputStream(templatePermitProtocolFile));
                LOGGER.debug("Book " + templatePermitProtocolFile + " loaded.");
            } catch (IOException e) {
                Frames.showErrorMessage(this,  "Помилка завантаження файла-шаблона: " + templatePermitProtocolFile);
                LOGGER.error(e.getMessage());
                break;
            }
            // визначення листа з модулями
            excelSheet = excelBook.getSheet(AppProperties.getProperty("gen.templatefile.sheet"));
            if (excelSheet == null) {
                Frames.showErrorMessage(this,  "Error loaded sheet " + AppProperties.getProperty("gen.templatefile.sheet"));
                LOGGER.error("Помилка відкриття листа " + AppProperties.getProperty("gen.templatefile.sheet") + " в файлі " + templatePermitProtocolFile);
                closeExcelWorkBook(excelBook);
                break;
            }
            // вставляєм значення з вікна значень
            excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.projectname")[0])
                    .getCell(coordinatesValuesInTemplateFile.get("gen.projectname")[1])
                    .setCellValue(valuesForPermitFile.getOrDefault("gen.projectname.value", ""));
            excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.supplier")[0])
                    .getCell(coordinatesValuesInTemplateFile.get("gen.supplier")[1])
                    .setCellValue(valuesForPermitFile.getOrDefault("gen.supplier.value", ""));
            excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.personppets")[0])
                    .getCell(coordinatesValuesInTemplateFile.get("gen.personppets")[1])
                    .setCellValue(valuesForPermitFile.getOrDefault("gen.personppets.value", ""));
            excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.personppe")[0])
                    .getCell(coordinatesValuesInTemplateFile.get("gen.personppe")[1])
                    .setCellValue(valuesForPermitFile.getOrDefault("gen.personppe.value", ""));
            excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.personpqm")[0])
                    .getCell(coordinatesValuesInTemplateFile.get("gen.personpqm")[1])
                    .setCellValue(valuesForPermitFile.getOrDefault("gen.personpqm.value", ""));
            excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.protocoldate")[0])
                    .getCell(coordinatesValuesInTemplateFile.get("gen.protocoldate")[1])
                    .setCellValue(valuesForPermitFile.getOrDefault("gen.protocoldate.value", ""));
            // вставляєм значення з табельки модулів
            StringBuilder value = new StringBuilder();
            String materialNr = "";
            String conNo = "";
            try {
                materialNr = tableModel.getValueAt(rowId, tableModel.getColumnId(AppProperties.getProperty("materialColumn"))).toString();
                value.append(materialNr).append("      ");
                Matcher m = pattern.matcher(materialNr);
                if (m.find()) {
                    materialNr = m.group();
                } else {
                    materialNr = "";
                }
                conNo = tableModel.getValueAt(rowId, tableModel.getColumnId(AppProperties.getProperty("conNoColumn"))).toString();
                value.append(conNo);
                excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.materialnr")[0])
                        .getCell(coordinatesValuesInTemplateFile.get("gen.materialnr")[1])
                        .setCellValue(value.toString());
                m = pattern.matcher(conNo);
                if (m.find()) {
                    conNo = m.group();
                    excelSheet.getRow(coordinatesValuesInTemplateFile.get("gen.artnr")[0])
                            .getCell(coordinatesValuesInTemplateFile.get("gen.artnr")[1])
                            .setCellValue(conNo);
                }
            } catch (NullPointerException npe) {
                // do nothing
            }
            // Refreshing all the formulas in a workbook
            XSSFFormulaEvaluator.evaluateAllFormulaCells(excelBook);
            // перевірка існування теки в яку зберігаються згенеровані протоколи допуску
            Path pathResult = Paths.get("generatedProtocols");
            if(!Files.exists(pathResult) || Files.isDirectory(pathResult)) {
                try {
                    Files.createDirectories(pathResult);
                } catch (IOException e) {
                    Frames.showErrorMessage(this, "Помилка створення директорії для згенерованих протоколів допуску.");
                    LOGGER.error(e.getMessage());
                    closeExcelWorkBook(excelBook);
                    break;
                }
            }
            // створення назви згенерованого файла
            String fileResult = pathResult +"\\" + materialNr + " " + new Date().toString().replaceAll(":", "") + " (" + MainFrame.getInstance().getCurrentUser().getTabelNr() + ")" + " (" + rowId + ") " + templatePermitProtocolFile;
            // запис згенерованого протоколу у файл
            try (FileOutputStream fileOut = new FileOutputStream(fileResult)) {
                excelBook.write(fileOut);
                LOGGER.debug("Book " + fileResult + " saved");
            } catch (IOException e) {
                Frames.showErrorMessage(this, "Неможливо зберегти файл: " + fileResult + ". " + e.getMessage());
            } finally {
                closeExcelWorkBook(excelBook);
            }
        }
        // знімаєм виділення з рядків
        jTableModules.clearSelection();
    }

    // Діалогове вікно задання даних для генерації протоколів допуску тест модулів
    private class GeneratePermitFileEditParametersDialog extends JDialog{

        public GeneratePermitFileEditParametersDialog() {
            super(ModulePermitFrame.this, true);
            willGenerate = false;
            this.initComponents();
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.pack();
            this.setBounds(Frames.getBoundsForCenteringFrame(this));
            this.setVisible(true);
        }

        // ініціалізує компоненти вікна
        private void initComponents() {
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridLayout(7, 1));
            //
            JPanel projectPanel = new JPanel();
            JLabel projectLabel = new JLabel("Project:");
            projectLabel.setPreferredSize(new Dimension(150, 15));
            JTextField projectField = new JTextField(20);
            projectPanel.add(projectLabel);
            projectPanel.add(projectField);
            contentPanel.add(projectPanel);
            //
            JPanel supplierPanel = new JPanel();
            JLabel supplierLabel = new JLabel("№ of suppliers order:");
            supplierLabel.setPreferredSize(new Dimension(150, 15));
            JTextField supplierField = new JTextField(20);
            supplierPanel.add(supplierLabel);
            supplierPanel.add(supplierField);
            contentPanel.add(supplierPanel);
            //
            JPanel ppetsPanel = new JPanel();
            JLabel ppetsLabel = new JLabel("PPE-TS:");
            ppetsLabel.setPreferredSize(new Dimension(150, 15));
            JTextField ppetsField = new JTextField(20);
            ppetsPanel.add(ppetsLabel);
            ppetsPanel.add(ppetsField);
            contentPanel.add(ppetsPanel);
            //
            JPanel ppePanel = new JPanel();
            JLabel ppeLabel = new JLabel("PPE:");
            ppeLabel.setPreferredSize(new Dimension(150, 15));
            JTextField ppeField = new JTextField(20);
            ppePanel.add(ppeLabel);
            ppePanel.add(ppeField);
            contentPanel.add(ppePanel);
            //
            JPanel pqmPanel = new JPanel();
            JLabel pqmLabel = new JLabel("PQM:");
            pqmLabel.setPreferredSize(new Dimension(150, 15));
            JTextField pqmField = new JTextField(20);
            pqmPanel.add(pqmLabel);
            pqmPanel.add(pqmField);
            contentPanel.add(pqmPanel);
            //
            JPanel datePanel = new JPanel();
            JLabel dateLabel = new JLabel("Дата:");
            dateLabel.setPreferredSize(new Dimension(150, 15));
            JTextField dateField = new JTextField(20);
            datePanel.add(dateLabel);
            datePanel.add(dateField);
            contentPanel.add(datePanel);
            //
            if(valuesForPermitFile != null) {
                projectField.setText(valuesForPermitFile.getOrDefault("gen.projectname.value", ""));
                supplierField.setText(valuesForPermitFile.getOrDefault("gen.supplier.value", ""));
                ppetsField.setText(valuesForPermitFile.getOrDefault("gen.personppets.value", ""));
                ppeField.setText(valuesForPermitFile.getOrDefault("gen.personppe.value", ""));
                pqmField.setText(valuesForPermitFile.getOrDefault("gen.personpqm.value", ""));
                dateField.setText(valuesForPermitFile.getOrDefault("gen.protocoldate.value", ""));
            }
            //
            JPanel buttonsPanel = new JPanel();
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> {
                Map<String, String> result = new LinkedHashMap<>();
                result.put("gen.projectname.value", projectField.getText());
                result.put("gen.supplier.value", supplierField.getText());
                result.put("gen.personppets.value", ppetsField.getText());
                result.put("gen.personppe.value", ppeField.getText());
                result.put("gen.personpqm.value", pqmField.getText());
                result.put("gen.protocoldate.value", dateField.getText());
                valuesForPermitFile = result;
                willGenerate = true; // прапор чи генеруватимуться протоколи допуску, за допомогою нього визначається, що ми натиснули кнопку ОК
                this.dispose();
            });
            JButton cancelButton = new JButton("CANCEL");
            cancelButton.addActionListener(e -> {
                //valuesForPermitFile = null;
                this.dispose();
            });
            buttonsPanel.add(okButton);
            buttonsPanel.add(cancelButton);
            contentPanel.add(buttonsPanel);
            this.getContentPane().add(contentPanel);
        }
    }

    // повертає карту координат куди потрібно задавати значення в файлі допуску тест модуля
    private Map<String, Integer[]> getCoordinatesValuesInTemplateFile() {
        // витяг координат полів в файлі допуску тест модуля
        Map<String, Integer[]> genProp = new LinkedHashMap<>();
        String[] tmpArr;
        String key;
        Integer[] tmpIntArr = new Integer[2];
        try {
            // x:y gen.projectname
            key = "gen.projectname";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
            // x:y gen.materialnr
            tmpIntArr = new Integer[2];
            key = "gen.materialnr";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
            // x:y gen.supplier
            tmpIntArr = new Integer[2];
            key = "gen.supplier";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
            // x:y gen.supplier
            tmpIntArr = new Integer[2];
            key = "gen.artnr";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
            // x:y gen.supplier
            tmpIntArr = new Integer[2];
            key = "gen.personppets";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
            // x:y gen.supplier
            tmpIntArr = new Integer[2];
            key = "gen.personppe";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
            // x:y gen.supplier
            tmpIntArr = new Integer[2];
            key = "gen.personpqm";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
            // x:y gen.supplier
            tmpIntArr = new Integer[2];
            key = "gen.protocoldate";
            tmpArr = AppProperties.getProperty(key).split(":");
            tmpIntArr[0] = Integer.parseInt(tmpArr[0]);
            tmpIntArr[1] = Integer.parseInt(tmpArr[1]);
            genProp.put(key, tmpIntArr);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
        return genProp;
    }

    // метод закриття ексель книги
    private void closeExcelWorkBook(Workbook excelBook) {
        // Closing the workbook
        if (excelBook != null) {
            try {
                excelBook.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    // метод збереження таблиці в файл
    private void saveTableModel() {
        JFileChooser fileChooser = new JFileChooser();
        // створюєм фільтри файлів у вікні вибору файла
        FileNameExtensionFilter xlsxFilter = new FileNameExtensionFilter("Microsoft Excel Documents", "xlsx");
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("Documents comma-separated values", "csv");
        fileChooser.addChoosableFileFilter(xlsxFilter);
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setAcceptAllFileFilterUsed(false); // вирубуєм фільтр "All files"
        fileChooser.setDialogTitle("Specify a file to save");
        // поточна тека вікна вибору, будем зберігати для того, щоб якщо потрібно повторно зробити вибір, щоб відображало зразу теку ту яка була вибрана перед цим
        File fileToSave = null;
        // показуєм діалогове вікно вибору файлу для збереження
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = fileChooser.getSelectedFile(); // вибраний файл
            //для случая, когда каталог может иметь ".", но само имя файла не имеет значения (например, /path/to.a/file)
            String extension = null; // розширення вибраного файлу
            int pointIndex = fileToSave.getAbsolutePath().lastIndexOf('.');
            int p = Math.max(fileToSave.getAbsolutePath().lastIndexOf('/'), fileToSave.getAbsolutePath().lastIndexOf('\\'));
            if (pointIndex > p) {
                extension = fileToSave.getAbsolutePath().substring(pointIndex + 1);
            }
			/*
			Якщо користувач не ввів розширення, або ввів розширення, яке не відповідає обраному фільтру,
			тоді додаємо розширення яке потрібне.
			 */
            switch (fileChooser.getFileFilter().getDescription()) {
                case "Documents comma-separated values":
                    if (extension == null || !extension.toLowerCase().equals("csv")) {
                        extension = "csv";
                        fileToSave = new File(fileToSave.getAbsolutePath() + "." + extension);
                    }
                    break;
                case "Microsoft Excel Documents":
                    if (extension == null || !extension.toLowerCase().equals("xlsx")) {
                        extension = "xlsx";
                        fileToSave = new File(fileToSave.getAbsolutePath() + "." + extension);
                    }
                    break;
            }
            // перевіряєм чи такий файл вже існує
            if (fileToSave.exists()) {
                int dialogButton = JOptionPane.YES_NO_OPTION;
                int dialogResult = JOptionPane.showConfirmDialog(this, "Замінити існуючий файл?", "Choose", dialogButton);
                if (dialogResult != 0) {
                    // if NO
                    return;
                }
            }
            // зберігаєм файл згідно обраного формату
            switch (extension.toLowerCase()) {
                case "csv":
                    saveTableToCSV(fileToSave);
                    break;
                case "xlsx":
                    saveTableToXLSX(fileToSave);
                    break;
                default:
                    Frames.showErrorMessage(this, "Такий формат файлу не підтримується!");
                    fileToSave = null;
            }
        }
    }

    // метод збереження таблиці в файл формату CSV
    private void saveTableToCSV(File fileToSave) {
        // write file
        try (FileWriter writer = new FileWriter(fileToSave)) {
            StringBuilder string = new StringBuilder();
            // записуєм заголовки
            for (int i = 0; i < jTableModules.getColumnModel().getColumnCount(); i++) {
                string.append(jTableModules.getColumnModel().getColumn(i).getHeaderValue());
                string.append(";");
            }
            writer.write(string.toString() + "\n");
            // записуєм модулі
            for (int i = 0; i < jTableModules.getRowCount(); i++) {
                string.setLength(0);
                for (int j = 0; j < jTableModules.getColumnCount(); j++) {
                    String s = "" + jTableModules.getValueAt(i, j);
                    if (s == null || s.equals("null"))
                        s = "";
                    else
                        s = s.replaceAll(";", "");
                    s = s.replaceAll("\n", "");
                    string.append(s);
                    string.append(";");
                }
                string.append("\n");
                writer.write(string.toString());
            }
        } catch (IOException e) {
            Frames.showErrorMessage(this, "Помилка зберігання файлу " + fileToSave.getAbsolutePath());
        }
    }

    // метод збереження таблиці в файл XLSX
    private void saveTableToXLSX(File fileToSave) {
        // Create a Workbook
        Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
        /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();
        // Create a Sheet
        Sheet sheet = workbook.createSheet("modules");
        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setColor(IndexedColors.RED.getIndex());
        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        // Create a Row
        Row headerRow = sheet.createRow(0);
        // записуєм заголовки
        // Create cells
        for (int i = 0; i < jTableModules.getColumnModel().getColumnCount(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(jTableModules.getColumnModel().getColumn(i).getHeaderValue().toString());
            cell.setCellStyle(headerCellStyle);
        }
        // Create Cell Style for formatting Date
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        // записуєм модулі
        int rowStart = 1;
        for (int i = 0; i < jTableModules.getRowCount(); i++) {
            Row row = sheet.createRow(i + rowStart);
            for (int j = 0; j < jTableModules.getColumnCount(); j++) {
                if (jTableModules.getValueAt(i, j) == null)
                    row.createCell(j).setCellValue((String) null);
                else
                    row.createCell(j).setCellValue(jTableModules.getValueAt(i, j).toString());
            }
        }
        // Resize all columns to fit the content size
        for (int i = 0; i < jTableModules.getColumnModel().getColumnCount(); i++) {
            sheet.autoSizeColumn(i);
        }
        // Write the output to a file
        try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            Frames.showErrorMessage(this, "Неможливо зберегти файл: " + fileToSave + ". " + e.getMessage());
        } finally {
            // Closing the workbook
            closeExcelWorkBook(workbook);
        }
    }

    // метод для перезавантаження заголовку таблиці, при фільтруванні таблиці додається/видаляється символ в заголовку
    @Override
    public void update() {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            jTableModules.getColumnModel().getColumn(i).setHeaderValue(tableModel.getColumnName(i));
        }
        jTableModules.getTableHeader().repaint();
    }


}