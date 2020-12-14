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

package mil.army.usace.erdc.crrel.dataquery.sharepoint;

import java.util.List;
import mil.army.usace.erdc.crrel.dataquery.DataQueryFactory.DB;
import mil.army.usace.erdc.crrel.dataquery.fluent.DataQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author U4RRCRSG
 */
public class SharepointTests {
    
    public SharepointTests() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void TestSharepointList() {
        String url = "https://sharepoint.net/sites/ELL/_vti_bin/owssvr.dll?CS=65001&XMLDATA=1&RowLimit=0&List={D2E5C9CE-B157-468C-BB5C-13016A9C0DA6}&View={B16D8455-5617-44C1-AE36-106DEB146CC4}&Query=ID%20LLID%20LinkTitleNoMenu%20Workflow_x0020_Status%20Author%20Created%20Description_x0020_of_x0020_Lesso%20Primary_x0020_Community_x0020_of%20Attachments";
        DataQuery dq = new DataQuery(DB.SHAREPOINT,url);
        List<LessonsLearned> test = dq.select(LessonsLearned.class).fetch();
        System.out.println(test.size());
    }
}
