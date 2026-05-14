gsap.registerPlugin(ScrollTrigger);

ScrollTrigger.config({ ignoreMobileResize: true });

const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const canHover = window.matchMedia('(hover: hover) and (pointer: fine)').matches;
const cursor = document.querySelector('.cursor');
const hoverElements = document.querySelectorAll('[data-hover], a, button, .project-image-wrapper');
const heroBackground = document.querySelector('.hero-bg-waves');
const typedWord = document.getElementById('typedWord');
const heroSubtitle = document.getElementById('hero-subtitle');
const introLoader = document.getElementById('introLoader');
const introCount = document.getElementById('introCount');

if (cursor && canHover && !prefersReducedMotion) {
    let mouseX = 0;
    let mouseY = 0;
    let cursorX = 0;
    let cursorY = 0;
    const setCursorX = gsap.quickSetter(cursor, 'x', 'px');
    const setCursorY = gsap.quickSetter(cursor, 'y', 'px');

    document.addEventListener('mousemove', (event) => {
        mouseX = event.clientX;
        mouseY = event.clientY;
    });

    gsap.ticker.add(() => {
        cursorX += (mouseX - cursorX) * 0.18;
        cursorY += (mouseY - cursorY) * 0.18;
        setCursorX(cursorX);
        setCursorY(cursorY);
    });

    hoverElements.forEach((element) => {
        element.addEventListener('mouseenter', () => cursor.classList.add('hovered'));
        element.addEventListener('mouseleave', () => cursor.classList.remove('hovered'));
    });
} else if (cursor) {
    cursor.style.display = 'none';
}

if (heroBackground && canHover && !prefersReducedMotion) {
    let glowX = window.innerWidth * 0.7;
    let glowY = window.innerHeight * 0.48;
    let targetGlowX = glowX;
    let targetGlowY = glowY;

    document.addEventListener('pointermove', (event) => {
        targetGlowX = event.clientX;
        targetGlowY = event.clientY;
    }, { passive: true });

    gsap.ticker.add(() => {
        glowX += (targetGlowX - glowX) * 0.12;
        glowY += (targetGlowY - glowY) * 0.12;
        heroBackground.style.setProperty('--pointer-x', `${(glowX / window.innerWidth) * 100}%`);
        heroBackground.style.setProperty('--pointer-y', `${(glowY / window.innerHeight) * 100}%`);
    });
}

if (typedWord) {
    const typedWords = [
        { text: 'move', color: '#458dff' },
        { text: 'learn', color: '#66ffb2' },
        { text: 'respond', color: '#79b8ff' },
        { text: 'connect', color: '#c9f6ff' }
    ];

    if (prefersReducedMotion) {
        typedWord.textContent = typedWords[0].text;
    } else {
        let wordIndex = 0;
        let characterIndex = typedWords[0].text.length;
        let isDeleting = true;

        const typeNextFrame = () => {
            const currentWord = typedWords[wordIndex];
            typedWord.style.color = currentWord.color;
            typedWord.textContent = currentWord.text.slice(0, characterIndex);

            if (heroSubtitle) {
                heroSubtitle.setAttribute(
                    'aria-label',
                    `Computer Science student building digital experiences that ${currentWord.text}.`
                );
            }

            if (!isDeleting && characterIndex === currentWord.text.length) {
                isDeleting = true;
                window.setTimeout(typeNextFrame, 1180);
                return;
            }

            if (isDeleting && characterIndex === 0) {
                isDeleting = false;
                wordIndex = (wordIndex + 1) % typedWords.length;
                window.setTimeout(typeNextFrame, 220);
                return;
            }

            characterIndex += isDeleting ? -1 : 1;
            window.setTimeout(typeNextFrame, isDeleting ? 42 : 72);
        };

        window.setTimeout(typeNextFrame, 900);
    }
}

const menuToggle = document.getElementById('menuToggle');
const menuText = document.getElementById('menuText');
const menuIcon = document.getElementById('menuIcon');
const menuBackdrop = document.getElementById('menuBackdrop');
let isMenuOpen = false;

function setMenu(open) {
    isMenuOpen = open;
    document.body.classList.toggle('menu-open', isMenuOpen);

    if (menuToggle) {
        menuToggle.setAttribute('aria-expanded', String(isMenuOpen));
    }

    if (menuText) {
        menuText.textContent = isMenuOpen ? 'Close' : 'Menu';
    }

    if (menuIcon) {
        gsap.to(menuIcon, {
            rotation: isMenuOpen ? 90 : 0,
            duration: 0.3,
            ease: 'power2.out'
        });
    }
}

if (menuToggle) {
    menuToggle.addEventListener('click', () => setMenu(!isMenuOpen));
}

if (menuBackdrop) {
    menuBackdrop.addEventListener('click', () => setMenu(false));
}

document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && isMenuOpen) {
        setMenu(false);
    }
});

function scrollToTarget(targetId) {
    if (targetId === '#') {
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
    }

    const target = document.querySelector(targetId);
    if (!target) return;

    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

document.querySelectorAll('a[href^="#"]').forEach((link) => {
    link.addEventListener('click', (event) => {
        const targetId = link.getAttribute('href');
        if (!targetId) return;

        event.preventDefault();

        if (isMenuOpen) {
            setMenu(false);
            window.setTimeout(() => scrollToTarget(targetId), 420);
            return;
        }

        scrollToTarget(targetId);
    });
});

function startPageMotion() {
    const nav = document.querySelector('.nav');
    const heroIntroTargets = document.querySelectorAll('.hero-content > *, .project-hero-content > *');
    const heroBackgrounds = document.querySelectorAll('.hero-bg-waves, .nest-hero-bg');
    const scrollIndicator = document.querySelector('.scroll-indicator');
    const heroContent = document.querySelector('.hero-content, .project-hero-content');
    const heroGrid = document.querySelector('.hero-grid');

    if (nav) {
        gsap.set(nav, { y: -24, opacity: 0 });
    }

    if (heroIntroTargets.length) {
        gsap.set(heroIntroTargets, { y: 34, opacity: 0 });
    }

    if (heroBackgrounds.length) {
        gsap.set(heroBackgrounds, { opacity: 0 });
    }

    if (scrollIndicator) {
        gsap.set(scrollIndicator, { y: 18, opacity: 0 });
    }

    const loadTl = gsap.timeline({ defaults: { ease: 'power3.out' } });

    if (nav) {
        loadTl.to(nav, { y: 0, opacity: 1, duration: 0.8, delay: 0.1 });
    }

    if (heroBackgrounds.length) {
        loadTl.to(heroBackgrounds, { opacity: 1, duration: 1.25 }, '-=0.55');
    }

    if (heroIntroTargets.length) {
        loadTl.to(heroIntroTargets, { y: 0, opacity: 1, duration: 0.95, stagger: 0.1 }, '-=0.65');
    }

    if (scrollIndicator) {
        loadTl.to(scrollIndicator, { y: 0, opacity: 1, duration: 0.8 }, '-=0.45');
    }

    if (heroContent) {
        gsap.to(heroContent, {
            scrollTrigger: {
                trigger: '.hero',
                start: 'top top',
                end: 'bottom top',
                scrub: true
            },
            y: -80,
            opacity: 0.18,
            ease: 'none'
        });
    }

    if (heroGrid) {
        gsap.to(heroGrid, {
            scrollTrigger: {
                trigger: '.hero',
                start: 'top top',
                end: 'bottom top',
                scrub: true
            },
            yPercent: 18,
            xPercent: -4,
            ease: 'none'
        });
    }

    gsap.utils.toArray('.section-intro, .section-header, .work-card, .about-portrait').forEach((element) => {
        gsap.from(element, {
            scrollTrigger: {
                trigger: element,
                start: 'top 82%'
            },
            y: 52,
            opacity: 0,
            duration: 0.9,
            ease: 'power3.out'
        });
    });

    const aboutSection = document.querySelector('.about-section-new');
    if (aboutSection) {
        const aboutWords = document.querySelectorAll('.about-word');
        gsap.set(aboutWords, { color: '#5b5b5b' });

        gsap.timeline({
            scrollTrigger: {
                trigger: aboutSection,
                start: 'top 58%',
                end: 'bottom 78%',
                scrub: true
            }
        }).to(aboutWords, {
            color: '#ffffff',
            stagger: 0.08,
            ease: 'none'
        });
    }
}

function playIntroLoader() {
    if (!introLoader || prefersReducedMotion) {
        document.body.classList.remove('intro-running');
        if (introLoader) {
            introLoader.remove();
        }
        if (!prefersReducedMotion) {
            startPageMotion();
        }
        return;
    }

    gsap.set('.nav', { y: -24, opacity: 0 });
    gsap.set('.hero-content > *', { y: 34, opacity: 0 });
    gsap.set('.hero-bg-waves', { opacity: 0 });
    gsap.set('.scroll-indicator', { y: 18, opacity: 0 });

    const counter = { value: 0 };
    const introTl = gsap.timeline({
        defaults: { ease: 'power3.out' },
        onComplete: () => {
            document.body.classList.remove('intro-running');
            introLoader.remove();
            startPageMotion();
        }
    });

    gsap.set('.intro-loader__mark', { y: 34, opacity: 0, filter: 'blur(10px)' });
    gsap.set('.intro-loader__line', { scaleX: 0 });
    gsap.set('.intro-loader__top span, .intro-loader__bottom span', { y: 12, opacity: 0 });

    introTl
        .to('.intro-loader__top span, .intro-loader__bottom span', {
            y: 0,
            opacity: 1,
            duration: 0.55,
            stagger: 0.05
        })
        .to('.intro-loader__mark', {
            y: 0,
            opacity: 1,
            filter: 'blur(0px)',
            duration: 0.72
        }, '-=0.28')
        .to('.intro-loader__line', {
            scaleX: 1,
            duration: 0.7
        }, '-=0.46')
        .to('.intro-loader__scan', {
            x: '138vw',
            duration: 1.25,
            ease: 'power2.inOut'
        }, '-=0.62')
        .to(counter, {
            value: 100,
            duration: 1.05,
            ease: 'power2.out',
            onUpdate: () => {
                if (introCount) {
                    introCount.textContent = String(Math.round(counter.value)).padStart(2, '0');
                }
            }
        }, '-=1.05')
        .to('.intro-loader__mark', {
            y: -20,
            opacity: 0,
            filter: 'blur(8px)',
            duration: 0.42,
            ease: 'power2.in'
        }, '+=0.08')
        .to('.intro-loader', {
            yPercent: -100,
            duration: 0.82,
            ease: 'power4.inOut'
        }, '-=0.1');
}

if (!prefersReducedMotion) {
    playIntroLoader();
} else {
    document.body.classList.remove('intro-running');
    if (introLoader) {
        introLoader.remove();
    }
}

window.addEventListener('load', () => {
    ScrollTrigger.refresh();
});
