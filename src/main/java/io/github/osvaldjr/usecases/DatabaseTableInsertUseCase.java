package io.github.osvaldjr.usecases;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang.math.NumberUtils.isNumber;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

@Component
public class DatabaseTableInsertUseCase {

  @PersistenceContext(unitName = "easyCucumberEntityManagerFactory")
  EntityManager entityManager;

  @Transactional
  public void execute(String tableName, List<TreeMap> lines) {
    String columns = getColumns(lines.get(0));
    String values = getInsertValues(lines);
    String query = format("INSERT INTO %s (%s) VALUES %s;", tableName, columns, values);
    entityManager.createNativeQuery(query).executeUpdate();
  }

  private String getInsertValues(List<TreeMap> lines) {
    Stream<String> lineValues = lines.stream().map(line -> getSingleLineValues(line.values()));
    return lineValues.map(line -> format("(%s)", line)).collect(joining(","));
  }

  private String getSingleLineValues(Collection<Object> values) {
    return values.stream().map(this::getSingleColumnValue).collect(joining(","));
  }

  private String getSingleColumnValue(Object value) {
    return isNumber(value.toString()) ? value.toString() : format("'%s'", value);
  }

  private String getColumns(Map<String, Object> map) {
    return map.keySet().stream().map(column -> format("\"%s\"", column)).collect(joining(","));
  }
}
