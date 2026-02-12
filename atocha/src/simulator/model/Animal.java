package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

import org.json.JSONObject;

import simulator.misc.Constants;

public abstract class Animal implements Entity, AnimalInfo{
	  protected String geneticCode;
	    protected Diet diet;
	    protected State state;
	    protected Vector2D pos;
	    protected Vector2D dest;
	    protected double energy;
	    protected double speed;
	    protected double age;
	    protected double desire;
	    protected double sightRange;

	    protected Animal mateTarget;
	    protected Animal baby;

	    protected AnimalMapView regionMngr;
	    protected SelectionStrategy mateStrategy;
		@Override
		public State getState() {
			// TODO Auto-generated method stub
			return state;
		}
		@Override
		public Vector2D getPosition() {
			// TODO Auto-generated method stub
			return pos;
		}
		@Override
		public String getGeneticCode() {
			// TODO Auto-generated method stub
			return geneticCode;
		}
		@Override
		public Diet getDiet() {
			// TODO Auto-generated method stub
			return diet;
		}
		@Override
		public double getSpeed() {
			// TODO Auto-generated method stub
			return speed;
		}
		@Override
		public double getSightRange() {
			// TODO Auto-generated method stub
			return sightRange;
		}
		@Override
		public double getEnergy() {
			// TODO Auto-generated method stub
			return energy;
		}
		@Override
		public double getAge() {
			// TODO Auto-generated method stub
			return age;
		}
		@Override
		public Vector2D getDestination() {
			// TODO Auto-generated method stub
			return dest;
		}
		@Override
		public boolean isPregnant() {
			return baby != null;
		}
		@Override
		public void update(double dt) {
			
			// TODO Auto-generated method stub
			
		}
		
		protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed, SelectionStrategy mateStrategy, Vector2D pos) {
			if (geneticCode == null || geneticCode.isEmpty())
	            throw new IllegalArgumentException("geneticCode no puede estar vacío");

	        if (sightRange <= 0)
	            throw new IllegalArgumentException("sightRange debe ser positivo");

	        if (initSpeed <= 0)
	            throw new IllegalArgumentException("initSpeed debe ser positivo");

	        if (mateStrategy == null)
	            throw new IllegalArgumentException("mateStrategy no puede ser null");

	        // Inicializaciones principales
	        this.geneticCode = geneticCode;
	        this.diet = diet;
	        this.sightRange = sightRange;
	        this.speed = Utils.getRandomizedParameter(initSpeed, 0.1);

	        this.mateStrategy = mateStrategy;
	        this.pos = pos; // puede ser null → se coloca luego en init()

	        // Atributos iniciales según enunciado
	        this.state = State.NORMAL;
	        this.energy = Constants.INIT_ENERGY;  // 100.0
	        this.desire = 0.0;
	        this.age = 0.0;

	        this.dest = null;
	        this.mateTarget = null;
	        this.baby = null;
	        this.regionMngr = null;
	        
		}
		
		protected Animal (Animal p1, Animal p2) {
			 this.dest = null;
		     this.baby = null;
		     this.mateTarget = null;
		     this.regionMngr = null;
		        this.state = State.NORMAL;
		        this.desire = 0.0;
		        this.age = 0.0;

		        // Herencia
		        this.geneticCode = p1.geneticCode;
		        this.diet = p1.diet;
		        this.mateStrategy = p2.mateStrategy;

		    
		        this.pos = p1.getPosition().plus(
		                Vector2D.get_random_vector(-1, 1)
		                        .scale(Constants.NEARBY_FACTOR * (Utils.RAND.nextGaussian() + 1))
		        );

		        
		        this.energy = Math.min(Constants.MAX_ENERGY,
		                (p1.energy + p2.energy) / 2.0);

		        
		        this.sightRange = Utils.getRandomizedParameter(
		                (p1.sightRange + p2.sightRange) / 2.0,
		                Constants.MUTATION_TOLERANCE
		        );

		        this.speed = Utils.getRandomizedParameter(
		                (p1.speed + p2.speed) / 2.0,
		                Constants.MUTATION_TOLERANCE
		        );
		}
		
		 @Override
		    public void init(AnimalMapView regMngr) {
			  this.regionMngr = regMngr;

		        // Si la posición era null → elegir una posición aleatoria dentro del mapa
		        if (pos == null) {
		            double x = Utils.RAND.nextDouble() * (regMngr.getWidth() - 1);
		            double y = Utils.RAND.nextDouble() * (regMngr.getHeight() - 1);
		            pos = new Vector2D(x, y);
		        } else {
		            // Asegurar que cae dentro de los límites del mapa
		            double x = Math.max(0, Math.min(pos.getX(), regMngr.getWidth() - 1));
		            double y = Math.max(0, Math.min(pos.getY(), regMngr.getHeight() - 1));
		            pos = new Vector2D(x, y);
		        }

		        // Elegir un destino aleatorio dentro del mapa
		        double dx = Utils.RAND.nextDouble() * (regMngr.getWidth() - 1);
		        double dy = Utils.RAND.nextDouble() * (regMngr.getHeight() - 1);
		        dest = new Vector2D(dx, dy);
		    }
		 
		 public Animal deliverBaby() {
		        Animal tmp = baby;
		        baby = null;
		        return tmp;
		    }
		 
		 protected void move(double speed) {
		        pos = pos.plus(
		                dest.minus(pos).direction().scale(speed)
		        );
		    }
		 
		 protected void setState(State state) {
		        this.state = state;

		        switch (state) {
		            case NORMAL:
		                setNormalStateAction();
		                break;
		            case HUNGER:
		                setHungerStateAction();
		                break;
		            case MATE:
		                setMateStateAction();
		                break;
		            case DANGER:
		                setDangerStateAction();
		                break;
		            case DEAD:
		                setDeadStateAction();
		                break;
		        }
		    }
		 
		 protected abstract void setNormalStateAction();
		    protected abstract void setMateStateAction();
		    protected abstract void setHungerStateAction();
		    protected abstract void setDangerStateAction();
		    protected abstract void setDeadStateAction();
		    
		    @Override
		    public JSONObject asJSON() {
		        JSONObject jo = new JSONObject();
		        jo.put("pos", new double[]{pos.getX(), pos.getY()});
		        jo.put("gcode", geneticCode);
		        jo.put("diet", diet.toString());
		        jo.put("state", state.toString());
		        return jo;
		    }

}
