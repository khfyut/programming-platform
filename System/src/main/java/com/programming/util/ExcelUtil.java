package com.programming.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.programming.vo.BatchImportProblemsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExcelUtil {

    public BatchImportProblemsVO parseExcelFile(MultipartFile file) throws Exception {
        List<BatchImportProblemsVO.ProblemItem> problems = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                BatchImportProblemsVO.ProblemItem item = new BatchImportProblemsVO.ProblemItem();
                
                Cell titleCell = row.getCell(0);
                Cell contentCell = row.getCell(1);
                Cell inputCell = row.getCell(2);
                Cell outputCell = row.getCell(3);
                Cell difficultyCell = row.getCell(4);
                Cell languageCell = row.getCell(5);
                Cell timeLimitCell = row.getCell(6);
                Cell memoryLimitCell = row.getCell(7);
                Cell tagsCell = row.getCell(8);
                Cell knowledgePointsCell = row.getCell(9);
                Cell hintsCell = row.getCell(10);
                Cell sampleExplanationCell = row.getCell(11);
                
                item.setTitle(getCellValue(titleCell));
                item.setContent(getCellValue(contentCell));
                item.setInput(getCellValue(inputCell));
                item.setOutput(getCellValue(outputCell));
                item.setDifficulty(getDifficultyValue(difficultyCell));
                item.setLanguage(getCellValue(languageCell));
                item.setTimeLimit(getIntValue(timeLimitCell));
                item.setMemoryLimit(getIntValue(memoryLimitCell));
                item.setTags(getCellValue(tagsCell));
                item.setKnowledgePoints(getCellValue(knowledgePointsCell));
                item.setHints(getCellValue(hintsCell));
                item.setSampleExplanation(getCellValue(sampleExplanationCell));
                
                problems.add(item);
            }
            
            log.info("Excel文件解析成功，共{}道题目", problems.size());
        }
        
        BatchImportProblemsVO vo = new BatchImportProblemsVO();
        vo.setProblems(problems);
        return vo;
    }

    public BatchImportProblemsVO parseCsvFile(MultipartFile file) throws Exception {
        List<BatchImportProblemsVO.ProblemItem> problems = new ArrayList<>();
        
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {
            
            String[] headers = csvReader.readNext();
            String[] nextLine;
            int lineNumber = 0;
            
            while ((nextLine = csvReader.readNext()) != null) {
                lineNumber++;
                
                if (nextLine.length < 11) {
                    log.warn("CSV第{}行数据不完整，跳过", lineNumber);
                    continue;
                }
                
                BatchImportProblemsVO.ProblemItem item = new BatchImportProblemsVO.ProblemItem();
                item.setTitle(nextLine[0]);
                item.setContent(nextLine[1]);
                item.setInput(nextLine[2]);
                item.setOutput(nextLine[3]);
                item.setDifficulty(Integer.parseInt(nextLine[4]));
                item.setLanguage(nextLine[5]);
                
                if (nextLine.length > 6 && !nextLine[6].trim().isEmpty()) {
                    item.setTimeLimit(Integer.parseInt(nextLine[6]));
                }
                if (nextLine.length > 7 && !nextLine[7].trim().isEmpty()) {
                    item.setMemoryLimit(Integer.parseInt(nextLine[7]));
                }
                if (nextLine.length > 8 && !nextLine[8].trim().isEmpty()) {
                    item.setTags(nextLine[8]);
                }
                if (nextLine.length > 9 && !nextLine[9].trim().isEmpty()) {
                    item.setKnowledgePoints(nextLine[9]);
                }
                if (nextLine.length > 10 && !nextLine[10].trim().isEmpty()) {
                    item.setHints(nextLine[10]);
                }
                if (nextLine.length > 11 && !nextLine[11].trim().isEmpty()) {
                    item.setSampleExplanation(nextLine[11]);
                }
                
                problems.add(item);
            }
            
            log.info("CSV文件解析成功，共{}道题目", problems.size());
        }
        
        BatchImportProblemsVO vo = new BatchImportProblemsVO();
        vo.setProblems(problems);
        return vo;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private Integer getDifficultyValue(Cell cell) {
        if (cell == null) {
            return 0;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }

    private Integer getIntValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    String value = cell.getStringCellValue();
                    if (value == null || value.trim().isEmpty()) {
                        return null;
                    }
                    return Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}