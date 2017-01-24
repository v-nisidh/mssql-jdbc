//---------------------------------------------------------------------------------------------------------------------------------
// File: SQLServerResultSetMetaData.java
//
//
// Microsoft JDBC Driver for SQL Server
// Copyright(c) Microsoft Corporation
// All rights reserved.
// MIT License
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), 
//  to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
//  and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions :
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
//  IN THE SOFTWARE.
//---------------------------------------------------------------------------------------------------------------------------------
 
 
package com.microsoft.sqlserver.jdbc;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;


/**
* A ResultSetMetaData object can be used to obtain the meta data (types
* and type properties) of the columns in a ResultSet.
*
* The API javadoc for JDBC API methods that this class implements are not repeated here. Please
* see Sun's JDBC API interfaces javadoc for those details.
*/

public final class SQLServerResultSetMetaData implements java.sql.ResultSetMetaData
{
    private SQLServerConnection con;
    private final SQLServerResultSet rs;
    public int nBeforeExecuteCols;
    static final private java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger("com.microsoft.sqlserver.jdbc.internals.SQLServerResultSetMetaData");

    static private final AtomicInteger baseID = new AtomicInteger(0);	// Unique id generator for each  instance (used for logging).
    final private String traceID;
    // Returns unique id for each instance.
    private static int nextInstanceID()
    {
        return baseID.incrementAndGet();
    }
    final public String toString()
    {
        return traceID;
    }


   /**
    * Create a new meta data object for the result set.
    * @param con the connection
    * @param rs the parent result set
    */
   /*L0*/ SQLServerResultSetMetaData(SQLServerConnection con, SQLServerResultSet rs) 
    {
        traceID = " SQLServerResultSetMetaData:"  + nextInstanceID();
        this.con = con;
        this.rs =rs;
        assert rs !=null;
        if(logger.isLoggable(java.util.logging.Level.FINE))
        {
            logger.fine(toString() + " created by (" + rs.toString() + ")");
        }
    }
    private void checkClosed() throws SQLServerException 
    {
        rs.checkClosed();
    }

   

   /* ------------------ JDBC API Methods --------------------- */

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        DriverJDBCVersion.checkSupportsJDBC4();
        // This class cannot be unwrapped
        return false;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        DriverJDBCVersion.checkSupportsJDBC4();
        T t;
        try
        {
            t = iface.cast(this);
        }
        catch (ClassCastException e)
        {
            throw new SQLServerException(e.getMessage(), e);
        }
        return t;
    }

    public String getCatalogName(int column) throws SQLServerException
    {
        checkClosed();
        return rs.getColumn(column).getTableName().getDatabaseName();
    }

   /*L0*/ public int getColumnCount() throws SQLServerException {
    checkClosed();
      if (rs==null)
        return 0;
      return rs.getColumnCount();
   }

    public int getColumnDisplaySize(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().getDisplaySize();
        }
        
        return rs.getColumn(column).getTypeInfo().getDisplaySize();
    }

    public String getColumnLabel(int column) throws SQLServerException
    {
        checkClosed();
        return rs.getColumn(column).getColumnName();
    }

    public String getColumnName(int column) throws SQLServerException
    {
        checkClosed();
        return rs.getColumn(column).getColumnName();
    }

    public int getColumnType(int column) throws SQLServerException
    {
        checkClosed();
		// under Katmai map the max types to non max to be inline with DBMD.
		TypeInfo typeInfo = rs.getColumn(column).getTypeInfo();

		CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	typeInfo = cryptoMetadata.getBaseTypeInfo();
        }

		JDBCType jdbcType = typeInfo.getSSType().getJDBCType();
		int r = jdbcType.asJavaSqlType();
		if (con.isKatmaiOrLater())
		{	
			SSType 	sqlType =	typeInfo.getSSType();
			
			switch (sqlType)
			{
				case VARCHARMAX:
					r = SSType.VARCHAR.getJDBCType().asJavaSqlType();
					break;
				case NVARCHARMAX:
					r =  SSType.NVARCHAR.getJDBCType().asJavaSqlType();
					break;	
				case VARBINARYMAX:
					r =  SSType.VARBINARY.getJDBCType().asJavaSqlType();
					break;
				case DATETIME:
				case SMALLDATETIME:
					r =  SSType.DATETIME2.getJDBCType().asJavaSqlType();
					break;
				case MONEY:
				case SMALLMONEY:
					r =  SSType.DECIMAL.getJDBCType().asJavaSqlType();
					break;
				case GUID:
					r = SSType.CHAR.getJDBCType().asJavaSqlType();
					break;
				default:
					// Do nothing
				break;
			}
		}
		return r;
    }

    public String getColumnTypeName(int column) throws SQLServerException
    { 
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().getSSTypeName();
        }
        
        return rs.getColumn(column).getTypeInfo().getSSTypeName();
    }

    public int getPrecision(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().getPrecision();
        }
        
        return rs.getColumn(column).getTypeInfo().getPrecision();
    }

    public int getScale(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().getScale();
        }
        
        return rs.getColumn(column).getTypeInfo().getScale();
    }

    public String getSchemaName(int column) throws SQLServerException
    {
        checkClosed();
        return rs.getColumn(column).getTableName().getSchemaName();
    }

    public String getTableName(int column) throws SQLServerException
    {
        checkClosed();
        return rs.getColumn(column).getTableName().getObjectName();
    }

    public boolean isAutoIncrement(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().isIdentity();
        }
        
        return rs.getColumn(column).getTypeInfo().isIdentity();
    }

    public boolean isCaseSensitive(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().isCaseSensitive();
        }
        
        return rs.getColumn(column).getTypeInfo().isCaseSensitive();
    }

    public boolean isCurrency(int column) throws SQLServerException
    {
        checkClosed();
        SSType ssType = rs.getColumn(column).getTypeInfo().getSSType();

        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	ssType = cryptoMetadata.getBaseTypeInfo().getSSType();
        }

        return SSType.MONEY == ssType ||
               SSType.SMALLMONEY == ssType;
    }

    public boolean isDefinitelyWritable(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return TypeInfo.UPDATABLE_READ_WRITE == cryptoMetadata.getBaseTypeInfo().getUpdatability();
        }
        
        return TypeInfo.UPDATABLE_READ_WRITE == rs.getColumn(column).getTypeInfo().getUpdatability();
    }

    public int isNullable(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().isNullable() ? columnNullable : columnNoNulls;
        }
        
        return rs.getColumn(column).getTypeInfo().isNullable() ? columnNullable : columnNoNulls;
    }

    public boolean isReadOnly(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return TypeInfo.UPDATABLE_READ_ONLY ==cryptoMetadata.getBaseTypeInfo().getUpdatability();
        }
        
        return TypeInfo.UPDATABLE_READ_ONLY == rs.getColumn(column).getTypeInfo().getUpdatability();
    }

    public boolean isSearchable(int column) throws SQLServerException
    {
        checkClosed();
        
        SSType ssType = null;
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        
        if(null != cryptoMetadata){
        	ssType = cryptoMetadata.getBaseTypeInfo().getSSType();
        }
        else{
        	ssType = rs.getColumn(column).getTypeInfo().getSSType();
        }
        
        switch (ssType)
        {
            case IMAGE:
            case TEXT:
            case NTEXT:
            case UDT:
            case XML:
                return false;

            default:
                return true;
        }
    }

    public boolean isSigned(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().getSSType().getJDBCType().isSigned();
        }

        return rs.getColumn(column).getTypeInfo().getSSType().getJDBCType().isSigned();
    }

    /**
     * Returns true if the column is a SQLServer SparseColumnSet 
     * @param column The column number
     * @return true if a column in a result set is a sparse column set, otherwise false. 
     * @throws SQLServerException when an error occurs
     */
    public boolean isSparseColumnSet(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().isSparseColumnSet();
        }
        
        return rs.getColumn(column).getTypeInfo().isSparseColumnSet();
    }

    public boolean isWritable(int column) throws SQLServerException
    {
        checkClosed();
        
        int updatability = -1;
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	updatability = cryptoMetadata.getBaseTypeInfo().getUpdatability();
        }
        else{
        	updatability = rs.getColumn(column).getTypeInfo().getUpdatability();
        }
        return TypeInfo.UPDATABLE_READ_WRITE == updatability ||
               TypeInfo.UPDATABLE_UNKNOWN == updatability;
    }

    public String getColumnClassName(int column) throws SQLServerException
    {
        checkClosed();
        
        CryptoMetadata cryptoMetadata = rs.getColumn(column).getCryptoMetadata();
        if(null != cryptoMetadata){
        	return cryptoMetadata.getBaseTypeInfo().getSSType().getJDBCType().className();
        }
        
        return rs.getColumn(column).getTypeInfo().getSSType().getJDBCType().className();
    }
}
