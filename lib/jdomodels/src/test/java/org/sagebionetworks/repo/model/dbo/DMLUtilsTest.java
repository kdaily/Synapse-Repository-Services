package org.sagebionetworks.repo.model.dbo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class DMLUtilsTest {

	// Here is our simple mapping.
	private TableMapping<Object> mapping = new AbstractTestTableMapping<Object>() {
		@Override
		public String getTableName() {
			return "SOME_TABLE";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("id", "ID", true).withIsBackupId(true),
					new FieldColumn("bigName", "BIG_NAME"), };
		}
	};

	private TableMapping<Object> secondaryOne = new AbstractTestTableMapping<Object>() {
		@Override
		public String getTableName() {
			return "SECONDARY_ONE";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("ownerId", "OWNER_ID", true).withIsBackupId(true),
					new FieldColumn("date", "DATE"), };
		}
	};

	private TableMapping<Object> secondaryTwo = new AbstractTestTableMapping<Object>() {
		@Override
		public String getTableName() {
			return "SECONDARY_TWO";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("rootId", "ROOT_ID", true).withIsBackupId(true),
					new FieldColumn("someValue", "VALUE"), };
		}
	};

	private TableMapping<Object> mappingTwoKeys = new AbstractTestTableMapping<Object>() {

		@Override
		public String getTableName() {
			return "TWO_KEY_TABLE";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("owner", "OWNER_ID", true),
					new FieldColumn("revNumber", "REV_NUMBER", true), new FieldColumn("bigName", "BIG_NAME"),
					new FieldColumn("smallName", "SMALL_NAME"), };
		}
	};

	private TableMapping<Object> migrateableMappingSelfForeignKey = new AbstractTestTableMapping<Object>() {
		@Override
		public String getTableName() {
			return "SOME_TABLE";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("id", "ID", true).withIsBackupId(true),
					new FieldColumn("etag", "ETAG").withIsEtag(true),
					new FieldColumn("parentId", "PARENT_ID").withIsSelfForeignKey(true), };
		}
	};

	private TableMapping<Object> migrateableMappingNoEtagNotSelfForeignKey = new AbstractTestTableMapping<Object>() {
		@Override
		public String getTableName() {
			return "SOME_TABLE";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("id", "ID", true).withIsBackupId(true), };
		}
	};

	private TableMapping<Object> migrateableMappingEtagAndId = new AbstractTestTableMapping<Object>() {
		@Override
		public String getTableName() {
			return "SOME_TABLE";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("id", "ID", true).withIsBackupId(true),
					new FieldColumn("etag", "ETAG").withIsEtag(true), new FieldColumn("parentId", "PARENT_ID") };
		}
	};

	private TableMapping<Object> migrateableMappingNoEtag = new AbstractTestTableMapping<Object>() {
		@Override
		public String getTableName() {
			return "SOME_TABLE";
		}

		@Override
		public FieldColumn[] getFieldColumns() {
			return new FieldColumn[] { new FieldColumn("id", "ID", true).withIsBackupId(true),
					new FieldColumn("etag", "ETAG"), new FieldColumn("parentId", "PARENT_ID") };
		}
	};

	@Test
	public void testCreateInsertStatement() {

		String dml = DMLUtils.createInsertStatement(mapping);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals("INSERT INTO SOME_TABLE(`ID`, `BIG_NAME`) VALUES (:id, :bigName)", dml);
	}

	@Test
	public void testAppendPrimaryKeySingle() {
		StringBuilder builder = new StringBuilder();
		DMLUtils.appendPrimaryKey(mapping, builder);
		String result = builder.toString();
		System.out.println(result);
		assertEquals("`ID` = :id", result);
	}

	@Test
	public void testAppendPrimaryKeyTwoKey() {
		StringBuilder builder = new StringBuilder();
		DMLUtils.appendPrimaryKey(mappingTwoKeys, builder);
		String result = builder.toString();
		System.out.println(result);
		assertEquals("`OWNER_ID` = :owner AND `REV_NUMBER` = :revNumber", result);
	}

	@Test
	public void testCreateGetByIDStatement() {
		// Here is our simple mapping.
		String dml = DMLUtils.createGetByIDStatement(mapping);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals("SELECT * FROM SOME_TABLE WHERE `ID` = :id", dml);
	}

	@Test
	public void testDeleteStatement() {
		// Here is our simple mapping.
		String dml = DMLUtils.createDeleteStatement(mapping);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals("DELETE FROM SOME_TABLE WHERE `ID` = :id", dml);
	}

	@Test
	public void testCreateUpdateStatment() {
		// Here is our simple mapping.
		String dml = DMLUtils.createUpdateStatment(mapping);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals("UPDATE SOME_TABLE SET `BIG_NAME` = :bigName WHERE `ID` = :id", dml);
	}

	@Test
	public void testCreateGetCountStatment() {
		// Here is our simple mapping.
		String dml = DMLUtils.createGetCountByPrimaryKeyStatement(mapping);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals("SELECT COUNT(ID) FROM SOME_TABLE", dml);
	}

	@Test
	public void testCreateGetMaxStatement() {
		String dml = DMLUtils.createGetMaxByBackupKeyStatement(mapping);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals("SELECT MAX(ID) FROM SOME_TABLE", dml);
	}

	@Test
	public void testCreateGetMinStatement() {
		String dml = DMLUtils.createGetMinByBackupKeyStatement(mapping);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals("SELECT MIN(ID) FROM SOME_TABLE", dml);
	}

	@Test
	public void testCreateUpdateStatmentTwoKeys() {
		// Here is our simple mapping.
		String dml = DMLUtils.createUpdateStatment(mappingTwoKeys);
		assertNotNull(dml);
		System.out.println(dml);
		assertEquals(
				"UPDATE TWO_KEY_TABLE SET `BIG_NAME` = :bigName, `SMALL_NAME` = :smallName WHERE `OWNER_ID` = :owner AND `REV_NUMBER` = :revNumber",
				dml);
	}

	@Test
	public void testCreateDeleteByBackupIdRange() {
		String sql = DMLUtils.createDeleteByBackupIdRange(migrateableMappingNoEtagNotSelfForeignKey);
		assertNotNull(sql);
		System.out.println(sql);
		assertEquals("DELETE FROM SOME_TABLE WHERE `ID` >= :BMINID AND `ID` < :BMAXID", sql);
	}

	@Test
	public void testGetBatchInsertOrUdpate() {
		String sql = DMLUtils.getBatchInsertOrUdpate(migrateableMappingSelfForeignKey);
		assertNotNull(sql);
		System.out.println(sql);
		assertEquals(
				"INSERT INTO SOME_TABLE(`ID`, `ETAG`, `PARENT_ID`) VALUES (:id, :etag, :parentId) ON DUPLICATE KEY UPDATE `ETAG` = :etag, `PARENT_ID` = :parentId",
				sql);
	}

	@Test
	public void testGetBatchInsertOrUdpatePrimaryKeyOnly() {
		String sql = DMLUtils.getBatchInsertOrUdpate(migrateableMappingNoEtagNotSelfForeignKey);
		assertNotNull(sql);
		System.out.println(sql);
		assertEquals("INSERT IGNORE INTO SOME_TABLE(`ID`) VALUES (:id)", sql);
	}

	@Test
	public void testCreateSelectChecksumStatementWithEtagColumn() {
		final String expectedSql = "SELECT CONCAT(SUM(CRC32(CONCAT(`ID`, '@', IFNULL(`ETAG`, 'NULL'), '@@', ?))), '%', BIT_XOR(CRC32(CONCAT(`ID`, '@', IFNULL(`ETAG`, 'NULL'), '@@', ?)))) FROM SOME_TABLE WHERE `ID` >= ? AND `ID` <= ?";
		String sql = DMLUtils.createSelectChecksumStatement(migrateableMappingEtagAndId);
		assertNotNull(sql);
		System.out.println(sql);
		assertEquals(expectedSql, sql);
	}

	@Test
	public void testCreateSelectChecksumStatementWithoutEtagColumn() {
		String expectedSql = "SELECT CONCAT(SUM(CRC32(CONCAT(`ID`, '@@', ?))), '%', BIT_XOR(CRC32(CONCAT(`ID`, '@@', ?)))) FROM SOME_TABLE WHERE `ID` >= ? AND `ID` <= ?";
		String sql = DMLUtils.createSelectChecksumStatement(migrateableMappingNoEtag);
		assertNotNull(sql);
		System.out.println(sql);
		assertEquals(expectedSql, sql);
	}

	@Test
	public void testCreateChecksumTableStatement() {
		String expectedSql = "CHECKSUM TABLE SOME_TABLE";
		String sql = DMLUtils.createChecksumTableStatement(migrateableMappingEtagAndId);
		assertNotNull(sql);
		System.out.println(sql);
		assertEquals(expectedSql, sql);
	}

	@Test
	public void testCreateMinMaxCountByKeyStatement() {
		String expectedSql = "SELECT MIN(`ID`), MAX(`ID`), COUNT(`ID`) FROM SOME_TABLE";
		String sql = DMLUtils.createGetMinMaxCountByKeyStatement(migrateableMappingEtagAndId);
		assertNotNull(sql);
		System.out.println(sql);
		assertEquals(expectedSql, sql);
	}

	@Test
	public void testGetBackupRangeBatch() {
		String expectedSql = "SELECT * FROM SOME_TABLE WHERE `ID` >= :BMINID AND `ID` < :BMAXID";
		String sql = DMLUtils.getBackupRangeBatch(mapping);
		assertEquals(expectedSql, sql);
	}

	@Test
	public void testPrimaryCardinality() {
		String expectedSql = 
				"SELECT P.ID, 1  + COUNT(S0.OWNER_ID) + COUNT(S1.ROOT_ID) AS cardinality"
				+ " FROM SOME_TABLE AS P"
				+ " LEFT JOIN SECONDARY_ONE AS S0 ON (P.ID =  S0.OWNER_ID)"
				+ " LEFT JOIN SECONDARY_TWO AS S1 ON (P.ID =  S1.ROOT_ID)"
				+ " WHERE P.ID >= ? AND P.ID < ? GROUP BY P.ID";
		TableMapping primaryMapping = mapping;
		List<TableMapping> secondaryMappings = Lists.newArrayList(secondaryOne, secondaryTwo);
		String sql = DMLUtils.createPrimaryCardinalitySql(primaryMapping, secondaryMappings);
		assertEquals(expectedSql, sql);
	}
	
	@Test
	public void testPrimaryCardinalityNoSecondary() {
		String expectedSql = 
				"SELECT P.ID, 1  AS cardinality FROM SOME_TABLE AS P WHERE P.ID >= ? AND P.ID < ? GROUP BY P.ID";
		TableMapping primaryMapping = mapping;
		List<TableMapping> secondaryMappings = new LinkedList<>();
		String sql = DMLUtils.createPrimaryCardinalitySql(primaryMapping, secondaryMappings);
		assertEquals(expectedSql, sql);
	}
}
