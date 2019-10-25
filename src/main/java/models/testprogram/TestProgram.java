package models.testprogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestProgram {
    private String prgName;                     //8W7974000P
    private String drawingDateOfProgram;        //28062019
    private String drawingOfProgram;            //TAB010619EE
    private String author;                      //Smolenskyy
    private String moduleGroup;                 //L0L
    private List<Wire> wires = new ArrayList<>();           // список проводів які входять в програму
    private List<Connector> connectors = new ArrayList<>(); // список конекторів які входять в програму

    public String getPrgName() {
        return prgName;
    }

    public void setPrgName(String prgName) {
        this.prgName = prgName;
    }

    public String getDrawingDateOfProgram() {
        return drawingDateOfProgram;
    }

    public void setDrawingDateOfProgram(String drawingDateOfProgram) {
        this.drawingDateOfProgram = drawingDateOfProgram;
    }

    public String getDrawingOfProgram() {
        return drawingOfProgram;
    }

    public void setDrawingOfProgram(String drawingOfProgram) {
        this.drawingOfProgram = drawingOfProgram;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getModuleGroup() {
        return moduleGroup;
    }

    public void setModuleGroup(String moduleGroup) {
        this.moduleGroup = moduleGroup;
    }

    public List<Wire> getWires() {
        return new ArrayList<>(wires);
    }

    public void setWires(List<Wire> wires) {
        this.wires = wires;
    }

    public List<Connector> getConnectors() {
        return new ArrayList<>(connectors);
    }

    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }

    public TestProgram(String prgName, String drawingDateOfProgram, String drawingOfProgram, String author, String moduleGroup, List<Wire> wires, List<Connector> connectors) {
        this.prgName = prgName;
        this.drawingDateOfProgram = drawingDateOfProgram;
        this.drawingOfProgram = drawingOfProgram;
        this.author = author;
        this.moduleGroup = moduleGroup;
        this.wires = wires;
        this.connectors = connectors;
    }

    public TestProgram(List<Wire> wires, List<Connector> connectors) {
        this.wires = wires;
        this.connectors = connectors;
    }

    @Override
    public String toString() {
        return "TestProgram{" +
                "prgName='" + prgName + '\'' +
                ", drawingDateOfProgram='" + drawingDateOfProgram + '\'' +
                ", drawingOfProgram='" + drawingOfProgram + '\'' +
                ", author='" + author + '\'' +
                ", moduleGroup='" + moduleGroup + '\'' + "\n" +
                " wiresCount=" + wires.size() + "\n" +
                " wires:\n" + wires + "\n" +
                " connectorsCount=" + connectors.size() + "\n" +
                " connectors:\n" + connectors +
                '}';
    }

    // Тестувальні програми вважаються рівними між собою, коли назва програми, номер і дата креслення, проводи та конектори є ідентичними в них.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestProgram that = (TestProgram) o;
        return Objects.equals(prgName, that.prgName) &&
                Objects.equals(drawingDateOfProgram, that.drawingDateOfProgram) &&
                Objects.equals(drawingOfProgram, that.drawingOfProgram) &&
                Objects.equals(wires, that.wires) &&
                Objects.equals(connectors, that.connectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prgName, drawingDateOfProgram, drawingOfProgram, wires, connectors);
    }
}
