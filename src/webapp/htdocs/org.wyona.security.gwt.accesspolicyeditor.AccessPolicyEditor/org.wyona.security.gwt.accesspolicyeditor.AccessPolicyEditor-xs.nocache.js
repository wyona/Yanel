function org_wyona_security_gwt_accesspolicyeditor_AccessPolicyEditor(){var k=window,j=document,s=k.external,t,p,o='',w={},F=[],C=[],n=[],z,B;if(!k.__gwt_stylesLoaded){k.__gwt_stylesLoaded={};}if(!k.__gwt_scriptsLoaded){k.__gwt_scriptsLoaded={};}function u(){try{return s&&(s.gwtOnLoad&&k.location.search.indexOf('gwt.hybrid')== -1);}catch(a){return false;}}
function v(){if(t&&p){t(z,'org.wyona.security.gwt.accesspolicyeditor.AccessPolicyEditor',o);}}
function r(){var i,h;j.write('<script id="__gwt_marker_org.wyona.security.gwt.accesspolicyeditor.AccessPolicyEditor"><\/script>');h=j.getElementById('__gwt_marker_org.wyona.security.gwt.accesspolicyeditor.AccessPolicyEditor');if(h){i=h.previousSibling;}function d(b){var a=b.lastIndexOf('/');return a>=0?b.substring(0,a+1):'';}
;if(i&&i.src){o=d(i.src);}if(o==''){var c=j.getElementsByTagName('base');if(c.length>0){o=c[c.length-1].href;}else{var g=j.location;var e=g.href;o=d(e.substr(0,e.length-g.hash.length));}}else if(o.match(/^\w+:\/\//)){}else{var f=j.createElement('img');f.src=o+'clear.cache.gif';o=d(f.src);}if(h){h.parentNode.removeChild(h);}}
function A(){var f=document.getElementsByTagName('meta');for(var d=0,g=f.length;d<g;++d){var e=f[d],h=e.getAttribute('name'),b;if(h){if(h=='gwt:property'){b=e.getAttribute('content');if(b){var i,c=b.indexOf('=');if(c>=0){h=b.substring(0,c);i=b.substring(c+1);}else{h=b;i='';}w[h]=i;}}else if(h=='gwt:onPropertyErrorFn'){b=e.getAttribute('content');if(b){try{B=eval(b);}catch(a){alert('Bad handler "'+b+'" for "gwt:onPropertyErrorFn"');}}}else if(h=='gwt:onLoadErrorFn'){b=e.getAttribute('content');if(b){try{z=eval(b);}catch(a){alert('Bad handler "'+b+'" for "gwt:onLoadErrorFn"');}}}}}}
function m(a,b){return b in F[a];}
function l(a){var b=w[a];return b==null?null:b;}
function E(d,e){var a=n;for(var b=0,c=d.length-1;b<c;++b){a=a[d[b]]||(a[d[b]]=[]);}a[d[c]]=e;}
function q(d){var e=C[d](),b=F[d];if(e in b){return e;}var a=[];for(var c in b){a[b[c]]=c;}if(B){B(d,a,e);}throw null;}
C['user.agent']=function(){var d=navigator.userAgent.toLowerCase();var b=function(a){return parseInt(a[1])*1000+parseInt(a[2]);};if(d.indexOf('opera')!= -1){return 'opera';}else if(d.indexOf('webkit')!= -1){return 'safari';}else if(d.indexOf('msie')!= -1){var c=/msie ([0-9]+)\.([0-9]+)/.exec(d);if(c&&c.length==3){if(b(c)>=6000){return 'ie6';}}}else if(d.indexOf('gecko')!= -1){var c=/rv:([0-9]+)\.([0-9]+)/.exec(d);if(c&&c.length==3){if(b(c)>=1008)return 'gecko1_8';}return 'gecko';}return 'unknown';};F['user.agent']={'gecko':0,'gecko1_8':1,'ie6':2,'opera':3,'safari':4};org_wyona_security_gwt_accesspolicyeditor_AccessPolicyEditor.onScriptLoad=function(a){org_wyona_security_gwt_accesspolicyeditor_AccessPolicyEditor=null;t=a;v();};r();A();var y;function x(){if(!p){p=true;v();if(j.removeEventListener){j.removeEventListener('DOMContentLoaded',x,false);}if(y){clearInterval(y);}}}
if(j.addEventListener){j.addEventListener('DOMContentLoaded',x,false);}var y=setInterval(function(){if(/loaded|complete/.test(j.readyState)){x();}},50);var D;try{E(['ie6'],'4D396AA27FC51FAF579AF1DDC7154B97');E(['opera'],'60493B13FCC3FA61952E66A2C5A97A85');E(['gecko1_8'],'A5056A57F9611491A8F4FEDA0E3EDBAF');E(['gecko'],'D105ED4E86084DDC61D9845F3BDEC1E5');E(['safari'],'D11C4A981D249FBF1234F47D97C70A78');D=n[q('user.agent')];}catch(a){return;}D+='.cache.js';if(!__gwt_scriptsLoaded['init.js']){__gwt_scriptsLoaded['init.js']=true;document.write('<script language="javascript" src="'+o+'init.js"><\/script>');}j.write('<script src="'+o+D+'"><\/script>');}
org_wyona_security_gwt_accesspolicyeditor_AccessPolicyEditor.__gwt_initHandlers=function(i,e,j){var d=window,g=d.onresize,f=d.onbeforeunload,h=d.onunload;d.onresize=function(a){try{i();}finally{g&&g(a);}};d.onbeforeunload=function(a){var c,b;try{c=e();}finally{b=f&&f(a);}if(c!=null){return c;}if(b!=null){return b;}};d.onunload=function(a){try{j();}finally{h&&h(a);}};};org_wyona_security_gwt_accesspolicyeditor_AccessPolicyEditor();