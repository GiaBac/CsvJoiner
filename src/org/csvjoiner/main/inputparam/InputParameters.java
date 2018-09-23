package org.csvjoiner.main.inputparam;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class InputParameters {

	private final String input1Path;
	private final String input2Path;
	private final String outputFileName;

	public InputParameters(String inputPath1, String inputPath2, String outputFileName) {
		this.input1Path = inputPath1;
		this.input2Path = inputPath2;

		this.outputFileName = (outputFileName == null) ? "joined.csv" : outputFileName;
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

			return new InputParameters(input1Path, input2Path, outputFileName);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar csvJoiner.jar <inputFile1> <inputFile2>", options);

			throw new ParserException();
		}
	}
}