package models.testmodule;

import java.util.Arrays;
import java.util.List;

public class Module {
	private List<String> params;


	public Module(List<String> params) {
		this.params = params;
	}

	// повертає значення і-го параметра
	public String getParam(int index) {
		try {
			return params.get(index);
		} catch (Exception e) {
			return null;
		}
	}

	public int getCountParams() {
	    return params.size();
    }


	@Override
	public String toString() {
		return "Module{" +
				Arrays.toString(params.toArray()) +
				'}';
	}
}