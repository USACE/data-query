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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mil.army.usace.erdc.crrel.dataquery.annotations.Optional;
import mil.army.usace.erdc.crrel.dataquery.rdbms.Record;
import mil.army.usace.erdc.crrel.dataquery.utils.DataQueryConverter;


/**
 *
 * @author rsgoss
 * @param <T>
 */
public class RecordsetCursor<T> implements Iterable<T>, AutoCloseable{
    private final PreparedStatement st;
    private final ResultSet rs;
    private final Connection conn;
    private final Class objClass;
    private final HashMap<Method,String> fieldMapping;
    private final String command;
    private final DataQueryConverter converter;
    private final boolean closeConnection;
    
    public RecordsetCursor(String command, Class objClass, HashMap<Method,String> fieldMapping, PreparedStatement st, ResultSet rs, Connection conn, DataQueryConverter converter,boolean closeConnection) throws SQLException{
        this.command=command;
        this.converter=converter;
        this.objClass=objClass;
        this.rs =rs;
        this.fieldMapping=fieldMapping;
        this.st=st;
        this.conn=conn;
        this.closeConnection=closeConnection;
        Logger.getLogger(RecordsetCursor.class.getName()).log(Level.INFO, String.format("Opened Cursor for %s -> %s",this.toString(),this.conn.toString()));
    }
    
    public ResultSetMetaData getMetadata(){
        try{
            return rs.getMetaData();
        }
        catch(SQLException ex){
            Logger.getLogger(RecordsetCursor.class.getName()).log(Level.SEVERE, null, ex);
            throw new DataQueryException("Unable to get result set metadata: ",ex);    
        }
    }

    public Connection getConnection(){
        return this.conn;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> iterator = new Iterator<T>(){
            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (SQLException ex) {
                    Logger.getLogger(RecordsetCursor.class.getName()).log(Level.SEVERE, null, ex);
                }
                return false;
            }

            @Override
            public T next() {
                try{
                    if(Record.class.isAssignableFrom(objClass)){
                        return (T) new Record(rs);
                    }
                    else{
                        T newObj = (T)objClass.newInstance();
                        for(Method method:fieldMapping.keySet()){
                            try{
                                Object val=converter.convert(rs.getObject((String)fieldMapping.get(method)),method);
                                if(val!=null)
                                    method.invoke(newObj,val);
                                }
                            catch(Exception ex){
                                if(!method.isAnnotationPresent(Optional.class)){
                                    throw new DataQueryException(command,method.getName(),ex);
                                }     
                            }
                        }
                        return newObj;
                    }
                }
                catch(InstantiationException | IllegalAccessException | DataQueryException ex){
                    if(ex instanceof DataQueryException)
                        throw (DataQueryException)ex;
                    else
                        throw new DataQueryException("Iterator Error:"+command,null,ex);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported."); 
            }
        };
        return iterator;
    }

    @Override
    public void close() throws SQLException{
        if(rs!=null)
            rs.close();
        if(st!=null)
            st.close();
        if(closeConnection){
            if(conn!=null){
                conn.close();
                Logger.getLogger(RecordsetCursor.class.getName()).log(Level.INFO, String.format("Closed Cursor for %s",this.toString()));
            }
        }
        else{
            Logger.getLogger(RecordsetCursor.class.getName()).log(Level.INFO, String.format("Deferred Closing of Cursor for %s",this.toString()));
        }
    }
}
