const fs = require('node:fs');
const path = require('node:path');
const zlib = require('node:zlib');
function crc32(bytes) {
  let c = 0xffffffff;
  for (const b of bytes) { c ^= b; for (let i=0;i<8;i++) c=(c>>>1)^((c&1)?0xedb88320:0); }
  return (c^0xffffffff)>>>0;
}
function chunk(type, data) {
  const t=Buffer.from(type), n=Buffer.alloc(4), crc=Buffer.alloc(4);
  n.writeUInt32BE(data.length);crc.writeUInt32BE(crc32(Buffer.concat([t,data])));
  return Buffer.concat([n,t,data,crc]);
}
const size=240, rows=Buffer.alloc(size*(1+size*3));
for(let y=0;y<size;y++)for(let x=0;x<size;x++){
  const dx=x-120,dy=y-120,r=dx*dx+dy*dy;
  let c=[22,60,44];
  if(r>=75*75&&r<=83*83)c=[228,240,232];
  if(Math.abs(dx)<16&&dy>-65&&dy<55)c=dy<0?[235,88,60]:[240,246,241];
  const at=y*(1+size*3)+1+x*3;for(let i=0;i<3;i++)rows[at+i]=c[i];
}
const header=Buffer.alloc(13);header.writeUInt32BE(size,0);header.writeUInt32BE(size,4);header[8]=8;header[9]=2;
const png=Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',header),chunk('IDAT',zlib.deflateSync(rows)),chunk('IEND',Buffer.alloc(0))]);
const output=path.resolve(__dirname,'../zeppos/assets/default.r/icon.png');
fs.mkdirSync(path.dirname(output),{recursive:true});fs.writeFileSync(output,png);
