package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;
import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;

public class Main {
	
	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String tag;
		private String desc;

		private ExecMode(String modeTag, String modeDesc) {
			tag = modeTag;
			desc = modeDesc;
		}

		public String getTag() {
			return tag;
		}

		public String getDesc() {
			return desc;
		}
	}

	// default values for some parameters
	//
	private final static Double DEFAULT_TIME = 10.0; 
	private final static Double DEFAULT_DELTA_TIME = 0.03; 
// in seconds

	// some attributes to stores values corresponding to command-line parameters
	//
	private static Double time = null;
	private static Double deltaTime = null;
	private static String inFile = null;
	private static String outFile = null;
	private static boolean simpleViewer = false;
	private static ExecMode mode = ExecMode.BATCH;
	
	// Factories
	private static Factory<Animal> animalsFactory;
	private static Factory<Region> regionsFactory;
	private static Factory<SelectionStrategy> strategiesFactory;


	private static void parseArgs(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = buildOptions();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parseHelpOption(line, cmdLineOptions);
			parseInFileOption(line);
			parseTimeOption(line);

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			
			parseDeltaTimeOption(line);
			parseOutFileOption(line);
			parseSimpleViewerOption(line);
			
			
			
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options buildOptions() {
		Options cmdLineOptions = new Options();

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("A configuration file.").build());

		// steps (time)
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("A real number representing the total simulation time in seconds. Default value: "
						+ DEFAULT_TIME + ".")
				.build());
		
		// delta time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representing actual time, in seconds, per simulation step. Default value: "
						+ DEFAULT_DELTA_TIME + ".").build());
		
		// output file
		cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());
		
		// simple viewer
		cmdLineOptions.addOption(Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

		return cmdLineOptions;
	}

	private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parseInFileOption(CommandLine line) throws ParseException {
		inFile = line.getOptionValue("i");
		if (mode == ExecMode.BATCH && inFile == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parseTimeOption(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", DEFAULT_TIME.toString());
		try {
			time = Double.parseDouble(t);
			assert (time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}
	
	private static void parseDeltaTimeOption(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", DEFAULT_DELTA_TIME.toString());
		try {
			deltaTime = Double.parseDouble(dt);
			assert (deltaTime > 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for delta-time: " + dt);
		}
	}

	private static void parseOutFileOption(CommandLine line) {
		outFile = line.getOptionValue("o");
	}

	private static void parseSimpleViewerOption(CommandLine line) {
		simpleViewer = line.hasOption("sv");
	}

	private static void initFactories() {
		//  Inicializar la factoria de estrategias
		List<Builder<SelectionStrategy>> strategyBuilders = new ArrayList<>();
		strategyBuilders.add(new SelectFirstBuilder());
		strategyBuilders.add(new SelectClosestBuilder());
		strategyBuilders.add(new SelectYoungestBuilder());
		strategiesFactory = new BuilderBasedFactory<>(strategyBuilders);

		// Inicializar la factoría de animales
		List<Builder<Animal>> animalBuilders = new ArrayList<>();
		animalBuilders.add(new SheepBuilder(strategiesFactory));
		animalBuilders.add(new WolfBuilder(strategiesFactory));
		animalsFactory = new BuilderBasedFactory<>(animalBuilders);

		// Inicializar la factoría de regiones
		List<Builder<Region>> regionBuilders = new ArrayList<>();
		regionBuilders.add(new DefaultRegionBuilder());
		regionBuilders.add(new DynamicSupplyRegionBuilder());
		regionsFactory = new BuilderBasedFactory<>(regionBuilders);
	}

	private static JSONObject loadJSONFile(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}


	private static void startBatchMode() throws Exception {
InputStream is = new FileInputStream(new File(inFile));
		
		// Cargar el JSON usando el método ya existente
		JSONObject jsonInput = loadJSONFile(is);
		is.close(); // Cerramos el stream de entrada
		
		// Determinar el OutputStream (archivo o consola)
		OutputStream os = outFile == null ? System.out : new FileOutputStream(new File(outFile));
		
		// dimensiones del mapa
		int width = jsonInput.getInt("width");
		int height = jsonInput.getInt("height");
		int rows = jsonInput.getInt("rows");
		int cols = jsonInput.getInt("cols");
		
		// Instanciar el Simulator y Controller
		Simulator sim = new Simulator(cols, rows, width, height, animalsFactory, regionsFactory);
		Controller ctrl = new Controller(sim);
		
		// Cargar los datos y ejecutar
		ctrl.loadData(jsonInput);
		ctrl.run(time, deltaTime, simpleViewer, os);
		
		// Cerrar el output si hemos escrito en un archivo
		if (outFile != null) {
			os.close();
		}
	}

	private static void startGUIMode() throws Exception {
		throw new UnsupportedOperationException("GUI mode is not ready yet ...");
	}

	private static void start(String[] args) throws Exception {
		initFactories();
		parseArgs(args);
		switch (mode) {
		case BATCH:
			startBatchMode();
			break;
		case GUI:
			startGUIMode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils.RAND.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}
