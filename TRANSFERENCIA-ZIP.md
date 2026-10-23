# Rebeca IA — pacote de transferência

Este repositório contém a interface web da assistente Rebeca e os documentos de base de conhecimento.

## Instalação em outro repositório GitHub
1. Extraia o arquivo ZIP.
2. Envie os arquivos e diretórios ao novo repositório, preservando os caminhos e o arquivo `index.html` na raiz.
3. Configure o GitHub Pages para publicar a partir da branch apropriada.
4. Confirme as URLs usadas no `index.html`: atualmente referem-se ao projeto Supabase original.
5. Caso queira um projeto Supabase independente, implante o código de `supabase/functions/rebeca-atendimento/index.ts` na nova instância e adapte as chamadas RPC, políticas de acesso e configurações. A cópia do código não transfere as tabelas, procedimentos, dados ou segredos do Supabase original.

## Limites da cópia
O ZIP do GitHub contém os arquivos versionados deste repositório, incluindo a função de atendimento exportada. Não inclui o banco de dados remoto nem suas permissões, configuração de domínio, segredos ou conteúdo que não esteja versionado. Não habilite registro de pré-reservas: o atendimento deve apenas informar os moradores e encaminhar reservas à administração.

## Procedimento de reserva
Reserva diretamente com síndico e administradora Predial; sem cadastro de pré-reserva na Rebeca. Áreas comuns: até 4 convidados por unidade; eventos autorizados com churrasqueira reservada: até 40 convidados, conforme as regras específicas.

## Contato da administração
+55 86 9833-8949

**Data da preparação:** 23/10/2026.

## Atendimento com Gemini (23/10/2026)
- Conversas: função `rebeca-gemini` no projeto Supabase onde a chave `GEMINI_API_KEY` já é mantida de forma protegida (ambiente da Lara). A função não consulta nem acessa os dados da Lara: usa somente os documentos do Villa Europa incluídos no próprio código.
- Calendário: continua no projeto Supabase anterior do Villa Europa, através de `rebeca-atendimento`.
- A chave Gemini não deve ser inserida em HTML, arquivos ZIP nem GitHub. Ao migrar a função para outro projeto Supabase, configure `GEMINI_API_KEY` em **Edge Functions → Secrets** do projeto de destino.
- No `index.html`, atualize o endpoint de chat somente quando a nova função estiver implantada no projeto de destino. A simples cópia do ZIP não migra nenhum segredo nem banco de dados.
- O serviço Gemini pode gerar consumo de API. Configure cotas e monitoramento no Google Cloud/AI Studio e proteções contra abuso adequadas antes de expandir a divulgação pública.

## Conversa por voz — 23/10/2026
- Botões na interface: **Ativar voz**, **Ouvir resposta**, **Parar** e **microfone**.
- Ao ativar a voz, Rebeca se apresenta em português brasileiro. As próximas respostas escritas também são narradas automaticamente, enquanto a voz estiver ativada.
- O microfone utiliza o reconhecimento de voz disponibilizado pelo navegador; requer autorização do usuário e pode não estar presente em alguns aparelhos ou navegadores.
- A voz neural usa o serviço `rebeca-voz`, com Gemini TTS, voice Aoede, configurado no Supabase. O código está em `supabase/functions/rebeca-voz/index.ts`.
- A chave de API permanece na variável de ambiente `GEMINI_API_KEY` do projeto Supabase conectado. **Nunca copie ou publique a chave** em GitHub, HTML, ZIP ou arquivos de configuração públicos.
- Para publicar em outro domínio/repositório GitHub, é necessário configurar os domínios autorizados nas funções de chat e voz, atualizar os endpoints no `index.html` e cadastrar os segredos no Supabase de destino.
- Geração de áudio por Gemini pode gerar cobrança ou consumo de cotas. Recomenda-se monitorar o uso, limitar requisições e implementar proteção por usuário/credenciais caso o sistema seja amplamente divulgado.
- A interface continua funcional sem microfone; se a voz neural falhar, o sistema usa voz de síntese do navegador quando disponível. Essa voz alternativa pode ser menos natural.
- A ativação do microfone depende de gesto do usuário; não há escuta contínua em segundo plano. As mensagens de texto e transcrições podem ser processadas pelos provedores de IA e reconhecimento de voz.
