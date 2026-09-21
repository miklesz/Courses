const deckId = new URLSearchParams(location.search).get('deck');
if (!deckId) location.replace('index.html');
const script = document.createElement('script');
script.src = `decks/${deckId}.js`;
script.onload = () => {
  const deck = window.PT_DECK;
  let current = 0;
  const image = document.querySelector('#slide');
  const title = document.querySelector('#title');
  const counter = document.querySelector('#counter');
  const media = document.querySelector('#media');
  const render = () => {
    const slide = deck.slides[current];
    document.title = `${deck.title} - ${current + 1}/${deck.slides.length}`;
    title.textContent = deck.title;
    image.src = slide.image;
    image.alt = slide.heading;
    counter.textContent = `${current + 1} / ${deck.slides.length}`;
    media.replaceChildren(...slide.media.map((url, index) => {
      const link = document.createElement('a');
      link.href = url;
      link.textContent = `Materiał ${index + 1}`;
      return link;
    }));
  };
  const move = (delta) => { current = Math.max(0, Math.min(deck.slides.length - 1, current + delta)); render(); };
  document.querySelector('#previous').addEventListener('click', () => move(-1));
  document.querySelector('#next').addEventListener('click', () => move(1));
  addEventListener('keydown', (event) => { if (event.key === 'ArrowLeft') move(-1); if (event.key === 'ArrowRight' || event.key === ' ') move(1); });
  render();
};
document.head.append(script);
