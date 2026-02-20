package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

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
		// TODO Auto-generated method stub
		if (Utils.RAND.nextDouble() < 0.5) {
			food += dt * factor;
		}
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		if (a.getDiet() == Diet.CARNIVORE) {
			return 0.0;
		}

		// Contamos cuántos herbívoros hay en esta región (n)
		int n = 0;
		for (Animal animal : this.animals) {
			if (animal.getDiet() == Diet.HERBIVORE) {
				n++;
			}
		}

		// Calculamos la comida teórica que necesitaría el animal según la fórmula
		double demand = 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt;

		// La comida entregada es el mínimo entre la disponible (food) y la calculada (demand)
		double foodGiven = Math.min(food, demand);

		// Restamos la comida entregada de la reserva de la región
		this.food -= foodGiven;

		return foodGiven;
	}
	

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector2D getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGeneticCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Diet getDiet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSightRange() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getEnergy() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAge() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Vector2D getDestination() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPregnant() {
		// TODO Auto-generated method stub
		return false;
	}

}
