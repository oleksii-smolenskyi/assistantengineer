package services;

import models.testprogram.TestProgram;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

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
                                readedPrograms.add(new TestProgram(new File(file.toString())));
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
        // КІНЕЦЬ СТРИРАННЯ
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
