package models.testprogram;

import org.junit.jupiter.api.BeforeAll;
import services.ReaderTestProgramsFromFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestProgramTest {
    List<TestProgram> samples = new ArrayList<>();

    @BeforeAll
    void formSamples() {
        //TestProgram testProgram = new TestProgram();
    }

    public static void main(String[] args) {
        try {
            TestProgram testProgram = ReaderTestProgramsFromFolder.loadTestProgramFromFile(new File("S:\\cfwinB9\\Prg_B9\\SON0219\\1L7029F.PRG"));
            System.out.println(testProgram);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
