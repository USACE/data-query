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
import org.apache.commons.convert.ConversionException;

/**
 *
 * @author k3endrsg
 */
public class DefaultConverter implements DataQueryConverter{
    

    @Override
    public Object convert(Object obj,Field field) throws ClassNotFoundException, ConversionException{
        Class fieldParam=field.getType();
        if(obj==null)
            return null;
        else{
            if(fieldParam.isPrimitive()){
                return obj;
            }
            else{ 
                return DataQueryConverter.convert(obj,fieldParam);
            }
        }
    }


    @Override
    public Object convert(Object obj,Method method) throws ClassNotFoundException, ConversionException{
        Class methodParam=method.getParameterTypes()[0];
        if(obj==null)
            return null;
        else{
            if(methodParam.isPrimitive()){
                return obj;
            }
            else{ 
                return DataQueryConverter.convert(obj,methodParam);
            }
        }
    }
}
