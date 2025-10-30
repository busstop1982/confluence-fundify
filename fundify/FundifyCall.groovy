package fundify

import fundify.FundifyInformation
import groovy.util.logging.Log4j
import org.json.*

@Log4j
class FundifyCall {
    private JSONObject apiResult
    private String callID, pageTitle = ' - '
    private ArrayList<FundifyInformation> infoList

    public FundifyCall(JSONObject apiResult){
        
        this.apiResult = apiResult
        JSONArray identifiers = apiResult.getJSONObject('call').getJSONArray('identifiers')
        for ( i : identifiers.iterator() as Iterator<JSONObject> ){
            if ( i.getString('type') == 'RIS_SYNERGY' ) this.callID = i.getString('value')
        }
        this.infoList = new ArrayList<FundifyInformation>(){{
                //add(new FundifyInformation('ris_syn_id', ['call','identifiers','0','value']))
                add(new FundifyInformation('call_acronym', ['call','acronym']))
                add(new FundifyInformation('funder_acronym', ['call','funder','0','funder','acronym']))
                add(new FundifyInformation('funder_name', ['call','funder','0','funder','name']))
                add(new FundifyInformation('call_characteristics', ['call', 'characteristics']))
                add(new FundifyInformation('call_description',['call','description']))
                add(new FundifyInformation('target_groups', ['call','targetGroups']))
                //add(new FundifyInformation('subjects',['call','subjects']))
                add(new FundifyInformation('internal_deadline',['annotation','internalDeadline','0','text']))
                add(new FundifyInformation('call_id',['call','id']))
                add(new FundifyInformation('call_name',['call','name']))
                add(new FundifyInformation('call_stages_timeline',['call','callStages']))
                add(new FundifyInformation('call_type',['call','type']))
                add(new FundifyInformation('legal_type',['call','legalType']))
                add(new FundifyInformation('call_website',['call','website']))
                add(new FundifyInformation('call_contacts',['call','contacts']))
                add(new FundifyInformation('min_project_volume',['call','minProjectVolume','amount']))
                add(new FundifyInformation('min_project_volume_curr',['call','minProjectVolume','currency']))
                add(new FundifyInformation('max_project_volume',['call','maxProjectVolume','amount']))
                add(new FundifyInformation('max_project_volume_curr',['call','maxProjectVolume','currency']))
                add(new FundifyInformation('min_project_duration',['call','minProjectDuration']))
                add(new FundifyInformation('max_project_duration',['call','maxProjectDuration']))
                add(new FundifyInformation('application_language',['call','applicationLanguages']))
            }}

        for ( tidbit : this.infoList ){
            def infoValue = this.goDeeper(apiResult,tidbit.getPath())
            tidbit.setValue(infoValue)
            tidbit.parse()
            if ( tidbit.getKey() == 'call_name' ) this.pageTitle = tidbit.getFValue()+pageTitle
            if ( tidbit.getKey() == 'call_acronym' ) this.pageTitle += tidbit.getFValue()
        }

    }

    private Object goDeeper (Object json, List keywords) {
        if ( json instanceof String || keywords.isEmpty() ) {return json}
        try {
            String word = keywords.remove(0)
            if ( json instanceof JSONObject ) {
                return goDeeper(json.get(word),keywords)
            }
            if ( json instanceof JSONArray ) {
                if ( json.length() == 1 ) return goDeeper(json.get(0),keywords);
                JSONArray out = new JSONArray()
                for ( foo : json.iterator() as java.util.Iterator<JSONObject>) {
                    List copywords = new ArrayList(keywords)
                    out.put(goDeeper(foo,copywords))
                }
                return out//goDeeper(json.get(0),keywords)
            }
        }
        catch (Exception e) {log.error('goDeeper: '+e); return ""}
    }

    public ArrayList<FundifyInformation> getInfoList(){
        return this.infoList
    }

    /**
    Return FundifyInformation object that matches the keyword
    */
    public FundifyInformation getFFInfoByKeyword(String keyword){
        for ( fufi : infoList ) {
            if ( fufi.hasKey(keyword) ) return fufi
        }
    }

    public String toString(){
        String out = '~~~ RIScallID: '+this.callID+' ~~~\n'
        for ( it : this.infoList ) out += it.toString()
        return out
    }

    /**
    Returns call-specific information to be inserted into the page template as a JSONObject.
    */
    public JSONObject getPageTemplateInfo(){
        def out = new JSONObject()
        for ( i : this.infoList ) {
            out.put(i.getKey(),i.getFValue())
        }
        return out
    }

    public String getPageTitle(){
        return this.pageTitle
    }

    public String getCallId(){
        return this.callID
    }

}
