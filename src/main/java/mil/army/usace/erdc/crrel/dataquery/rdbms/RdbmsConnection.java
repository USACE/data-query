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

package mil.army.usace.erdc.crrel.dataquery.rdbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import mil.army.usace.erdc.crrel.dataquery.connection.DataQueryConnection;
import static mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsUtils.setParams;

/**
 *
 * @author rsgoss
 */
public class RdbmsConnection implements DataQueryConnection{
    
    private final Connection conn;
    
    public RdbmsConnection(Connection conn){
        this.conn=conn;
    }


    @Override
    public Connection getConnection(){
        return this.conn;
    }

    @Override
    public void close(){
        try{
            conn.close();
        }
        catch(SQLException ex){
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void startTransaction(){
        try{
            conn.setAutoCommit(false);
        }
        catch(SQLException ex){
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void commitTransaction(){
        try{
            conn.commit();
            conn.setAutoCommit(true);
        }
        catch(SQLException ex){
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void rollbackTransaction(){
        try{
            conn.rollback();
        }
        catch(SQLException ex){
            throw new RuntimeException(ex);
        }
    }


    @Override
    public boolean inTransaction(){
        try{
            return (conn.getAutoCommit())?false:true;
        }
        catch(SQLException ex){
            throw new RuntimeException(ex);
        }
    } 
    
    
    @Override
    public int executeCommand(String sql,Object... params){
        PreparedStatement st=null;
        try{
            st = conn.prepareStatement(sql);
            setParams(st,params);
            int rowsAffected = st.executeUpdate();
            return rowsAffected;
        }
        catch(SQLException ex){
            throw new RuntimeException(ex);
        }
        finally{
            if(st!=null){
                try{
                    st.close();
                }
                catch(Exception ex){}
            }
        }
    }
    
}
