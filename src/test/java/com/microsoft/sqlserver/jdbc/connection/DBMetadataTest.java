package com.microsoft.sqlserver.jdbc.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.testframework.AbstractTest;
import com.microsoft.sqlserver.testframework.DBTable;
import com.microsoft.sqlserver.testframework.util.RandomUtil;

@RunWith(JUnitPlatform.class)
public class DBMetadataTest extends AbstractTest {
	@Test
	public void testDatabaseMetaData() throws SQLException {
		String functionName = RandomUtil.getIdentifier("proc");
		functionName = DBTable.escapeIdentifier(functionName);

		SQLServerDataSource ds = new SQLServerDataSource();
		ds.setURL(connectionString);

		Connection con = ds.getConnection();

		//drop function
		String sqlDropFunction = "if exists (select * from dbo.sysobjects where id = object_id(N'[dbo]." + functionName + "')" + "and xtype in (N'FN', N'IF', N'TF'))"
				+ "drop function " + functionName;
		con.createStatement().execute(sqlDropFunction);

		//create function
		String sqlCreateFunction = "CREATE  FUNCTION " + functionName + " (@text varchar(8000), @delimiter varchar(20) = ' ') RETURNS @Strings TABLE "
				+ "(position int IDENTITY PRIMARY KEY, value varchar(8000)) AS BEGIN INSERT INTO @Strings VALUES ('DDD') RETURN END ";
		con.createStatement().execute(sqlCreateFunction);

		DatabaseMetaData md = con.getMetaData();
		ResultSet arguments = md.getProcedureColumns(null, null, null, "@TABLE_RETURN_VALUE");

		if (arguments.next()) {
			arguments.getString("COLUMN_NAME");
			arguments.getString("DATA_TYPE"); // call this function to make sure it does not crash
		}

		con.createStatement().execute(sqlDropFunction);
	}
}
