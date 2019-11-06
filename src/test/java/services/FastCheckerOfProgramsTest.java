package services;

import java.io.IOException;
import java.nio.file.Paths;

public class FastCheckerOfProgramsTest {

    public static void main(String[] args) {
        try {
            new FastCheckerOfPrograms(new ReaderTestProgramsFromFolder(Paths.get("src/test/resources/testprograms")),
                    true, true, true, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
