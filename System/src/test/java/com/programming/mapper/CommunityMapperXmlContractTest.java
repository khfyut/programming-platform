package com.programming.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityMapperXmlContractTest {

    private static final Path MAPPER_XML = Path.of("src/main/resources/mybatis/CommunityMapper.xml");

    @Test
    void publicPostQueriesReturnAuthorDisplayFields() throws IOException {
        String xml = readMapperXml();

        assertSelectReturnsAuthorFields(xml, "selectPosts");
        assertSelectReturnsAuthorFields(xml, "selectPostById");
        assertSelectReturnsAuthorFields(xml, "selectCommentsByPostId");
    }

    private String readMapperXml() throws IOException {
        return Files.readString(MAPPER_XML, StandardCharsets.UTF_8);
    }

    private void assertSelectReturnsAuthorFields(String xml, String selectId) {
        String statement = extractSelectStatement(xml, selectId).toLowerCase();

        assertTrue(
                statement.contains("join user") || statement.contains("join `user`"),
                selectId + " must join the user table so community pages can display the author"
        );
        assertTrue(
                statement.contains("author_name"),
                selectId + " must expose author_name for frontend authorName mapping"
        );
        assertTrue(
                statement.contains("avatar_url"),
                selectId + " must expose avatar_url for author avatar mapping"
        );
    }

    private String extractSelectStatement(String xml, String selectId) {
        String startMarker = "<select id=\"" + selectId + "\"";
        int start = xml.indexOf(startMarker);
        assertTrue(start >= 0, "Missing select statement: " + selectId);

        int bodyStart = xml.indexOf('>', start);
        int end = xml.indexOf("</select>", bodyStart);
        assertTrue(bodyStart >= 0 && end > bodyStart, "Malformed select statement: " + selectId);

        return xml.substring(bodyStart + 1, end);
    }
}
