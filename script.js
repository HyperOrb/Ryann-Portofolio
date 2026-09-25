/**
 * ==========================================================================
 * RYANN CHANDIARI // HYPERORB PORTFOLIO RUNTIME ENGINE 2026
 * GSAP · Web Audio Synthesizer · 2D Canvas Physics · Cmd+K · CLI Terminal
 * ==========================================================================
 */

// Register GSAP ScrollTrigger if available
if (typeof gsap !== 'undefined' && typeof ScrollTrigger !== 'undefined') {
    gsap.registerPlugin(ScrollTrigger);
    ScrollTrigger.config({ ignoreMobileResize: true });
}

// Global environmental flags
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const canHover = window.matchMedia('(hover: hover) and (pointer: fine)').matches;

/* ─── 1. WEB AUDIO API SYNTHESIZER (ZERO ASSET DEPENDENCY) ─── */
class SoundEngine {
    constructor() {
        this.ctx = null;
        this.isMuted = localStorage.getItem('sfx_muted') === 'true';
        this.initialized = false;
    }

    init() {
        if (this.initialized) return;
        try {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (AudioCtx) {
                this.ctx = new AudioCtx();
                this.initialized = true;
            }
        } catch (e) {
            console.warn('Web Audio not supported in this environment', e);
        }
    }

    playTick(freq = 1400, type = 'sine', duration = 0.035, vol = 0.04) {
        if (this.isMuted || prefersReducedMotion) return;
        this.init();
        if (!this.ctx) return;

        if (this.ctx.state === 'suspended') {
            this.ctx.resume();
        }

        try {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            osc.type = type;
            osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
            osc.frequency.exponentialRampToValueAtTime(freq * 0.7, this.ctx.currentTime + duration);

            gain.gain.setValueAtTime(vol, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + duration);

            osc.connect(gain);
            gain.connect(this.ctx.destination);

            osc.start();
            osc.stop(this.ctx.currentTime + duration);
        } catch (err) {
            // Audio errors fail silently
        }
    }

    playSuccess() {
        this.playTick(1200, 'sine', 0.05, 0.05);
        setTimeout(() => this.playTick(1800, 'sine', 0.06, 0.06), 45);
    }

    toggle() {
        this.isMuted = !this.isMuted;
        localStorage.setItem('sfx_muted', String(this.isMuted));
        return this.isMuted;
    }
}

const soundEngine = new SoundEngine();

/* ─── 2. LIVE TELEMETRY CLOCK (JAKARTA WIB, UTC+7) ─── */
function initLiveClock() {
    const navClock = document.getElementById('navClock');
    const footerClock = document.getElementById('footerClock');

    function updateClocks() {
        try {
            const now = new Date();
            const timeString = now.toLocaleTimeString('en-US', {
                timeZone: 'Asia/Jakarta',
                hour12: false,
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
            const formatted = `${timeString} WIB`;

            if (navClock) navClock.textContent = formatted;
            if (footerClock) footerClock.textContent = formatted;
        } catch (e) {
            // Fallback to local time if timezone string not recognized
            const now = new Date();
            const fallback = now.toTimeString().split(' ')[0] + ' WIB';
            if (navClock) navClock.textContent = fallback;
            if (footerClock) footerClock.textContent = fallback;
        }
    }

    updateClocks();
    setInterval(updateClocks, 1000);
}

/* ─── 3. THEME ACCENT COLOR SWITCHER ─── */
function initThemeSwitcher() {
    const themes = ['cyan', 'lime', 'violet', 'amber'];
    const themeCycleBtn = document.getElementById('themeCycleBtn');

    // Retrieve saved theme or default to cyan
    const savedTheme = localStorage.getItem('theme_accent') || 'cyan';
    document.documentElement.setAttribute('data-accent', savedTheme);

    function cycleTheme() {
        const current = document.documentElement.getAttribute('data-accent') || 'cyan';
        const currentIndex = themes.indexOf(current);
        const nextIndex = (currentIndex + 1) % themes.length;
        const nextTheme = themes[nextIndex];

        document.documentElement.setAttribute('data-accent', nextTheme);
        localStorage.setItem('theme_accent', nextTheme);
        soundEngine.playSuccess();
    }

    if (themeCycleBtn) {
        themeCycleBtn.addEventListener('click', cycleTheme);
    }

    return cycleTheme;
}

/* ─── 4. SFX TOGGLE BUTTON ─── */
function initSfxToggle() {
    const sfxBtn = document.getElementById('sfxToggle');
    const sfxIcon = document.getElementById('sfxIcon');
    const sfxText = document.getElementById('sfxText');

    function updateSfxUI() {
        if (!sfxBtn) return;
        if (soundEngine.isMuted) {
            sfxBtn.classList.add('is-muted');
            if (sfxIcon) sfxIcon.textContent = '✕';
            if (sfxText) sfxText.textContent = 'MUTED';
        } else {
            sfxBtn.classList.remove('is-muted');
            if (sfxIcon) sfxIcon.textContent = '♫';
            if (sfxText) sfxText.textContent = 'SFX';
        }
    }

    updateSfxUI();

    if (sfxBtn) {
        sfxBtn.addEventListener('click', () => {
            soundEngine.toggle();
            updateSfxUI();
            if (!soundEngine.isMuted) {
                soundEngine.playSuccess();
            }
        });
    }
}

/* ─── 5. CUSTOM MAGNET CURSOR ─── */
function initCustomCursor() {
    const cursorDot = document.querySelector('.cursor-dot');
    const cursorRing = document.querySelector('.cursor-ring');
    const cursorGlow = document.querySelector('.cursor-glow');
    const cursorLabel = document.getElementById('cursorLabel');

    if (!canHover || prefersReducedMotion || !cursorDot || !cursorRing) return;

    let mouseX = window.innerWidth / 2;
    let mouseY = window.innerHeight / 2;
    let ringX = mouseX;
    let ringY = mouseY;
    let glowX = mouseX;
    let glowY = mouseY;

    window.addEventListener('pointermove', (e) => {
        mouseX = e.clientX;
        mouseY = e.clientY;

        // Fast update for dot
        cursorDot.style.transform = `translate3d(${mouseX}px, ${mouseY}px, 0) translate(-50%, -50%)`;
    }, { passive: true });

    // Smooth physics loop for ring and glow
    function renderCursor() {
        ringX += (mouseX - ringX) * 0.18;
        ringY += (mouseY - ringY) * 0.18;
        glowX += (mouseX - glowX) * 0.08;
        glowY += (mouseY - glowY) * 0.08;

        cursorRing.style.transform = `translate3d(${ringX}px, ${ringY}px, 0) translate(-50%, -50%)`;
        if (cursorGlow) {
            cursorGlow.style.transform = `translate3d(${glowX}px, ${glowY}px, 0) translate(-50%, -50%)`;
        }

        requestAnimationFrame(renderCursor);
    }
    requestAnimationFrame(renderCursor);

    // Hover attachments
    const hoverTargets = document.querySelectorAll('a, button, [data-hover], .bento-card, .skill-tag, .tenet-card');
    hoverTargets.forEach((target) => {
        target.addEventListener('mouseenter', () => {
            cursorRing.classList.add('is-hovered');
            cursorDot.classList.add('is-hidden');
            const customLabel = target.getAttribute('data-hover');
            if (customLabel && cursorLabel) {
                cursorLabel.textContent = customLabel;
            } else if (cursorLabel) {
                cursorLabel.textContent = 'VIEW';
            }
            soundEngine.playTick(1600, 'sine', 0.02, 0.02);
        });

        target.addEventListener('mouseleave', () => {
            cursorRing.classList.remove('is-hovered');
            cursorDot.classList.remove('is-hidden');
        });
    });
}

/* ─── 6. INTERACTIVE HYPERORB LIVING CANVAS ─── */
function initLivingCanvas() {
    const canvas = document.getElementById('orbCanvas');
    if (!canvas || prefersReducedMotion) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = 0;
    let height = 0;
    let dpr = Math.min(window.devicePixelRatio || 1, 2);
    let isVisible = true;

    function resize() {
        width = canvas.clientWidth;
        height = canvas.clientHeight;
        canvas.width = width * dpr;
        canvas.height = height * dpr;
        ctx.scale(dpr, dpr);
    }
    resize();
    window.addEventListener('resize', resize, { passive: true });

    // Observe hero visibility to prevent wasteful computation offscreen
    const heroObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            isVisible = entry.isIntersecting;
        });
    }, { threshold: 0.05 });

    const heroSection = document.getElementById('hero');
    if (heroSection) heroObserver.observe(heroSection);

    // Particles system
    const PARTICLE_COUNT = Math.min(Math.floor(window.innerWidth / 18), 75);
    const particles = [];
    const centerOrb = {
        x: width * 0.72,
        y: height * 0.44,
        baseRadius: 80,
        currentRadius: 80,
        pulseSpeed: 0.03,
        pulseVal: 0
    };

    let pointer = {
        x: width * 0.72,
        y: height * 0.44,
        targetX: width * 0.72,
        targetY: height * 0.44,
        isHovered: false
    };

    // Click shockwave
    let shockwave = {
        x: 0,
        y: 0,
        radius: 0,
        active: false,
        maxRadius: 360,
        speed: 12
    };

    class Particle {
        constructor() {
            this.reset(true);
        }

        reset(initial = false) {
            this.angle = Math.random() * Math.PI * 2;
            this.orbitRadius = 90 + Math.random() * 260;
            this.orbitSpeed = (0.003 + Math.random() * 0.008) * (Math.random() > 0.5 ? 1 : -1);
            this.size = 1 + Math.random() * 2.2;
            this.alpha = 0.2 + Math.random() * 0.65;
            this.driftX = (Math.random() - 0.5) * 0.4;
            this.driftY = (Math.random() - 0.5) * 0.4;

            this.x = initial ? centerOrb.x + Math.cos(this.angle) * this.orbitRadius : centerOrb.x;
            this.y = initial ? centerOrb.y + Math.sin(this.angle) * this.orbitRadius : centerOrb.y;
            this.vx = 0;
            this.vy = 0;
        }

        update() {
            this.angle += this.orbitSpeed;

            // Target orbit position relative to center orb
            const targetX = centerOrb.x + Math.cos(this.angle) * this.orbitRadius;
            const targetY = centerOrb.y + Math.sin(this.angle) * this.orbitRadius;

            // Natural spring toward orbit position
            this.vx += (targetX - this.x) * 0.02;
            this.vy += (targetY - this.y) * 0.02;

            // Pointer attraction / interaction
            const dx = pointer.x - this.x;
            const dy = pointer.y - this.y;
            const dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < 180 && dist > 1) {
                const force = (180 - dist) / 180;
                this.vx += (dx / dist) * force * 1.2;
                this.vy += (dy / dist) * force * 1.2;
            }

            // Shockwave repulsion
            if (shockwave.active) {
                const swDx = this.x - shockwave.x;
                const swDy = this.y - shockwave.y;
                const swDist = Math.sqrt(swDx * swDx + swDy * swDy);
                const diff = Math.abs(swDist - shockwave.radius);

                if (diff < 35 && swDist > 0) {
                    this.vx += (swDx / swDist) * 8;
                    this.vy += (swDy / swDist) * 8;
                }
            }

            // Apply friction
            this.vx *= 0.88;
            this.vy *= 0.88;

            this.x += this.vx + this.driftX;
            this.y += this.vy + this.driftY;
        }

        draw(accentColor) {
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(${accentColor}, ${this.alpha})`;
            ctx.fill();
        }
    }

    for (let i = 0; i < PARTICLE_COUNT; i++) {
        particles.push(new Particle());
    }

    window.addEventListener('pointermove', (e) => {
        const rect = canvas.getBoundingClientRect();
        pointer.targetX = e.clientX - rect.left;
        pointer.targetY = e.clientY - rect.top;
        pointer.isHovered = true;
    }, { passive: true });

    window.addEventListener('pointerdown', (e) => {
        const rect = canvas.getBoundingClientRect();
        shockwave.x = e.clientX - rect.left;
        shockwave.y = e.clientY - rect.top;
        shockwave.radius = 10;
        shockwave.active = true;
        soundEngine.playTick(800, 'sine', 0.04, 0.03);
    }, { passive: true });

    function getAccentRgb() {
        const style = getComputedStyle(document.documentElement);
        return style.getPropertyValue('--accent-rgb').trim() || '56, 189, 248';
    }

    function animate() {
        if (isVisible) {
            ctx.clearRect(0, 0, width, height);

            // Interpolate pointer
            pointer.x += (pointer.targetX - pointer.x) * 0.1;
            pointer.y += (pointer.targetY - pointer.y) * 0.1;

            // Center orb pulse
            centerOrb.pulseVal += centerOrb.pulseSpeed;
            centerOrb.currentRadius = centerOrb.baseRadius + Math.sin(centerOrb.pulseVal) * 8;

            // Dynamically adjust center position based on window aspect ratio
            centerOrb.x = width > 900 ? width * 0.72 : width * 0.5;
            centerOrb.y = width > 900 ? height * 0.44 : height * 0.38;

            const accentColor = getAccentRgb();

            // Render Center Core Glow
            const coreGradient = ctx.createRadialGradient(
                centerOrb.x, centerOrb.y, 0,
                centerOrb.x, centerOrb.y, centerOrb.currentRadius * 2
            );
            coreGradient.addColorStop(0, `rgba(${accentColor}, 0.22)`);
            coreGradient.addColorStop(0.5, `rgba(${accentColor}, 0.06)`);
            coreGradient.addColorStop(1, 'transparent');

            ctx.beginPath();
            ctx.arc(centerOrb.x, centerOrb.y, centerOrb.currentRadius * 2, 0, Math.PI * 2);
            ctx.fillStyle = coreGradient;
            ctx.fill();

            // Render Orbital Rings
            ctx.beginPath();
            ctx.arc(centerOrb.x, centerOrb.y, centerOrb.currentRadius * 1.4, 0, Math.PI * 2);
            ctx.strokeStyle = `rgba(${accentColor}, 0.12)`;
            ctx.lineWidth = 1;
            ctx.setLineDash([4, 8]);
            ctx.stroke();
            ctx.setLineDash([]);

            // Draw Constellation Lines between close particles
            for (let i = 0; i < particles.length; i++) {
                particles[i].update();
                particles[i].draw(accentColor);

                for (let j = i + 1; j < particles.length; j++) {
                    const dx = particles[i].x - particles[j].x;
                    const dy = particles[i].y - particles[j].y;
                    const dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < 110) {
                        ctx.beginPath();
                        ctx.moveTo(particles[i].x, particles[i].y);
                        ctx.lineTo(particles[j].x, particles[j].y);
                        ctx.strokeStyle = `rgba(${accentColor}, ${(1 - dist / 110) * 0.16})`;
                        ctx.lineWidth = 0.8;
                        ctx.stroke();
                    }
                }
            }

            // Shockwave expansion
            if (shockwave.active) {
                shockwave.radius += shockwave.speed;
                const waveAlpha = Math.max(0, 1 - shockwave.radius / shockwave.maxRadius) * 0.35;

                ctx.beginPath();
                ctx.arc(shockwave.x, shockwave.y, shockwave.radius, 0, Math.PI * 2);
                ctx.strokeStyle = `rgba(${accentColor}, ${waveAlpha})`;
                ctx.lineWidth = 2;
                ctx.stroke();

                if (shockwave.radius >= shockwave.maxRadius) {
                    shockwave.active = false;
                }
            }
        }

        requestAnimationFrame(animate);
    }
    requestAnimationFrame(animate);
}

/* ─── 7. HERO TYPEWRITER EFFECT ─── */
function initHeroTypewriter() {
    const typeWord = document.getElementById('typeWord');
    if (!typeWord || prefersReducedMotion) return;

    const phrases = [
        'move',
        'learn',
        'reason',
        'respond',
        'scale',
        'think'
    ];

    let phraseIndex = 0;
    let charIndex = phrases[0].length;
    let isDeleting = true;

    function tick() {
        const current = phrases[phraseIndex];

        if (isDeleting) {
            typeWord.textContent = current.substring(0, charIndex);
            charIndex--;

            if (charIndex < 0) {
                isDeleting = false;
                phraseIndex = (phraseIndex + 1) % phrases.length;
                setTimeout(tick, 220);
                return;
            }
            setTimeout(tick, 45);
        } else {
            typeWord.textContent = current.substring(0, charIndex);
            charIndex++;

            if (charIndex > current.length) {
                isDeleting = true;
                setTimeout(tick, 1400);
                return;
            }
            setTimeout(tick, 75);
        }
    }

    setTimeout(tick, 1000);
}

/* ─── 8. 3D CARD TILT & CURSOR SPOTLIGHT ─── */
function initTiltAndSpotlight() {
    if (!canHover || prefersReducedMotion) return;

    const cards = document.querySelectorAll('[data-tilt]');

    cards.forEach((card) => {
        let bounds = null;

        function updateBounds() {
            bounds = card.getBoundingClientRect();
        }

        card.addEventListener('mouseenter', () => {
            updateBounds();
        });

        card.addEventListener('mousemove', (e) => {
            if (!bounds) updateBounds();

            const x = e.clientX - bounds.left;
            const y = e.clientY - bounds.top;

            // Set spotlight CSS variables
            card.style.setProperty('--mouse-x', `${x}px`);
            card.style.setProperty('--mouse-y', `${y}px`);

            // Subtle 3D tilt calculation (-5 to +5 degrees)
            const centerX = bounds.width / 2;
            const centerY = bounds.height / 2;
            const rotateX = ((y - centerY) / centerY) * -5;
            const rotateY = ((x - centerX) / centerX) * 5;

            card.style.transform = `perspective(1000px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) scale3d(1.015, 1.015, 1.015)`;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
        });
    });
}

/* ─── 9. PROJECT BENTO FILTERING ─── */
function initProjectFilters() {
    const filterTabs = document.querySelectorAll('.filter-tab');
    const projectCards = document.querySelectorAll('.bento-card');

    filterTabs.forEach((tab) => {
        tab.addEventListener('click', () => {
            const filter = tab.getAttribute('data-filter');

            filterTabs.forEach((t) => {
                t.classList.remove('is-active');
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add('is-active');
            tab.setAttribute('aria-selected', 'true');

            soundEngine.playTick(1800, 'sine', 0.03, 0.04);

            projectCards.forEach((card) => {
                const category = card.getAttribute('data-category');
                const match = filter === 'all' || category === filter;

                if (match) {
                    card.style.display = '';
                    setTimeout(() => {
                        card.style.opacity = '1';
                        card.style.transform = 'scale(1)';
                    }, 20);
                } else {
                    card.style.opacity = '0';
                    card.style.transform = 'scale(0.96)';
                    setTimeout(() => {
                        card.style.display = 'none';
                    }, 250);
                }
            });
        });
    });
}

/* ─── 10. INTERACTIVE DEVELOPER TERMINAL ─── */
function initTerminal() {
    const terminalForm = document.getElementById('terminalForm');
    const terminalInput = document.getElementById('terminalInput');
    const terminalOutput = document.getElementById('terminalOutput');
    const quickCmdBtns = document.querySelectorAll('.term-quick-cmd');

    if (!terminalForm || !terminalInput || !terminalOutput) return;

    const commandResponses = {
        help: `Available commands:
  • about     - Who is Ryann Chandiari?
  • skills    - High-level technical architecture & tooling
  • projects  - Shipped software & case studies
  • contact   - Direct channels, email & socials
  • theme     - Cycle accent color theme
  • sfx       - Toggle Web Audio synthesizer
  • clear     - Clear terminal buffer
  • nest      - Inspect Nest (macOS AI Finder companion)
  • pip       - Inspect Pip (macOS Multimodal Vision agent)
  • bali      - Inspect Beauty of Bali platform
  • tokopedia - Inspect NLP Sentiment Classifier
  • cardcast  - Inspect Card Cast! Unity game
  • tamanbaca - Inspect Pojok Baca Android app`,

        about: `RYANN CHANDIARI // PROFILE DOSSIER
CS Student based in Jakarta, Indonesia (UTC+7).
Obsessed with native macOS craftsmanship, agentic AI systems that can execute real tools, and tactile creative web experiences.
GitHub: @HyperOrb`,

        skills: `STACK ARCHITECTURE:
• macOS Native: Swift, SwiftUI, AppKit, macOS Automation, Accessibility APIs, Daemons
• AI & ML: Gemini API, OpenAI APIs, Ollama, Python, SVM, Scikit-learn, Screen Vision
• Creative Web: React, Vite, Tailwind CSS, GSAP, WebGL/Canvas, High-precision CSS
• Mobile & Systems: Android (Jetpack Compose), Firebase Firestore, Unity Engine (C#)`,

        projects: `SHIPPED SYSTEMS & CASE STUDIES:
[01] Nest        - Finder-native macOS AI companion (Swift / AppKit / Ollama)
[02] Pip         - macOS Agentic vision companion (Local Multimodal AI)
[03] Beauty Bali - Travel & cultural platform (React / Vite / Tailwind)
[04] Tokopedia   - Sentiment analysis classification pipeline (Python / SVM)
[05] Card Cast!  - Tower defense deckbuilder systems (Unity / C#)
[06] Pojok Baca  - Community library with ML review rating (Android / Compose / Firebase)`,

        contact: `DIRECT CHANNELS:
• Email:    ryann.chandiari@gmail.com
• GitHub:   https://github.com/HyperOrb
• LinkedIn: https://www.linkedin.com/in/rynnchan
• IG:       https://www.instagram.com/rynnchn_/`,

        nest: `NEST // MACOS SYSTEM
Finder-native AI assistant bringing safe LLM actions into macOS file operations.
Stack: Swift, AppKit, SwiftUI, Ollama.
Visit: nest.html for full case study.`,

        pip: `PIP // AGENTIC VISION COMPANION
Menu bar AI companion that perceives screen context and automates UI tasks.
Stack: Native macOS, Local Multimodal AI, UI Automation.
Visit: pip.html for full case study.`,

        bali: `BEAUTY OF BALI // CULTURE & TRAVEL PLATFORM
Modern web platform with clean REST architecture and high-performance UI.
Stack: React, Vite, Tailwind CSS, REST API.
Visit: beautyofbali.html.`,

        tokopedia: `TOKOPEDIA SENTIMENT ANALYSIS // NLP
Machine learning model classifying ecommerce reviews with SVM.
Stack: Python, Scikit-learn, NLP text preprocessing.
Visit: tokopedia.html.`,

        cardcast: `CARD CAST! // GAME ARCHITECTURE
Stylized low-poly card-based tower defense built with Unity and C#.
Visit: cardcast.html.`,

        tamanbaca: `POJOK BACA // ANDROID COMMUNITY APP
Library application with Firebase sync and sentiment rating engine.
Stack: Kotlin, Jetpack Compose, Firebase Firestore.
Visit: tamanbaca.html.`
    };

    function appendOutput(userCmd, responseText) {
        const entry = document.createElement('div');
        entry.className = 'term-log-entry';

        entry.innerHTML = `
            <div class="term-user-line">
                <span class="term-prompt-inline">guest@hyperorb:~$</span>
                <span>${userCmd}</span>
            </div>
            <div class="term-response-text"><pre>${responseText}</pre></div>
        `;

        terminalOutput.appendChild(entry);
        terminalOutput.scrollTop = terminalOutput.scrollHeight;
    }

    function executeCommand(rawCmd) {
        const cmd = rawCmd.trim().toLowerCase();
        if (!cmd) return;

        soundEngine.playTick(1600, 'sine', 0.03, 0.03);

        if (cmd === 'clear') {
            terminalOutput.innerHTML = `
                <div class="term-line term-banner">
                    <p class="term-welcome">TERMINAL BUFFER CLEARED · TYPE 'help' FOR COMMAND LIST</p>
                </div>
            `;
            return;
        }

        if (cmd === 'theme') {
            const cycle = initThemeSwitcher();
            cycle();
            appendOutput(cmd, 'Theme accent color cycled successfully.');
            return;
        }

        if (cmd === 'sfx') {
            const isMuted = soundEngine.toggle();
            initSfxToggle();
            appendOutput(cmd, `Web Audio Synthesizer: ${isMuted ? 'MUTED' : 'ENABLED'}`);
            return;
        }

        if (commandResponses[cmd]) {
            appendOutput(cmd, commandResponses[cmd]);
        } else {
            appendOutput(cmd, `command not found: "${cmd}". Type 'help' to see available commands.`);
        }
    }

    terminalForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const value = terminalInput.value;
        terminalInput.value = '';
        executeCommand(value);
    });

    quickCmdBtns.forEach((btn) => {
        btn.addEventListener('click', () => {
            const cmd = btn.getAttribute('data-cmd');
            if (cmd) executeCommand(cmd);
        });
    });
}

/* ─── 11. COMMAND PALETTE (CMD + K) ─── */
function initCommandPalette() {
    const cmdkModal = document.getElementById('cmdkModal');
    const cmdkBackdrop = document.getElementById('cmdkBackdrop');
    const cmdkInput = document.getElementById('cmdkInput');
    const cmdkTrigger = document.getElementById('cmdkTrigger');
    const heroCmdTrigger = document.getElementById('heroCmdTrigger');
    const cmdkResults = document.getElementById('cmdkResults');
    const cmdkCloseBtn = document.getElementById('cmdkCloseBtn');

    if (!cmdkModal || !cmdkInput || !cmdkResults) return;

    let isOpen = false;

    function openCmdk() {
        isOpen = true;
        cmdkModal.classList.add('is-open');
        document.body.classList.add('cmdk-open');
        cmdkInput.value = '';
        filterCmdk('');
        setTimeout(() => cmdkInput.focus(), 50);
        soundEngine.playSuccess();
    }

    function closeCmdk() {
        isOpen = false;
        cmdkModal.classList.remove('is-open');
        document.body.classList.remove('cmdk-open');
    }

    function filterCmdk(query) {
        const clean = query.trim().toLowerCase();
        const items = cmdkResults.querySelectorAll('.cmdk-item');
        let hasMatch = false;

        items.forEach((item) => {
            const text = (item.getAttribute('data-search') || item.textContent).toLowerCase();
            const match = !clean || text.includes(clean);
            item.style.display = match ? 'flex' : 'none';
            if (match) hasMatch = true;
        });
    }

    cmdkInput.addEventListener('input', (e) => {
        filterCmdk(e.target.value);
    });

    // Close on clicking backdrop (the modal wrapper itself)
    cmdkModal.addEventListener('click', (e) => {
        if (e.target === cmdkModal) {
            closeCmdk();
        }
    });

    if (cmdkCloseBtn) {
        cmdkCloseBtn.addEventListener('click', closeCmdk);
    }

    // Keyboard shortcut Cmd+K or Ctrl+K or Escape
    window.addEventListener('keydown', (e) => {
        if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
            e.preventDefault();
            if (isOpen) closeCmdk();
            else openCmdk();
        } else if (e.key === 'Escape' && isOpen) {
            closeCmdk();
        }
    });

    if (cmdkTrigger) cmdkTrigger.addEventListener('click', openCmdk);
    if (heroCmdTrigger) heroCmdTrigger.addEventListener('click', openCmdk);

    // Actions inside cmdk
    cmdkResults.addEventListener('click', (e) => {
        const item = e.target.closest('.cmdk-item');
        if (!item) return;

        const action = item.getAttribute('data-action');
        if (action === 'copy-email') {
            navigator.clipboard.writeText('ryann.chandiari@gmail.com');
            const statusEl = document.getElementById('cmdkEmailStatus');
            if (statusEl) statusEl.textContent = '✓ Copied!';
            soundEngine.playSuccess();
            setTimeout(closeCmdk, 600);
        } else if (action === 'cycle-theme') {
            const cycle = initThemeSwitcher();
            cycle();
            closeCmdk();
        } else if (action === 'toggle-sfx') {
            soundEngine.toggle();
            initSfxToggle();
            closeCmdk();
        } else {
            closeCmdk();
        }
    });
}

/* ─── 12. COPY EMAIL INTERACTION WITH HAPTIC FEEDBACK ─── */
function initCopyEmail() {
    const copyBtn = document.getElementById('copyEmailBtn');
    const copyText = document.getElementById('copyBtnText');
    if (!copyBtn) return;

    copyBtn.addEventListener('click', () => {
        navigator.clipboard.writeText('ryann.chandiari@gmail.com').then(() => {
            if (copyText) copyText.textContent = '✓ Copied!';
            soundEngine.playSuccess();
            copyBtn.style.background = 'var(--accent-primary)';
            copyBtn.style.color = '#000';

            setTimeout(() => {
                if (copyText) copyText.textContent = 'Copy Email';
                copyBtn.style.background = '';
                copyBtn.style.color = '';
            }, 2000);
        }).catch(() => {
            window.location.href = 'mailto:ryann.chandiari@gmail.com';
        });
    });
}

/* ─── 13. MOBILE DRAWER NAVIGATION ─── */
function initMobileDrawer() {
    const menuToggle = document.getElementById('menuToggle');
    const drawerClose = document.getElementById('drawerClose');
    const drawerBackdrop = document.getElementById('drawerBackdrop');
    const menuBackdrop = document.getElementById('menuBackdrop');
    const drawerLinks = document.querySelectorAll('.drawer-link, .sidebar-link');

    function toggleMenu() {
        const isOpen = document.body.classList.toggle('menu-open');
        soundEngine.playTick(1600, 'sine', 0.03, 0.03);
        if (menuToggle) {
            menuToggle.setAttribute('aria-expanded', String(isOpen));
        }
    }

    function closeMenu() {
        document.body.classList.remove('menu-open');
        if (menuToggle) {
            menuToggle.setAttribute('aria-expanded', 'false');
        }
    }

    if (menuToggle) menuToggle.addEventListener('click', toggleMenu);
    if (drawerClose) drawerClose.addEventListener('click', closeMenu);
    if (drawerBackdrop) drawerBackdrop.addEventListener('click', closeMenu);
    if (menuBackdrop) menuBackdrop.addEventListener('click', closeMenu);

    drawerLinks.forEach((link) => {
        link.addEventListener('click', closeMenu);
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && document.body.classList.contains('menu-open')) {
            closeMenu();
        }
    });
}

/* ─── 14. GSAP SCROLLTRIGGER & KINETIC WORD REVEALS ─── */
function initScrollMotion() {
    if (prefersReducedMotion || typeof gsap === 'undefined' || typeof ScrollTrigger === 'undefined') return;

    // Kinetic typography statement word highlight
    const statement = document.getElementById('kineticStatement');
    if (statement) {
        const words = statement.querySelectorAll('.kinetic-word');

        words.forEach((word, index) => {
            ScrollTrigger.create({
                trigger: word,
                start: 'top 82%',
                onEnter: () => word.classList.add('is-revealed'),
                onLeaveBack: () => word.classList.remove('is-revealed')
            });
        });
    }

    // Metric and Bento card scroll reveals
    gsap.utils.toArray('.metric-card, .bento-card, .arsenal-bay, .tenet-card, .social-glass-card').forEach((el) => {
        gsap.from(el, {
            scrollTrigger: {
                trigger: el,
                start: 'top 88%'
            },
            y: 36,
            opacity: 0,
            duration: 0.75,
            ease: 'power3.out'
        });
    });

    // Back to top button
    const backToTopBtn = document.getElementById('backToTopBtn');
    if (backToTopBtn) {
        backToTopBtn.addEventListener('click', () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
            soundEngine.playTick(1800, 'sine', 0.04, 0.04);
        });
    }
}

/* ─── 15. CINEMATIC INTRO LOADER SEQUENCE ─── */
function playIntroLoader() {
    const loader = document.getElementById('introLoader');
    const introCount = document.getElementById('introCount');

    if (!loader || prefersReducedMotion) {
        document.body.classList.remove('intro-running');
        if (loader) loader.remove();
        initScrollMotion();
        return;
    }

    const counter = { val: 0 };

    if (typeof gsap !== 'undefined') {
        const tl = gsap.timeline({
            defaults: { ease: 'power3.out' },
            onComplete: () => {
                document.body.classList.remove('intro-running');
                loader.remove();
                initScrollMotion();
                soundEngine.playSuccess();
            }
        });

        tl.to('.intro-loader__line', {
            scaleX: 1,
            duration: 0.95,
            ease: 'power2.inOut'
        })
        .to(counter, {
            val: 100,
            duration: 1.05,
            ease: 'power2.inOut',
            onUpdate: () => {
                if (introCount) {
                    introCount.textContent = String(Math.round(counter.val)).padStart(2, '0');
                }
            }
        }, '-=0.95')
        .to('.intro-loader__scan', {
            x: '140vw',
            duration: 1.1,
            ease: 'power2.inOut'
        }, '-=0.85')
        .to('.intro-loader', {
            yPercent: -100,
            duration: 0.8,
            ease: 'power4.inOut'
        }, '+=0.05');
    } else {
        // Fallback without GSAP
        document.body.classList.remove('intro-running');
        loader.remove();
    }
}

/* ─── INITIALIZATION LIFECYCLE ─── */
window.addEventListener('DOMContentLoaded', () => {
    initLiveClock();
    initThemeSwitcher();
    initSfxToggle();
    initCustomCursor();
    initLivingCanvas();
    initHeroTypewriter();
    initTiltAndSpotlight();
    initProjectFilters();
    initTerminal();
    initCommandPalette();
    initCopyEmail();
    initMobileDrawer();

    playIntroLoader();
});

window.addEventListener('load', () => {
    if (typeof ScrollTrigger !== 'undefined') {
        ScrollTrigger.refresh();
    }
});
