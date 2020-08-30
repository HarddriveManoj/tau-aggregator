package com.mgk.tau.input;

import com.mgk.tau.TauConstants;
import com.mgk.tau.exceptions.TauConfigException;
import com.mgk.tau.input.data.GroupInfo;
import com.mgk.tau.input.data.TestCaseGroupedData;
import com.mgk.tau.utils.Validation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class ExcelGroupedInputProvider {

    private static Logger log = LoggerFactory.getLogger(ExcelGroupedInputProvider.class);

    public static LinkedHashMap<String, TestCaseGroupedData> getTestCaseData(String excelFile, String sheetName, int startRow, int startCol, String testCaseIdGroup, String testCaseIdCol) throws Exception {
        String path = TauConstants.EXCEL_DIRECTORY + excelFile;

        URL location = ClassLoader.getSystemResource(path);

        if(location == null) {
            throw new TauConfigException(path + " doesn't exist in " + System.getProperty("java.class.path"));
        }

        File excel = new File(location.getFile());
        Validation.assertTrueData(excel.exists(), String.format("File does not exist: %s", excel.getPath()));

        LinkedHashMap<String, TestCaseGroupedData> res = new LinkedHashMap<>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(excel);;
            Workbook workbook = StringUtils.endsWith(excel.getPath(), "xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(sheetName);
            FormulaEvaluator eval = workbook.getCreationHelper().createFormulaEvaluator();
            Validation.assertTrueData(sheet != null, String.format("Excel sheet not present %s", sheetName));
            List<GroupInfo> groupInfo = getGroupInfo(sheet, startRow, startCol);
            for(int i = startRow + 2; i<= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                TestCaseGroupedData processedRow = processRow(row, i, groupInfo, startCol, testCaseIdGroup, testCaseIdCol, eval);
                if(processedRow == null) {
                    continue;
                }
                String testCaseId = processedRow.getTestCaseId();
                if(res.containsKey(testCaseId)) {
                    addTo(res.get(testCaseId), processedRow);
                } else {
                    res.put(testCaseId, processedRow);
                }

            }

        } finally {
            IOUtils.closeQuietly(fis);
        }
        return res;
        }
    }

}
