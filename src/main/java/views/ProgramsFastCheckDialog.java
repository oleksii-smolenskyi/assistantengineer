package views;

import services.FastCheckerOfPrograms;
import services.ReaderTestPrograms;
import services.ReaderTestProgramsFromFolder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;

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
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
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
                        ProgramsFastCheckDialog.this.initFastCheck(null, emptyConnectorsCheckBox.isSelected(),
                                notContactedWireCheckBox.isSelected(), softAdaptedCheckBox.isSelected(), spliceAdaptedCheckBox.isSelected());
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
        this.setVisible(true);
    }
}
