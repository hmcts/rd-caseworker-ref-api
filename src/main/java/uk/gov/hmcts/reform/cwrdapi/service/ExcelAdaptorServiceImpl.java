package uk.gov.hmcts.reform.cwrdapi.service;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExcelAdaptorServiceImpl implements ExcelAdaptorService {

    public List<Object> parseExcel(Workbook workbook, Class classType) {
        List<String> headers = new LinkedList<>();
        Sheet sheet = workbook.getSheetAt(1);
        collectHeaderList(headers, sheet);

        // check at least 1 row
        if (sheet.getPhysicalNumberOfRows() < 2) {
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, "Invalid Excel File");
        }

        try {
            return mapToPojo(headers, sheet, classType);
        } catch (Exception e) {
            throw new ExcelValidationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while parsing ");
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
        int headerIndex = 0;
        List<Object> objectList = new ArrayList<>();
        Set<Class> childClassNames= new HashSet<>();
        Map<String, Object> childClassHeaderValues = new HashMap<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next();//skip header
        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Object bean = Class.forName(classType.getName()).getDeclaredConstructor().newInstance();
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                Object cellValue = getCellValue(cell);
                setFields(cellValue, bean, headers ,headerIndex, childClassNames, childClassHeaderValues);
                headerIndex = headerIndex + 1;
            }
            //populateChildObjects(bean, childClassNames, childClassHeaderValues);
            objectList.add(bean);
        }
        return objectList;
    }

    private void populateChildObjects(Object bean,
                           List<Class> childClassNames, Map<String, Object> childClassHeaderValues)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
            try {
                for (Field field : bean.getClass().getDeclaredFields()) {
                    MappingField mappingField = AnnotationUtils.findAnnotation(field, MappingField.class);
                    if (mappingField.columnName().isBlank()) {
                        Class listType = mappingField.clazz();
                        //List<listType> lists = new ArrayList<>();
                        List list = (List)Class.forName("java.util.ArrayList").getDeclaredConstructor().newInstance();
                        Object childBean = Class.forName(mappingField.clazz().getName()).getDeclaredConstructor().newInstance();
                        for (Field childBeanField : childBean.getClass().getDeclaredFields()) {
                            MappingField mappingFieldForChild = AnnotationUtils.findAnnotation(childBeanField, MappingField.class);
                            childClassHeaderValues.forEach((k, v) -> {
                                if(containsIgnoreCase(k, mappingFieldForChild.columnName())){
                                    childBeanField.setAccessible(TRUE);
                                    try {
                                        childBeanField.set(childBean, v);
                                        list.add(childBeanField);
                                    } catch (IllegalAccessException e) {
                                        throw new ExcelValidationException(HttpStatus.BAD_REQUEST, "Invalid Excel File");
                                    }
                                }
                            });

                        }

                        field.setAccessible(TRUE);
                        field.set(bean, list);
                    }
                }
            } catch (IllegalAccessException ex) {
                throw new ExcelValidationException(HttpStatus.BAD_REQUEST, "Invalid Excel File");
            }
        }



    private void setFields(Object cellValue, Object bean, List<String> headers, int headerIndex,
                           Set<Class> childClassNames, Map<String, Object> childClassHeaderValues) {
        Class objectClass = bean.getClass();
        try {
            for (Field field : objectClass.getDeclaredFields()) {
                MappingField mappingField = AnnotationUtils.findAnnotation(field, MappingField.class);
                if (negate(mappingField.columnName().isBlank()) &&
                        headers.get(headerIndex).equals(mappingField.columnName())) {
                        field.setAccessible(TRUE);
                        field.set(bean, cellValue);
                } else {
                    // map set
                    childClassHeaderValues.put(headers.get(headerIndex), cellValue);
                    childClassNames.add(mappingField.clazz());

                }
            }
        } catch (IllegalAccessException ex) {
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, "Invalid Excel File");
        }
    }

    public Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                 return cell.getStringCellValue();
            case NUMERIC:
                 return cell.getNumericCellValue();
            default: return null;
        }
    }
}



