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

package mil.army.usace.erdc.crrel.dataquery.web.implementations.sharepoint.lists;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mil.army.usace.erdc.crrel.dataquery.RecordsetCursor;
import mil.army.usace.erdc.crrel.dataquery.annotations.SharepointValue;
import mil.army.usace.erdc.crrel.dataquery.annotations.Transient;
import static mil.army.usace.erdc.crrel.dataquery.connection.DataQueryConnection.GET;
import static mil.army.usace.erdc.crrel.dataquery.connection.DataQueryConnection.SET;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select;
import mil.army.usace.erdc.crrel.dataquery.utils.NameUtils;
import static mil.army.usace.erdc.crrel.dataquery.utils.NameUtils.getFieldMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author rsgoss
 */
public class SharepointListSelect implements Select{
    
    private final String url;
    
    public SharepointListSelect(String url){
        this.url=url;
    }

    @Override
    public <T> RecordsetCursor<T> getRecordsCursor(Class objClass, String command, boolean useDeclaredOnly, boolean isFullCommand, boolean closeConnection, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getRecord(Class objClass, String criteria, boolean useDeclaredOnly, boolean isFullCommand, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> List<T> getRecords(Class type, String filterString, boolean useDeclaredOnly, boolean isFullCommand, Object[] params) {
        ArrayList records = new ArrayList();
         try{
             if(filterString==null)
                filterString="";
             URL spUrl = new URL(url+filterString);
             Authenticator.setDefault(new SpAuthenticator());
             InputStream ins = spUrl.openConnection().getInputStream();
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document doc = db.parse(ins);
             doc.getDocumentElement().normalize();
             NodeList nodeLst = doc.getElementsByTagName("z:row");
             HashMap<Method,String> fieldMapping = getFieldMapping(type,SET,false,false); //@TODO same as below...
             HashMap<String,SharepointValue.SPTYPE> spTypeMap = getSharepointMapping(type,GET,false,false); //@TODO pull last 2 args from entity
             for (int s = 0; s < nodeLst.getLength(); s++) {
                Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                     Element fstElmnt = (Element) fstNode;
                     Object newObj = type.newInstance();
                     for(Method method:fieldMapping.keySet()){
                         String fieldName=fieldMapping.get(method);
                         //System.out.println(fieldName);
                         SharepointValue.SPTYPE spType=spTypeMap.get(fieldName);
                         Object value=fstElmnt.getAttribute(fieldName);
                         
                         if(spType==SharepointValue.SPTYPE.VALUE){
                             Class[] argTypes=method.getParameterTypes();
                             if(value.equals("")){
                                 value=null;
                             }
                             else{
                                 if(argTypes[0].getCanonicalName().equals(java.sql.Timestamp.class.getCanonicalName())){
                                    DateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
                                    value = new java.sql.Timestamp((df.parse(((String)value).substring(0,10))).getTime());
                                 }
                                 else if(argTypes[0].getCanonicalName().equals(java.lang.Integer.class.getCanonicalName())){
                                     try{
                                        value=Integer.parseInt((String)value);
                                     }
                                     catch(NumberFormatException ex){
                                        value=(int)Double.parseDouble((String)value);
                                     }
                                 }
                                 else if(argTypes[0].getCanonicalName().equals(java.lang.Double.class.getCanonicalName())){
                                     value=Double.parseDouble((String)value);
                                 }
                             }                             
                         }
                         else{
                             if(value.equals("")){
                                 value=null;
                             }
                             else{
                                 String[] idVals = ((String)value).split(";");
                                 if(spType==SharepointValue.SPTYPE.IDPK){
                                     value=Integer.parseInt(idVals[0]);
                                 }
                                 else{
                                     value=idVals[1].substring(1);
                                 }
                             }                             
                         }
                         method.invoke(newObj,value);
                     }
                    records.add(newObj);
                }
             }
             return records;
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
        
    }
    
    private HashMap<String,SharepointValue.SPTYPE> getSharepointMapping(Class cls, int type, Boolean isCamelCased, Boolean useDeclaredOnly){
        HashMap<String,SharepointValue.SPTYPE> spMapping = new HashMap<>();
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
                        String fieldName=NameUtils.getDbName(isCamelCased,methodName.substring(3),method);
                        SharepointValue.SPTYPE spType=NameUtils.getSPTYPE(cls);
                        spMapping.put(fieldName, spType);
                    }
                }
            }
        }
        return spMapping;
    }
    
    
    
    private static String kuser = ""; // your account name
    private static String kpass = ""; // your password for the account
    
    private static class SpAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(kuser, kpass.toCharArray()));
        }
    }

    @Override
    public <T> List<T> getDbStructs(Class objClass, String command, boolean isFullCommand, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getDbStruct(Class objClass, String command, boolean isFullCommand, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getSingleSqlValue(String sql, Class returnType, Object[] params, boolean returnEmptyRecordsetAsNull) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getRecordsAsJson(String sql, JsonKeyCase jsonKeyCase, Boolean useCamelCase, Boolean escapeHtml, SimpleDateFormat dateFormatter, Object... params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> List<T> getRecordsAsCollection(ReturnType returnType, String sql, Class[] returnTypes, Object[] params) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T, S> HashMap<T, S> getRecordsAsHashmap(String sql, Object[] params, Class[] returnTypes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
