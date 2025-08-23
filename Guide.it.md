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

Markdown è un formato di testo semplice per la scrittura di documenti strutturati, basato su convenzioni per l' indicazione della formattazione nelle e-mail e nei post su Usenet.
È stato sviluppato da John Gruber (con l'aiuto di Aaron Swartz) e rilasciato nel 2004 sotto forma di una descrizione della sintassi e uno script Perl (Markdown.pl) per la conversione di Markdown in HTML.

Nel decennio successivo sono state sviluppate decine di implementazioni in molti linguaggi.
Alcune hanno esteso la sintassi Markdown originale con convenzioni per note a piè di pagina, tabelle e altri
elementi del documento.
Alcuni consentivano la visualizzazione dei documenti Markdown in formati diversi dall'HTML.

Siti web come Reddit, StackOverflow e GitHub hanno portato milioni di persone a utilizzare Markdown.
E Markdown ha iniziato ad essere utilizzato anche al di fuori del web, per scrivere libri, articoli, presentazioni, lettere e
appunti di lezioni.

# Cosa è il CommonMark?

CommonMark was developed to address inconsistencies and ambiguities in Markdown.

The CommonMark specification defines rules for elements such as headings, lists, links, emphasis,
and code blocks, among others.

By adhering to the CommonMark standard, developers can ensure consistent rendering of Markdown
content across different applications and platforms.

# Cosa è il GitHub Flavored Markdown?

GitHub Flavored Markdown, often shortened as GFM, is the dialect of Markdown that is currently
supported for user content on GitHub.com and GitHub Enterprise.

This formal specification, based on the CommonMark Spec, defines the syntax and semantics of this
dialect.

GFM is a strict superset of CommonMark. All the features which are supported in GitHub user content
and that are not specified on the original CommonMark Spec are hence known as extensions, and
highlighted as such.

While GFM supports a wide range of inputs, it’s worth noting that GitHub.com and GitHub Enterprise
perform additional post-processing and sanitization after GFM is converted to HTML to ensure
security and consistency of the website.

# Cosa sono le formule LaTeX?

LaTeX is a typesetting system commonly used for producing scientific and mathematical documents.

LaTeX provides a powerful way to represent mathematical notation in documents, allowing users to
create complex equations and formulas with ease.

# Come si usano le formule LaTeX nel Markdown?

Mathematical expressions are key to information sharing amongst engineers, scientists, data
scientists, and mathematicians.

You can use the $ and $$ delimiters in GFM to insert math expressions in TeX and LaTeX style syntax.

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
