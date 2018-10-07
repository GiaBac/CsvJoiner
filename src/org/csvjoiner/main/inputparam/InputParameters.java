package org.csvjoiner.main.inputparam;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class InputParameters {

	private static final Map<String, String> MATCHING_COLUMN_INPUT1_VS_INPUT2_DEFAULT = new HashMap<String, String>();
	static {
		MATCHING_COLUMN_INPUT1_VS_INPUT2_DEFAULT.put("Station", "Station");
		MATCHING_COLUMN_INPUT1_VS_INPUT2_DEFAULT.put("Depth", "Pressure, Digiquartz [db]");
	}

	private final String input1Path;
	private final String input2Path;
	private final String outputFileName;
	private Map<String, String> matchingCriteria;

	public InputParameters(String inputPath1, String inputPath2, String outputFileName,
			Map<String, String> matchingCriteria) {
		this.input1Path = inputPath1;
		this.input2Path = inputPath2;

		this.outputFileName = (outputFileName == null) ? "joined.csv" : outputFileName;
		this.matchingCriteria = (matchingCriteria == null) ? MATCHING_COLUMN_INPUT1_VS_INPUT2_DEFAULT
				: matchingCriteria;
	}

	public String getInput1Path() {
		return input1Path;
	}

	public String getInput2Path() {
		return input2Path;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public Map<String, String> getMatchinCriteriaInput1VsInput2() {
		return matchingCriteria;
	}

	public static InputParameters parseInputParam(String[] args) throws ParserException {

		Options options = new Options();
		options.addOption("o", true, "the name of the file that will contains the result");
		try {

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			if (cmd.getArgs().length != 2) {
				System.out.println("Wrong number of input arguments! You must insert 2 arguments. You insert "
						+ args.length + " instead.");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar csvJoiner.jar <inputFile1> <inputFile2>", options);
				System.out.println(
						"Sample of usage: java -jar csvJoiner.jar fileOriginaleComponenti4E5Prime90MatriciCSV.csv \"ESAW-1-2-d (originale con bottom depth).csv\" fullTestOutput.csv");

				throw new ParserException();
			}

			final String input1Path = cmd.getArgs()[0];
			final String input2Path = cmd.getArgs()[1];
			final String outputFileName = cmd.getOptionValue('o');
			final Map<String, String> matchinCriteria = parseMatchinCriteria(cmd.getOptionValue('m'));

			return new InputParameters(input1Path, input2Path, outputFileName, matchinCriteria);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar csvJoiner.jar <inputFile1> <inputFile2>", options);

			throw new ParserException();
		}
	}

	private static Map<String, String> parseMatchinCriteria(String inputMatchinCriteria) {
		if (inputMatchinCriteria == null || inputMatchinCriteria.isEmpty()) {
			return null;
		}

		return null;

	}
}