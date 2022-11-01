package org.csvjoiner.main.matching;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;

public class MatchingHelper {

	public static Set<CSVRecord> matchRow_Equals(CSVRecord csvRecordInput1, Iterable<CSVRecord> recordsFileInput2,
			Map<String, String> matchingColumnInput1VsInput2) {

		Set<CSVRecord> matchedRecords = new HashSet<CSVRecord>();

		for (CSVRecord csvRecord2 : recordsFileInput2) {

			boolean isMatch = isRecordMatchByEquals(csvRecordInput1, matchingColumnInput1VsInput2, csvRecord2);

			if (isMatch) {
				matchedRecords.add(csvRecord2);
			}
		}

		return matchedRecords;
	}

	public static Set<CSVRecord> matchRow_MostNear(CSVRecord csvRecordInput1, List<CSVRecord> recordsFileInput2,
			Map<String, String> matchingColumnInput1VsInput2) {
		Set<CSVRecord> matchedRecords = new HashSet<CSVRecord>();

		for (CSVRecord csvRecord2 : recordsFileInput2) {

			Optional<MostNearMatch> match = isRecordMatchByMostNear(csvRecordInput1, matchingColumnInput1VsInput2,
					csvRecord2);

			if (match.isPresent()) {
				if())
				matchedRecords.add(csvRecord2);
			}
		}

		return matchedRecords;
	}

	private static boolean isRecordMatchByEquals(CSVRecord csvRecordInput1,
			Map<String, String> matchingColumnInput1VsInput2, CSVRecord csvRecordInput2) {
		for (Entry<String, String> colToMatch : matchingColumnInput1VsInput2.entrySet()) {

			String valueFromInput1 = csvRecordInput1.get(colToMatch.getKey());
			String valueFromInput2 = csvRecordInput2.get(colToMatch.getValue());

			if (valueFromInput1 == null || !valueFromInput1.equals(valueFromInput2)) {
				return false;
			}
		}

		return true;
	}

	private static Optional<MostNearMatch> isRecordMatchByMostNear(CSVRecord csvRecordInput1,
			Map<String, String> matchingColumnInput1VsInput2, CSVRecord csvRecordInput2) {

		MostNearMatch candidate = null;

		for (Entry<String, String> colToMatch : matchingColumnInput1VsInput2.entrySet()) {

			String valueFromInput1 = csvRecordInput1.get(colToMatch.getKey());
			String valueFromInput2 = csvRecordInput2.get(colToMatch.getValue());

			ParsePosition pos1 = new ParsePosition(0);
			Number numberFromInput1 = NumberFormat.getInstance().parse(valueFromInput1, pos1);
			ParsePosition pos2 = new ParsePosition(0);
			Number numberFromInput2 = NumberFormat.getInstance().parse(valueFromInput2, pos2);

			if (valueFromInput1.length() == pos1.getIndex() && valueFromInput2.length() == pos2.getIndex()) {
				Double distance = Math.abs(numberFromInput1.doubleValue() - numberFromInput2.doubleValue());
				candidate = new MostNearMatch(csvRecordInput2, colToMatch.getValue(), distance);
			}

			if (valueFromInput1 == null || !valueFromInput1.equals(valueFromInput2)) {
				// If exact match fails, the row not match (regardless the most-near match)
				return Optional.empty();
			}
		}

		return Optional.of(new MostNearMatch(csvRecordInput2));
	}
}
