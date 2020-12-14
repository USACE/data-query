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

import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mil.army.usace.erdc.crrel.dataquery.DataQueryException;
import mil.army.usace.erdc.crrel.dataquery.annotations.Optional;
import mil.army.usace.erdc.crrel.dataquery.RecordsetCursor;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select;
import static mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsUtils.closeStatement;
import static mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsUtils.getSqlFields;
import static mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsUtils.setParams;
import mil.army.usace.erdc.crrel.dataquery.utils.DataQueryConverter;

import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.dbToFieldName;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getDbName;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getDbStructFieldMapping;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getEntitySchema;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getFieldMapping;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getSql;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getTableName;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.useCamelCase;
import mil.army.usace.erdc.crrel.dataquery.utils.Tuple;
import mil.army.usace.erdc.crrel.dataquery.utils.Tuple3;

/**
 *
 * @author rsgoss
 */
public class RdbmsDefaultSelect implements Select{
    
    private final RdbmsConnection rconn;
    private final DataQueryConverter converter;
    
    public RdbmsDefaultSelect(RdbmsConnection rconn,DataQueryConverter converter){
        this.rconn=rconn;
        this.converter=converter;
    }
    
    @Override
    public <T> RecordsetCursor<T> getRecordsCursor(Class objClass,String command,boolean useDeclaredOnly, boolean isFullCommand, boolean closeConnection, Object[] criteriaParams){        
        HashMap<Method,String> fieldMapping;
        if(isFullCommand){
            Boolean isCamelCased=useCamelCase(objClass);
            fieldMapping = getFieldMapping(objClass,RdbmsConnection.SET, isCamelCased, useDeclaredOnly);
        }
        else{
            Tuple<String,HashMap<Method,String>> sqlCommand=prepareSelectCommand(objClass,command,useDeclaredOnly);
            command=sqlCommand.getA();
            fieldMapping=sqlCommand.getB();
        }
        return commandToCursor(objClass,command,fieldMapping,closeConnection,criteriaParams);
    }
    
    @Override
    public <T> List<T> getDbStructs(Class objClass,String command, boolean isFullCommand, Object[] params){
        HashMap<Field,String> fieldMapping;
        if(isFullCommand){
            Boolean isCamelCased=useCamelCase(objClass);
            fieldMapping = getDbStructFieldMapping(objClass);
        }
        else{
            Tuple<String,HashMap<Field,String>> sqlCommand=prepareDbStructSelectCommand(objClass,command);
            command=sqlCommand.getA();
            fieldMapping=sqlCommand.getB();
        }
        return commandToDbStruct(objClass,command,fieldMapping,params);
    }
    
    @Override
    public <T> T getDbStruct(Class objClass,String command, boolean isFullCommand, Object[] params){
        HashMap<Field,String> fieldMapping;
        if(isFullCommand){
            Boolean isCamelCased=useCamelCase(objClass);
            fieldMapping = getDbStructFieldMapping(objClass);
        }
        else{
            Tuple<String,HashMap<Field,String>> sqlCommand=prepareDbStructSelectCommand(objClass,command);
            command=sqlCommand.getA();
            fieldMapping=sqlCommand.getB();
        }
        List<T> records=commandToDbStruct(objClass,command,fieldMapping,params);
        if(records.size()>0)
            return records.get(0);
        else
            return null;
    }
    
    @Override
    public <T> List<T> getRecords(Class objClass,String command,boolean useDeclaredOnly, boolean isFullCommand, Object[] params){
        HashMap<Method,String> fieldMapping;
        if(isFullCommand){
            Boolean isCamelCased=useCamelCase(objClass);
            fieldMapping = getFieldMapping(objClass,RdbmsConnection.SET, isCamelCased, useDeclaredOnly);
        }
        else{
            Tuple<String,HashMap<Method,String>> sqlCommand=prepareSelectCommand(objClass,command,useDeclaredOnly);
            command=sqlCommand.getA();
            fieldMapping=sqlCommand.getB();
        }
        return commandToRecords(objClass,command,fieldMapping,params);
    }

    @Override
    public <T> T getRecord(Class objClass,String criteria, boolean useDeclaredOnly, boolean isFullCommand, Object[] criteriaParams){
        List<T> records=getRecords(objClass,criteria,useDeclaredOnly,isFullCommand, criteriaParams);
        if(records.size()==1){
            return records.get(0);
        }
        else if(records.size()==0){
            return null;
        }
        else{
            throw new RuntimeException("Query returned more than 1 record.");
        }
    }
       
    @Override
    public <T> T getSingleSqlValue(String sql, Class returnType, Object[] params, boolean returnEmptyRecordsetAsNull){
        PreparedStatement st=null;
        ResultSet rs=null;
        try{
            st = rconn.getConnection().prepareStatement(sql);
            setParams(st,params);
            rs = st.executeQuery();
            Object val=null;
            //if there are no records, check returnEmptyRecordsetIsNull boolean 
            //and allow error to be thrown if returnEmptyRecordsetIsNull==false
            //fyi...returnEmptyRecordsetIsNull==false is the public behavior
            if(rs.next() || !returnEmptyRecordsetAsNull){ 
                val=rs.getObject(1);
            }
            if(val==null){
                return null;
            }
            else{
                if(returnType!=null)
                    return (T)DataQueryConverter.convert(rs.getObject(1),returnType);
                else
                    return (T)rs.getObject(1);
            }
        }
        catch(Exception ex){
            throw new DataQueryException(sql,null,ex);
        }
        finally{
            closeStatement(rs,st);
        }
    }
    
    @Override
    public String getRecordsAsJson(String sql, JsonKeyCase jsonKeyCase, Boolean useCamelCase, Boolean escapeHtml, SimpleDateFormat dateFormatter, Object... params){
        PreparedStatement st=null;
        ResultSet rs=null;
        ResultSetMetaData rsMetaData=null;
        Gson gson = new Gson();
        try{
            StringBuilder stringBuilder=null;
            stringBuilder = new StringBuilder("[");
            Connection conn=rconn.getConnection();
            st = conn.prepareStatement(sql,java.sql.ResultSet.TYPE_FORWARD_ONLY);
            setParams(st,params);
            rs = st.executeQuery();
            rsMetaData=rs.getMetaData();
            while (rs.next()) {      
                stringBuilder.append("{");
                for(int i=1;i<=rsMetaData.getColumnCount();i++){
                    String attrName;
                    switch(jsonKeyCase){
                        case UPPER:
                            attrName=rsMetaData.getColumnName(i).toUpperCase();
                            break;
                        case LOWER:
                            attrName=rsMetaData.getColumnName(i).toLowerCase();
                            break;
                        default:
                            attrName=rsMetaData.getColumnName(i);                            
                    }
                    String test=dbToFieldName(attrName);
                    stringBuilder.append("\"")
                                 .append((useCamelCase)?dbToFieldName(attrName):attrName)
                                 .append("\":");
                    Object val=rs.getObject(i);
                    if(val==null){
                        stringBuilder.append("null,");
                    }
                    else if(val instanceof Number){
                        stringBuilder.append(val.toString()).append(",");
                    }
                    else if(val instanceof java.sql.Date){
                        stringBuilder.append("\"").append(dateFormatter.format(new java.util.Date(((java.sql.Date)val).getTime()))).append("\",");
                    }
                    else if(val instanceof java.sql.Timestamp){
                        stringBuilder.append("\"").append(dateFormatter.format(new java.util.Date(((java.sql.Timestamp)val).getTime()))).append("\",");
                    }
                    else{
                        if(escapeHtml)
                            stringBuilder.append(gson.toJson(rs.getObject(i).toString())).append(",");
                        else
                            stringBuilder.append("\"").append(rs.getObject(i).toString()).append("\",");
                    }                       
                }
                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                stringBuilder.append("},");                       
            }
            if(stringBuilder.length()>1)
                stringBuilder.deleteCharAt(stringBuilder.length()-1);
            stringBuilder.append("]");
            return stringBuilder.toString();   
        }
        catch(Exception ex){
            throw new DataQueryException(sql,null,ex);
        }
        finally{
            closeStatement(rs,st);
        }
    
    }
    
    @Override
    public <T> List<T> getRecordsAsCollection(ReturnType returnType, String sql, Class[] returnTypes, Object[] params){
        if(returnTypes.length==0) returnTypes=null;
        PreparedStatement st=null;
        ResultSet rs=null;
        ResultSetMetaData rsMetaData=null;
        try{
            List<T> records = new ArrayList<>();
            Connection conn=rconn.getConnection();
            st = conn.prepareStatement(sql);
            setParams(st,params);
            rs = st.executeQuery();
            if(returnType==ReturnType.RECORDSET || returnType==ReturnType.RECORDSETMAP)
                rsMetaData=rs.getMetaData();
            while (rs.next()) {
                switch(returnType){
                    case ARRAYLIST:
                        if(returnTypes!=null)
                            records.add((T)DataQueryConverter.convert(rs.getObject(1),returnTypes[0]));
                        else
                            records.add((T)rs.getObject(1));
                        break;
                    case ARRAYLIST_TUPLE:
                        if(returnTypes!=null)
                            records.add((T)new Tuple(DataQueryConverter.convert(rs.getObject(1),returnTypes[0]),DataQueryConverter.convert(rs.getObject(2),returnTypes[1])));
                        else
                            records.add((T)new Tuple(rs.getObject(1),rs.getObject(2)));
                        break;
                    case ARRAYLIST_TUPLE3:
                        if(returnTypes!=null)
                            records.add((T)new Tuple3(DataQueryConverter.convert(rs.getObject(1),returnTypes[0]),DataQueryConverter.convert(rs.getObject(2),returnTypes[1]),DataQueryConverter.convert(rs.getObject(3),returnTypes[2])));
                        else
                            records.add((T)new Tuple3(rs.getObject(1),rs.getObject(2),rs.getObject(3)));
                        break;
                    case RECORDSET:
                        ArrayList record = new ArrayList();
                        for(int i=0;i<rsMetaData.getColumnCount();i++){
                            record.add((returnTypes==null)?rs.getObject(i+1):DataQueryConverter.convert(rs.getObject(i+1),returnTypes[i]));
                        }
                        ((ArrayList)records).add(record);
                        break;
                    case RECORDSETMAP:
                        HashMap<String,Object> recordmap = new HashMap<>();
                        for(int i=0;i<rsMetaData.getColumnCount();i++){
                            recordmap.put(rsMetaData.getColumnName(i+1),(returnTypes==null)?rs.getObject(i+1):DataQueryConverter.convert(rs.getObject(i+1),returnTypes[i]));
                        }
                        ((ArrayList)records).add(recordmap);
                        break;
                }
            }
            return records;
        }
        catch(Exception ex){
            throw new DataQueryException(sql,null,ex);
        }
        finally{
            closeStatement(rs,st);
        }
    }
    
   
    @Override
    public <T,S> HashMap<T,S> getRecordsAsHashmap(String sql,Object[] params, Class[] returnTypes){
        PreparedStatement st=null;
        ResultSet rs=null;
        ResultSetMetaData rsMetaData=null;
        try{
            HashMap<T,S> records = new HashMap<>();
            Connection conn=rconn.getConnection();
            st = conn.prepareStatement(sql);
            setParams(st,params);
            rs = st.executeQuery();
            while (rs.next()) { 
                if(returnTypes!=null)
                    records.put((T)DataQueryConverter.convert(rs.getObject(1),returnTypes[0]),(S)DataQueryConverter.convert(rs.getObject(2),returnTypes[1]));
                else
                    records.put((T)rs.getObject(1),(S)rs.getObject(2)); 
            }
            return records;
        }
        catch(Exception ex){
            throw new DataQueryException(sql,null,ex);
        }
        finally{
            closeStatement(rs,st);
        }
        
    }
    

        
    private Tuple<String,HashMap<Method,String>>  prepareSelectCommand(Class objClass,String criteria,boolean useDeclaredOnly){
        String command=null;
        String schema=getEntitySchema(objClass);
        Boolean isCamelCased=useCamelCase(objClass);
        HashMap<Method,String> fieldMapping = getFieldMapping(objClass,RdbmsConnection.SET, isCamelCased, useDeclaredOnly);
        command=getSql(objClass);
        if(command==null){
            String tableName=getTableName(objClass);
            if(tableName==null)
                tableName=getDbName(isCamelCased,objClass.getSimpleName(),null);
            command="select "+getSqlFields(fieldMapping)+" from "+ ((schema==null)?"":(schema+"."))+tableName;
        }
        if(criteria!=null && (!criteria.equals(""))){
            command+=" "+criteria;
        }
        return new Tuple<>(command,fieldMapping);
    }

    private Tuple<String,HashMap<Field,String>>  prepareDbStructSelectCommand(Class objClass,String criteria){
        String command=null;
        String schema=getEntitySchema(objClass);
        Boolean isCamelCased=useCamelCase(objClass);
        HashMap<Field,String> fieldMapping = getDbStructFieldMapping(objClass);
        command=getSql(objClass);
        if(command==null){
            String tableName=getTableName(objClass);
            if(tableName==null)
                tableName=getDbName(isCamelCased,objClass.getSimpleName(),null);
            command="select "+getSqlFields(fieldMapping)+" from "+ ((schema==null)?"":(schema+"."))+tableName;
        }
        if(criteria!=null && (!criteria.equals(""))){
            command+=" "+criteria;
        }
        return new Tuple<>(command,fieldMapping);
    }

    private <T> RecordsetCursor<T> commandToCursor(Class objClass,String command,HashMap<Method,String> fieldMapping, boolean closeConnection, Object[] params){
        PreparedStatement st=null;
        ResultSet rs=null;
        try{
            Connection conn=rconn.getConnection();
            st = conn.prepareStatement(command);
            setParams(st,params);
            rs = st.executeQuery();
            RecordsetCursor<T> stream= new RecordsetCursor(command, objClass, fieldMapping, st, rs, conn,this.converter,closeConnection);
            return stream;
        }
        catch(Exception ex){
            closeStatement(rs, st);
            if(ex instanceof DataQueryException)
                throw (DataQueryException)ex;
            else
                throw new DataQueryException(command,null,ex);

        }
    }
    
    private <T> List<T> commandToDbStruct(Class dbStructClazz, String command, HashMap<Field,String> fieldMapping, Object[] params){
        PreparedStatement st=null;
        ResultSet rs=null;        
        try{
            ArrayList<T> records = new ArrayList();
            Connection conn=rconn.getConnection();
            st = conn.prepareStatement(command);
            setParams(st,params);
            rs = st.executeQuery();
            while (rs.next()) {
                T newObj = (T)dbStructClazz.newInstance();
                for(Field field:fieldMapping.keySet()){
                    try{
                        Object val=converter.convert(rs.getObject((String)fieldMapping.get(field)),field);
                        if(val!=null)
                            field.set(newObj,val);
                    }
                    catch(Exception ex){
                        throw new DataQueryException(command,"Unable to map field:"+field.getName(),ex);
                    }
                }
                records.add(newObj);
            }
            return records;
        }
        catch(Exception ex){
            if(ex instanceof DataQueryException)
                throw (DataQueryException)ex;
            else
                throw new DataQueryException(command,null,ex);            
        }
        finally{
            closeStatement(rs, st);
        }
    }
    
    private <T> List<T> commandToRecords(Class objClass, String command, HashMap<Method,String> fieldMapping, Object[] params){
        PreparedStatement st=null;
        ResultSet rs=null;
        try{
            ArrayList<T> records = new ArrayList();
            Connection conn=rconn.getConnection();
            st = conn.prepareStatement(command);
            setParams(st,params);
            rs = st.executeQuery();
            while (rs.next()) {
                T newObj = (T)objClass.newInstance();
                for(Method method:fieldMapping.keySet()){
                    try{
                        Object val=converter.convert(rs.getObject((String)fieldMapping.get(method)),method);
                        if(val!=null)
                            method.invoke(newObj,val);
                    }
                    catch(Exception ex){
                        if(!method.isAnnotationPresent(Optional.class)){
                            throw new DataQueryException(command,"Unable to map method:"+method.getName(),ex);
                        }     
                    }
                }
                records.add(newObj);
            }
            return records;
        }
        catch(Exception ex){
            if(ex instanceof DataQueryException)
                throw (DataQueryException)ex;
            else
                throw new DataQueryException(command,null,ex);            
        }
        finally{
            closeStatement(rs, st);
        }
    }
    
    
    
}
