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

package mil.army.usace.erdc.crrel.dataquery.rdbms.oracle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mil.army.usace.erdc.crrel.dataquery.DataQueryFactory;
import mil.army.usace.erdc.crrel.dataquery.RecordsetCursor;
import mil.army.usace.erdc.crrel.dataquery.connection.action.Select;
import mil.army.usace.erdc.crrel.dataquery.fluent.DataQuery;
import mil.army.usace.erdc.crrel.dataquery.rdbms.Record;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author rsgoss
 */
public class OracleTests {
    public static String dbUrl="jdbc:oracle:thin:username/pass@server/instance";
    
    public OracleTests() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void ConnectionTest(){
        String url="";
        try(DataQuery db = new DataQuery(DataQueryFactory.DB.ORACLE,url)){
            System.out.println("connected");
        }        
    }

    /*
    @Test
    public void ResultTest() throws SQLException{
        try(DataQuery db = new DataQuery(DataQueryFactory.DB.ORACLE,dbUrl)){
            List<Map<String,Object>> test = db.select(Select.ReturnType.RECORDSETMAP)
                                             .sql("select * from mmc_assignment")
                                             .fetch();
            System.out.println(test.size());
        }
    }
    
    @Test
    public void RecordTest() throws SQLException{
        try(DataQuery db = new DataQuery(DataQueryFactory.DB.ORACLE,dbUrl)){
            try(RecordsetCursor<Record> cursor=db.select(Record.class)
                                                 .sql("select * from mmc_assignment")
                                                 .fetchCursor()){
                List<Map<String,Object>> test = new ArrayList<>();
                for(Record rec:cursor){
                   test.add(rec.toMap());
                }
                System.out.println(test.size());
            }
        }
    }
*/
}
