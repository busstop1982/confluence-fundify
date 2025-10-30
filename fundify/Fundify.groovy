package fundify

import fundify.FundifyCall
import java.sql.Array
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import java.net.URL
import org.json.*
import groovy.util.logging.Log4j

@Log4j
class Fundify {
    String generalFundifyURL

    public Fundify(String universityRisId = 'ris:UE:orgunit:3000'){
        this.generalFundifyURL = "https://fundify.arisnet.ac.at/api/ris-synergy/funding/v1/annotated-calls/university/${universityRisId}"
    }

    private String extractRisId(JSONObject apiCall){
        JSONArray idefix = apiCall.getJSONObject('call').getJSONArray('identifiers')
        for ( i : idefix.iterator() as Iterator<JSONObject> ){
            if ( i.getString('type') == 'RIS_SYNERGY' ) return i.getString('value')
        }
    }

    JSONArray callResultUni() {
        def urmel = new URL(generalFundifyURL)
        def slurpy = new JsonSlurper()
        def http = new HTTPBuilder()
        def jsonInfo = new JSONObject()
        try {
            http.request(urmel, Method.GET, ContentType.JSON) {
                uri.path = urmel.getPath()
                response.success = { groovyx.net.http.HttpResponseDecorator resp ->
                    String foo = resp.entity.content.text
                    jsonInfo = slurpy.parseText(foo)
                }
            }
        } catch (Exception e) {log.error('callresult: '+e)}

        JSONArray obj = new JSONArray(jsonInfo)
        return obj
    }

    ArrayList<FundifyCall> getCalls(JSONArray input = callResultUni(), List createdPageCallIds = new ArrayList<>()){
        List out = new ArrayList<FundifyCall>()
        for (int i = 0; i<input.length(); i++ ) {
            JSONObject buffer = input.getJSONObject(i)
            String callID = extractRisId(buffer)
            if ( createdPageCallIds.contains(callID) ) { log.info "$callID exists"; continue }
            FundifyCall fuficall = new FundifyCall(buffer)
            out.add(fuficall)
        }
        return out
    }

    ArrayList<FundifyCall> getSpecificCalls(JSONArray apiResult = callResultUni(), List wantedCallIds){
        List out = new ArrayList<FundifyCall>()
        for (int i = 0; i<apiResult.length(); i++) {
            JSONObject buffer = apiResult.getJSONObject(i)
            String callID = extractRisId(buffer)
            if ( wantedCallIds.contains(callID) ) {
                FundifyCall fuficall = new FundifyCall(buffer)
                out.add(fuficall)
            }
        }
        return out
    }

    /**
    @param wantedCallId format: ris:<funder_acronym>:funding:<callID>
    */
    FundifyCall getOneCall(String wantedCallId){
        def urmel = new URL(this.generalFundifyURL+'/call/'+wantedCallId)
        def slurpy = new JsonSlurper()
        def http = new HTTPBuilder()
        def jsonInfo = new JSONObject()
        try {
            http.request(urmel, Method.GET, ContentType.JSON) {
                uri.path = urmel.getPath()
                response.success = { groovyx.net.http.HttpResponseDecorator resp ->
                    String foo = resp.entity.content.text
                    jsonInfo = slurpy.parseText(foo)
                }
            }
        } catch (Exception e) {log.error e}
        FundifyCall out = new FundifyCall(jsonInfo as JSONObject)
        return out
    }

}
