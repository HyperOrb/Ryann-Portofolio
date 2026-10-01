'use strict';

const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
const email = 'ryann.chandiari@gmail.com';
const projectLinks = [
    ['Nest', 'nest.html', 'Finder-native macOS AI companion'],
    ['Pip', 'pip.html', 'Local, screen-aware macOS agent'],
    [
        'Beauty of Bali',
        'beautyofbali.html',
        'Culture and travel web experience',
    ],
    [
        'Tokopedia Sentiment',
        'tokopedia.html',
        'Interpretable review classification',
    ],
    ['Card Cast!', 'cardcast.html', 'Unity tower defense deckbuilder'],
    ['Pojok Baca', 'tamanbaca.html', 'Android library and sentiment engine'],
];

// Native dialogs supply focus containment, Escape, and focus restoration.
const mobileMenu = document.getElementById('mobileMenu');
const menuToggle = document.getElementById('menuToggle');
const searchDialog = document.getElementById('searchDialog');
const searchInput = document.getElementById('searchInput');
const searchResults = document.getElementById('searchResults');

function openDialog(dialog) {
    if (!dialog || dialog.open) return;
    document.querySelectorAll('dialog[open]').forEach((other) => other.close());
    dialog.showModal();
}

document.querySelectorAll('dialog').forEach((dialog) => {
    // Search fields consume Escape to clear their text in some browsers.
    // Closing explicitly keeps Escape consistent across every dialog state.
    dialog.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            event.preventDefault();
            dialog.close();
        }
    });
    dialog.querySelectorAll('[data-close-dialog]').forEach((button) => {
        button.addEventListener('click', () => dialog.close());
    });
    dialog.querySelectorAll('a').forEach((link) => {
        link.addEventListener('click', () => dialog.close());
    });
    dialog.addEventListener('click', (event) => {
        if (event.target !== dialog) return;
        const box = dialog.getBoundingClientRect();
        if (
            event.clientX < box.left ||
            event.clientX > box.right ||
            event.clientY < box.top ||
            event.clientY > box.bottom
        )
            dialog.close();
    });
});

if (mobileMenu && menuToggle) {
    menuToggle.addEventListener('click', () => {
        openDialog(mobileMenu);
        menuToggle.setAttribute('aria-expanded', 'true');
    });
    mobileMenu.addEventListener('close', () =>
        menuToggle.setAttribute('aria-expanded', 'false'),
    );
    window
        .matchMedia('(min-width: 651px)')
        .addEventListener('change', (event) => {
            if (event.matches && mobileMenu.open) mobileMenu.close();
        });
}

function filterSearch() {
    if (!searchInput || !searchResults) return;
    const query = searchInput.value.trim().toLowerCase();
    const links = [...searchResults.querySelectorAll('a')];
    links.forEach((link) => {
        link.hidden = !(link.dataset.search || link.textContent)
            .toLowerCase()
            .includes(query);
    });
    searchResults.querySelectorAll('.eyebrow').forEach((heading) => {
        heading.hidden = Boolean(query);
    });
    document.getElementById('searchEmpty').hidden = links.some(
        (link) => !link.hidden,
    );
}

function openSearch() {
    if (!searchDialog) return;
    searchInput.value = '';
    filterSearch();
    openDialog(searchDialog);
    searchInput.focus();
}

document
    .querySelectorAll('[data-open-search]')
    .forEach((button) => button.addEventListener('click', openSearch));
if (searchDialog) {
    searchInput.addEventListener('input', filterSearch);
    document.addEventListener('keydown', (event) => {
        if (
            (event.metaKey || event.ctrlKey) &&
            event.key.toLowerCase() === 'k'
        ) {
            event.preventDefault();
            if (searchDialog.open) searchDialog.close();
            else openSearch();
        }
    });
    searchDialog.addEventListener('keydown', (event) => {
        const links = [...searchResults.querySelectorAll('a')].filter(
            (link) => !link.hidden,
        );
        if (!links.length) return;
        const current = links.indexOf(document.activeElement);
        if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
            event.preventDefault();
            const next =
                event.key === 'ArrowDown'
                    ? (current + 1) % links.length
                    : current < 0
                      ? links.length - 1
                      : (current - 1 + links.length) % links.length;
            links[next].focus();
        } else if (
            event.key === 'Enter' &&
            document.activeElement === searchInput
        ) {
            event.preventDefault();
            links[0].click();
        }
    });
}

// Filter immediately, without racing animation timers or changing the reading order.
const filterButtons = document.querySelectorAll('[data-filter]');
const projects = document.querySelectorAll('#projectGrid > [data-category]');
filterButtons.forEach((button) => {
    button.addEventListener('click', () => {
        filterButtons.forEach((other) =>
            other.setAttribute('aria-pressed', String(other === button)),
        );
        let count = 0;
        projects.forEach((project) => {
            project.hidden =
                button.dataset.filter !== 'all' &&
                project.dataset.category !== button.dataset.filter;
            if (!project.hidden) count++;
        });
        const status = document.getElementById('filterStatus');
        if (status)
            status.textContent = `Showing ${count} projects: ${button.textContent.trim()}.`;
    });
});

// The screenshot retains a fixed display slot while the selected image loads.
const nestPreview = document.getElementById('nestPreview');
if (nestPreview) {
    document.querySelectorAll('[data-preview]').forEach((button) => {
        button.addEventListener('click', () => {
            if (button.getAttribute('aria-pressed') === 'true') return;
            nestPreview.srcset = button.dataset.previewSrcset;
            nestPreview.src = button.dataset.preview;
            nestPreview.alt = button.dataset.previewAlt;
            document
                .querySelectorAll('[data-preview]')
                .forEach((other) =>
                    other.setAttribute(
                        'aria-pressed',
                        String(other === button),
                    ),
                );
        });
    });
}

// Only run a small entrance on sections as they arrive. Never hide content first.
if (!reducedMotion.matches && 'IntersectionObserver' in window) {
    const revealObserver = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('reveal-enter');
                    revealObserver.unobserve(entry.target);
                }
            });
        },
        { threshold: 0.1 },
    );
    document
        .querySelectorAll('[data-reveal]')
        .forEach((element) => revealObserver.observe(element));
}

// Observe one narrow horizontal band so navigation reflects the visible section.
if ('IntersectionObserver' in window) {
    const sectionObserver = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                const link = document.querySelector(
                    `.desktop-nav a[href="#${entry.target.id}"]`,
                );
                if (!link) return;
                if (entry.isIntersecting) {
                    document
                        .querySelectorAll('.desktop-nav a[aria-current]')
                        .forEach((other) =>
                            other.removeAttribute('aria-current'),
                        );
                    link.setAttribute('aria-current', 'location');
                } else {
                    link.removeAttribute('aria-current');
                }
            });
        },
        { rootMargin: '-20% 0px -70% 0px' },
    );
    document
        .querySelectorAll('main > section[id]')
        .forEach((section) => sectionObserver.observe(section));
}

let toastTimer;
function showToast(message) {
    const toast = document.getElementById('toast');
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add('is-visible');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('is-visible'), 3500);
}

document.querySelectorAll('[data-copy-email]').forEach((button) => {
    let resetTimer;
    button.addEventListener('click', async () => {
        try {
            await navigator.clipboard.writeText(email);
            const label = button.querySelector('[data-copy-label]');
            if (label) label.textContent = 'Copied';
            showToast('Email address copied.');
            clearTimeout(resetTimer);
            resetTimer = setTimeout(() => {
                if (label) label.textContent = 'Copy';
            }, 2500);
        } catch {
            showToast(
                'Copy unavailable. You can select the email address or open the email link.',
            );
        }
    });
});

const clock = new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Asia/Jakarta',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
});
function updateClock() {
    document.querySelectorAll('[data-local-time]').forEach((element) => {
        element.textContent = `${clock.format(new Date())} WIB`;
    });
}
if (document.querySelector('[data-local-time]')) {
    updateClock();
    setInterval(updateClock, 60000);
}
document.querySelectorAll('[data-year]').forEach((element) => {
    element.textContent = new Date().getFullYear();
});

// Terminal content is always built as text or known links, never parsed from input.
const terminalForm = document.getElementById('terminalForm');
const terminalInput = document.getElementById('terminalInput');
const terminalOutput = document.getElementById('terminalOutput');
const terminalHistory = [];
let historyIndex = 0;
const terminalResponses = {
    help: 'Commands: whoami, about, skills, projects, nest, pip, bali, tokopedia, cardcast, tamanbaca, contact, github, linkedin, date, pwd, clear.\nUse ↑ and ↓ for command history. Links open the real project pages.',
    whoami: 'Ryann Chandiari — Computer Science student & developer.\nBased in Jakarta, Indonesia. Curious about native macOS, agentic AI, and thoughtful digital experiences.',
    about: 'I enjoy turning technical ideas into clear, useful experiences.\nCurrently studying Computer Science and open to software & AI opportunities.',
    skills: 'Native: Swift, SwiftUI, AppKit, macOS Automation\nAI & data: Ollama, Python, Scikit-learn, LLM APIs, NLP\nWeb: JavaScript, React, Vite, HTML, CSS, Tailwind\nMobile & games: Kotlin, Jetpack Compose, Firebase, Unity, C#, Git',
    contact: `Email: ${email}\nGitHub: github.com/HyperOrb\nLinkedIn: linkedin.com/in/rynnchan\nInstagram: instagram.com/rynnchn_/`,
    pwd: '/home/ryann/portfolio',
};

function runCommand(value) {
    if (!terminalOutput) return;
    const command = value.trim().slice(0, 200);
    if (!command) return;
    terminalHistory.push(command);
    if (terminalHistory.length > 50) terminalHistory.shift();
    historyIndex = terminalHistory.length;
    terminalInput.value = '';
    const key = command.toLowerCase();
    const projectAliases = {
        nest: 'Nest',
        pip: 'Pip',
        bali: 'Beauty of Bali',
        tokopedia: 'Tokopedia Sentiment',
        cardcast: 'Card Cast!',
        tamanbaca: 'Pojok Baca',
    };
    if (key === 'clear') {
        terminalOutput.replaceChildren();
        return;
    }
    const prompt = document.createElement('p');
    prompt.className = 'terminal-command';
    prompt.textContent = `guest ~ $ ${command}`;
    terminalOutput.append(prompt);
    const response = document.createElement('p');
    if (key === 'projects' || key === 'ls') {
        projectLinks.forEach(([name, href, description]) => {
            const link = document.createElement('a');
            link.href = href;
            link.textContent = `${name} — ${description}`;
            response.append(link, document.createElement('br'));
        });
    } else if (Object.hasOwn(projectAliases, key)) {
        const project = projectLinks.find(
            ([name]) => name === projectAliases[key],
        );
        const link = document.createElement('a');
        link.href = project[1];
        link.textContent = `${project[0]} — ${project[2]}. Open the case study ↗`;
        response.append(link);
    } else if (key === 'github' || key === 'linkedin') {
        const link = document.createElement('a');
        link.href =
            key === 'github'
                ? 'https://github.com/HyperOrb'
                : 'https://www.linkedin.com/in/rynnchan';
        link.textContent = link.href;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        response.append(link);
    } else {
        response.textContent =
            key === 'date'
                ? new Intl.DateTimeFormat('en-GB', {
                      timeZone: 'Asia/Jakarta',
                      dateStyle: 'full',
                      timeStyle: 'short',
                  }).format(new Date())
                : Object.hasOwn(terminalResponses, key)
                  ? terminalResponses[key]
                  : `Command not found: ${command}. Type help to see what you can try.`;
    }
    terminalOutput.append(response);
    while (terminalOutput.children.length > 100)
        terminalOutput.firstElementChild.remove();
    terminalOutput.scrollTop = terminalOutput.scrollHeight;
}

if (terminalForm) {
    terminalForm.addEventListener('submit', (event) => {
        event.preventDefault();
        runCommand(terminalInput.value);
    });
    document
        .querySelectorAll('[data-command]')
        .forEach((button) =>
            button.addEventListener('click', () =>
                runCommand(button.dataset.command),
            ),
        );
    terminalInput.addEventListener('keydown', (event) => {
        if (event.key !== 'ArrowUp' && event.key !== 'ArrowDown') return;
        event.preventDefault();
        historyIndex = Math.min(
            terminalHistory.length,
            Math.max(0, historyIndex + (event.key === 'ArrowUp' ? -1 : 1)),
        );
        terminalInput.value = terminalHistory[historyIndex] || '';
    });
}

// Demo animation is opt-in and can always be paused back to its static poster.
document.querySelectorAll('[data-demo-src]').forEach((button) => {
    const image = document.getElementById(button.dataset.demoTarget);
    if (!image) return;
    const poster = image.getAttribute('src');
    button.setAttribute('aria-pressed', 'false');
    const stop = () => {
        image.src = poster;
        button.setAttribute('aria-pressed', 'false');
        button.textContent = 'Play demo';
    };
    button.addEventListener('click', () => {
        if (button.getAttribute('aria-pressed') === 'true') stop();
        else {
            image.src = button.dataset.demoSrc;
            button.setAttribute('aria-pressed', 'true');
            button.textContent = 'Pause demo';
        }
    });
    reducedMotion.addEventListener('change', (event) => {
        if (event.matches) stop();
    });
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) stop();
    });
});

// Activate enhanced controls only after their listeners are ready.
document.documentElement.classList.add('js-enabled');
