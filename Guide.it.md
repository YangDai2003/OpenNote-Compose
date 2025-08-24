- [English](Guide.md)
- [Italiano](Guide.it.md)
- [简体中文](Guide.zh.md)

# Indice

- [Cosa è il Markdown?](#cosa-è-il-markdown)
- [Cosa è il CommonMark?](#cosa-è-il-commonmark)
- [Cosa è il GitHub Flavored Markdown?](#what-is-github-flavored-markdown)
- [Cosa sono le formule LaTeX?](#cosa-sono-le-formule-latex)
- [Come si usano le formule LaTeX nel Markdown?](#come-si-usano-le-formule-latex-nel-markdown)
- [Cosa offre OpenNote?](#cosa-offre-opennote)
- [Elementi base di sintassi Markdown](#elementi-base-di-sintassi-markdown)
- [Elementi avanzati di sintassi Markdown](#elementi-avanzati-di-sintassi-markdown)
- [Sintassi delle formule LaTeX](#sintassi-delle-formule-latex)
- [Lettere greche](#lettere-greche)
- [Scorciatoie da tastiera](#scorciatoie-da-tastiera)
- [Dispense](#dispense)

# Cosa è il Markdown?

Markdown è un formato di testo semplice per la scrittura di documenti strutturati, basato su convenzioni per l'indicazione della formattazione nelle e-mail e nei post su Usenet.
È stato sviluppato da John Gruber (con l'aiuto di Aaron Swartz) e rilasciato nel 2004 sotto forma di una descrizione della sintassi e uno script Perl (Markdown.pl) per la conversione dello stesso in HTML.

Nel decennio successivo sono state sviluppate decine di implementazioni in molti linguaggi.
Alcune hanno esteso la sintassi Markdown originale con convenzioni per note a piè di pagina, tabelle e altri
elementi del documento.
Alcuni siti consentivano la visualizzazione dei documenti Markdown in formati diversi dall'HTML.

Siti web come Reddit, StackOverflow e GitHub hanno portato milioni di persone a utilizzare Markdown.
E Markdown ha iniziato ad essere utilizzato anche al di fuori del web, per scrivere libri, articoli, presentazioni, lettere e
appunti di lezioni.

# Cosa è il CommonMark?

CommonMark è stato sviluppato con l'obiettivo di risolvere le incongruenze e le ambiguità presenti in Markdown.

La specifica CommonMark definisce le regole per elementi quali intestazioni, elenchi, collegamenti, enfasi
e blocchi di codice.

Aderendo allo standard CommonMark, gli sviluppatori possono garantire una resa coerente dei contenuti Markdown su diverse applicazioni e piattaforme.

# Cosa è il GitHub Flavored Markdown?

GitHub Flavored Markdown, spesso abbreviato in GFM, è la declinazione di Markdown attualmente supportata per i contenuti degli utenti su GitHub.com e GitHub Enterprise.

Questa specifica formale, basata sulla CommonMark Spec, definisce la sintassi e la semantica di questa variante Markdown.

GFM è un superset rigoroso di CommonMark. Tutte le funzionalità supportate nei contenuti degli utenti GitHub e non specificate nella specifica CommonMark originale sono considerate come estensioni e pertanto, sono evidenziate come tali.

Sebbene GFM supporti un'ampia gamma di input, vale la pena notare che GitHub.com e GitHub Enterprise eseguono un'ulteriore post-elaborazione e sanificazione dopo la conversione di GFM in HTML per garantire la sicurezza e la coerenza del sito web.

# Cosa sono le formule LaTeX?

LaTeX è un sistema di composizione tipografica comunemente utilizzato per la produzione di documenti scientifici e matematici.

LaTeX offre un modo efficace per rappresentare la notazione matematica nei documenti, consentendo agli utenti di creare equazioni e formule complesse con facilità.

# Come si usano le formule LaTeX nel Markdown?

Le espressioni matematiche sono fondamentali per la condivisione delle informazioni tra ingegneri, scienziati, data scientists e matematici.

È possibile utilizzare i delimitatori $ e $$ in GFM per inserire espressioni matematiche nella sintassi TeX e LaTeX.

# Cosa offre OpenNote?

OpenNote supporta la sintassi CommonMark e GitHub Flavored Markdown (GFM), oltre alle formule matematiche LaTeX.

Ciò consente agli utenti di creare note riccamente formattate con supporto per intestazioni, elenchi, collegamenti,
enfasi, blocchi di codice, tabelle ed espressioni matematiche, e permette di esportare le note in
vari formati, tra cui TXT, MD (Markdown) e HTML.

# Elementi base di sintassi Markdown

|    Elemento    |              Sintassi              |
|:--------------:|:----------------------------------:|
|  Intestazione  | `# H1` <br/> `## H2`<br/> `### H3` |
|    Corsivo     |       `_italic_ or *italic*`       |
|   Grassetto    |       `**bold** or __bold__`       |
|   Citazione    |           `> Blockquote`           |
|      Link      | `[title](https://www.example.com)` |
|     Codice     |            `` `code` ``            |
| Lista numerata | `1. List item 1 or 2) List item 2` |
| Lista generica | `- Apple or + Banana or * Orange`  |
|    Immagine    |     `![alt text](image.jpeg)`      |

# Elementi avanzati di sintassi Markdown

|    Elemento   |               Sintassi             |
|:-------------:|:----------------------------------:|
|    Barrato    |        `~~Strikethrough~~`         |
|  Sottolineato |          `++Underline++`           |
|  Evidenziato  |           `==Marking==`            |
|   Attività    | `- [x] Task 1`<br/> `- [ ] Task 2` |
| Riferimento intestazione |      `# Heading {#custom-id}`      |

# Sintassi delle formule LaTeX

|  Elemento   |      Sintassi      |
|:-----------:|:------------------:|
|  In linea   |      `$x^2$`       |
|   Blocco    |     `$$x^2$$`      |
| Ritorno a capo |    `\\ or \\\\`    |
|   Spazio    |    `\quad or \`    |
|  Esponente  |       `x^2`        |
|  Deponente  |       `y_1`        |
| Espressione |       `{x}`        |
|  Sopralineatura   |   `\overline{x}`   |
|  Sottolineatura  |  `\underline{x}`   |
|  Frazione   |   `\frac{x}{y}`    |
| Parentesi sinistra  |      `\left(`      |
| Parentesi destra |     `\right)`      |
| Parentesi tonde | `\left(x+y\right)` |
| Parentesi quadre | `\left[x+y\right]` |
| Parentesi graffe | `\left{x+y\right}` |
|    Radice   |   `\sqrt[n]{x}`    |
|      ×      |      `\times`      |
|      ÷      |       `\div`       |
|      ±      |       `\pm`        |
|      ≠      |       `\neq`       |
|      ≈      |     `\approx`      |
|      ≤      |       `\leq`       |
|      ≥      |       `\geq`       |
|      ∞      |      `\infty`      |
|      ∑      |       `\sum`       |
|      ∏      |      `\prod`       |
|      ∫      |       `\int`       |
|      ∑      |       `\sum`       |
|     lim     |       `\lim`       |
|      ∀      |     `\forall`      |
|      ∃      |     `\exists`      |
|      ∴      |    `\therefore`    |
|      ∵      |     `\because`     |
|      ⊂      |     `\subset`      |
|      ⊃      |     `\supset`      |
|      ⊆      |    `\subseteq`     |
|      ⊇      |    `\supseteq`     |
|      ∈      |       `\in`        |
|      ∉      |      `\notin`      |

# Lettere greche

| Maiuscolo |  Sintassi  | Minuscolo |  Sintassi  |
|:---------:|:----------:|:---------:|:----------:|
|     A     |    `A`     |     α     |  `\alpha`  |
|     Β     |    `B`     |     β     |  `\beta`   |
|     Γ     |  `\Gamma`  |     γ     |  `\gamma`  |
|     Δ     |  `\Delta`  |     δ     |  `\delta`  |
|     Ε     |    `E`     |     ε     | `\epsilon` |
|     Ζ     |    `Z`     |     ζ     |  `\zeta`   |
|     Η     |    `H`     |     η     |   `\eta`   |
|     Θ     |  `\Theta`  |     θ     |  `\theta`  |
|     Ι     |    `I`     |     ι     |  `\iota`   |
|     Κ     |    `K`     |     κ     |  `\kappa`  |
|     Λ     | `\Lambda`  |     λ     | `\lambda`  |
|     Μ     |    `M`     |     μ     |   `\mu`    |
|     Ν     |    `N`     |     ν     |   `\nu`    |
|     Ξ     |   `\Xi`    |     ξ     |   `\xi`    |
|     Ο     |    `O`     |     ο     | `\omicron` |
|     Π     |   `\Pi`    |     π     |   `\pi`    |
|     Ρ     |    `P`     |     ρ     |   `\rho`   |
|     Σ     |  `\Sigma`  |     σ     |  `\sigma`  |
|     Τ     |    `T`     |     τ     |   `\tau`   |
|     Υ     | `\Upsilon` |     υ     | `\upsilon` |
|     Φ     |   `\Phi`   |     φ     |   `\phi`   |
|     Χ     |    `X`     |     χ     |   `\chi`   |
|     Ψ     |   `\Psi`   |     ψ     |   `\psi`   |
|     Ω     |  `\Omega`  |     ω     |  `\omega`  |

# Scorciatoie da tastiera

|     Element      |    Shortcut    |
|:----------------:|:--------------:|
|  Seleziona tutto |    `Ctrl+A`    |
|      Taglia      |    `Ctrl+X`    |
|       Copia      |    `Ctrl+C`    |
|      Incolla     |    `Ctrl+V`    |
|       Annulla    |    `Ctrl+Z`    |
|       Ripeti     |    `Ctrl+Y`    |
| Trova e sostituisci |    `Ctrl+F`    |
|    Grassetto     |    `Ctrl+B`    |
|      Corsivo     |    `Ctrl+I`    |
|    Sottolineato  |    `Ctrl+U`    |
|     Barrato      |    `Ctrl+D`    |
|     Evidenzia    |    `Ctrl+M`    |
|      Tabella     |    `Ctrl+T`    |
|       Link       |    `Ctrl+K`    |
|     Anteprima    |    `Ctrl+P`    |
|   Intestazione   |   `Ctrl+1~6`   |
|     Immagine     | `Ctrl+Shift+I` |
|       Lista      | `Ctrl+Shift+L` |
|       Scan       | `Ctrl+Shift+S` |
|      Codice      | `Ctrl+Shift+K` |
|     Citazione    | `Ctrl+Shift+Q` |
| Formula matematica | `Ctrl+Shift+M` |
| Righello orizzontale | `Ctrl+Shift+R` |
|     Attività     | `Ctrl+Shift+T` |
| Diagramma Mermaid | `Ctrl+Shift+D` |
|      Modelli     | `Ctrl+Shift+P` |
|      Audio       | `Ctrl+Shift+A` |
|      Video       | `Ctrl+Shift+V` |

# Dispense

Puoi trovare ulteriori informazioni su Markdown, CommonMark, GitHub Flavored Markdown, LaTeX Math e diagrammi Mermaid ai seguenti link:

- [CommonMark](https://commonmark.org/)
- [GitHub Flavored Markdown](https://github.github.com/gfm/)
- [LaTeX](https://www.latex-project.org/)
- [Mermaid](https://mermaid.js.org/)
