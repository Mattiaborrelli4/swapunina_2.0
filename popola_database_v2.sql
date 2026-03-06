-- Script SQL corretto per popolare SwapUnina
-- Basato sulla struttura reale delle tabelle

-- =================================================================
-- VERIFICA CATEGORIE ESISTENTI
-- =================================================================

-- Assicuriamoci che le categorie esistano
INSERT INTO categoria (nome) VALUES ('LIBRI') ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('ELETTRONICA') ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('ABBIGLIAMENTO') ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('SPORT') ON CONFLICT (nome) DO NOTHING;
INSERT INTO categoria (nome) VALUES ('ALTRO') ON CONFLICT (nome) DO NOTHING;

-- =================================================================
-- 1. INSERISCI OGGETTI (struttura corretta senza anno_acquisto)
-- =================================================================

INSERT INTO oggetto (nome, descrizione, image_url, categoria_id, origine) VALUES
-- Libri (categoria_id deve essere impostato correttamente)
('Calcolo Differenziale Adams', 'Libro di analisi matematica, edizione 2023', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400',
 (SELECT id FROM categoria WHERE nome='LIBRI' LIMIT 1), 'USATO'),

('Fisica Generale Mencuccini', 'Corso completo di fisica universitaria', 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400',
 (SELECT id FROM categoria WHERE nome='LIBRI' LIMIT 1), 'USATO'),

('Manuale Diritto Privato', 'Testo diritto privato - Resnick', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400',
 (SELECT id FROM categoria WHERE nome='LIBRI' LIMIT 1), 'USATO'),

('Chimica Organica Smith', 'Testo chimica organica con esercizi', 'https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400',
 (SELECT id FROM categoria WHERE nome='LIBRI' LIMIT 1), 'USATO'),

('Programmazione Java Coad', 'Corso completo programmazione Java', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400',
 (SELECT id FROM categoria WHERE nome='LIBRI' LIMIT 1), 'USATO'),

('Algebra Lineare Lang', 'Testo algebra lineare universitario', 'https://images.unsplash.com/photo-1509228627152-72ae9ae6848d?w=400',
 (SELECT id FROM categoria WHERE nome='LIBRI' LIMIT 1), 'USATO'),

-- Elettronica
('Apple iPhone 12', 'iPhone 12 64GB nero, perfettamente funzionante', 'https://images.unsplash.com/photo-1610577872481-c95cd6d9e654?w=400',
 (SELECT id FROM categoria WHERE nome='ELETTRONICA' LIMIT 1), 'USATO'),

('Sony WH-1000XM4', 'Cuffie Bluetooth con cancellazione rumore', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=400',
 (SELECT id FROM categoria WHERE nome='ELETTRONICA' LIMIT 1), 'USATO'),

('Samsung Galaxy Tab S7', 'Tablet Samsung 11 pollici, 128GB', 'https://images.unsplash.com/photo-1561154464-82e9adf32764?w=400',
 (SELECT id FROM categoria WHERE nome='ELETTRONICA' LIMIT 1), 'USATO'),

('MacBook Air M1', 'MacBook Air 2020 M1 256GB, perfette condizioni', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400',
 (SELECT id FROM categoria WHERE nome='ELETTRONICA' LIMIT 1), 'USATO'),

('Nintendo Switch', 'Console Nintendo con Joy-Con, versione OLED', 'https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=400',
 (SELECT id FROM categoria WHERE nome='ELETTRONICA' LIMIT 1), 'NUOVO'),

('GoPro Hero 10', 'Action camera 4K, usata poche volte', 'https://images.unsplash.com/photo-1564466809058-bf4114d5535d?w=400',
 (SELECT id FROM categoria WHERE nome='ELETTRONICA' LIMIT 1), 'USATO'),

-- Abbigliamento
('Giacca Levi''s', 'Giacca jeans taglia M, usata poco', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400',
 (SELECT id FROM categoria WHERE nome='ABBIGLIAMENTO' LIMIT 1), 'USATO'),

('Sneakers Nike Air Max', 'Scarpe running Nike taglia 43, nuove', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400',
 (SELECT id FROM categoria WHERE nome='ABBIGLIAMENTO' LIMIT 1), 'NUOVO'),

('Zaino Invicta', 'Zaino per università nero, capiente', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400',
 (SELECT id FROM categoria WHERE nome='ABBIGLIAMENTO' LIMIT 1), 'USATO'),

('Cappotto Woolrich', 'Cappotto invernale taglia L, ottime condizioni', 'https://images.unsplash.com/photo-1539533113208-f6df8cc8b743?w=400',
 (SELECT id FROM categoria WHERE nome='ABBIGLIAMENTO' LIMIT 1), 'USATO'),

('T-Shirt Supreme', 'T-shirt originale Supreme taglia M', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400',
 (SELECT id FROM categoria WHERE nome='ABBIGLIAMENTO' LIMIT 1), 'NUOVO'),

('Jeans Diesel', 'Pantalone jeans skinny taglia 50', 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400',
 (SELECT id FROM categoria WHERE nome='ABBIGLIAMENTO' LIMIT 1), 'USATO'),

-- Sport
('Mountain Bike Trek', 'Bicicletta mountain bike telaio carbonio', 'https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400',
 (SELECT id FROM categoria WHERE nome='SPORT' LIMIT 1), 'USATO'),

('Scarpe da calcio Adidas', 'Scarpini calcio Adidas Predator, taglia 43', 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=400',
 (SELECT id FROM categoria WHERE nome='SPORT' LIMIT 1), 'USATO'),

('Pallone da pallavolo', 'Pallone da pallavolo professionale', 'https://images.unsplash.com/photo-1614632537423-5e1c4072a025?w=400',
 (SELECT id FROM categoria WHERE nome='SPORT' LIMIT 1), 'USATO'),

('Attrezzi palestra', 'Set manubri e bilanciere palestra', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400',
 (SELECT id FROM categoria WHERE nome='SPORT' LIMIT 1), 'NUOVO'),

('Rollerblade professionali', 'Pattini inline velocità', 'https://images.unsplash.com/photo-1564466809058-bf4114d55352?w=400',
 (SELECT id FROM categoria WHERE nome='SPORT' LIMIT 1), 'USATO'),

('Tapis roulant pieghevole', 'Tapis roulant elettrico usato poco', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400',
 (SELECT id FROM categoria WHERE nome='SPORT' LIMIT 1), 'USATO'),

-- Altri
('Monopattino Xiaomi', 'Monopattino elettrico Xiaomi Pro 2', 'https://images.unsplash.com/photo-1571127236794-81c0bbfe1ce3?w=400',
 (SELECT id FROM categoria WHERE nome='ALTRO' LIMIT 1), 'USATO'),

('Macchina fotografica Canon', 'Canon EOS 4000D con obiettivo 18-55mm', 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=400',
 (SELECT id FROM categoria WHERE nome='ALTRO' LIMIT 1), 'USATO'),

('Chitarra acustica Yamaha', 'Chitarra folk Yamaha con custodia', 'https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=400',
 (SELECT id FROM categoria WHERE nome='ALTRO' LIMIT 1), 'USATO'),

('Tastiera meccanica', 'Tastiera gaming RGB Keychron', 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=400',
 (SELECT id FROM categoria WHERE nome='ALTRO' LIMIT 1), 'NUOVO'),

('Lampada da scrivania', 'Lampada LED smart con controllo app', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400',
 (SELECT id FROM categoria WHERE nome='ALTRO' LIMIT 1), 'USATO'),

('Plant da interno', 'Piante grasse set da 5 con vasi', 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=400',
 (SELECT id FROM categoria WHERE nome='ALTRO' LIMIT 1), 'NUOVO')
ON CONFLICT DO NOTHING;

-- =================================================================
-- 2. INSERISCI ANNUNCI (collegati agli utenti esistenti)
-- =================================================================

-- Utenti ID: 1-15 (dalla query precedente)
-- Distribuiamo gli annunci tra diversi utenti

-- Annuncio 1: Libro Calcolo - Utente 4 (mattia borrelli - 123@studenti.unina.it)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Libro Calcolo Differenziale Adams - Edizione 2023', id, 25.00, true, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 4,
       NOW() - INTERVAL '10 days', image_url,
       'Vendo libro di analisi matematica Adams, edizione 2023. Libro in ottime condizioni, sottolineature a matita.'
FROM oggetto WHERE nome='Calcolo Differenziale Adams' LIMIT 1;

-- Annuncio 2: Fisica Generale - Utente 10 (cccc)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Fisica Generale Mencuccini - Corso Completo', id, 30.00, false, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 10,
       NOW() - INTERVAL '8 days', image_url,
       'Corso completo di fisica universitaria Mencuccini. Usato ma in buone condizioni.'
FROM oggetto WHERE nome='Fisica Generale Mencuccini' LIMIT 1;

-- Annuncio 3: iPhone 12 - Utente 11 (aaaa)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Apple iPhone 12 64GB Nero - Perfettamente Funzionante', id, 350.00, true, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 11,
       NOW() - INTERVAL '5 days', image_url,
       'Vendo iPhone 12 64GB colore nero. Telefono in perfette condizioni, batteria 85%. Include caricabatterie.'
FROM oggetto WHERE nome='Apple iPhone 12' LIMIT 1;

-- Annuncio 4: Cuffie Sony - Utente 13 (aaaaa)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Sony WH-1000XM4 - Cuffie Bluetooth ANC', id, 180.00, false, 'SCAMBIO', 'CONSEGNA_MANUALE', 'ATTIVO', 13,
       NOW() - INTERVAL '3 days', image_url,
       'Cuffie Sony con cancellazione rumore. Cerco scambio con tablet o Kindle. Condizioni perfette.'
FROM oggetto WHERE nome='Sony WH-1000XM4' LIMIT 1;

-- Annuncio 5: MacBook Air - Utente 4 (mattia borrelli)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'MacBook Air M1 256GB - Ottime Condizioni', id, 750.00, true, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 4,
       NOW() - INTERVAL '7 days', image_url,
       'MacBook Air 2020 con chip M1, 256GB SSD. Condizioni eccellenti, batteria 92%.'
FROM oggetto WHERE nome='MacBook Air M1' LIMIT 1;

-- Annuncio 6: Nintendo Switch - Utente 10 (cccc)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Nintendo Switch OLED - Nuovissima', id, 320.00, true, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 10,
       NOW() - INTERVAL '2 days', image_url,
       'Nintendo Switch versione OLED ancora nella scatola. Regalo non gradito.'
FROM oggetto WHERE nome='Nintendo Switch' LIMIT 1;

-- Annuncio 7: Mountain Bike - Utente 11 (aaaa)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Mountain Bike Trek Telaio Carbonio', id, 1200.00, false, 'SCAMBIO', 'CONSEGNA_MANUALE', 'ATTIVO', 11,
       NOW() - INTERVAL '12 days', image_url,
       'MTB Trek professionale telaio carbonio. Cerco scambio con moto 50cc o vendo.'
FROM oggetto WHERE nome='Mountain Bike Trek' LIMIT 1;

-- Annuncio 8: Monopattino Xiaomi - Utente 13 (aaaaa)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Monopattino Xiaomi Pro 2 - 45km Autonomia', id, 400.00, true, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 13,
       NOW() - INTERVAL '6 days', image_url,
       'Monopattino Xiaomi Pro 2, autonomia 45km, velocità max 25km/h. Usato solo per università.'
FROM oggetto WHERE nome='Monopattino Xiaomi' LIMIT 1;

-- Annuncio 9: Chitarra Yamaha - Utente 4
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Chitarra Acustica Yamaha con Custodia', id, 120.00, false, 'SCAMBIO', 'CONSEGNA_MANUALE', 'ATTIVO', 4,
       NOW() - INTERVAL '4 days', image_url,
       'Chitarra folk Yamaha perfetta per principianti. Cerco scambio con basso o tastiera.'
FROM oggetto WHERE nome='Chitarra acustica Yamaha' LIMIT 1;

-- Annuncio 10: Tastiera meccanica - Utente 10
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Tastiera Meccanica Keychron RGB Wireless', id, 85.00, false, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 10,
       NOW() - INTERVAL '1 day', image_url,
       'Tastiera gaming Keychron K2 con switch Brown retroilluminati RGB. Bluetooth o cablata.'
FROM oggetto WHERE nome='Tastiera meccanica' LIMIT 1;

-- Annuncio 11: Giacca Levi's - Utente 11
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Giacca Jeans Levi''s Taglia M', id, 45.00, false, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 11,
       NOW() - INTERVAL '9 days', image_url,
       'Giacca jeans Levi''s taglia M, usata pochissimo. Come nuova.'
FROM oggetto WHERE nome='Giacca Levi''s' LIMIT 1;

-- Annuncio 12: Sneakers Nike - Utente 13
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Sneakers Nike Air Max Taglia 43 - Nuove', id, 65.00, true, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 13,
       NOW() - INTERVAL '11 days', image_url,
       'Scarpe running Nike Air Max taglia 43, mai usate. Ancora con scatola.'
FROM oggetto WHERE nome='Sneakers Nike Air Max' LIMIT 1;

-- Annuncio 13: Zaino Invicta - Utente 4
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Zaino Invicto Nero per Università', id, 35.00, false, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 4,
       NOW() - INTERVAL '15 days', image_url,
       'Zaino Invicta nero capiente con porta PC 15 pollici. Usato un anno.'
FROM oggetto WHERE nome='Zaino Invicta' LIMIT 1;

-- Annuncio 14: Samsung Tablet - Utente 10
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, image_url, descrizione)
SELECT 'Samsung Galaxy Tab S7 128GB', id, 400.00, true, 'SCAMBIO', 'SPEDIZIONE', 'ATTIVO', 10,
       NOW() - INTERVAL '13 days', image_url,
       'Tablet Samsung 11 pollici 128GB. Cerco scambio con laptop o vendo.'
FROM oggetto WHERE nome='Samsung Galaxy Tab S7' LIMIT 1;

-- =================================================================
-- 3. INSERISCI OFFERTE (struttura corretta: annuncio_id, offerente_id, prezzo_offerto)
-- =================================================================

-- Offerta 1: Sul MacBook
INSERT INTO offerta (annuncio_id, offerente_id, prezzo_offerto, data_ora)
VALUES (
    (SELECT id FROM annuncio WHERE titolo LIKE '%MacBook%' LIMIT 1),
    10, -- cccc offre sul MacBook di mattia
    700.00,
    NOW() - INTERVAL '6 hours'
);

-- Offerta 2: Sul Nintendo Switch
INSERT INTO offerta (annuncio_id, offerente_id, prezzo_offerto, data_ora)
VALUES (
    (SELECT id FROM annuncio WHERE titolo LIKE '%Nintendo Switch%' LIMIT 1),
    13, -- aaaaa offre
    300.00,
    NOW() - INTERVAL '1 day'
);

-- Offerta 3: Sul iPhone
INSERT INTO offerta (annuncio_id, offerente_id, prezzo_offerto, data_ora)
VALUES (
    (SELECT id FROM annuncio WHERE titolo LIKE '%iPhone 12%' LIMIT 1),
    4, -- mattia offre
    330.00,
    NOW() - INTERVAL '12 hours'
);

-- Offerta 4: Sul Monopattino
INSERT INTO offerta (annuncio_id, offerente_id, prezzo_offerto, data_ora)
VALUES (
    (SELECT id FROM annuncio WHERE titolo LIKE '%Monopattino%' LIMIT 1),
    11, -- aaaa offre
    380.00,
    NOW() - INTERVAL '2 days'
);

-- Offerta 5: Sul MacBook (altra offerta)
INSERT INTO offerta (annuncio_id, offerente_id, prezzo_offerto, data_ora)
VALUES (
    (SELECT id FROM annuncio WHERE titolo LIKE '%MacBook%' LIMIT 1),
    13, -- aaaaa offre
    720.00,
    NOW() - INTERVAL '1 day'
);

-- =================================================================
-- 4. INSERISCI RECENSIONI (struttura: acquirente_id, venditore_id, annuncio_id, commento, punteggio)
-- =================================================================

-- Recensione 1: Utente 10 acquirente, Utente 4 venditore
INSERT INTO recensione (acquirente_id, venditore_id, annuncio_id, commento, punteggio, data_recensione)
VALUES (
    10,
    4,
    (SELECT id FROM annuncio WHERE venditore_id = 4 LIMIT 1),
    'Persona affidabilissima! Transazione perfetta, oggetto come descritto. Consigliatissimo!',
    5,
    NOW() - INTERVAL '5 days'
);

-- Recensione 2: Utente 11 acquirente, Utente 10 venditore
INSERT INTO recensione (acquirente_id, venditore_id, annuncio_id, commento, punteggio, data_recensione)
VALUES (
    11,
    10,
    (SELECT id FROM annuncio WHERE venditore_id = 10 LIMIT 1),
    'Venditore serio e puntuale. Oggetto in perfette condizioni.',
    5,
    NOW() - INTERVAL '7 days'
);

-- Recensione 3: Utente 13 acquirente, Utente 11 venditore
INSERT INTO recensione (acquirente_id, venditore_id, annuncio_id, commento, punteggio, data_recensione)
VALUES (
    13,
    11,
    (SELECT id FROM annuncio WHERE venditore_id = 11 LIMIT 1),
    'Tutto ok, nulla da dire. Consegna veloce.',
    4,
    NOW() - INTERVAL '3 days'
);

-- Recensione 4: Utente 4 acquirente, Utente 13 venditore
INSERT INTO recensione (acquirente_id, venditore_id, annuncio_id, commento, punteggio, data_recensione)
VALUES (
    4,
    13,
    (SELECT id FROM annuncio WHERE venditore_id = 13 LIMIT 1),
    'Perfetto! Tutto come descritto. 5 stelle!',
    5,
    NOW() - INTERVAL '10 days'
);

-- Recensione 5: Utente 10 acquirente, Utente 4 venditore (seconda recensione)
INSERT INTO recensione (acquirente_id, venditore_id, annuncio_id, commento, punteggio, data_recensione)
VALUES (
    10,
    4,
    (SELECT id FROM annuncio WHERE venditore_id = 4 OFFSET 1 LIMIT 1),
    'Seconda volta che acquisto da questo venditore. Sempre affidabile!',
    5,
    NOW() - INTERVAL '1 day'
);

-- =================================================================
-- 5. INSERISCI SCAMBI (richiede annuncio_richiesto_id E annuncio_offerto_id)
-- =================================================================

-- Scambio 1: Utente 10 vuole il MacBook di Utente 4, offre il suo tablet
INSERT INTO scambio (richiedente_id, annuncio_richiesto_id, annuncio_offerto_id, stato, messaggio, data_proposta)
VALUES (
    10,
    (SELECT id FROM annuncio WHERE titolo LIKE '%MacBook%' LIMIT 1),
    (SELECT id FROM annuncio WHERE titolo LIKE '%Samsung Galaxy Tab%' LIMIT 1),
    'IN_ATTESA',
    'Ti interessa scambiare il MacBook con il mio tablet? Aggiungo anche euro se necessario.',
    NOW() - INTERVAL '3 days'
);

-- Scambio 2: Utente 13 vuole le cuffie di Utente 4, offre la tastiera
INSERT INTO scambio (richiedente_id, annuncio_richiesto_id, annuncio_offerto_id, stato, messaggio, data_proposta)
VALUES (
    13,
    (SELECT id FROM annuncio WHERE titolo LIKE '%Sony WH-1000XM4%' LIMIT 1),
    (SELECT id FROM annuncio WHERE titolo LIKE '%Tastiera Meccanica%' LIMIT 1),
    'IN_ATTESA',
    'Ti va la tastiera meccanica in cambio delle cuffie?',
    NOW() - INTERVAL '2 days'
);

-- Scambio 3: Utente 11 vuole la chitarra di Utente 4, offre lo zaino
INSERT INTO scambio (richiedente_id, annuncio_richiesto_id, annuncio_offerto_id, stato, data_proposta, data_accettazione)
VALUES (
    11,
    (SELECT id FROM annuncio WHERE titolo LIKE '%Chitarra%' LIMIT 1),
    (SELECT id FROM annuncio WHERE titolo LIKE '%Zaino%' LIMIT 1),
    'ACCETTATO',
    NOW() - INTERVAL '8 days',
    NOW() - INTERVAL '7 days'
);

-- =================================================================
-- STATISTICHE FINALI
-- =================================================================

SELECT '==========================================';
SELECT 'DATABASE POPOLATO CON SUCCESSO!';
SELECT '==========================================';
SELECT 'OGGETTI: ' || COUNT(*) FROM oggetto;
SELECT 'ANNUNCI: ' || COUNT(*) FROM annuncio;
SELECT 'OFFERTE: ' || COUNT(*) FROM offerta;
SELECT 'RECENSIONI: ' || COUNT(*) FROM recensione;
SELECT 'SCAMBI: ' || COUNT(*) FROM scambio;
SELECT 'MESSAGGI: ' || COUNT(*) FROM messaggio;
SELECT '==========================================';
