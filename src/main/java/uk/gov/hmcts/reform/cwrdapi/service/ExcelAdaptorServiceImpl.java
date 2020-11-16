package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.negate;

@Service
@SuppressWarnings("unchecked")
public class ExcelAdaptorServiceImpl implements ExcelAdaptorService {
    public static String FILE_NO_DATA_ERROR_MESSAGE = "No data in Excel File";
    public static String ERROR_FILE_PARSING_ERROR_MESSAGE = "Error while parsing ";

    public List<Object> parseExcel(Workbook workbook, Class classType) {
        List<String> headers = new LinkedList<>();
        Sheet sheet = workbook.getSheetAt(1);
        collectHeaderList(headers, sheet);

        // check at least 1 row
        if (sheet.getPhysicalNumberOfRows() < 2) {
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_DATA_ERROR_MESSAGE);
        }

        try {
            return mapToPojo(headers, sheet, classType);
        } catch (Exception e) {
            throw new ExcelValidationException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_FILE_PARSING_ERROR_MESSAGE);
        }

    }

    public void collectHeaderList(List<String> headers, Sheet sheet) {
        Row row = sheet.getRow(0);
        Iterator<Cell> headerIterator = row.cellIterator();
        while (headerIterator.hasNext()) {
            Cell cell = headerIterator.next();
            headers.add(cell.getStringCellValue());
        }
    }

    public List<Object> mapToPojo(List<String> headers, Sheet sheet, Class classType) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        List<Object> objectList = new ArrayList<>();
        Map<String, Object> childHeaderValues = new HashMap<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next();//skip header
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Object bean = Class.forName(classType.getName()).getDeclaredConstructor().newInstance();
            Map<String, Field> fieldHashMap = getBeanFields(bean);
            for (int i = 0; i < headers.size(); i++) {
                setFields(getCellValue(row.getCell(i)), bean, headers.get(i), fieldHashMap, childHeaderValues);
            }
            //populateChildObjects(bean, childClassNames, childClassHeaderValues);
            objectList.add(bean);
        }
        return objectList;
    }

    private void setFields(Object cellValue, Object bean, String header, Map<String, Field> fieldHashMap,
                           Map<String, Object> childHeaderValues) throws IllegalAccessException {
        Field field = fieldHashMap.get(header);
        if (nonNull(field)) {
            field.setAccessible(TRUE);
            field.set(bean, cellValue);
            fieldHashMap.remove(header);
        } else {
            childHeaderValues.put(header, cellValue);
        }
    }

    //called once per Row/Object
    private Map<String, Field> getBeanFields(Object bean) {
        Map<String, Field> fieldHashMap = new HashMap<>();
        Class objectClass = bean.getClass();
        Field[] fields = objectClass.getDeclaredFields();
        for (Field field: fields) {
            MappingField mappingField = AnnotationUtils.findAnnotation(field, MappingField.class);
            if (negate(mappingField.columnName().isEmpty())) {
                fieldHashMap.put(mappingField.columnName(), field);
            } else {
                fieldHashMap.put(mappingField.clazz().getCanonicalName(), field);
            }
        }
        return fieldHashMap;
    }

    public Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return Double.valueOf(cell.getNumericCellValue()).intValue();
            default:
                return null;
        }
    }
}



