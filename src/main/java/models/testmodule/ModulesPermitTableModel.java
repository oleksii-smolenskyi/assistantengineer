package models.testmodule;

import models.Filterable;
import models.Observer;
import models.Progressable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.AppProperties;
import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Реалізує табличну модель даних для її завантаження в JTable, для цього розширює клас AbstractTableModel.
 * <br>
 * Реалізує інтерфейси:
 * <br>
 * - Progressable - для отримання інформації щодо процесу ініціалізації об'єкта
 * <br>
 * - Filterable - для можливості фільтрування даних в табличній моделі
 * @author Oleksii Smolenskyi
 * @see AbstractTableModel
 * @see Progressable
 * @see Filterable
 *
 */
public class ModulesPermitTableModel extends AbstractTableModel implements Progressable, Filterable, models.Observable {
    private static final Logger LOGGER = LogManager.getLogger(ModulesPermitTableModel.class.getName());
    private String pathPdfFiles;    // шлях до папки з пдф файлами допусків
    private String XLSXFile;    // шлях до xlsx табельки
    private List<String> headersFromXLSX;   // список заголовків колонок в xlsx табельці
    private List<String> showHeaders;   // список заголовків колонок які відображати
    private List<String> tableHeaders;   // список заголовків колонок які відображати
    private int rowNrHeader;        // номер рядка заголовку листа
    private int colNrNotNull;       // номер стовпця в якому не може бути пустих значень, якщо значення пусте, значить кінець листа
    private String materialColumn; // назва стовпця Material в таблиці Excel
    private int materialColumnId; // номер стовпця Material в таблиці Excel
    private List<ModuleForPermitModel> modulesFromXLSX; // список зчитаних модулів з xlsx табельки
    private List<ModuleForPermitModel> modulesToShow; // список зчитаних модулів з xlsx табельки
    private Map<Integer, Set<String>> filters = new HashMap<>(); // карта фільтрів таблиці Map<Integer column index, Set<String> items to show>
    private Map<String, List<File>> modulesFilesMap; // список зчитаних назв файлів з папки, формат <матеріал номер, список файлів до цього матеріал номера
    // поля необхідні для завантаження Excel файлу
    private XSSFWorkbook excelBook; // xlsx книга
    private XSSFSheet excelSheet;   // sheet Order
    private DataFormatter formatter = new DataFormatter(); // Formatter for reading cells
    // поля необхідні для контролю процесу ініціалізації обєкта
    private IOException exception; // якщо ініціалізація обєкта закінчиться невдало, тут буде збережено виключення на якому спинилась ініціалізація
    private volatile int ready = -1; // чи готовий обЄкт до використання 100 - готовий, -1 - ініціалізація завершилася помилкою(використовувати обєкт далі неможливо) 0 - 99 - обєкт в процесі ініціалізації
    private List<models.Observer> observers = new ArrayList<>();
    private String statusMessage;

    /**
     * Ініціалізує об'єкт, завантажує файл властивостей, зчитує назви файлів з теки допусків, завантажує таблицю з модулями
     *
     * @throws IOException
     */
    public ModulesPermitTableModel() throws IOException {
        // завантажуємо властивості
        loadProperties();
        // перевіряємо чи існує тека з файлами допусків модулів
        if (pathPdfFiles == null || !new File(pathPdfFiles).exists())
            throw new IOException("Path pathPdfFiles: " + pathPdfFiles + " not found!");
        // перевіряємо чи існує файл таблиці з існуючими тест модулями
        if (XLSXFile == null || !new File(XLSXFile).exists())
            throw new IOException("Path XLSXFile: " + XLSXFile + " not found!");
        // готовність об'єкта до використання = 0
        ready = 0;
        // завантажуєм список існуючих допусків
        loadListFromFolder();
        // завантажуєм табельку з модулями
        loadModulesFromXLSX();
    }

    /**
     * Додає спостерігача.
     *
     * @param o спостерігач, який реалізує інтерфейс models.Observer
     */
    @Override
    public void addObserver(models.Observer o) {
        observers.add(o);
    }

    /**
     * Видаляє спостерігача.
     * @param o спостерігач, який реалізує інтерфейс models.Observer
     */
    @Override
    public void removeObserver(models.Observer o) {
        observers.remove(o);
    }

    /**
     * Сповіщає спостерігачів про зміни в заголовку таблиці
     */
    @Override
    public void update() {
        if (observers == null)
            return;

        for (Observer o : observers) {
            o.update();
        }
    }

    /**
     * Повертає кількість рядків таблиці(кількість зчитаних модулів з таблиці)
     *
     * @return кількість рядків табличної моделі
     */
    @Override
    public int getRowCount() {
        return modulesToShow.size();
    }

    /**
     * Повертає кількість стовпців, тобто кількість зчитуваних полів з таблиці + 2
     * (1 стовпець - номер рядка в табельці, останній стовпець чи є допуск даного тест модуля)
     *
     * @return кількість стовпців в табличній моделі
     */
    @Override
    public int getColumnCount() {
        return tableHeaders.size();
    }

    /**
     * Повертає значення конкретної клітинки, по вхідним параметра номеру рядка і стовпця табличної моделі.
     *
     * @param rowIndex    номер рядка таблиці
     * @param columnIndex номер стовпця таблиці
     * @return Значення конкретної клітинки таблиці
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // Якщо індекси виходять за межі таблиці
        if(columnIndex < 0 || columnIndex >= getColumnCount() || rowIndex < 0 || rowIndex >= getRowCount())
            return null;
        return modulesToShow.get(rowIndex).getParam(columnIndex);
    }

    /**
     * Метод для визначення заголовку стовпця по індексу в таблиці
     *
     * @param c номер стовпця табличної моделі
     * @return заголовок стовпця
     */
    @Override
    public String getColumnName(int c) {
        // якщо індекс більший чим загальна кількість стовпчиків
        if (c > tableHeaders.size())
            return null;
        return tableHeaders.get(c);
    }

    /**
     * Метод для визначення номеру стовпця по назві в таблиці
     *
     * @param columnName назва стовпця табличної моделі
     * @return індекс стовпця по його назві
     */
    public int getColumnId(String columnName) {
        return tableHeaders.indexOf(columnName);
    }

    /**
     * @param idColumn
     * @return Set елементів стовпчика по індексу idColumn
     */
    @Override
    public Set<String> getFilterColumnItems(int idColumn) {
        Set<String> result = new TreeSet<>();
        Object item;
        String itemS;
        List<Module> modulesFiltered = new ArrayList<>();
        if (filters.size() > 1) {
            for (int i = 0; i < modulesFromXLSX.size(); i++) {
                boolean addToresult = true;
                for (Map.Entry<Integer, Set<String>> entry : filters.entrySet()) {
                    if (entry.getKey() == idColumn)
                        continue;
                    if (!entry.getValue().contains(modulesFromXLSX.get(i).getParam(entry.getKey()))) {
                        addToresult = false;
                        break;
                    }
                }
                if (addToresult) {
                    modulesFiltered.add(modulesFromXLSX.get(i));
                }
            }
        } else {
            modulesFiltered = new ArrayList<>(modulesFromXLSX);
        }

        for (int i = 0; i < modulesFiltered.size(); i++) {
            item = modulesFiltered.get(i).getParam(idColumn);
            if (item != null) {
                itemS = "" + item;
                if (!itemS.equals(""))
                    result.add(itemS);
                else
                    result.add("Пусто");
            } else {
                result.add("Пусто");
            }
        }
        return result;
    }

    /**
     * Забезпечує фільтрування таблиці, по номеру стовпчика і сету елементів згідно якого відбувається фільтрування
     *
     * @param idColumn  номер колонки по якій додаємо фільтр
     * @param newFilter Set елементів по якому фільтруватимем
     */
    @Override
    public void addFilter(int idColumn, Set<String> newFilter) {
        Set<String> filter = new LinkedHashSet<>(newFilter);
        if (filter.contains("Пусто")) {
            filter.add(null);
            filter.remove("Пусто");
        }
        // оголошуєм новий список модулів - результуючий після фільтрування
        List<ModuleForPermitModel> newModulesList = new ArrayList<>();
        // якщо по цьому індексу стовпчика вже є фільтр
        if (filters.containsKey(idColumn)) {
            LOGGER.debug("Replace filter in column " + idColumn + " New filter: " + newFilter + " All Items: " + getFilterColumnItems(idColumn));
            if (getFilterColumnItems(idColumn).equals(newFilter) && newFilter.equals(getFilterColumnItems(idColumn))) {
                LOGGER.debug("Remove filter in column " + idColumn);
                String newHeader = tableHeaders.get(idColumn).replace("▼ ", "");
                tableHeaders.set(idColumn, newHeader);
                update();
                filters.remove(idColumn);
            } else {
                // додаємо фільтр
                putNewFilter(idColumn, filter);
            }
            // проходимо по всіх модулях з табельки
            for (int i = 0; i < modulesFromXLSX.size(); i++) {
                boolean checkOK = true;
                // прохід по всіх фільтрах
                for (Map.Entry<Integer, Set<String>> entry : filters.entrySet()) {
                    if (!entry.getValue().contains(modulesFromXLSX.get(i).getParam(entry.getKey()))) {
                        checkOK = false;
                        break;
                    }
                }
                // якщо і-тий модуль пройшов всі фільтри
                if (checkOK)
                    newModulesList.add(modulesFromXLSX.get(i));
            }
        } else { // якщо по цьому стовпчику ще не було фільтру, тоді фільтруємо не всі модулі з табельки а тільки ті, що зараз відображаються
            if (newFilter.equals(getFilterColumnItems(idColumn))) {
                //System.out.println("kill filter " + idColumn);
                //filters.remove(idColumn);

                LOGGER.debug("No filter specified");
                return;
            } else {
                String newHeader = "▼ " + tableHeaders.get(idColumn);
                tableHeaders.set(idColumn, newHeader);
                update();
                // додаємо фільтр
                //filters.put(idColumn, filter);
                LOGGER.debug("New filter in column " + idColumn + " Filter: " + newFilter + " All Items: " + getFilterColumnItems(idColumn));
                putNewFilter(idColumn, filter);
                for (int i = 0; i < getRowCount(); i++) {
                    if (filter.contains(getValueAt(i, idColumn))) {
                        newModulesList.add(modulesToShow.get(i));
                    }
                }
            }
        }
        // присвоюєм результати
        modulesToShow = newModulesList;
        // апдейтим таблицю
        this.fireTableDataChanged();
    }

    // метод для додавання фільтру
    private void putNewFilter(int idColumn, Set<String> filter) {
        Set<String> newFilter = new LinkedHashSet<>(filter);
        if (newFilter.contains("Пусто")) {
            newFilter.add(null);
            newFilter.remove("Пусто");
        }
        filters.put(idColumn, newFilter);
    }

    /*
            return Map<String material, List<File>>
            material - string material number
            List<File> - список файлів допусків до material номеру
        */
    private void loadListFromFolder() {
        statusMessage = "Завантаження допусків тест. модулів...";
        //String regex = "[0-9]{3}_[0-9]{3}_[0-9]{3}=(\\d|\\D)+(.pdf)|M-[0-9]+=(\\d|\\D)+(.pdf)";
        String regex = AppProperties.getProperty("file_name_regex");
        modulesFilesMap = new HashMap<>();
        // отримуємо список всіх файлів в папці
        File[] files = new File(pathPdfFiles).listFiles();
        // проходимо по масиву файлів
        for (File file : files) {
            // формуєм material номер
            String material = file.getName();
            if (material.matches(regex)) {
                // берем значення до знаку '=' і видаляєм всі '_' i '-', '_' - TSK, '-' - Kufferath
                material = material.substring(0, file.getName().indexOf("=")).replaceAll("_", "").replaceAll("-", "");
            }
            // заповнюємо мапу результатами
            if (modulesFilesMap.containsKey(material)) {
                List<File> tmpList = modulesFilesMap.get(material);
                tmpList.add(file);
            } else {
                List<File> list = new ArrayList<File>();
                list.add(file);
                modulesFilesMap.put(material, list);
            }
        }
        upReady(10);
    }

    /**
     * Завантажує тест. модулі з файлу xlsx
     * <p>
     * Запускає окремий потік, в якому завантажує <code>excelBook</code> книгу і зчитує тест модулі.
     * </p>
     */
    private void loadModulesFromXLSX() {
        Thread thread = new Thread(() -> {
            try {
                statusMessage = "Ініціалізація табельки з тест. модулями...";
                initXLSXFile(); // ініціалізуємо книгу
                upReady(50);
                readHeaders();  // читаєм заголовок
                upReady(10);
                statusMessage = "Зчитування модулів з табельки...";
                readModules();  // читаєм модулі
                upReady(30);
                statusMessage = "Готово...";
            } catch (IOException e) {
                ready = -1;
                LOGGER.error(e.getMessage());
                exception = new IOException(e.getMessage());
            } finally {
                try {
                    excelBook.close();
                    excelSheet = null;
                    excelBook = null;
                    System.gc();
                    LOGGER.info("Book " + XLSXFile + " closed.");
                } catch (Exception e) {
                    LOGGER.error("Book cann't close.");
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @return
     */
    public IOException getStopException() {
        return exception;
    }

    // метод завантаження властивостей і ініціалізації певних змінних
    private void loadProperties() throws IOException {
        pathPdfFiles = AppProperties.getProperty("pathPdfFiles");
        XLSXFile = AppProperties.getProperty("pathXLSXFile");
        LOGGER.debug("[properties] pathPdfFiles: " + pathPdfFiles);
        LOGGER.debug("[properties] pathXLSXFile: " + XLSXFile);
        String[] headers = AppProperties.getProperty("showColumns").split("~");
        showHeaders = new ArrayList<>();
        for (String value : headers) {
            showHeaders.add(value.trim());
        }
        tableHeaders = new ArrayList<>(showHeaders);
        tableHeaders.add(0, "line");
        tableHeaders.add("Допуск");
        // зчитуєм номер рядка заголовка табельки з модулями
        rowNrHeader = 0;
        try {
            rowNrHeader = Integer.parseInt(AppProperties.getProperty("headerRow"));
        } catch (NumberFormatException nfe) {
            LOGGER.warn("Bad format properties file in key \'headerRow\'");
        }
        // зчитуєм назву стовпця з матеріал номером
        materialColumn = AppProperties.getProperty("materialColumn");
        if (materialColumn == null) {
            throw new IOException("Bad format properties file in key \'materialColumn\'");
        }
    }

    // метод для читання заголовків колонок з файлу xlsx
    private void readHeaders() throws IOException {
        // отримуємо список заголовків
        if (excelBook != null && excelSheet != null) {
            Row row = excelSheet.getRow(rowNrHeader);
            headersFromXLSX = new ArrayList<>();
            int iCellHeader = 0;
            Cell cellHeader = row.getCell(iCellHeader, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            while (cellHeader != null) {
                headersFromXLSX.add(formatter.formatCellValue(cellHeader).trim());
                iCellHeader++;
                cellHeader = row.getCell(iCellHeader, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            }
            if (!headersFromXLSX.containsAll(showHeaders))
                throw new IOException("XLSX file doesn't contain headersFromXLSX for showing: " + Arrays.toString(showHeaders.toArray()) + "! Correct properties file.");
            // ----
        } else {
            throw new IOException("Book or sheet not initialized!");
        }
        materialColumnId = tableHeaders.indexOf(materialColumn);
        // перевіряємо чи в зчитаних заголовках є назва поля, яке не може бути пустим, з файлу налаштувань
        if (headersFromXLSX.contains(AppProperties.getProperty("notNullColumn"))) {
            colNrNotNull = headersFromXLSX.indexOf(AppProperties.getProperty("notNullColumn"));
        } else {
            throw new IOException("Bad format properties file in key \'notNullColumn\'! Column " + AppProperties.getProperty("notNullColumn") + " not found.");
        }
    }

    // load xlsx file
    private void initXLSXFile() throws IOException {
        ZipSecureFile.setMinInflateRatio(0);
        modulesFromXLSX = new ArrayList<>();
        // завантаження книги
        LOGGER.info("Start load data from " + XLSXFile);
        try {
            excelBook = new XSSFWorkbook(new FileInputStream(XLSXFile));
            LOGGER.info("Book " + XLSXFile + " loaded.");
        } catch (IOException e) {
            throw new IOException("Error loaded xlsx file " + XLSXFile);
        }
        // визначення листа з модулями
        excelSheet = excelBook.getSheet(AppProperties.getProperty("sheetName", "Order"));
        if (excelSheet == null) {
            throw new IOException("Error loaded sheet " + AppProperties.getProperty("sheetName", "Order"));
        }
    }

    // читання модулів з табельки
    private void readModules() {
        // наступний рядок після заголовку
        Row row;
        int indexRow = rowNrHeader + 1;
        row = excelSheet.getRow(indexRow);
        Cell cellNotNull = row.getCell(colNrNotNull, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        SimpleDateFormat sdf = new SimpleDateFormat(AppProperties.getProperty("dateFormat", "dd.MM.yyyy"));
        List<String> paramListModule = null;
        // створюєм маску для зчитаних матеріа номерів
        Pattern p = Pattern.compile(AppProperties.getProperty("material_regex", ""));
        // поки не кінець таблиці
        while (cellNotNull != null) {
            paramListModule = new ArrayList<>();
            // поки не кінець рядка
            for (int iCol = 0; iCol < headersFromXLSX.size(); iCol++) {
                // якщо поточна колонка відображається в результатах
                if (showHeaders.contains(headersFromXLSX.get(iCol))) {
                    Cell cell = row.getCell(iCol, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);  // читаєм комірку
                    if (cell != null) { // якщо вміст комірки не порожній
                        if (cell.getCellType() == CellType.NUMERIC) {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                paramListModule.add(sdf.format(cell.getDateCellValue()));
                                continue;
                            } else {
                                paramListModule.add(formatter.formatCellValue(cell));
                                continue;
                            }
                        } else if (cell.getCellType() == CellType.STRING) {
                            paramListModule.add(cell.getStringCellValue());
                            continue;
                        } else {
                            LOGGER.warn("Bad data in row: " + indexRow);
                        }
                    } else {
                        paramListModule.add(null);
                    }
                }
            }
            paramListModule.add(0, String.valueOf(indexRow));
            // пропускаєм матеріал-номер через маску
            String tmpMaterial = paramListModule.get(materialColumnId);
            if (paramListModule.get(materialColumnId) != null) {
                Matcher m = p.matcher(tmpMaterial);
                if (m.find()) {
                    tmpMaterial = m.group().replaceAll("-", "").replaceAll("_", "");
                }
            }
            // створюєм новий модуль на основі зчитаних значень
            ModuleForPermitModel module = new ModuleForPermitModel(paramListModule);
            // додаєм йому список допусків
            module.addPermitFiles(modulesFilesMap.get(tmpMaterial));
            modulesFromXLSX.add(module);
            indexRow++;
            row = excelSheet.getRow(indexRow);
            cellNotNull = row.getCell(colNrNotNull, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        }
        modulesToShow = new ArrayList<>(modulesFromXLSX);
    }

    /**
     * Використовується для отримання інформації чи готовий об'єкт до використання
     *
     * @return статус готовності об'єкту до використання (true/false)
     */
    public boolean isReady() {
        if (ready >= 100)
            return true;
        return false;
    }

    /**
     * Використовується для отримання інформації про %(0-100) статус ініціалізації об'єкту
     *
     * @return на скільки об'єкт класу готовий до використання
     */
    public int getReady() {
        return ready;
    }

    // метод для збільшення прогресу завантаження обєкта
    private synchronized void upReady(int up) {
        if (ready >= 0)
            ready += up;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Повертає множину файлів для тест модуля по індексу index в списку відображуваних модулів.
     *
     * @param index в списку відображуваних модулів
     * @return множину файлів допусків
     */
    public Set<File> getPermitFilesForModuleByIndex(int index) {
        if (index <= modulesToShow.size()) {
            return modulesToShow.get(index).getPermitFiles();
        }
        return null;
    }
}
