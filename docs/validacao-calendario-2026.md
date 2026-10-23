# Validação do calendário oficial de 2026

**Documento:** CALENDARIO_2026.pdf (12 páginas, 1 mês por página).
**Método:** conferência das cores dos 104 sábados e domingos diretamente na renderização das páginas do PDF, comparadas à classificação presente em index.html.
**Resultado:** 104/104 classificações coincidem com o documento. Sem divergências detectadas.

## Regras
- Verde: uso exclusivo dos moradores.
- Vermelho: uso aberto para moradores e visitantes convidados.
- Datas sem classificação: não presumir permissão.
- A cor **não indica disponibilidade de churrasqueira**, nem representa reserva aprovada.
- Apenas a administração pode confirmar reservas e autorizá-las.
- Ao abrir o calendário, exibir mês e ano atuais do dispositivo. Se o ano não for 2026, informar que não há calendário oficial validado para esse ano.

## Conferência mensal — datas verdes
| Mês | Dias verdes (demais finais de semana: vermelhos) |
|---|---|
| Janeiro | 10, 11, 24, 25 |
| Fevereiro | 7, 8, 21, 22 |
| Março | 7, 8, 21, 22 |
| Abril | 4, 5, 18, 19 |
| Maio | 2, 3, 16, 17, 30, 31 |
| Junho | 13, 14, 27, 28 |
| Julho | 11, 12, 25, 26 |
| Agosto | 8, 9, 22, 23 |
| Setembro | 5, 6, 19, 20 |
| Outubro | 3, 4, 17, 18, 31 |
| Novembro | 1, 14, 15, 28, 29 |
| Dezembro | 12, 13, 26, 27 |

## Limites dos testes
- Verificação da correspondência entre o PDF e a tabela de cores: **concluída**.
- Presença de cálculo de mês atual e resposta por data no HTML do GitHub: **confirmada no código**.
- Teste interativo em navegador publicado: **não executado**.
- GitHub Pages: **não confirmado como ativo**.
- Integração com Supabase e banco de reservas: **pendente**.
