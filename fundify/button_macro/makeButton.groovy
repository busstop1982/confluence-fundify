
import com.atlassian.confluence.api.service.content.*
import com.atlassian.confluence.api.model.*
import com.atlassian.confluence.api.model.content.JsonContentProperty
import com.atlassian.confluence.security.ContentPermission
import com.atlassian.confluence.api.model.content.id.ContentId

import java.io.StringWriter
import groovy.xml.MarkupBuilder
import groovyx.net.http.*
import groovy.json.JsonSlurper
import com.onresolve.scriptrunner.runner.customisers.PluginModule


def writer = new StringWriter()
def builder = new MarkupBuilder(writer)
//def admin = context.getEntity()
def pageId = context.getEntity().getId()
def permissions = context.getEntity().getContentPermissionSet(ContentPermission.VIEW_PERMISSION)
def slurpy = new JsonSlurper()
def risid
@PluginModule
ContentPropertyService cps

ContentPropertyService.ContentPropertyFinder finder = cps.find(new Expansion('metadata'))
JsonContentProperty property = finder
    .withContentId(ContentId.of(pageId))
    .withPropertyKey('callID')
    .fetchOrNull()

if (property) risid = property.getValue().getValue().replace('"',"")

builder.p {
    div([type:'text', id:'risid'],risid)
    button(class:'aui-button aui-button-primary',onclick:'updatePage()','Update Page')
    div(id:'result')
    //div(id:'endfield',"pkey class: "+risid)
}
/*builder.p{
    div(context.getClass())
    div(context.entity)
    div(context.getSpaceKey())
    div(context.getEntity().getEntity())
}*/
writer.toString()