# Coinflip

Ein Coinflip-Plugin für Paper-Server mit Vault-Economy. Spieler setzen Geld, ein anderer Spieler hält mit dem gleichen Einsatz dagegen, und ein 50/50-Münzwurf entscheidet, wer den ganzen Pot bekommt. Alles läuft über eine übersichtliche GUI.

## Features

- **Coinflip-Übersicht** mit allen offenen Coinflips als Spielerköpfe, inklusive Einsatz und möglichem Gewinn
- **Coinflip erstellen** per GUI, der Betrag wird bequem auf einem Schild eingegeben
- **Kurzschreibweise** für Beträge: `500`, `2.5k`, `1m`, `3b`
- **Vault-Integration**: funktioniert mit jedem Economy-Plugin, das Vault unterstützt (z. B. EssentialsX)
- **Seiten** für viele gleichzeitige Coinflips
- **Eigene Coinflips abbrechen** mit voller Rückerstattung
- **Automatische Rückerstattung** aller offenen Einsätze beim Server-Stopp
- **Optionale Steuer** auf den Gewinn
- **Broadcast** für große Coinflips
- **Alle Nachrichten anpassbar** mit MiniMessage, jeder Text hat einen schwarzen Schatten

## Voraussetzungen

| Was | Version |
|---|---|
| Server | Paper 1.21.11 |
| Java | 21 |
| [Vault](https://www.spigotmc.org/resources/vault.34315/) (oder VaultUnlocked) | Pflicht |
| Economy-Plugin, z. B. [EssentialsX](https://essentialsx.net/) | Pflicht |

Ohne Vault startet das Plugin nicht. Ohne Economy-Plugin deaktiviert es sich beim Start mit einer Fehlermeldung in der Konsole.

## Installation

1. Vault und ein Economy-Plugin in den `plugins`-Ordner legen.
2. `coinflip-0.1.jar` aus dem passenden Versionsordner (z. B. `1.21.11/`) in den `plugins`-Ordner legen.
3. Server starten. Die `config.yml` wird unter `plugins/coinflip/` erstellt.

## Benutzung

### Übersicht (`/coinflip` oder `/cf`)

```
┌───────────────────────── Coinflips ─────────────────────────┐
│ [Kopf] [Kopf] [Kopf]  ...  offene Coinflips (27 pro Seite)   │
│                                                              │
│                                                              │
│ [Amboss]          [Rot] [Info] [Grün]              [Farbstoff]│
└──────────────────────────────────────────────────────────────┘
```

| Item | Funktion |
|---|---|
| Spielerkopf | Klick tritt dem Coinflip bei. Klick auf den eigenen bricht ihn ab. |
| Amboss | Neu laden, um frisch erstellte Coinflips zu sehen |
| Rote Glasscheibe | Vorherige Seite |
| Kisten-Lore | Aktuelle Seite und Anzahl offener Coinflips |
| Grüne Glasscheibe | Nächste Seite |
| Grüner Farbstoff | Eigenen Coinflip erstellen |

### Coinflip erstellen

| Item | Funktion |
|---|---|
| Schild | Zeigt den eingestellten Betrag |
| Amboss | Öffnet ein Schild, in die erste Zeile den Betrag schreiben |
| Grüne Glasscheibe | Bestätigen, der Einsatz wird abgezogen |

### Ablauf

1. Spieler A erstellt einen Coinflip über z. B. 10.000. Das Geld wird sofort abgezogen.
2. Spieler B klickt den Coinflip an und zahlt ebenfalls 10.000.
3. Die Münze entscheidet: Der Gewinner bekommt 20.000 (abzüglich Steuer, falls eingestellt).
4. Beide bekommen eine Nachricht. Ist der Ersteller offline, wird ihm der Gewinn trotzdem gutgeschrieben.

## Befehle

| Befehl | Beschreibung | Permission |
|---|---|---|
| `/coinflip`, `/cf` | Öffnet die Coinflip-Übersicht | `coinflip.use` |
| `/coinflip reload` | Lädt die `config.yml` neu | `coinflip.admin` |

## Permissions

| Permission | Beschreibung | Standard |
|---|---|---|
| `coinflip.use` | Menü öffnen, Coinflips erstellen und beitreten | alle Spieler |
| `coinflip.admin` | Konfiguration neu laden | OP |

## Konfiguration

```yaml
min-bet: 100              # Mindesteinsatz
max-bet: 10000000         # Höchsteinsatz
tax-percent: 0            # Steuer in Prozent vom Gesamtpot (0 = keine)
max-open-per-player: 3    # Wie viele offene Coinflips ein Spieler gleichzeitig haben darf
broadcast-min-bet: 10000  # Ab diesem Einsatz wird das Ergebnis an alle gesendet (0 = immer, -1 = nie)
```

### Nachrichten

Alle Nachrichten stehen unter `messages:` und nutzen [MiniMessage](https://docs.advntr.dev/minimessage/format.html). Ein schwarzer Textschatten wird automatisch vor jeden Text gesetzt, du musst ihn nicht selbst eintragen.

```yaml
messages:
  prefix: "<gold><bold>Coinflip</bold></gold> <dark_gray>»</dark_gray> "
  created: "<green>Coinflip über <yellow><amount></yellow> erstellt."
```

Verfügbare Platzhalter:

| Platzhalter | Bedeutung | Verwendet in |
|---|---|---|
| `<amount>` | Einsatz | die meisten Nachrichten |
| `<payout>` | Ausgezahlter Gewinn | `won` |
| `<opponent>` | Name des Gegners | `won`, `lost` |
| `<winner>`, `<loser>` | Namen der Spieler | `broadcast` |
| `<min>`, `<max>` | Einsatzgrenzen | `amount-out-of-range` |
| `<max>` | Maximal offene Coinflips | `too-many-open` |
| `<input>` | Ungültige Eingabe | `invalid-amount` |
| `<error>` | Fehler vom Economy-Plugin | `transaction-failed` |

Nach Änderungen `/coinflip reload` ausführen.

## Hinweise

- Offene Coinflips werden nicht gespeichert. Beim Server-Stopp bekommen alle Ersteller ihren Einsatz automatisch zurück.
- Einem eigenen Coinflip kann man nicht beitreten. Zum Testen braucht man einen zweiten Spieler.
- Die Betragseingabe nutzt ein Schild, das nur für den Spieler kurz unter ihm angezeigt wird. In der Welt wird nichts verändert.

## Selbst bauen

```bash
mvn clean package
```

Die fertige Jar landet in `BUILD/coinflip-<version>.jar`.

## Autor

LeonfoundU

README.md created by claude 
