package fundify

import fundify.*
import util.ServiceHelper
import org.json.JSONArray
import groovy.util.logging.Log4j

@Log4j
class PageHandler{
    ServiceHelper h = new ServiceHelper()
    Fundify f = new Fundify()
    String space, parentPage, templatePage, propertyKey
    PageBuilder bob
    JSONArray apiResult = f.callResultUni()

    public PageHandler(){
        //spacekey of the space where the page is to be created
        this.space = 'CT'
        //page-id of parent of the new page
        this.parentPage = '176324719'
        //page-id of the template used for creating new pages
        this.templatePage = '176324890'
        //name of the ContentProperty the page is identified by
        this.propertyKey = 'callID'
        this.bob = new PageBuilder(this.space, this.parentPage, this.templatePage)    
    }

    def updateAllPages(){
        List knownIds = h.getContentPropertiesOfSpace(space,propertyKey)        
        ArrayList<FundifyCall> allTheCalls = f.getSpecificCalls(apiResult,knownIds)
        for ( call : allTheCalls ) {
            bob.update(call)
        }
    }

    boolean updateSpecificPage(String callID){
        FundifyCall updateMe = f.getOneCall(callID)
        try {
            bob.update(updateMe)
            return (true)
        }
        catch(Exception e){log.error e; return false}
    }

    def createFundifyPages(){
        List knownIds = h.getContentPropertiesOfSpace(space,propertyKey)
        ArrayList<FundifyCall> newCalls = f.getCalls(apiResult,knownIds)
        for ( call : newCalls ){
            bob.build(call)
        }
    }
}
