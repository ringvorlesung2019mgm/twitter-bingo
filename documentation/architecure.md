# **Architektur**

# Komponenten
Endziel: Jede Komponente ist ein eigenständiges Programm und kommuniziert mit den anderen Komponenten ausschließlich über Kafka (und ggf. Zookeeper). Von jeder Komponente können jederzeit mehrere Instanzen gleichzeizig laufen um Lastverteilung, Ausfallsicherheit und unterbrechungsfreie Updates zu ermöglichen. (Microservice)

Übergangsweise wird ersteinmal davon ausgegangen, dass von jeder Komponente nur eine Instanz existiert. Außerdem können mehrere Komponenten (z.B. Producer und Webapp) in einem einzelnen Programm zusammengefasst werden. (Monolith)


![alt text](arch.png "Logo Title Text 1")


## **Producer**
Der Producer ruft basierend auf von den Endnutzern vorgegebenen Filterkriterien (Hashtags,Userhandles,Geolocation) Tweets von der Twitter-API ab und speichert sie in Kafka. Die Tweets werden dabei anhand der Filterkriterien auf mehrere Kafka-Topics aufgeteilt (es landen NICHT alle tweets im gleichen topic!).

Die Anfrage eines Nutzers nach Tweets die einem bestimmten Filter entsprechen wird im folgenden Abo genannt.

Jeder Producer bearbeitet mit einem Thread pro Abo parallel mehrere Abo. Existiert zu einem von einem Nutzer angeforderten Abo bereits ein Producer-Thread (Nutzer a analysiert bereits #tatort und Nutzer b will das nun auch tun) wird kein neuer Thread gestartet, sondern der bereits bestehende weiter verwendet.

Da mehrere Producer-Instanzen parallel arbeiten sollen müssen einige Dinge beachtet werden:
Jedes Abo muss GENAU EINER Instanz zugeordnet werden
Fällt eine Instanz aus müssen die anderen Instanzen deren Abos übernehmen
Existiert ein Abo bereits in einer Instanz darf keine weitere Instanz ein identisches Abo bearbeiten, da sonst die tweets mehrfach in ihrem Kafka-Topic vorliegen. (möchte ein neuer Nutzer die selben tweets beobachten soll er das bestehende Abo mit nutzen)
Wenn sich kein aktiver Nutzer mehr für einen bestimmtes Abo interessiert soll das Abo (also der Producer-Thread) (nach einer gewissen Zeit) entfernt werden. Gerade wenn mehrere Nutzer ein Abo teilen muss sichergestellt werden, dass der Thread erst beendet wird, wenn DER LETZTE NUTZER der Abos sich nicht mehr dafür Interessiert.

Die Realtime-Streaming-API von Twitter liefert nur neu erstellte Tweets. Ein Nutzer der einen bestimmten Hashtag analysiert müsste also warten, bis neue Tweets zu diesem Hashtag erstellt werden. Allerdings bietet Twitter eine weitere API um (bis zu 7 Tage) alte Tweets abzurufen. Beide APIs werden kombiniert benutzt um sowohl bestehende als auch neue Tweets zu erhalten.
Wird ein Producer-Thread gestartet muss dieser prüfen ob und bis zu welchem Zeitpunkt bereits Tweets zu diesem Filterkriterium in Kafka vorhanden sind und nachfolgend nur neuere historische Tweets von der Twitter-API laden. Parallel dazu startet er mit dem Streaming neuer Tweets zu seinem Hashtag.

### **Überganslösung Producerverwaltung**
Da die Organisation von mehreren unabhängigen Producer-Instanzen mit einigem Aufwand verbunden ist, wird vorerst mit einem einzelnen Producer gearbeitet. Dadurch reduziert sich der Verwaltungsaufwand erheblich. Außerdem wird der Producer in den Webserver integriert, wodurch die Kommunikation zwischen Producer und Webapp bequem mittels Java passieren kann (kein IPC, Kafka, Zookeeper o.ä. nötig).

### **Topic-Struktur**
Wie erwähnt werden nicht alle Tweets in einem einzelnen Topic gespeichert, sondern anhand der Filterkriterien (im einfachsten Fall der Hashtag über den sie abgerufen wurden) auf mehrere Topics verteilt.

Das Topic wird aus dem Query erzeugt un hat das folgende Format:

UNI_tweets_hashtag_mentioneduser

UNI = Prefix für alle Topics des Projektes
tweets = präfix für alle topics die tweets enthalten
hashtag = Der Hashtag des Queries
mentioneduser = Der Nutzername nach dem die Tweets gefiltert wurden

Kommt eine Komponente nicht im Query vor, wird sie als leerer String betrachtet, die Trennzeichen müssen dennoch vorkommen.  
Beispiel ohne mentioneduser: UNI_tweets_hashtag__

Enthält eine Komponente mehrere Teile (z.b. bei filterung nach mehreren Hashtags) werden diese durch Punkte (".") getrennt. Einzelne Komponenten sind (sofern sinnvoll) alphabetisch zu sortieren.  

In Zukunft können weitere Filterkriterien (z.b. geokoordinaten) ergänzt werden.


## **Analyzer**
Der Analyzer liest die Tweets die der Producer in die verschiedenen Topics geschrieben hat und führt eine Sentiment-Analyse darauf aus.
Der Tweet (erweitert um das Ergebnis der Analyse) wird anschließend wieder (in ein neues topic) in Kafka gespeichert. Die ursprünglliche Zuordnung der Tweets zu unterschiedlichen Topics muss dabei natürlich erhalten bleiben.

Das topic der verarbeiteten tweets entspricht den der rohtweets mit dem Suffix "-analyzed"

Sollte sich herausstellen das die Sentiment-Analyse ein Performance-Engpass ist, da Tweets stellenweise mehrfach analysiert werden (z.B. da sie mehrere Hashtags enthalten und somit mehreren verschiedenen Filterkriterien entsprechen) könnte man einen Cache-Server (Memcached, Redis) hinzuziehen.

Der Analyzer ist ein eigenständiges Python-Programm und kommuniziert mit den restlichen Komponenten ausschließlich über Kafka.

## **Webapp**
Die Webapp besteht aus zwei Komponenten, Backend und Frontend.
Das Backend liest die fertig analysierten Tweets aus Kafka (die zu dem jeweiligen Nutzer gehören) und übermittelt sie an das Frontend, welches sie dem Nutzer anzeigt. Außerdem ermöglicht das Frontend dem Nutzer anzugeben welche Tweets er analysieren möchte. Diese Analyseanfrage wird an das Backend übermittelt, welches diese Anfrage letztendlich an die Producer übermittelt.

### **Frontend**
Das Frontend bietet dem Nutzer die Möglichkeit Queries an das Backend zu schicken und zeigt die Ergebnisse der Querries auf mehrere Arten an:  
- Lister der n aktuellsten Tweets samt Analyseergebnis
- Positivste / Negativste Tweets der letzten X Minuten
- Graph der zeitlichen entwicklung der durchschnittlichen (oder summierten) Stimmung

### **Backend**
Das Backend stellt APIs zur Verfügung die jeweils ein Query entgegennehmen und Ergebnisse zurückgeben.
Für jedes Frontendelement (Tweetliste, Graph etc.) existiert ein eigener API-Endpunkt der die Ergebnisse in einer für das Frontenelement geeigneten Art zurückgibt.

Wenn ein eingehender API-Aufruf erfordert, dass ein neuer Producer gestartet wird (da zu dem angefragten Query noch kein PRoducer aktiv ist), so sorgt das Backend dafür das ein entsprechender Producer gestartet wird.

# Implementierungsdetails

## **Kommuniktation Frontend-Backend**
Das Frontend soll in Echtzeit aktualisiert werden. Dafür müssen Daten vom Backend gestreamt werden.
Dafür werden  HTTP/1.1 chunked responses verwendet.

## **Lebenszeit von Producer-Threads**
Jeder Producer-Thread liefet Daten für einen oder mehrere aktive Nutzer. Sobald es 5 Minuten lang keinen aktiven Nutzer zu einem Producer-Thread mehr gibt wird dieser beendet.

## **Zuordnung von Nutzern**
Damit Queries und deren Ergebnisse ihrem jeweiligen Nutzer zugeordnet werden können, müssen die Nutzer identifizierbar sein. Daher erhält jeder User eine zufällige aber eindeutige SessionID. Diese wird in einem Cookie gespeichert und muss bei jedem API-Aufruf mitgegeben werden.

## **Zuordnung von Abos zu Producern**
ZURZEIT NOCH NICHT RELEVANT.  
Siehe Producer/Übergangslösung
Nutzeranfragen nach Tweets müssen EXAKT EINEM Producer zugeordnet werden. Ich denke das dafür ein System für verteiltes Locking zwingend nötig ist. Da Kafka sowieso einen Zookeeper benötigt würde es sich anbieten Zookeeper dafür zu nutzen.
Ablauf:
Alle Producer watchen ein Verzeichnis (z.b. /abos) in Zookeper auf veränderungen
Nutzer erstellt neues Abo
Webapp-Backend erstellt einen Eintrag in /abos für das abo (existiert der eintrag bereits wird ein Zähler in dem Eintrag erhöht)
Die Producer-Instanzen bemerken den neuen Eintrag und konkurrieren um ein Lock für den Eintrag. Die Instanz die das Lock erhält erzeugt einen Thread für das Abo

Das beschriebene Verfahren bringt verschiedene Vorteile:
Abos werden nicht mehrfach an Producer vegeben
Fällt eine Producer-Instanz aus verfallen ihre Locks und andere Instanzen übernehmen diese automatisch (so funktieren zookeper-locks, da müssen wir nichts implementieren)
Meldet sich ein User ab, löscht dessen Webapp-Instanz seinen Eintrag in /abos (beziehungsweise dekrementiert den Counter und löscht wenn dieser 0 erreicht) woraufhin der Producer-Thread beendet wird. Abos die keinen Mehr interessieren werden gelöscht.
Fällt eine Webapp-Instanz aus verschwinden automatisch alle von ihr angelegten Einträge in /abos. Es bleiben also keine Thread-Leichen zurück.

## **Clusterverwaltung**
Es handelt sich hier um ein Verteiltes System. Ein solches manuell zu aufzubauen und zu verwalten ist aufwändig und nervig.
Daher soll hierfür Docker verwendet werden.
Dabei beschreibt man in einer Datei welche Komponenten man mit welchen Optionen und wie vielen Replikaten man haben möchte und docker startet/beendet/updated dann auf Kommando das komplette System. Das funktioniert sogar verteilt über mehrere Server.
Außerdem ist ein solcher Cluster portabel und jeder der Docker installiert hat kann mit einem einzelnen Kommando ein komplettes Cluster starten.
