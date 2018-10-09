import url from 'url';
import path from 'path';
import process from 'process';
import Module from 'module';

const builtins = Module.builtinModules;
const JS_EXTENSIONS = new Set(['.js', '.mjs']);

export function resolve(specifier, parentModuleURL, defaultResolve) {
	console.log("----------------------------------------------------------------");
//	console.log("__dirname:", __dirname);
//	console.log("__filename:", __filename);
	console.log("specifier:", specifier);
	console.log("parentModuleURL:", parentModuleURL);
//	console.log("defaultResolve:", defaultResolve);
	
  if (builtins.includes(specifier)) {
//	  console.log("builtins.includes(specifier)");
    return {
      url: specifier,
      format: 'builtin'
    };
  }
  
  if (/^\.{0,2}[/]/.test(specifier) !== true && !specifier.startsWith('file:')) {
//	  console.log("/^\.{0,2}[/]/.test(specifier) !== true && !specifier.startsWith('file:')");
    return defaultResolve(specifier, parentModuleURL);
  }
  
//  console.log("specifier:", specifier, ", parentModuleURL:", parentModuleURL);
  
  if (parentModuleURL == undefined) {
	  parentModuleURL = "file://";  
  }
  
  const resolved = new url.URL(specifier, parentModuleURL);
  const ext = path.extname(resolved.pathname);
  
  if (!JS_EXTENSIONS.has(ext)) {
    throw new Error(
      `Cannot load file with non-JavaScript file extension ${ext}.`);
  }
  
//  console.log("resolved:", resolved);
  return {
    url: resolved.href,
    format: 'esm'
  };
}
