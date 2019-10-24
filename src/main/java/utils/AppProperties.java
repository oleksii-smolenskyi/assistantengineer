package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import views.MainFrame;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.util.*;

// Клас для керування налаштуваннями програми в файлі properties.
public class AppProperties {
    private static boolean propertiesOK;
    private static final Logger LOGGER = LogManager.getLogger(AppProperties.class.getName());

    private AppProperties() {
    }

    private static Properties properties = new Properties();
    private static final String k = "Bar12345Bar12345";
    private static final Key aesKey;
    private static Cipher cipherEncrypt;
    private static Cipher cipherDecrypt;
    private static final String PROPERTIES_FILE = "TableModules.properties";
    private static String PROPERTIES_FILE_PATH;

    static {
        aesKey = new SecretKeySpec(k.getBytes(), "AES");
        try {
            cipherEncrypt = Cipher.getInstance("AES");
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, aesKey);
            cipherDecrypt = Cipher.getInstance("AES");
            cipherDecrypt.init(Cipher.DECRYPT_MODE, aesKey);
        } catch (Exception e) {
            Frames.showErrorMessage(null, "Помилка ініціалізації шифрувальника.");
            LOGGER.error(e.getMessage());
        }
        try {
            PROPERTIES_FILE_PATH = UtilityMethods.getMainPath() + "\\" + PROPERTIES_FILE;
        } catch (IOException e) {
            Frames.showErrorMessage(null, "Помилка отримання шляху до теки програми.");
            LOGGER.error(e.getMessage());
        }
        loadPropertiesFromFile();
    }

    /**
     * Повертає значення властивості по ключу.
     * @param key
     * @return
     */
    public static synchronized String getProperty(String key) {
        try {
            String propertiesHashKey = "properties.h";
            // Якщо ключ є ключом властивості хешу всіх властивостей, тоді ми його повертаєм без розшифрування, оскільки він зберігається беш шифрування
            if(key.equals(propertiesHashKey))
                return properties.getProperty(key);
            else // повертаємо розшифроване значення властивості
                return new String(cipherDecrypt.doFinal(properties.getProperty(key).getBytes()));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Повертає значення властивості по ключу. Якщо властивості з таким ключом не знайдено повертається defaultValue.
     * @param key
     * @param defaultValue
     * @return
     */
    public static synchronized String getProperty(String key, String defaultValue) {
        try {
            String propertiesHashKey = "properties.h";
            // Якщо ключ є ключом властивості хешу всіх властивостей, тоді ми його повертаєм без розшифрування, оскільки він зберігається беш шифрування
            if(key.equals(propertiesHashKey))
                return properties.getProperty(key);
            else // повертаємо розшифроване значення властивості
                return new String(cipherDecrypt.doFinal(properties.getProperty(key).getBytes()));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static synchronized void setProperty(String key, String value) {
        if (value == null)
            value = "";
        try {
            if(key.equals("properties.h"))
                properties.setProperty(key, value);
            else
                properties.setProperty(key, new String(cipherEncrypt.doFinal(value.getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // метод збереження властивостей у файл .properties
    public static synchronized void savePropertiesToFile() {
        // визначаєм хеш-код суми рядків значень значень властивостей
        StringBuilder textProperties = new StringBuilder();
        List<String> keys = new ArrayList<>(properties.stringPropertyNames());
        // сортуємо ключі, щоб хеш-код був одинаковий
        keys.sort((o1, o2) -> o1.compareTo(o2));
        for (String key : keys) {
            if (key.equals("properties.h"))
                continue;
            textProperties.append(key).append(getProperty(key));
        }
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(textProperties.toString());
        // задаєм параметр хеш-коду
        setProperty("properties.h", UsersManager.getCryptHashSHA256(md5));
        // зберігаємо властивості
        try {
            properties.store(new FileOutputStream(new File(PROPERTIES_FILE_PATH)), "");
        } catch (IOException e) {
            Frames.showErrorMessage(null, "Помилка збереження файла з параметрами.");
            LOGGER.error(e.getMessage());
        }
        LOGGER.info(MainFrame.getInstance().getCurrentUser().getTabelNr() + " зберіг файл властивостей.");
    }

    public static synchronized void clearProperties() {
        properties = new Properties();
    }

    // Завантажує властивості з файлу .properties
    public static synchronized void loadPropertiesFromFile() {
        StringBuilder propertiesFileName = new StringBuilder();
        try {
            properties.load(new FileReader(PROPERTIES_FILE_PATH));
            if (!isPropertiesHashOK()) {
                propertiesOK = false;
                Frames.showErrorMessage(null, "Хтось ковиряв налаштування вручну!");
                LOGGER.warn("Хтось ковиряв налаштування вручну!");
            } else {
                propertiesOK = true;
            }
        } catch (IOException e) {
            Frames.showErrorMessage(null, "Помилка завантаження файла з параметрами.");
            LOGGER.error(e.getMessage());
        }
    }

    // перевіряє хеш-код збережений в файлі з фактично підрахованим на основі зчитаних властивостей
    private static boolean isPropertiesHashOK() {
        String hash = getProperty("properties.h");
        StringBuilder textProperties = new StringBuilder();
        List<String> keys = new ArrayList<>(properties.stringPropertyNames());
        // сортуємо ключі, щоб хеш-код був одинаковий
        keys.sort((o1, o2) -> o1.compareTo(o2));
        // формуємо всі властивості в один об'єкт
        for (String key : keys) {
            if (key.equals("properties.h"))
                continue;
            textProperties.append(key).append(getProperty(key));
        }
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(textProperties.toString());
        // перевіряємо хеш-код збережений в файлі з фактично підрахованим на основі зчитаних властивостей
        if (UsersManager.getCryptHashSHA256(md5).equals(hash))
            return true;
        return false;
    }

    public static boolean isPropertiesOK() {
        return propertiesOK;
    }

    public static void setPropertiesOK(boolean propertiesOK) {
        AppProperties.propertiesOK = propertiesOK;
        MainFrame.getInstance().setEnableButtons(propertiesOK);
    }
}
