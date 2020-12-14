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

package mil.army.usace.erdc.crrel.dataquery.rdbms.implementations.oracle;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mil.army.usace.erdc.crrel.dataquery.ConnectionConfig;
import mil.army.usace.erdc.crrel.dataquery.rdbms.DatabaseDialect;

/**
 *
 * @author rsgoss
 */
public class OracleDialect implements DatabaseDialect{

    private final String serviceTemplate="jdbc:oracle:thin:@%s:%s/%s";
    private final String sidTemplate="jdbc:oracle:thin:@%s:%s:%s";
    private ConnectionType connectionType = ConnectionType.SERVICENAME;

    public enum ConnectionType{
        SID,
        SERVICENAME
    }

    public void setConnectionType(ConnectionType ct){
        this.connectionType=ct;
    }

    @Override
    public String getSequenceSql(String sequenceName){
        return String.format("%s.NEXTVAL",sequenceName);            
    }
    
    @Override
    public String getGuidSql() {
        return "sys_guid()";
    }

    @Override
    public Map<Class, Class> getSqlToNativeTypeMap() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Class, String> getNativeToDbTypeMap() {
        return new HashMap<Class, String>() {
            {
                put(Double.class, "NUMBER(38,16)");
                put(Float.class,  "NUMBER(38,8)");
                put(Long.class,"NUMBER(38,0)");
                put(Integer.class,"NUMBER(16,0)");
                put(String.class,"VARCHAR2(%d)");
                put(Date.class,"DATE");
                put(java.sql.Date.class,"DATE");
                put(java.sql.Timestamp.class,"TIMESTAMP");
            }
        };
    }

    /*
    command should return a count field of 'is_existing'
     */
    @Override
    public String getCheckTableSql(String tableName) {
        return String.format("select count(table_name) as is_existing from user_tables where table_name='%s'",tableName.toUpperCase());
    }

    @Override
    public String getJdbcUrl(ConnectionConfig config) {
        if(this.connectionType==ConnectionType.SERVICENAME){
            return String.format(this.serviceTemplate,config.host,config.port,config.instance);
        }
        else{
            return String.format(this.sidTemplate,config.host,config.port,config.instance);
        }
    }


}
