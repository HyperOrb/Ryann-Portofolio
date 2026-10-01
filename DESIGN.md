# Portfolio redesign

## Audit and scope

The site is seven static HTML pages with one shared stylesheet and script. That architecture stays: there is no framework or build tool to migrate. Existing project narratives, imagery, demos, repositories, team credits, contact details, project filtering, quick search, and the developer terminal are retained.

The previous design competes with its own content: a blocking intro, canvas particles, gradients, custom cursor, sound synthesizer, accent switcher, nested glass panels, six requested font families, and repeated decorative metadata. Four case pages also have inaccessible menu controls. The shared script has unsafe terminal HTML interpolation, racing filter timers, repeated event registration, and controls that depend on GSAP loading. Case images lack dimensions and load multi-megabyte PNGs eagerly. Lenis is downloaded but unused.

Remove the effects and third-party runtimes. Keep motion in image/arrow feedback, native dialogs, and short, once-only section entrances. Preserve visible content when JavaScript is unavailable.

## Reference lock

The user authorized direct implementation and substantial creative freedom. No live Refero tools are configured; research uses its bundled typography, motion, craft, and anti-generic guidance plus these primary references:

- [Pentagram digital work](https://www.pentagram.com/digital-design): primary direction; light canvas, deliberate scale, generous real work imagery, and a portfolio-first hierarchy. Adapt the principles, not the brand or layout.
- [Brittany Chiang](https://brittanychiang.com/): explicit project roles, context, technologies, and straightforward navigation. No navy palette or copied composition.
- [Josh Comeau about](https://www.joshwcomeau.com/about-josh/): genuine first-person identity and optional interactions that do not obstruct content.

Preserve: cool neutral canvas, ink typography, oversized same-family display type, fine dividing rules, actual product images, asymmetric composition, and a single cobalt interaction accent. Reject: decorative serif word swaps, gradients, glass, fake evidence, progress-bar skill ratings, replacement cursors, autoplay sound, and blocked entrances.

## Decisions and tokens

| Decision                                 | Source                                     | Role / reason                                                                                               |
| ---------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------------------------- |
| Cool white `#f5f6f8`, ink `#16181d`      | Primary reference + brief                  | Neutral canvas lets six distinct projects carry their own imagery.                                          |
| Cobalt `#2456ed`                         | Brief: one restrained accent               | Primary actions, active controls, links, and keyboard focus.                                                |
| Geist variable + Fragment Mono           | Existing identity + typography craft       | Display/body in Geist; mono exclusively for small labels and terminal. Self-host only needed Latin subsets. |
| 1280px maximum, fluid 20–80px gutters    | Primary reference + responsive brief       | Consistent alignment, readable content, intentional phone composition.                                      |
| 4/8/12/16/24/32/48/64/96 spacing         | Craft guidance                             | Predictable rhythm and content density.                                                                     |
| Project image panels, flat text sections | Primary reference + actual asset inventory | Imagery belongs to work; unrelated content needs no card chrome.                                            |
| Native mobile/search dialogs             | Accessibility + native-first requirement   | Built-in focus containment, Escape, and focus restoration.                                                  |
| Screenshot switcher for Nest             | Real screenshots + interaction brief       | Shows actual interface/context; supports keyboard and touch.                                                |
| 120/200/360ms, cubic-bezier(.22,1,.36,1) | Motion craft                               | Short feedback with no delayed navigation; reduced-motion override.                                         |

## Content corrections

Pojok Baca's bundled Android source implements a keyword-based sentiment engine; TensorFlow Lite is a future integration, not a deployed model. Tokopedia's reported best iteration is 96.22%, and its improvement from 63.78% is 32.44 percentage points. Preserve reported results without inventing an evaluation method. Card Cast's contradictory end-condition copy is kept focused on its verified wave/phase mechanics.

## Asset strategy

Use optimized WebP copies of existing screenshots and portrait; preserve originals. Cap screenshot widths at 1600px, portrait at 900px, and icons at 512px. Give every image intrinsic dimensions and lazy-load below the fold. Load animated demos only on request. No fabricated imagery or new production dependencies.

## Local use

Serve this folder with any static server, for example `python3 -m http.server 4173`, then open `http://localhost:4173`. There is no compilation step. Edit homepage content in `index.html`, case narratives in their named HTML files, global tokens in `style.css`, and case layouts in `case-study.css`.


## Validation

- All seven routes checked at 320, 390, 650, 768, 1024, 1440, and 1920px: no horizontal overflow or broken images.
- Browser checks cover mobile focus containment/restoration, Escape, search/empty state/arrow navigation, rapid project filters, screenshot switching, terminal HTML-injection resistance/history/links, clipboard copy, opt-in demo play/pause, and reduced motion. The reusable check is `scripts/verify-browser.js`.
- Narrow mobile content and navigation also work without JavaScript.
- Axe WCAG 2/2.1 A/AA: zero violations across seven pages at desktop and mobile, plus open menu/search states. Manual checks supplement automated scans; this is not a claim of accessibility certification.
- Final local Lighthouse mobile performance: **97**, LCP **2.4s**, TBT **10ms**, CLS **0**, transferred **291 KiB**. These are local lab measurements, not deployed-network guarantees. Responsive sources reduced transfer from the initial 530 KiB audit.
- Native screenshot galleries provide full-size links; phone previews, mobile hero composition, and case tables were inspected visually.
- JavaScript syntax and diff whitespace checks pass. No production packages, bundler, or build step were added.

To repeat the browser check with the local server running:

```sh
npx --yes --package @playwright/cli playwright-cli -s=portfolio-check open http://127.0.0.1:4173
npx --yes --package @playwright/cli playwright-cli -s=portfolio-check run-code --filename=scripts/verify-browser.js
```

The browser tool is verification-only. Production pages use local HTML, CSS, JavaScript, WebP images, and two locally hosted font subsets.
