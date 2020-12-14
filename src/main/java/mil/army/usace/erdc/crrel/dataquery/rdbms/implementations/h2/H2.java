/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 
 */

package mil.army.usace.erdc.crrel.dataquery.rdbms.implementations.h2;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import mil.army.usace.erdc.crrel.dataquery.DataSource;
import mil.army.usace.erdc.crrel.dataquery.connection.DataQueryConnection;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Dml;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select;
import mil.army.usace.erdc.crrel.dataquery.rdbms.DatabaseDialect;
import mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsConnection;
import mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsDefaultConverter;
import mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsDefaultDml;
import mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsDefaultSelect;
import mil.army.usace.erdc.crrel.dataquery.utils.DataQueryConverter;

/**
 *
 * @author rsgoss
 */
public class H2 implements DataSource,AutoCloseable{
    
    private final RdbmsDefaultSelect select;
    private final RdbmsDefaultDml dml;
    private final RdbmsConnection rconn;
    private final DatabaseDialect dbDialect;
    private final DataQueryConverter converter;
    
    
    public H2(String url) throws SQLException, ClassNotFoundException{
        Connection conn=connect(url);
        this.rconn=new RdbmsConnection(conn);
        this.converter=new RdbmsDefaultConverter();
        this.dbDialect=new H2Dialect();
        this.select=new RdbmsDefaultSelect(rconn,converter);
        this.dml=new RdbmsDefaultDml(rconn,dbDialect);
    }
    
    public H2(Connection conn) {
        this.rconn=new RdbmsConnection(conn);
        this.converter=new RdbmsDefaultConverter();
        this.dbDialect=new H2Dialect();
        this.select=new RdbmsDefaultSelect(rconn,converter);
        this.dml=new RdbmsDefaultDml(rconn,dbDialect);
    }
    
    private Connection connect(String url) throws SQLException, ClassNotFoundException{
        Class.forName ("org.h2.Driver"); 
        Driver driver=new org.h2.Driver();
        DriverManager.registerDriver(driver);
        DriverManager.setLoginTimeout(10);
        Connection conn = DriverManager.getConnection(url);
        conn.setAutoCommit(true);
        return conn;
    }

    public Long getNextSequenceVal(String sequenceName){
        String sql="select nextval('"+sequenceName+"')";
        Object val=select.getSingleSqlValue(sql, null,null,false);
        return ((java.math.BigDecimal)val).longValue();
    }

    @Override
    public void close() throws Exception {
        rconn.close();
    }

    @Override
    public Select getSelect() {
        return this.select;
    }

    @Override
    public Dml getDml() {
        return this.dml;
    }

    @Override
    public DataQueryConnection getDataQueryConnection() {
        return this.rconn;
    }
    
}
