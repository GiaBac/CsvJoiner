package org.csvjoiner.main;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.csvjoiner.main.inputparam.InputParameters;
import org.csvjoiner.main.inputparam.ParserException;
import org.csvjoiner.main.matching.MatchingHelper;

public class Main {

	private static final String INPUT2_HEADER_PREFIX = "I2_";
	private static final String INPUT1_HEADER_PREFIX = "I1_";

	public static void main(String[] args) {
		System.out.println("Starting!\nVersion 1.3");
		InputParameters inputParams;

		try {
			inputParams = InputParameters.parseInputParam(args);
		} catch (ParserException e1) {
			stopProgram(1);
			return;
		}

		final String input1Path = inputParams.getInput1Path();
		final String input2Path = inputParams.getInput2Path();
		final String outputFileName = inputParams.getOutputFileName();
		final Map<String, String> matchinCriteriaInput1VsInput2 = inputParams.getMatchinCriteriaInput1VsInput2();
		final boolean isMostNearMatchingEnabled = inputParams.isMostNearMatchingEnabled();

		System.out.println("Input File 1: " + input1Path);
		System.out.println("Input File 2: " + input2Path);
		System.out.println("Output File: " + outputFileName);
		System.out.println("Matchin Criteria: " + matchinCriteriaInput1VsInput2);
		System.out.println("Is most-near matching enabled: " + isMostNearMatchingEnabled);

		final ParsedFile parsedFileInput1 = parseCSVFile(input1Path, matchinCriteriaInput1VsInput2.keySet());
		final ParsedFile parsedFileInput2 = parseCSVFile(input2Path, matchinCriteriaInput1VsInput2.values());

		Map<CSVRecord, Set<CSVRecord>> matchingMap = new HashMap<CSVRecord, Set<CSVRecord>>();
		Set<CSVRecord> allMatchingRowsInput2 = new HashSet<CSVRecord>();

		for (CSVRecord csvRecord : parsedFileInput1.getRecords()) {
			System.out.println("Matchin row #" + csvRecord.getRecordNumber() + " of file " + input1Path);

			final Set<CSVRecord> matchingRowsInput2AgainstOneRow;
			if (isMostNearMatchingEnabled) {
				matchingRowsInput2AgainstOneRow = MatchingHelper.matchRow_MostNear(csvRecord,
						parsedFileInput2.getRecords(), matchinCriteriaInput1VsInput2,
						inputParams.getInput1ColNameMostNearMatching());
			} else {
				matchingRowsInput2AgainstOneRow = MatchingHelper.matchRow_Equals(csvRecord,
						parsedFileInput2.getRecords(), matchinCriteriaInput1VsInput2);
			}

			System.out.println("Found " + matchingRowsInput2AgainstOneRow.size() + " match");

			allMatchingRowsInput2.addAll(matchingRowsInput2AgainstOneRow);
			matchingMap.put(csvRecord, matchingRowsInput2AgainstOneRow);
		}

		String[] outputHeaders = calculateOutputHeaders(parsedFileInput1.getColumns(), parsedFileInput2.getColumns());

		try {
			writeMatchinRowOnCSVFile(matchingMap, outputHeaders, parsedFileInput1, parsedFileInput2,
					allMatchingRowsInput2, outputFileName);
		} catch (IOException e) {
			System.out
					.println("Something goes wrong when write the output file: " + outputFileName + "\n Details:" + e);
		}

		System.out.println("Complete!");
	}

	private static void writeNotMAtchinRowOfInputOnCSVOutputFile(CSVPrinter csvPrinter, ParsedFile parsedFileInput,
			Set<CSVRecord> allMatchingRowsInput, String[] outputHeaders, String inputHeaderPrefix) throws IOException {

		List<CSVRecord> rowInput2NotMatched = new ArrayList<CSVRecord>();
		rowInput2NotMatched.addAll(parsedFileInput.getRecords());
		rowInput2NotMatched.removeAll(allMatchingRowsInput);

		Set<ArrayList<String>> result = new HashSet<ArrayList<String>>();
		for (CSVRecord csvRecord : rowInput2NotMatched) {
			ArrayList<String> oneRow = new ArrayList<String>();
			for (String header : outputHeaders) {
				final String origHeader;
				final String value;
				if (header.startsWith(inputHeaderPrefix)) {
					origHeader = header.substring(inputHeaderPrefix.length());
					value = csvRecord.get(origHeader);
					oneRow.add(value);
				} else {
					oneRow.add("");
				}
			}
			result.add(oneRow);
		}

		System.out.println("Writing on output file non-matching row of file " + parsedFileInput.getFileName()
				+ ": there are " + result.size() + " non-matching row...");

		for (List<String> outputRow : result) {
			csvPrinter.printRecord(outputRow);
		}
	}

	private static String[] calculateOutputHeaders(Set<String> columnsInput1, Set<String> columnsInput2) {

		ArrayList<String> outputHeader = new ArrayList<String>();

		for (String col : columnsInput1) {
			outputHeader.add(INPUT1_HEADER_PREFIX + col);
		}

		for (String col : columnsInput2) {
			outputHeader.add(INPUT2_HEADER_PREFIX + col);
		}

		return outputHeader.toArray(new String[] {});
	}

	private static void writeMatchinRowOnCSVFile(Map<CSVRecord, Set<CSVRecord>> matchingMap, String[] outputHeaders,
			ParsedFile parsedFileInput1, ParsedFile parsedFileInput2, Set<CSVRecord> allMatchingRowsInput2,
			String outputFileName) throws IOException {

		System.out.println("Writing on output file matching rows...");

		BufferedWriter writer = null;
		CSVPrinter csvPrinter = null;
		try {
			writer = Files.newBufferedWriter(Paths.get(outputFileName));
			csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader(outputHeaders).withDelimiter(';'));

			for (Entry<CSVRecord, Set<CSVRecord>> matchingEntry : matchingMap.entrySet()) {
				Set<List<String>> outputRows = createOutputRows(matchingEntry, outputHeaders);

				for (List<String> outputRow : outputRows) {
					csvPrinter.printRecord(outputRow);
				}
			}

			writeNotMAtchinRowOfInputOnCSVOutputFile(csvPrinter, parsedFileInput2, allMatchingRowsInput2, outputHeaders,
					INPUT2_HEADER_PREFIX);
			Set<CSVRecord> allMatchingRowsInput1 = extractMatchingRowOnlyInput1(matchingMap);
			writeNotMAtchinRowOfInputOnCSVOutputFile(csvPrinter, parsedFileInput1, allMatchingRowsInput1, outputHeaders,
					INPUT1_HEADER_PREFIX);

			csvPrinter.flush();
		} finally {
			if (csvPrinter != null)
				csvPrinter.close();
			if (writer != null)
				writer.close();
		}

		System.out.println("Writing on output file... Complete!");
	}

	private static Set<CSVRecord> extractMatchingRowOnlyInput1(Map<CSVRecord, Set<CSVRecord>> matchingMap) {
		Set<CSVRecord> matchingRowInput1 = new HashSet<CSVRecord>();

		for (CSVRecord csvRecordInput1 : matchingMap.keySet()) {
			Set<CSVRecord> matchingRows = matchingMap.get(csvRecordInput1);
			if (matchingRows != null && matchingRows.size() != 0) {
				matchingRowInput1.add(csvRecordInput1);
			}
		}

		return matchingRowInput1;
	}

	private static Set<List<String>> createOutputRows(Entry<CSVRecord, Set<CSVRecord>> matchingEntry,
			String[] outputHeaders) {
		Set<List<String>> result = new HashSet<List<String>>();

		for (CSVRecord input2Entry : matchingEntry.getValue()) {

			ArrayList<String> oneRow = new ArrayList<String>();
			for (String header : outputHeaders) {

				final String origHeader;
				final String value;
				if (header.startsWith(INPUT1_HEADER_PREFIX)) {
					origHeader = header.substring(INPUT1_HEADER_PREFIX.length());
					value = matchingEntry.getKey().get(origHeader);
				} else {
					origHeader = header.substring(INPUT2_HEADER_PREFIX.length());
					value = input2Entry.get(origHeader);
				}

				oneRow.add(value);
			}

			result.add(oneRow);
		}
		return result;
	}

	private static ParsedFile parseCSVFile(final String filePath, Collection<String> mandatoryColumns) {
		final Reader inputReader;
		try {
			inputReader = new FileReader(filePath);
		} catch (FileNotFoundException e) {
			System.out.println("Cannot open file: " + filePath + " Error reported:  " + e);
			stopProgram(1);
			return null; // unreachable: eclipse don't know that stopProgram, do exit
		}

		final List<CSVRecord> records;
		final Set<String> columns;
		try {
			CSVParser parser = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter(';').parse(inputReader);
			records = parser.getRecords();
			columns = parser.getHeaderMap().keySet();
		} catch (IOException e) {
			System.out.println("Cannot parse file: " + filePath + " Error reported:  " + e);
			stopProgram(1);
			return null; // unreachable: eclipse don't know that stopProgram, do exit
		}

		checkMandatoryColumns(filePath, records, mandatoryColumns);

		return new ParsedFile(filePath, records, columns);
	}

	private static void checkMandatoryColumns(String filePath, List<CSVRecord> records,
			Collection<String> mandatoryColumns) {
		// Check mandatory columns for Input1
		if (records.isEmpty()) {
			System.out.println("File " + filePath + "had no data");
			stopProgram(0);
		}

		CSVRecord record = records.get(0);

		try {
			for (String mandatoryCol : mandatoryColumns) {
				record.get(mandatoryCol);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Error parsing file " + filePath + " missing mandatory column. \n" + e);
			stopProgram(0);
		}

		System.out.println("File " + filePath + " parsed correctly");
	}

	private static void stopProgram(int status) {
		System.out.println("Program will be stopped.");
		System.exit(0);
	}

}
