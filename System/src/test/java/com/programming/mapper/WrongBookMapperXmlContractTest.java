package com.programming.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WrongBookMapperXmlContractTest {

    private static final Path MAPPER_XML = Path.of("src/main/resources/mybatis/WrongBookMapper.xml");

    @Test
    void insertWrongBookBackfillsGeneratedIdForWrongBookItemInsert() throws IOException {
        String xml = Files.readString(MAPPER_XML, StandardCharsets.UTF_8);
        String statementOpenTag = extractStatementOpenTag(xml, "insertWrongBook");

        assertTrue(
                statementOpenTag.contains("useGeneratedKeys=\"true\""),
                "insertWrongBook must enable generated key backfill"
        );
        assertTrue(
                statementOpenTag.contains("keyProperty=\"id\""),
                "insertWrongBook must backfill WrongBook.id before inserting wrong_book_item"
        );
    }

    @Test
    void insertWrongBookItemBackfillsGeneratedIdForReviewPlanInsert() throws IOException {
        String xml = Files.readString(MAPPER_XML, StandardCharsets.UTF_8);
        String statementOpenTag = extractStatementOpenTag(xml, "insertWrongBookItem");

        assertTrue(
                statementOpenTag.contains("useGeneratedKeys=\"true\""),
                "insertWrongBookItem must enable generated key backfill"
        );
        assertTrue(
                statementOpenTag.contains("keyProperty=\"id\""),
                "insertWrongBookItem must backfill WrongBookItem.id before creating review_plan"
        );
    }

    private String extractStatementOpenTag(String xml, String insertId) {
        String startMarker = "<insert id=\"" + insertId + "\"";
        int start = xml.indexOf(startMarker);
        assertTrue(start >= 0, "Missing insert statement: " + insertId);

        int end = xml.indexOf('>', start);
        assertTrue(end > start, "Malformed insert statement: " + insertId);

        return xml.substring(start, end + 1);
    }
}
