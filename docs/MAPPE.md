# Mappe OSM - prima prova
Il generatore produce un archivio cartografico per il percorso di importazione Zepp OS documentato da Gadgetbridge. Non e un APK, uno ZAB, una traccia GPX o uno ZIP da pubblicare come sito.

## Obiettivo
Confrontare i sentieri e le etichette di una zona nota con la mappa nativa e con OsmAnd. Il T-Rex 3 Pro dichiara gia mappe OSM: nuovi dati non garantiscono maggior dettaglio se il renderer dell'orologio non li mostra.

## Scaricare
GitHub > Actions > Prepara mappa OSM > esecuzione verde > Artifacts > trex3-mappa-osm.
Estrarre l'archivio scaricato: al suo interno si trova trex3-mappa-osm.zip (mappa), con area e controllo SHA256 separati.
Non modificare la struttura interna della mappa.

## Installazione ancora da verificare
La via documentata per questo formato e Gadgetbridge; non e dimostrato che Zepp accetti direttamente questo ZIP.
Prima della prova verificare supporto attuale del T-Rex 3 Pro, metodo di collegamento e conservazione dei dati. Non disassociare l'orologio, ripristinarlo o sostituire mappe funzionanti alla cieca.
Confrontare poi lo stesso punto geografico e zoom, annotando sentieri, nomi e POI mancanti. Fuori dall'area scaricata la mappa potrebbe non apparire.
Questa prima mappa usa lo stile del convertitore, non include automaticamente curve di livello aggiuntive ne promette piu informazioni di quella nativa.

## Fonti e attribuzione
Dati © OpenStreetMap contributors, ODbL: https://www.openstreetmap.org/copyright
Estratto Geofabrik Nord-Ovest: https://download.geofabrik.de/europe/italy/nord-ovest.html
Convertitore Gared: https://github.com/Gared/zeppos-map-creator (commit fissato in maps/area.json)
Formato/importazione: https://gadgetbridge.org/basics/topics/zeppos/
Non si scaricano in massa tile dai server cartografici pubblici OSM.
