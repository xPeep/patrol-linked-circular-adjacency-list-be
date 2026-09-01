# Patrol Loop API

Backend semestrální práce. Server drží cyklickou trasu skladového robota **ve vlastní datové
struktuře v paměti** — žádná databáze. Spring Boot tuto strukturu pouze zpřístupní přes REST API,
se kterým si povídá dodaný frontend.

Hlavní část projektu **není** Spring Boot. Hlavní část je vlastní
`CircularLinkedList` — cyklický obousměrně zřetězený seznam.

---

## Co dostáváš hotové

| Soubor | Proč je hotový |
|---|---|
| `build.gradle.kts`, `gradlew`, `application.yaml` | infrastruktura, závislosti a CORS originy |
| `frontend/index.html` | dodaný frontend, nebudeš ho programovat |
| `config/WebCorsConfiguration.kt` | CORS, ať se s tím nezdržuješ |
| `PatrolApplication.kt` | vygeneroval Spring Initializr |
| `datastructure/CircularList.kt` | **rozhraní = specifikace**, jeho KDoc čti jako první |
| `model/Checkpoint.kt`, `Priority.kt`, `PatrolState.kt` | doménový model, přesný tvar dat |
| `dto/CheckpointResponse.kt`, `PatrolStateResponse.kt` | tvar odpovědi, na který spoléhá frontend |
| `service/PatrolEmptyException.kt` | doménová výjimka pro prázdnou trasu |
| celý `src/test` | **zadání** — needitovat, nemazat, nedávat `@Disabled` |

## Co musíš napsat

| Soubor | Co s ním |
|---|---|
| `datastructure/CircularLinkedList.kt` | kostra s `TODO()` — **hlavní část práce** |
| `service/PatrolService.kt` | kostra s `TODO()` |
| `controller/PatrolController.kt` | prázdná třída, endpointy jsou na tobě |
| `dto/CheckpointCreateRequest.kt` | neexistuje — vytvoř, včetně validace |
| `dto/ApiErrorResponse.kt` | neexistuje — vytvoř |
| `controller/ApiExceptionHandler.kt` | neexistuje — vytvoř `@RestControllerAdvice` |
| mapování model → DTO | kdekoli uznáš za vhodné |

Na začátku padá 162 ze 169 testů. To je správný výchozí stav.

## Jak to spustit

```bash
./gradlew test        # celá sada testů
./gradlew bootRun     # server i frontend na http://localhost:8080
```

Frontend je jeden statický soubor `frontend/index.html` — žádný npm, žádný build.
Gradle ho přibalí serveru mezi statické zdroje, takže **`bootRun` spustí obojí naráz**
a stačí otevřít <http://localhost:8080>. Stránka mluví na stejný origin, takže se
v tomhle režimu neřeší CORS.

Panel **Komunikace se serverem** ukazuje každý request a odpověď tak, jak jdou po drátě —
včetně chybových `ApiErrorResponse`. Je to nejrychlejší způsob, jak zjistit, kde se
tvoje odpověď liší od kontraktu. Šipky <kbd>&larr;</kbd> <kbd>&rarr;</kbd> hýbou robotem.

Volitelně jde frontend pustit i na vlastním portu:

```bash
./gradlew frontend    # první volný z 5500, 4200, 8081, 5555
```

Pak je to skutečný cross-origin požadavek, který funguje díky hotové
`config/WebCorsConfiguration`. Vlastní port: `./gradlew frontend -PfrontendPort=9999`
(ten origin si pak přidej do `patrol.cors.allowed-origins` v `application.yaml`).

Doporučené pořadí práce:

1. `CircularLinkedList` — začni prázdným seznamem, pak jedním prvkem, teprve potom více prvků.
2. Rozjeď `CircularLinkedList*Test` do zelena. Teprve pak pokračuj dál.
3. `PatrolService` — překládá operace aplikace na operace nad seznamem.
4. `PatrolController` + `ApiExceptionHandler` — přesný HTTP kontrakt.
5. Dodaný frontend jako finální integrační test.

Spustit jen jednu skupinu testů:

```bash
./gradlew test --tests "*CircularLinkedList*"
./gradlew test --tests "*PatrolService*"
./gradlew test --tests "*Controller*"
```

---

## Datová struktura

`CircularList<T>` je rozhraní, `CircularLinkedList<T>` je tvoje implementace.

```
Entrance ⇄ Shelf A ⇄ Charging ⇄ Dispatch
    ↑         current                 ↓
    └─────────────────────────────────┘
```

**Povinná pravidla, která testy kontrolují reflexí i nad zdrojovým kódem:**

* prvky žijí ve vlastních uzlech — `ArrayList`, `MutableList`, `HashMap`, pole ani jiná
  hotová kolekce se uvnitř `CircularLinkedList.kt` nesmí objevit;
* seznam má pole `first`, `last`, `current`, uzel má `data`, `next`, `previous`;
* `last.next === first` a `first.previous === last` vždy;
* po každé operaci musí platit `node.next.previous === node` pro každý uzel;
* na prázdném seznamu jsou `first`, `last` i `current` `null`;
* odebraný uzel už není z `first` dosažitelný;
* `iterator()` začíná vždy u `first`, projde seznam **právě jednou** a skončí;
* iterátor je fail-fast: strukturální změna během iterace vyhodí `ConcurrentModificationException`.

Sémantika operací je popsaná v KDoc rozhraní `CircularList`. Čti ho, testy ho berou doslova.
Zejména:

| Operace | Chování |
|---|---|
| `addLast` | přidá za `last`; nad prázdným seznamem prvek je `first`, `last` i `current`; `current` jinak nehýbe |
| `addAfterCurrent` | vloží hned za `current`; když je `current` zároveň `last`, nový prvek se stane `last`; `current` nehýbe |
| `current` / `next` / `previous` / `removeCurrent` | nad prázdným seznamem vyhodí `NoSuchElementException` |
| `next` / `previous` | vrací **nový** aktuální prvek, na okrajích se zacyklí |
| `removeCurrent` | vrátí odebraný prvek, `current` přejde na jeho následníka (z `last` tedy na `first`) |

---

## REST kontrakt

Base URL `http://localhost:8080`, `Content-Type: application/json`.

| HTTP | Endpoint | Request | Success | Status |
|---|---|---|---|---|
| GET | `/api/patrol` | — | `PatrolStateResponse` | 200 |
| POST | `/api/checkpoints` | `CheckpointCreateRequest` | `PatrolStateResponse` | 201 |
| POST | `/api/patrol/next` | — | `PatrolStateResponse` | 200 / 409 |
| POST | `/api/patrol/previous` | — | `PatrolStateResponse` | 200 / 409 |
| DELETE | `/api/checkpoints/current` | — | `PatrolStateResponse` | 200 / 409 |

Pravidla, na kterých frontend stojí:

* každá úspěšná mutace vrací **celý nový stav**, ne jen změněný checkpoint;
* `checkpoints` jde vždy od `first` po `last`, nikdy nezačíná u `current`, nikdy není `null`
  (prázdná trasa je `[]`);
* `current` je objekt, nebo `null` u prázdné trasy;
* `id` generuje server, klient ho neposílá;
* `priority` je pouze `LOW`, `NORMAL`, `HIGH`;
* v odpovědi se nikdy neobjeví interní uzly ani reference `next` / `previous`;
* nevalidní vstup je 400 s `ApiErrorResponse`, nikdy 500;
* operace nad prázdnou trasou je 409 s `error: "PATROL_EMPTY"`, nikdy 500.

Přesné chybové hlášky, které testy očekávají:

```
400  Checkpoint name must not be blank
400  Checkpoint description must not be blank
400  Checkpoint priority must be one of LOW, NORMAL, HIGH
409  Cannot move to next checkpoint because patrol route is empty
409  Cannot move to previous checkpoint because patrol route is empty
409  Cannot remove current checkpoint because patrol route is empty
```

---

## Co dělají jednotlivé testovací třídy

| Třída | Co hlídá |
|---|---|
| `CircularLinkedListBehaviourTest` | veřejné chování seznamu, všechny hraniční případy |
| `CircularLinkedListIteratorTest` | iterátor projde právě jeden cyklus a zastaví, fail-fast |
| `CircularLinkedListStructureTest` | reflexí: uzly, obousměrnost, uzavřený kruh, anchory |
| `CircularLinkedListSourceRulesTest` | zákaz hotových kolekcí přímo ve zdrojovém kódu |
| `CircularLinkedListRandomizedTest` | tisíce náhodných operací proti referenční implementaci |
| `PatrolServiceTest` | doménová logika a doménové chyby místo pádů |
| `PatrolServiceStructureTest` | reflexí: service opravdu drží trasu ve vlastním `CircularList` |
| `PatrolControllerTest` | HTTP kontrakt izolovaně, service je mock |
| `PatrolApiIntegrationTest` | celý běžící server od HTTP až po datovou strukturu |

> Když se testy nečekaně **zaseknou**, skoro jistě máš nekonečnou smyčku v `iterator()`
> nebo rozbitý kruh. JUnit má nastavený 60s timeout, takže dostaneš `TimeoutException`.

## Definition of Done

* [ ] `./gradlew test` je celé zelené
* [ ] `CircularLinkedList` nepoužívá hotovou kolekci jako úložiště
* [ ] seznam je cyklický a obousměrně zřetězený
* [ ] `current` / `next` / `previous` / `add` / `remove` fungují včetně hraničních případů
* [ ] iterátor skončí po jednom cyklu
* [ ] `PatrolService` používá vlastní seznam
* [ ] REST API odpovídá kontraktu
* [ ] dodaný frontend na <http://localhost:8080> funguje
