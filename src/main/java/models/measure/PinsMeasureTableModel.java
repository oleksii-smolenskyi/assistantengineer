package models.measure;

import models.Filterable;
import models.Observable;
import models.Observer;
import models.Progressable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 *
 */
public class PinsMeasureTableModel extends AbstractTableModel implements Progressable, Filterable, Observable {
    private static final Logger LOGGER = LogManager.getLogger(PinsMeasureTableModel.class.getName());
    // рядок sql запиту до бази даних на вибірку вимірів голок
    private static final String querySql = "SELECT FK50_Measure_CompIDs.TestSystem, ModuleTemplate_Modules.XCode, FK50_Measure_Pins.Pin, FK50_Measure_Pins.MeasuredValue, FK50_PinsModules.SpringForce, FK50_Measure_Pins.TestDate, FK50_PinType.Description, FK50_PinType.TolerancePlus, FK50_PinType.ToleranceMinus FROM FK50_Measure_CompIDs, FK50_Measure_Pins, ModuleTemplate_Modules, FK50_PinsModules, FK50_PinType WHERE (((ModuleTemplate_Modules.ModuleTableID)=[FK50_Measure_CompIDs].[ModuleTableID]) AND ((ModuleTemplate_Modules.ModuleId)=[FK50_Measure_CompIDs].[ModuleID]) AND ((FK50_Measure_CompIDs.EntryID)=[FK50_Measure_Pins].[EntryID]) AND ((FK50_PinsModules.ModuleTableID)=[FK50_Measure_CompIDs].[ModuleTableID]) AND ((FK50_PinsModules.ModuleID)=[FK50_Measure_CompIDs].[ModuleID]) AND ((FK50_PinsModules.Pin)=[FK50_Measure_Pins].[Pin]) AND ((FK50_PinType.SAP_No)=[FK50_PinsModules].[PinType_SAP_No])) ORDER BY FK50_Measure_CompIDs.TestSystem, ModuleTemplate_Modules.XCode, FK50_Measure_Pins.Pin;";
    private String server;  // ip сервера
    private String nameDB;  // назва бази даних
    private String userName;// користувач
    private String userPass;// пароль користувача
    List<PinMeasureModel> pinsMeasure = new ArrayList<>(); // зчитані виміри голок
    List<PinMeasureModel> pinsMeasuredToShow = new ArrayList<>(); // зчитані виміри голок
    private int ready; // готовність об'єкта до використання
    private List<String> tableHeaders = new ArrayList<>(Arrays.asList(new String[]{"Назва столу", "Тест модуль", "Пін", "Виміряне значення", "Сила пружності", "Дата", "Голка", "+ Толеранція %", "- Толеранція %", "Результат"}));
    private IOException exception;
    private Map<Integer, Set<String>> filters = new HashMap<>(); // карта фільтрів таблиці Map<Integer column index, Set<String> items to show>


    /**
     * Ініціалізує об'єкт згідно зчитаних даних з бази даних, з якою відбувається з'єднання згідно вхідних параметрів.
     * @param server DNS ім'я або ip-адреса
     * @param nameDB назва бази даних
     * @param userName ім'я користувача
     * @param userPass пароль користувача
     * @throws IOException
     */
    public PinsMeasureTableModel(String server, String nameDB, String userName, String userPass) throws IOException {
        if(server != null && !server.equals(""))
            this.server = server;
        else
            throw new IOException("Не заданий параметр підключення: server ip");
        if(nameDB != null && !nameDB.equals(""))
            this.nameDB = nameDB;
        else
            throw new IOException("Не заданий параметр підключення: database name");
        if(userName != null && !userName.equals(""))
            this.userName = userName;
        else
            throw new IOException("Не заданий параметр підключення: user name");
        if(userPass != null && !userPass.equals(""))
            this.userPass = userPass;
        else
            throw new IOException("Не заданий параметр підключення: user password");
        LOGGER.info("Параметри підключення до бази даних: сервер: " + server + " база даних: " + nameDB + " користувач: " + userName);
        loadData();
    }

    /**
     * Ініціалізує об'єкт згідно зчитаних даних з файлу бази даних
     * @param dbFile файл бази даних
     * @throws IOException
     */
    public PinsMeasureTableModel(File dbFile) throws IOException {
        LOGGER.debug("Завантаження даних з локальонго файлу mdb " + dbFile);
        if(dbFile == null) {
            throw new IOException("Не задано файл бази даних.");
        }
        if(!Files.exists(Paths.get(dbFile.toURI()))) {
            throw new IOException("Файл " + dbFile + " не існує.");
        }
        loadDataFromMDB(dbFile);
    }

    private void loadDataFromMDB(File dbFile) throws IOException {
        // registering Oracle JDBC driver class
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        }
        catch(ClassNotFoundException cnfex) {
            throw new IOException("Problem in loading MS Access JDBC driver");
        }
        ready = 0;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection connection = null;
                Statement statement = null;
                ResultSet resultSet = null;
                // Step 2: Opening database connection
                try {
                    String dbURL = "jdbc:ucanaccess://" + dbFile;
                    // Step 2.A: Create and
                    // get connection using DriverManager class
                    connection = DriverManager.getConnection(dbURL);
                    // Step 2.B: Creating JDBC Statement
                    statement = connection.createStatement();
                    // Step 2.C: Executing SQL and
                    // retrieve data into ResultSet
                    resultSet = statement.executeQuery(querySql);
                    List<String> pinMeasureParams;
                    LOGGER.debug("Підключено до файлу і виконано запит на вибірку. Кількість результатів = " + resultSet.getFetchSize());
                    // поки є результати
                    while (resultSet.next()) {
                        pinMeasureParams = new ArrayList<>();
                        for(int i = 1; i<= PinMeasureModel.PARAMS_COUNT - 1; i++) {
                            pinMeasureParams.add(resultSet.getString(i));
                        }
                        try {
                            pinsMeasure.add(new PinMeasureModel(pinMeasureParams));
                        } catch (IOException ioe) {
                            LOGGER.error(ioe.getMessage() + " : " + pinMeasureParams);
                        }
                    }
                    ready = 100;
                    LOGGER.debug("ready = 100");
                }
                catch(SQLException e){
                    ready = -1;
                    LOGGER.error(e.getMessage());
                    exception = new IOException(e.getMessage());
                }
                finally {
                    // Step 3: Closing database connection
                    try {
                        if(null != connection) {
                            // cleanup resources, once after processing
                            resultSet.close();
                            statement.close();
                            // and then finally close connection
                            connection.close();
                            LOGGER.debug("З'єднання з базою закрито.");
                        }
                    }
                    catch (SQLException sqlex) {
                        ready = -1;
                        exception = new IOException(sqlex.getMessage());
                    }
                }
                pinsMeasuredToShow = new ArrayList<>(pinsMeasure);
            }
        });
        thread.start();
    }

    /**
     * Считує дані з бази даних.
     * @throws IOException
     */
    private void loadData() throws IOException {
        ready = 0;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // створюєм конект з базою даних
                try (Connection conn = connectToDB()){
                    LOGGER.info("З'єднання з базою даних успішно виконано.");
                    Statement statement = conn.createStatement();
                    // виконуєм sql запит
                    ResultSet rs = statement.executeQuery(querySql);
                    List<String> pinMeasureParams;
                    // поки є результати
                    while (rs.next()) {
                        pinMeasureParams = new ArrayList<>();
                        for(int i = 1; i<= PinMeasureModel.PARAMS_COUNT - 1; i++) {
                            pinMeasureParams.add(rs.getString(i));
                        }
                        try {
                            pinsMeasure.add(new PinMeasureModel(pinMeasureParams));
                        } catch (IOException ioe) {
                            LOGGER.error(ioe.getMessage() + " : " + pinMeasureParams);
                        }
                    }
                    ready = 100;
                } catch (SQLException|IOException e) {
                    ready = -1;
                    LOGGER.error(e.getMessage());
                    exception = new IOException(e.getMessage());
                }
                //pinsMeasure.forEach(System.out::println);
                pinsMeasuredToShow = new ArrayList<>(pinsMeasure);
            }
        });
        thread.start();
    }

    /**
     * Створює з'єднання з базою даних і повертає його.
     * @return з'єднання з базою даних
     * @throws IOException
     */
    private Connection connectToDB() throws IOException{
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Помилка ініціалізації класу: com.microsoft.sqlserver.jdbc.SQLServerDriver");
            throw new IOException("Помилка ініціалізації класу: com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        //"jdbc:sqlserver://localhost;databaseName=AdventureWorks;user=MyUserName;password=*****;"
        String connString = "jdbc:sqlserver://" + server + ";databaseName=" + nameDB;
        Connection connection;
        try {
            connection = DriverManager.getConnection(connString, userName, userPass);
            return connection;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            throw new IOException("Не можливо підключитися до бази даних: " + nameDB);
        }
    }


    @Override
    public int getRowCount() {
        return pinsMeasuredToShow.size();
    }

    @Override
    public int getColumnCount() {
        return PinMeasureModel.PARAMS_COUNT;
    }

    /**
     *
     * @param column номер стовпця табличної моделі
     * @return назву стовпця
     */
    @Override
    public String getColumnName(int column) {
        return tableHeaders.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(pinsMeasuredToShow != null && !pinsMeasuredToShow.isEmpty()) {
            return pinsMeasuredToShow.get(rowIndex).getParam(columnIndex);
        }
        return null;
    }


    private List<models.Observer> observers = new ArrayList<>();

    /**
     * Додає спостерігача.
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
        if(observers == null)
            return;

        for(Observer o : observers) {
            o.update();
        }
    }

    @Override
    public Set<String> getFilterColumnItems(int idColumn) {
        List<PinMeasureModel> pinsMesureFiltered = new ArrayList<>();
        if (filters.size() > 1) {
            for (int i = 0; i < pinsMeasure.size(); i++) {
                boolean addToresult = true;
                for (Map.Entry<Integer, Set<String>> entry : filters.entrySet()) {
                    if (entry.getKey() == idColumn)
                        continue;
                    if (!entry.getValue().contains(pinsMeasure.get(i).getParam(entry.getKey()))) {
                        addToresult = false;
                        break;
                    }
                }
                if (addToresult) {
                    pinsMesureFiltered.add(pinsMeasure.get(i));
                }
            }
        } else {
            pinsMesureFiltered = new ArrayList<>(pinsMeasure);
        }
        Object item;
        String itemS;
        Set<String> result = new TreeSet<>();
        for (int i = 0; i < pinsMesureFiltered.size(); i++) {
            item = pinsMesureFiltered.get(i).getParam(idColumn);
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

    @Override
    public void addFilter(int idColumn, Set<String> newFilter) {
        System.out.println(newFilter);
        // оголошуєм новий список модулів - результуючий після фільтрування
        List<PinMeasureModel> newPinsMeasuringList = new ArrayList<>();
        // якщо по цьому індексу стовпчика вже є фільтр
        if (filters.containsKey(idColumn)) {
            LOGGER.debug("Replace filter in column " + idColumn +" New filter: " + newFilter + " All Items: " + getFilterColumnItems(idColumn));
            if (getFilterColumnItems(idColumn).equals(newFilter) && newFilter.equals(getFilterColumnItems(idColumn))) {
                LOGGER.debug("Remove filter in column " + idColumn);
                String newHeader = tableHeaders.get(idColumn).replace("▼ ", "");
                tableHeaders.set(idColumn, newHeader);
                update();
                filters.remove(idColumn);
            } else {
                // додаємо фільтр
                putNewFilter(idColumn, newFilter);
            }
            // проходимо по всіх модулях з табельки
            for (int i = 0; i < pinsMeasure.size(); i++) {
                boolean checkOK = true;
                // прохід по всіх фільтрах
                for (Map.Entry<Integer, Set<String>> entry : filters.entrySet()) {
                    if (!entry.getValue().contains(pinsMeasure.get(i).getParam(entry.getKey()))) {
                        checkOK = false;
                        break;
                    }
                }
                // якщо і-тий модуль пройшов всі фільтри
                if (checkOK)
                    newPinsMeasuringList.add(pinsMeasure.get(i));
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
                putNewFilter(idColumn, newFilter);
                for (int i = 0; i < getRowCount(); i++) {
                    if (newFilter.contains(getValueAt(i, idColumn))) {
                        newPinsMeasuringList.add(pinsMeasuredToShow.get(i));
                    }
                }
            }
        }
        // присвоюєм результати
        pinsMeasuredToShow = newPinsMeasuringList;
        // апдейтим таблицю
        this.fireTableDataChanged();
    }

    // метод для додавання фільтру
    private void putNewFilter(int idColumn, Set<String> filter) {
        Set<String> newFilter = new LinkedHashSet<>(filter);
        if(newFilter.contains("Пусто")) {
            newFilter.add(null);
            newFilter.remove("Пусто");
        }
        filters.put(idColumn, newFilter);
    }

    @Override
    public int getReady() {
        return ready;
    }

    @Override
    public boolean isReady() {
        if(ready >= 100)
            return true;
        return false;
    }

    // метод для збільшення прогресу завантаження обєкта
    private synchronized void upReady(int up) {
        if (ready >= 0)
            ready += up;
    }


    public IOException getStopException() {
        return exception;
    }

}
