function p(t){const r=new Date(t);return Number.isNaN(r.getTime())?t:r.toLocaleString()}function a(t){return i(t)||"暂无可显示的分析结果。"}function i(t){if(t==null)return"";if(typeof t=="string"){const r=t.trim();if(r&&m(r))try{return i(JSON.parse(r))}catch{return o(t)}return o(t)}if(typeof t=="number"||typeof t=="boolean")return String(t);if(Array.isArray(t))return t.map(r=>i(r)).filter(r=>r.trim().length>0).join(`

`);if(typeof t=="object"){const r=t,n=r.text;return typeof n=="string"?o(n):Object.entries(r).map(([e,f])=>s(e,f)).filter(e=>e.trim().length>0).join(`

`)}return""}function s(t,r){const n=i(r);if(!n)return"";const e=c(t);return n.includes(`
`)?`${e}:
${u(n)}`:`${e}: ${n}`}function c(t){return t.replace(/[_-]+/g," ").replace(/\s+/g," ").trim().replace(/\b\w/g,r=>r.toUpperCase())}function u(t){return t.split(`
`).map(r=>r.trim()?`  ${r}`:r).join(`
`)}function m(t){const r=t.trim();return r.startsWith("{")&&r.endsWith("}")||r.startsWith("[")&&r.endsWith("]")}function o(t){return t.replace(/\\n/g,`
`).trim()}export{p as f,a as t};
