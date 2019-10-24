package services;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class TestReaderTestProgramsFromFolder {

    public static void main(String[] args) {
        try {
            new ReaderTestProgramsFromFolder(Paths.get("S:\\cfwinB9\\Prg_B9\\Arhiv\\Serie48"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReader() {
        try {
            ReaderTestProgramsFromFolder reaader = new ReaderTestProgramsFromFolder(Paths.get("testdata/testprograms"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
