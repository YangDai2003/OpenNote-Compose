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
- [Sintassi Markdown di base](#sintassi-markdown-di-base)
- [Elementi avanzati di sintassi Markdown](#elementi-avanzati-di-sintassi-markdown)
- [Sintassi delle formule LaTeX](#sintassi-delle-formule-latex)
- [Lettere greche](#lettere-greche)
- [Scorciatoie da tastiera](#scorciatoie-da-tastiera)
- [Dispense](#dispense)

# Cosa è il Markdown?

Markdown is a plain text format for writing structured documents, based on conventions for
indicating formatting in email and usenet posts.
It was developed by John Gruber (with help from Aaron Swartz) and released in 2004 in the form of a
syntax description and a Perl script (Markdown.pl) for converting Markdown to HTML.

In the next decade, dozens of implementations were developed in many languages.
Some extended the original Markdown syntax with conventions for footnotes, tables, and other
document elements.
Some allowed Markdown documents to be rendered in formats other than HTML.

Websites like Reddit, StackOverflow, and GitHub had millions of people using Markdown.
And Markdown started to be used beyond the web, to author books, articles, slide shows, letters, and
lecture notes.

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

OpenNote supports CommonMark and GitHub Flavored Markdown (GFM) syntax, as well as LaTeX math
syntax.

This allows users to create richly formatted notes with support for headings, lists, links,
emphasis, code blocks, tables, and mathematical expressions, and allows notes to be exported in
various formats including TXT, MD (Markdown), and HTML.

# Elementi avanzati di sintassi Markdown

|    Elemento    |              Sintassi              |
|:--------------:|:----------------------------------:|
|     Titolo     | `# H1` <br/> `## H2`<br/> `### H3` |
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
|   ID Titolo   |      `# Heading {#custom-id}`      |

# Sintassi delle formule LaTeX

|  Elemento   |      Sintassi      |
|:-----------:|:------------------:|
|   Inline    |      `$x^2$`       |
|   Display   |     `$$x^2$$`      |
|   newline   |    `\\ or \\\\`    |
| Whitespace  |    `\quad or \`    |
| Superscript |       `x^2`        |
|  Subscript  |       `y_1`        |
| Expression  |       `{x}`        |
|  Overline   |   `\overline{x}`   |
|  Underline  |  `\underline{x}`   |
|  Fraction   |   `\frac{x}{y}`    |
| Left Paren  |      `\left(`      |
| Right Paren |     `\right)`      |
| Parentheses | `\left(x+y\right)` |
|  Parentesi  | `\left[x+y\right]` |
|   Braces    | `\left{x+y\right}` |
|    Root     |   `\sqrt[n]{x}`    |
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
|    Select All    |    `Ctrl+A`    |
|       Cut        |    `Ctrl+X`    |
|       Copy       |    `Ctrl+C`    |
|      Paste       |    `Ctrl+V`    |
|       Undo       |    `Ctrl+Z`    |
|       Redo       |    `Ctrl+Y`    |
| Find and Replace |    `Ctrl+F`    |
|       Bold       |    `Ctrl+B`    |
|      Italic      |    `Ctrl+I`    |
|    Underline     |    `Ctrl+U`    |
|  Strikethrough   |    `Ctrl+D`    |
|       Mark       |    `Ctrl+M`    |
|      Table       |    `Ctrl+T`    |
|       Link       |    `Ctrl+K`    |
|     Preview      |    `Ctrl+P`    |
|     Heading      |   `Ctrl+1~6`   |
|      Image       | `Ctrl+Shift+I` |
|       List       | `Ctrl+Shift+L` |
|       Scan       | `Ctrl+Shift+S` |
|       Code       | `Ctrl+Shift+K` |
|      Quote       | `Ctrl+Shift+Q` |
|       Math       | `Ctrl+Shift+M` |
| Horizontal Rule  | `Ctrl+Shift+R` |
|       Task       | `Ctrl+Shift+T` |
| Mermaid Diagram  | `Ctrl+Shift+D` |
|    Templates     | `Ctrl+Shift+P` |
|      Audio       | `Ctrl+Shift+A` |
|      Video       | `Ctrl+Shift+V` |

# Dispense

Puoi trovare ulteriori informazioni su Markdown, CommonMark, GitHub Flavored Markdown, LaTeX Math e diagrammi Mermaid ai seguenti link:

- [CommonMark](https://commonmark.org/)
- [GitHub Flavored Markdown](https://github.github.com/gfm/)
- [LaTeX](https://www.latex-project.org/)
- [Mermaid](https://mermaid.js.org/)
