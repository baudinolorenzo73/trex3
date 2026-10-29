import {test} from 'node:test';
import assert from 'node:assert/strict';
import {validFix,fixQuery} from '../zeppos/lib/bridge.js';
const now=1800000000000;
const good={v:1,session:'abc_12',seq:1,lat:45.06,lon:7.39,observedAt:now,valid:true};
test('Posizione valida e query senza codice segreto',()=>{
 assert.equal(validFix(good,now),true);const q=new URLSearchParams(fixQuery(good,now));
 assert.equal(q.get('lat'),'45.06');assert.equal(q.get('session'),'abc_12');assert.equal(q.has('token'),false);
});
test('Rifiuta coordinate, sequenze, sessioni e tempi non validi',()=>{
 for(const bad of [{lat:NaN},{lat:91},{lon:181},{seq:0},{seq:1.5},{session:'x&token=evil'},{v:2},{valid:false},{observedAt:now-15001},{observedAt:now+120001}]){
  assert.equal(validFix({...good,...bad},now),false);assert.throws(()=>fixQuery({...good,...bad},now));
 }
 assert.equal(validFix({...good,lat:0,lon:0},now),true);
});
