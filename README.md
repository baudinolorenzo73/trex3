# trex3 · T-Rex GPS Bridge 0.1.0

Progetto di Lorenzo per Amazfit T-Rex 3 Pro: mappe escursionistiche OSM e collegamento con OsmAnd su Android. App distinta da Lettore GEA.

## Stato concreto

- Mappe: generazione sperimentale di un pacchetto nativo da dati OSM, con controllo ZIP, CRC e SHA256. Prima area dimostrativa: Avigliana. Non e ancora dimostrata l'installazione su T-Rex 3 Pro o un maggior dettaglio rispetto alla mappa nativa.
- Orologio: mini-app GPS e prova collegamento attraverso il Side Service Zepp.
- Android: ricezione coordinate, rilevamento OsmAnd e invio facoltativo tramite API setLocation.
- Versione iniziale sperimentale, da provare su hardware. Nessuna nuova cartografia viene installata automaticamente e non sono implementati ricalcolo sentieri, mappe in una mini-app dedicata o istruzioni di svolta al polso.

## Compilare su GitHub

Aprire **Actions**:
- **Controlli**: test automatici.
- **Compila APK e Zepp OS**: genera due artifact separati, **trex3-android-0.1.0** (APK) e **trex3-orologio-0.1.0** (ZAB).
- **Prepara mappa OSM**: genera l'artifact **trex3-mappa-osm**.

Un'esecuzione verde dimostra la compilazione o i controlli indicati, non il funzionamento sul tuo orologio.
Da **Artifacts** scaricare ed estrarre lo ZIP esterno. Lo ZIP della mappa dentro l'artifact deve restare integro.
I file in questo progetto non vanno convertiti dal pulsante generico HTML/Python di Pubblica: le compilazioni sono gia definite in .github/workflows.

## Prima prova orologio → tablet → OsmAnd

1. Installare l'APK Android sullo stesso telefono/tablet a cui e associato l'orologio tramite Zepp. Non sostituisce Lettore GEA.
2. Aprire T-Rex GPS Bridge e premere **Avvia ponte**. Per la prima prova lasciare disattivato **Invia GPS dell'orologio a OsmAnd**.
3. Scaricare i sorgenti di questo progetto e avviare **INSTALLA_OROLOGIO.bat** sul PC con Node disponibile. Scansionare il QR da Zepp in modalita sviluppatore. Il ZAB gia compilato e nell'artifact, ma un download di ZAB non installa automaticamente la mini-app.
4. Nelle impostazioni Zepp della nuova mini-app inserire il **Codice collegamento** copiato dall'app Android. E un codice locale: non pubblicarlo qui.
5. Con Zepp attivo sul tablet, aprire la mini-app e premere **Prova collegamento**.
6. All'aperto premere **Avvia GPS**. Confrontare le coordinate sul polso e sul tablet.
7. Per la prova OsmAnd, selezionare la versione installata nell'app Android, attivare il relativo interruttore e aprire la mappa di OsmAnd. Se richiesto, autorizzare il collegamento esterno.
8. Il messaggio **Posizione inviata** indica accettazione della richiesta API, non verifica della posizione mostrata: controllarla sulla mappa. Se OsmAnd non riceve, riportare versione installata e messaggi del ponte.

Non usare dati simulati come se fossero una posizione reale. Nessun mock provider Android e richiesto da questa prima implementazione.
La mini-app invia latitudine/longitudine: quota, velocita e accuratezza non vengono inventate.
Il server e solo loopback (porta 8089, distinta da Lettore GEA); il codice resta privato sul dispositivo. Non esiste un servizio cloud per le coordinate. Non viene registrata una cronologia GPS.

## Limiti della prima prova

Il ponte Android si ferma dopo 20 minuti. La mini-app deve restare aperta; durante la prova richiede temporaneamente display acceso fino a 10 minuti.
Zepp OS limita il GPS nel servizio in background: autonomia e continuita a schermo spento NON sono garantite.
La perdita del collegamento fa scadere il dato; su OsmAnd evitare di confondere una vecchia posizione con una ricezione attuale.
L'API OsmAnd presente nei sorgenti correnti deve essere verificata sulla versione installata. L'API AIDL e una dipendenza del progetto; non si incorpora il motore cartografico OsmAnd.

L'APK e firmato con una chiave di sviluppo mantenuta nella cache Actions. Se la cache della firma viene persa, una nuova compilazione puo non aggiornare quella installata: non disinstallare per tentativi, conservare prima i dati. Per distribuzione stabile prevedere firma privata protetta separata.

## Mappe

Leggere [istruzioni mappe](docs/MAPPE.md). Nessuna importazione automatica, reset, modifica del firmware o disassociazione da Zepp.
Obiettivo del confronto: sentieri, riferimenti e nomi leggibili come nelle mappe escursionistiche. OSM non significa automaticamente stesso dettaglio grafico di OsmAnd.

## Licenze e riferimenti

Dati © OpenStreetMap contributors, ODbL: https://www.openstreetmap.org/copyright
Convertitore: https://github.com/Gared/zeppos-map-creator e Gadgetbridge-tools.
Zepp/ZML: https://github.com/zepp-health/zml
API OsmAnd: https://osmand.net/docs/technical/osmand-api-sdk/
Il progetto non include dati personali, chiavi di firma, token o estratti cartografici nel repository.
