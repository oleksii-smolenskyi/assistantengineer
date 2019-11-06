package views;

import services.FastCheckerOfPrograms;
import services.ReaderTestPrograms;
import services.ReaderTestProgramsFromFolder;
import utils.AppProperties;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Діалогогве вікно "швидкої" перевірки тестувальних програм на можливі грубі помилки,
// які могли бути допущені при створенні програм тестування.
public class ProgramsFastCheckDialog extends JDialog {

    // діалогове вікно ініціалізації параметрів перевірки тестувальних програм
    class ProgramsFastCheckInitDialog extends JDialog {

        public ProgramsFastCheckInitDialog() {
            this.setModal(true);
            initWindow();
            initComponents();
        }

        // ініціалізує діалогове вікно
        private void initWindow() {
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.setResizable(false);
            this.setTitle("Виберіть параметри перевірки тест. програм");
        }

        // ініціалізує компоненти діалогового вікна
        private void initComponents() {
            JPanel contentPanel = new JPanel();
            JCheckBox emptyConnectorsCheckBox = new JCheckBox("Перевірка на конектори без з'єднань");
            JCheckBox notContactedWireCheckBox = new JCheckBox("Перевірка на не законтактовані проводи");
            JCheckBox softAdaptedCheckBox = new JCheckBox("Перевірка на не чітко заданий Adapted");
            JCheckBox spliceAdaptedCheckBox = new JCheckBox("Перевірка на звари, що тестуються фізично(Adapted select)");
            JPanel selectPathContentPanel = new JPanel();   // панель для вибору джерела тестувальних програм
            List<String> sourceItems = new ArrayList<>();
            sourceItems.add(0, "");
            sourceItems.addAll(getDatabasesSourceItems());
            sourceItems.add("Path: Вибрати шлях до теки з програмами");
            JComboBox programsSource = new JComboBox();
            DefaultComboBoxModel model = new DefaultComboBoxModel(sourceItems.toArray());
            programsSource.setModel(model);
            programsSource.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(((String)programsSource.getSelectedItem()).startsWith("Path:")) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setCurrentDirectory(new File("."));
                        fileChooser.setMultiSelectionEnabled(false);
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        if(fileChooser.showDialog(ProgramsFastCheckDialog.this, "Select") == JFileChooser.APPROVE_OPTION) {
                            programsSource.removeAllItems();
                            programsSource.addItem("Path: " + fileChooser.getSelectedFile());
                        } else {
                            programsSource.setSelectedIndex(0);
                        }
                    }
                }
            });
            programsSource.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    DefaultComboBoxModel model = new DefaultComboBoxModel(sourceItems.toArray());
                    programsSource.setModel(model);
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {

                }

            });
            programsSource.setEditable(false);
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.add(programsSource);
            contentPanel.add(emptyConnectorsCheckBox);
            contentPanel.add(notContactedWireCheckBox);
            contentPanel.add(softAdaptedCheckBox);
            contentPanel.add(spliceAdaptedCheckBox);
            JPanel confirmButtonsPanel = new JPanel();
            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // TODO валідацію обраних параметрів користувачем
                    try {
                        if(((String)programsSource.getSelectedItem()).startsWith("Path: ")) {
                            ProgramsFastCheckDialog.this.initFastCheck(Paths.get(((String)programsSource.getSelectedItem()).substring(6).trim()), emptyConnectorsCheckBox.isSelected(),
                                    notContactedWireCheckBox.isSelected(), softAdaptedCheckBox.isSelected(), spliceAdaptedCheckBox.isSelected());
                        }
                    } catch (IOException ex) {
                        // TODO save exception to stopException
                    }
                    ProgramsFastCheckInitDialog.this.dispose();
                }
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ProgramsFastCheckInitDialog.this.dispose();
                    ProgramsFastCheckDialog.this.dispose();
                }
            });
            confirmButtonsPanel.add(okButton);
            confirmButtonsPanel.add(cancelButton);
            contentPanel.add(confirmButtonsPanel);
            this.getContentPane().add(contentPanel);
            this.pack();
            this.setVisible(true);
        }
    }

    // повертає список баз даних, прописаних в налаштуваннях програми
    private List<String> getDatabasesSourceItems() {
        String dbs = AppProperties.getProperty("data_bases");
        List<String> databasesSourceItems = null;
        if(dbs != null && !dbs.equals("")) {
            Map<String, String[]> DBs = new LinkedHashMap<>();
            String[] dbArr = dbs.split("~");
            // формуємо налаштування до кожної бази даних
            for (String db : dbArr) {
                String dbServer = AppProperties.getProperty("db." + db + ".server");
                String dbName = AppProperties.getProperty("db." + db + ".name");
                String dbUser = AppProperties.getProperty("db." + db + ".user");
                String dbPass = AppProperties.getProperty("db." + db + ".pass");
                // перевіряєм чи всі параметри задані
                if (dbServer == null || dbName == null || dbUser == null || dbPass == null) {
                    continue;
                }
                DBs.put(db, new String[]{dbServer, dbName, dbUser, dbPass});
            }
            databasesSourceItems = new ArrayList<>();
            for(Map.Entry<String, String[]> entry : DBs.entrySet()) {
                databasesSourceItems.add("DB: " + entry.getKey());
            }
        }
        return databasesSourceItems;
    }

    public ProgramsFastCheckDialog() {
        // створюємо вікно для задання параметрів перевірки тестувальних програм користувачем
        ProgramsFastCheckInitDialog initDialog = new ProgramsFastCheckInitDialog();
        init();
    }

    // ініціалізує вікно результатів перевірки програм тестування
    private void init() {
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setTitle("Результат перевірки тест. програм");
    }

    // перевіряч тестувальних програм
    FastCheckerOfPrograms fastCheckerOfPrograms;
    // зчитувач тест програм
    ReaderTestPrograms readerTestPrograms;

    // ініціалізує читача тестувальних програм
    private void initFastCheck(Path path, boolean checkEmptyConnectors, boolean checkNotConnectedWire, boolean checkSoftAdapted, boolean checkSpliceAdapted) throws IOException {
        // TODO ініціалізувати рідера тест програм
        readerTestPrograms = new ReaderTestProgramsFromFolder(path);
        fastCheckerOfPrograms = new FastCheckerOfPrograms(readerTestPrograms, checkEmptyConnectors, checkNotConnectedWire, checkSoftAdapted, checkSpliceAdapted);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(readerTestPrograms.getPrograms());
        this.dispose();
    }
}
