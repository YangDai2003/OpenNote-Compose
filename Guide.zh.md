- [简体中文](Guide.zh.md)
- [English](README.md)

# 导航

- [什么是 Markdown？](#什么是-markdown)
- [什么是 CommonMark？](#什么是-commonmark)
- [什么是 GitHub Flavored Markdown？](#什么是-github-flavored-markdown)
- [什么是 LaTeX？](#什么是-latex)
- [怎么在 Markdown 中使用 LaTeX？](#怎么在-markdown-中使用-latex)
- [OpenNote 提供了什么？](#opennote-提供了什么)
- [一些基本的 Markdown 语法](#一些基本的-markdown-语法)
- [一些扩展的 Markdown 语法](#一些扩展的-markdown-语法)
- [LaTeX 数学语法](#latex-数学语法)
- [希腊字母](#希腊字母)
- [参考](#参考)

# 什么是 Markdown？

Markdown 是一种用于编写结构化文档的纯文本格式，基于以下约定指示电子邮件和新闻组帖子中的格式。

它是由约翰·格鲁伯（John Gruber）开发的，并于 2004 年以语法描述和 Perl 脚本的形式发布（Markdown.pl）用于将
Markdown 转换为 HTML。

在接下来的十年里，数十个实施以多种语言开发。
有些使用约定扩展了原始 Markdown 语法脚注、表格和其他文档元素。
有些允许 Markdown 文档呈现在 HTML 以外的格式。

Reddit、StackOverflow 和 GitHub 等网站拥有数百万人使用 Markdown。
Markdown 开始在网络之外使用，用于撰写书籍、文章、幻灯片、信件和讲义。

# 什么是 CommonMark？

CommonMark 的开发是为了解决 Markdown 中的不一致和歧义。

CommonMark 规范定义了标题、列表、链接、强调和代码块等元素的规则。

通过遵守 CommonMark 标准，开发人员可以确保在不同应用程序和平台上一致地呈现 Markdown 内容。

# 什么是 GitHub Flavored Markdown？

GitHub Flavored Markdown（通常缩写为 GFM）是 Markdown 的方言，目前 GitHub.com 和 GitHub Enterprise
上的用户内容受支持。

该正式规范基于 CommonMark 规范，定义了该方言的语法和语义。

GFM 是 CommonMark 的严格超集。 因此，GitHub 用户内容中支持且原始 CommonMark 规范中未指定的所有功能都称为扩展，并如此突出显示。

虽然 GFM 支持广泛的输入，但值得注意的是，GitHub.com 和 GitHub Enterprise 在 GFM 转换为 HTML
后会执行额外的后处理和清理，以确保网站的安全性和一致性。

# 什么是 LaTeX？

LaTeX 是一种通常用于生成科学和数学文档的排版系统。
LaTeX 提供了一种在文档中表示数学符号的强大方法，允许用户轻松创建复杂的方程和公式。

# 怎么在 Markdown 中使用 LaTeX？

数学表达式是工程师、科学家、数据科学家和数学家之间信息共享的关键。
您可以在 GFM 中使用 $ 和 $$ 分隔符以 TeX 和 LaTeX 样式语法插入数学表达式。

# OpenNote 提供了什么？

OpenNote 支持 CommonMark 和 GitHub Flavored Markdown (GFM) 语法，以及 LaTeX 数学语法。
这允许用户创建格式丰富的笔记，支持标题、列表、链接、强调、代码块、表格和数学表达式，并允许以各种格式导出笔记，包括
TXT、MD (Markdown) 和 HTML。

# 一些基本的 Markdown 语法

|  元素  |                   语法                   |
|:----:|:--------------------------------------:|
|  标题  |   `# H1` <br/> `## H2`<br/> `### H3`   |
|  斜体  |         `_italic_ or *italic*`         |
|  粗体  |               `**bold**`               |
|  引用  |             `> Blockquote`             |
|  链接  |   `[title](https://www.example.com)`   |
| 行内代码 |              `` `code` ``              |
| 有序列表 | `1. List item 1`<br/> `2. List item 2` |
| 无序列表 |       `- Apple`<br/> `- Banana`        |
|  图片  |       `![alt text](image.jpeg)`        |

# 一些扩展的 Markdown 语法

|  元素   |                 语法                 |
|:-----:|:----------------------------------:|
|  删除线  |        `~~Strikethrough~~`         |
| 任务列表  | `- [x] Task 1`<br/> `- [ ] Task 2` |
| 标题 ID |      `# Heading {#custom-id}`      |

# LaTeX 数学语法

|  元素  |         语法         |
|:----:|:------------------:|
|  行内  |      `$x^2$`       |
|  行块  |     `$$x^2$$`      |
|  换行  |    `\\ or \\\\`    |
|  空格  |    `\quad or \`    |
|  上标  |       `x^2`        |
|  下标  |       `y_1`        |
| 表达式  |       `{x}`        |
| 上划线  |   `\overline{x}`   |
| 下划线  |  `\underline{x}`   |
|  分数  |   `\frac{x}{y}`    |
| 左小括号 |      `\left(`      |
| 右小括号 |     `\right)`      |
| 小括号  | `\left(x+y\right)` |
| 中括号  | `\left[x+y\right]` |
| 大括号  | `\left{x+y\right}` |
|  根号  |   `\sqrt[n]{x}`    |
|  ×   |      `\times`      |
|  ÷   |       `\div`       |
|  ±   |       `\pm`        |
|  ≠   |       `\neq`       |
|  ≈   |     `\approx`      |
|  ≤   |       `\leq`       |
|  ≥   |       `\geq`       |
|  ∞   |      `\infty`      |
|  ∑   |       `\sum`       |
|  ∏   |      `\prod`       |
|  ∫   |       `\int`       |
|  ∑   |       `\sum`       |
| lim  |       `\lim`       |
|  ∀   |     `\forall`      |
|  ∃   |     `\exists`      |
|  ∴   |    `\therefore`    |
|  ∵   |     `\because`     |
|  ⊂   |     `\subset`      |
|  ⊃   |     `\supset`      |
|  ⊆   |    `\subseteq`     |
|  ⊇   |    `\supseteq`     |
|  ∈   |       `\in`        |
|  ∉   |      `\notin`      |

# 希腊字母

| 大写 |     语法     | 小写 |     语法     |
|:--:|:----------:|:--:|:----------:|
| A  |    `A`     | α  |  `\alpha`  |
| Β  |    `B`     | β  |  `\beta`   |
| Γ  |  `\Gamma`  | γ  |  `\gamma`  |
| Δ  |  `\Delta`  | δ  |  `\delta`  |
| Ε  |    `E`     | ε  | `\epsilon` |
| Ζ  |    `Z`     | ζ  |  `\zeta`   |
| Η  |    `H`     | η  |   `\eta`   |
| Θ  |  `\Theta`  | θ  |  `\theta`  |
| Ι  |    `I`     | ι  |  `\iota`   |
| Κ  |    `K`     | κ  |  `\kappa`  |
| Λ  | `\Lambda`  | λ  | `\lambda`  |
| Μ  |    `M`     | μ  |   `\mu`    |
| Ν  |    `N`     | ν  |   `\nu`    |
| Ξ  |   `\Xi`    | ξ  |   `\xi`    |
| Ο  |    `O`     | ο  | `\omicron` |
| Π  |   `\Pi`    | π  |   `\pi`    |
| Ρ  |    `P`     | ρ  |   `\rho`   |
| Σ  |  `\Sigma`  | σ  |  `\sigma`  |
| Τ  |    `T`     | τ  |   `\tau`   |
| Υ  | `\Upsilon` | υ  | `\upsilon` |
| Φ  |   `\Phi`   | φ  |   `\phi`   |
| Χ  |    `X`     | χ  |   `\chi`   |
| Ψ  |   `\Psi`   | ψ  |   `\psi`   |
| Ω  |  `\Omega`  | ω  |  `\omega`  |

# 键盘快捷键

|     元素     |      快捷键       |
|:----------:|:--------------:|
|     全选     |    `Ctrl+A`    |
|     剪切     |    `Ctrl+X`    |
|     复制     |    `Ctrl+C`    |
|     粘贴     |    `Ctrl+V`    |
|     撤回     |    `Ctrl+Z`    |
|     重做     |    `Ctrl+Y`    |
|   查找与替换    |    `Ctrl+F`    |
|     粗体     |    `Ctrl+B`    |
|     斜体     |    `Ctrl+I`    |
|    下划线     |    `Ctrl+U`    |
|    删除线     |    `Ctrl+D`    |
|     标记     |    `Ctrl+M`    |
|     表格     |    `Ctrl+T`    |
|     链接     |    `Ctrl+K`    |
|     预览     |    `Ctrl+P`    |
|     标题     |   `Ctrl+1~6`   |
|     图片     | `Ctrl+Shift+I` |
|     列表     | `Ctrl+Shift+L` |
|     扫描     | `Ctrl+Shift+S` |
|     代码     | `Ctrl+Shift+K` |
|     引用     | `Ctrl+Shift+Q` |
|     数学     | `Ctrl+Shift+M` |
|    分隔线     | `Ctrl+Shift+R` |
|    任务列表    | `Ctrl+Shift+T` |
| Mermaid 图表 | `Ctrl+Shift+D` |
|     模板     | `Ctrl+Shift+P` |

# 参考

你可以在以下链接中了解更多关于 Markdown、CommonMark、GitHub Flavored Markdown、LaTeX Math 和 Mermaid
的信息：

- [CommonMark](https://commonmark.org/)
- [GitHub Flavored Markdown](https://github.github.com/gfm/)
- [LaTeX](https://www.latex-project.org/)
- [Mermaid](https://mermaid.js.org/)
