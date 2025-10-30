package fundify

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

import util.ServiceHelper

@BaseScript CustomEndpointDelegate delegate
updateFundifyPage(
    httpMethod: "POST", groups: ["confluence-administrators", "ct-admin"]
) { MultivaluedMap queryParams, String body ->
    def jason = new JsonSlurper().parseText(body)
    def ph = new PageHandler()
    String risID = jason['risID']
    if (queryParams){
        def out = queryParams.getFirst('pageId')
        Response.ok(JsonOutput.toJson([type:'success',title:'page updated',body:"${out}"])).build()
    }
    else{
        def updated = true;ph.updateSpecificPage(risID)
        if (updated) {
            def result = [
                message: "$risID received, page update successful."
            ]
            Response.ok(JsonOutput.toJson(result)).build()
        }
        else {
            Response.status(500).build()
        }
    }

}

updateFundifyPage(
    httpMethod: "GET", groups: ['confluence-administrators', 'ct-admin']
) { MultivaluedMap queryParams ->
    def pageId = queryParams.getFirst('pageId') as String
    def ph = new PageHandler()
    if ( pageId ){
        def h = new ServiceHelper()
        def risID = h.getContentProperty(pageId,'callID')
        def updated = ph.updateSpecificPage(risID)
        def flag = [
            type: 'success',
            body: 'Reload page to see changes.'
        ]
        if ( updated ) {
            Response.ok(JsonOutput.toJson(flag)).build()
        }
        else {
            flag.type = 'error'
            flag.body = 'Update not successful.'
            Response.ok(JsonOutput.toJson(flag)).build()
        }
    }
    else Response.status(500).build()
}
