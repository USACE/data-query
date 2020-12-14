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

package mil.army.usace.erdc.crrel.dataquery.fluent;

import java.sql.Connection;
import java.util.logging.Logger;

import mil.army.usace.erdc.crrel.dataquery.ConnectionConfig;
import mil.army.usace.erdc.crrel.dataquery.DataQueryFactory;
import mil.army.usace.erdc.crrel.dataquery.DataQueryFactory.DB;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select.ReturnType;
import mil.army.usace.erdc.crrel.dataquery.DataSource;
import mil.army.usace.erdc.crrel.dataquery.connection.DataQueryConnection;


/**
 *
 * @author k3endrsg
 */
public class DataQuery implements AutoCloseable{
    
    public final DataSource dataSource;
    
    private static final Logger LOG = Logger.getLogger(DataQuery.class.getName()); 
    
    public DataQuery(Connection connection){
        this.dataSource=DataQueryFactory.create(connection);
    }
    
    
    public DataQuery(DB dbType, String dbUrl){
        this.dataSource = DataQueryFactory.create(dbType,dbUrl);
    }

    public DataQuery(DB dbType, ConnectionConfig config){
        this.dataSource = DataQueryFactory.create(dbType,config);
    }
    
    

    @Override
    public void close(){
        dataSource.getDataQueryConnection().close();        
    }
    
    public SelectQuery select(Class clazz){
        return new SelectQuery(dataSource.getSelect(),clazz);
    }
    
    public SelectQuery select(ReturnType returnType){
        return new SelectQuery(dataSource.getSelect(),returnType);
    }
    
    public InsertQuery insert(){
        return new InsertQuery(dataSource.getDml());
    }
    
    public UpdateQuery update(){
        return new UpdateQuery(dataSource.getDml());
    }
    
    
    public ExecuteCommand executeUpdate(){
        return new ExecuteCommand(dataSource.getDataQueryConnection());
    }
        
    public DeleteQuery delete(){
        return new DeleteQuery(dataSource.getDml());
    }
    
    public DeleteQuery delete(Class clazz){
        return new DeleteQuery(dataSource.getDml(),clazz);
    }
    
    public void transaction(Transaction t){
        DataQueryConnection dqConn = dataSource.getDataQueryConnection();
        dqConn.startTransaction();
        try{
            t.transaction();
            dqConn.commitTransaction();
        }
        catch(Exception ex){
            dqConn.rollbackTransaction();
            throw ex;
        }
    }
    
}
