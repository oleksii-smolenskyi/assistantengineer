package models.testprogram;

import java.util.List;

public class Connector {
    public static final int ADAPTED_AS_NEED = 2;
    public static final int ADAPTED_CHECKED = 1;
    public static final int ADAPTED_NOT_CHECKED = 0;

    String connectorName;
    String description;
    String connectorNr;
    int pinsCount;
    int adapted;
    List<String> pinsText;

    public Connector(String connectorName, String description, String conectorCode, int pinsCount) {
        this.connectorName = connectorName;
        this.description = description;
        this.connectorNr = conectorCode;
        this.pinsCount = pinsCount;
    }

    // метод задання об'єкту "Конектор" списку пінтекстів
    public void setPinsText(List<String> pinsText) {
        this.pinsText = pinsText;
    }

    public int getAdapted() {
        return adapted;
    }

    public void setAdapted(int adapted) {
        if(pinsCount < 2 && (adapted < 0 || adapted > 2)) {
            this.adapted = ADAPTED_CHECKED;
            return;
        }
        this.adapted = adapted;
    }

    @Override
    public String toString() {
        return "Connector{" +
                "connectorName='" + connectorName + '\'' +
                ", description='" + description + '\'' +
                ", connectorNr='" + connectorNr + '\'' +
                ", pinsCount=" + pinsCount +
                ", adapted=" + adapted +
                ", pinsText=" + pinsText +
                '}' + "\n";
    }
}
