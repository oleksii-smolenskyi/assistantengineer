package services;

import models.testprogram.TestProgram;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ReaderTestProgramsFromFolderTest {

    @Test
    void testExceptions() {
        Assertions.assertThrows(IOException.class, () -> new ReaderTestProgramsFromFolder(Paths.get("phantom_path")));
    }

    @Test
    public void testReader() {
        List<TestProgram> data = new ArrayList<>();
        ReaderTestProgramsFromFolder reader = null;
        try {
            reader = new ReaderTestProgramsFromFolder(Paths.get("src/test/resources/testprograms"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(!reader.isReady()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertEquals (data, reader.getPrograms());
    }
}
