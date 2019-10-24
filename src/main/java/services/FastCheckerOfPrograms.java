package services;

import models.Progressable;

import java.io.IOException;

// ���� ��� ������������ "������" �������� ���� �������� �� ������ ���� �������, ������� ��� �� ��������
public class FastCheckerOfPrograms implements Progressable {
    private ReaderTestPrograms readerTestPrograms;
    private boolean checkEmptyConnectors;
    private boolean checkNotConnectedWire;
    private boolean checkSoftAdapted;
    private boolean checkSpliceAdapted;

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
}
