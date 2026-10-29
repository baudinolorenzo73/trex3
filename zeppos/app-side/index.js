import { BaseSideService } from '@zeppos/zml/base-side';
import { VERSION, fixQuery } from '../lib/bridge';
AppSideService(BaseSideService({
  async onRequest(request,response){
    try {
      if(request.method==='PING'){response(null,{ok:true,version:VERSION});return;}
      let token=settings.settingsStorage.getItem('bridge_token')||'';
      try{token=JSON.parse(token);}catch(_){}
      token=String(token).trim();
      if(!/^[a-f0-9]{32}$/.test(token))throw Error('Inserisci il codice del tablet nelle impostazioni Zepp');
      let endpoint;
      if(request.method==='PHONE_PING')endpoint='/ping';
      else if(request.method==='FIX')endpoint='/fix?'+fixQuery(request.params);
      else throw Error('Comando non supportato');
      const result=await fetch({url:'http://127.0.0.1:8089'+endpoint,method:'GET',headers:{'X-Trex-Token':token},timeout:7000});
      if(result.status!==200)throw Error(result.status===401?'Codice collegamento errato':'Tablet: errore '+result.status);
      const data=typeof result.body==='string'?JSON.parse(result.body):result.body;
      if(!data||typeof data.ok!=='boolean')throw Error('Risposta tablet non valida');
      response(null,data);
    }catch(e){response(null,{error:e.message||'Collegamento al tablet non riuscito'});}
  }
}));
