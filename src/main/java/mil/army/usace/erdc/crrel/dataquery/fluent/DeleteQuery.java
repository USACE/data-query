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

import java.util.ArrayList;
import java.util.List;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Dml;

/**
 *
 * @author k3endrsg
 */
public class DeleteQuery {
    
    private final Dml dml;
    private List records = new ArrayList();
    private int batchSize=-1;
    private boolean useBatch=true;
    private String criteria=null;
    private Class objClass=null;
    private Object[] params=null;
    private String tableName=null;
    
    public DeleteQuery(Dml d){
        this.dml=d;
    }
    
    public DeleteQuery(Dml d, Class clazz){
        this.dml=d;
        this.objClass=clazz;
    }
    
    public DeleteQuery record(Object record){
        records.add(record);
        return this;
    }
    
    public DeleteQuery records(List records){
        this.records=records;
        return this;
    }
    
    public DeleteQuery tableName(String tableName){
        this.tableName=tableName;
        return this;
    }
    
    public DeleteQuery batchSize(int batchSize){
        this.batchSize=batchSize;
        return this;
    }
    
    public DeleteQuery useBatch(boolean useBatch){
        this.useBatch=useBatch;
        return this;
    }
    
    public DeleteQuery criteria(String criteria){
        this.criteria=criteria;
        return this;
    }
    
    public DeleteQuery params(Object... params){
        this.params=params;
        return this;
    }
    
    
    public void execute(){
        if(batchSize>-1){
            dml.setBatchSize(batchSize);
        }
        dml.setUseBatching(useBatch);

        if(records.size()>0){
            this.dml.deleteRecords(records,tableName);
        }
        else{
            this.dml.deleteRecords(objClass, criteria, params);
        }
        
    }
    
}
