package com.microsoft.sqlserver.jdbc.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.sql.PooledConnection;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.ISQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;
import com.microsoft.sqlserver.testframework.AbstractTest;
import com.microsoft.sqlserver.testframework.DBConnection;
import com.microsoft.sqlserver.testframework.DBTable;
import com.microsoft.sqlserver.testframework.util.RandomUtil;

@RunWith(JUnitPlatform.class)
public class PoolingTest extends AbstractTest {
	@Test
    public void testPooling() throws SQLException {
        assumeTrue(!DBConnection.isSqlAzure(DriverManager.getConnection(connectionString)), "Skipping test case on Azure SQL.");

        String randomTableName = RandomUtil.getIdentifier("table");

        // make the table a temporary table (will be created in tempdb database)
        String tempTableName = "#" + randomTableName;

        SQLServerXADataSource XADataSource1 = new SQLServerXADataSource();
        XADataSource1.setURL(connectionString);
        XADataSource1.setDatabaseName("tempdb");

        PooledConnection pc = XADataSource1.getPooledConnection();
        Connection conn = pc.getConnection();

        // create table in tempdb database
        conn.createStatement().execute("create table [" + tempTableName + "] (myid int)");
        conn.createStatement().execute("insert into [" + tempTableName + "] values (1)");
        conn.close();

        conn = pc.getConnection();

        boolean tempTableFileRemoved = false;
        try {
            conn.createStatement().executeQuery("select * from [" + tempTableName + "]");
        }
        catch (SQLServerException e) {
            // make sure the temporary table is not found.
            if (e.getMessage().startsWith("Invalid object name")) {
                tempTableFileRemoved = true;
            }
        }
        assertTrue(tempTableFileRemoved, "Temporary table is not removed.");
    }

	@Test
	public void testConnectionPoolReget() throws SQLException {
		SQLServerXADataSource ds = new SQLServerXADataSource();
		ds.setURL(connectionString);

		PooledConnection pc = ds.getPooledConnection();
		Connection con = pc.getConnection();

		// now reget a connection
		Connection con2 = pc.getConnection();

		// assert that the first connection is closed.
		assertTrue(con.isClosed(), "First connection is not closed");
	}

	@Test
	public void testConnectionPoolConnFunctions() throws SQLException {
		String tableName = RandomUtil.getIdentifier("table");
		tableName = DBTable.escapeIdentifier(tableName);

		String sql1 = "if exists (select * from dbo.sysobjects where name = '" + tableName + "' and type = 'U')\n" + "drop table " + tableName + "\n" + "create table " + tableName
				+ "\n" + "(\n" + "wibble_id int primary key not null,\n" + "counter int null\n" + ");";
		String sql2 = "if exists (select * from dbo.sysobjects where name = '" + tableName + "' and type = 'U')\n" + "drop table " + tableName + "\n";

		SQLServerXADataSource ds = new SQLServerXADataSource();
		ds.setURL(connectionString);

		PooledConnection pc = ds.getPooledConnection();
		Connection con = pc.getConnection();

		Statement statement = con.createStatement();
		statement.execute(sql1);
		statement.execute(sql2);
		con.clearWarnings();
		pc.close();
	}

	@Test
	public void testConnectionPoolClose() throws SQLException {
		SQLServerXADataSource ds = new SQLServerXADataSource();
		ds.setURL(connectionString);

		PooledConnection pc = ds.getPooledConnection();
		Connection con = pc.getConnection();

		pc.close();
		// assert that the first connection is closed.
		assertTrue(con.isClosed(), "Connection is not closed with pool close");
	}

	@Test
	public void testConnectionPoolClientConnectionId() throws SQLException {
		SQLServerXADataSource ds = new SQLServerXADataSource();
		ds.setURL(connectionString);

		PooledConnection pc = ds.getPooledConnection();
		ISQLServerConnection con = (ISQLServerConnection) pc.getConnection();

		UUID Id1 = con.getClientConnectionId();
		assertTrue(Id1 != null, "Unexecepted: ClientConnectionId is null from Pool");
		con.close();

		// now reget the connection
		ISQLServerConnection con2 = (ISQLServerConnection) pc.getConnection();

		UUID Id2 = con2.getClientConnectionId();
		con2.close();

		assertEquals(Id1, Id2, "ClientConnection Ids from pool are not the same.");
	}
}
