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

package mil.army.usace.erdc.crrel.dataquery;


import java.util.UUID;
import static mil.army.usace.erdc.crrel.dataquery.utils.DataQueryConverter.convert;

import org.apache.commons.convert.ConversionException;
/**
 *
 * @author k3endrsg
 */
public class GeneratedKey {
    private final String name;
    private final Object value;
    
    
    public GeneratedKey(String keyName, Object value){
        this.name=keyName;
        this.value=value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
    
    public Long getValueAsLong(){
        return getValue(Long.class);
    }
    
    public Integer getValueAsInt(){
        return getValue(Integer.class);
    }
    
    public String getValueAsString(){
        return getValue(String.class);
    }
    
    public UUID getValueAsUUID(){
        return getValue(UUID.class);
    }
    
    public <T> T getValue(Class clazz){
        try{
            return (T)convert(this.value,clazz);
        }
        catch(ClassNotFoundException|ConversionException ex){
            throw new RuntimeException(ex.getMessage());
        }
    }


}
