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

package mil.army.usace.erdc.crrel.dataquery.connection.action;

import mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsDefaultSelect;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import mil.army.usace.erdc.crrel.dataquery.RecordsetCursor;

/**
 *
 * @author rsgoss
 */
public interface Select {
    
    enum JsonKeyCase{
        UPPER,
        LOWER,
        DEFAULT
    }
    
    enum ReturnType {
        HASHMAP,
        SINGLEVALUE,
        ARRAYLIST,
        ARRAYLIST_TUPLE,
        ARRAYLIST_TUPLE3,
        RECORDSET,
        RECORDSETMAP,
        JSON
    }
    
    <T> RecordsetCursor<T> getRecordsCursor(Class objClass,String command,boolean useDeclaredOnly, boolean isFullCommand,boolean closeConnection, Object[] params);
    <T> T getRecord(Class objClass,String criteria, boolean useDeclaredOnly, boolean isFullCommand, Object[] params);
    <T> List<T> getRecords(Class objClass,String command,boolean useDeclaredOnly, boolean isFullCommand, Object[] params);
    <T> List<T> getDbStructs(Class objClass,String command, boolean isFullCommand, Object[] params);
    <T> T getDbStruct(Class objClass,String command, boolean isFullCommand, Object[] params);
    <T> T getSingleSqlValue(String sql, Class returnType, Object[] params, boolean returnEmptyRecordsetAsNull);
    String getRecordsAsJson(String sql, RdbmsDefaultSelect.JsonKeyCase jsonKeyCase, Boolean useCamelCase, Boolean escapeHtml, SimpleDateFormat dateFormatter, Object... params);
    <T> List<T> getRecordsAsCollection(RdbmsDefaultSelect.ReturnType returnType, String sql, Class[] returnTypes, Object[] params);
    <T,S> HashMap<T,S> getRecordsAsHashmap(String sql,Object[] params, Class[] returnTypes);
    
}
