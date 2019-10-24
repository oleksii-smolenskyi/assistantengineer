package models.testprogram;

import java.util.Objects;

public class Wire {
    String diameter;
    String wireNr;
    String connectorName1;
    String connectorName2;
    String pin1;
    String pin2;
    String color;

    public Wire(String diameter, String wireNr, String connectorName1, String connectorName2, String pin1, String pin2, String color) {
        this.diameter = diameter;
        this.wireNr = wireNr;
        this.connectorName1 = connectorName1;
        this.connectorName2 = connectorName2;
        this.pin1 = pin1;
        this.pin2 = pin2;
        this.color = color;
    }

    @Override
    public String toString() {
        return "Wire{" +
                "diameter='" + diameter + '\'' +
                ", wireNr='" + wireNr + '\'' +
                ", connectorName1='" + connectorName1 + '\'' +
                ", connectorName2='" + connectorName2 + '\'' +
                ", pin1='" + pin1 + '\'' +
                ", pin2='" + pin2 + '\'' +
                ", color='" + color + '\'' +
                '}' + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wire wire = (Wire) o;
        return Objects.equals(diameter, wire.diameter) &&
                Objects.equals(wireNr, wire.wireNr) &&
                Objects.equals(connectorName1, wire.connectorName1) &&
                Objects.equals(connectorName2, wire.connectorName2) &&
                Objects.equals(pin1, wire.pin1) &&
                Objects.equals(pin2, wire.pin2) &&
                Objects.equals(color, wire.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diameter, wireNr, connectorName1, connectorName2, pin1, pin2, color);
    }
}
