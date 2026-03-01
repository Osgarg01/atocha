package simulator.misc;

public final class Constants {
	// Evita que alguien instancie la clase
    private Constants() {}

    // se usan en Animal y subclases
    public static final double INIT_ENERGY = 100.0;
    public static final double MUTATION_TOLERANCE = 0.2;
    public static final double NEARBY_FACTOR = 60.0;
    public static final double COLLISION_RANGE = 8;
    public static final double HUNGER_DECAY_EXP_FACTOR = 0.007;
    public static final double MAX_ENERGY = 100.0;
    public static final double MAX_DESIRE = 100.0;

    // Sheep
    public static final String SHEEP_GENETIC_CODE = "Sheep";
    public static final double INIT_SIGHT_SHEEP = 40;
    public static final double INIT_SPEED_SHEEP = 35;
    public static final double BOOST_FACTOR_SHEEP = 2.0;
    public static final double MAX_AGE_SHEEP = 8;
    public static final double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
    public static final double FOOD_DROP_RATE_SHEEP = 20.0;
    public static final double DESIRE_THRESHOLD_SHEEP = 65.0;
    public static final double DESIRE_INCREASE_RATE_SHEEP = 40.0;
    public static final double PREGNANT_PROBABILITY_SHEEP = 0.9;

    // Wolf
    public static final String WOLF_GENETIC_CODE = "Wolf";
    public static final double INIT_SIGHT_WOLF = 50;
    public static final double INIT_SPEED_WOLF = 60;
    public static final double BOOST_FACTOR_WOLF = 3.0;
    public static final double MAX_AGE_WOLF = 14.0;
    public static final double FOOD_THRSHOLD_WOLF = 50.0;
    public static final double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
    public static final double FOOD_DROP_RATE_WOLF = 18.0;
    public static final double FOOD_DROP_DESIRE_WOLF = 10.0;
    public static final double FOOD_EAT_VALUE_WOLF = 50.0;
    public static final double DESIRE_THRESHOLD_WOLF = 65.0;
    public static final double DESIRE_INCREASE_RATE_WOLF = 30.0;
    public static final double PREGNANT_PROBABILITY_WOLF = 0.75;

    // DefaultRegion
    public static final double FOOD_EAT_RATE_HERBS = 60.0;
    public static final double FOOD_SHORTAGE_TH_HERBS = 5.0;
    public static final double FOOD_SHORTAGE_EXP_HERBS = 2.0;

    // DynamicSupplyRegion
    public static final double INIT_FOOD = 100.0;
    public static final double FACTOR = 2.0;
}
