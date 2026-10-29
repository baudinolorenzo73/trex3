// Zeus includes this private module but its clean installation does not resolve it.
const fs = require('node:fs');
const path = require('node:path');
const modules = path.resolve(__dirname, '../zeppos/node_modules');
const source = path.join(modules, '@zeppos/zeus-cli/private-modules/zeppos-app-utils');
const target = path.join(modules, 'zeppos-app-utils');
if (!fs.existsSync(path.join(source, 'package.json'))) throw Error('Modulo privato Zeus non trovato: reinstallare le dipendenze.');
if (!fs.existsSync(target)) fs.symlinkSync(source, target, process.platform === 'win32' ? 'junction' : 'dir');
console.log('Modulo incluso in Zeus collegato correttamente.');
