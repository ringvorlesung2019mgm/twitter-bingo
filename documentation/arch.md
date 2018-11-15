## Architekturentwurf

#### Grundsätzlicher Aufbau

![There should be an architecture diagram here!](arch.png "Logo Title Text 1")

Das Frontend generiert nach dem Aufruf durch den Nutzer eine Session-ID (kurz SID). 
Der Nutzer gibt über das Web-Frontend eine Query (SID,Suchbegriff,Filterkriterien) ein.
Das Webfrontend sendet die Query an eine REST-API im Backend der Anwendung.
Im Backend wird ein Abonnement für diesen Suchbegriff erstellt.
Basierend auf den Abos werden Rohtweet (RT) Producer gestartet, die von der Twitter API Daten abrufen und in den Apache Kafka schreiben.
Es werden auch RT-Consumer gestartet, die alle Tweets, die in Rohtweet-Topics geschrieben werden, durch die Sentiment-Analyse annotieren und über den Analysetweet (AT) Producer in den Apache-Kafka schreiben. 
AT-Consumer warten darauf, das Tweets in die Analyse-Topics geschrieben werden, senden durch das Backend über Chunked-Responses die annotierten Tweets zurück an das Web-Frontend.

#### Topics

Es existieren 2 Arten von Topics: Rohtopics und Analysetopics

Namensschema für Prototyp:
Rohtopic -> UNI\_raw\_{Suchbegriff}
Rohtopic -> UNI\_anno\_{Suchbegriff}

Geplante Erweiterungen:
* Geokoordinaten
* User-Handle

#### Datenstrukturen

TODO Tweets
TODO Annotierte Tweets
TODO Abos

####




