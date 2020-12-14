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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mil.army.usace.erdc.crrel.dataquery.DataQueryException;
import mil.army.usace.erdc.crrel.dataquery.GeneratedKey;
import mil.army.usace.erdc.crrel.dataquery.annotations.GeneratedValue;
import mil.army.usace.erdc.crrel.dataquery.annotations.GenerationType;
import mil.army.usace.erdc.crrel.dataquery.annotations.Id;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Dml;
import static mil.army.usace.erdc.crrel.dataquery.rdbms.RdbmsUtils.setParams;
import mil.army.usace.erdc.crrel.dataquery.utils.DataQueryConverter;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.dbToFieldName;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getDbDataType;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getDbName;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getEntitySchema;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getFieldMapping;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getIdFieldName;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getTableName;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.useCamelCase;

/**
 *
 * @author rsgoss
 */
public class RdbmsDefaultDml implements Dml{
    
    private final RdbmsConnection rconn;
    private int batchSize=100;
    private boolean useBatch=true;
    private final DatabaseDialect dbDialect;
    
    
    public RdbmsDefaultDml(RdbmsConnection rconn,DatabaseDialect dbDialect){
        this.rconn=rconn;
        this.dbDialect=dbDialect;
    }
    
    @Override
    public void setBatchSize(int batchSize){
        this.batchSize=batchSize;
    }
    
    @Override
    public int getBatchSize(){
        return this.batchSize;
    }
    
    @Override
    public void setUseBatching(boolean useBatch){
        this.useBatch=useBatch;
    }
    
    @Override
    public boolean getUseBatching(){
        return this.useBatch;
    }

    @Override
    public DatabaseDialect getDialect() {
        return this.dbDialect;
    }


    private enum DML{
        INSERT,
        UPDATE,
        DELETE
    }
    
    //@Override
    /*
    public void insertRecord(Object record) {
        ArrayList records = new ArrayList();
        records.add(record);
        insertRecords(records,false,null);
    }
    */
    
    
    @Override
    public GeneratedKey insertRecord(Object record, boolean useDeclaredOnly,boolean returnGeneratedKey,String tableName) {
        ArrayList records = new ArrayList();
        if(returnGeneratedKey){
            return insertWithGeneratedKeyReturn(record, useDeclaredOnly);
        }
        else{
            records.add(record);
            insertRecords(records,useDeclaredOnly,tableName);
            return null;
        }
    }
    
    @Override
    public void insertRecords(List records, boolean useDeclaredOnly, String tableName){
        runDml(DML.INSERT,records,useDeclaredOnly,null,tableName);
    }
    
    @Override
    public void updateRecords(List records, boolean useDeclaredOnly, List<String> includeFields, String tableName){
        runDml(DML.UPDATE,records,useDeclaredOnly,includeFields,tableName);
    }
    
    @Override
    public void updateRecord(Object record, boolean useDeclaredOnly, String tableName){
        ArrayList records = new ArrayList();
        records.add(record);
        updateRecords(records,useDeclaredOnly,null,tableName);
    }
    
    @Override
    public void deleteRecord(Object record, String tableName){
        List records = new ArrayList();
        records.add(record);
        deleteRecords(records, tableName);
    }
    
    @Override
    public void deleteRecords(List records, String tableName){
        runDml(DML.DELETE,records,false,null, tableName);
    }

   
    @Override
    public int deleteRecords(Class objClass,String criteria, Object... criteriaParams){
        PreparedStatement st=null;
        boolean inTrans=rconn.inTransaction();
        if(!inTrans)
            rconn.startTransaction();
        try{
            String schema=getEntitySchema(objClass);
            Boolean isCamelCased=useCamelCase(objClass);
            String tableName=getTableName(objClass);
            if(tableName==null)
                tableName=((schema==null)?"":(schema+"."))+getDbName(isCamelCased,objClass.getSimpleName(),null);
            String sql="delete from " + tableName;
            if(criteria!=null && (!criteria.equals(""))){
                sql+=" "+criteria;
            }
            st = rconn.getConnection().prepareStatement(sql);
            setParams(st,criteriaParams);
            int rowsDeleted = st.executeUpdate();
            if(!inTrans)
                rconn.commitTransaction();
            return rowsDeleted;
        }
        catch(Exception ex){
            ex.printStackTrace();
            if(!inTrans)
                rconn.rollbackTransaction();
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
    
    private GeneratedKey insertWithGeneratedKeyReturn(Object record,boolean useDeclaredOnly){
        PreparedStatement st=null;
        boolean inTrans=rconn.inTransaction();
        String command=null;
        int batchCount=0;
        GeneratedKey gk = null;
        if(!inTrans)
            rconn.startTransaction();
        try{
            Class objClass = record.getClass();
            String schema=getEntitySchema(objClass);
            Boolean isCamelCased=useCamelCase(objClass);
            HashMap<Method,String> fieldMapping = getFieldMapping(objClass,rconn.GET,isCamelCased, useDeclaredOnly);
            String idFieldName=getIdFieldName(fieldMapping);
            HashMap<Integer,Method> indexMapping = new HashMap();
            String tableName=getTableName(objClass);
            if(tableName==null)
                tableName=getDbName(isCamelCased,objClass.getSimpleName(),null);
            command=getInsertCommand(tableName, schema, fieldMapping, indexMapping);
            Connection conn=rconn.getConnection();
            if(idFieldName!=null){
                st = conn.prepareStatement(command, new String[]{idFieldName});
            }
            else{
                st = conn.prepareStatement(command);
            }
            for(int index : indexMapping.keySet()){ //@TODO same block of code in both insert and insert with key return.
                Method method = indexMapping.get(index);
                Class dbDataType = getDbDataType(method);
                Object value;
                if(dbDataType!=null){
                    value = DataQueryConverter.convert(indexMapping.get(index).invoke(record,null),dbDataType); 
                }
                else{
                    value = indexMapping.get(index).invoke(record,null);
                }
                if(value instanceof java.util.Date){
                    value=new java.sql.Date(((java.util.Date)value).getTime());
                }
                st.setObject((Integer)index,value);
            }
            st.execute();
            ResultSet rs = st.getGeneratedKeys();
            if(rs.next()){
                gk = new GeneratedKey(idFieldName,rs.getObject(1));
            }     
            if(!inTrans)
                rconn.commitTransaction();
            return gk;
        }
        catch(Exception ex){
            ex.printStackTrace();
            if(!inTrans)
                rconn.rollbackTransaction();
            throw new DataQueryException(command,"insertWithGeneratedKeyReturn",ex);
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
    
    //@TODO prepare DML methods for DbStruct
    private void runDml(DML dmlType,List records, boolean useDeclaredOnly, List<String> includeFields, String tableName){
        PreparedStatement st=null;
        boolean inTrans=this.rconn.inTransaction();
        int batchCount=0;
        String command=null;
        if(!inTrans)
            this.rconn.startTransaction();
        try{
            Object obj = records.get(0);
            Class objClass = obj.getClass();
            String schema=getEntitySchema(objClass);
            Boolean isCamelCased=useCamelCase(objClass);
            HashMap<Method,String> fieldMapping = getFieldMapping(objClass,rconn.GET,isCamelCased, useDeclaredOnly);
            HashMap<Integer,Method> indexMapping = new HashMap();
            if(tableName==null){
                tableName=getTableName(objClass);
                if(tableName==null){
                    tableName=getDbName(isCamelCased,objClass.getSimpleName(),null);
                }
            }   
            if(dmlType==DML.UPDATE)
                command=getUpdateCommand(tableName, schema, fieldMapping, indexMapping, includeFields);
            else if(dmlType==DML.INSERT)
                command=getInsertCommand(tableName, schema, fieldMapping, indexMapping);
            else
                command=getDeleteCommand(tableName,schema,fieldMapping,indexMapping);
            
            Connection conn=rconn.getConnection();
            st = conn.prepareStatement(command);
            for(Object record : records){
                for(int index : indexMapping.keySet()){
                    Method method = indexMapping.get(index);
                    Class dbDataType = getDbDataType(method);
                    Object value;
                    if(dbDataType!=null){
                        value = DataQueryConverter.convert(indexMapping.get(index).invoke(record,null),dbDataType); 
                    }
                    else{
                        value = indexMapping.get(index).invoke(record,null);
                    }
                    if(value instanceof java.util.Date){
                        value=new java.sql.Date(((java.util.Date)value).getTime());
                    }
                    st.setObject((Integer)index,value);
                }
                
                if (useBatch==true) 
                    st.addBatch();
                else 
                    st.executeUpdate();
                
                if(useBatch==true && ++batchCount%batchSize==0){
                    st.executeBatch();
                }
            }
            if(useBatch==true)
                st.executeBatch();  //flush out remaining records
            if(!inTrans)
                rconn.commitTransaction();
        }
        catch(Exception ex){
            ex.printStackTrace();
            if(!inTrans)
                rconn.rollbackTransaction();
            throw new DataQueryException(command,"runDml",ex);
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
    
    private String getDeleteCommand(String tableName, String schema, HashMap<Method,String> fieldMapping, HashMap<Integer,Method> indexMapping){
        String command="delete from " +  ((schema==null)?"":(schema+".")) +tableName;
        String fields="";
        int paramIndex=1;
        String pkField=null;

        Method pkFieldMethod=null;
        for(Method dbFieldMethod : fieldMapping.keySet()){
            if(dbFieldMethod.isAnnotationPresent(Id.class)){
                pkFieldMethod=dbFieldMethod;
                pkField=(String)fieldMapping.get(pkFieldMethod);
            }
        }
        if(pkFieldMethod==null){
            throw new DataQueryException(String.format("Missing Entity Primary Key Field for %s",tableName));
        }
        indexMapping.put(paramIndex,pkFieldMethod);
        command+=" where "+pkField+" = ?";
        return command;
    }
    
    private String getUpdateCommand(String tableName, String schema, HashMap<Method,String> fieldMapping, HashMap<Integer,Method> indexMapping, List<String> includeFields){
        String command="update " +  ((schema==null)?"":(schema+".")) +tableName + " set ";
        String fields="";
        int paramIndex=1;
        String pkField=null;

        Method pkFieldMethod=null;
        for(Method dbFieldMethod : fieldMapping.keySet()){
            if(dbFieldMethod.isAnnotationPresent(Id.class)){
                pkFieldMethod=dbFieldMethod;
                pkField=(String)fieldMapping.get(pkFieldMethod);
            }
            else{
                String field=(String)fieldMapping.get(dbFieldMethod);
                if(includeFields!=null){
                    String incField=dbToFieldName(field);
                    if(includeFields.contains(incField)){
                        fields+=field+"=?,";
                        indexMapping.put(paramIndex,dbFieldMethod);
                        paramIndex++;
                    }
                }
                else{
                    fields+=field+"=?,";
                    indexMapping.put(paramIndex,dbFieldMethod);
                    paramIndex++;
                }
                
            }
        }
        if(pkFieldMethod==null){
            throw new DataQueryException(String.format("Missing Entity Primary Key Field for %s",tableName));
        }
        indexMapping.put(paramIndex,pkFieldMethod); //@TODO if there is no pkfield..throw exception!
        fields=fields.substring(0,fields.length()-1);
        command+=fields + " where "+pkField+" = ?";
        return command;
    }
    
    
    
    private String getInsertCommand(String tableName,String schema,HashMap<Method,String> fieldMapping,HashMap<Integer, Method> indexMapping){
        String command="insert into " + ((schema==null)?"":(schema+"."))+tableName;
        String fields="(";
        String values="(";
        int paramIndex=1;

        for(Method dbFieldMethod : fieldMapping.keySet()){
            String fieldName=fieldMapping.get(dbFieldMethod);
            if(dbFieldMethod.isAnnotationPresent(Id.class)){
                if(dbFieldMethod.isAnnotationPresent(GeneratedValue.class)){
                    //generally do nothing here..simply omit pk field..
                    GeneratedValue gv = dbFieldMethod.getAnnotation(GeneratedValue.class);
                    fields+=fieldName+",";
                    if(gv.strategy()==GenerationType.AUTO){
                        values+=((schema==null)?"":(schema+"."))+dbDialect.getSequenceSql("seq_"+tableName)+",";
                    }
                    else if(gv.strategy()==GenerationType.GUID){
                        values+=((schema==null)?"":(schema+"."))+dbDialect.getGuidSql()+",";
                    }
                    else{
                        values+=((schema==null)?"":(schema+"."))+dbDialect.getSequenceSql(gv.generator())+",";
                    }
                }
                else{
                    fields+=fieldName+",";
                    values+="?,";
                    indexMapping.put(paramIndex,dbFieldMethod);
                    paramIndex++;                              
                }
            }
            else{
                fields+=fieldName+",";
                values+="?,";
                indexMapping.put(paramIndex,dbFieldMethod);
                paramIndex++;                    
            }
        }

        fields=fields.substring(0,fields.length()-1)+")";
        values=values.substring(0,values.length()-1)+")";
        command+=fields+" values "+ values;
        return command;
    }
    
    
    
}
