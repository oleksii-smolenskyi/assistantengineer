package models.testprogram;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    // конструктор з вхідним параметром File, об'єкт створючатиметься на основі зчитаних даних з файлу(програми тестування)
    public TestProgram(File file) throws IOException {
        if(Objects.isNull(file))
            throw new IOException("Не заданий файл.");
        if(!file.toString().toLowerCase().endsWith(".ord") && !file.toString().toLowerCase().endsWith(".prg")) {
            throw new IOException("Не відповідний формат файлу: " + file.toString());
        }
        AtomicInteger lineId = new AtomicInteger(0);        // лічильник пройдених рядків в файлі
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while(reader.ready()) {
                line = readLineFromFile(reader, lineId);
                if (line.startsWith("Zn:")) {
                    prgName = line.substring(line.indexOf("Zn:") + 3);
                } else if (line.startsWith("Ec:")) {
                    drawingDateOfProgram = line.substring(line.indexOf("Ec:") + 3);
                } else if (line.startsWith("Rr:")) {
                    author = line.substring(line.indexOf("Rr:") + 3);
                } else if (line.startsWith("Zm:")) {
                    drawingOfProgram = line.substring(line.indexOf("Zm:") + 3);
                } else if (line.startsWith("Lk:")) {
                    moduleGroup = line.substring(line.indexOf("Lk:") + 3);
                } else if (line.startsWith("Vb:")) {
                    line = readLineFromFile(reader, lineId);
                    String diameter = null;
                    String wireNr = null;
                    String connectorName1 = null;
                    String connectorName2 = null;
                    String pin1 = null;
                    String pin2 = null;
                    String color = null;
                    while (line.startsWith("Vn:") || line.startsWith("St:") || line.startsWith("Vq:")
                            || line.startsWith("Vf:") || line.startsWith("T1:") || line.startsWith("T2:")
                            || line.startsWith("X1:") || line.startsWith("X2:") || line.startsWith("Tx:")) {
                        String startLine = line.substring(0, 3);
                        switch (startLine) {
                            case "Vq:" : diameter = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "Vf:" : color = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "T1:" : pin1 = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "T2:" : pin2 = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "X1:" : connectorName1 = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "X2:" : connectorName2 = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "Tx:" : wireNr = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            default: line = readLineFromFile(reader, lineId);
                        }
                    }
                    if(diameter != null && wireNr != null && connectorName1 != null && connectorName2 != null
                        && pin1 != null && pin2 != null && color != null) {
                        wires.add(new Wire(diameter, wireNr, connectorName1, connectorName2, pin1, pin2, color));
                    }
                } else if (line.startsWith("Xc:")) {
                    String connectorName = line.substring(3);
                    line = readLineFromFile(reader, lineId);
                    String description = null;
                    int pins = 0;
                    String text = null;
                    String conectorCode = null;
                    cycle: while (line.startsWith("Sb:") || line.startsWith("Sc:") || line.startsWith("Sk:")
                            || line.startsWith("Sg:") || line.startsWith("Vt:") || line.startsWith("Pt:")) {
                        String startLine = line.substring(0, 3);
                        switch (startLine) {
                            case "Sc:" : conectorCode = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "Sg:" : description = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "Sb:" : text = line.substring(3).trim(); line = readLineFromFile(reader, lineId); break;
                            case "Sk:" : pins = Integer.parseInt(line.substring(3).trim()); line = readLineFromFile(reader, lineId); break;
                            case "Vt:" : line = readLineFromFile(reader, lineId); break;
                            case "Pt:" : line = readLineFromFile(reader, lineId); break;
                            default: break cycle;
                        }
                    }
                    if(connectorName != null && conectorCode != null) {
                        connectors.add(new Connector(connectorName, description, conectorCode, pins));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Файл " + file + " не знайдено.");
        } catch (IOException e) {
            throw new IOException("Помилка читання файлу: " + file);
        }
    }

    // читає стрічку з потоку та інкрементить лічильник стрічок
    private String readLineFromFile(BufferedReader reader, AtomicInteger lineId) throws IOException {
        lineId.incrementAndGet();
        String resultLine = reader.readLine().trim();
        return resultLine;
    }

    public TestProgram(Connection dbConnection, String testProgramName) {

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
}
