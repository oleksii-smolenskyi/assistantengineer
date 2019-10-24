package models;

import utils.UsersManager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tabelNr;
    private String passSHA256;
    private String accessGroupName;
    private Map<String, String> access = new HashMap<>();

    public User(String tabelNr, String passSHA256, String accessGroupName) {
        this.tabelNr = tabelNr;
        this.passSHA256 = passSHA256;
        this.accessGroupName = accessGroupName;
    }

    public String getTabelNr() {
        return tabelNr;
    }

    public String getPassSHA256() {
        return passSHA256;
    }

    // повертає true коли користувач має право на редагування користувачів
    public boolean hasAccessToUsers() {
        if(access != null && access.get("u").equals(UsersManager.FULL_ACCESS))
            return true;
        return false;
    }

    // повертає true коли користувач має право на редагування користувачів
    public boolean hasAccessToPreferencesApp() {
        if(access != null && access.get("pr").equals(UsersManager.FULL_ACCESS))
            return true;
        return false;
    }

    // повертає true коли користувач має право на редагування користувачів
    public boolean hasAccessToPermitsOfModules() {
        System.out.println(access);
        if(access != null && access.get("pm").equals(UsersManager.FULL_ACCESS))
            return true;
        return false;
    }

    // повертає true коли користувач має право на редагування користувачів
    public boolean hasAccessToPinsMeasuringResults() {
        if(access != null && access.get("pmd").equals(UsersManager.FULL_ACCESS))
            return true;
        return false;
    }

    public String getAccessGroupName() {
        return accessGroupName;
    }

    public void setUserRights(String accessGroupName, Map<String, String> accessGroup) {
        if(accessGroup != null && accessGroupName != null) {
            this.access = accessGroup;
            this.accessGroupName = accessGroupName;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "tabelNr='" + tabelNr + '\'' +
                ", passSHA256='" + passSHA256 + '\'' +
                ", accessGroupName='" + accessGroupName + '\'' +
                ", access=" + access +
                '}';
    }
}
