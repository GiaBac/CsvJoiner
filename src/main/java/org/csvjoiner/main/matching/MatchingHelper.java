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
			Map<String, String> matchingColumnInput1VsInput2, String mostNearMatchCol) {
		Set<CSVRecord> matchedRecords = new HashSet<CSVRecord>(1);

		MostNearMatch mostNearMatch = null;
		for (CSVRecord csvRecord2 : recordsFileInput2) {
			Optional<MostNearMatch> match = isRecordMatchByMostNear(csvRecordInput1, matchingColumnInput1VsInput2,
					csvRecord2, mostNearMatchCol);

			if (match.isPresent()) {
				MostNearMatch current = match.get();
				if (mostNearMatch == null || current.getDistance() < mostNearMatch.getDistance()) {
					mostNearMatch = current;
				}

			}
		}

		if (mostNearMatch != null) {
			matchedRecords.add(mostNearMatch.getCsvRecord2());
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
			Map<String, String> matchingColumnInput1VsInput2, CSVRecord csvRecordInput2, String mostNearMatchCol) {

		MostNearMatch candidate = null;

		for (Entry<String, String> colToMatch : matchingColumnInput1VsInput2.entrySet()) {

			String input1ColToMatch = colToMatch.getKey();
			String valueFromInput1 = csvRecordInput1.get(input1ColToMatch);
			String valueFromInput2 = csvRecordInput2.get(colToMatch.getValue());

			if (input1ColToMatch.equals(mostNearMatchCol)) {
				String vfi1Clean = valueFromInput1.replaceAll(",", ".");
				String vfi2Clean = valueFromInput2.replaceAll(",", ".");

				if (vfi1Clean.isEmpty() || valueFromInput2.isEmpty()) {
					return Optional.empty();
				}

				double dvalInput1 = Double.parseDouble(vfi1Clean);
				double dvalInput2 = Double.parseDouble(vfi2Clean);
				Double distance = Math.abs(dvalInput1 - dvalInput2);
				candidate = new MostNearMatch(csvRecordInput2, distance);

			} else if (valueFromInput1 == null || !valueFromInput1.equals(valueFromInput2)) {
				// If one exact match fails, the row not match (regardless the most-near match)
				return Optional.empty();
			}
		}

		return (candidate == null) ? Optional.empty() : Optional.of(candidate);
	}
}
