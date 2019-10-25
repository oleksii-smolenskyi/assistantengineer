package services;

import models.Progressable;

import java.io.IOException;

// Клас для забезпечення "швидкої" перевірки тест програми на можливі грубі помилки, допущені при її створенні
public class FastCheckerOfPrograms implements Progressable {
    private ReaderTestPrograms readerTestPrograms;
    private boolean checkEmptyConnectors;
    private boolean checkNotConnectedWire;
    private boolean checkSoftAdapted;
    private boolean checkSpliceAdapted;
    private String statusMessage;

    public FastCheckerOfPrograms(ReaderTestPrograms readerTestPrograms, boolean checkEmptyConnectors, boolean checkNotConnectedWire, boolean checkSoftAdapted, boolean checkSpliceAdapted) {
        this.readerTestPrograms = readerTestPrograms;
        this.checkEmptyConnectors = checkEmptyConnectors;
        this.checkNotConnectedWire = checkNotConnectedWire;
        this.checkSoftAdapted = checkSoftAdapted;
        this.checkSpliceAdapted = checkSpliceAdapted;
        checkPrograms();
    }

    private void checkPrograms() {
    }

    @Override
    public int getReady() {
        return 0;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public IOException getStopException() {
        return null;
    }

    @Override
    public String getStatusMessage() {
        return null;
    }
}
