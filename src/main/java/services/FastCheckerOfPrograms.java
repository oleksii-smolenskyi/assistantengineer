package services;

import models.Progressable;
import models.testprogram.TestProgram;

import java.io.IOException;
import java.util.List;

// Клас для забезпечення "швидкої" перевірки тест програми на можливі грубі помилки, допущені при її створенні
public class FastCheckerOfPrograms implements Progressable {
    private ReaderTestPrograms readerTestPrograms;
    private boolean checkEmptyConnectors;
    private boolean checkNotConnectedWire;
    private boolean checkSoftAdapted;
    private boolean checkSpliceAdapted;
    private String statusMessage;


    public FastCheckerOfPrograms(ReaderTestPrograms readerTestPrograms, boolean checkEmptyConnectors,
                                 boolean checkNotConnectedWire, boolean checkSoftAdapted, boolean checkSpliceAdapted) {
        this.readerTestPrograms = readerTestPrograms;
        this.checkEmptyConnectors = checkEmptyConnectors;
        this.checkNotConnectedWire = checkNotConnectedWire;
        this.checkSoftAdapted = checkSoftAdapted;
        this.checkSpliceAdapted = checkSpliceAdapted;
        checkPrograms();
    }

    // Перевіряє програми по заданим параметрам
    private void checkPrograms() {
        while(!readerTestPrograms.isReady()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        List<TestProgram> programs = readerTestPrograms.getPrograms();
        for(TestProgram program : programs) {
            // TODO checking programs
            System.out.println(program.getPrgName());
        }
    }

    @Override
    public int getReady() {
        return 0;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    // Змінна в якій буде зберігатися IOException, якщо такий виникне під час ініціалізації чи роботи об'єкта читача тест. програм
    private IOException stopException;

    @Override
    public IOException getStopException() {
        return stopException;
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }
}
