package models.testprogram;

import java.io.File;
import java.io.IOException;

public class TestTestProgram {

    public static void main(String[] args) {
        try {
            TestProgram testProgram = new TestProgram(new File("S:\\cfwinB9\\Prg_B9\\SON0219\\1L7029F.PRG"));
            System.out.println(testProgram);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
