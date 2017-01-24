package com.microsoft.sqlserver.jdbc.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.microsoft.sqlserver.testframework.AbstractTest;

@RunWith(JUnitPlatform.class)
public class WarningTest extends AbstractTest {
	@Test
	public void testWarnings() throws SQLException {
		SQLServerConnection conn = (SQLServerConnection) DriverManager.getConnection(connectionString);

		Properties info = conn.getClientInfo();
		conn.setClientInfo(info);
		SQLWarning warn = conn.getWarnings();
		assertEquals(null, warn, "Warnings found.");

		Properties info2 = new Properties();
		String[] infoArray = { "prp1", "prp2", "prp3", "prp4", "prp5" };
		for (int i = 0; i < 5; i++) {
			info2.put(infoArray[i], "");
		}
		conn.setClientInfo(info2);
		warn = conn.getWarnings();
		for (int i = 4; i >= 0; i--) {
			assertTrue(warn.toString().contains(infoArray[i]), "Warnings not found!");
			warn = warn.getNextWarning();
		}
		conn.clearWarnings();

		conn.setClientInfo("prop7", "");
		warn = conn.getWarnings();
		assertTrue(warn.toString().contains("prop7"), "Warnings not found!");
		warn = warn.getNextWarning();
		assertEquals(null, warn, "Warnings found!");
	}
}
