package views;

import models.User;
import utils.Frames;
import utils.UsersManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class EditUsersFrame extends JDialog {

    public EditUsersFrame(JFrame owner) {
        super(owner, "Користувачі", true);
        this.setBounds(0, 0, 400, 400);
        this.setBounds(Frames.getBoundsForCenteringFrame(this));
        this.setResizable(false);
        this.setLayout(new FlowLayout());
        initComponents();
        this.setVisible(true);
    }

    // ініціалізація компонентів фрейму
    private void initComponents() {
        initGroupsComponents();
        initUsersComponents();
        initConfirmButtons();
    }

    // ініціалізує кнопки підтвердження зберігання внесених змін в дані користувачів або в групи
    private void initConfirmButtons() {
        JPanel confirmPanel = new JPanel();
        JButton okButton = new JButton("OK");
        // при натисканні на ОК зберігаєм користувачів
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UsersManager.saveUsers();
                } catch (IOException ex) {
                    Frames.showErrorMessage(EditUsersFrame.this, ex.getMessage());
                    return;
                }
                EditUsersFrame.this.dispose();
            }
        });
        // при натисканні на CANCEL завантажуєм користувачів з файлів (скидаєм внесені зміни)
        JButton cancelButton = new JButton("CANCEL");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UsersManager.loadUsers();
                } catch (IOException ex) {
                    Frames.showErrorMessage(EditUsersFrame.this, ex.getMessage());
                } finally {
                    EditUsersFrame.this.dispose();
                }
            }
        });
        confirmPanel.add(okButton);
        confirmPanel.add(cancelButton);
        this.getContentPane().add(confirmPanel);
    }

    private JList listOfUsers;

    // діалогове вікно для створення/редагування користувача
    class UserEditor extends JDialog {
        User user;

        public UserEditor(JDialog owner) {
            super(owner, "Створити користувача", true);
            initComponentsGroupEditor();
        }

        boolean editableMode = false;

        public UserEditor(JDialog owner, User user) {
            super(owner, "Редагувати користувача", true);
            editableMode = true;
            this.user = user;
            initComponentsGroupEditor();
        }

        private JTextField nameUser = new JTextField(20);
        private JPasswordField passUser = new JPasswordField(20);
        private JPasswordField passConfirmUser = new JPasswordField(20);
        private JComboBox accessGroupUser;

        // ініціалізує компоненти редагування групи користувачів
        private void initComponentsGroupEditor() {
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridLayout(5,2));
            //
            JLabel nameUserLabel = new JLabel("Табельний номер:");
            nameUser.setPreferredSize(new Dimension(200, 20));
            contentPanel.add(nameUserLabel);
            contentPanel.add(nameUser);
            //
            JLabel passUserLabel = new JLabel("Пароль:");
            passUserLabel.setPreferredSize(new Dimension(200, 20));
            contentPanel.add(passUserLabel);
            passUser.setEchoChar('*');
            contentPanel.add(passUser);
            //
            JLabel passConfirmUserLabel = new JLabel("Підтвердження паролю:");
            passConfirmUserLabel.setPreferredSize(new Dimension(200, 20));
            contentPanel.add(passConfirmUserLabel);
            passConfirmUser.setEchoChar('*');
            contentPanel.add(passConfirmUser);
            //
            JLabel accessGroupLabel = new JLabel("Група користувачів:");
            accessGroupLabel.setPreferredSize(new Dimension(200, 20));
            accessGroupUser = new JComboBox(UsersManager.getAccessGroups().keySet().toArray());
            contentPanel.add(accessGroupLabel);
            contentPanel.add(accessGroupUser);
            //
            contentPanel.add(new JLabel());
            JButton okButton = new JButton("OK");
            contentPanel.add(okButton);
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(nameUser.getText().equals("") && passUser.equals("") && accessGroupUser.getSelectedIndex() <= 0) {
                        Frames.showErrorMessage(UserEditor.this, "Не заповнені поля!");
                        return;
                    }
                    String password = new String(passUser.getPassword());
                    String passwordConfirm = new String(passConfirmUser.getPassword());
                    // перевірка на пробіли і пусті поля
                    if(nameUser.getText().startsWith(" ") || nameUser.getText().endsWith(" ") ||  nameUser.getText().equals("")) {
                        Frames.showErrorMessage(UserEditor.this, "Не правильна назва групи!");
                        return;
                    } else // якщо не редагування, а новий користувач, перевірка на існування вже такого користувача
                        if(!editableMode && UsersManager.getAccessGroups().containsKey(nameUser.getText())) {
                        Frames.showErrorMessage(UserEditor.this, "Такий користувач вже існує!");
                        return;
                    } else // перевірка чи введений пароль ідентичний підтвердженню
                        if(!password.equals(passwordConfirm)) {
                        Frames.showErrorMessage(UserEditor.this, "Не вірне підтвердження паролю, введіть значення ще раз!");
                        passUser.setText("");
                        passConfirmUser.setText("");
                        return;
                    }
                    User newUser;
                    // якщо режим редагування існуючого користувача
                    if(editableMode) {
                        /*System.out.println("password field: " + password);
                        System.out.println("user pass: " + user.getPassSHA256());*/
                        if (!password.equals(user.getPassSHA256())) {
                            newUser = new User(nameUser.getText(), UsersManager.getCryptHashSHA256(password), (String) accessGroupUser.getSelectedItem());
                            //System.out.println("new pass: " + UsersManager.getCryptHashSHA256(password));
                        } else {
                            newUser = new User(nameUser.getText(), password, (String) accessGroupUser.getSelectedItem());
                            //System.out.println("old pass, hash: " + password);
                        }
                    } else
                        newUser = new User(nameUser.getText(), UsersManager.getCryptHashSHA256(password), (String) accessGroupUser.getSelectedItem());
                    newUser.setUserRights((String) accessGroupUser.getSelectedItem(), UsersManager.getAccessGroups().get(accessGroupUser.getSelectedItem()));
                    UsersManager.addUser(newUser);
                    DefaultListModel model = (DefaultListModel) listOfUsers.getModel();
                    if(!editableMode)
                        model.addElement(nameUser.getText());
                    UserEditor.this.dispose();
                }
            });
            if(editableMode)
                setValues();
            this.getContentPane().add(contentPanel);
            this.pack();
            this.setResizable(false);
            this.setBounds(Frames.getBoundsForCenteringFrame(this));
            this.setVisible(true);
        }

        // встановлює значення в елементи, якщо була задана група прав
        private void setValues() {
            if(user == null)
                return;
            nameUser.setText(user.getTabelNr());
            nameUser.setEditable(false);
            passUser.setText(user.getPassSHA256());
            passConfirmUser.setText(user.getPassSHA256());
            accessGroupUser.setSelectedItem(user.getAccessGroupName());
        }
    } // end of class UserEditor


    // ініціалізуєм елементи вікна для роботи з користувачами
    private void initUsersComponents() {
        // Створюємо панель для компонентів управління користувачами
        JPanel panelForUsers = new JPanel();
        // список для користувачів
        //JList listOfUsers;
        System.out.println(UsersManager.getUsers());
        DefaultListModel model = new DefaultListModel();
        for(String group : UsersManager.getUsers().keySet())
            model.addElement(group);
        listOfUsers = new JList(model);
        //listOfUsers = new JList(UsersManager.getUsers().values().toArray());
        listOfUsers.setFixedCellWidth(300);
        JScrollPane scrollUsers = new JScrollPane(listOfUsers);

        this.getContentPane().add(panelForUsers);

        JPanel panelButtonsEditUser = new JPanel();
        panelButtonsEditUser.setLayout(new GridLayout(3, 1));

        JButton addUser = new JButton("Add");
        addUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UserEditor(EditUsersFrame.this);
            }
        });

        JButton editUser = new JButton("Edit");
        editUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(listOfUsers.getSelectedIndex() >= 0) {
                    new UserEditor(EditUsersFrame.this, UsersManager.getUsers().get(listOfUsers.getSelectedValue()));
                }
            }
        });

        JButton removeUser = new JButton("Remove");
        removeUser.addActionListener(e -> {
            String selectedItem;
            int selectedItemIndex;
            if(!listOfUsers.isSelectionEmpty() && listOfUsers.getSelectedValue() instanceof String) {
                selectedItem = (String)listOfUsers.getSelectedValue();
                selectedItemIndex = listOfUsers.getSelectedIndex();
                if(selectedItem.equals("System")) {
                    Frames.showErrorMessage(this, "Не можливо видалити цього користувача.");
                } else {
                    UsersManager.removeUser(selectedItem);
                    model.removeElementAt(selectedItemIndex);
                }
            }
        });

        panelButtonsEditUser.add(addUser);
        panelButtonsEditUser.add(editUser);
        panelButtonsEditUser.add(removeUser);
        panelForUsers.add(scrollUsers);
        panelForUsers.add(panelButtonsEditUser);
    }

    // діалогове вікно для створення/редагування групи прав доступу користувачів
    class GroupEditor extends JDialog {
        Map<String, String> group;

        public GroupEditor(JDialog owner) {
            super(owner, "Створити групу", true);
            UsersManager.getAccessGroups();
            initComponentsGroupEditor();
        }

        boolean editableMode = false;

        public GroupEditor(JDialog owner, Map<String, String> group) {
            super(owner, "Редагувати групу", true);
            editableMode = true;
            this.group = group;
            initComponentsGroupEditor();
        }

        private JTextField nameGroupField = new JTextField(20);
        private JCheckBox usersEditableCheck = new JCheckBox();
        private JCheckBox preferencesEditableCheck = new JCheckBox();
        private JCheckBox permitModulesCheck = new JCheckBox();
        private JCheckBox pinMeasuringCheck = new JCheckBox();

        // ініціалізує компоненти редагування групи користувачів
        private void initComponentsGroupEditor() {
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridLayout(6,2));
            //
            JLabel nameGroupLabel = new JLabel("Назва групи:");
            nameGroupLabel.setPreferredSize(new Dimension(200, 20));
            contentPanel.add(nameGroupLabel);
            contentPanel.add(nameGroupField);
            //
            JLabel usersEditableLabel = new JLabel("Доступ до користувачів:");
            usersEditableLabel.setPreferredSize(new Dimension(200, 20));
            usersEditableCheck.setSelected(false);
            contentPanel.add(usersEditableLabel);
            contentPanel.add(usersEditableCheck);
            //
            JLabel preferencesEditableLabel = new JLabel("Доступ до налаштувань:");
            preferencesEditableLabel.setPreferredSize(new Dimension(200, 20));
            preferencesEditableCheck.setSelected(false);
            contentPanel.add(preferencesEditableLabel);
            contentPanel.add(preferencesEditableCheck);
            //
            JLabel permitModulesLabel = new JLabel("Доступ до допусків модулів:");
            permitModulesLabel.setPreferredSize(new Dimension(200, 20));
            permitModulesCheck.setSelected(false);
            contentPanel.add(permitModulesLabel);
            contentPanel.add(permitModulesCheck);
            //
            JLabel pinMeasuringLabel = new JLabel("Доступ до вимірів голок:");
            pinMeasuringLabel.setPreferredSize(new Dimension(200, 20));
            pinMeasuringCheck.setSelected(false);
            contentPanel.add(pinMeasuringLabel);
            contentPanel.add(pinMeasuringCheck);
            //
            contentPanel.add(new JLabel());
            JButton okButton = new JButton("OK");
            contentPanel.add(okButton);
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(UsersManager.getAccessGroups());
                    if(nameGroupField.getText().startsWith(" ") || nameGroupField.getText().endsWith(" ") ||  nameGroupField.getText().equals("")) {
                        Frames.showErrorMessage(GroupEditor.this, "Не правильна назва групи!");
                        return;
                    } else if(!editableMode && UsersManager.getAccessGroups().containsKey(nameGroupField.getText())) {
                        Frames.showErrorMessage(GroupEditor.this, "Група з такою назвою користувачів вже існує!");
                        return;
                    }
                    Map<String, String> accessGroup = new HashMap<>();
                    accessGroup.put("n", nameGroupField.getText());
                    accessGroup.put("u", usersEditableCheck.isSelected() ? UsersManager.FULL_ACCESS : UsersManager.NO_ACCESS);
                    accessGroup.put("pr", preferencesEditableCheck.isSelected() ? UsersManager.FULL_ACCESS : UsersManager.NO_ACCESS);
                    accessGroup.put("pm", permitModulesCheck.isSelected() ? UsersManager.FULL_ACCESS : UsersManager.NO_ACCESS);
                    accessGroup.put("pmd", pinMeasuringCheck.isSelected() ? UsersManager.FULL_ACCESS : UsersManager.NO_ACCESS);
                    UsersManager.addAccessGroup(accessGroup);
                    DefaultListModel model = (DefaultListModel) listOfGroups.getModel();
                    if(!editableMode)
                        model.addElement(nameGroupField.getText());
                    GroupEditor.this.dispose();
                }
            });
            //
            if(editableMode)
                setValues();
            this.getContentPane().add(contentPanel);
            this.pack();
            this.setResizable(false);
            this.setBounds(Frames.getBoundsForCenteringFrame(this));
            this.setVisible(true);
        }

        // встановлює значення в елементи, якщо була задана група прав
        private void setValues() {
            if(group == null || group.size() <= 0)
                return;
            nameGroupField.setText(group.getOrDefault("n", ""));
            nameGroupField.setEditable(false);
            if(group.getOrDefault("u", "").equals(UsersManager.FULL_ACCESS)) {
                usersEditableCheck.setSelected(true);
            }
            if(group.getOrDefault("pr", "").equals(UsersManager.FULL_ACCESS)) {
                preferencesEditableCheck.setSelected(true);
            }
            if(group.getOrDefault("pmd", "").equals(UsersManager.FULL_ACCESS)) {
                pinMeasuringCheck.setSelected(true);
            }
            if(group.getOrDefault("pm", "").equals(UsersManager.FULL_ACCESS)) {
                permitModulesCheck.setSelected(true);
            }
        }
    } // end of class GroupEditor

    private JList listOfGroups;

    // ініціалізуєм елементи вікна для роботи з групами користувачів
    private void initGroupsComponents() {
        // Створюємо панель для компонентів управління групами користувачів
        JPanel panelForGroups = new JPanel();
        // список груп користувачів
        DefaultListModel model = new DefaultListModel();
        for(String group : UsersManager.getAccessGroups().keySet())
            model.addElement(group);
        listOfGroups = new JList(model);
        listOfGroups.setFixedCellWidth(300);
        listOfGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollGroups = new JScrollPane(listOfGroups);
        this.getContentPane().add(panelForGroups);
        // створюємо панель для кнопок керування групами користувачів
        JPanel panelButtonsEditGroups = new JPanel();
        panelButtonsEditGroups.setLayout(new GridLayout(3, 1));
        // додаєм кнопку для додавання нового користувача
        JButton addGroup = new JButton("Add");
        addGroup.addActionListener(e -> {
            new GroupEditor(this);
        });
        // додаєм кнопку для редагування обраного користувача
        JButton editGroup = new JButton("Edit");
        editGroup.addActionListener(e -> {
            if(listOfGroups.getSelectedValue() != null)
                new GroupEditor(this, UsersManager.getAccessGroups().get((String) listOfGroups.getSelectedValue()));
        });
        // додаєм кнопку для видалення обраного користувача
        JButton removeGroup = new JButton("Remove");
        removeGroup.addActionListener(e -> {
            String selectedItem;
            int selectedItemIndex;
            if(!listOfGroups.isSelectionEmpty() && listOfGroups.getSelectedValue() instanceof String) {
                selectedItem = (String)listOfGroups.getSelectedValue();
                selectedItemIndex = listOfGroups.getSelectedIndex();
                if(selectedItem.equals("System")) {
                    Frames.showErrorMessage(this, "Не можливо стерти цю групу.");
                } else {
                    UsersManager.removeAccessGroup(selectedItem);
                    model.removeElementAt(selectedItemIndex);
                }
            }
        });
        // додаєм кнопки на панель
        panelButtonsEditGroups.add(addGroup);
        panelButtonsEditGroups.add(editGroup);
        panelButtonsEditGroups.add(removeGroup);
        panelForGroups.add(scrollGroups);
        panelForGroups.add(panelButtonsEditGroups);
    }
}
