## Anforderungsdefinition

#### Vorstellung Ist-Zustand

Einige Bestandteile der Architektur sind durch den Kunden vorgegeben. 
Die Sentiment-Analyse ist bereits vorhanden und wird im Rahmen dieses Projekts als eine Blackbox betrachtet.
Der Kunde verfügt über eine Apache Kafka Instanz, die für dieses Projekt die Rolle der „Datenbank“ einnehmen soll. 
Der Kunde verfügt über einen Twitter-API-Schlüssel, über den die gesuchten Tweets ausgelesen werden sollen.
Im Rahmen der Ringvorlesung des letzten Jahres wurde mit dem Kundenunternehmen ebenfalls ein Projekt basierend auf der Twitter-API durchgeführt. (TODO Ref zu OSTMap einfügen)
Teile des Frontends könnten von uns wiederverwendet werden. 

#### Vorstellung des Soll-Zustands

Im Rahmen dieses Projekts soll eine Anwendung entstehen, in der über eine Webseite eine Sentiment-Analyse zu einem Suchbegriff auf Twitter durchgeführt werden kann. 
Die Suche soll zusätzlich durch Zeitraum, geografischen Bereich und Verkettung von Suchbegriffen eingeschränkt werden können. 
Die Anwendung soll über zwei Modi verfügen: Live-Modus (LM) und History-Modus (HM)
Im LM werden Tweets, die aktuell gepostet wurden in Echtzeit ausgewertet und dem Nutzer angezeigt.
Im HM dagegen wird nur die Historie der Tweets über einen gewünschten Zeitraum analysiert.
Beide Modi können seperat genutzt werden, allerdings werden im Standard-Usecase der Anwendung gemeinsam genutzt.

Die Anwendung soll gleichzeitig von vielen Nutzern genutzt werden können. 

Die gesuchten Tweets sollen von der Twitter-API importiert, von der Sentiment-Analyse annotiert und über die Website ausgegeben. 

Folgende Aggregationen / Darstellungen sollen möglich sein:
* Anzahl der analysierten Tweets / Anzahl positiv annotiert / Anzahl negativ annotiert
* simplen Auflistung einer gewissen Zahl annotierter Tweets (Relevanzanalyse zur Reduktion der darzustellenden Tweets )
* eine Ausgabe des durchschnittlichen Sentiments (Zahlenwert, mglw mit Trend in Form von Pfeil / Ampel-Symbolik)
* „Highlight“-Tweets (best-/schlechtbewertetester Tweet)
* ein zeitbasierter Graph des Sentiments 

#### Rahmenbedingungen
Der Apache Kafka des Kunden soll als „Datenbank“ des Projekts genutzt werden.

#### Funktionale Anforderungen

##### Frontend

| Anf. | Beschreibung | Abh. | Prio. |
|---|---|---|---|
| F1 | Die Bedienoberfläche enthält einen Eingabebereich, über den die gewünschte Query eingegeben werden kann. | - | Muss |
| F1-1 | Der Eingabebereich enthält ein Eingabefeld, über das ein Suchbegriff eingegeben werden kann. | - |  Muss |
| F1-1-1 | Die im Eingabefeld können mehrere Suchbegriffe verkettet (UND, ODER, XOR) werden. | - | Kann |
| F1-2 | Über den Eingabebereich kann der Zeitraum der Suche ausgewählt werden. | - | Soll |
| F1-3 | Über den Eingabebereich kann der HM aktiviert werden. | - | Muss |
| F1-4 | Über den Eingabebereich kann der LM aktiviert werden. | - | Muss |
| F1-5 | HM und LM können kombiniert werden. | - | Muss |
| F1-6 | Es kann eine Bereich für die Suche ein einem Rahmen bestimmter Geopositionen ausgewählt werden. | - | Kann |
| F2 | Die Bedienoberfläche enthält einen Ausgabebereich, über den die Ergebnisse der Query dargestellt werden. | - | Muss |
| F2-1 | Die Anzahl der analysierten Tweets wird dargestellt. | - |  Muss |
| F2-1-1 | Die Anzahl der positiv/negativ bewerteten Tweets wird dargestellt. | - |  Soll |
| F2-2 | Die annotierten Tweets werden in einer Liste dargestellt. | - |  Muss |
| F2-3 | Das durchschnittliche Sentiment wird als Zahlenwert dargestellt. | - |  Muss |
| F2-4 | Die Highlight-Tweets (best-/schlechtbewertester Tweet) werden dargestellt.  | - |  Soll |
| F2-5 | Die Bewertungen der Tweets wird als Graph über den gewünschten Zeitraum dargestellt.  | - | Soll |
| F3 | Im Live-Modus wird der Ausgabebereich in Echtzeit geupdatet.  | - | Muss |

##### Backend

| Anf. | Beschreibung | Abh. | Prio. |
|---|---|---|---|
| F4 | Die Query wird vom Frontend an das Backend gesendet und dort ausgewertet. | - | Muss |
| F5 | Die gesuchten Tweets werden von Twitter über die Twitter-API in das System importiert. | - |  Muss |
| F6 | Die importierten Tweets werden durch die Sentiment-Analyse annotiert. | - | Muss |
| F7 | Das Ergebnis der Query kann vom Frontend aus dem Backend abgerufen werden.  | - | Muss |


#### Nichtfunktionale Anforderungen

##### Frontend

| Anf. | Beschreibung | Abh. | Prio. |
|---|---|---|---|
|NF1|Die Anwendung soll von bis zu 100 Personen gleichzeitig genutzt werden können.|-|Muss|
|NF2|Die Bereitstellung der von 500 historischen Tweets darf nicht länger als 30 Sekunden dauern.|-|Soll|
|NF3|Im History-Modus können die historischen Tweets bis zu 7 Tage in der Vergangenheit ausgewertet werden.|-|Soll|
|NF4|Bei Bereitstellung aktueller Tweets dürfen nicht mehr als 30 Sekunden zwischen dem Empfang der Tweets und der Bereitstellung für das Frontend liegen.|-|Soll|

##### Backend

| Anf. | Beschreibung | Abh. | Prio. |
|---|---|---|---|
|NF5|Das Frontend benötigt für die Darstellung einer Anfrage nicht länger als 60 Sekunden.|-|Soll|
|NF6|Die Darstellung von 500 historischen Tweets darf nicht länger als 30 Sekunden dauern.|-|Soll|
|NF7|Bei Bereitstellung aktueller Tweets dürfen nicht mehr als 30 Sekunden zwischen Bereitstehen der Tweets im Backend und der Datstellung im Frontend liegen.|-|Soll|
