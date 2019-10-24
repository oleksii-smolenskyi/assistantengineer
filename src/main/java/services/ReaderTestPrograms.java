package services;

import models.Progressable;
import models.testprogram.TestProgram;
import java.util.List;

// Інтерфейс читача програм тестування
public interface ReaderTestPrograms extends Progressable {
    // Повертає список зчитаних програм тестування
    List<TestProgram> getPrograms();
}
