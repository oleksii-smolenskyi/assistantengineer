package models.testmodule;

import java.io.File;
import java.util.*;

/**
 * Різниця від класу Model в тому, що є файли допусків.
 */
public class ModuleForPermitModel extends Module {
    private Set<File> permitFiles; // файли допусків модуля

    public ModuleForPermitModel(List<String> params) {
        super(params);
    }

    /**
     * Повертає параметр модуля по індексу
     * @param index Індекс параметра модуля
     * @return значення параметра модуля
     */
    @Override
    public String getParam(int index) {
        if(index <= getCountParams()) {
            if (index == getCountParams())
                if(permitFiles != null)
                    return permitFiles.size() > 0 ? "є" : null;
                else
                    return null;
            else
                return super.getParam(index);
        }
        return null;
    }

    /**
     * Додає до модуля колекцію з файлів допусків.
     * @param collection Колекція з файлів допусків
     */
    public void addPermitFiles(Collection<File> collection) {
        //System.out.println(collection);
        if(collection != null) {
            permitFiles = new LinkedHashSet<>(collection);
        }
    }

    /**
     * Повертає сет файлів допусків модуля
     * @return сет файлів допусків модуля, якщо допусків немає тоді null
     */
    public Set<File> getPermitFiles() {
        return permitFiles;
    }

    /**
     * Повертає кількість файлів допусків для тест модуля (може бути декілька файлів для одного модуля)
      * @return кількість файлів допусків для тест модуля
     */
    public int getCountPermitFiles() {
        return permitFiles.size();
    }
}
