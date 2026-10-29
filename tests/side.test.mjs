import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import {VERSION,fixQuery} from '../zeppos/lib/bridge.js';

function harness({token='',fetcher=()=>{throw Error('HTTP inatteso');}}={}){
  let service, requests=[];
  const source=readFileSync(new URL('../zeppos/app-side/index.js',import.meta.url),'utf8').replace(/^import .*;\r?\n/gm,'');
  vm.runInNewContext(source,{
    BaseSideService:x=>x,AppSideService:x=>{service=x;},VERSION,fixQuery,
    settings:{settingsStorage:{getItem:()=>token}},
    fetch:async options=>{requests.push(options);return fetcher(options);}
  });
  return {requests,async call(request){
    let count=0,answer;
    await service.onRequest(request,(error,data)=>{assert.equal(error,null);count++;answer=data;});
    assert.equal(count,1,'ogni RPC deve rispondere una sola volta');
    return answer;
  }};
}
test('PING locale funziona senza codice e senza HTTP',async()=>{
  const h=harness();assert.equal((await h.call({method:'PING'})).ok,true);assert.equal(h.requests.length,0);
});
test('Codice mancante e posizione non valida non raggiungono il telefono',async()=>{
  for(const token of ['', 'non-valido', 'a'.repeat(32)]){
    const h=harness({token});
    assert.ok((await h.call({method:'FIX',params:{lat:NaN}})).error);
    assert.equal(h.requests.length,0);
  }
});
test('Codice in header, ricezione e inoltro dei messaggi del telefono',async()=>{
  const token='a'.repeat(32);
  const h=harness({token:JSON.stringify(token),fetcher:()=>({status:200,body:'{"ok":true,"osmand":"attendo GPS"}'})});
  const result=await h.call({method:'PHONE_PING'});
  assert.equal(result.osmand,'attendo GPS');
  assert.equal(h.requests[0].url,'http://127.0.0.1:8089/ping');
  assert.equal(h.requests[0].headers['X-Trex-Token'],token);
  assert.equal(h.requests[0].url.includes(token),false);
  assert.equal(h.requests[0].timeout,7000);
});
test('HTTP negato, JSON corrotto, risposta malformata e timeout producono errori RPC',async()=>{
  for(const fetcher of [
    ()=>({status:401,body:''}),()=>({status:500,body:''}),
    ()=>({status:200,body:'{'}),()=>({status:200,body:'{}'}),
    ()=>{throw Error('timeout');}
  ]){
    const h=harness({token:'a'.repeat(32),fetcher});
    assert.ok((await h.call({method:'PHONE_PING'})).error);
  }
});
