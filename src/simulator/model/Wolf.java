package simulator.model;

import simulator.misc.Constants;
import simulator.misc.Utils;
import simulator.misc.Vector2D;
import java.util.List;

public class Wolf extends Animal{
    private Animal huntTarget;
    private SelectionStrategy huntingStrategy;

    public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy,  Vector2D pos){
        super(Constants.WOLF_GENETIC_CODE, Diet.CARNIVORE, Constants.INIT_SIGHT_WOLF, Constants.INIT_SPEED_WOLF, mateStrategy, pos);
        this.huntTarget = null;
        if (huntingStrategy == null)
            throw new IllegalArgumentException("huntingStrategy cannot be null");
        this.huntingStrategy = huntingStrategy;
    }

    protected Wolf(Wolf p1, Animal p2){
        super(p1,p2);
        this.huntingStrategy = p1.huntingStrategy;
        this.huntTarget = null;
    }

    @Override
    protected void setNormalStateAction() {
    	this.huntTarget = null;
    	this.mateTarget = null;
    }
    
    @Override
    protected void setMateStateAction() {
    	this.huntTarget = null;
    }
    
    @Override
    protected void setHungerStateAction() {
    	this.mateTarget = null;
    }
    
    @Override
    protected void setDangerStateAction() {
    	// Wolves do not use DANGER in this model, clear hunt/mate targets
    	this.huntTarget = null;
    	this.mateTarget = null;
    }

    @Override
    protected void setDeadStateAction() {
    	this.huntTarget = null;
    	this.mateTarget = null;
    }
    
    @Override
    public void update(double dt) {
    	// 1. Si el estado es DEAD no hacer nada
    	if (state == State.DEAD) return;
    	
    	// 2. Actualizar el objeto según el estado del animal
    	updateState(dt);
    	
    	// 3. Si la posición está fuera del mapa, ajustarla y cambiar su estado a NORMAL
    	if (adjustPosition()) {
    		setState(State.NORMAL);
    	}

    	// 4. Si energy es 0.0 o age es mayor de 14.0, cambiar su estado a DEAD
    	if (energy <= 0.0 || age > Constants.MAX_AGE_WOLF) {
    		setState(State.DEAD);
    		return;
    	}
    	
    	// 5. Si su estado no es DEAD, pide comida al gestor
    	if (state != State.DEAD && regionMngr != null) {
    		double food = regionMngr.getFood(this, dt);
    		energy = Utils.constrainValueInRange(energy + food, 0.0, Constants.MAX_ENERGY);
    	}
    	
    }
    
        @Override
    public void updateState (double dt) {
       
        switch (state) {
            case NORMAL:
                updateNormal(dt);
                break;

            case HUNGER:
                updateHunger(dt);
                break;

            case MATE:
            	updateMate(dt);
                break;

            case DANGER:
            case DEAD:
            default:
                break;
                
        }

    }
        
    private void updateMate(double dt) {
    	if (mateTarget != null && (mateTarget.getState() == State.DEAD || pos.distanceTo(mateTarget.getPosition()) > sightRange)) {
			mateTarget = null;
		}

		if (mateTarget == null && regionMngr != null) {
			List<Animal> candidates = regionMngr.getAnimalsInRange(this, a -> a.getGeneticCode().equals(this.geneticCode));
			mateTarget = candidates.isEmpty() ? null : mateStrategy.select(this, candidates);
		}

		if (mateTarget == null) {
			// avanza normalmente
			if (dest == null || pos.distanceTo(dest) < Constants.COLLISION_RANGE) {
				if (regionMngr != null) dest = new Vector2D(Utils.RAND.nextDouble() * (regionMngr.getWidth() - 1), Utils.RAND.nextDouble() * (regionMngr.getHeight() - 1));
			}
			move(speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
			age += dt;
			energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_WOLF * dt, 0.0, Constants.MAX_ENERGY);
			desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_WOLF * dt, 0.0, Constants.MAX_DESIRE);
		} else {
			// persigue
			dest = mateTarget.getPosition();
			move(Constants.BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
			age += dt;
			energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_WOLF * Constants.FOOD_DROP_BOOST_FACTOR_WOLF * dt, 0.0, Constants.MAX_ENERGY);
			desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_WOLF * dt, 0.0, Constants.MAX_DESIRE);

			if (pos.distanceTo(mateTarget.getPosition()) < Constants.COLLISION_RANGE) {
				this.desire = 0.0;
				mateTarget.desire = 0.0;
				if (this.baby == null && Utils.RAND.nextDouble() < Constants.PREGNANT_PROBABILITY_WOLF) {
					this.baby = new Wolf(this, mateTarget);
				}
				energy = Utils.constrainValueInRange(energy - 10.0, 0.0, Constants.MAX_ENERGY);
				mateTarget = null;
			}
		}

		if (energy < Constants.FOOD_THRSHOLD_WOLF) {
			setState(State.HUNGER);
		} else if (desire < Constants.DESIRE_THRESHOLD_WOLF) {
			setState(State.NORMAL);
		}
		}

	private void updateHunger(double dt) {
    	 if (huntTarget == null || huntTarget.getState() == State.DEAD || pos.distanceTo(huntTarget.getPosition()) > sightRange) {
             List<Animal> candidatos = regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.HERBIVORE);
             huntTarget = candidatos.isEmpty() ? null : huntingStrategy.select(this, candidatos);
         }

         if (huntTarget != null) {
             dest = huntTarget.getPosition();
             if (pos.distanceTo(huntTarget.getPosition()) < Constants.COLLISION_RANGE) {
                 huntTarget.setState(State.DEAD);
                 huntTarget = null;
                 energy = Utils.constrainValueInRange(energy + Constants.FOOD_EAT_VALUE_WOLF, 0.0, Constants.MAX_ENERGY);
                 // despus de comer decide el siguiente estado
                 if (energy > Constants.FOOD_THRSHOLD_WOLF) {
                     if (desire < Constants.DESIRE_THRESHOLD_WOLF) setState(State.NORMAL);
                     else setState(State.MATE);
                 }
             } else {
                 move(Constants.BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
                 age += dt;
                 energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_WOLF * Constants.FOOD_DROP_BOOST_FACTOR_WOLF * dt, 0.0, Constants.MAX_ENERGY);
                 desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_WOLF * dt, 0.0, Constants.MAX_DESIRE);
             }
         } else {
             // no hay objetivos entonces se mueve de manera aleatoria
             if (dest == null && regionMngr != null) {
                 double dx = Utils.RAND.nextDouble() * (regionMngr.getWidth() - 1);
                 double dy = Utils.RAND.nextDouble() * (regionMngr.getHeight() - 1);
                 dest = new Vector2D(dx, dy);
             }
             move(speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
             age += dt;
             energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_WOLF * dt, 0.0, Constants.MAX_ENERGY);
             desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_WOLF * dt, 0.0, Constants.MAX_DESIRE);
         }
		}

	private void updateNormal(double dt) {
        if (dest == null && regionMngr != null) {
            double dirx = Utils.RAND.nextDouble() * (regionMngr.getWidth() - 1);
            double diry = Utils.RAND.nextDouble() * (regionMngr.getHeight() - 1);
            dest = new Vector2D(dirx, diry);
        }

        move(speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
        age += dt;
        energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_WOLF * dt, 0.0, Constants.MAX_ENERGY);
        desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_WOLF * dt, 0.0, Constants.MAX_DESIRE);

        if (energy < Constants.FOOD_THRSHOLD_WOLF) setState(State.HUNGER);
        else if (desire > Constants.DESIRE_THRESHOLD_WOLF) setState(State.MATE);
    }
        
        
 }   



