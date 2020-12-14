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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


/**
 *
 * @author rsgoss
 */
public class RdbmsUtils {
    
    private static final int GET=0;
    private static final int SET=1;
    
    protected final static String getSqlFields(HashMap<?,String> fieldMapping){
        String fields=null;
        for(String fieldName : fieldMapping.values()){
            if(fields==null){
                fields=fieldName;
            }
            else{
                fields+=","+fieldName;
            }
        }
        return fields;
    }
    
    protected final static void setParams(PreparedStatement st,Object[] params) throws SQLException{
        if(params!=null){
            for(int i=0;i<params.length;i++){
                if(params[i] instanceof java.util.Date){
                    st.setDate(i+1, new java.sql.Date(((java.util.Date)params[i]).getTime()));
                }
                else{
                    st.setObject(i+1,(Object)params[i]);
                }
            }
        }
    }
    
    
    public static void closeStatement(ResultSet rs, PreparedStatement st){
        if(rs!=null){
            try{
                rs.close();
            }
            catch(Exception ex){}
        }
        if(st!=null){
            try{
                st.close();
            }
            catch(Exception ex){}
        } 
    }
    
}
