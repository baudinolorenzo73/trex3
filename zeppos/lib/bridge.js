export const VERSION='0.1.0';
export const INTERVAL_MS=3000;
export function validFix(f,now=Date.now()) {
  return !!f&&f.v===1&&typeof f.session==='string'&&/^[a-zA-Z0-9_-]{1,64}$/.test(f.session)
    &&Number.isInteger(f.seq)&&f.seq>=1&&Number.isFinite(f.lat)&&Math.abs(f.lat)<=90
    &&Number.isFinite(f.lon)&&Math.abs(f.lon)<=180&&Number.isFinite(f.observedAt)
    &&f.observedAt<=now+120000&&f.observedAt>=now-15000&&f.valid===true;
}
export function fixQuery(f,now=Date.now()) {
  if(!validFix(f,now))throw Error('Posizione non valida o scaduta');
  return ['v','session','seq','lat','lon','observedAt','valid'].map(k=>k+'='+encodeURIComponent(String(f[k]))).join('&');
}
