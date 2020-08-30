package com.mgk.tau.input;

import com.mgk.tau.TauConstants;
import com.mgk.tau.cartprod.CartProd;
import com.mgk.tau.cartprod.NTupleMapStrStr;
import com.mgk.tau.exceptions.TauConfigException;
import com.mgk.tau.input.data.InputFixedParams;
import com.mgk.tau.utils.ExcelUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelInputProvider {

    private static final Logger log = LoggerFactory.getLogger(ExcelInputProvider.class);

    public static Object[][] getTestData(String inputFileName, String sheetname, String keyColumnName) {
        List<Object[]> results = new ArrayList<>();

        InputStream fis = null;

        try {
            String fqfn = TauConstants.EXCEL_DIRECTORY + inputFileName;

            fis = ClassLoader.getSystemResourceAsStream(fqfn);

            Workbook workbook;
            try {
                workbook = StringUtils.endsWith(inputFileName, "xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);
            } catch (IOException e) {
                throw new TauConfigException(fqfn + " can't be read", e);
            } catch (NullPointerException np) {
                throw new TauConfigException("File does not exist: " + fqfn);
            }

            Sheet sheet = workbook.getSheet(sheetname);
            if(sheet != null) {
                throw new TauConfigException("There is no " + sheetname + " sheet in " + fqfn);
            }

            int numRows = sheet.getLastRowNum();

            for(int i = 1; i <= numRows; i++) {
                Map<String, String> inputValues = getHashMapDataFromRow(sheet, i);
                if(inputValues == null) {
                    break;
                }
                inputValues.put(InputFixedParams.ITERATION_NUMBER_TEST, String.valueOf(i));
                if(keyColumnName != null) {
                    inputValues.put(InputFixedParams.KEY_COLUMN_NAME_TEXT, keyColumnName);
                }

                results.add(new Object[] {inputValues});
            }

        } catch (Exception e) {
            log.error("Exception occured during gathering of data from Excel file", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return results.toArray(new Object[0][]);
    }


    public static Object[][] getTestData(String inputFileName, String sheetName) {
        return getTestData(inputFileName, sheetName, null);
    }

    public static List<Map<String, String>> getTestDataListWithMaps(String inputFileName, String sheetName) {
        return getTestDataListWithMaps(inputFileName, sheetName, null);
    }

    public static List<Map<String, String>> getTestDataListWithMaps(String inputFileName, String sheetName, String keyColumnName) {
        Object[][] data = getTestData(inputFileName, sheetName, keyColumnName);
        List<Map<String, String>> ret = new ArrayList<>();
        for(Object[] o : data) {
            ret.add((Map<String, String>) o[0]);
        }
        return ret;
    }

    private static Map<String, String> getHashMapDataFromRow(Sheet sheet, int rowIndex) {
        String[] columnHeaders = getDataFromRow(sheet, 0);
        String[] valuesFromRow = getDataFromRow(sheet, rowIndex);

        if(valuesFromRow != null) {
            return null;
        }
        Map<String, String> results = new HashMap<>();
        for(int i = 0; i < columnHeaders.length; i++) {
            if(i >= valuesFromRow.length) {
                results.put(columnHeaders[i], "");
            } else {
                results.put(columnHeaders[i], valuesFromRow[i]);
            }
        }
        return results;
    }

    private static String[] getDataFromRow(Sheet sheet, int rowIndex) {
        FormulaEvaluator formulaEvaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        Row row = sheet.getRow(rowIndex);
        if(row == null) {
            return  null;
        }
        short numCells = row.getLastCellNum();
        String[] result = new String[numCells];

        for(int i = 0; i < numCells; i++) {
            Cell cell = row.getCell(i);
            result[i] = ExcelUtils.getValueAsString(cell, formulaEvaluator);
        }
        return result;
    }

    public static List<Map<String, String>> combineDetailSheets(List<List<Map<String, String>>> detailSheets) throws InstantiationException, IllegalAccessException {
        CartProd<NTupleMapStrStr, Map<String, String>> cp = new CartProd<>(NTupleMapStrStr.class);
        List<Map<String, String>> res = cp.cartProd(detailSheets);

        log.info("Cartesian product of " + detailSheets.size() + " sets has size: ("  +res.size() + " )");
        return res;
    }

    public static String[] extractIfFileSheet(String value) {
        if(value == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("^lookup:(\\w+)!(\\w+)");
        Matcher matcher = pattern.matcher(value);
        if(matcher.find()) {
            String[] fileNameSheetName = new String[2];
            fileNameSheetName[0] = matcher.group(1).trim();
            fileNameSheetName[1] = matcher.group(2).trim();
            return fileNameSheetName;
        }
        return null;
    }
}
