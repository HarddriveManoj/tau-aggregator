package com.mgk.tau.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

public final class ExcelUtils {
    private ExcelUtils() {

    }

    public static Object getValue(Cell cell, FormulaEvaluator eval) {
        if(cell == null) {
            return null;
        }
        int cellType = cell.getCellType();
        if(cellType == Cell.CELL_TYPE_BLANK || cellType == Cell.CELL_TYPE_ERROR) {
            return null;
        } else if(cellType == Cell.CELL_TYPE_BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if(cellType == Cell.CELL_TYPE_NUMERIC) {
            if(HSSFDateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                double o = cell.getNumericCellValue();
                if(o == ((long)o)) {
                    return new Long((long) o);

                } else {
                    return o;
                }
            }
        } else  if(cellType == Cell.CELL_TYPE_FORMULA) {
            return eval.evaluate(cell).formatAsString();
        } else if(cellType == Cell.CELL_TYPE_STRING) {
            return StringUtils.trimToNull(cell.getStringCellValue());
        } else {
            throw new IllegalArgumentException("Cannot process this cell: " +  cell);
        }
    }


    public static String getValueAsString(Cell cell, FormulaEvaluator formulaEvaluator) {
        int cellType = Cell.CELL_TYPE_BLANK;
        if(cell != null) {
            cellType = cell.getCellType();
        }

        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_ERROR:
                return "";
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_NUMERIC:
                DataFormatter fmt = new DataFormatter();
                return fmt.formatCellValue(cell);
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            case Cell.CELL_TYPE_FORMULA:
                CellValue cellValue = formulaEvaluator.evaluate(cell);
                String val = cellValue.getStringValue();
                return StringUtils.isBlank(val) ? cellValue.formatAsString() : val;
            default:
                return "";
        }
    }
}
