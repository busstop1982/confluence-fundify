package fundify

import com.atlassian.confluence.json.json.Json
import fundify.FundifyCall
import java.sql.Array
import groovy.json.JsonSlurper
import groovyx.net.http.*
import java.net.URL
import org.json.*
import groovy.util.logging.Log4j

@Log4j
class Fundify {
    String generalFundifyURL
    String apiToken

    public Fundify(String universityRisId = 'ris:UE:orgunit:3000'){
        this.generalFundifyURL = "https://fundify-staging.arisnet.ac.at/api/ris-synergy/funding/v1/annotated-calls/university/${universityRisId}"
        this.setApiToken()
    }

    void setApiToken(){
        def tokenReply
        JsonSlurper slurp = new JsonSlurper()
        HTTPBuilder tokenrequest = new HTTPBuilder("https://id.arisnet.ac.at/realms/fundify/protocol/openid-connect/token")
        String clientId = "your_client_here"
        String clientSecret = "your_secret_here"
        tokenrequest.request(Method.POST, ContentType.URLENC){
                            body = [grant_type:'client_credentials', client_id:clientId, client_secret:clientSecret]
                            response.success = { resp, HashMap json ->
                                //json is a hashmap with all the information stored in the key - hence the following abomination                                
                                tokenReply = slurp.parseText(json.keySet().toString())
                                this.apiToken = tokenReply["access_token"][0]
                            }
                            response.failure = { HttpResponseDecorator resp ->
                                log.error resp.entity.content.text
                            }
                        }          
    }

    private String extractRisId(JSONObject apiCall){
        JSONArray idefix = apiCall.getJSONObject('call').getJSONArray('identifiers')
        for ( i : idefix.iterator() as Iterator<JSONObject> ){
            if ( i.getString('type') == 'RIS_SYNERGY' ) return i.getString('value')
        }
    }

    JSONArray callResultUni() {
        def slurpy = new JsonSlurper()
        def http = new HTTPBuilder(this.generalFundifyURL)
        http.headers["Authorization"] = "Bearer ${this.apiToken}"
        def jsonInfo = new JSONObject()
        http.request(Method.GET, ContentType.JSON) {
            response.success = { HttpResponseDecorator resp ->
                String foo = resp.entity.content.text
                jsonInfo = slurpy.parseText(foo)
            }
            response.failure = { HttpResponseDecorator resp ->
                log.error resp.entity.content.text
            }
        }
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
        def slurpy = new JsonSlurper()
        def http = new HTTPBuilder(this.generalFundifyURL+'/call/'+wantedCallId.replace(' ','%20'))
        http.headers["Authorization"] = "Bearer ${this.apiToken}"
        def jsonInfo = new JSONObject()
        try {
            http.request(Method.GET, ContentType.JSON) {
                response.success = { HttpResponseDecorator resp ->
                    String foo = resp.entity.content.text
                    jsonInfo = slurpy.parseText(foo)
                }
                response.failure = { HttpResponseDecorator resp ->
                    log.error resp.entity.content.text
                }
            }
        } catch (Exception e) {log.error e}
        FundifyCall out = new FundifyCall(jsonInfo as JSONObject)
        return out
    }

}