-- Script di popolamento database SwapUnina
-- Crea dati di esempio realistici per: annunci, messaggi, scambi, offerte, recensioni

-- =================================================================
-- PRIMA: Verifica ID utenti esistenti
-- =================================================================

-- Gli utenti nel database:
-- 123@studenti.unina.it (ID: 12)
-- aaaa@studenti.unina.it (ID: ?)
-- aaaaa@studenti.unina.it (ID: ?)
-- cccc@studenti.unina.it (ID: ?)
-- mario.rossi@email.com (ID: ?)
-- luigi.bianchi@email.com (ID: ?)
-- giulia.verdi@email.com (ID: ?)

-- =================================================================
-- VERIFICA PRESENZA OGGETTI GIÀ ESISTENTI
-- =================================================================
-- Se gli oggetti esistono già, non li reinseriamo

-- =================================================================
-- 1. INSERISCI OGGETTI (se non esistono già)
-- =================================================================

-- Libri
INSERT INTO oggetto (nome, descrizione, anno_acquisto, stato_conservazione, categoria, image_url) VALUES
('Calcolo Differenziale Adams', 'Libro di analisi matematica, edizione 2023', 2023, 'OTTIMO', 'LIBRI', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400'),
('Fisica Generale - Mencuccini', 'Corso completo di fisica universitaria', 2022, 'BUONO', 'LIBRI', 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400'),
('Manuale di Diritto Privato', 'Libro diritto privato - Resnick', 2023, 'NUOVO', 'LIBRI', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400'),
('Chimica Organica - Smith', 'Testo chimica organica con esercizi', 2022, 'DISCRETO', 'LIBRI', 'https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=400'),
('Programmazione Java - Coad', 'Corso completo programmazione Java', 2023, 'OTTIMO', 'LIBRI', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400'),
('Algebra Lineare - Lang', 'Testo algebra lineare universitario', 2021, 'BUONO', 'LIBRI', 'https://images.unsplash.com/photo-1509228627152-72ae9ae6848d?w=400'),

-- Elettronica
('Apple iPhone 12', 'iPhone 12 64GB nero, perfettamente funzionante', 2021, 'BUONO', 'ELETTRONICA', 'https://images.unsplash.com/photo-1610577872481-c95cd6d9e654?w=400'),
('Sony WH-1000XM4', 'Cuffie Bluetooth con cancellazione rumore', 2022, 'OTTIMO', 'ELETTRONICA', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=400'),
('Samsung Galaxy Tab S7', 'Tablet Samsung 11 pollici, 128GB', 2022, 'BUONO', 'ELETTRONICA', 'https://images.unsplash.com/photo-1561154464-82e9adf32764?w=400'),
('MacBook Air M1', 'MacBook Air 2020 M1 256GB, perfette condizioni', 2020, 'OTTIMO', 'ELETTRONICA', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400'),
('Nintendo Switch', 'Console Nintendo con Joy-Con, versione OLED', 2023, 'NUOVO', 'ELETTRONICA', 'https://images.unsplash.com/photo-1578303512597-81e6cc155b3e?w=400'),
('GoPro Hero 10', 'Action camera 4K, usata poche volte', 2022, 'OTTIMO', 'ELETTRONICA', 'https://images.unsplash.com/photo-1564466019707-c61d2b54c3e6?w=400'),

-- Abbigliamento
('Giacca Levi''s', 'Giacca jeans taglia M, usata poco', 2023, 'OTTIMO', 'ABBIGLIAMENTO', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400'),
('Sneakers Nike Air Max', 'Scarpe running Nike taglia 43, nuove', 2023, 'NUOVO', 'ABBIGLIAMENTO', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400'),
('Zaino Invicta', 'Zaino per università nero, capiente', 2022, 'BUONO', 'ABBIGLIAMENTO', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400'),
('Cappotto woolrich', 'Cappotto invernale taglia L, ottime condizioni', 2021, 'OTTIMO', 'ABBIGLIAMENTO', 'https://images.unsplash.com/photo-1539533113208-f6df8cc8b743?w=400'),
('T-shirt Supreme', 'T-shirt originale Supreme taglia M', 2023, 'NUOVO', 'ABBIGLIAMENTO', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400'),
('Jeans Diesel', 'Pantalone jeans skinny taglia 50', 2022, 'BUONO', 'ABBIGLIAMENTO', 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400'),

-- Sport
('Mountain Bike Trek', 'Bicicletta mountain bike telaio carbonio', 2022, 'OTTIMO', 'SPORT', 'https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400'),
('Scarpe da calcio Adidas', 'Scarpini calcio Adidas Predator, taglia 43', 2023, 'BUONO', 'SPORT', 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=400'),
('Pallavolo Mizuno', 'Pallone da pallavolo professionale', 2022, 'OTTIMO', 'SPORT', 'https://images.unsplash.com/photo-1614632537423-5e1c4072a025?w=400'),
('Attrezzi palestra', 'Set manubri e bilanciere palestra', 2023, 'NUOVO', 'SPORT', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400'),
('Rollerblade professionali', 'Pattini inline velocità', 2021, 'BUONO', 'SPORT', 'https://images.unsplash.com/photo-1564466809058-bf4114d55352?w=400'),
('Tapis roulant pieghevole', 'Tapis roulant elettrico usato poco', 2022, 'OTTIMO', 'SPORT', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400'),

-- Altri
('Monopattino Xiaomi', 'Monopattino elettrico Xiaomi Pro 2', 2022, 'OTTIMO', 'ALTRO', 'https://images.unsplash.com/photo-1571127236794-81c0bbfe1ce3?w=400'),
('Macchina fotografica Canon', 'Canon EOS 4000D con obiettivo 18-55mm', 2021, 'BUONO', 'ALTRO', 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=400'),
('Chitarra acustica Yamaha', 'Chitarra folk Yamaha con custodia', 2020, 'BUONO', 'ALTRO', 'https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=400'),
('Tastiera meccanica', 'Tastiera gaming RGB Keychron', 2023, 'NUOVO', 'ALTRO', 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=400'),
('Lampada da scrivania', 'Lampada LED smart con controllo app', 2023, 'OTTIMO', 'ALTRO', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400'),
('Plant da interno', 'Piante grasse set da 5 con vasi', 2023, 'NUOVO', 'ALTRO', 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=400');

-- =================================================================
-- 2. INSERISCI ANNUNCI (associa oggetti a utenti)
-- =================================================================

-- Nota: Assumo che gli utenti abbiano ID 12, 13, 14, 15, 16, 17
-- In pratica devi prima leggere gli ID reali dal database

-- Annuncio 1: Libro calcolo (utente 12)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Libro Calcolo Differenziale Adams - Edizione 2023',
        (SELECT id FROM oggetto WHERE nome='Calcolo Differenziale Adams' LIMIT 1),
        25.00, true, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 12, NOW() - INTERVAL '10 days',
        'Vendo libro di analisi matematica Adams, edizione 2023. Libro in ottime condizioni, sottolineature a matita. Perfetto per corso di Analisi Matematica 1.');

-- Annuncio 2: Fisica Generale (utente 13)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Fisica Generale Mencuccini - Corso Completo',
        (SELECT id FROM oggetto WHERE nome='Fisica Generale - Mencuccini' LIMIT 1),
        30.00, false, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 13, NOW() - INTERVAL '8 days',
        'Corso completo di fisica universitaria Mencuccini. Usato ma in buone condizioni, copertina un po'' consumata ma interno integro.');

-- Annuncio 3: iPhone 12 (utente 14)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Apple iPhone 12 64GB Nero - Perfettamente Funzionante',
        (SELECT id FROM oggetto WHERE nome='Apple iPhone 12' LIMIT 1),
        350.00, true, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 14, NOW() - INTERVAL '5 days',
        'Vendo iPhone 12 64GB colore nero. Telefono in perfette condizioni, batteria 85%, no graffi. Incluso caricabatterie originale e scatola.');

-- Annuncio 4: Cuffie Sony (utente 15)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Sony WH-1000XM4 - Cuffie Bluetooth ANC',
        (SELECT id FROM oggetto WHERE nome='Sony WH-1000XM4' LIMIT 1),
        180.00, false, 'SCAMBIO', 'CONSEGNA_MANUALE', 'ATTIVO', 15, NOW() - INTERVAL '3 days',
        'Cuffie Sony con cancellazione rumore top di gamma. Cerco scambio con tablet o Kindle. Condizioni perfette, usate 6 mesi.');

-- Annuncio 5: MacBook Air (utente 12)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('MacBook Air M1 256GB - Ottime Condizioni',
        (SELECT id FROM oggetto WHERE nome='MacBook Air M1' LIMIT 1),
        750.00, true, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 12, NOW() - INTERVAL '7 days',
        'MacBook Air 2020 con chip M1, 256GB SSD. Condizioni eccellenti, batteria 92%. Include caricabatterie USB-C originale. Perfetto per università.');

-- Annuncio 6: Nintendo Switch (utente 13)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Nintendo Switch OLED - Nuovissima',
        (SELECT id FROM oggetto WHERE nome='Nintendo Switch' LIMIT 1),
        320.00, true, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 13, NOW() - INTERVAL '2 days',
        'Nintendo Switch versione OLED ancora nella scatola. Regalo non gradito, vendo a prezzo speciale. Include Joy-Con rosso/blu e dock.');

-- Annuncio 7: Mountain Bike (utente 14)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Mountain Bike Trek Telaio Carbonio - Top di Gamma',
        (SELECT id FROM oggetto WHERE nome='Mountain Bike Trek' LIMIT 1),
        1200.00, false, 'SCAMBIO_O_VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 14, NOW() - INTERVAL '12 days',
        'MTB Trek professionale telaio carbonio. Cerco scambio con moto 50cc o vendo. 21 velocità, sospensioni Fox, pneumatici tubeless.');

-- Annuncio 8: Monopattino Xiaomi (utente 15)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Monopattino Elettrico Xiaomi Pro 2 - 45km Autonomia',
        (SELECT id FROM oggetto WHERE nome='Monopattino Xiaomi' LIMIT 1),
        400.00, true, 'VENDITA', 'CONSEGNA_MANUALE', 'ATTIVO', 15, NOW() - INTERVAL '6 days',
        'Monopattino Xiaomi Pro 2, autonomia 45km, velocità max 25km/h. Usato solo per andare all''università. Perfette condizioni.');

-- Annuncio 9: Chitarra Yamaha (utente 12)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Chitarra Acustica Yamaha Folk con Custodia',
        (SELECT id FROM oggetto WHERE nome='Chitarra acustica Yamaha' LIMIT 1),
        120.00, false, 'SCAMBIO', 'CONSEGNA_MANUALE', 'ATTIVO', 12, NOW() - INTERVAL '4 days',
        'Chitarra folk Yamaha perfetta per principianti. Cerco scambio con basso elettrico o tastiera. Include custodia morbida e plettre.');

-- Annuncio 10: Tastiera meccanica (utente 13)
INSERT INTO annuncio (titolo, oggetto_id, prezzo, in_evidenza, tipologia, modalita_consegna, stato, venditore_id, data_pubblicazione, descrizione)
VALUES ('Tastiera Meccanica Keychron RGB - Wireless',
        (SELECT id FROM oggetto WHERE nome='Tastiera meccanica' LIMIT 1),
        85.00, false, 'VENDITA', 'SPEDIZIONE', 'ATTIVO', 13, NOW() - INTERVAL '1 day',
        'Tastiera gaming Keychron K2 con switch Brown retroilluminati RGB. Connettività Bluetooth o cablata. Usata 2 mesi per smartworking.');

-- =================================================================
-- 3. INSERISCI MESSAGGI (chat tra utenti)
-- =================================================================

-- NOTA: I messaggi sono criptati con AES-GCM, quindi per inserirli direttamente
-- nel database serve usare il servizio di crittografia dell'applicazione.
-- Qui creo record di esempio con dati criptati fittizi.

-- Chat tra utente 12 e 13 sull'annuncio 3 (iPhone)
INSERT INTO messaggio (mittente_id, destinatario_id, testo_encrypted, iv, data_invio, annuncio_id, algoritmo_encryption)
VALUES
(12, 14, '\x' || repeat('00', 100), '\x' || repeat('AA', 12), NOW() - INTERVAL '2 days', (SELECT id FROM annuncio WHERE titolo LIKE '%iPhone 12%' LIMIT 1), 'AES/GCM/NoPadding'),
(14, 12, '\x' || repeat('11', 100), '\x' || repeat('BB', 12), NOW() - INTERVAL '2 days' + INTERVAL '1 hour', (SELECT id FROM annuncio WHERE titolo LIKE '%iPhone 12%' LIMIT 1), 'AES/GCM/NoPadding'),
(12, 14, '\x' || repeat('22', 100), '\x' || repeat('CC', 12), NOW() - INTERVAL '1 day', (SELECT id FROM annuncio WHERE titolo LIKE '%iPhone 12%' LIMIT 1), 'AES/GCM/NoPadding');

-- =================================================================
-- 4. INSERISCI SCAMBI
-- =================================================================

-- Scambio 1: Utente 13 vuole il MacBook di utente 12, offre la sua bici
INSERT INTO scambio (richiedente_id, annuncio_richiesto_id, annuncio_offerto_id, stato, messaggio, data_proposta)
VALUES
(13, (SELECT id FROM annuncio WHERE titolo LIKE '%MacBook%' LIMIT 1), (SELECT id FROM annuncio WHERE titolo LIKE '%Mountain Bike%' LIMIT 1), 'IN_ATTESA', 'Ti interessa scambiare il MacBook con la mia MTB? Aggiungo anche euro se necessario.', NOW() - INTERVAL '3 days');

-- Scambio 2: Utente 14 vuole la chitarra di utente 12
INSERT INTO scambio (richiedente_id, annuncio_richiesto_id, annuncio_offerto_id, stato, messaggio, data_proposta)
VALUES
(14, (SELECT id FROM annuncio WHERE titolo LIKE '%Chitarra%' LIMIT 1), (SELECT id FROM annuncio WHERE titolo LIKE '%Monopattino%' LIMIT 1), 'ACCETTATO', 'Ti va il monopattino in cambio della chitarra?', NOW() - INTERVAL '5 days');

-- Aggiorna data accettazione scambio 2
UPDATE scambio SET data_accettazione = NOW() - INTERVAL '4 days' WHERE id = (SELECT id FROM scambio WHERE messaggio LIKE '%Ti va il monopattino%' LIMIT 1);

-- =================================================================
-- 5. INSERISCI OFFERTE DI NEGOZIAZIONE
-- =================================================================

-- Offerta 1: Sconto sul MacBook
INSERT INTO offerta (annuncio_id, offerente_id, venditore_id, importo, messaggio, stato, tipo, data_creazione)
VALUES
((SELECT id FROM annuncio WHERE titolo LIKE '%MacBook%' LIMIT 1), 13, 12, 700.00, 'Ti offro 700€ subito, contanti?', 'IN_ATTESA', 'INIZIALE', NOW() - INTERVAL '6 hours');

-- Offerta 2: Sconto sul Nintendo Switch
INSERT INTO offerta (annuncio_id, offerente_id, venditore_id, importo, messaggio, stato, tipo, data_creazione)
VALUES
((SELECT id FROM annuncio WHERE titolo LIKE '%Nintendo Switch%' LIMIT 1), 15, 13, 300.00, '300€ e lo prendo oggi stesso', 'IN_ATTESA', 'INIZIALE', NOW() - INTERVAL '1 day');

-- Controfferta alla offerta 2
INSERT INTO offerta (annuncio_id, offerente_id, venditore_id, importo, messaggio, stato, tipo, data_creazione, offerta_padre_id)
VALUES
((SELECT id FROM annuncio WHERE titolo LIKE '%Nintendo Switch%' LIMIT 1), 13, 15, 310.00, 'Accetto ma minimo 310€', 'IN_ATTESA', 'CONTRO_OFFERTA', NOW() - INTERVAL '12 hours', (SELECT id FROM offerta WHERE messaggio LIKE '%300€%' LIMIT 1));

-- =================================================================
-- 6. INSERISCI RECENSIONI
-- =================================================================

-- Recensione 1: Utente 12 ha fatto uno scambio con utente 14
INSERT INTO recensione (recensore_id, recensito_id, valutazione, commento, data_recensione, scambio_id)
VALUES
(14, 12, 5, 'Persona affidabilissima! Scambio perfetto, monopattino come descritto. Consigliatissimo!', NOW() - INTERVAL '3 days', (SELECT id FROM scambio WHERE stato = 'ACCETTATO' LIMIT 1));

-- Recensione 2: Utente 14 ha ricevuto recensione da utente 12
INSERT INTO recensione (recensore_id, recensito_id, valutazione, commento, data_recensione, scambio_id)
VALUES
(12, 14, 5, 'Chitarra ricevuta in perfette condizioni, transizione smooth. Grazie!', NOW() - INTERVAL '3 days', (SELECT id FROM scambio WHERE stato = 'ACCETTATO' LIMIT 1));

-- =================================================================
-- STATISTICHE FINALI
-- =================================================================

DO $$
DECLARE
    v_oggetti INT;
    v_annunci INT;
    v_messaggi INT;
    v_scambi INT;
    v_offerte INT;
    v_recensioni INT;
BEGIN
    SELECT COUNT(*) INTO v_oggetti FROM oggetto;
    SELECT COUNT(*) INTO v_annunci FROM annuncio;
    SELECT COUNT(*) INTO v_messaggi FROM messaggio;
    SELECT COUNT(*) INTO v_scambi FROM scambio;
    SELECT COUNT(*) INTO v_offerte FROM offerta;
    SELECT COUNT(*) INTO v_recensioni FROM recensione;

    RAISE NOTICE '========================================';
    RAISE NOTICE 'DATABASE POPOLATO CON SUCCESSO!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Oggetti inseriti: %', v_oggetti;
    RAISE NOTICE 'Annunci inseriti: %', v_annunci;
    RAISE NOTICE 'Messaggi inseriti: %', v_messaggi;
    RAISE NOTICE 'Scambi inseriti: %', v_scambi;
    RAISE NOTICE 'Offerte inserite: %', v_offerte;
    RAISE NOTICE 'Recensioni inserite: %', v_recensioni;
    RAISE NOTICE '========================================';
END $$;
