package io.github.yangziwen.webmonitor.repository.base;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.sql2o.Connection;
import org.sql2o.Query;

import io.github.yangziwen.webmonitor.util.ReflectionUtil;

public class BaseRepository<E> extends ReadOnlyBaseRepository<E> {

	public BaseRepository(DataSource dataSource) {
		super(dataSource);
	}

	private static <T> String generateUpdateSql(ModelMapping<T> modelMapping, Map<String, Object> params) {
		String idFieldName = modelMapping.getIdFieldName();
		String idColumnName = modelMapping.getIdColumn();
		Map<String, String> mappingWithoutId = modelMapping.getFieldColumnMappingWithoutIdField();

		StringBuilder updateBuff = new StringBuilder().append(" UPDATE ").append(modelMapping.getTable(params));
		Entry<?, ?>[] entrys = mappingWithoutId.entrySet().toArray(new Entry[]{});
		Entry<?, ?> entry = entrys[0];
		updateBuff.append(" SET ").append(entry.getValue()).append("=:").append(entry.getKey());
		for (int i = 1; i < entrys.length; i++) {
			entry = entrys[i];
			updateBuff.append(", ").append(entry.getValue()).append("=:").append(entry.getKey());
		}
		updateBuff.append(" WHERE ").append(idColumnName).append("=:").append(idFieldName);

		return updateBuff.toString();
	}

	private static <T> String generateInsertSql(ModelMapping<T> modelMapping, Map<String, Object> params) {
		Map<String, String> mappingWithoutId = modelMapping.getFieldColumnMappingWithoutIdField();

		StringBuilder insertBuff = new StringBuilder().append(" INSERT INTO ").append(modelMapping.getTable(params));
		Entry<?, ?>[] entrys = mappingWithoutId.entrySet().toArray(new Entry[]{});
		insertBuff.append(" ( ").append(entrys[0].getValue());
		for (int i = 1; i < entrys.length; i++) {
			insertBuff.append(", ").append(entrys[i].getValue());
		}
		insertBuff.append(" ) VALUES ( :").append(entrys[0].getKey());
		for (int i = 1; i < entrys.length; i++) {
			insertBuff.append(", :").append(entrys[i].getKey());
		}
		insertBuff.append(" ) ");

		return insertBuff.toString();
	}

	private static <T> String generateBatchInsertSql(ModelMapping<T> modelMapping, Map<String, Object> params, int batchSize) {
	    Map<String, String> mappingWithoutId = modelMapping.getFieldColumnMappingWithoutIdField();

	    StringBuilder insertBuff = new StringBuilder().append(" INSERT INTO ").append(modelMapping.getTable(params));
        Entry<?, ?>[] entrys = mappingWithoutId.entrySet().toArray(new Entry[]{});
        insertBuff.append(" ( ").append(entrys[0].getValue());
        for (int i = 1; i < entrys.length; i++) {
            insertBuff.append(", ").append(entrys[i].getValue());
        }
        insertBuff.append(" ) VALUES ( ");
        for (int i = 0; i < batchSize; i++) {
            insertBuff.append(":").append(entrys[0].getKey()).append(Operator.__).append(i);
            for (int j = 1; j < entrys.length; j++) {
                insertBuff.append(", :").append(entrys[j].getKey()).append(Operator.__).append(i);
            }
            insertBuff.append(" ) ");
            if (i < batchSize - 1) {
                insertBuff.append(",");
            }
        }

        return insertBuff.toString();
	}

	private void fillIdValue(E entity, Object id) {
		Field idField = modelMapping.getIdField();
		if (idField == null) {
			return;
		}
		if (idField.getType() == String.class) {
			ReflectionUtil.setFieldValue(entity, idField, id.toString());
		}
		else if (idField.getType() == Integer.class) {
			ReflectionUtil.setFieldValue(entity, idField, Integer.valueOf(id.toString()));
		}
		else if (idField.getType() == Long.class) {
			ReflectionUtil.setFieldValue(entity, idField, Long.valueOf(id.toString()));
		}
	}

	public void insert(E entity) {
		insert(entity, EMPTY_PARAMS);
	}

	public void insert(E entity, Map<String, Object> params) {
		try (Connection conn = sql2o.open()) {
			Query query = conn.createQuery(generateInsertSql(modelMapping, params), true);
			Field idField = modelMapping.getIdField();
			for (Field field : modelMapping.getFields()) {
				if (field == null || idField == field) {
					continue;
				}
				Object value = ReflectionUtil.getFieldValue(entity, field);
				query.addParameter(field.getName(), value);
			}
			Object id = query.executeUpdate().getKey();
			fillIdValue(entity, id);
		}
	}

	public void batchInsert(List<E> entities) {
	    batchInsert(entities, EMPTY_PARAMS);
	}

	public void batchInsert(List<E> entities, Map<String, Object> params) {
	    try (Connection conn = sql2o.open()) {
            Query query = conn.createQuery(generateBatchInsertSql(modelMapping, params, entities.size()), true);
            Field idField = modelMapping.getIdField();
            for (int i = 0; i < entities.size(); i++) {
                E entity = entities.get(i);
                for (Field field : modelMapping.getFields()) {
                    if (field == null || idField == field) {
                        continue;
                    }
                    Object value = ReflectionUtil.getFieldValue(entity, field);
                    query.addParameter(field.getName() + Operator.__ + i, value);
                }
            }
            query.executeUpdate();
        }
	}

	public void update(E entity) {
		update(entity, EMPTY_PARAMS);
	}

	public void update(E entity, Map<String, Object> params) {
		try (Connection conn = sql2o.open()) {
			Query query = conn.createQuery(generateUpdateSql(modelMapping, params));
			for (Field field : modelMapping.getFields()) {
			    Object value = ReflectionUtil.getFieldValue(entity, field);
				query.addParameter(field.getName(), value);
			}
			query.executeUpdate();
		}
	}

	public void delete(E entity) {
		delete(entity, EMPTY_PARAMS);
	}

	public void delete(E entity, Map<String, Object> params) {
		deleteById(ReflectionUtil.<Long>getFieldValue(entity, modelMapping.getIdField()), params);
	}

	public void deleteById(Long id) {
		deleteById(id, EMPTY_PARAMS);
	}

	public void deleteById(Long id, Map<String, Object> params) {
		String sql = "DELETE FROM " + modelMapping.getTable(params) + " WHERE " + modelMapping.getIdColumn() + " = :id";
		try (Connection conn = sql2o.open()) {
			conn.createQuery(sql).addParameter("id", id).executeUpdate();
		}
	}

	public void deleteByParams(Map<String, Object> params) {
		StringBuilder sqlBuff = new StringBuilder("DELETE FROM ")
				.append(modelMapping.getTable(params));
		appendWhere(params, sqlBuff);
		try (Connection conn = sql2o.open()) {
			Query query = conn.createQuery(sqlBuff.toString());
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addParameter(entry.getKey(), entry.getValue());
			}
			query.executeUpdate();
		}

	}

}
