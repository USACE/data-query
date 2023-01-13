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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.PooledConnection;
import mil.army.usace.erdc.crrel.dataquery.utils.DataQueryConverter;
import oracle.jdbc.OracleConnection;
import static oracle.sql.NUMBER.toBytes;
import org.apache.commons.convert.AbstractConverter;
import org.apache.commons.convert.ConversionException;
import org.apache.commons.convert.Converters;

/**
 *
 * @author rsgoss
 */
public class OracleConverter implements DataQueryConverter{
    
    static{
        Converters.registerConverter(new OracleDateToSqlDate());
        Converters.registerConverter(new OracleTimestampToDate());
        Converters.registerConverter(new OracleTimestampTzToDate());
        Converters.registerConverter(new OracleClobToString());
        Converters.registerConverter(new OracleBlobToByteArray());
        Converters.registerConverter(new OracleBlobToInputStream()); 
        Converters.registerConverter(new OracleUUIDToByteArray());
        Converters.registerConverter(new OracleByteArrayToUUID());
    }
    
    
    private final OracleConnection oc;
    
    public OracleConverter(Connection conn){
        try{
            if(conn instanceof PooledConnection){
                System.out.println(((PooledConnection)conn).getConnection().getClass().getName());
                this.oc=((PooledConnection)conn).getConnection().unwrap(OracleConnection.class);
            } else {
                this.oc=conn.unwrap(OracleConnection.class);
            }
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object convert(Object obj,Method method) throws ClassNotFoundException, ConversionException, SQLException{
        Class methodParam=method.getParameterTypes()[0];
        if(obj==null)
            return null;
        else if(obj instanceof oracle.sql.TIMESTAMPTZ){
            java.sql.Date sqlDate=((oracle.sql.TIMESTAMPTZ)obj).dateValue(oc);
            return DataQueryConverter.convert(sqlDate,methodParam);
        }
        else{
            if(methodParam.isPrimitive()){
                return obj;
            }
            else{ 
                return DataQueryConverter.convert(obj,methodParam);
            }
        }
    }

    @Override
    public Object convert(Object obj,Field field) throws ClassNotFoundException, ConversionException, SQLException{
        Class fieldParam=field.getType();
        if(obj==null)
            return null;
        else if(obj instanceof oracle.sql.TIMESTAMPTZ){
            java.sql.Date sqlDate=((oracle.sql.TIMESTAMPTZ)obj).dateValue(oc);
            return DataQueryConverter.convert(sqlDate,fieldParam);
        }
        else{
            if(fieldParam.isPrimitive()){
                return obj;
            }
            else{ 
                return DataQueryConverter.convert(obj,fieldParam);
            }
        }
    }
    
    public static class OracleDateToSqlDate extends AbstractConverter<oracle.sql.DATE, java.sql.Date> {
        public OracleDateToSqlDate() {
            super(oracle.sql.DATE.class, java.sql.Date.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.DATE.class.equals(sourceClass) || oracle.sql.DATE.class.equals(sourceClass)) && java.sql.Date.class.equals(targetClass);
        }

        @Override
        public java.sql.Date convert(oracle.sql.DATE obj) throws ConversionException {
            return obj.dateValue();
        }
    }
    
    public static class OracleTimestampToSqlDate extends AbstractConverter<oracle.sql.TIMESTAMP, java.sql.Date> {
        public OracleTimestampToSqlDate() {
            super(oracle.sql.TIMESTAMP.class, java.sql.Date.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.TIMESTAMP.class.equals(sourceClass) || oracle.sql.TIMESTAMP.class.equals(sourceClass)) && java.sql.Date.class.equals(targetClass);
        }

        @Override
        public java.sql.Date convert(oracle.sql.TIMESTAMP obj) throws ConversionException {
            try{
                return new java.sql.Date(obj.dateValue().getTime());
            }
            catch(java.sql.SQLException ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
    public static class OracleTimestampToDate extends AbstractConverter<oracle.sql.TIMESTAMP, java.util.Date> {
        public OracleTimestampToDate() {
            super(oracle.sql.TIMESTAMP.class, java.util.Date.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.TIMESTAMP.class.equals(sourceClass) || oracle.sql.TIMESTAMP.class.equals(sourceClass)) && java.util.Date.class.equals(targetClass);
        }

        @Override
        public java.sql.Date convert(oracle.sql.TIMESTAMP obj) throws ConversionException {
            try{
                return obj.dateValue();
            }
            catch(java.sql.SQLException ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
    public static class OracleTimestampTzToDate extends AbstractConverter<oracle.sql.TIMESTAMPTZ, java.util.Date> {
        public OracleTimestampTzToDate() {
            super(oracle.sql.TIMESTAMPTZ.class, java.util.Date.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.TIMESTAMPTZ.class.equals(sourceClass) || oracle.sql.TIMESTAMPTZ.class.equals(sourceClass)) && java.util.Date.class.equals(targetClass);
        }

        @Override
        public java.util.Date convert(oracle.sql.TIMESTAMPTZ obj) throws ConversionException {
            try{
  
                return obj.dateValue();
            }
            catch(java.sql.SQLException ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
    /*
    public static class OracleTimestampTzToSQLDate extends AbstractConverter<oracle.sql.TIMESTAMPTZ, java.sql.Date> {
        public OracleTimestampTzToSQLDate() {
            super(oracle.sql.TIMESTAMPTZ.class, java.util.Date.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.TIMESTAMPTZ.class.equals(sourceClass) || oracle.sql.TIMESTAMPTZ.class.equals(sourceClass)) && java.sql.Date.class.equals(targetClass);
        }

        @Override
        public java.sql.Date convert(oracle.sql.TIMESTAMPTZ obj) throws ConversionException {
            try{
                return obj.dateValue();
            }
            catch(java.sql.SQLException ex){
                throw new ConversionException(ex);
            }        
        }
    }
    */
    
    public static class OracleClobToString extends AbstractConverter<oracle.sql.CLOB, String> {
        public OracleClobToString() {
            super(oracle.sql.CLOB.class, String.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.CLOB.class.equals(sourceClass) || oracle.sql.CLOB.class.equals(sourceClass)) && String.class.equals(targetClass);
        }

        @Override
        public String convert(oracle.sql.CLOB obj) throws ConversionException {
            try{
                return obj.stringValue();
            }
            catch(java.sql.SQLException ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
    public static class OracleBlobToByteArray extends AbstractConverter<oracle.sql.BLOB, byte[]> {
        public OracleBlobToByteArray() {
            super(oracle.sql.BLOB.class, byte[].class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.BLOB.class.equals(sourceClass) || oracle.sql.BLOB.class.equals(sourceClass)) && byte[].class.equals(targetClass);
        }

        @Override
        public byte[] convert(oracle.sql.BLOB obj) throws ConversionException {
            try{
                return obj.getBytes(1, (int)obj.length());
            }
            catch(Exception ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
    public static class OracleBlobToInputStream extends AbstractConverter<oracle.sql.BLOB, InputStream> {
        public OracleBlobToInputStream() {
            super(oracle.sql.BLOB.class, InputStream.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (oracle.sql.BLOB.class.equals(sourceClass) || oracle.sql.BLOB.class.equals(sourceClass)) && InputStream.class.equals(targetClass);
        }

        @Override
        public InputStream convert(oracle.sql.BLOB obj) throws ConversionException {
            try{
                return obj.getBinaryStream();
            }
            catch(Exception ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
    public static class OracleUUIDToByteArray extends AbstractConverter<java.util.UUID, byte[]> {
        public OracleUUIDToByteArray() {
            super(java.util.UUID.class, byte[].class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (java.util.UUID.class.equals(sourceClass) || java.util.UUID.class.equals(sourceClass)) && byte[].class.equals(targetClass);
        }

        @Override
        public byte[] convert(java.util.UUID obj) throws ConversionException {
            try{
                ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
                bb.putLong(obj.getMostSignificantBits());
                bb.putLong(obj.getLeastSignificantBits());
                return bb.array();
            }
            catch(Exception ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
    public static class OracleByteArrayToUUID extends AbstractConverter<byte[],java.util.UUID> {
        public OracleByteArrayToUUID() {
            super(byte[].class,java.util.UUID.class);
        }

        @Override
        public boolean canConvert(Class<?> sourceClass, Class<?> targetClass) {
            return (byte[].class.equals(sourceClass) || byte[].class.equals(sourceClass)) && java.util.UUID.class.equals(targetClass);
        }

        @Override
        public java.util.UUID convert(byte[] obj) throws ConversionException {
            try{
                ByteBuffer bb = ByteBuffer.wrap(obj);
                long firstLong = bb.getLong();
                long secondLong = bb.getLong();
                return new UUID(firstLong, secondLong);
            }
            catch(Exception ex){
                throw new ConversionException(ex);
            }        
        }
    }
    
}
