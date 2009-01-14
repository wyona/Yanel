/**
 * 
 */
package org.wyona.yanel.servlet.menu;

import org.apache.log4j.Logger;
import org.wyona.yanel.core.Resource;
import org.wyona.yanel.core.api.attributes.WorkflowableV1;
import org.wyona.yanel.core.attributes.versionable.RevisionInformation;
import org.wyona.yanel.core.workflow.Transition;
import org.wyona.yanel.core.workflow.Workflow;
import org.wyona.yanel.core.workflow.WorkflowException;
import org.wyona.yanel.core.workflow.WorkflowHelper;


/**
 * @author gary
 *
 */
public class RevisionTransitions implements RevisionTransitionsMenuContent {
    
    private static Logger log = Logger.getLogger(RevisionTransitions.class);
    
    private Resource resource;
    private String revision;
    private String language;
    private ITransitionMenuContent menuItems;

    
    public RevisionTransitions(Resource resource, String revn, String lang, ITransitionMenuContent menuItems) {
        this.resource = resource;
        this.revision = revn;
        this.language = lang;
        this.menuItems = menuItems;
    }

    private String getContent() {
        String content = "";

        try {
            Workflow workflow = WorkflowHelper.getWorkflow(this.resource);
            
            if (workflow != null) {
                content = "<ul>";

                WorkflowableV1 workflowable = (WorkflowableV1) this.resource;
                String state = workflowable.getWorkflowState(revision);

                Transition[] transitions = workflow.getTransitions();
//                ITransitionMenuContent menuItems =
//                    new TransitionMenuContentImpl(getResource(), state, getRevisionInfo().getName(), getMenuLanguageCode());

                for (int i = 0; i < transitions.length; i++) {
                    content += menuItems.getTransitionElement(transitions[i]);
                }

                content += "</ul>";
            }
            
        } catch (WorkflowException e) {
            content = "";
            log.error("Could not get workflow.", e);
        }

        return content;
    }

    /* (non-Javadoc)
     * @see org.wyona.yanel.servlet.menu.RevisionTransitionsMenuContent#getMenuLanguageCode()
     */
    public String getMenuLanguageCode() {
        return this.language;
    }

    /* (non-Javadoc)
     * @see org.wyona.yanel.servlet.menu.RevisionTransitionsMenuContent#getResource()
     */
    public Resource getResource() {
        return this.resource;
    }

    /* (non-Javadoc)
     * @see org.wyona.yanel.servlet.menu.RevisionTransitionsMenuContent#toHTML()
     */
    public String toHTML() {
        return getContent();
    }

}
