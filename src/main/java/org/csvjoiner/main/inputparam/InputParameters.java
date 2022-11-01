package org.csvjoiner.main.inputparam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	private boolean isMostNearMatchingEnabled;
	private String colNameMostNearMatchingEnabled;

	public InputParameters(String inputPath1, String inputPath2, String outputFileName,
			Map<String, String> matchingCriteria, String colNameMostNearMatchingEnabled) {
		this.input1Path = inputPath1;
		this.input2Path = inputPath2;
		this.colNameMostNearMatchingEnabled = colNameMostNearMatchingEnabled;

		this.outputFileName = (outputFileName == null) ? "joined.csv" : outputFileName;
		this.matchingCriteria = (matchingCriteria == null) ? MATCHING_COLUMN_INPUT1_VS_INPUT2_DEFAULT
				: matchingCriteria;
		this.isMostNearMatchingEnabled = (colNameMostNearMatchingEnabled == null) ? false : true;
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

	public String colNameMostNearMatchingEnabled() {
		return colNameMostNearMatchingEnabled;
	}

	public boolean isMostNearMatchingEnabled() {
		return isMostNearMatchingEnabled;
	}

	public static InputParameters parseInputParam(String[] args) throws ParserException {

		Options options = new Options();
		options.addOption("o", true, "the name of the file that will contains the result");
		options.addOption("m", true,
				"the matchin criteria. Use the json format, a sample:  {\\\"col1stFile\\\":\\\"col2ndFile\\\",\\\"anotherCol1stFile\\\":\\\"anotherCol2ndFile\\\"}\n"
						+ "(the \\\" are to escape double quote: pay attention to: 1) put it and 2) no space after the ',')\n"
						+ "in this way the algo matching the row that have: \n"
						+ "column <col1stFile> (from 1st file) against <col2ndFile> (from second file) AND \n"
						+ "column <anotherCol1stFile> against <anotherCol2ndFile>");
		options.addOption("a", true,
				"the name of the column whenre enable most-near approximation for matching criteria (must be a numeric columns)");
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
			final String colNameMostNearMatchingEnabled = cmd.getOptionValue('a');

			return new InputParameters(input1Path, input2Path, outputFileName, matchinCriteria,
					colNameMostNearMatchingEnabled);
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

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> matchinCriteriaMap;
		try {
			matchinCriteriaMap = objectMapper.readValue(inputMatchinCriteria, HashMap.class);

			return matchinCriteriaMap;
		} catch (IOException e) {
			System.out.println("Error reading matching criteria: use the default one. " + e.getMessage());

			return null;
		}
	}
}