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

import mil.army.usace.erdc.crrel.dataquery.annotations.Column;
import mil.army.usace.erdc.crrel.dataquery.annotations.Entity;


@Entity()
public class LessonsLearned {

    Integer spid;
    Integer llid;
    String title;
    String attachments;
    String recommendation;
    String _text_;
    String community; 
    String workflowstatus;
    String author;
    String created;

    @Column(name="ows_ID")
    public Integer getSpid() {
        return spid;
    }

    @Column(name="ows_ID")
    public void setSpid(Integer spid) {
        this.spid = spid;
    }

    @Column(name="ows_LLID")
    public Integer getLlid() {
        return llid;
    }

    @Column(name="ows_LLID")
    public void setLlid(Integer llid) {
        this.llid = llid;
    }

    @Column(name="ows_LinkTitleNoMenu")
    public String getTitle() {
        return title;
    }

    @Column(name="ows_LinkTitleNoMenu")
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Column(name="ows_Attachments")
    public String getAttachments() {
        return attachments;
    }

    @Column(name="ows_Attachments")
    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    @Column(name="Recommended_x0020_Action_x0028_s")
    public String getRecommendation() {
        return recommendation;
    }

    @Column(name="Recommended_x0020_Action_x0028_s")
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    @Column(name="ows_Description_x0020_of_x0020_Lesso")
    public String getText_() {
        return _text_;
    }

    @Column(name="ows_Description_x0020_of_x0020_Lesso")
    public void setText_(String _text_) {
        this._text_ = _text_;
    }

    @Column(name="ows_Primary_x0020_Community_x0020_of")
    public String getCommunity() {
        return community;
    }

    @Column(name="ows_Primary_x0020_Community_x0020_of")
    public void setCommunity(String community) {
        this.community = community;
    }

    @Column(name="ows_Workflow_x0020_Status")
    public String getWorkflowstatus() {
        return workflowstatus;
    }

    @Column(name="ows_Workflow_x0020_Status")
    public void setWorkflowstatus(String workflowstatus) {
        this.workflowstatus = workflowstatus;
    }

    @Column(name="ows_Author")
    public String getAuthor() {
        return author;
    }

    @Column(name="ows_Author")
    public void setAuthor(String author) {
        this.author = author;
    }

    @Column(name="ows_Created")
    public String getCreated() {
        return created;
    }

    @Column(name="ows_Created")
    public void setCreated(String created) {
        this.created = created;
    }
    
    
}
