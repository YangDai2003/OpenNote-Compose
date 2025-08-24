- [English](PRIVACY_POLICY.md)
- [Italiano](PRIVACY_POLICY.it.md)

## Privacy Policy di Open Note

Open Note è un'applicazione Android open source sviluppata da Yang Dai.  
Il codice sorgente è disponibile su GitHub con licenza GPL-3.0.

### Raccolta dati

Open Note non raccoglie alcuna informazione personale o riservata come indirizzi, nomi o indirizzi e-mail.

### Autorizzazioni richiesta

L'app richiede le seguenti autorizzazioni, come riportato nel file `AndroidManifest.xml`:

https://github.com/YangDai2003/OpenNote-Compose/blob/4bc1cafa7368d81c539a09374e95d9859ab170a4/app/src/main/AndroidManifest.xml#L4-L7

| Autorizzazioni                         | Scopo                                                                                                       |
|------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `android.permission.USE_BIOMETRIC` | Consente l'autenticazione biometrica per fornire un accesso sicuro alle note protette.                                  |
| `android.permission.INTERNET`      | Consente la connettività di rete per il caricamento di contenuti web (immagini, video e altri media) durante il rendering del markdown. |

### Dipendenze

L'app utilizza le seguenti dipendenze:

- **Room**: Per la gestione del database locale.
- **Hilt**: Per l'iniezione delle dipendenze.
- **Compose**: Per la creazione dell'interfaccia utente.
- **CommonMark**: Per il rendering e analisi sintattica del markdown.
- **ColorPicker**: Per le funzionalità di selezione dei colori.
- **Glance**: Per la creazione dei widget dell'app.

### Condivisione dei dati personali

Open Note non condivide con terzi alcun dato personale o sensibile degli utenti.

### Eliminazione dei dati personali

Di solito tutti i dati vengono memorizzati localmente e possono essere cancellati dall'utente in qualsiasi momento.  
Gli utenti possono anche aggiungere uno spazio di archiviazione cloud personale e caricare dati, nel qual caso l'applicazione non è responsabile delle azioni di terzi.

---

Per qualsiasi domanda relativa alla presente informativa o alla protezione dei dati personali, inviare le proprie richieste, opinioni o suggerimenti a: dy15800837435@gmail.com