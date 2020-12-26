package com.overlord.gitstats.analyser.writer;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.overlord.gitstats.analyser.model.ReportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportWriter {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final String parentDir;
    private final String repoName;

    public ReportWriter(String parentDir, String repoName) {
        this.parentDir = parentDir;
        this.repoName = repoName;
    }

    public String writeCsv(List<ReportRow> reportRow) {
        File parent = new File(this.parentDir);
        if(!parent.exists())
            parent.mkdir();

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(ReportRow.class)
                .withHeader()
                .withQuoteChar('"');
        String filename = getFilename();
        try {
            mapper.writer(schema).writeValue(new File(parentDir + '/' + filename), reportRow);
        } catch (IOException e) {
            LOGGER.error("Error while writing report for repo {}", repoName, e);
            return null;
        }

        return filename;
    }

    private String getFilename() {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String validCharsRepoName = this.repoName.replaceAll("[\\\\/:*\"<>|]", "");
        return "report_" + validCharsRepoName + '_' + datetime + ".csv";
    }
}
