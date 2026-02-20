package simulator.model;

import simulator.misc.Vector2D;

public class DefaultRegion extends Region{

	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		if (a.getDiet() == Diet.CARNIVORE) return 0.0;
		
		int n = 0;
		for (Animal an : animals) {
			if (an.getDiet() == Diet.HERBIVORE) {
				n++;
			}
		}
		return 60.0*Math.exp(-Math.max(0,n-5.0)*2.0)*dt;
		
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
