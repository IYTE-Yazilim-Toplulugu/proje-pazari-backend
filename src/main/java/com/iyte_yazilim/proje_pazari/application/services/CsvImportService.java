package com.iyte_yazilim.proje_pazari.application.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

/** RFC 4180-compliant CSV import helper. */
@Service
@Slf4j
public class CsvImportService {

    public record ImportResult<T>(List<T> successes, List<String> errors, int totalRows) {}

    /**
     * Parses {@code csvContent} using commons-csv. The first record is treated as a header row.
     * Each subsequent record is mapped by {@code rowMapper}; any exception thrown by the mapper is
     * captured as a row-level error string.
     */
    public <T> ImportResult<T> parse(
            String csvContent, Function<CSVRecord, T> rowMapper, String context) {
        List<T> successes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int rowNumber = 0;

        CSVFormat format =
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();

        try (org.apache.commons.csv.CSVParser parser = format.parse(new StringReader(csvContent))) {
            for (CSVRecord record : parser) {
                rowNumber++;
                try {
                    successes.add(rowMapper.apply(record));
                } catch (Exception e) {
                    errors.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to parse {} CSV: {}", context, e.getMessage(), e);
            errors.add("CSV parse error: " + e.getMessage());
        }

        return new ImportResult<>(successes, errors, rowNumber);
    }
}
