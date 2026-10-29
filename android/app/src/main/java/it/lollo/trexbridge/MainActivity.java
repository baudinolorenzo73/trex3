package it.lollo.trexbridge;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageInfo;
import android.os.*;
import android.view.View;
import android.widget.*;
import java.security.SecureRandom;
import java.util.*;

public final class MainActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private TextView status;
    private android.content.SharedPreferences prefs;
    private final List<String> packages=new ArrayList<>();
    private final Runnable refresh=new Runnable() {
        @Override public void run() {
            Fix f=BridgeService.latest;
            String gps=f==null?"Nessuna posizione ricevuta":String.format(Locale.ITALY,"%.6f, %.6f\nUltima ricezione: %d s fa",f.lat,f.lon,Math.max(0,(System.currentTimeMillis()-f.receivedAt)/1000));
            status.setText(BridgeService.state+"\n\n"+gps+"\n\n"+BridgeService.apiState+"\n\n"+BridgeService.sendState);
            handler.postDelayed(this,1000);
        }
    };
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        prefs=getSharedPreferences("bridge",0);
        if(!prefs.contains("token")){
            byte[] bytes=new byte[16];new SecureRandom().nextBytes(bytes);
            StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format(Locale.ROOT,"%02x",b&255));
            prefs.edit().putString("token",s.toString()).apply();
        }
        ScrollView scroll=new ScrollView(this);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(28,24,28,24);
        scroll.addView(box);setContentView(scroll);
        text(box,"T-Rex GPS Bridge · 0.1.0",24);
        text(box,"Prima prova: avvia il ponte, inserisci il codice nelle impostazioni Zepp della mini-app e prova il GPS all'aperto. Zepp deve restare attivo sullo stesso dispositivo Android.",17);
        text(box,"Codice collegamento",18);
        TextView code=text(box,prefs.getString("token",""),17);code.setTextIsSelectable(true);
        button(box,"Copia codice",v->{
            ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Codice collegamento",prefs.getString("token","")));
            Toast.makeText(this,"Codice copiato",Toast.LENGTH_SHORT).show();
        });
        List<String> labels=new ArrayList<>();
        for(String pkg:new String[]{"net.osmand.plus","net.osmand","net.osmand.dev"}){
            try{PackageInfo p=getPackageManager().getPackageInfo(pkg,0);packages.add(pkg);labels.add(pkg+" · "+p.versionName);}catch(Exception ignored){}
        }
        text(box,"OsmAnd installato",18);
        Spinner spinner=new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels.isEmpty()?Collections.singletonList("OsmAnd non trovato"):labels));
        box.addView(spinner);
        String selected=prefs.getString("package","");
        int index=packages.indexOf(selected);if(index>=0)spinner.setSelection(index);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View view,int position,long id){
                if(position<packages.size())prefs.edit().putString("package",packages.get(position)).apply();
            }
        });
        Switch forward=new Switch(this);forward.setText("Invia GPS dell'orologio a OsmAnd");
        forward.setChecked(prefs.getBoolean("forward",false));box.addView(forward);
        forward.setOnCheckedChangeListener((b,on)->prefs.edit().putBoolean("forward",on).apply());
        text(box,"Lascia spento per verificare prima la ricezione. Nessuna posizione fittizia Android: questa prova usa l'API di OsmAnd. Se cambi app OsmAnd, ferma e riavvia il ponte.",16);
        button(box,"Avvia ponte (20 minuti)",v->{
            if(BridgeService.running){Toast.makeText(this,"Ponte gia attivo",Toast.LENGTH_SHORT).show();return;}
            if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},42);
            startForegroundService(new Intent(this,BridgeService.class));
        });
        button(box,"Apri OsmAnd",v->{
            Intent launch=getPackageManager().getLaunchIntentForPackage(prefs.getString("package","net.osmand.plus"));
            if(launch!=null)startActivity(launch);else Toast.makeText(this,"Installa o seleziona OsmAnd",Toast.LENGTH_LONG).show();
        });
        button(box,"Ferma ponte",v->stopService(new Intent(this,BridgeService.class)));
        status=text(box,"",17);
        text(box,"Coordinate conservate soltanto in memoria. Il ponte si ferma automaticamente dopo 20 minuti. Con collegamento interrotto la posizione non deve essere considerata aggiornata.",15);
    }
    private TextView text(LinearLayout box,String value,int size){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setPadding(0,12,0,12);box.addView(t);return t;
    }
    private void button(LinearLayout box,String label,View.OnClickListener listener){
        Button b=new Button(this);b.setText(label);b.setOnClickListener(listener);box.addView(b);
    }
    @Override protected void onResume(){super.onResume();handler.post(refresh);}
    @Override protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
