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
Im HM dagegen wird die Historie der Tweets über einen gewünschten Zeitraum analysiert.
Beide Modi können seperat genutzt werden, allerdings werden im Standart-Usecase der Anwendung gemeinsam genutzt. 

Die Anwendung soll gleichzeitig von vielen Nutzern genutzt werden können. 

Die gesuchten Tweets sollen von der Twitter-API importiert, von der Sentiment-Analyse annotiert und über die Website ausgegeben. 

Folgende Aggregationen / Darstellungen sollen möglich sein:
* Anzahl der analysierten Tweets
* simplen Auflistung einer gewissen Zahl annotierter Tweets 
* eine Ausgabe des durchschnittlichen Sentiments (Zahlenwert, mglw mit Trend in Form von Pfeil / Ampel-Symbolik)
* „Highlight“-Tweets (best-/schlechtbewertetester Tweet)
* ein zeitbasierter Graph des Sentiments 

#### Rahmenbedingungen
Der Apache Kafka des Kunden soll als „Datenbank“ des Projekts genutzt werden.

#### Funktionale Anforderungen

#### Nichtfunktionale Anforderungen



 
