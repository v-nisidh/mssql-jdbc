// ---------------------------------------------------------------------------------------------------------------------------------
// File: DBTable.java
//
//
// Microsoft JDBC Driver for SQL Server
// Copyright(c) Microsoft Corporation
// All rights reserved.
// MIT License
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"),
// to deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense,
// and / or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions :
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.
// ---------------------------------------------------------------------------------------------------------------------------------

package com.microsoft.sqlserver.testframework;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.sqlserver.testframework.sqlType.SqlType;
import com.microsoft.sqlserver.testframework.sqlType.VariableLengthType;
import com.microsoft.sqlserver.testframework.util.RandomUtil;

/**
 * This class holds data for Table.
 */
public class DBTable extends AbstractSQLGenerator {

    public static final Logger log = Logger.getLogger("DBTable");
    String tableName;
    String escapedTableName;
    List<DBColumn> columns;
    int totalColumns;
    int totalRows = 2; // default row count set to 2
    DBSchema schema;

    /**
     * Initializes {@link DBTable} with tableName, schema, and {@link DBColumns}
     * 
     * @param autoGenerateSchema
     *            <code>true</code> : generates schema with all available
     *            dataTypes in SqlType class
     */
    public DBTable(boolean autoGenerateSchema) {

        this.tableName = RandomUtil.getIdentifier("table");
        this.escapedTableName = escapeIdentifier(tableName);
        this.schema = new DBSchema(autoGenerateSchema);
        if (autoGenerateSchema) {
            addColumns();
        }
        else {
            this.columns = new ArrayList<DBColumn>();
        }
        this.totalColumns = columns.size();
    }

    /**
     * Similar to {@link DBTable#DBTable(boolean)}, but uses existing list of
     * columns Used internally to clone schema
     * 
     * @param DBTable
     */
    private DBTable(DBTable sourceTable) {
        this.tableName = RandomUtil.getIdentifier("table");
        this.escapedTableName = escapeIdentifier(tableName);
        this.columns = sourceTable.columns;
        this.totalColumns = columns.size();
        this.schema = sourceTable.schema;
    }

    /**
     * adds a columns for each SQL type in DBSchema
     */
    private void addColumns() {
        totalColumns = schema.getNumberOfSqlTypes();
        columns = new ArrayList<DBColumn>(totalColumns);

        for (int i = 0; i < totalColumns; i++) {
            SqlType sqlType = schema.getSqlType(i);
            DBColumn column = new DBColumn(RandomUtil.getIdentifier(sqlType.getName()), sqlType);
            columns.add(column);
        }
    }

    /**
     * gets table name of the {@link DBTable} object
     * 
     * @return {@link String} table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * gets escaped table name of the {@link DBTable} object
     * 
     * @return {@link String} escaped table name
     */
    public String getEscapedTableName() {
        return escapedTableName;
    }

    /**
     * 
     * @return total rows in the table
     */
    public int getTotalRows() {
        return totalRows;
    }

    /**
     * 
     * @param totalRows
     *            set the number of rows in table, default value is 2
     */
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    /**
     * create table
     * 
     * @param dbstatement
     */
    boolean createTable(DBStatement dbstatement) {
        try {
            dropTable(dbstatement);
            String sql = createTableSql();
            return dbstatement.execute(sql);
        }
        catch (SQLException ex) {
            fail(ex.getMessage());
        }
        return false;
    }

    String createTableSql() {
        StringJoiner sb = new StringJoiner(SPACE_CHAR);

        sb.add(CREATE_TABLE);
        sb.add(escapedTableName);
        sb.add(OPEN_BRACKET);
        for (int i = 0; i < totalColumns; i++) {
            DBColumn column = getColumn(i);
            sb.add(escapeIdentifier(column.getColumnName()));
            sb.add(column.getSqlType().getName());
            // add precision and scale
            if (VariableLengthType.Precision == column.getSqlType().getVariableLengthType()) {
                sb.add(OPEN_BRACKET);
                sb.add("" + column.getSqlType().getPrecision());
                sb.add(CLOSE_BRACKET);
            }
            else if (VariableLengthType.Scale == column.getSqlType().getVariableLengthType()) {
                sb.add(OPEN_BRACKET);
                sb.add("" + column.getSqlType().getPrecision());
                sb.add(COMMA);
                sb.add("" + column.getSqlType().getScale());
                sb.add(CLOSE_BRACKET);
            }

            sb.add(COMMA);
        }
        sb.add(CLOSE_BRACKET);
        return sb.toString();
    }

    /**
     * populate table with values
     * 
     * @param dbstatement
     * @return
     */
    boolean populateTable(DBStatement dbstatement) {
        try {
            populateValues();
            String sql = populateTableSql();
            return dbstatement.execute(sql);
        }
        catch (SQLException ex) {
            fail(ex.getMessage());
        }
        return false;
    }

    private void populateValues() {
        // generate values for all columns
        for (int i = 0; i < totalColumns; i++) {
            DBColumn column = getColumn(i);
            column.populateValues(totalRows);
        }
    }

    public SqlType getSqlType(int columnIndex) {
        return getColumn(columnIndex).getSqlType();
    }

    public String getColumnName(int columnIndex) {
        return getColumn(columnIndex).getColumnName();
    }

    public int totalColumns() {
        return totalColumns;
    }

    /**
     * 
     * @return new DBTable object with same schema
     */
    public DBTable cloneSchema() {

        DBTable clonedTable = new DBTable(this);
        return clonedTable;
    }

    /**
     * 
     * @return query to create table
     */
    String populateTableSql() {
        StringJoiner sb = new StringJoiner(SPACE_CHAR);

        sb.add("INSERT");
        sb.add("INTO");
        sb.add(escapedTableName);
        sb.add("VALUES");

        for (int i = 0; i < totalRows; i++) {
            if (i != 0) {
                sb.add(COMMA);
            }
            sb.add(OPEN_BRACKET);
            for (int colNum = 0; colNum < totalColumns; colNum++) {

                // TODO: add betterway to enclose data
                if (JDBCType.CHAR == getColumn(colNum).getSqlType().getJdbctype()
                        || JDBCType.VARCHAR == getColumn(colNum).getSqlType().getJdbctype()
                        || JDBCType.NCHAR == getColumn(colNum).getSqlType().getJdbctype()
                        || JDBCType.NVARCHAR == getColumn(colNum).getSqlType().getJdbctype()
                        || JDBCType.TIMESTAMP == getColumn(colNum).getSqlType().getJdbctype()
                        || JDBCType.DATE == getColumn(colNum).getSqlType().getJdbctype()
                        || JDBCType.TIME == getColumn(colNum).getSqlType().getJdbctype())
                    sb.add("'" + String.valueOf(getColumn(colNum).getRowValue(i)) + "'");
                else
                    sb.add(String.valueOf(getColumn(colNum).getRowValue(i)));

                if (colNum < totalColumns - 1) {
                    sb.add(COMMA);
                }
            }
            sb.add(CLOSE_BRACKET);
        }

        return (sb.toString());
    }

    /**
     * Drop table from Database
     * 
     * @param dbstatement
     * @return true if table dropped
     */
    boolean dropTable(DBStatement dbstatement) {
        boolean result = false;
        try {
            String sql = dropTableSql();
            result = dbstatement.execute(sql);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Table Deleted " + tableName);
            }
            else {
                log.fine("Table did not exist : " + tableName);
            }
        }
        catch (SQLException ex) {
            fail(ex.getMessage());
        }
        return result;
    }

    /**
     * This will give you query for Drop Table.
     */
    String dropTableSql() {
        StringJoiner sb = new StringJoiner(SPACE_CHAR);
        sb.add("IF OBJECT_ID");
        sb.add(OPEN_BRACKET);
        sb.add(wrapName(tableName));
        sb.add(",");
        sb.add(wrapName("U"));
        sb.add(CLOSE_BRACKET);
        sb.add("IS NOT NULL");
        sb.add("DROP TABLE");
        sb.add(escapedTableName); // for drop table no need to wrap.
        return sb.toString();
    }

    /**
     * new column to add to DBTable based on the SqlType
     * 
     * @param sqlType
     */
    public void addColumn(SqlType sqlType) {
        schema.addSqlTpe(sqlType);
        DBColumn column = new DBColumn(RandomUtil.getIdentifier(sqlType.getName()), sqlType);
        columns.add(column);
        ++totalColumns;
    }

    /**
     * 
     * @param index
     * @return DBColumn
     */
    DBColumn getColumn(int index) {
        return columns.get(index);
    }
}
