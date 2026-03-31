package simulator.model;

import simulator.misc.Utils;


public class DynamicSupplyRegion extends Region {
	
	private double food;
	private double factor;
	
	public DynamicSupplyRegion(double initialfood, double fact) {
		super();
		if (initialfood <= 0) throw new IllegalArgumentException("La comida inicial debe ser positiva");
		if (fact < 0) throw new IllegalArgumentException("El factor no debe ser negativo");
		
		this.food = initialfood;
		this.factor = fact;
	}

	@Override
	public void update(double dt) {
		
		if (Utils.RAND.nextDouble() < 0.5) {
			food += dt * factor;
		}
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		if (a.getDiet() == Diet.CARNIVORE) {
			return 0.0;
		}

		// Contamos cuántos herbívoros hay en esta región
		int n = 0;
		for (Animal animal : this.animals) {
			if (animal.getDiet() == Diet.HERBIVORE) {
				n++;
			}
		}

		// Calculamos la comida teórica que necesitaría el animal según la fórmula
		double demand = 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt;

		// La comida entregada es el mínimo entre la disponible y la calculada 
		double foodGiven = Math.min(food, demand);

		// Restamos la comida entregada de la reserva de la región
		this.food -= foodGiven;

		return foodGiven;
	}
	

	@Override 
	public String toString() {
		return "Dynamic supply region";
	}
}	