package views;

import models.Filterable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class FilterFrame extends JDialog {
    private int idColumn;       // номер стовпця до якого застосовуватиметься фільтрація
    private Filterable table;   // обєкт фільтрування
    private List<JCheckBox> allItems = new ArrayList<>();   // всі значення доступні для фільтрації
    private List<JCheckBox> filteredItems;   // всі значення доступні для фільтрації
    private Box box = Box.createVerticalBox();  // бокс для відображення чек-боксів вибору
    private JCheckBox selectAll;    // чек-бокс для управління всіма чек-боксами "Вибрати все"
    private JButton okButton;   // кнопка для підтвердження застосування фільтру
    private JTextField search;  // поле для пошуку і фільтрації елементів вибору
    private int width = 200;
    private int height = 300;
    private AtomicInteger countSelectedItems; // кількість відмічених елементів
    private int countAllItems; // кількість всіх елементів

    /** Фрейм для забезпечення фільтрування об'єкту класу Filterable
     *
     * @param owner основний фрейм
     * @param x позиція фрейму на екрані
     * @param y позиція фрейму на екрані
     * @param idColumn номер стовпця до якого буде застосовуватися фільтрація
     * @param table об'єкт фільтрації
     */
    public FilterFrame(JFrame owner, int x, int y, int idColumn, Filterable table) {
        super(owner, "", false);
        // без рамки вікна
        this.setUndecorated(true);
        // задаєм розміри фрейму
        // перевіряєм чи вікно фільтрування this не випадає за межі власника
        if(owner.getX() + owner.getWidth() >= x + width)
            this.setBounds(x, y, width, height);
        else // якщо вікно фільтрування випадає за межі власника
            this.setBounds(x - width, y, width, height);
        // стовпцик до якого викликається фільтр
        this.idColumn = idColumn;
        // обєкт фільтрування
        this.table = table;
        // ініціалізуєм компоненти на фреймі
        initComponents();
    }

    private void initComponents() {
        if(table != null) {
            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            this.add(contentPane);
            this.addWindowFocusListener(new WindowFocusListener() {
                @Override
                public void windowGainedFocus(WindowEvent e) {}

                @Override
                public void windowLostFocus(WindowEvent e) {
                    // якщо фрейм втрачає фокус - закриваєм його
                    FilterFrame.this.dispose();
                }
            });
            Set<String> elements = table.getFilterColumnItems(idColumn);
            // ініціалізуєм поле фільтрування списку
            // додаєм поле для фільтрування списку чек боксів like in Excel
            search = new JTextField();
            search.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        Set<String> filter = new HashSet<>();
                        for (JCheckBox checkBox : allItems) {
                            if (checkBox.isSelected())
                                filter.add(checkBox.getText());
                        }
                        table.addFilter(idColumn, filter);
                        FilterFrame.this.dispose();
                    }
                    filterItems(search.getText());
                }
            });
            contentPane.add(search);
            // ініціалізуєм ОК - кнопку застосування фільтра
            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Set<String> filter = new HashSet<>();
                    for (JCheckBox checkBox : filteredItems) {
                        if (checkBox.isSelected())
                            filter.add(checkBox.getText());
                    }
                    table.addFilter(idColumn, filter);
                    FilterFrame.this.dispose();
                }
            });
            // додаєм чек бокс "Виділити все" для керування всім списком чекбоксів
            selectAll = new JCheckBox("(Виділити все)");
            selectAll.setSelected(true);
            // додаєм слухача, для відслідковування зміни стану чек-боксу
            selectAll.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // якщо відмічений/вибраний елемент
                    if(selectAll.isSelected()) {
                        // відмічаємо всі елементи
                        for(JCheckBox checkBox : filteredItems) {
                            checkBox.setSelected(true);
                        }
                    } else {
                        // знімаєм відміченість зі всіх елементів
                        for(JCheckBox checkBox : filteredItems) {
                            checkBox.setSelected(false);
                        }
                    }
                }
            });
            box.add(selectAll);
            // загальна кількість елементів
            countAllItems = elements.size();
            // лічильник вибраних(відмічених) елементів(чек-боксів)
            countSelectedItems = new AtomicInteger(countAllItems);
            // додаємо всі чек-бокси
            for(String element : elements) {
                JCheckBox item = new JCheckBox(element);
                item.setSelected(true);
                item.setFont(new Font(null, Font.PLAIN, 12));
                // додаєм слухача, для відслідковування зміни стану елемента(чек-боксу)
                item.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        // якщо елемент є відмічений
                        if(e.getStateChange() == ItemEvent.SELECTED) {
                            countSelectedItems.incrementAndGet();
                            if(countSelectedItems.intValue() == filteredItems.size()) {
                                selectAll.setSelected(true);
                            }
                        }
                        // якщо елемент не відмічений
                        if(e.getStateChange() == ItemEvent.DESELECTED) {
                            // зменшуєм лічильник вибраних елементів
                            countSelectedItems.decrementAndGet();
                            if(selectAll.isSelected() == true) {
                                selectAll.setSelected(false);
                            }
                        }
                    }
                });
                // додаєм елемент
                box.add(item);
                allItems.add(item);
            }
            // відфільтровані елементи рівні всім елементам
            filteredItems = new ArrayList<>(allItems);
            // додаєм панель з чек-боксами на скрол
            JScrollPane scrollPane = new JScrollPane(box);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            contentPane.add(scrollPane);
            contentPane.add(okButton);
            // перевіряєм кількість елементів, якщо їх мало, підганяєм розмір фрейму
            if(elements.size() <= 10) {
                this.pack();
                this.setSize(new Dimension(this.width, this.getBounds().height));
            }
            this.revalidate();
            this.setVisible(true);
        } else {
            dispose();
        }
    }

    // метод фільтрації чек-боксів у вікні
    private void filterItems(String s) {
        // якщо маска фільтру не порожній
        if(s != null && !s.equals("")) {
            filteredItems = new ArrayList<>();
            countSelectedItems.set(0);
            box.removeAll();
            box.add(selectAll);
            Pattern pattern = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
            for(JCheckBox checkBox : allItems) {
                if(pattern.matcher(checkBox.getText()).find()) {
                    checkBox.setSelected(true);
                    box.add(checkBox);
                    filteredItems.add(checkBox);
                    countSelectedItems.incrementAndGet();
                }
            }
            selectAll.setSelected(true);
        } else { // якщо маска фільтру порожня, відображаєм всі можливі варіанти вибору
            box.removeAll();
            box.add(selectAll);
            filteredItems = new ArrayList<>(allItems);
            countSelectedItems.set(countAllItems);
            for(JCheckBox checkBox : allItems) {
                checkBox.setSelected(true);
                box.add(checkBox);
            }
            selectAll.setSelected(true);
        }
        box.repaint();
        box.revalidate();
    }


}
