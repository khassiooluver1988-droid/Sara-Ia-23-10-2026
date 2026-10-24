const allowedOrigin = "https://khassiooluver1988-droid.github.io";
const common = {"Access-Control-Allow-Origin":allowedOrigin,"Access-Control-Allow-Headers":"content-type","Access-Control-Allow-Methods":"GET, POST, OPTIONS","Cache-Control":"no-store"};
const json=(data:unknown,status=200)=>new Response(JSON.stringify(data),{status,headers:{...common,"Content-Type":"application/json; charset=utf-8"}});
const visits=new Map<string,{reset:number,count:number}>();
let providerQuotaUntil=0; // Evita repetir chamadas quando o limite diário da API estiver esgotado.

Deno.serve(async(req:Request)=>{
 if(req.method==="OPTIONS")return new Response(null,{status:204,headers:common});
 if(req.method==="GET")return json({service:"rebeca-voz",configured:Boolean(Deno.env.get("GEMINI_API_KEY")||Deno.env.get("GOOGLE_API_KEY")),voice:"Aoede",language:"pt-BR"});
 if(req.method!=="POST")return json({error:"Método não permitido"},405);
 const origin=req.headers.get("origin")||"";
 if(origin&&origin!==allowedOrigin)return json({error:"Origem não permitida"},403);
 const ip=req.headers.get("cf-connecting-ip")||req.headers.get("x-forwarded-for")?.split(",")[0]||"unknown";
 const now=Date.now();let v=visits.get(ip);if(!v||now>v.reset)v={reset:now+15*60*1000,count:0};if(++v.count>10)return json({code:"local_rate_limit",error:"Muitas solicitações de áudio em poucos minutos. Tente novamente mais tarde."},429);visits.set(ip,v);if(visits.size>1800)visits.clear();
 let body:any;try{body=await req.json()}catch{return json({error:"Dados inválidos"},400)}
 const text=typeof body?.text==="string"?body.text.trim().slice(0,950):"";
 if(text.length<2)return json({error:"Informe o texto para narração"},400);
 const key=Deno.env.get("GEMINI_API_KEY")||Deno.env.get("GOOGLE_API_KEY");
 if(!key)return json({error:"Síntese neural de voz não configurada"},503);
 const model=Deno.env.get("REBECA_TTS_MODEL")||"gemini-3.8-flash-lite-tts";
 if(Date.now()<providerQuotaUntil)return json({code:"tts_daily_quota",error:"A cota diária da voz natural terminou. O chat continua funcionando por texto.",retryAfterSeconds:Math.ceil((providerQuotaUntil-Date.now())/1000)},429);
 const payload={model,input:[{type:"user_input",content:[{type:"text",text,annotations:[{type:"speech_metadata",style:"Voz feminina natural, calorosa e acolhedora, em português brasileiro. Falar como uma atendente humana educada, com dicção clara, pausas naturais, ritmo moderado e tom conversacional, sem narração publicitária."}]}]}],response_format:{type:"audio",mime_type:"audio/wav"},generation_config:{speech_config:[{voice:"Aoede"}]}};
 const ctl=new AbortController();const timeout=setTimeout(()=>ctl.abort(),22000);
 try{const r=await fetch("https://generativelanguage.googleapis.com/v1beta/interactions",{method:"POST",headers:{"Content-Type":"application/json","x-goog-api-key":key},body:JSON.stringify(payload),signal:ctl.signal});
 if(!r.ok){
   const diagnostic=(await r.text()).slice(0,500);
   console.error("Rebeca voz TTS error",r.status,diagnostic);
   if(r.status===429){
     const m=diagnostic.match(/retry in\s+(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?/i);
     const duration=m?((Number(m[1]||0)*3600)+(Number(m[2]||0)*60)+Number(m[3]||0)):600;
     providerQuotaUntil=Date.now()+Math.max(60,duration)*1000;
     return json({code:"tts_daily_quota",error:"O limite diário gratuito da voz natural foi atingido. A resposta permanece disponível em texto.",retryAfterSeconds:Math.max(60,duration)},429);
   }
   return json({code:"tts_provider_error",error:"Serviço de voz natural temporariamente indisponível.",status:r.status},503);
 }
 const result=await r.json();const audios=(result.steps||[]).filter((x:any)=>x.type==="model_output").flatMap((x:any)=>x.content||[]).filter((x:any)=>x.type==="audio");const data=result.output_audio?.data||audios[audios.length-1]?.data;if(!data||typeof data!=="string")return json({error:"O provedor não retornou áudio"},502);
 const binary=atob(data);const bytes=new Uint8Array(binary.length);for(let i=0;i<binary.length;i++)bytes[i]=binary.charCodeAt(i);
 return new Response(bytes,{status:200,headers:{...common,"Content-Type":"audio/wav","Content-Disposition":"inline; filename=rebeca-voz.wav","X-Rebeca-Voice":"Aoede","X-Rebeca-Model":model}});
 }catch(e){console.error("Rebeca voz error",String(e));return json({error:"Falha ao gerar voz neural"},503)}finally{clearTimeout(timeout)}
});