package com.microsoft.sqlserver.jdbc.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.testframework.AbstractTest;
import com.microsoft.sqlserver.testframework.util.RandomUtil;

@RunWith(JUnitPlatform.class)
public class TimeoutTest extends AbstractTest {
	String randomServer = RandomUtil.getIdentifier("Server");
	String waitForDelaySPName = "waitForDelaySP";
	final int waitForDelaySeconds = 10;

	@Test
	public void testDefaultLoginTimeout() {
		long timerStart = 0;
		long timerEnd = 0;
		try {
			timerStart = System.currentTimeMillis();
			// Try a non existing server and see if the default timeout is 15 seconds
			DriverManager.getConnection("jdbc:sqlserver://" + randomServer + ";user=sa;password=pwd;");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("The TCP/IP connection to the host"));
			timerEnd = System.currentTimeMillis();
		}
		assertTrue(0 != timerEnd, "Should not have connected.");

		long timeDiff = timerEnd - timerStart;
		assertTrue(timeDiff > 14000);
	}

	@Test
	public void testFailoverInstanceResolution() throws SQLServerException {
		long timerStart = 0;
		long timerEnd = 0;
		try {
			timerStart = System.currentTimeMillis();
			// Try a non existing server and see if the default timeout is 15 seconds
			DriverManager.getConnection("jdbc:sqlserver://" + randomServer + ";databaseName=FailoverDB_abc;failoverPartner=" + randomServer + "\\foo;user=sa;password=pwd;");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("The TCP/IP connection to the host"));
			timerEnd = System.currentTimeMillis();
		}
		assertTrue(0 != timerEnd, "Should not have connected.");

		long timeDiff = timerEnd - timerStart;
		assertTrue(timeDiff > 14000);
	}

	@Test
	public void testFOInstanceResolution2() throws SQLServerException {
		long timerStart = 0;
		long timerEnd = 0;
		try {
			timerStart = System.currentTimeMillis();
			// Try a non existing server and see if the default timeout is 15 secs at least
			DriverManager.getConnection("jdbc:sqlserver://" + randomServer + "\\fooggg;databaseName=FailoverDB;failoverPartner=" + randomServer + "\\foo;user=sa;password=pwd;");
		} catch (Exception e) {
			timerEnd = System.currentTimeMillis();
		}
		assertTrue(0 != timerEnd, "Should not have connected.");

		long timeDiff = timerEnd - timerStart;
		assertTrue(timeDiff > 14000);
	}

	@Test
	public void testQueryTimeout() throws Exception {
		SQLServerConnection conn = (SQLServerConnection) DriverManager.getConnection(connectionString);

		dropWaitForDelayProcedure(conn);
		createWaitForDelayPreocedure(conn);

		conn = (SQLServerConnection) DriverManager.getConnection(connectionString + ";queryTimeout=" + (waitForDelaySeconds / 2) + ";");

		try {
			conn.createStatement().execute("exec " + waitForDelaySPName);
			throw new Exception("Exception for queryTimeout is not thrown.");
		} catch (Exception e) {
			if (!(e instanceof SQLServerException)) {
				throw e;
			}
			assertEquals(e.getMessage(), "The query has timed out.", "Invalid exception message");
		}
	}

	@Test
	public void testSocketTimeout() throws Exception {
		SQLServerConnection conn = (SQLServerConnection) DriverManager.getConnection(connectionString);

		dropWaitForDelayProcedure(conn);
		createWaitForDelayPreocedure(conn);

		conn = (SQLServerConnection) DriverManager.getConnection(connectionString + ";socketTimeout=" + (waitForDelaySeconds * 1000 / 2) + ";");

		try {
			conn.createStatement().execute("exec " + waitForDelaySPName);
			throw new Exception("Exception for socketTimeout is not thrown.");
		} catch (Exception e) {
			if (!(e instanceof SQLServerException)) {
				throw e;
			}
			assertEquals(e.getMessage(), "Read timed out", "Invalid exception message");
		}
	}

	private void dropWaitForDelayProcedure(SQLServerConnection conn) throws SQLException {
		String sql = " IF EXISTS (select * from sysobjects where id = object_id(N'" + waitForDelaySPName + "') and OBJECTPROPERTY(id, N'IsProcedure') = 1)" + " DROP PROCEDURE "
				+ waitForDelaySPName;
		conn.createStatement().execute(sql);
	}

	private void createWaitForDelayPreocedure(SQLServerConnection conn) throws SQLException {
		String sql = "CREATE PROCEDURE " + waitForDelaySPName + " AS" + " BEGIN" + " WAITFOR DELAY '00:00:" + waitForDelaySeconds + "';" + " END";
		conn.createStatement().execute(sql);
	}
}
