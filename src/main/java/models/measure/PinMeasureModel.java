package models.measure;

import utils.UtilityMethods;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PinMeasureModel {
    static final int PARAMS_COUNT = 10;
    private List<String> params;

    /**
     *
     * @param params
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
                this.params.add(9, "OK");
            } else {
                this.params.add(9, "nOK");
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
}
