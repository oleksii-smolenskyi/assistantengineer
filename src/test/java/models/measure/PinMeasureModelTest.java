package models.measure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PinMeasureModelTest {

    @Test
    void nullArgument() {
        Assertions.assertThrows(IOException.class, () -> new PinMeasureModel(null));
    }

    @Test
    void emptyArgument() {
        Assertions.assertThrows(IOException.class, () -> new PinMeasureModel(new ArrayList<>()));
    }

    @Test
    void moreCountParameters() {
        Assertions.assertThrows(IOException.class, () -> new PinMeasureModel(new ArrayList<>(11)));
    }

    @Test
    void lessCountParameters() {
        Assertions.assertThrows(IOException.class, () -> new PinMeasureModel(new ArrayList<>(9)));
    }

    @Test
    void nokFormatParameters() {
        Assertions.assertThrows(NumberFormatException.class, () -> new PinMeasureModel(new ArrayList<>(Arrays.asList(new String[] {"", "", "", "", "", "", "", "", "" }))));
    }

    @Test
    void okParameters() {
        /*
        0 - String : Test table; 1 - String : Test module; 2 - Integer : Pin number;
     *                  3 - Double : measured value; 4 - Double : nominal value; 5 - String : date; 6 - String : pin type;
     *                 7 - Double : floorTolerance; 8 - Double : ceilTolerance ; 9 - "OK" ||"nOK"
         */
        List<String> params = new ArrayList<>(10);
        params.add("Test table");
        params.add("Test module");
        params.add("0");
        params.add("1.55");
        params.add("1.5");
        params.add("43557.4");
        params.add("Pin type");
        params.add("10.0");
        params.add("10.0");
        PinMeasureModel pinMeasureModel = null;
        try {
            pinMeasureModel = new PinMeasureModel(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("Test table", pinMeasureModel.getParam(0));
        Assertions.assertEquals("Test module", pinMeasureModel.getParam(1));
        Assertions.assertEquals("1", pinMeasureModel.getParam(2));
        Assertions.assertEquals("1.55", pinMeasureModel.getParam(3));
        Assertions.assertEquals("1.5", pinMeasureModel.getParam(4));
        Assertions.assertEquals("02.04.2019 12:36:00", pinMeasureModel.getParam(5));
        Assertions.assertEquals("Pin type", pinMeasureModel.getParam(6));
        Assertions.assertEquals("10.0", pinMeasureModel.getParam(7));
        Assertions.assertEquals("10.0", pinMeasureModel.getParam(8));
        Assertions.assertEquals(PinMeasureModel.MEASURED_OK, pinMeasureModel.getParam(9));
        params = new ArrayList<>(10);
        params.add("Test table");
        params.add("Test module");
        params.add("0");
        params.add("1.66");
        params.add("1.5");
        params.add("43557.4");
        params.add("Pin type");
        params.add("10.0");
        params.add("10.0");
        pinMeasureModel = null;
        try {
            pinMeasureModel = new PinMeasureModel(params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals("Test table", pinMeasureModel.getParam(0));
        Assertions.assertEquals("Test module", pinMeasureModel.getParam(1));
        Assertions.assertEquals("1", pinMeasureModel.getParam(2));
        Assertions.assertEquals("1.66", pinMeasureModel.getParam(3));
        Assertions.assertEquals("1.5", pinMeasureModel.getParam(4));
        Assertions.assertEquals("02.04.2019 12:36:00", pinMeasureModel.getParam(5));
        Assertions.assertEquals("Pin type", pinMeasureModel.getParam(6));
        Assertions.assertEquals("10.0", pinMeasureModel.getParam(7));
        Assertions.assertEquals("10.0", pinMeasureModel.getParam(8));
        Assertions.assertEquals(PinMeasureModel.MEASURED_NOK, pinMeasureModel.getParam(9));
    }
}

