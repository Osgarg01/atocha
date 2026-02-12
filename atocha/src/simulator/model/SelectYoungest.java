package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as == null || as.isEmpty()) {
			return null;
		}
		
		Animal youngest = null;
		for (Animal other : as) {
			if (youngest == null || other.getAge() < youngest.getAge()) {
				youngest = other;
			}
		}
		return youngest;
	}

}
