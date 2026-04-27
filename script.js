// Initialize Lenis Smooth Scroll
const lenis = new Lenis({
    duration: 1.2,
    easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)), // https://www.desmos.com/calculator/brs54l4xou
    direction: 'vertical',
    gestureDirection: 'vertical',
    smooth: true,
    mouseMultiplier: 1,
    smoothTouch: false,
    touchMultiplier: 2,
    infinite: false,
})

// Native RAF removed to avoid conflict with GSAP ticker

// Sync GSAP with Lenis
gsap.registerPlugin(ScrollTrigger);

ScrollTrigger.config({ ignoreMobileResize: true });

// Update ScrollTrigger on Lenis scroll
lenis.on('scroll', ScrollTrigger.update);

gsap.ticker.add((time) => {
    lenis.raf(time * 1000)
})
gsap.ticker.lagSmoothing(0);

// --- Custom Cursor Logic ---
const cursor = document.querySelector('.cursor');
const hoverElements = document.querySelectorAll('[data-hover], a, button, .project-image-wrapper');
const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
const canHover = window.matchMedia('(hover: hover) and (pointer: fine)').matches;

if (cursor && canHover && !prefersReducedMotion) {
    let mouseX = 0;
    let mouseY = 0;
    let cursorX = 0;
    let cursorY = 0;
    const setCursorX = gsap.quickSetter(cursor, 'x', 'px');
    const setCursorY = gsap.quickSetter(cursor, 'y', 'px');

    document.addEventListener('mousemove', (e) => {
        mouseX = e.clientX;
        mouseY = e.clientY;
    });

    gsap.ticker.add(() => {
        cursorX += (mouseX - cursorX) * 0.2;
        cursorY += (mouseY - cursorY) * 0.2;
        setCursorX(cursorX);
        setCursorY(cursorY);
    });
} else if (cursor) {
    cursor.style.display = 'none';
}

hoverElements.forEach((el) => {
    el.addEventListener('mouseenter', () => {
        cursor.classList.add('hovered');
    });
    el.addEventListener('mouseleave', () => {
        cursor.classList.remove('hovered');
    });
});

// --- Typewriter Effect ---
const titleElement = document.getElementById('hero-title');
const subtitleElement = document.getElementById('hero-subtitle');

const mainPrefix = "Sup, I'm ";
const mainName = "Ryann Chandiari.";
const fullMainText = mainPrefix + mainName;
const subtitlePrefix = "I'm a ";
const rotatingWords = [
    "Computer Science student.",
    "software engineer.",
    "website developer.",
    "tech enthusiast.",
    "problem solver."
];

let mainIndex = 0;
let wordIndex = 0;
let subIndex = 0;
let isDeleting = false;

// 1. Type the main title first
function typeMainTitle() {
    if (mainIndex < fullMainText.length) {
        let typed = fullMainText.substring(0, mainIndex + 1);
        let htmlStr = "";

        if (mainIndex < mainPrefix.length) {
            htmlStr = `<span style="color: #a6a6a6;">${typed}</span>`;
        } else {
            let typedName = typed.substring(mainPrefix.length);
            htmlStr = `<span style="color: #a6a6a6;">${mainPrefix}</span><span style="color: #ffffff;">${typedName}</span>`;
        }

        titleElement.innerHTML = htmlStr + '<span class="cursor-blink">|</span>';
        mainIndex++;
        setTimeout(typeMainTitle, 80 + Math.random() * 40);
    } else {
        // Remove cursor from main title and start subtitle
        titleElement.innerHTML = `<span style="color: #a6a6a6;">${mainPrefix}</span><span style="color: #ffffff;">${mainName}</span>`;
        setTimeout(typeSubtitle, 500);
    }
}

// 2. Loop the subtitle typing
function typeSubtitle() {
    const currentWord = rotatingWords[wordIndex];
    let displayText = subtitlePrefix;

    if (isDeleting) {
        displayText += currentWord.substring(0, subIndex - 1);
        subIndex--;
    } else {
        displayText += currentWord.substring(0, subIndex + 1);
        subIndex++;
    }

    subtitleElement.innerHTML = displayText + '<span class="cursor-blink">|</span>';

    // Typing speed logic
    let typeSpeed = isDeleting ? 40 : 100 + Math.random() * 50;

    if (!isDeleting && subIndex === currentWord.length) {
        // Pause at end of word
        typeSpeed = 1500;
        isDeleting = true;
    } else if (isDeleting && subIndex === 0) {
        isDeleting = false;
        wordIndex = (wordIndex + 1) % rotatingWords.length;
        typeSpeed = 500; // pause before next word
    }

    setTimeout(typeSubtitle, typeSpeed);
}

// --- Page Load & Start Sequence ---
if (titleElement) {
    titleElement.innerHTML = '<span class="cursor-blink">|</span>';
    subtitleElement.innerHTML = '';
}

// Initial states for animation
gsap.set('.nav', { y: -50, opacity: 0 });
gsap.set('.hero-bg-waves', { opacity: 0 });
gsap.set('.scroll-indicator', { y: 20, opacity: 0 });

// Page Load Timeline
const loadTl = gsap.timeline();

loadTl.to('.nav', { y: 0, opacity: 1, duration: 1, ease: 'power3.out', delay: 0.2 })
    .to('.hero-bg-waves', { opacity: 0.6, duration: 1.5, ease: 'power2.out' }, '-=0.5')
    .call(() => {
        // Start typing after initial layout revealed
        if (titleElement) setTimeout(typeMainTitle, 200);
    })
    .to('.scroll-indicator', { y: 0, opacity: 1, duration: 1, ease: 'power3.out' }, '+=0.5');


// --- GSAP Animations ---

// 1. Reveal White Container over Hero
// The hero is sticky, so the white container naturally overlays it.
// We can add a slight tilt or shadow tweak as it comes up natively, but the CSS handles the core overlay.
// Let's add a parallax fade to the hero content.
gsap.to('.hero-content', {
    scrollTrigger: {
        trigger: '.content-reveal',
        start: "top 100%",
        end: "top 20%",
        scrub: true
    },
    y: -100,
    opacity: 0,
    ease: "none"
});

gsap.fromTo('.hero-bg-waves',
    { opacity: 0.6, scale: 1 },
    {
        scrollTrigger: {
            trigger: '.content-reveal',
            start: "top 100%",
            end: "top 0%",
            scrub: true,
            invalidateOnRefresh: true,
            onLeaveBack: () => {
                gsap.to('.hero-bg-waves', {
                    opacity: 0.6,
                    scale: 1,
                    duration: 0.2,
                    overwrite: true
                });
            }
        },
        opacity: 0,
        scale: 0.9,
        ease: "none"
    }
);

// 2. Horizontal Parallax Text inside Project Cards
const projectCards = document.querySelectorAll('.project-card');

projectCards.forEach((card) => {
    const bgText = card.querySelector('.project-bg-text');
    const speed = bgText.getAttribute('data-speed') || 1;

    gsap.to(bgText, {
        scrollTrigger: {
            trigger: card,
            start: 'top bottom',
            end: 'bottom top',
            scrub: true
        },
        x: () => -window.innerWidth * speed * 0.5,
        ease: 'none'
    });

    if (!canHover || prefersReducedMotion) return;

    const imgWrapper = card.querySelector('.project-image-wrapper');
    gsap.set(imgWrapper, { transformPerspective: 1000 });
    const setRotateX = gsap.quickSetter(imgWrapper, 'rotationX', 'deg');
    const setRotateY = gsap.quickSetter(imgWrapper, 'rotationY', 'deg');
    let targetX = 0;
    let targetY = 0;
    let currentX = 0;
    let currentY = 0;
    let isHovering = false;
    let rafId = null;

    const animateTilt = () => {
        currentX += (targetX - currentX) * 0.15;
        currentY += (targetY - currentY) * 0.15;
        setRotateX(currentX);
        setRotateY(currentY);

        const isSettled = Math.abs(targetX - currentX) < 0.05 && Math.abs(targetY - currentY) < 0.05;
        if (isHovering || !isSettled) {
            rafId = requestAnimationFrame(animateTilt);
        } else {
            rafId = null;
        }
    };

    card.addEventListener('mouseenter', () => {
        isHovering = true;
        if (!rafId) {
            rafId = requestAnimationFrame(animateTilt);
        }
    });

    card.addEventListener('mousemove', (e) => {
        const rect = imgWrapper.getBoundingClientRect();
        const x = e.clientX - rect.left - rect.width / 2;
        const y = e.clientY - rect.top - rect.height / 2;
        const multiplier = 20;
        targetX = (-y / (rect.height / 2)) * multiplier;
        targetY = (x / (rect.width / 2)) * multiplier;
    });

    card.addEventListener('mouseleave', () => {
        isHovering = false;
        targetX = 0;
        targetY = 0;
        if (!rafId) {
            rafId = requestAnimationFrame(animateTilt);
        }
    });
});

// Intro text reveal
gsap.from('.intro-text h2', {
    scrollTrigger: {
        trigger: '.intro-text',
        start: "top 80%"
    },
    y: 50,
    opacity: 0,
    duration: 1,
    ease: "power3.out"
});

// Back to top
document.getElementById('backToTop').addEventListener('click', () => {
    lenis.scrollTo(0, { duration: 1.5 });
});

// --- Menu Toggle Logic ---
const menuToggle = document.getElementById('menuToggle');
const menuText = document.getElementById('menuText');
const menuIcon = document.getElementById('menuIcon');
let isMenuOpen = false;

menuToggle.addEventListener('click', () => {
    isMenuOpen = !isMenuOpen;
    document.body.classList.toggle('menu-open', isMenuOpen);

    if (isMenuOpen) {
        menuText.textContent = 'Close';
        lenis.stop(); // Stop scroll when menu is open
        gsap.to(menuIcon, { rotation: 90, duration: 0.3 });
    } else {
        menuText.textContent = 'Menu';
        lenis.start(); // Resume scroll
        gsap.to(menuIcon, { rotation: 0, duration: 0.3 });
    }
});

// --- Smooth Scroll for Sidebar Links ---
const sidebarLinks = document.querySelectorAll('.sidebar-link');
sidebarLinks.forEach(link => {
    link.addEventListener('click', (e) => {
        const targetId = link.getAttribute('href');
        if (targetId && targetId.startsWith('#')) {
            e.preventDefault();

            // Close the menu if it's open
            if (isMenuOpen) {
                menuToggle.click();
            }

            // Wait for the 0.6s CSS transition on #pageWrapper to finish 
            // so Lenis can calculate the correct vertical position
            setTimeout(() => {
                if (targetId === '#') {
                    lenis.scrollTo(0, { duration: 1.5 });
                } else {
                    const targetEl = document.querySelector(targetId);
                    if (targetEl) {
                        lenis.scrollTo(targetEl, { duration: 1.5, offset: -50 });
                    }
                }
            }, 650);
        }
    });
});
