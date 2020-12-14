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


package mil.army.usace.erdc.crrel.dataquery;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import mil.army.usace.erdc.crrel.dataquery.rdbms.implementations.h2.H2;
import mil.army.usace.erdc.crrel.dataquery.rdbms.implementations.oracle.Oracle;
import mil.army.usace.erdc.crrel.dataquery.rdbms.implementations.postgres.Postgres;
import mil.army.usace.erdc.crrel.dataquery.rdbms.implementations.sqlite.Sqlite;
import mil.army.usace.erdc.crrel.dataquery.rdbms.implementations.sqlserver.SqlServer;
import mil.army.usace.erdc.crrel.dataquery.web.implementations.sharepoint.lists.SharepointList;




public class DataQueryFactory {
    public static enum DB {
        POSTGRES,
        ORACLE,
        H2,
        SQLITE,
        SQLSERVER,
        NETEZZA,
        SHAREPOINT
    }
    
    public static DataSource create(Connection conn){
        try{
            DatabaseMetaData metadata = conn.getMetaData();
            String productName = metadata.getDatabaseProductName();
            if(productName.toLowerCase().contains("oracle")){
                return new Oracle(conn);
            }
            else if(productName.toLowerCase().contains("h2")){
                return new H2(conn);
            } 
            else if(productName.toLowerCase().contains("sqlserver")){
                return new SqlServer(conn);
            }
            else{
                throw new UnsupportedOperationException("Unsupported Database Connection");
            }
        }
        catch(Exception ex){
           throw new RuntimeException(ex.getMessage());
        }     
    }
    
    
    public static DataSource create(DB db, String url){
        try{
            DataSource dataSource;
            switch(db){
                case ORACLE:
                    dataSource = new Oracle(url);
                    break;
                case H2:
                    dataSource = new H2(url);
                    break;
                case POSTGRES:
                    dataSource = new Postgres(url);
                    break;
                case SQLITE:
                    dataSource = new Sqlite(url);
                    break;
                case SQLSERVER:
                    dataSource = new SqlServer(url);
                    break;
                case SHAREPOINT:
                    dataSource = new SharepointList(url);
                    break;
                default:
                    throw new DataQueryException("Unsupported Database URL");
            }
            return dataSource;
        }
        catch(Exception ex){
            throw(new RuntimeException(ex));
        }
    }

    public static DataSource create(DB db, ConnectionConfig config){
        try{
            DataSource dataSource;
            switch(db){
                case ORACLE:
                    dataSource = new Oracle(config);
                    break;
                default:
                    throw new DataQueryException("Unsupported Database Config");
            }
            return dataSource;
        }
        catch(Exception ex){
            throw(new RuntimeException(ex));
        }
    }
    
}
