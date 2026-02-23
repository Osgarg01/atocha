package simulator.factories;
import org.json.JSONObject;
import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {
	
	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic supply region");
	}

	@Override
	protected Region createInstance(JSONObject data) {
		// Valores por defecto especificados en el enunciado
		double factor = 2.0;
		double food = 100.0;

		if (data.has("factor")) factor = data.getDouble("factor");
		if (data.has("food")) food = data.getDouble("food");

		// Validamos según la constructora de DynamicSupplyRegion
		if (factor < 0) throw new IllegalArgumentException("Factor cannot be negative");
		if (food <= 0) throw new IllegalArgumentException("Food must be positive");

		return new DynamicSupplyRegion(food, factor);
	}
}