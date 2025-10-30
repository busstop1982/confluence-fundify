package fundify

import fundify.Fundify
import fundify.FundifyCall
import util.ServiceHelper
import org.json.*
import com.atlassian.confluence.api.model.content.*
import com.atlassian.confluence.api.model.people.User
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import com.atlassian.confluence.api.service.content.ContentService
import com.atlassian.confluence.security.PermissionManager
import com.atlassian.confluence.security.Permission
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import groovy.util.logging.Log4j


@Log4j
class PageBuilder {
    String space, parent, template, propertyKey
    Content newcontent, templatePage, mama
    @PluginModule
    ContentService contentService
    @PluginModule
    PermissionManager perman
    ServiceHelper h = new ServiceHelper()
    Fundify f = new Fundify()
    List<Label> labels

    public PageBuilder(String space, String parent, String template){
        this.space = space
        this.parent = parent
        this.template = template
        this.templatePage = h.getPageById(template)
        this.labels = h.getLabels(template).getResults()
        this.mama = h.getPageById(parent)
        this.propertyKey = 'callID'
    }

    public void build(FundifyCall call){
        JSONObject replacer = call.getPageTemplateInfo()
        String cobo = this.templatePage.getBody().get(ContentRepresentation.STORAGE).getValue()
        replacer.keys().each { key ->
            cobo = cobo.replace(key,replacer.getString(key))
        }
        try {
            newcontent = Content.builder(templatePage)
                .body(cobo, ContentRepresentation.STORAGE)
                .title(call.getPageTitle())
                .type(ContentType.PAGE)
                .parent(mama)
                .build()
        }
        catch (Exception e){log.error e}
        
        Content nc
        try {
            //assert contentService.validator().validateCreate(newcontent) : "Can't create new page."
            nc = contentService.create(newcontent)
        }
        catch (Exception e){log.error e}
        try {
            def bar = h.addPropertyToContent(nc,'callID',call.getCallId())
        }
        catch (Exception e){log.error e}
        try {
            def foo = h.addLabels(nc.getId(), labels)
        }
        catch (Exception e){log.error e}
    }

    public void update(FundifyCall call){
        // template: https://community.developer.atlassian.com/t/contentservice-is-unable-to-update-content-page-position/41227
        Content oldVersion = h.getContentByContentProperty(this.space,this.propertyKey,call.getCallId())
        JSONObject replacer = call.getPageTemplateInfo()
        String cobo = this.templatePage.getBody().get(ContentRepresentation.STORAGE).getValue()
        def coref = h.getContentReference(this.template)
        def cuser = AuthenticatedUserThreadLocal.get()
        com.atlassian.sal.api.user.UserKey userKey = cuser.getKey()
        User currentUser = User.fromUserkey(userKey)
        replacer.keys().each { key ->
            cobo = cobo.replace(key,replacer.getString(key))
        }

        //assert perman.hasPermission(cuser,Permission.EDIT,oldVersion): "${cuser.getName()} has no editing permissions"
        try {
            def nextVersion = oldVersion.getVersion()
            .nextBuilder()
            .message('Updated via automation.')
            .when(new Date())
            .by(currentUser)
            .minorEdit(false)
            .content(coref)
            .build()

            Content.ContentBuilder contentBuilder = Content.builder(oldVersion)
            Content updatedContent = contentBuilder
            .version(nextVersion)
            .body(cobo, ContentRepresentation.STORAGE)
            .parent(this.mama)
            .build()

        contentService.update(updatedContent)
        }
        catch (Exception e){log.error e}
    }
}


