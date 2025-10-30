## confluence-fundify
### overall remarks:
Grundsaetzlich werden Calls von der FUNDify API abgerufen, die Infos werden formatiert und pro Call in einer Instanz der Klasse FundifyCall abgespeichert. Es gibt eine Template Seite mit der gewuenschten Formatierung und Platzhalter fuer die entsprechenden Werte. Der PageBuilder schnappt sich den Pagebody vom Template, setzt die Werte von einem Call ein, gibt der neuen Seite eine ContentProperty (-> callRisId) und legt eine neue Seite an. Ueber diese ContentProperty wir
d die Seite nacher identifiziert.
Die gerelle Idee ist einmal am Tag createFundifyPages() aus der Klasse PageHandler laufen zu lassen. Das Ding holt sich die callRisIds von den Seiten die im Space schon existieren und legt Seiten fuer callIDs an die es noch nicht gibt.

### Seiten Udpates:
Es gibt eine Methode alle/eine existierende(n) Seiten upzudaten -> class PageBuilder
Ich hab einen Button in die Template Seite eingebaut ders ermoeglichen soll fuer die Seite ein Update zu machen. Der setzt ein request an einen custom endpoin
t von ScriptRunner ab und der loest dann die updateSpecificPage Methode aus. Ich bild mir ein das hat schon funktioniert, aber das letzte Mal hab ich vor eine
m halben/dreiviertel Jahr dran gearbeitet und war am herumtesten. Und mittlerweile braucht die API credentials, dh ich kann grad nichts ausprobieren ^^

### Fundify:
- Constructor setzt die universityRisId automatisch falls kein Wert uebergeben wird.
- die API braucht seitdem ich sie das letzte Mal verwendet hab Credentials; das muss noch bei callResultUni() und getOneCall() implementiert werden.

### PageHandler:
Da sind im constructor der Space, Page template, etc. vermerkt - entsprechend anpassen fuer den/das verwendeten Space/Template/ParentPage

### FundifyCall:
Hier werden die gewuenschten Infos ausm json vom API call extrahiert und so weiterverarbeitet, dass sie in die Templateseite eingefuegt werden koennen. Bei Be
darf hinzufuegen/wegnehmen. Syntax is: FundifyInformation(variable\_name, \[json,tree,element\]) - eine 0 indiziert, dass eine Aufspaltung nach Sprachen vorliegt.

### FundifyInformation:
Das is der fragilste Teil, weil eigentlich nur Formatierung und anpassen an html/confluence stattfindet. Englisch als Hauptsprache is semi-hardgecoded.
