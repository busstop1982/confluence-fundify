package util

import java.util.ArrayList
import java.util.HashMap
import java.lang.Iterable
import com.atlassian.confluence.api.model.pagination.PageResponse
import com.atlassian.confluence.api.model.content.id.ContentId
import com.atlassian.confluence.api.model.pagination.SimplePageRequest
import com.atlassian.confluence.api.model.pagination.ContentCursor
import com.atlassian.confluence.api.model.Expansion
import com.atlassian.confluence.api.model.Expansions
import com.atlassian.confluence.api.model.JsonString
import com.atlassian.confluence.api.model.content.ContentSelector
import com.atlassian.confluence.api.model.content.*
import com.atlassian.confluence.api.model.content.JsonContentProperty as JCP
import com.atlassian.confluence.api.service.content.SpaceService
import com.atlassian.confluence.api.service.content.ContentService
import com.atlassian.confluence.api.service.content.ContentLabelService
import com.atlassian.confluence.api.service.content.ContentPropertyService
import com.atlassian.confluence.api.service.content.ContentService.ContentFinder
import com.onresolve.scriptrunner.runner.customisers.PluginModule
import com.atlassian.confluence.api.model.reference.Reference
import org.json.*
import groovy.util.logging.Log4j
import groovy.json.JsonSlurper
import com.fasterxml.jackson.databind.ObjectMapper

/**
* Handles multiple Services that interact with confluence content.
*/
@Log4j
class ServiceHelper {
    //Logger log = Logger.getLogger("com.onresolve.scriptrunner.runner.ScriptRunnerImpl")
    @PluginModule
    ContentService contentService
    @PluginModule
    SpaceService spaceService
    @PluginModule
    ContentLabelService contentLabelService
    @PluginModule
    ContentPropertyService contentPropertyService

    //SimplePageRequest pageRequest = new SimplePageRequest(0, 10)
    //ContentFinder bodyfetcher = contentService.find(new Expansion(Content.Expansions.BODY, new Expansions(new Expansion("storage"))))

    /**
    * Searches single Content via content ID and property key and returns String of property value.
    * @param pageId Content identifier
    * @param propertyKey name of the property
    * @return String of the property value
    */
    String getContentProperty(ContentId pageId, String propertyKey){
        try{
            ContentPropertyService.ContentPropertyFinder finder = contentPropertyService.find(new Expansion('metadata'))
            JsonContentProperty property = finder
                .withContentId(pageId)
                .withPropertyKey(propertyKey)
                .fetchOrNull()
            if (property){
                String paS = property.getValue().getValue()
                JsonSlurper slurpy = new JsonSlurper()
                return slurpy.parseText(paS)
            }
        }
        catch(Exception e){log.error e}
    }

    /**
    * @see ServiceHelper#getContentProperty(ContentId pageId, String propertyKey)
    */
    String getContentProperty(String pageId, String propertyKey) {
        return getContentProperty(ContentId.of(Long.parseLong(pageId)),propertyKey)
    }

    List getContentPropertiesOfSpace(String spaceKey, String propertyKey) {
        List out = new ArrayList<>()
        List<ContentId> allPageIds = getPageIdsOfSpace(spaceKey)
        for ( id : allPageIds ) {
            def cp = getContentProperty(id,propertyKey)
            if ( cp ) out.add(cp)
        }
        return out
    }

    /**
    * Returns page with specified Space and ContentProperty key/value pair
    */
    Content getContentByContentProperty(Space space, String propertyKey, String propertyValue){
        List<ContentId> allPages = this.getPageIdsOfSpace(space)
        allPages.removeIf(Objects::isNull)
        ContentId rightPage
        for ( id : allPages ) {
            if ( getContentProperty(id,propertyKey) == propertyValue ){
                rightPage = id
                break;
            }
        }
        return this.getPageById(rightPage)
    }

    /*
    * @see ServiceHelper#getContentByContentProperty(Space space, String propertyKey, String propertyValue)
    */
    Content getContentByContentProperty(String spacekey, String propertyKey, String propertyValue){
        Space space = this.getSpaceByKey(spacekey)
        return this.getContentByContentProperty(space as Space, propertyKey, propertyValue)
    }

    /**
    * Creates a JsonContentProperty (= Container for arbitrary JSON data attached to some Content).
    * @param page Content the property will be added to
    * @param propertyKey Property key to be added
    * @param propertyValue Property value to be added
    * @return the ContentProperty created
    */
    JsonContentProperty addPropertyToContent(Content page, String propertyKey, String propertyValue){
        try{
            def objectMapper = new ObjectMapper()
            def jsonValue = objectMapper.writeValueAsString(propertyValue)
            JCP newProperty = JCP.builder()
                .content(page)
                .key(propertyKey)
                .value(new JsonString(jsonValue))
                .build()
            return contentPropertyService.create(newProperty)
        }
        catch(Exception e){log.error e}
    }

    /**
    * Finds a single Space with the specified spacekey.
    * @param spacekey Space key of the space to be found
    * @return the space in question
    */
    Space getSpaceByKey(String spacekey) {
        try {
            return spaceService
                .find(new Expansion('name'))
                .withKeys(spacekey)
                .fetch()
                .get()
        }
        catch (Exception e) {log.warn(e);return null}
    }

    PageResponse<Label> getLabels(String pageid) {
        return contentLabelService.getLabels(ContentId.of(Long.parseLong(pageid)), Label.Prefix.values().toList(), new SimplePageRequest(0, 5))
    }

    PageResponse<Label> addLabels(String pageid, Iterable<Label> labels) {
        return this.addLabels(ContentId.of(Long.parseLong(pageid)), labels)
    }

    PageResponse<Label> addLabels(ContentId pageid, Iterable<Label> labels) {
        return contentLabelService.addLabels(pageid, labels)
    }

    Content getPageById(String pageid) {
        return getPageById(ContentId.of(Long.parseLong(pageid)))
    }

    Content getPageById(ContentId pageid) {
        try {
            return contentService.find(
                new Expansion(Content.Expansions.BODY, new Expansions(new Expansion("storage"))),
                new Expansion(Content.Expansions.ANCESTORS),
                new Expansion(Content.Expansions.SPACE),
                new Expansion(Content.Expansions.VERSION)
                )
                .withId(pageid)
                .fetchOrNull()
        }
        catch (Exception e) {log.error(e);return null}
    }

    String getPageBodyById(String pageid) {
        return getPageById(pageid).getBody().get(ContentRepresentation.STORAGE).getValue()
    }

    Content getContentById(ContentId id) {
        return contentService.find(
            new Expansion(Content.Expansions.BODY, new Expansions(new Expansion("storage"))))
            .withId(id)
            .fetchOrNull()
    }

    Content getContentById(String id) {
        return getContentById(ContentId.of(Long.parseLong(id)))
    }

    /*
    * @see ServiceHelper#getPageIdsOfSpace(Space space, Integer pageCount)
    * @param spaceid Confluence space key
    */
    List<ContentId> getPageIdsOfSpace(String spaceid, Integer pageCount=30) {
        Space space = getSpaceByKey(spaceid)
        return this.getPageIdsOfSpace(space,pageCount)
    }

    /**
    * Returns a list of Page-IDs of all pages in a Space identified via space.
    * @param space Confluence space
    * @param pageCount size of pagination blocks. optional, defaults to 30
    * @return List of Page-IDs as ContentId
    */
    List<ContentId> getPageIdsOfSpace(Space space, Integer pageCount=30){
        def out = new ArrayList<ContentId>()
        def ctRequestResult
        def firstPageId = contentService.find(new Expansion(Content.Expansions.BODY))
            .withSpace(space)
            .fetchOrNull()
            .getId()
            .asLong()
        def curse = ContentCursor.createCursor(false,firstPageId)
        do {
            def spr = new SimplePageRequest(curse,pageCount)
            ctRequestResult = contentService.find(new Expansion(Content.Expansions.BODY))
                .withSpace(space)
                .fetchMany(ContentType.PAGE,spr)
            for (page : ctRequestResult.getResults()) out.add(page.getId())
            curse = ContentCursor.createCursor(false,out[-1].asLong())
        }while( ctRequestResult.hasMore() )
        return out        
    }

    Reference<Content> getContentReference(ContentId pageId){
        //def cs = new ContentSelector()
        //def cont = cs.from(this.getContentById(pageId) as Content)
        return Content.buildReference(pageId)
    }

    Reference<Content> getContentReference(String pageId){
        return getContentReference(ContentId.of(Long.parseLong(pageId)))
    }

}
