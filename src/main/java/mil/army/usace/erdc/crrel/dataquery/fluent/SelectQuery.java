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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import mil.army.usace.erdc.crrel.dataquery.RecordsetCursor;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select.JsonKeyCase;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select.ReturnType;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.isDbStruct;
import mil.army.usace.erdc.crrel.dataquery.utils.Tuple;

/**
 *
 * @author k3endrsg
 */
public class SelectQuery {
    
    private final Select select;
    private Class returnTypeClass=null;
    private ReturnType returnType=null;
    private Class[] returnedTypes=new Class[]{};
    private String sql=null;
    private String criteria=null;
    private Object[] params=null;
    private boolean useDeclaredOnly=false;
    private JsonKeyCase jsonKeyCase=JsonKeyCase.DEFAULT;
    private Boolean useCamelCase=true; //used only by JSON return type
    private Boolean escapeHtml=true;
    private SimpleDateFormat dateFormat= new SimpleDateFormat("MM/dd/yyyy");
    private Boolean returnEmptyRecordsetAsNull=false;
    private boolean closeConnection=true;
    
    
    public SelectQuery(Select s, Class clazz){
        this.select=s;
        this.returnTypeClass=clazz;
    }
    
    public SelectQuery(Select nrq, ReturnType returnType){
        this.select=nrq;
        this.returnType=returnType;
    }
    
    public SelectQuery sql(String sql){
        this.sql=sql;
        return this;
    }
    
    public SelectQuery criteria(String criteria){
        this.criteria=criteria;
        return this;
    }
    
    public SelectQuery jsonKeyCase(JsonKeyCase jsonKeyCase){
        this.jsonKeyCase=jsonKeyCase;
        return this;
    }
    
    public SelectQuery useCamelCase(Boolean useCamelCase){
        this.useCamelCase=useCamelCase;
        return this;
    }
    
    public SelectQuery escapeHtml(Boolean escapeHtml){
        this.escapeHtml=escapeHtml;
        return this;
    }
    
    public SelectQuery returnEmptyRecordsetAsNull(Boolean returnEmptyRecordsetAsNull){
        this.returnEmptyRecordsetAsNull=returnEmptyRecordsetAsNull;
        return this;
    }
    
    public SelectQuery dateFormat(SimpleDateFormat dateFormat){
        this.dateFormat=dateFormat;
        return this;
    }
    
    public SelectQuery params(Object... params){
        this.params=params;
        return this;
    }
    
    public SelectQuery useDeclaredOnly(boolean useDeclaredOnly){
        this.useDeclaredOnly=useDeclaredOnly;
        return this;
    }
    
    public SelectQuery closeConnection(boolean closeConnection){
        this.closeConnection=closeConnection;
        return this;
    }
    
    public SelectQuery returnedTypes(Class... returnedTypes){
        this.returnedTypes=returnedTypes;
        return this;
    }
    
    public <T> T fetchRow(){
            //Tuple<String,Boolean> commandTuple=getCommand();
            //return rdq.getRecord(returnTypeClass, commandTuple.getA(), useDeclaredOnly, commandTuple.getB(), params);
        return this.fetchRowFromClass();
    }
    
    public <T> T fetchValue(){
        if(returnType==ReturnType.JSON){
            return (T)select.getRecordsAsJson(sql,jsonKeyCase,useCamelCase,escapeHtml,dateFormat,params);
        }
        else{
            return select.getSingleSqlValue(sql, returnedTypes[0], params,returnEmptyRecordsetAsNull);
        }
    }
    
    public <T,S> HashMap<T,S> fetchMap(){
        return select.getRecordsAsHashmap(sql, params, returnedTypes);
    }
    
    //public String fetchJson(){
    //    return nrq.getRecordsAsJson(sql, params);
    //}
    
    public <T> List<T> fetch(){
        if(returnTypeClass!=null){
            return fetchRowsFromClass();
        }
        else{
            return fetchRowsFromSimpleCollection();
        }
    }
    
    public <T> RecordsetCursor<T> fetchCursor(){
        Tuple<String,Boolean> commandTuple=getCommand();
        return select.getRecordsCursor(returnTypeClass, commandTuple.getA(), useDeclaredOnly, commandTuple.getB(),closeConnection, params);
    }
    
    ////////////////////////////////////////////////////////
    private Tuple<String,Boolean> getCommand(){
        boolean isFullSqlCommand=false;
        String command=criteria;
        if(sql!=null){
            isFullSqlCommand=true;
            command=sql;
        }
        return new Tuple(command,isFullSqlCommand);
    }
    
    private <T> List<T> fetchRowsFromSimpleCollection(){
        return select.getRecordsAsCollection(returnType, sql, returnedTypes, params);
    }
    
    private <T> T fetchRowFromClass(){
        if(isDbStruct(returnTypeClass)){
            Tuple<String,Boolean> commandTuple=getCommand();
            return select.getDbStruct(returnTypeClass, commandTuple.getA(), commandTuple.getB(),params);
        }
        else{
            Tuple<String,Boolean> commandTuple=getCommand();
            return select.getRecord(returnTypeClass, commandTuple.getA(), useDeclaredOnly, commandTuple.getB(), params);
        }
    }
    
    private <T> List<T> fetchRowsFromClass(){
        if(isDbStruct(returnTypeClass)){
            Tuple<String,Boolean> commandTuple=getCommand();
            return select.getDbStructs(returnTypeClass, commandTuple.getA(), commandTuple.getB(),params);
        }
        else{
            Tuple<String,Boolean> commandTuple=getCommand();
            return select.getRecords(returnTypeClass, commandTuple.getA(), useDeclaredOnly, commandTuple.getB(), params);
        }
    }
    
    private <T> T fetchSingleValueRow(){
        return null;
    }
    
    
}
