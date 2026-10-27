-- ==========================================
-- STRUTTURA DATABASE (schema.sql)
-- ==========================================

-- 1. IMMAGINI
CREATE TABLE IF NOT EXISTS IMMAGINI (
    id_img INT AUTO_INCREMENT PRIMARY KEY,
    percorso_file VARCHAR(255) NOT NULL,
    tipo_risorsa VARCHAR(50) CHECK (tipo_risorsa IN ('Nemico','Stanza','Giocatore','Item','Mappa'))
);

-- 2. LIVELLO
CREATE TABLE IF NOT EXISTS LIVELLO (
    id_livello INT PRIMARY KEY,
    id_img INT,
    FOREIGN KEY (id_img) REFERENCES IMMAGINI(id_img) ON DELETE SET NULL
);

-- 3. NEMICO
CREATE TABLE IF NOT EXISTS NEMICO (
    id_nemico INT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    max_hp INT NOT NULL,
    danno_attacco INT NOT NULL,
    precisione DOUBLE,
    tipo VARCHAR(50) NOT NULL,
    messaggio_agguato VARCHAR(255) NOT NULL,
    id_img INT,
    FOREIGN KEY (id_img) REFERENCES IMMAGINI(id_img) ON DELETE SET NULL
);

-- 4. ITEM
CREATE TABLE IF NOT EXISTS ITEM (
    id_item INT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descrizione TEXT NOT NULL,
    id_img INT,
    utilizzabile_in_scontro BOOLEAN DEFAULT FALSE,
    valore_effetto INT DEFAULT 0,
    target_effetto VARCHAR(50) CHECK (target_effetto IN ('SALUTE_GIOCATORE', 'POTENZIAMENTO_ARMA', 'nessuno')),
    tipo VARCHAR(30) NOT NULL,
    FOREIGN KEY (id_img) REFERENCES IMMAGINI(id_img) ON DELETE SET NULL
);

-- 5. STANZA
CREATE TABLE IF NOT EXISTS STANZA (
    id_stanza INT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descrizione TEXT NOT NULL,
    bloccata BOOLEAN DEFAULT FALSE,
    stanza_letale BOOLEAN DEFAULT FALSE,
    stanza_letale_danno INT DEFAULT 0,
    id_nemico_presente INT NULL,
    id_img INT,
    id_livello INT NOT NULL,
    FOREIGN KEY (id_nemico_presente) REFERENCES NEMICO(id_nemico) ON DELETE SET NULL,
    FOREIGN KEY (id_livello) REFERENCES LIVELLO(id_livello) ON DELETE SET NULL,
    FOREIGN KEY (id_img) REFERENCES IMMAGINI(id_img) ON DELETE SET NULL
);

-- 6. GIOCATORE
CREATE TABLE IF NOT EXISTS GIOCATORE (
    id_giocatore INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    hp_attuali INT NOT NULL DEFAULT 100,
    hp_massimi INT NOT NULL DEFAULT 100,
    id_img INT,
    id_stanza INT,
    FOREIGN KEY (id_img) REFERENCES IMMAGINI(id_img) ON DELETE SET NULL,
    FOREIGN KEY (id_stanza) REFERENCES STANZA(id_stanza) ON DELETE SET NULL
);

-- 7. PARTITA
CREATE TABLE IF NOT EXISTS PARTITA (
    id_partita INT PRIMARY KEY,
    id_giocatore INT NOT NULL,
    FOREIGN KEY (id_giocatore) REFERENCES GIOCATORE(id_giocatore) ON DELETE CASCADE
);

-- 8. ADIACENZE STANZA
CREATE TABLE IF NOT EXISTS ADIACENZE_STANZA (
    stanza_partenza INT NOT NULL,
    stanza_arrivo INT NOT NULL,
    verso_comando VARCHAR(50) CHECK (verso_comando IN ('nord','sud','ovest','est')),
    PRIMARY KEY (stanza_partenza, stanza_arrivo),
    FOREIGN KEY (stanza_partenza) REFERENCES STANZA(id_stanza) ON DELETE CASCADE,
    FOREIGN KEY (stanza_arrivo) REFERENCES STANZA(id_stanza) ON DELETE CASCADE
);

-- 9. TABELLE DI STATO PARTITA
CREATE TABLE IF NOT EXISTS STANZA_PARTITA (
    id_stanza INT NOT NULL,
    id_partita INT NOT NULL,
    PRIMARY KEY (id_stanza, id_partita),
    FOREIGN KEY (id_stanza) REFERENCES STANZA(id_stanza) ON DELETE CASCADE,
    FOREIGN KEY (id_partita) REFERENCES PARTITA(id_partita) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS STANZA_PARTITA_ITEM (
    id_stanza INT NOT NULL,
    id_partita INT NOT NULL,
    slot_index INT NOT NULL,
    id_item INT NOT NULL,
    PRIMARY KEY (id_stanza, id_partita, slot_index),
    FOREIGN KEY (id_stanza) REFERENCES STANZA(id_stanza) ON DELETE CASCADE,
    FOREIGN KEY (id_partita) REFERENCES PARTITA(id_partita) ON DELETE CASCADE,
    FOREIGN KEY (id_item) REFERENCES ITEM(id_item) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS STANZA_PARTITA_NEMICO (
    id_stanza INT NOT NULL,
    id_partita INT NOT NULL,
    id_nemico INT NOT NULL,
    hp_correnti INT NOT NULL,
    PRIMARY KEY (id_stanza, id_partita, id_nemico),
    FOREIGN KEY (id_stanza) REFERENCES STANZA(id_stanza) ON DELETE CASCADE,
    FOREIGN KEY (id_partita) REFERENCES PARTITA(id_partita) ON DELETE CASCADE,
    FOREIGN KEY (id_nemico) REFERENCES NEMICO(id_nemico) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ITEM_PARTITA (
    id_partita INT NOT NULL,
    id_item INT NOT NULL,
    slot_index INT NOT NULL,
    PRIMARY KEY (id_partita, id_item),
    FOREIGN KEY (id_partita) REFERENCES PARTITA(id_partita) ON DELETE CASCADE,
    FOREIGN KEY (id_item) REFERENCES ITEM(id_item) ON DELETE CASCADE
);
-- Tabella oggetti nelle stanze della partita con slot fissi
CREATE TABLE IF NOT EXISTS STANZA_PARTITA_ITEM (
    id_stanza INT NOT NULL,
    id_partita INT NOT NULL,
    slot_index INT NOT NULL,
    id_item INT NOT NULL,
    PRIMARY KEY (id_stanza, id_partita, slot_index),
    FOREIGN KEY (id_stanza) REFERENCES STANZA(id_stanza) ON DELETE CASCADE,
    FOREIGN KEY (id_partita) REFERENCES PARTITA(id_partita) ON DELETE CASCADE,
    FOREIGN KEY (id_item) REFERENCES ITEM(id_item) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS STANZA_ITEM_BASE (
    id_stanza INT NOT NULL,
    id_item INT NOT NULL,
    slot_index INT NOT NULL,
    PRIMARY KEY (id_stanza, slot_index),
    FOREIGN KEY (id_stanza) REFERENCES STANZA(id_stanza) ON DELETE CASCADE,
    FOREIGN KEY (id_item) REFERENCES ITEM(id_item) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS STANZA_ITEM_RICHIESTO (
    id_stanza INT NOT NULL,
    id_item INT NOT NULL,
    PRIMARY KEY (id_stanza, id_item),
    FOREIGN KEY (id_stanza) REFERENCES STANZA(id_stanza) ON DELETE CASCADE,
    FOREIGN KEY (id_item) REFERENCES ITEM(id_item) ON DELETE CASCADE
);

