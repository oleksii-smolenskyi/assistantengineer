package views;

import models.Observer;
import models.measure.PinsMeasureTableModel;
import utils.AppProperties;
import utils.Frames;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PinsMeasuredFrame extends JFrame implements Observer {
    private PinsMeasureTableModel tableModel;
    private JTable jTableMeasures;
    private JPanel mainPanel;
    private Map<String, String[]> DBs;

    public PinsMeasuredFrame() throws IOException {
        initFrame();
        // отримуєм рядок всіх баз даних
        String dbs = AppProperties.getProperty("data_bases");
        if(dbs == null || dbs.equals("")) {
            throw new IOException("Відсутні налаштування баз даних.");
        }
        DBs = new LinkedHashMap<>();
        String[] dbArr = dbs.split("~");
        // формуємо налаштування до кожної бази даних
        for(String db : dbArr) {
            String dbServer = AppProperties.getProperty("db." + db + ".server");
            String dbName = AppProperties.getProperty("db." + db + ".name");
            String dbUser = AppProperties.getProperty("db." + db + ".user");
            String dbPass = AppProperties.getProperty("db." + db + ".pass");
            // перевіряєм чи всі параметри задані
            if(dbServer == null || dbName == null || dbUser == null || dbPass == null) {
                continue;
            }
            DBs.put(db, new String[]{dbServer, dbName, dbUser, dbPass});
        }
        // додаєм пункт меню для вибору локального файлу бази даних *.mdb
        final String localDBFileMenuItem = "Файл .mdb ...";
        DBs.put(localDBFileMenuItem, null);
        // викликаєм діалогове вікно вибору бази даних до якої конектимся для вибірки
        String selectedDB = Frames.getSelectedValueFromSelectingFrame(this, DBs.keySet());
        // якщо база даних була вибрана
        if(selectedDB != null) {
            if(selectedDB.equals(localDBFileMenuItem)) {
                File file = getSelectedFile();
                tableModel = new PinsMeasureTableModel(file);
            } else {
                String[] paramsDB = DBs.get(selectedDB);
               // try {
                    tableModel = new PinsMeasureTableModel(paramsDB[0], paramsDB[1], paramsDB[2], paramsDB[3]);
/*                } catch (IOException e) {
                    throw new IOException(e.getMessage());
                }*/
            }
            initComponents();
        } else {
            dispose();
        }
    }

    private File getSelectedFile() {
        JFileChooser fileChooser = new JFileChooser();
        // створюєм фільтри файлів у вікні вибору файла
        FileNameExtensionFilter mdbFilter = new FileNameExtensionFilter("Microsoft Access file *.mdb", "mdb");
        fileChooser.addChoosableFileFilter(mdbFilter);
        fileChooser.setAcceptAllFileFilterUsed(false); // вирубуєм фільтр "All files"
        fileChooser.setDialogTitle("Specify a file");
        // поточна тека вікна вибору, будем зберігати для того, щоб якщо потрібно повторно зробити вибір, щоб відображало зразу теку ту яка була вибрана перед цим
        File selectedFile = null;
        // показуєм діалогове вікно вибору файлу для збереження
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile(); // вибраний файл
            //для случая, когда каталог может иметь ".", но само имя файла не имеет значения (например, /path/to.a/file)
            String extension = null; // розширення вибраного файлу
            int pointIndex = selectedFile.getAbsolutePath().lastIndexOf('.');
            int p = Math.max(selectedFile.getAbsolutePath().lastIndexOf('/'), selectedFile.getAbsolutePath().lastIndexOf('\\'));
            if (pointIndex > p) {
                extension = selectedFile.getAbsolutePath().substring(pointIndex + 1);
            }
			/*
			Якщо користувач не ввів розширення, або ввів розширення, яке не відповідає обраному фільтру,
			тоді додаємо розширення яке потрібне.
			 */
            switch (fileChooser.getFileFilter().getDescription()) {
                case "Microsoft Access file *.mdb":
                    if (extension == null || !extension.toLowerCase().equals("mdb")) {
                        extension = "mdb";
                        selectedFile = new File(selectedFile.getAbsolutePath() + "." + extension);
                    }
                    break;
            }
        }
        return selectedFile;
    }

    // ініціалізуєм фрейм
    private void initFrame() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Виміри голок");
        this.setBounds(0, 0, 1000, 800);
        this.setBounds(Frames.getBoundsForCenteringFrame(this));
        this.setMinimumSize(new Dimension(800,600));
        this.setLayout(new BorderLayout());
    }

    // ініціалізуєм компоненти на фреймі
    private void initComponents() {
        // чекаєм ініціалізації/завантаження моделі(таблиці) модулів
        Frames.showProgressWindow(this, tableModel);
        // перевіряємо чи модель таблиці готова, не готова - закриваєм це вікно
        if(!tableModel.isReady()) {
            Frames.showErrorMessage(null, tableModel.getStopException().getMessage());
            this.dispose();
            return;
        }
        tableModel.addObserver(this);
        // ініціалізуєм таблицю для відображення
        jTableMeasures = new JTable(tableModel);
        jTableMeasures.getTableHeader().setReorderingAllowed(false); // забороняєм совати стовпці
        // додаємо слухача на клік по заголовку талиці, для виклику сортувального вікна
        jTableMeasures.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = jTableMeasures.columnAtPoint(e.getPoint());
                new FilterFrame(PinsMeasuredFrame.this, e.getXOnScreen(), e.getYOnScreen(), col, tableModel);
            }
        });
        JScrollPane jScrollPane = new JScrollPane(jTableMeasures); // додаєм таблицю на скрол
        // mainPanel - панель таблиці результатів
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(jScrollPane); // додаєм скрол на панель
        // додаєм панель на форму
        this.add(mainPanel, BorderLayout.CENTER);
        this.setVisible(true);
        this.revalidate();
    }

    // метод для перезавантаження заголовку таблиці, при фільтруванні таблиці додається/видаляється символ в заголовку
    @Override
    public void update() {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            jTableMeasures.getColumnModel().getColumn(i).setHeaderValue(tableModel.getColumnName(i));
        }
        jTableMeasures.getTableHeader().repaint();
    }
}