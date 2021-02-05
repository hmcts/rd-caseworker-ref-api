package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.micrometer.core.instrument.util.StringUtils.isNotEmpty;
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
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_VALID_SHEET_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IS_PRIMARY_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUIRED_CW_SHEET_NAME;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUIRED_ROLE_MAPPING_SHEET_NAME;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class ExcelAdaptorServiceImpl implements ExcelAdaptorService {
    @Value("${excel.acceptableHeaders}")
    private List<String> acceptableHeaders;

    FormulaEvaluator evaluator;

    public <T> List<T> parseExcel(Workbook workbook, Class<T> classType) {
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        if (workbook.getNumberOfSheets() < 1) { // check at least 1 sheet present
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_DATA_ERROR_MESSAGE);
        }
        Sheet sheet;
        if (classType.isAssignableFrom(CaseWorkerProfile.class)) {
            sheet = workbook.getSheet(REQUIRED_CW_SHEET_NAME);
        } else {
            sheet = workbook.getSheet(REQUIRED_ROLE_MAPPING_SHEET_NAME);
        }
        if (Objects.isNull(sheet)) {
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_VALID_SHEET_ERROR_MESSAGE);
        } else if (sheet.getPhysicalNumberOfRows() < 2) { // check at least 1 row
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_DATA_ERROR_MESSAGE);
        }
        List<String> headers = new LinkedList<>();
        collectHeaderList(headers, sheet);
        if (classType.equals(uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile.class)) {
            validateHeaders(headers);
        }
        return mapToPojo(sheet, classType);
    }

    private void validateHeaders(List<String> headers) {
        //below mentioned code can be shortened to headers.containsAll(acceptableHeaders),
        //but current code is better from debugging standpoint.
        acceptableHeaders.forEach(acceptableHeader -> {
            if (!headers.contains(acceptableHeader)) {
                log.error(FILE_MISSING_HEADERS);
                throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_MISSING_HEADERS);
            }
        });
    }

    private <T> List<T> mapToPojo(Sheet sheet, Class<T> classType) {
        List<T> objectList = new ArrayList<>();
        Map<String, Object> childHeaderToCellMap = new HashMap<>();
        Map<String, Field> parentFieldMap = new HashMap<>();
        List<String> headers = new LinkedList<>();

        collectHeaderList(headers, sheet);
        //scan parent and domain object fields by reflection and make maps
        List<Triple<String, Field, List<Field>>> customObjectFieldsMapping =
            createBeanFieldMaps(classType, parentFieldMap);
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next();//skip header
        Field rowField = getRowIdField((Class<Object>) classType);
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            //Skipping empty rows
            if (checkIfRowIsEmpty(row)) {
                continue;
            }
            Object bean = getInstanceOf(classType.getName());//create parent object
            setFieldValue(rowField, bean, row.getRowNum());
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
        Object parentBean, List<Triple<String, Field, List<Field>>> customObjectFields,
        Map<String, Object> childHeaderValues) {
        customObjectFields.forEach(customObjectTriple -> {
            Field parentField = customObjectTriple.getMiddle();
            List<Object> domainObjectList = new ArrayList<>();
            int objectCount = findAnnotation(parentField, MappingField.class).objectCount();//take count from parent
            for (int i = 0; i < objectCount; i++) {

                //getInstanceOf(customObjectTriple.getLeft());//instantiate child domain object
                Object childDomainObject = null;
                for (Field childField : customObjectTriple.getRight()) {
                    MappingField mappingField = findAnnotation(childField, MappingField.class);
                    childDomainObject = getChildObject(childHeaderValues, customObjectTriple, i,
                        childDomainObject, childField, mappingField);
                }
                if (nonNull(childDomainObject)) {
                    domainObjectList.add(childDomainObject); //add populated child domain object into list
                }
            }
            setFieldValue(parentField, parentBean, domainObjectList);//finally set list to parent field
        });
    }

    private Object getChildObject(Map<String, Object> childHeaderValues, Triple<String,
        Field, List<Field>> customObjectTriple, int i, Object childDomainObject, Field childField,
                                  MappingField mappingField) {
        if (nonNull(mappingField)) {
            String domainObjectColumnName = mappingField.columnName().split(DELIMITER_COMMA)[i].trim();
            Object fieldValue = childHeaderValues.get(domainObjectColumnName);
            if (nonNull(fieldValue) && isNotEmpty(fieldValue.toString())) {
                childDomainObject = isNull(childDomainObject) ? getInstanceOf(customObjectTriple.getLeft())
                    : childDomainObject;
                setFieldValue(childField, childDomainObject, fieldValue);
                setIsPrimaryField(childDomainObject, mappingField, domainObjectColumnName);
            }

        }
        return childDomainObject;
    }

    //called once per file only
    private <T> List<Triple<String, Field, List<Field>>> createBeanFieldMaps(Class<T> objectClass,
                                                                             Map<String, Field> headerToCellValueMap) {
        List<Triple<String, Field, List<Field>>> customObjects = new ArrayList<>();
        for (Field field : objectClass.getDeclaredFields()) {
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
        if (nonNull(field) && nonNull(value) && isNotEmpty(value.toString())) {
            makeAccessible(field);
            setField(field, bean, value);
        }
    }

    private Object getInstanceOf(String className) {
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
        if (nonNull(mappingField.isPrimary()) && domainObjectColumnName.equals(mappingField.isPrimary())) {
            try {
                Field primaryField = childDomainObject.getClass().getDeclaredField(IS_PRIMARY_FIELD);
                setFieldValue(primaryField, childDomainObject, true);
            } catch (NoSuchFieldException e) {
                throwFileParsingException();
            }
        }
    }

    private Object getCellValue(Cell cell) {
        if (isNull(cell)) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return Integer.valueOf((int) cell.getNumericCellValue());
            case FORMULA:
                return getValueFromFormula(cell);
            default:
                return null;
        }
    }
    //This method has been added for functional test purpose.
    //It should be removed before deploying to production

    private Object getValueFromFormula(Cell cell) {
        switch (evaluator.evaluateFormulaCell(cell)) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                return cell.getStringCellValue();
            default:
                return null;
        }
    }

    private Field getRowIdField(Class<Object> classType) {
        try {
            return classType.getSuperclass().getDeclaredField("rowId");
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("invalid Row exception");
        }
    }

    private void throwFileParsingException() {
        throw new ExcelValidationException(INTERNAL_SERVER_ERROR, ERROR_FILE_PARSING_ERROR_MESSAGE);
    }

    private boolean checkIfRowIsEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            Object cellValue = getCellValue(cell);
            if (nonNull(cellValue) && isNotEmpty(cellValue.toString())) {
                return false;
            }
        }
        return true;
    }

}