package com.overlord.gitstats.analyser.writer;

import com.overlord.gitstats.analyser.model.ReportRow;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportWriterTest {

    @Test
    public void shouldWriteSampleReportFile() {
        String parentDir = "test-output";
        String filename = new ReportWriter(parentDir, "dummy-repo").writeCsv(createDummyReport());
        Assert.assertNotNull(filename);
    }

    private List<ReportRow> createDummyReport() {
        List<ReportRow> dummyReport = new ArrayList<>();
        ReportRow row = ReportRow.Builder.instance()
                .withCommitSHA("abc")
                .withFilePath("def")
                .withNewFnSign("ghi")
                .withOldFnSign("jkl")
                .build();
        dummyReport.add(row);
        row = ReportRow.Builder.instance()
                .withCommitSHA("abc")
                .withFilePath("def")
                .withNewFnSign("ghi")
                .withOldFnSign("jkl")
                .build();
        dummyReport.add(row);
        row = ReportRow.Builder.instance()
                .withCommitSHA("abc")
                .withFilePath("def")
                .withNewFnSign("ghi")
                .withOldFnSign("jkl")
                .build();
        dummyReport.add(row);
        return dummyReport;
    }
}
