// Run against the local static server:
// playwright-cli -s=portfolio open http://127.0.0.1:4173
// playwright-cli -s=portfolio run-code --filename=scripts/verify-browser.js
async function verifyPortfolio(page) {
    const base = 'http://127.0.0.1:4173/';
    const routes = [
        'index.html',
        'nest.html',
        'pip.html',
        'beautyofbali.html',
        'tokopedia.html',
        'cardcast.html',
        'tamanbaca.html',
    ];
    const widths = [320, 390, 650, 768, 1024, 1440, 1920];
    const errors = [];
    const assert = (condition, message) => {
        if (!condition) throw new Error(message);
    };
    const onError = (error) => errors.push(error.message);
    page.on('pageerror', onError);
    await page.emulateMedia({ reducedMotion: 'reduce' });
    for (const route of routes) {
        await page.goto(base + route);
        await page.evaluate(() => document.fonts.ready);
        for (const width of widths) {
            await page.setViewportSize({ width, height: 900 });
            const state = await page.evaluate(() => ({
                width: innerWidth,
                scrollWidth: document.documentElement.scrollWidth,
                h1: document.querySelectorAll('h1').length,
                undimensioned: [...document.images]
                    .filter((i) => !i.width || !i.height)
                    .map((i) => i.src),
                unnamed: [...document.querySelectorAll('button')].filter(
                    (b) =>
                        !b.textContent.trim() && !b.getAttribute('aria-label'),
                ).length,
                animation: getComputedStyle(document.querySelector('h1'))
                    .animationName,
            }));
            assert(
                state.scrollWidth <= state.width + 1,
                `${route} overflows at ${width}: ${state.scrollWidth}`,
            );
            assert(state.h1 === 1, `${route}: exactly one H1 required`);
            assert(
                !state.undimensioned.length,
                `${route}: images missing dimensions`,
            );
            assert(!state.unnamed, `${route}: buttons missing names`);
            assert(
                state.animation === 'none',
                `${route}: reduced motion not respected`,
            );
        }
        const missing = await page.evaluate(async () => {
            const images = [...document.images];
            await Promise.all(
                images.map((image) => {
                    image.loading = 'eager';
                    return image.decode().catch(() => {});
                }),
            );
            return images.filter((i) => !i.naturalWidth).map((i) => i.src);
        });
        assert(!missing.length, `${route}: broken images ${missing}`);
    }
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto(base);
    await page.getByRole('button', { name: 'Menu', exact: true }).click();
    assert(
        await page.locator('#mobileMenu').evaluate((d) => d.open),
        'Mobile menu must open',
    );
    await page.keyboard.press('Tab');
    assert(
        await page.evaluate(
            () => document.activeElement.closest('#mobileMenu') !== null,
        ),
        'Menu must contain keyboard focus',
    );
    await page.keyboard.press('Escape');
    await page.waitForFunction(
        () =>
            !document.getElementById('mobileMenu').open &&
            document
                .getElementById('menuToggle')
                .getAttribute('aria-expanded') === 'false',
    );
    assert(
        await page.evaluate(() => document.activeElement.id === 'menuToggle'),
        'Menu must return focus',
    );
    assert(
        (await page.locator('#menuToggle').getAttribute('aria-expanded')) ===
            'false',
        'Menu expanded state must reset',
    );
    await page
        .getByRole('button', {
            name: 'Search projects and navigation, Command or Control K',
        })
        .click();
    await page.locator('#searchInput').fill('swift');
    await page.keyboard.press('ArrowDown');
    assert(
        await page.evaluate(
            () => document.activeElement.getAttribute('href') === 'nest.html',
        ),
        'Search arrow key must select matching project',
    );
    await page.locator('#searchInput').fill('no-such-project');
    assert(
        await page.locator('#searchEmpty').isVisible(),
        'Search must have an empty state',
    );
    await page.keyboard.press('Escape');
    await page.keyboard.press('Control+k');
    assert(
        await page.locator('#searchDialog').evaluate((d) => d.open),
        'Search shortcut must open dialog',
    );
    await page.keyboard.press('Escape');
    for (const [filter, count] of [
        ['native', 2],
        ['web', 2],
        ['interactive', 2],
        ['native', 2],
        ['all', 6],
    ]) {
        await page.locator(`[data-filter="${filter}"]`).click();
        assert(
            (await page.locator('#projectGrid > article:visible').count()) ===
                count,
            `Filter ${filter} must show ${count} projects`,
        );
    }
    await page.getByRole('button', { name: 'In context', exact: true }).click();
    assert(
        (await page.locator('#nestPreview').getAttribute('src')).endsWith(
            'nest-2.webp',
        ),
        'Nest preview must switch',
    );
    await page.getByRole('button', { name: 'Overview', exact: true }).click();
    await page.locator('#terminal > summary').click();
    await page
        .locator('#terminalInput')
        .fill('<img src=x onerror="window.__terminalInjected=true">');
    await page.locator('#terminalInput').press('Enter');
    assert(
        (await page.locator('#terminalOutput img').count()) === 0,
        'Terminal must never parse user HTML',
    );
    assert(
        !(await page.evaluate(() => window.__terminalInjected)),
        'Terminal must not execute user content',
    );
    await page.locator('#terminalInput').fill('projects');
    await page.locator('#terminalInput').press('Enter');
    assert(
        (await page.locator('#terminalOutput a').count()) === 6,
        'Terminal must list six working project links',
    );
    await page.locator('#terminalInput').press('ArrowUp');
    assert(
        (await page.locator('#terminalInput').inputValue()) === 'projects',
        'Terminal history must work',
    );
    await page.locator('[data-command="clear"]').click();
    assert(
        (await page.locator('#terminalOutput').textContent()) === '',
        'Terminal clear must work',
    );
    await page
        .context()
        .grantPermissions(['clipboard-read', 'clipboard-write']);
    await page.locator('[data-copy-email]').click();
    await page.waitForFunction(
        () =>
            document.querySelector('[data-copy-label]').textContent ===
            'Copied',
    );
    assert(
        (await page.evaluate(() => navigator.clipboard.readText())) ===
            'ryann.chandiari@gmail.com',
        'Clipboard must receive real email',
    );
    await page.goto(base + 'pip.html');
    await page.locator('[data-demo-src]').click();
    assert(
        (await page.locator('#demoImage').getAttribute('src')).endsWith(
            'pip-demo.gif',
        ),
        'Pip demo must play on request',
    );
    await page.locator('[data-demo-src]').click();
    assert(
        (await page.locator('#demoImage').getAttribute('src')).endsWith(
            'pip-demo.webp',
        ),
        'Pip demo must pause to static poster',
    );
    assert(
        !errors.length,
        `Unexpected JavaScript errors: ${errors.join(', ')}`,
    );
    page.off('pageerror', onError);
    await page.goto(base);
    await page.setViewportSize({ width: 1440, height: 1000 });
    console.log(
        `PASS: ${routes.length} routes × ${widths.length} widths, menus, focus, search, filters, preview, safe terminal, clipboard, opt-in demo, reduced motion, and no JavaScript errors.`,
    );
}
