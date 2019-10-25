package services;

import models.testprogram.Connector;
import models.testprogram.TestProgram;
import models.testprogram.Wire;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

// Читач програм тестування з теки, реалізація інтерфейсу ReaderTestPrograms.
// Зчитує всі тест програми з теки і всіх вкладених тек, та формує список з об'єктів TestProgram.
public class ReaderTestProgramsFromFolder implements ReaderTestPrograms {
    // шлях до теки з програмами
    private Path path;

    // Конструктор класу приймає параметром об'єкт Path, який є шляхом до теки з програмами тестування, які потрібно зчитати
    public ReaderTestProgramsFromFolder(Path path) throws IOException {
        // перевіряємо чи існує такий шлях
        if (!Files.exists(path))
            throw new IOException("Шлях " + path + " не існує.");
        // TODO перевірку чи вхідний шлях є текою
        this.path = path;
        loadPrograms(); // зчитуєм програми тестування
    }

    // змінна, яка відображає чи зчитувач програм завершив роботу
    private int ready = 0;

    List<TestProgram> readedPrograms = new ArrayList<>();   // список зчитаних програм
    List<String> failedList = new ArrayList<>();            // список файлів, які не можливо було прочитати з певних причин

    // Зчитує програми тестування
    private void loadPrograms() {
        // зчитувати програми тестування будем в окремому потоці
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // здійснюється прохід по файлам, що знаходяться в теці
                    Files.walkFileTree(path, new SimpleFileVisitor(){
                        @Override
                        public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
                            System.out.println(file);
                            if(file.toString().toLowerCase().endsWith("prg")) {
                                readedPrograms.add(loadTestProgramFromFile(new File(file.toString())));
                            }
                            return super.visitFile(file, attrs);
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
                            failedList.add(file.toString());
                            return super.visitFileFailed(file, exc);
                        }
                    });
                } catch (IOException e) {
                    stopException = e;
                    ready = -1;
                }
                ready = 100;
            }
        };
        // запускаєм потік на виконання, як демон
        Thread threadTask = new Thread(task);
        threadTask.setDaemon(true);
        threadTask.start();

        // TODO СТЕРТИ ЦЕЙ КУСОК ПІСЛЯ ВІДЛАДКИ
        while(threadTask.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(TestProgram testProgram : readedPrograms) {
            System.out.println(testProgram);
        }
        System.out.println("count programs: " + readedPrograms.size());
        // КІНЕЦЬ СТИРАННЯ
    }

    public static TestProgram loadTestProgramFromFile(File file) throws IOException {
        if(Objects.isNull(file))
            throw new IOException("Не заданий файл.");
        if(!file.toString().toLowerCase().endsWith(".ord") && !file.toString().toLowerCase().endsWith(".prg")) {
            throw new IOException("Не відповідний формат файлу: " + file.toString());
        }
        AtomicInteger lineId = new AtomicInteger(0);        // лічильник пройдених рядків в файлі
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            String prgName = null;
            String drawingDateOfProgram = null;
            String author = null;
            String drawingOfProgram = null;
            String moduleGroup = null;
            List<Wire> wires = new ArrayList<>();
            List<Connector> connectors = new ArrayList<>();
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
                    int adapted = 0;
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
                            case "Vt:" : adapted = Integer.parseInt(line.substring(3).trim()); line = readLineFromFile(reader, lineId); break;
                            case "Pt:" : line = readLineFromFile(reader, lineId); break;
                            default: break cycle;
                        }
                    }
                    if(connectorName != null && conectorCode != null) {
                        connectors.add(new Connector(connectorName, description, conectorCode, pins, adapted));
                    }
                }
            }
            return new TestProgram(prgName, drawingDateOfProgram, drawingOfProgram, author, moduleGroup, wires, connectors);
        } catch (FileNotFoundException e) {
            throw new IOException("Файл " + file + " не знайдено.");
        } catch (IOException e) {
            throw new IOException("Помилка читання файлу: " + file);
        }
    }

    // читає стрічку з потоку та інкрементить лічильник стрічок
    private static String readLineFromFile(BufferedReader reader, AtomicInteger lineId) throws IOException {
        lineId.incrementAndGet();
        String resultLine = reader.readLine().trim();
        return resultLine;
    }

    // Повертає список зчитаних програм
    @Override
    public List<TestProgram> getPrograms() {
        if(isReady())
            return readedPrograms;
        else
            return null;
    }

    // Повертає числове відображення готовності об'єкта
    @Override
    public synchronized int getReady() {
        return ready;
    }

    // Повертає true у випадку коли зчитувач програм завершив свою роботу. В іншому випадку повертає false.
    @Override
    public synchronized boolean isReady() {
        return ready >= 100 ? true : false;
    }

    // Змінна в якій буде зберігатися IOException, якщо такий виникне під час ініціалізації чи роботи об'єкта читача тест. програм
    private IOException stopException;

    // Повертає виняток на якому припинив свою роботу об'єкт
    @Override
    public IOException getStopException() {
        return stopException;
    }

    // Повертає список файлів, які не можливо було зчитати з певних причин при обході теки з тест. програмами
    public List<String> getFailedList() {
        // якщо зчитувач завершив свою роботу повертаєм список
        if(isReady())
            return failedList;
        else // якщо не завершив свою роботу повертаєм null
            return null;
    }


}
