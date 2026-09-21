#!/usr/bin/env node

import { execFileSync } from 'node:child_process';
import { cpSync, existsSync, mkdirSync, readdirSync, readFileSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { basename, dirname, extname, join, relative } from 'node:path';

const root = dirname(dirname(new URL(import.meta.url).pathname));
const sourceDir = join(root, 'Podstawy Telekomunikacji');
const outputDir = join(sourceDir, 'web');
const notesDir = join(sourceDir, 'notes');
const buildDir = join(root, '.build', 'pt-web');
const requestedDecks = new Set(process.argv.slice(2));
const allPresentations = readdirSync(sourceDir)
  .filter((name) => name.endsWith('.pptx'))
  .sort((a, b) => a.localeCompare(b, 'pl'));
const presentations = allPresentations
  .filter((name) => requestedDecks.size === 0 || requestedDecks.has(name.slice(0, 2)))
  ;

const run = (command, args) => execFileSync(command, args, { stdio: 'inherit' });
const xmlText = (xml) => [...xml.matchAll(/<a:t>([\s\S]*?)<\/a:t>/g)]
  .map((match) => match[1]
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&#(\d+);/g, (_, code) => String.fromCodePoint(Number(code)))
    .trim())
  .filter(Boolean);

const slug = (value) => value
  .normalize('NFKD')
  .replace(/[\u0300-\u036f]/g, '')
  .replace(/[^a-zA-Z0-9]+/g, '-')
  .replace(/(^-|-$)/g, '')
  .toLowerCase();

const exportPdf = (input, outputDir) => {
  run('soffice', ['--headless', '--convert-to', 'pdf', '--outdir', outputDir, input]);
};

const copyMedia = (deckDir, targetDir, slideNumber, relations) => {
  const references = [...relations.matchAll(/Id="([^"]+)"[^>]*Target="\.\.\/media\/([^"]+)"/g)]
    .map((match) => ({ id: match[1], name: match[2] }));
  const slideXml = readFileSync(join(deckDir, 'ppt', 'slides', `slide${slideNumber}.xml`), 'utf8');
  const ids = new Set([...slideXml.matchAll(/r:(?:embed|link)="([^"]+)"/g)].map((match) => match[1]));
  const media = [];

  for (const reference of references.filter((item) => ids.has(item.id))) {
    const source = join(deckDir, 'ppt', 'media', reference.name);
    const extension = extname(source).toLowerCase();
    if (!existsSync(source) || !['.mp4', '.m4v', '.mov', '.webm', '.ts', '.mp3', '.wav', '.m4a'].includes(extension)) continue;

    const base = `slide-${String(slideNumber).padStart(3, '0')}-${slug(basename(reference.name, extension))}`;
    const target = extension === '.ts' ? join(targetDir, `${base}.mp4`) : join(targetDir, `${base}${extension}`);
    if (extension === '.ts') {
      run('ffmpeg', ['-y', '-i', source, '-c:v', 'libx264', '-c:a', 'aac', '-movflags', '+faststart', target]);
    } else {
      cpSync(source, target);
    }
    media.push(relative(outputDir, target));
  }
  return media;
};

rmSync(buildDir, { recursive: true, force: true });
mkdirSync(buildDir, { recursive: true });
mkdirSync(outputDir, { recursive: true });
mkdirSync(notesDir, { recursive: true });

const convertedCatalog = [];
for (const presentation of presentations) {
  const deckId = presentation.slice(0, 2);
  const title = presentation.replace(/^\d+\s+/, '').replace(/\.pptx$/, '');
  const deckBuild = join(buildDir, deckId);
  const pdf = join(deckBuild, presentation.replace(/\.pptx$/, '.pdf'));
  const raster = join(deckBuild, 'raster');
  const assets = join(outputDir, 'assets', deckId);
  const mediaDir = join(outputDir, 'media', deckId);
  const deckScript = join(outputDir, 'decks', `${deckId}.js`);
  const notePath = join(notesDir, `${deckId}-${slug(title)}.md`);

  console.log(`\n${presentation}`);
  mkdirSync(deckBuild, { recursive: true });
  run('unzip', ['-q', join(sourceDir, presentation), '-d', deckBuild]);
  exportPdf(join(sourceDir, presentation), deckBuild);
  mkdirSync(raster, { recursive: true });
  run('pdftoppm', ['-r', '105', '-png', pdf, join(raster, 'slide')]);
  mkdirSync(assets, { recursive: true });
  mkdirSync(mediaDir, { recursive: true });
  mkdirSync(dirname(deckScript), { recursive: true });

  const slideXmls = readdirSync(join(deckBuild, 'ppt', 'slides'))
    .filter((name) => /^slide\d+\.xml$/.test(name))
    .sort((a, b) => Number(a.match(/\d+/)[0]) - Number(b.match(/\d+/)[0]));
  const rasterPngs = readdirSync(raster)
    .filter((name) => /-\d+\.png$/.test(name))
    .sort((a, b) => Number(a.match(/-(\d+)\.png$/)[1]) - Number(b.match(/-(\d+)\.png$/)[1]));
  const slides = [];
  const markdown = [`# ${title}`, '', `Oryginał: [${presentation}](../${encodeURIComponent(presentation)})`, '', '## Slajdy', ''];

  for (const [ordinal, png] of rasterPngs.entries()) {
    const file = slideXmls[ordinal];
    const number = ordinal + 1;
    const sourceSlideNumber = Number(file.match(/\d+/)[0]);
    const image = join(assets, `slide-${String(number).padStart(3, '0')}.webp`);
    run('cwebp', ['-quiet', '-q', '82', '-resize', '1400', '0', join(raster, png), '-o', image]);

    const slideXml = readFileSync(join(deckBuild, 'ppt', 'slides', file), 'utf8');
    const relationsPath = join(deckBuild, 'ppt', 'slides', '_rels', `${file}.rels`);
    const relations = existsSync(relationsPath) ? readFileSync(relationsPath, 'utf8') : '';
    const text = xmlText(slideXml);
    const media = copyMedia(deckBuild, mediaDir, sourceSlideNumber, relations);
    const imageRelative = relative(dirname(notePath), image).split('\\').join('/');
    const mediaRelative = media.map((entry) => relative(dirname(notePath), join(outputDir, entry)).split('\\').join('/'));
    const heading = text[0] || `Slajd ${number}`;
    markdown.push(`### ${number}. ${heading}`, '', `![${heading}](${imageRelative})`, '');
    if (text.length > 1) markdown.push(text.slice(1).join('  \n'), '');
    for (const item of mediaRelative) markdown.push(`[Odtwórz materiał z tego slajdu](${item})`, '');
    slides.push({
      number,
      image: `assets/${deckId}/${basename(image)}`,
      heading,
      media: media.map((entry) => entry.split('\\').join('/')),
    });
  }

  writeFileSync(notePath, `${markdown.join('\n')}\n`);
  writeFileSync(deckScript, `window.PT_DECK = ${JSON.stringify({ id: deckId, title, slides }, null, 2)};\n`);
  convertedCatalog.push({ id: deckId, title, note: relative(outputDir, notePath).split('\\').join('/'), source: presentation, count: slides.length });
}

const catalog = existsSync(join(outputDir, 'decks'))
  ? readdirSync(join(outputDir, 'decks')).filter((name) => name.endsWith('.js')).map((name) => {
    const deck = JSON.parse(readFileSync(join(outputDir, 'decks', name), 'utf8').replace(/^window\.PT_DECK = /, '').replace(/;\s*$/, ''));
    const source = allPresentations.find((item) => item.slice(0, 2) === deck.id) ?? `${deck.id} ${deck.title}.pptx`;
    const note = readdirSync(notesDir).find((item) => item.startsWith(`${deck.id}-`));
    return { id: deck.id, title: deck.title, note: `../notes/${note}`, source, count: deck.slides.length };
  }).sort((a, b) => a.id.localeCompare(b.id))
  : convertedCatalog;
writeFileSync(join(outputDir, 'catalog.js'), `window.PT_CATALOG = ${JSON.stringify(catalog, null, 2)};\n`);
console.log('\nPT web conversion complete.');
