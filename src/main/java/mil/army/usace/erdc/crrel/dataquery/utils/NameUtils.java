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

package mil.army.usace.erdc.crrel.dataquery.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import static mil.army.usace.erdc.crrel.dataquery.connection.DataQueryConnection.GET;
import mil.army.usace.erdc.crrel.dataquery.annotations.Column;
import mil.army.usace.erdc.crrel.dataquery.annotations.DbDataType;
import mil.army.usace.erdc.crrel.dataquery.annotations.DbStruct;
import mil.army.usace.erdc.crrel.dataquery.annotations.Entity;
import mil.army.usace.erdc.crrel.dataquery.annotations.Id;
import mil.army.usace.erdc.crrel.dataquery.annotations.SharepointValue;
import mil.army.usace.erdc.crrel.dataquery.annotations.SharepointValue.SPTYPE;
import mil.army.usace.erdc.crrel.dataquery.annotations.Transient;


/**
 *
 * @author rsgoss
 */
public class NameUtils {
    
    public final static HashMap<Field,String> getDbStructFieldMapping(Class dbStructClazz){
        HashMap<Field,String> fieldMapping = new HashMap<>();
        Field[] fields= dbStructClazz.getDeclaredFields();
        for(Field field:fields){
            if(!Modifier.isStatic(field.getModifiers())){ //exclude static fields
                if(field.getAnnotation(Transient.class)==null){
                    fieldMapping.put(field,NameUtils.getDbName(field));
                }
            }
        }
        return fieldMapping;
    }
    

    public final static HashMap<Method,String> getFieldMapping(Class cls, int type, Boolean isCamelCased, Boolean useDeclaredOnly){
        HashMap<Method,String> fieldMapping = new HashMap<>();
        Method[] methods;
        if(useDeclaredOnly){
            methods=cls.getDeclaredMethods();
        }
        else{
            methods = cls.getMethods();
        }
        for(Method method: methods){
            if(method.getAnnotation(Transient.class)==null){
                String methodName=method.getName();
                if(type==GET){
                    if(methodName.startsWith("get") && !methodName.equals("getClass")){
                        fieldMapping.put(method,NameUtils.getDbName(isCamelCased,methodName.substring(3),method));
                    }
                }
                else{
                    if(methodName.startsWith("set") && !methodName.equals("getClass")){
                        fieldMapping.put(method,NameUtils.getDbName(isCamelCased,methodName.substring(3),method));
                    }
                }
            }
        }
        return fieldMapping;
    }
    
    public final static String getDbName(Class clazz){
        Boolean useCamelCase=useCamelCase(clazz);
        return NameUtils.getDbName(useCamelCase,clazz.getSimpleName(),null);
    }
    
    
    public final static String getIdFieldName(Class clazz){
        Boolean useCamelCase=useCamelCase(clazz);
        HashMap<Method,String> fieldMap=getFieldMapping(clazz, GET, useCamelCase, Boolean.TRUE);
        for(Method method:fieldMap.keySet()){
            if(method.isAnnotationPresent(Id.class)){
                return NameUtils.getDbName(useCamelCase,method.getName().substring(3),method);
            }
        }
        return null;
    }
    
    public final static String getIdFieldName(HashMap<Method,String> fieldMap){
        for(Method method:fieldMap.keySet()){
            if(method.isAnnotationPresent(Id.class)){
                return fieldMap.get(method);
            }
        }
        return null;
    }
    

    public final static Boolean useCamelCase(Class cls){
        Entity entityAnno = (Entity)cls.getAnnotation(Entity.class);
        if(entityAnno!=null){
            return entityAnno.useCamelCase();
        }
        else{
            return false;
        }
    }
    
    public final static Boolean isDbStruct(Class cls){
        DbStruct dbStructAnno = (DbStruct)cls.getAnnotation(DbStruct.class);
        return dbStructAnno!=null;
    }
    
    public final static String getEntitySchema(Class cls) {
        Entity entityAnno = (Entity)cls.getAnnotation(Entity.class);
        if(entityAnno!=null && !entityAnno.schema().equals("")){
            return entityAnno.schema();
        }
        else{
            return null;
        }
    }
    

    public final static String getTableName(Class cls){
        Entity entityAnno = (Entity)cls.getAnnotation(Entity.class);
        if(entityAnno!=null && !entityAnno.table().equals("")){
            return entityAnno.table();
        }
        else{
            return null;
        }
    }
    
    public final static String getSql(Class cls){
        Entity entityAnno = (Entity)cls.getAnnotation(Entity.class);
        if(entityAnno!=null && !entityAnno.sql().equals("")){
            return entityAnno.sql();
        }
        else{
            return null;
        }
    }
    
    /*
    public final static String getUrl(Class cls){
        Entity entityAnno = (Entity)cls.getAnnotation(Entity.class);
        if(entityAnno!=null && !entityAnno.url().equals("")){
            return entityAnno.url();
        }
        else{
            return null;
        }
    }
    */
    
    public final static SPTYPE getSPTYPE(Class cls){
        SharepointValue sptAnno = (SharepointValue)cls.getAnnotation(SharepointValue.class);
        if(sptAnno!=null){
            return sptAnno.type();
        }
        else{
            return SPTYPE.VALUE;  //if not explicitly designated, default to value type
        }
    }
    
    public static String getDbName(String attributeName){
        String fieldName="";
        for(int i=0;i<attributeName.length();i++){
            if(i==0) fieldName+=Character.toLowerCase(attributeName.charAt(i));
            else{
                char c = attributeName.charAt(i);
                if(Character.isUpperCase(c))
                    fieldName+="_"+Character.toLowerCase(c);
                else
                    fieldName+=c;
            }
        }
        return fieldName;
    }
    
    
    public static String getDbName(Field field){
        if(field.isAnnotationPresent(Column.class)){
            Column c = field.getAnnotation(Column.class);
            return c.name();
        }
        else{
            return getDbName(field.getName());
        }
    }
    
   
    public static String getDbName(Boolean isCamelCase, String attributeName, Method method){
        if(method!=null && method.isAnnotationPresent(Column.class)){
            Column c = method.getAnnotation(Column.class);
            return c.name();
        }
        else{
            String fieldName="";
            if(isCamelCase){
                fieldName=attributeName;
            }
            else{
                fieldName=getDbName(attributeName);
            }
            return fieldName;
        }
    }
    
    
    public static String dbToClassName(String dbName) throws ClassNotFoundException{
        String className="";
        for(int i=0;i<dbName.length();i++){
            if(i==0) className+=Character.toUpperCase(dbName.charAt(i));
            else{
                char c = dbName.charAt(i);
                if(c=='_'){
                    i++;
                    className+=Character.toUpperCase(dbName.charAt(i));
                }
                else{
                    className+=Character.toLowerCase(c);
                }
            }
        }
        return className; 
    }
    
    public static String dbToFieldName(String dbName){
        String fieldName="";
        for(int i=0;i<dbName.length();i++){
            char c = dbName.charAt(i);
            if(c=='_'){
                i++;
                fieldName+=Character.toUpperCase(dbName.charAt(i));
            }
            else{
                fieldName+=Character.toLowerCase(c);
            }   
        }
        return fieldName; 
    }
    
    public static Class getDbDataType(Method method){
        if(method!=null && method.isAnnotationPresent(DbDataType.class)){
            DbDataType d = method.getAnnotation(DbDataType.class);
            return d.dataType();
        }
        else{
            return null;
        }
    }
    
    
    
}
