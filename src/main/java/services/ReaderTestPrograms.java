package services;

import models.Progressable;
import models.testprogram.TestProgram;
import java.util.List;

// ��������� ������ ������� ����������
public interface ReaderTestPrograms extends Progressable {
    // ������� ������ �������� ������� ����������
    List<TestProgram> getPrograms();
}
