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

public class ConnectionConfig {
    public String host;
    public String port;
    public String instance;
    public String database;
    public String user;
    public String password;

    public ConnectionConfig host(String host){
        this.host=host;
        return this;
    }

    public ConnectionConfig port(String port){
        this.port=port;
        return this;
    }

    public ConnectionConfig instance(String instance){
        this.instance=instance;
        return this;
    }

    public ConnectionConfig database(String database){
        this.database=database;
        return this;
    }

    public ConnectionConfig user(String user){
        this.user=user;
        return this;
    }

    public ConnectionConfig password(String password){
        this.password=password;
        return this;
    }

    public String getName(){
        return String.format("%s@%s:%s/%s/%s",user,host,port,instance,database);
    }
}
