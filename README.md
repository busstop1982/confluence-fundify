## confluence-fundify
### overall remarks:
Grundsaetzlich werden Calls von der FUNDify API abgerufen, die Infos werden formatiert und pro Call in einer Instanz der Klasse FundifyCall abgespeichert. Es gibt eine Template Seite mit der gewuenschten Formatierung und Platzhalter fuer die entsprechenden Werte. Der PageBuilder schnappt sich den Pagebody vom Template, setzt die Werte von einem Call ein, gibt der neuen Seite eine ContentProperty (-> callRisId) und legt eine neue Seite an. Ueber diese ContentProperty wird die Seite nacher identifiziert.
Die gerelle Idee ist einmal am Tag createFundifyPages() aus der Klasse PageHandler laufen zu lassen. Das Ding holt sich die callRisIds von den Seiten die im Space schon existieren und legt Seiten fuer callIDs an die es noch nicht gibt.

### Seiten Udpates:
Es gibt eine Methode alle/eine existierende(n) Seiten upzudaten -> class PageBuilder
Updates einzelner Seiten funktionieren ueber ein UI-Fragment das im '...'-Menu im entsprechenden Space fuer Leute mit edit-permissions auftaucht - verwendet das GET-request in fundify/rest\_update

[UI-Fragment config](https://github.com/busstop1982/confluence-fundify/blob/main/ui_frag.png)

[custom REST-endpoint config](https://github.com/busstop1982/confluence-fundify/blob/main/rest_endpoint.png)

Als Alternative hab ich einen Button in die Template Seite eingebaut ders ermoeglichen soll fuer die Seite ein Update zu machen. Der setzt ein request an einen custom endpoint von ScriptRunner ab (siehe fundify/rest\_conditional.groovy und fundify/rest\_update.groovy) und der loest dann die updateSpecificPage Methode aus. Ich glaub nicht dass ich den funktional hatte, aber das letzte Mal hab ich vor einem halben/dreiviertel Jahr dran gearbeitet und war am herumtesten. Und mittlerweile braucht die API credentials, dh ich kann grad nichts ausprobieren ^^

### fundify/Fundify:
- Constructor setzt die universityRisId automatisch falls kein Wert uebergeben wird.
- die API braucht seitdem ich sie das letzte Mal verwendet hab Credentials; das muss noch bei callResultUni() und getOneCall() implementiert werden.

### fundify/PageHandler:
Da sind im constructor der Space, Page template, etc. vermerkt - entsprechend anpassen fuer den/das verwendeten Space/Template/ParentPage

### fundify/FundifyCall:
Hier werden die gewuenschten Infos ausm json vom API call extrahiert und so weiterverarbeitet, dass sie in die Templateseite eingefuegt werden koennen. Bei Bedarf hinzufuegen/wegnehmen. Syntax is: FundifyInformation(variable\_name, \[json,tree,element\]) - eine 0 im json-tree indiziert, dass eine Aufspaltung nach Sprachen vorliegt.

### fundify/FundifyInformation:
Das is der fragilste Teil, weil eigentlich nur Formatierung und anpassen an html/confluence stattfindet - war sehr viel trial&error. Englisch als Hauptsprache is semi-hardgecoded.

### util/FundifySqlHelper:
Eine potentiell ressourcenschonendere Variante um die schon vorhandenen Calls zu eruieren. Damit koennte man die getContentPropertiesOfSpace im PageHandler ersetzen.

### fundify/rest\_conditional fundfiy/rest_update:
Vorlagen fuer die custom Endpoints fuers Seiten Udpates. Hier sind Spaces-Key und Gruppen hardgecoded die! rest\_conditional ueberprueft ob die ausfuehrende Person Editierrechte auf der Seite hat; im Endpoint selber kann das Request nur von den aufgefuehrten Gruppen abgesetzt werden.

### macro Folder:
Macro Code und js File fuer einen Button der ein Seiten-Update ausloest. Ist bei uns auf der Template-Seite eingefuegt und hat noch einiges an debug-info dabei, weil in der Experimentierphase.
