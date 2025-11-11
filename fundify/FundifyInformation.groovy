package fundify

import org.apache.commons.lang3.StringUtils as SU
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.json.*
import groovy.util.logging.Log4j

@Log4j
class FundifyInformation{
    private String keyword, formattedValue = ''
    private List jsonPath
    Object value
    private String callID
    final List<String> languageFields = ['funder_name','call_name','call_description']
    final Map<String,String> langDict = new HashMap<String,String>() {{
        put("en","English")
        put("de","German")
    }}

    public FundifyInformation(String kw, List jsonPath){
        this.keyword = kw
        this.jsonPath = jsonPath
        this.value = null
    }

    public setValue(Object v){
        this.value = v
        if (this.keyword in languageFields) this.value = formatLanguageFields(v)
    }

    public String getKey(){
        return this.keyword
    }

    public boolean hasKey(String keyword){
        return this.keyword == keyword
    }

    public List getPath(){
        return this.jsonPath
    }

    public String getFValue(){
        return this.formattedValue
    }

    public String toString(){
        return "${getKey()} (${formattedValue.getClass()}//${value.getClass()}):\n $formattedValue\n"
    }

    private void setID(String id){
        this.callID = id
    }

    public String getID(){
        return this.callID
    }

    private Object formatLanguageFields(Object input){
        if ( !input || input instanceof String ) return input
        //some fields have multiple entries additionally to languages
        if ( (input instanceof JSONArray) && (input.get(0) instanceof JSONArray) ){
            try {
                JSONArray newarray = new JSONArray()
                for ( it : input.iterator() as java.util.Iterator<JSONArray> ){
                    JSONObject newentry = new JSONObject()
                    for ( field : it.iterator() as java.util.Iterator<JSONObject> ){
                        newentry.put(field.getString('lang'),field.get('text'))
                    }
                    newarray.put(newentry)
                }
                return newarray
            }
            catch (Exception e){log.error e}
        }
        else{
            try {
                JSONObject newobject = new JSONObject()
                for ( field : input.iterator() as java.util.Iterator<JSONObject> ){
                    newobject.put(field.getString('lang'),field.getString('text'))
                }
                return newobject
            }
            catch (Exception e) {log.error "language filter: $e \n ${input}"}

        }
    }

    private void parseStagesTimeline(){
        JSONArray buffer = new JSONArray()
        for ( int i=0;i<(this.value as JSONArray).length();i++ ) {
            //String stage = "Stage ${i+1}: "
            JSONObject currentStage = (this.value as JSONArray).get(i) as JSONObject
            //stage += "Stage ${currentStage.getString('number')}: "+this.wrapInDateTag(currentStage.getString('callStageStart'))+' - '+this.wrapInDateTag(currentStage.getString('callStageEnd'))
            buffer.put("Stage "+currentStage.getString('number')+": "+this.wrapInDateTag(currentStage.getString('callStageStart'))+' - '+this.wrapInDateTag(currentStage.getString('callStageEnd')))
        }
        this.value = buffer
    }

    private String wrapInDateTag(String date) {
        return """<time datetime=\"${parseDate(date)}\" />"""
    }

    private String parseDate(String incoming) {
        Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}")
        Matcher matcher = datePattern.matcher(incoming)
        return matcher.find() ? matcher.group() : '1970-11-01'
    }

    private void parseToList(){
        if ( this.value instanceof String ) {this.formattedValue = this.value; return}
        String buffer='<p>'
        this.value.each{ it ->
            buffer += '<br/>'+it.toString()
        }
        this.formattedValue = buffer.replaceFirst('<br/>','')+'</p>'
    }

    private void parseToItemizedList(){
        String out = "<ul>"
        try { this.value.each { it -> out = out+'<li>'+it+'</li>' };
            this.formattedValue = out+'</ul>'
        }
        catch (Exception e) {log.error('makeitemizedlist: '+e)}
    }

    private void parseWebsite(){
        String buffer = ''
        for ( line : (this.value as JSONArray).iterator() ){
            buffer += this.wrapHyperLink((line as String).replace("&","&amp;"))
        }
        this.formattedValue = buffer
    }

    private void prettyfiString(){
        JSONArray list = this.value as JSONArray
        for ( int i=0;i<list.length();i++ ){
            String buffer = SU.capitalize(list.getString(i).replace('_',' ').toLowerCase())
            list.put(i,buffer)
        }
        this.formattedValue = list
    }

    private String wrapEmailLink(String email) {
        return """<a class="email" href="mailto:${email}" style="text-decoration: none;margin-left: 1.0px;">${email}</a>"""
    }

    private String wrapHyperLink(String link) {
        return """<p><a class="" href="$link">$link</a></p>"""
    }

    private void parseContacts() {
        String out = ''
        this.value.each { JSONObject contact -> {
            String contactPanel = ""
            contact.keys().each (
                key -> {
                    if ( key == 'name' && contact.get(key) ) contactPanel += "<p><strong>${contact.get(key)}</strong></p>"
                    if ( key == 'phone' && contact.get(key) ) contactPanel += "<p>T: ${contact.get(key)}</p>"
                    if ( key == 'email' && contact.get(key) ) contactPanel += "<p>${wrapEmailLink(contact.get(key) as String)}</p>"
                }
            )
            out += contactPanel
        }}
        this.formattedValue = out
    }

    private void filterLanguage(String lang='en') {
        if ( this.value instanceof JSONArray ){
            def filtered = new JSONArray()
            for ( info : this.value.iterator() as Iterator<JSONObject> ){
                try{ filtered.put(info.getString(lang)) }
                catch(Exception e){ filtered.put("No ${langDict[lang].toLowerCase()} version provided.")}
            }
            this.formattedValue = filtered
        }
        if ( this.value instanceof JSONObject ){
            try { this.formattedValue = this.value.getString(lang) }
            catch(Exception e){log.error e; this.formattedValue = "No ${langDict[lang].toLowerCase()} version provided."}
        }
        
    }

    /*this is ugly and only works for 2 languages or less bc try/catch
    */
    private void filterLanguage2(){
        switch (this.value.getClass()){
            case 'class org.json.JSONObject':
                try{
                    this.value = (this.value as JSONObject).getString('en')
                }
                catch (JSONException je){
                    try { this.value = (this.value as JSONObject).getString('de') }
                    catch (JSONException aje){log.error aje; this.formattedValue='no information provided(jObj).'}
                }
                break;
            case 'class org.json.JSONArray':
                def filtered = new JSONArray()
                for ( info : this.value.iterator() as Iterator<JSONObject> ){
                    try {
                        filtered.put(info.getString('en'))
                    }
                    catch(JSONException je){
                        try { filtered.put(info.getString('de')) }
                        catch (JSONException aje){log.error aje; filtered='no information provided(jArr).'}
                    }
                    finally{
                        this.value = filtered
                    }
                }
                break;
            default:
                this.formattedValue = 'There has been a problem filtering the language.'
        }
    }

    private void formatProjectDuration() {
        String out = ''
        def JSONObject jason = this.value as JSONObject
        for ( key : jason.keys() ) {
            out += jason.get(key) != 0 ? ", ${jason.get(key)} ${key}" : '';
        }
        this.formattedValue = out.replaceFirst(', ','')
    }

    private void parseApplicationLanguages() {
        JSONArray out = new JSONArray()
        for ( entry : this.value ) { out.put(langDict[entry]) }
        this.value = out
    }

    private void formatMoney() {
        String formatter = '%,d'
        this.formattedValue = String.format(formatter,(this.value as String).toInteger())
    }

    private void escapeChars() {
        this.formattedValue = this.formattedValue.replace("&","&amp;")
        this.formattedValue = this.formattedValue.replace("<","&lt;")
    }

    public void parse(){
        switch (keyword){
            case 'call_website':
                parseWebsite()
                break;
            case 'call_stages':
                this.formattedValue = wrapInDateTag(this.value as String)
                break;
            case 'call_stages_timeline':
                parseStagesTimeline()
                parseToList()
                break;
            case 'call_characteristics':
            case 'target_groups':
                prettyfiString()
                parseToItemizedList()
                break;
            case 'call_contacts':
                parseContacts()
                break;
            case 'funder_name':
            case 'call_name':
                filterLanguage2()
                parseToList()
                break;
            case 'call_description':
                filterLanguage2()
                parseToList()
                escapeChars()
                break;
            case 'min_project_duration':
            case 'max_project_duration':
                formatProjectDuration()
                break;
            case 'legal_type':
                this.formattedValue = (this.value as String).replace('PROJECT','')
                break;
            case 'funder_acronym':
            case 'funder_name':
                parseToList()
                break;
            case 'application_language':
                parseApplicationLanguages()
                parseToList()
                break;
            case 'min_project_volume':
            case 'max_project_volume':
                formatMoney()
                break;
            default:
                this.formattedValue = this.value
        }
    }
}
