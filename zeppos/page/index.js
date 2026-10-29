import { BasePage } from '@zeppos/zml/base-page';
import { createWidget, widget, prop, align, text_style } from '@zos/ui';
import { getDeviceInfo } from '@zos/device';
import { Geolocation } from '@zos/sensor';
import { setPageBrightTime, resetPageBrightTime } from '@zos/display';
import { VERSION, INTERVAL_MS, validFix } from '../lib/bridge';
Page(BasePage({
  build(){
    this.alive=true;this.active=false;this.inFlight=false;this.seq=0;this.lastSend=0;
    this.session=Date.now().toString(36)+'_'+Math.random().toString(36).slice(2,10);
    const d=getDeviceInfo();this.s=(d.width||480)/480;
    const text=(label,y,h,size)=>createWidget(widget.TEXT,{x:80*this.s,y:y*this.s,w:320*this.s,h:h*this.s,text:label,text_size:size*this.s,color:0xffffff,align_h:align.CENTER_H,align_v:align.CENTER_V,text_style:text_style.WRAP});
    text('T-Rex GPS Bridge '+VERSION,48,40,22);
    this.coords=text('Premi Avvia GPS\nall’aperto',104,96,25);
    this.state=text('Prova iniziale: mini-app aperta',208,62,19);
    const button=(label,y,fn)=>createWidget(widget.BUTTON,{x:100*this.s,y:y*this.s,w:280*this.s,h:52*this.s,text:label,text_size:23*this.s,color:0xffffff,normal_color:0x205a48,press_color:0x627b6e,radius:20,click_func:fn});
    this.gpsButton=button('Avvia GPS',292,()=>this.toggle());
    button('Prova collegamento',358,()=>this.ping());
    text('by Lollo ®2026',426,18,14);
  },
  status(t){if(this.alive)this.state.setProperty(prop.TEXT,t);},
  async ping(){
    if(this.inFlight||!this.alive)return;this.inFlight=true;this.status('Verifica Zepp e tablet...');
    try {
      const local=await this.request({method:'PING'},{timeout:10000});
      if(!local?.ok)throw Error('Zepp non risponde');
      const remote=await this.request({method:'PHONE_PING'},{timeout:10000});
      if(remote?.error)throw Error(remote.error);
      if(!remote?.ok)throw Error('Tablet non disponibile');
      this.status('Zepp e tablet collegati');
    }catch(e){this.status(e.message||'Collegamento non riuscito');}finally{this.inFlight=false;}
  },
  toggle(){
    if(this.active){this.stopGps();return;}
    try{
      this.gps=new Geolocation();
      this.callback=()=>this.onPosition();
      this.gps.onChange(this.callback);
      this.gps.start();this.active=true;
      this.gpsButton.setProperty(prop.MORE,{x:100*this.s,y:292*this.s,w:280*this.s,h:52*this.s,text:'Ferma GPS'});
      // Solo durante la prova: la continuita a display spento non e ancora collaudata.
      try{setPageBrightTime({brightTime:600000});}catch(_){}
      this.status('Ricerca satelliti...');this.onPosition();
    }catch(e){this.stopGps();this.status('GPS: '+(e.message||'controlla i permessi'));}
  },
  onPosition(){
    if(!this.alive||!this.active)return;
    try{
      if(this.gps.getStatus()!=='A'){this.status('Posizione non valida: attendo GPS');return;}
      const f={v:1,session:this.session,seq:++this.seq,lat:this.gps.getLatitude({format:'DD'}),lon:this.gps.getLongitude({format:'DD'}),observedAt:Date.now(),valid:true};
      if(!validFix(f)){this.status('Coordinate non valide');return;}
      this.coords.setProperty(prop.TEXT,f.lat.toFixed(6)+'\n'+f.lon.toFixed(6));
      if(this.inFlight||Date.now()-this.lastSend<INTERVAL_MS)return;
      this.lastSend=Date.now();this.sendFix(f);
    }catch(e){this.status('GPS: '+(e.message||'errore'));}
  },
  async sendFix(f){
    this.inFlight=true;
    try{
      const result=await this.request({method:'FIX',params:f},{timeout:10000});
      if(result?.error)throw Error(result.error);
      if(!result?.ok)throw Error('Invio non confermato');
      if(this.active)this.status('Tablet riceve · '+(result.osmand||'OsmAnd disattivato'));
    }catch(e){if(this.active)this.status(e.message||'Invio non riuscito');}
    finally{this.inFlight=false;}
  },
  stopGps(){
    this.active=false;
    try{if(this.gps&&this.callback)this.gps.offChange(this.callback);}catch(_){}
    try{this.gps?.stop();}catch(_){}
    this.gps=null;this.callback=null;
    try{resetPageBrightTime();}catch(_){}
    if(this.alive){this.gpsButton.setProperty(prop.MORE,{x:100*this.s,y:292*this.s,w:280*this.s,h:52*this.s,text:'Avvia GPS'});this.status('GPS fermato');}
  },
  onDestroy(){this.stopGps();this.alive=false;}
}));
