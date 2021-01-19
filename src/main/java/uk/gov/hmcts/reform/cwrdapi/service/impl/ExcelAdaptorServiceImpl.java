package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DELIMITER_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ERROR_FILE_PARSING_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_MISSING_HEADERS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_MISSING_HEADER_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_VALID_SHEET_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IS_PRIMARY_FIELD;

@Service
@SuppressWarnings("unchecked")
@Slf4j
public class ExcelAdaptorServiceImpl implements ExcelAdaptorService {

    @Value("${excel.acceptableHeaders}")
    private List<String> acceptableHeaders;

    public <T> List<T> parseExcel(Workbook workbook, Class<T> classType) {
        if (workbook.getNumberOfSheets() < 1) { // check at least 1 sheet present
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_DATA_ERROR_MESSAGE);
        }
        Sheet sheet = workbook.getSheet(CaseWorkerConstants.REQUIRED_SHEET_NAME);
        if (null == sheet) {
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_VALID_SHEET_ERROR_MESSAGE);
        } else if (sheet.getPhysicalNumberOfRows() < 2) { // check at least 1 row
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_DATA_ERROR_MESSAGE);
        }
        List<String> headers = new LinkedList<>();
        collectHeaderList(headers, sheet);
        validateHeaders(headers);
        return mapToPojo(sheet, classType, headers);
    }

    private void validateHeaders(List<String> headers) {
        //below mentioned code can be shortened to headers.containsAll(acceptableHeaders),
        //but current code is better from debugging standpoint.
        acceptableHeaders.forEach(acceptableHeader -> {
            if (!headers.contains(acceptableHeader)) {
                log.warn(String.format(FILE_MISSING_HEADER_NAME, acceptableHeader));
                throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_MISSING_HEADERS);
            }
        });
    }

    private <T> List<T> mapToPojo(Sheet sheet, Class<T> classType, List<String> headers) {
        List<T> objectList = new ArrayList<>();
        Map<String, Object> childHeaderToCellMap = new HashMap<>();
        Map<String, Field> parentFieldMap = new HashMap<>();

        //scan parent and domain object fields by reflection and make maps
        List<Triple<String,Field, List<Field>>> customObjectFieldsMapping =
                createBeanFieldMaps(classType, parentFieldMap);
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next();//skip header
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Object bean = getInstanceOf(classType.getName());//create parent object
            for (int i = 0; i < headers.size(); i++) { //set all parent fields
                setParentFields(getCellValue(row.getCell(i)), bean, headers.get(i), parentFieldMap,
                        childHeaderToCellMap);
            }
            populateChildDomainObjects(bean, customObjectFieldsMapping, childHeaderToCellMap);
            objectList.add((T) bean);
        }
        return objectList;
    }

    private void populateChildDomainObjects(
            Object parentBean, List<Triple<String,Field, List<Field>>> customObjectFields,
            Map<String, Object> childHeaderValues) {
        customObjectFields.forEach(customObjectTriple -> {
            Field parentField = customObjectTriple.getMiddle();
            List<Object> domainObjectList = new ArrayList<>();
            int objectCount = findAnnotation(parentField, MappingField.class).objectCount();//take count from parent
            for (int i = 0; i < objectCount; i++) {
                Object childDomainObject = getInstanceOf(customObjectTriple.getLeft());//instantiate child domain object
                for (Field childField: customObjectTriple.getRight()) {
                    MappingField mappingField = findAnnotation(childField, MappingField.class);
                    if (nonNull(mappingField)) {
                        String domainObjectColumnName = mappingField.columnName().split(DELIMITER_COMMA)[i].trim();
                        setFieldValue(childField, childDomainObject, childHeaderValues.get(domainObjectColumnName));
                        setIsPrimaryField(childDomainObject, mappingField, domainObjectColumnName);
                    }
                }
                domainObjectList.add(childDomainObject);//add populated child domain object into list
            }
            setFieldValue(parentField, parentBean, domainObjectList);//finally set list to parent field
        });
    }

    //called once per file only
    private <T> List<Triple<String,Field, List<Field>>> createBeanFieldMaps(Class<T> objectClass,
                                                                  Map<String, Field> headerToCellValueMap) {
        List<Triple<String,Field, List<Field>>> customObjects = new ArrayList<>();
        for (Field field: objectClass.getDeclaredFields()) {
            MappingField mappingField = findAnnotation(field, MappingField.class);
            if (isNull(mappingField)) {
                // do nothing
            } else if (!(mappingField.columnName().isEmpty())) {
                headerToCellValueMap.put(mappingField.columnName(), field);
            } else {
                // make triple of child domain object class name, parent field, respective list of domain object fields
                customObjects.add(Triple.of(mappingField.clazz().getCanonicalName(), field,
                        asList(mappingField.clazz().getDeclaredFields())));
            }
        }
        return customObjects;
    }

    private void collectHeaderList(List<String> headers, Sheet sheet) {
        Row row = sheet.getRow(0);
        Iterator<Cell> headerIterator = row.cellIterator();
        while (headerIterator.hasNext()) {
            Cell cell = headerIterator.next();
            headers.add(cell.getStringCellValue().trim());
        }
    }

    private void setFieldValue(Field field, Object bean, Object value) {
        if (nonNull(field) && nonNull(value)) {
            makeAccessible(field);
            setField(field, bean, value);
        }
    }

    private Object getInstanceOf(String className)  {
        Object objectInstance = null;
        try {
            objectInstance = Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throwFileParsingException();
        }
        return objectInstance;
    }

    private void setParentFields(Object cellValue, Object bean, String header, Map<String, Field> fieldHashMap,
                           Map<String, Object> childHeaderValues) {
        Field field = fieldHashMap.get(header);
        if (nonNull(field)) {
            setFieldValue(field, bean, cellValue);
        } else {
            childHeaderValues.put(header, cellValue);
        }
    }

    private void setIsPrimaryField(Object childDomainObject, MappingField mappingField, String domainObjectColumnName) {
        if (StringUtils.isNotEmpty(mappingField.isPrimary()) && domainObjectColumnName.equals(mappingField.isPrimary())) {
            try {
                Field primaryField = childDomainObject.getClass().getDeclaredField(IS_PRIMARY_FIELD);
                setFieldValue(primaryField, childDomainObject, true);
            } catch (NoSuchFieldException e) {
                throwFileParsingException();
            }
        }
    }

    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return Integer.valueOf((int)cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private void throwFileParsingException() {
        throw new ExcelValidationException(INTERNAL_SERVER_ERROR, ERROR_FILE_PARSING_ERROR_MESSAGE);
    }
}