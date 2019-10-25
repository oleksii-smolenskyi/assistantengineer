package models;

import java.io.IOException;

public interface Progressable {
    int getReady();
    boolean isReady();
    String getStatusMessage();
    IOException getStopException();
}
