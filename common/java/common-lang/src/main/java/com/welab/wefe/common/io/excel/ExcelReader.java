/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.io.excel;

import com.alibaba.fastjson.util.TypeUtils;
import com.welab.wefe.common.util.StringUtil;
import org.apache.poi.ss.usermodel.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
public class ExcelReader implements Closeable {
    private Workbook workbook;

    public ExcelReader(String fileName) throws IOException {
        File file = new File(fileName);
        workbook = WorkbookFactory.create(file);
    }

    public ExcelReader(File file) throws IOException {
        workbook = WorkbookFactory.create(file);
    }

    public ExcelReader(InputStream stream) throws IOException {
        workbook = WorkbookFactory.create(stream);
    }

    /**
     * Get the number of sheets
     */
    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    public Sheet getSheet(int sheetIndex) {
        return workbook.getSheetAt(sheetIndex);
    }

    public Sheet getSheet(String sheetName) {
        return workbook.getSheet(sheetName);
    }

    /**
     * Gets the number of data rows of the specified sheet
     */
    public long getRowCount(int sheetIndex) {
        return workbook.getSheetAt(sheetIndex).getPhysicalNumberOfRows();
    }

    /**
     * Gets the number of columns in the first row of the specified sheet
     */
    public int getColumnCount(int sheetIndex) {
        return workbook.getSheetAt(sheetIndex).getRow(0).getPhysicalNumberOfCells();
    }

    /**
     * Gets the data of the specified row of the specified sheet
     */
    public List<Object> getRowData(int sheetIndex, int rowIndex) {
        Row row = workbook.getSheetAt(sheetIndex).getRow(rowIndex);

        if (row == null) {
            return null;
        }

        List<Object> result = new ArrayList<>();
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {

            Cell cell = row.getCell(i);
            Object value = getCellValue(cell);

            result.add(value);
        }

        return result;
    }

    /**
     * Gets the data of the specified row of the specified sheet
     * empty columns at the end of the row are automatically excluded
     */
    public List<Object> getRowDataWithoutLastEmptyCell(int sheetIndex, int rowIndex) {
        List<Object> list = getRowData(sheetIndex, rowIndex);
        while (true) {

            int index = list.size() - 1;

            Object value = list.get(index);
            if (value == null || StringUtil.isEmpty(String.valueOf(value))) {
                list.remove(index);
            } else {
                break;
            }
        }

        return list;
    }

    /**
     * Get column header
     */
    public List<String> getColumnNames(int sheetIndex) {
        return getRowDataWithoutLastEmptyCell(sheetIndex, 0)
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Traverse sheets with header rows
     */
    public void readSheetWithTitleRow(int sheetIndex, Consumer<List<String>> headRowConsumer, Consumer<Map<String, Object>> consumer) {
        readSheetWithTitleRow(sheetIndex, headRowConsumer, consumer, -1);
    }

    /**
     * Traverse sheets with header rows
     */
    public void readSheetWithTitleRow(int sheetIndex, Consumer<List<String>> headRowConsumer, Consumer<Map<String, Object>> consumer, long maxReadLineCount) {
        List<String> titles = getColumnNames(sheetIndex);

        if (headRowConsumer != null) {
            headRowConsumer.accept(titles);
        }

        for (int y = 1; y < getRowCount(sheetIndex); y++) {

            // Read data row
            List<Object> rowData = getRowData(sheetIndex, y);

            if (rowData == null) {
                break;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            for (int x = 0; x < titles.size(); x++) {

                // Add the default column of the data row as null
                Object value = rowData.size() > x ? rowData.get(x) : null;

                item.put(titles.get(x), value);
            }

            consumer.accept(item);

            // End the traversal after reading the specified number of rows
            if (maxReadLineCount > 0 && y == maxReadLineCount) {
                break;
            }
        }
    }

    /**
     * Traverse sheets without header rows
     */
    public void readSheetWithoutTitleRow(int sheetIndex, Consumer<List<Object>> consumer) {

        for (int i = 0; i < getRowCount(sheetIndex); i++) {
            List<Object> rowData = getRowData(sheetIndex, i);
            consumer.accept(rowData);
        }
    }

    /**
     * Get data in cell
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType();

        switch (type) {
            case FORMULA:
            case NUMERIC:

                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }

                double doubleValue = cell.getNumericCellValue();
                Integer intValue = TypeUtils.castToInt(doubleValue);

                // Try to output integers
                if (doubleValue == intValue) {
                    return intValue;
                } else {
                    return doubleValue;
                }

            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                return cell.getErrorCellValue() + "";
            case STRING:
                return cell.getStringCellValue();
            case BLANK:
                return null;
            default:
                return cell.toString();
        }

    }

    public Workbook getWorkbook() {
        return workbook;
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
