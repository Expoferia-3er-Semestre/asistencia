// ── Navbar scroll
const navbar = document.getElementById("navbar");
window.addEventListener("scroll", () => {
  navbar.classList.toggle("scrolled", window.scrollY > 50);
});

// ── Hamburger
const navToggle = document.getElementById("navToggle");
const navLinks = document.getElementById("navLinks");
navToggle.addEventListener("click", () => navLinks.classList.toggle("open"));
navLinks
  .querySelectorAll("a")
  .forEach((a) =>
    a.addEventListener("click", () => navLinks.classList.remove("open")),
  );

// ── Hero Slider
const quotes = [
  '"Educación con valores, formando el futuro de nuestra comunidad."',
  '"La disciplina es el puente entre metas y logros."',
  '"Nuestros estudiantes son el orgullo de la Parroquia San Francisco."',
  '"Unidos construimos una comunidad educativa de excelencia."',
];

const slides = document.querySelectorAll(".hero-slide");
const dotsWrap = document.getElementById("heroDots");
const quoteEl = document.getElementById("heroQuote");
let heroIdx = 0;
let heroTimer;

slides.forEach((_, i) => {
  const d = document.createElement("button");
  d.className = "hero-dot" + (i === 0 ? " active" : "");
  d.addEventListener("click", () => goToHero(i));
  dotsWrap.appendChild(d);
});

function goToHero(idx) {
  slides[heroIdx].classList.remove("active");
  dotsWrap.children[heroIdx].classList.remove("active");
  heroIdx = (idx + slides.length) % slides.length;
  slides[heroIdx].classList.add("active");
  dotsWrap.children[heroIdx].classList.add("active");
  quoteEl.classList.remove("visible");
  setTimeout(() => {
    quoteEl.textContent = quotes[heroIdx];
    quoteEl.classList.add("visible");
  }, 400);
}

function nextHero() {
  goToHero(heroIdx + 1);
}

function startTimer() {
  heroTimer = setInterval(nextHero, 5000);
}
function resetTimer() {
  clearInterval(heroTimer);
  startTimer();
}

document.getElementById("heroPrev").addEventListener("click", () => {
  goToHero(heroIdx - 1);
  resetTimer();
});
document.getElementById("heroNext").addEventListener("click", () => {
  nextHero();
  resetTimer();
});
startTimer();

// ── Gallery Slider
const galleryTrack = document.getElementById("galleryTrack");
const gallerySlides = galleryTrack.querySelectorAll(".gallery-slide");
let gIdx = 0;

function goToGallery(idx) {
  gIdx = (idx + gallerySlides.length) % gallerySlides.length;
  galleryTrack.style.transform = `translateX(-${gIdx * 100}%)`;
}

document
  .getElementById("galleryPrev")
  .addEventListener("click", () => goToGallery(gIdx - 1));
document
  .getElementById("galleryNext")
  .addEventListener("click", () => goToGallery(gIdx + 1));
setInterval(() => goToGallery(gIdx + 1), 4000);

// ── Scroll animations
const animEls = document.querySelectorAll(".mv-card, .news-card");
const observer = new IntersectionObserver(
  (entries) => {
    entries.forEach((e) => {
      if (e.isIntersecting) {
        e.target.classList.add("visible");
        observer.unobserve(e.target);
      }
    });
  },
  { threshold: 0.15 },
);
animEls.forEach((el) => observer.observe(el));
