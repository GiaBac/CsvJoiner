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

public class Main {

	private static final String INPUT2_HEADER_PREFIX = "I2_";
	private static final String INPUT1_HEADER_PREFIX = "I1_";
	private static final Map<String, String> MATCHING_COLUMN_INPUT1_VS_INPUT2 = new HashMap<String, String>();
	static {
		MATCHING_COLUMN_INPUT1_VS_INPUT2.put("Station", "Station");
		MATCHING_COLUMN_INPUT1_VS_INPUT2.put("Depth", "Pressure, Digiquartz [db]");
	}

	public static void main(String[] args) {
		System.out.println("Starting!");

		if (args.length != 3) {
			System.out.println("Wrong number of input arguments! You must insert 3 arguments. You insert " + args.length
					+ " instead.");
			System.out.println("Sintax: java -jar csvJoiner.jar <inputFile1> <inputFile2> <outputFile>");
			System.out.println(
					"Sample of usage: java -jar csvJoiner.jar fileOriginaleComponenti4E5Prime90MatriciCSV.csv \"ESAW-1-2-d (originale con bottom depth).csv\" fullTestOutput.csv");

			stopProgram(1);
		}

		final String input1Path = args[0];
		final String input2Path = args[1];
		final String outputFileName = args[2];

		System.out.println("Input File 1: " + input1Path);
		System.out.println("Input File 2: " + input2Path);
		System.out.println("Output File: " + outputFileName);
		System.out.println("Matchin Criteria: " + MATCHING_COLUMN_INPUT1_VS_INPUT2);

		final ParsedFile parsedFileInput1 = parseCSVFile(input1Path, MATCHING_COLUMN_INPUT1_VS_INPUT2.keySet());
		final ParsedFile parsedFileInput2 = parseCSVFile(input2Path, MATCHING_COLUMN_INPUT1_VS_INPUT2.values());

		Map<CSVRecord, Set<CSVRecord>> matchingMap = new HashMap<CSVRecord, Set<CSVRecord>>();
		Set<CSVRecord> allMatchingRowsInput2 = new HashSet<CSVRecord>();

		for (CSVRecord csvRecord : parsedFileInput1.getRecords()) {
			System.out.println("Matchin row #" + csvRecord.getRecordNumber() + " of file " + input1Path);

			Set<CSVRecord> matchingRowsInput2AgainstOneRow = matchRow(csvRecord, parsedFileInput2.getRecords(),
					MATCHING_COLUMN_INPUT1_VS_INPUT2);

			System.out.println("Found " + matchingRowsInput2AgainstOneRow.size() + " match");

			allMatchingRowsInput2.addAll(matchingRowsInput2AgainstOneRow);
			matchingMap.put(csvRecord, matchingRowsInput2AgainstOneRow);
		}

		String[] outputHeaders = calculateOutputHeaders(parsedFileInput1.getColumns(), parsedFileInput2.getColumns());

		try {
			writeMatchinRowOnCSVFile(matchingMap, outputHeaders, parsedFileInput2, allMatchingRowsInput2,
					outputFileName);
		} catch (IOException e) {
			System.out
					.println("Something goes wrong when write the output file: " + outputFileName + "\n Details:" + e);
		}

		System.out.println("Complete!");
	}

	private static void writeNotMAtchinRowOfInput2OnCSVOutputFile(CSVPrinter csvPrinter, ParsedFile parsedFileInput2,
			Set<CSVRecord> allMatchingRowsInput2, String[] outputHeaders) throws IOException {

		List<CSVRecord> rowInput2NotMatched = new ArrayList<CSVRecord>();
		rowInput2NotMatched.addAll(parsedFileInput2.getRecords());
		rowInput2NotMatched.removeAll(allMatchingRowsInput2);

		Set<ArrayList<String>> result = new HashSet<ArrayList<String>>();
		for (CSVRecord csvRecord : rowInput2NotMatched) {
			ArrayList<String> oneRow = new ArrayList<String>();
			for (String header : outputHeaders) {
				final String origHeader;
				final String value;
				if (header.startsWith(INPUT2_HEADER_PREFIX)) {
					origHeader = header.substring(INPUT2_HEADER_PREFIX.length());
					value = csvRecord.get(origHeader);
					oneRow.add(value);
				} else {
					oneRow.add("");
				}
			}
			result.add(oneRow);
		}

		System.out.println("Writing on output file non-matching row of file input2: there are " + result.size()
				+ " non-matching row of file input2...");

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
			ParsedFile parsedFileInput2, Set<CSVRecord> allMatchingRowsInput2, String outputFileName)
			throws IOException {

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

			writeNotMAtchinRowOfInput2OnCSVOutputFile(csvPrinter, parsedFileInput2, allMatchingRowsInput2,
					outputHeaders);

			csvPrinter.flush();
		} finally {
			if (csvPrinter != null)
				csvPrinter.close();
			if (writer != null)
				writer.close();
		}

		System.out.println("Writing on output file... Complete!");
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

	private static Set<CSVRecord> matchRow(CSVRecord csvRecordInput1, Iterable<CSVRecord> recordsFileInput2,
			Map<String, String> matchingColumnInput1VsInput2) {

		Set<CSVRecord> matchedRecords = new HashSet<CSVRecord>();

		for (CSVRecord csvRecord2 : recordsFileInput2) {

			boolean isMatch = isRecordMatch(csvRecordInput1, matchingColumnInput1VsInput2, csvRecord2);

			if (isMatch) {
				matchedRecords.add(csvRecord2);
			}
		}

		return matchedRecords;
	}

	private static boolean isRecordMatch(CSVRecord csvRecordInput1, Map<String, String> matchingColumnInput1VsInput2,
			CSVRecord csvRecordInput2) {
		for (Entry<String, String> colToMatch : matchingColumnInput1VsInput2.entrySet()) {

			String valueFromInput1 = csvRecordInput1.get(colToMatch.getKey());
			String valueFromInput2 = csvRecordInput2.get(colToMatch.getValue());

			if (valueFromInput1 == null || !valueFromInput1.equals(valueFromInput2)) {
				return false;
			}
		}

		return true;
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

		return new ParsedFile(records, columns);
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
