package it.lollo.trexbridge;

import android.app.*;
import android.content.*;
import android.os.*;
import net.osmand.aidlapi.IOsmAndAidlInterface;
import net.osmand.aidlapi.map.ALocation;
import net.osmand.aidlapi.map.SetLocationParams;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public final class BridgeService extends Service {
    static volatile boolean running;
    static volatile String state="Ponte fermato", apiState="OsmAnd non collegato", sendState="Invio a OsmAnd disattivato";
    static volatile Fix latest;
    private volatile IOsmAndAidlInterface api;
    private ScheduledExecutorService worker;
    private NanoHTTPD server;
    private boolean bound;
    private String token, target;
    private String lastSent="";
    private final Map<String,Long> sequences=new LinkedHashMap<>();
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ServiceConnection connection=new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            api=IOsmAndAidlInterface.Stub.asInterface(binder);
            apiState="Servizio OsmAnd collegato; apri la mappa e autorizza l'accesso se richiesto";
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            api=null;apiState="Collegamento OsmAnd interrotto";
        }
        @Override public void onNullBinding(ComponentName name) {api=null;apiState="OsmAnd non concede il collegamento";}
    };

    @Override public void onCreate() {
        super.onCreate();
        token=getSharedPreferences("bridge",0).getString("token","");
        target=getSharedPreferences("bridge",0).getString("package","net.osmand.plus");
        latest=null;lastSent="";sendState="Attendo coordinate";
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("bridge","Collegamento T-Rex",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE);
        startForeground(42,new Notification.Builder(this,"bridge").setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("T-Rex GPS Bridge").setContentText("Prova attiva per massimo 20 minuti").setContentIntent(open).setOngoing(true).build());
        worker=Executors.newSingleThreadScheduledExecutor();
        try {
            if (token.length()!=32) throw new IOException("Codice collegamento assente");
            server=new NanoHTTPD("127.0.0.1",8089) {
                @Override public Response serve(IHTTPSession request) {
                    if (request.getMethod()!=Method.GET) return answer(Response.Status.METHOD_NOT_ALLOWED,false,"Metodo non ammesso");
                    if (request.getUri().length()>100 || (request.getQueryParameterString()!=null && request.getQueryParameterString().length()>1024))
                        return answer(Response.Status.BAD_REQUEST,false,"Richiesta troppo lunga");
                    String supplied=request.getHeaders().getOrDefault("x-trex-token","");
                    if (!MessageDigest.isEqual(token.getBytes(StandardCharsets.US_ASCII),supplied.getBytes(StandardCharsets.US_ASCII)))
                        return answer(Response.Status.UNAUTHORIZED,false,"Codice errato");
                    if ("/ping".equals(request.getUri())) return answer(Response.Status.OK,true,apiState);
                    if (!"/fix".equals(request.getUri())) return answer(Response.Status.NOT_FOUND,false,"Comando non previsto");
                    try {
                        Map<String,String> params=new HashMap<>();
                        for (Map.Entry<String,List<String>> item:request.getParameters().entrySet()) {
                            if (item.getValue().size()!=1) throw new IllegalArgumentException("Parametro duplicato");
                            params.put(item.getKey(),item.getValue().get(0));
                        }
                        Fix fix=new Fix(params,System.currentTimeMillis());
                        synchronized(sequences) {
                            Long previous=sequences.get(fix.session);
                            if (previous!=null && fix.seq<=previous) throw new IllegalArgumentException("Posizione gia ricevuta");
                            sequences.put(fix.session,fix.seq);
                            if(sequences.size()>8) sequences.remove(sequences.keySet().iterator().next());
                            latest=fix;
                        }
                        state="Ricezione GPS attiva";
                        return answer(Response.Status.OK,true,sendState);
                    } catch(Exception e) {return answer(Response.Status.BAD_REQUEST,false,e.getMessage());}
                }
            };
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT,false);
            running=true;state="Ponte attivo, attendo orologio";
            connectOsmand();
            worker.scheduleWithFixedDelay(this::sendLatest,0,1,TimeUnit.SECONDS);
            main.postDelayed(this::stopSelf,20*60*1000);
        } catch(Exception e) {
            state="Ponte non avviato: "+e.getMessage();
            stopSelf();
        }
    }
    private NanoHTTPD.Response answer(NanoHTTPD.Response.Status status,boolean ok,String message) {
        JSONObject body=new JSONObject();
        try {body.put("ok",ok);body.put(ok?"osmand":"error",message);body.put("version","0.1.0");}catch(Exception ignored){}
        return NanoHTTPD.newFixedLengthResponse(status,"application/json",body.toString());
    }
    private void connectOsmand() {
        try {
            Intent intent=new Intent("net.osmand.aidl.OsmandAidlServiceV2").setPackage(target);
            int flags=Context.BIND_AUTO_CREATE;
            if(Build.VERSION.SDK_INT>=34) flags|=Context.BIND_ALLOW_ACTIVITY_STARTS;
            bound=bindService(intent,connection,flags);
            apiState=bound?"Collegamento OsmAnd in corso":"OsmAnd non disponibile: verifica versione e app selezionata";
        }catch(Exception e){apiState="OsmAnd: "+e.getClass().getSimpleName();}
    }
    private void sendLatest() {
        try {
            if(!getSharedPreferences("bridge",0).getBoolean("forward",false)){
                sendState="Invio a OsmAnd disattivato";return;
            }
            Fix fix=latest;
            if(fix==null || !fix.fresh(System.currentTimeMillis())){sendState="GPS assente o scaduto; nessun invio";return;}
            IOsmAndAidlInterface remote=api;
            if(remote==null){sendState="OsmAnd non collegato";return;}
            if(fix.key().equals(lastSent))return;
            // L'API Zepp usata espone lat/lon: non inventare quota, velocita o accuratezza.
            ALocation loc=new ALocation(fix.lat,fix.lon,fix.observedAt,false,0,false,0,false,0,false,0,false,0);
            boolean accepted=remote.setLocation(new SetLocationParams(loc,5000));
            if(accepted){lastSent=fix.key();sendState="Posizione inviata; verifica sulla mappa OsmAnd";}
            else sendState="OsmAnd rifiuta: apri la mappa e autorizza il collegamento";
        }catch(Exception e){sendState="Invio OsmAnd: "+e.getClass().getSimpleName();}
    }
    @Override public int onStartCommand(Intent intent,int flags,int id) {return START_NOT_STICKY;}
    @Override public IBinder onBind(Intent intent) {return null;}
    @Override public void onDestroy() {
        running=false;main.removeCallbacksAndMessages(null);
        if(server!=null)server.stop();
        if(worker!=null)worker.shutdownNow();
        if(bound)try{unbindService(connection);}catch(Exception ignored){}
        api=null;latest=null;
        if(!state.startsWith("Ponte non avviato"))state="Ponte fermato";
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }
}
