package utils;

import models.Progressable;
import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Утилітний клас для методів роботи з фреймами та діалоговими вікнами.
 * @author Oleksii Smolenskyi
 */
public class Frames {

    private Frames() {

    }
    /**
     * Створює діалогове вікно відображення прогресу ініціалізації об'єкта, який реалізовує інтерфейс Progressable.
     *
     * @param ownerFrame фрейм власник діалогового вікна
     * @param obj об'єкт для якого відображаєм прогрес, повинен реалізовувати інтерфейс Progressable
     */
    public static void showProgressWindow(JFrame ownerFrame, Progressable obj) {
        final JDialog dlg = new JDialog(ownerFrame, "Loading data...", true);
        JProgressBar dpb = new JProgressBar(0, 100);
        dlg.add(BorderLayout.CENTER, dpb);
        JLabel statusLabel = new JLabel("Progress...");
        dlg.add(BorderLayout.NORTH, statusLabel);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.setSize(300, 75);
        dlg.setLocationRelativeTo(ownerFrame);
        Thread t = new Thread(new Runnable() {
            public void run() {
                dlg.setVisible(true);
            }
        });
        t.start();
        // поки обєкт не готовий
        while(!obj.isReady() || dlg.isVisible()) {
            dpb.setValue(obj.getReady());
            if(dpb.getValue() >= 100){
                dlg.setVisible(false);
            } else {
                if(obj.getReady() < 0) {
                    break;
                }
            }
            try {
                Thread.sleep(100);
                statusLabel.setText("Progress: " + obj.getStatusMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Повертає обєкт Rectangle(розташування та розміри) для вікна, щоб вікно було в центрі екрану
     * @param window
     * @return
     */
    public static Rectangle getBoundsForCenteringFrame (Window window) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int width, height;
        width = window.getWidth();
        height = window.getHeight();
        return new Rectangle(dimension.width / 2 - width / 2, dimension.height / 2 - height / 2, width, height);
    }

    /**
     * Створює діалогове вікно для повідомлень-помилок, які відбулися в процесі роботи програми
     * @param owner власник діалогового вікна
     * @param error стрічка для відображення повідомлення
     */
    public static void showErrorMessage(Window owner, String error) {
        JOptionPane.showMessageDialog(owner, error, "Error", JOptionPane.ERROR_MESSAGE);

    }

    /**
     * Створює діалогове вікно в якому створюється список для вибору і подальшого "відкриття" файлу
     * @param ownerFrame фрейм власник
     * @param files колекція з файлів, з яких потрібно зробити вибір
     */
    public static void showSelectingAndExecutingFileFrame(JFrame ownerFrame, Collection<File> files) {
        // якщо для вибору тільки 1 файл, відкриваєм його зразу без показу вікна вибору
        if(files.size() == 1) {
            File[] arr = new File[1];
            try {
                Desktop.getDesktop().open(files.toArray(arr)[0]);
            } catch (IOException ioe) {
                Frames.showErrorMessage(ownerFrame, ioe.getMessage());
            }
            return;
        }
        // створюємо діалогове вікно для вибору файла з множини файлів
        final String dialogTitle = "Виберіть файл";
        final int width = 250;  // ширина діалогового вікна
        final int height = 100; // висота діалогового вікна
        JDialog dlg = new JDialog(ownerFrame, dialogTitle, true);
        dlg.setBounds(ownerFrame.getX() + ownerFrame.getWidth() / 2 - width / 2, ownerFrame.getY() + ownerFrame.getHeight() / 2 - height / 2, width, height);
        // створюєм мапу з ключом - назва файлу і значенням - сам файл
        Map<String, File> fileName = new LinkedHashMap<>();
        for(File file : files) {
            fileName.put(file.getName(), file);
        }
        JList list = new JList(fileName.keySet().toArray());
        dlg.add(BorderLayout.CENTER, list);
        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && !list.isSelectionEmpty()) {
                    String selectedValue = (String) list.getSelectedValue();
                    try {
                        Desktop.getDesktop().open(fileName.get(selectedValue));
                    } catch (IOException ioe) {
                        Frames.showErrorMessage(ownerFrame, ioe.getMessage());
                    }
                    dlg.dispose();
                }
            }
        });
        dlg.setVisible(true);
    }

    /**
     * Створює діалогове вікно в якому створюється список для вибору і подальшого "відкриття" файлу
     * @param ownerFrame фрейм власник
     * @param values колекція стрічок-значень з яких потрібно зробити вибір
     * @return вибране значення
     */
    public static String getSelectedValueFromSelectingFrame(JFrame ownerFrame, Collection<String> values) {
        StringBuilder resultValue = new StringBuilder();
        JDialog dlg = new JDialog(ownerFrame, "Зробіть вибір:", true);
        dlg.setResizable(false);
        dlg.setBounds(ownerFrame.getX() + ownerFrame.getWidth() / 2 - 250 / 2, ownerFrame.getY() + ownerFrame.getHeight() / 2 - 120 / 2, 250, 120);
        JList list = new JList(values.toArray());
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dlg.add(BorderLayout.CENTER, scrollPane);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && !list.isSelectionEmpty()) {
                    resultValue.append ((String) list.getSelectedValue());
                    dlg.dispose();
                }
            }
        });
        dlg.setVisible(true);
        while(dlg.isVisible()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(resultValue.length() < 1)
            return null;
        else
            return resultValue.toString();
    }
}
