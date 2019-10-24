package utils;

import models.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.util.*;

/**
 * Клас для роботи з даними користувачів(створення, редагування, верифікація користувачів, тощо).
 *  @author Oleksii Smolenskyi
 */
public class UsersManager {
    public static final String FULL_ACCESS = "full access";
    public static final String NO_ACCESS = "no access";
    //public static final String DIESEL_GATE_ACCESS = "diesel access";
    private static final String USERS_FILE = "users.json";
    private static Map<String, Map<String, String>> accessGroups = new HashMap<>();
    private static Map<String, User> users = new HashMap<>();
    private static Cryptographer cryptographer = new Cryptographer("Bar12345Bar12345");

    private static User systemUser;
    private static Map<String, String> systemGroup;

    static {
        try {
            // створюєм системного користувача і системну групу, завжди повинні бути, не залежно від файлу з користувачами
            systemGroup = new HashMap<>();
            systemGroup.put("n", "System");
            systemGroup.put("u", FULL_ACCESS);
            systemGroup.put("pr", FULL_ACCESS);
            systemGroup.put("pm", FULL_ACCESS);
            systemGroup.put("pmd", FULL_ACCESS);
            accessGroups.put(systemGroup.get("n"), systemGroup);
            systemUser = new User("System", getCryptHashSHA256("ffff"), "System");
            users.put("System", systemUser);
            systemUser.setUserRights("System", systemGroup);
            // завантажуєм користувачів і їх групи
            loadUsers();
        } catch (IOException e) {
            Frames.showErrorMessage(null, e.getMessage());
        }
    }

    // не потрібно робити обєкт класу, клас утилітний, всі методи статичні
    private UsersManager() {
    }

    /**
     * Завантажує користувачів з файлу.
     *
     * @throws IOException
     */
    public synchronized static void loadUsers() throws IOException {
        // очищуєм мапи груп і користувачів
        accessGroups = new HashMap<>();
        addAccessGroup(systemGroup);
        users = new HashMap<>();
        addUser(systemUser);
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        // відкриваєм файл з користувачами
        try (FileReader reader = new FileReader(UtilityMethods.getMainPath() + "\\" + USERS_FILE)) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray objectList = (JSONArray) obj;
            for (Object object : objectList) {
                JSONObject jsonObject = (JSONObject) object;
                // якщо JSON обєкт є користувачем
                if (jsonObject.get("us") != null) {
                    User readedUser = parseJsonUser((JSONObject) jsonObject.get("us"));
                    if (readedUser != null) {
                        users.put(readedUser.getTabelNr(), readedUser);
                    }
                }
                // якщо JSON обєкт є групою користувачів
                if (jsonObject.get("gr") != null) {
                    // парсим json об'єкт
                    Map<String, String> readedGroup = parseJsonGroup((JSONObject) jsonObject.get("gr"));
                    if (readedGroup != null) {
                        accessGroups.put(readedGroup.get("n"), readedGroup);
                    }
                }
            }
            for (User user : users.values()) {
                user.setUserRights(user.getAccessGroupName(), accessGroups.get(user.getAccessGroupName()));
            }
            //System.out.println("users: " + users);
        } catch (FileNotFoundException e) {
            throw new IOException("File with users doesn't exist!");
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }

    // Парсить JSON і повертає карту з параметрами рівнів доступу групи користувачів до функціоналу програми
    private static Map<String, String> parseJsonGroup(JSONObject gr) {
        Map<String, String> groupMap;
        //Get employee object within list
        JSONObject group = (JSONObject) gr.get("gr");
        //Get employee first name
        String groupName = cryptographer.decrypt((String) group.get("n"));
        String usersEditAccess = cryptographer.decrypt((String) group.get("u"));
        String propertiesEditAccess = cryptographer.decrypt((String) group.get("pr"));
        String permitModulesAccess = cryptographer.decrypt((String) group.get("pm"));
        String pinMeasuringAccess = cryptographer.decrypt((String) group.get("pmd"));
        String hashGroup = (String) gr.get("h");
        // перевіряєм хеш, чи ніхто нічого не міняв вручну в файлі з користувачами
        StringBuilder data = new StringBuilder();
        data.append(groupName).append(usersEditAccess).append(propertiesEditAccess).append(permitModulesAccess).append(pinMeasuringAccess);
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data.toString());
        if (md5.equals(hashGroup)) {
            groupMap = new HashMap<>();
            groupMap.put("n", groupName);
            groupMap.put("u", usersEditAccess);
            groupMap.put("pr", propertiesEditAccess);
            groupMap.put("pm", permitModulesAccess);
            groupMap.put("pmd", pinMeasuringAccess);
            return groupMap;
        }
        return null;
    }

    /**
     * Зберігає список користувачів в файл.
     * @throws IOException
     */
    public synchronized static void saveUsers() throws IOException {
        //Add employees to list
        JSONArray userList = new JSONArray();
        // зберігаєм налаштування груп користувачів
        for (String name : accessGroups.keySet()) {
            // групу System в файл не зберігаєм
            if (name.equals("System"))
                continue;
            Map<String, String> group = accessGroups.get(name);
            String groupName = group.get("n");
            String usersEditAccess = group.get("u");
            String propertiesEditAccess = group.get("pr");
            String permitModulesAccess = group.get("pm");
            String pinMeasuringAccess = group.get("pmd");
            if (groupName == null || usersEditAccess == null || propertiesEditAccess == null || permitModulesAccess == null || pinMeasuringAccess == null)
                throw new IOException("Неправильно внесена група користувачів: " + group);
            // рахуєм хеш користувача
            StringBuilder data = new StringBuilder();
            data.append(groupName).append(usersEditAccess).append(propertiesEditAccess).append(permitModulesAccess).append(pinMeasuringAccess);
            //System.out.println("data: " + data.toString());
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data.toString());
            //System.out.println("md5: " + md5);
            JSONObject accessGroupDetails = new JSONObject();
            accessGroupDetails.put("n", cryptographer.encrypt(groupName));
            accessGroupDetails.put("u", cryptographer.encrypt(usersEditAccess));
            accessGroupDetails.put("pr", cryptographer.encrypt(propertiesEditAccess));
            accessGroupDetails.put("pm", cryptographer.encrypt(permitModulesAccess));
            accessGroupDetails.put("pmd", cryptographer.encrypt(pinMeasuringAccess));
            JSONObject accessGroupJSON = new JSONObject();
            accessGroupJSON.put("gr", accessGroupDetails);
            accessGroupJSON.put("h", md5);
            JSONObject userJSONFinal = new JSONObject();
            userJSONFinal.put("gr", accessGroupJSON);
            userList.add(userJSONFinal);
        }
        // зберігаєм користувачів
        for (User user : users.values()) {
            // користувача System в файл не зберігаєм
            if (user.getTabelNr().equals("System"))
                continue;
            String userNr = user.getTabelNr();
            String userPass = user.getPassSHA256();
            String userAccessGroup = user.getAccessGroupName();
            if (userNr == null || userPass == null || userAccessGroup == null)
                throw new IOException("Неправильно внесений користувач: " + userNr);
            // рахуєм хеш користувача
            StringBuilder data = new StringBuilder();
            data.append(userNr).append(userPass).append(userAccessGroup);
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data.toString());
            JSONObject userDetails = new JSONObject();
            userDetails.put("n", cryptographer.encrypt(userNr));
            userDetails.put("p", userPass);
            userDetails.put("a", cryptographer.encrypt(userAccessGroup));
            JSONObject userJSON = new JSONObject();
            userJSON.put("u", userDetails);
            userJSON.put("h", md5);
            JSONObject userJSONFinal = new JSONObject();
            userJSONFinal.put("us", userJSON);
            userList.add(userJSONFinal);
        }
        //Write JSON file
        try (FileWriter file = new FileWriter(UtilityMethods.getMainPath() + "\\" + USERS_FILE)) {
            file.write(userList.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new IOException("Помилка збереження користувачів.");
        }
    }

    // парсить JSON обєкт користувача і повертає об'єкт User
    private static User parseJsonUser(JSONObject user) {
        //Get employee object within list
        JSONObject employeeObject = (JSONObject) user.get("u");
        //Get employee first name
        String userNr = cryptographer.decrypt((String) employeeObject.get("n"));
        String userPass = (String) employeeObject.get("p");
        String userAccess = cryptographer.decrypt((String) employeeObject.get("a"));
        // get hash
        String userHash = (String) user.get("h");
        // перевіряєм хеш, чи ніхто нічого не міняв в файлі з користувачами
        StringBuilder data = new StringBuilder();
        data.append(userNr).append(userPass).append(userAccess);
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data.toString());
        if (md5.equals(userHash)) {
            return new User(userNr, userPass, userAccess);
        }
        return null;
    }

    // повертає користувачів у вигляді мапи <String, User> = <Табельний користувача, об'єкт користувача>
    public synchronized static HashMap<String, User> getUsers() {
        if (users != null)
            return new HashMap<>(users);
        return null;
    }

    /**
     * Повертає список груп доступу.
     *
     * @return
     */
    public synchronized static Map<String, Map<String, String>> getAccessGroups() {
        return accessGroups;
    }

    /**
     * Задаєм список груп доступу.
     */
    public synchronized static void addAccessGroup(Map<String, String> accessGroup) {
        if (accessGroups == null)
            return;
        accessGroups.put(accessGroup.get("n"), accessGroup);
    }

    /**
     * додає користувача
     */
    public synchronized static void addUser(User user) {
        if (user == null)
            return;
        users.put(user.getTabelNr(), user);
    }

    // видаляє групу прав користувачів по назві
    public synchronized static void removeAccessGroup(String name) {
        if (name != null)
            accessGroups.remove(name);
    }

    // видаляє користувача по табельному номеру
    public synchronized static void removeUser(String tableNr) {
        if (tableNr != null)
            users.remove(tableNr);
    }

    /**
     * Перевіряє чи існує користувач по логіну і паролю. Повертає користувача, або null в іншому випадку.
     * @param tabelNr  табельний номер
     * @param password пароль
     * @return користувача, якщо по вхідним параметрам такий є, або null якщо такого користувача нема.
     */
    public synchronized static User verifyUser(String tabelNr, String password) {
        User user = users.get(tabelNr);
        if (user != null) {
            if (user.getTabelNr().equals(tabelNr) && user.getPassSHA256().equals(getCryptHashSHA256(password))) {
                return user;
            }
        }
        return null;
    }

    //
    public static String getCryptHashSHA256(String password) {
        String result = org.apache.commons.codec.digest.DigestUtils.sha256Hex(password);
        return result;
    }
}