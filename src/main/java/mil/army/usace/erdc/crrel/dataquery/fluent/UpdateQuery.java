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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Dml;

/**
 *
 * @author k3endrsg
 */
public class UpdateQuery {
    
    private final Dml rdq;
    private List records = new ArrayList();
    private boolean useDeclaredOnly=false;
    private int batchSize=-1;
    private boolean useBatch=true;
    private List<String> includeFields=null;
    private String tableName=null;
    
    public UpdateQuery(Dml d){
        this.rdq=d;
    }
    
    public UpdateQuery record(Object record){
        records.add(record);
        return this;
    }
    
    public UpdateQuery records(List records){
        this.records=records;
        return this;
    }
    
    public UpdateQuery tableName(String tableName){
        this.tableName=tableName;
        return this;
    }
    
    public UpdateQuery useDeclaredOnly(boolean useDeclaredOnly){
        this.useDeclaredOnly=useDeclaredOnly;
        return this;
    }
    
    public UpdateQuery batchSize(int batchSize){
        this.batchSize=batchSize;
        return this;
    }
    
    public UpdateQuery useBatch(boolean useBatch){
        this.useBatch=useBatch;
        return this;
    }
    
    public UpdateQuery includeFields(List<String> includeFields){
        this.includeFields=includeFields;
        return this;
    }
    
    public UpdateQuery includeFields(String[] includeFields){
        this.includeFields=Arrays.asList(includeFields);
        return this;
    }
    
    public UpdateQuery includeFields(Set<String> includeFields){
        List<String>ifield= new ArrayList<>(includeFields);
        this.includeFields=ifield;
        return this;
    }
    
    public void execute(){
        if(batchSize>-1){
            rdq.setBatchSize(batchSize);
        }
        rdq.setUseBatching(useBatch);
        this.rdq.updateRecords(records,useDeclaredOnly,includeFields,tableName);
    }
    
}
