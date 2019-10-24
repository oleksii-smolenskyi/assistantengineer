package services;

import models.Progressable;

import java.io.File;
import java.io.IOException;

public class ReaderTestProgramsFromKonzept implements Progressable {
    private int ready = 0;

    @Override
    public synchronized int getReady() {
        return 0;
    }

    @Override
    public synchronized boolean isReady() {
        return false;
    }

    IOException stopException;
    @Override
    public synchronized IOException getStopException() {
        return stopException;
    }

    private File fileKonzept;

    public ReaderTestProgramsFromKonzept(File fileKonzept) throws IOException {
        if(fileKonzept.toString().endsWith(".xlsx")) {
            throw new IOException("Не підтримуваний формат файлу " + fileKonzept);
        }
        this.fileKonzept = fileKonzept;
    }


}
