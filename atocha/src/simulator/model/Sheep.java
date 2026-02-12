package simulator.model;
import simulator.misc.Vector2D;
import simulator.misc.Constants;
import simulator.misc.Utils;

import java.util.List;

public class Sheep extends Animal{
    private Animal dangerSource;
    private SelectionStrategy dangerStrategy;

    public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos){
        super(Constants.SHEEP_GENETIC_CODE, Diet.HERBIVORE, Constants.INIT_SIGHT_SHEEP, Constants.INIT_SPEED_SHEEP, mateStrategy, pos);

        if (dangerStrategy == null)
            throw new IllegalArgumentException("dangerStrategy cannot be null");

        this.dangerStrategy = dangerStrategy;
        this.dangerSource = null;
    }

    protected Sheep(Sheep p1, Animal p2){
        super(p1,p2);
        this.dangerStrategy = p1.dangerStrategy;
          this.dangerSource = null;
    }

    @Override
    public void update(double dt) {
        if (dt <= 0) throw new IllegalArgumentException("dt must be positive");

        if (state == State.DEAD) return;

        // adjust position if outside map (wrap-around) and reset to NORMAL
        if (regionMngr != null) {
            double x = pos.getX();
            double y = pos.getY();
            double w = regionMngr.getWidth();
            double h = regionMngr.getHeight();
            boolean outside = x < 0 || x >= w || y < 0 || y >= h;
            if (outside) {
                while (x >= w) x -= w;
                while (x < 0) x += w;
                while (y >= h) y -= h;
                while (y < 0) y += h;
                pos = new Vector2D(x, y);
                setState(State.NORMAL);
            }
        }

        // State-specific behaviour
        switch (state) {
            case NORMAL:
                // choose new dest if needed
                if (dest == null && regionMngr != null) {
                    double dx = Utils.RAND.nextDouble() * (regionMngr.getWidth() - 1);
                    double dy = Utils.RAND.nextDouble() * (regionMngr.getHeight() - 1);
                    dest = new Vector2D(dx, dy);
                }

                if (dest != null && pos.distanceTo(dest) < Constants.COLLISION_RANGE && regionMngr != null) {
                    double dx = Utils.RAND.nextDouble() * (regionMngr.getWidth() - 1);
                    double dy = Utils.RAND.nextDouble() * (regionMngr.getHeight() - 1);
                    dest = new Vector2D(dx, dy);
                }

                move(speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
                age += dt;
                energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_SHEEP * dt, 0.0, Constants.MAX_ENERGY);
                desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_SHEEP * dt, 0.0, Constants.MAX_DESIRE);

                // look for danger
                if (dangerSource == null && regionMngr != null) {
                    List<Animal> candidates = regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE);
                    if (!candidates.isEmpty()) dangerSource = dangerStrategy.select(this, candidates);
                }

                if (dangerSource != null) setState(State.DANGER);
                else if (desire > Constants.DESIRE_THRESHOLD_SHEEP) setState(State.MATE);
                break;

            case DANGER:
                if (dangerSource != null && dangerSource.getState() == State.DEAD) dangerSource = null;
                if (dangerSource == null) {
                    // fallback to NORMAL behaviour
                    setState(State.NORMAL);
                    break;
                }

                dest = pos.plus(pos.minus(dangerSource.getPosition()).direction());
                move(2.0 * speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
                age += dt;
                energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_SHEEP * Constants.FOOD_DROP_BOOST_FACTOR_SHEEP * dt, 0.0, Constants.MAX_ENERGY);
                desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_SHEEP * dt, 0.0, Constants.MAX_DESIRE);

                if (regionMngr != null) {
                    double d = pos.distanceTo(dangerSource.getPosition());
                    if (d > sightRange) {
                        List<Animal> candidates = regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE);
                        dangerSource = candidates.isEmpty() ? null : dangerStrategy.select(this, candidates);
                    }
                }

                if (dangerSource == null) {
                    if (desire < Constants.DESIRE_THRESHOLD_SHEEP) setState(State.NORMAL);
                    else setState(State.MATE);
                }
                break;

            case MATE:
                if (mateTarget != null) {
                    if (mateTarget.getState() == State.DEAD) mateTarget = null;
                    else if (pos.distanceTo(mateTarget.getPosition()) > sightRange) mateTarget = null;
                }

                if (mateTarget == null && regionMngr != null) {
                    List<Animal> candidates = regionMngr.getAnimalsInRange(this, a -> a.getGeneticCode().equals(this.geneticCode));
                    mateTarget = candidates.isEmpty() ? null : mateStrategy.select(this, candidates);
                }

                if (mateTarget == null) {
                    // behave as normal
                    setState(State.NORMAL);
                    break;
                }

                dest = mateTarget.getPosition();
                move(2.0 * speed * dt * Math.exp((energy - Constants.INIT_ENERGY) * Constants.HUNGER_DECAY_EXP_FACTOR));
                age += dt;
                energy = Utils.constrainValueInRange(energy - Constants.FOOD_DROP_RATE_SHEEP * Constants.FOOD_DROP_BOOST_FACTOR_SHEEP * dt, 0.0, Constants.MAX_ENERGY);
                desire = Utils.constrainValueInRange(desire + Constants.DESIRE_INCREASE_RATE_SHEEP * dt, 0.0, Constants.MAX_DESIRE);

                if (pos.distanceTo(mateTarget.getPosition()) < Constants.COLLISION_RANGE) {
                    this.desire = 0.0;
                    mateTarget.desire = 0.0;
                    if (this.baby == null && Utils.RAND.nextDouble() < Constants.PREGNANT_PROBABILITY_SHEEP) this.baby = new Sheep(this, mateTarget);
                    mateTarget = null;

                    if (dangerSource == null && regionMngr != null) {
                        List<Animal> candidates = regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE);
                        dangerSource = candidates.isEmpty() ? null : dangerStrategy.select(this, candidates);
                    }

                    if (dangerSource != null) setState(State.DANGER);
                    else if (desire < Constants.DESIRE_THRESHOLD_SHEEP) setState(State.NORMAL);
                }
                break;

            case HUNGER:
            case DEAD:
            default:
                break;
        }

        // death by age/energy
        if (energy <= 0.0 || age > Constants.MAX_AGE_SHEEP) {
            setState(State.DEAD);
            return;
        }

        // get food from region
        if (state != State.DEAD && regionMngr != null) {
            double food = regionMngr.getFood(this, dt);
            energy = Utils.constrainValueInRange(energy + food, 0.0, Constants.MAX_ENERGY);
        }
    }

    @Override
    protected void setNormalStateAction() {
        this.mateTarget = null;
        this.dangerSource = null;
    }

    @Override
    protected void setMateStateAction() {
        this.dangerSource = null;
    }

    @Override
    protected void setHungerStateAction() {
        // Sheep do not use HUNGER, but clear mateTarget for safety
        this.mateTarget = null;
    }

    @Override
    protected void setDangerStateAction() {
        this.mateTarget = null;
    }

    @Override
    protected void setDeadStateAction() {
        this.mateTarget = null;
        this.dangerSource = null;
    }   
}
