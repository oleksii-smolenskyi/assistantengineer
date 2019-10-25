package models.testprogram;

import java.util.List;

public class Connector {
    public static final int ADAPTED_AS_NEED = 0;
    public static final int ADAPTED_CHECKED = 1;
    public static final int ADAPTED_NOT_CHECKED = 2;

    private String xCode;
    private String description;
    private String connectorNr;
    private int pinsCount;
    private int adapted;
    private List<String> pinsText;

    public Connector(String xCode, String description, String conectorCode, int pinsCount, int adapted) {
        this.xCode = xCode;
        this.description = description;
        this.connectorNr = conectorCode;
        this.pinsCount = pinsCount;
        setAdapted(adapted);
    }

    // метод задання об'єкту "Конектор" списку пінтекстів
    public void setPinsText(List<String> pinsText) {
        this.pinsText = pinsText;
    }

    public int getAdapted() {
        return adapted;
    }

    public void setAdapted(int adapted) {
        if(pinsCount < 2 && pinsCount >= 0 && (adapted < 0 || adapted > 2)) {
            this.adapted = ADAPTED_CHECKED;
            return;
        }
        this.adapted = adapted;
    }

    @Override
    public String toString() {
        return "Connector{" +
                "xCode='" + xCode + '\'' +
                ", description='" + description + '\'' +
                ", connectorNr='" + connectorNr + '\'' +
                ", pinsCount=" + pinsCount +
                ", adapted=" + adapted +
                ", pinsText=" + pinsText +
                '}' + "\n";
    }
}
