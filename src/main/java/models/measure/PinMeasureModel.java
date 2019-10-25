package models.measure;

import utils.UtilityMethods;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

// Модель запису виміру тестувальної голки.
public class PinMeasureModel {
    static final int PARAMS_COUNT = 10;
    public static final String MEASURED_OK = "OK";
    public static final String MEASURED_NOK = "nOK";
    private List<String> params;

    /**
     *
     * @param params    0 - String : Test table; 1 - String : Test module; 2 - Integer : Pin number;
     *                  3 - Double : measured value; 4 - Double : nominal value; 5 - String : date; 6 - String : pin type;
     *                 7 - Double : floorTolerance; 8 - Double : ceilTolerance ; 9 - "OK" ||"nOK"
     * @throws IOException
     */
    public PinMeasureModel(List<String> params) throws IOException {
        if(params != null && params.size() == PARAMS_COUNT - 1) {
            this.params = params;
            // коригуємо номер піна(в базі починається з 0)
            Integer pinNumber = Integer.parseInt(params.get(2));
            pinNumber++;
            this.params.set(2, pinNumber.toString());
            // опрацьовуєм дані вимірів
            Double measuredValue = Double.parseDouble(params.get(3));
            Double nominalValue = Double.parseDouble(params.get(4));
            Double floorTolerance = Double.parseDouble(params.get(7));
            Double ceilTolerance = Double.parseDouble(params.get(8));
            // визначається в яку сторону заокруглювати, якщо результат не входить в верхню межу толеранції, заокруглюєм до меншого, в іншому випадку до більшого
            if(nominalValue * (1.0 + ceilTolerance / 100) < measuredValue) {
                measuredValue = Math.floor(measuredValue * 100) / 100;
            } else {
                measuredValue = Math.ceil(measuredValue * 100) / 100;
            }
            // визначаєм чи виміряне значення є в толеранції
            if(nominalValue * (1.0 + ceilTolerance / 100) > measuredValue && nominalValue * (1.0 - floorTolerance / 100) < measuredValue) {
                this.params.add(9, MEASURED_OK);
            } else {
                this.params.add(9, MEASURED_NOK);
            }
            this.params.set(3, measuredValue.toString());
            // опрацьовуєм дату
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
            this.params.set(5, dateFormat.format(UtilityMethods.fromDoubleToDateTime(Double.parseDouble(params.get(5)))));
        }
        else
            throw new IOException("Помилковий список параметрів для створення моделі виміру голки.");
    }

    /**
     *
     * @param index індекс параметру
     * @return
     */
    public String getParam(int index) {
        return params.get(index);
    }

    @Override
    public String toString() {
        return "PinMeasureModel{" +
                "params=" + params +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PinMeasureModel that = (PinMeasureModel) o;
        return Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(params);
    }
}
