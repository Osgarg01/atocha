package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

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
import simulator.view.MainWindow;

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
	public static Double deltaTime = null;
	private static String inFile = null;
	private static String outFile = null;
	private static boolean simpleViewer = false;
	private static ExecMode mode = ExecMode.GUI;
	
	// Factories
	public static Factory<Animal> animalsFactory;
	public static Factory<Region> regionsFactory;
	public static Factory<SelectionStrategy> strategiesFactory;


	private static void parseArgs(String[] args) throws ParseException {

		// define the valid command line options
		//
		Options cmdLineOptions = buildOptions();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(cmdLineOptions, args);
		parseHelpOption(line, cmdLineOptions);
		parseModeOption(line);
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
	}

	private static Options buildOptions() {
		Options cmdLineOptions = new Options();

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		//m
		
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg()
				.desc("Execution Mode. Possible values: 'batch' (Batch mode), 'gui' (Graphical User Interface mode). Default value: 'gui'.")
				.build());
		
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
	// NUEVO M�TODO PARA LEER EL MODO
		private static void parseModeOption(CommandLine line) throws ParseException {
			String m = line.getOptionValue("m", "gui"); // "gui" por defecto
			if (m.equalsIgnoreCase("batch")) {
				mode = ExecMode.BATCH;
			} else if (m.equalsIgnoreCase("gui")) {
				mode = ExecMode.GUI;
			} else {
				throw new ParseException("Invalid value for mode: " + m);
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

	private static void parseOutFileOption(CommandLine line) throws ParseException {
		outFile = line.getOptionValue("o");
		if (mode == ExecMode.BATCH && outFile == null) {
			throw new ParseException("In batch mode an output file is required");
		}
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

		// Inicializar la factor�a de animales
		List<Builder<Animal>> animalBuilders = new ArrayList<>();
		animalBuilders.add(new SheepBuilder(strategiesFactory));
		animalBuilders.add(new WolfBuilder(strategiesFactory));
		animalsFactory = new BuilderBasedFactory<>(animalBuilders);

		// Inicializar la factor�a de regiones
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
		
		// Cargar el JSON usando el m�todo ya existente
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

	// NUEVO M�TODO PARA ARRANCAR LA GUI
		private static void startGUIMode() throws Exception {
			Simulator sim;
			Controller ctrl;

			if (inFile != null) {
				// Si el usuario pasa un fichero, lo cargamos
				InputStream is = new FileInputStream(new File(inFile));
				JSONObject jsonInput = loadJSONFile(is);
				is.close();
				
				int width = jsonInput.getInt("width");
				int height = jsonInput.getInt("height");
				int rows = jsonInput.getInt("rows");
				int cols = jsonInput.getInt("cols");
				
				sim = new Simulator(cols, rows, width, height, animalsFactory, regionsFactory);
				ctrl = new Controller(sim);
				ctrl.loadData(jsonInput);
			} else {
				// Si no pasa fichero, usamos valores por defecto (20 cols, 15 rows, 800 width, 600 height)
				sim = new Simulator(20, 15, 800, 600, animalsFactory, regionsFactory);
				ctrl = new Controller(sim);
			}

			// Arrancar la ventana gr�fica principal de forma segura
			SwingUtilities.invokeAndWait(() -> new MainWindow(ctrl));
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
			if (mode == ExecMode.GUI) {
				javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
			System.exit(1);
		}
	}
}
