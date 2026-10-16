# One pipeline from the collected repos: a mature, complete and accurate KG from big, diverse tables

**Version 2.0** · 16 Oct 2026

| Version | Date | Changes |
|---|---|---|
| 2.0 | 16 Oct 2026 | Reviewed v1 stage by stage and added concrete improvements for every repo under `external/`. Added a **reference-data layer** built on public financial-services standards (FIBO, FINOS CDM, ISO 20022, GLEIF LEI, ISO 20275 ELF, ISO 10383 MIC, ISO 4217 / 3166, FpML, ACTUS, XBRL, BODS, FollowTheMoney). Rebuilt entity resolution around standard identifiers, with Splink anchored on GLEIF. Added schema matching, data-quality gates, provenance and lineage, link prediction for completeness, and a measurable definition of "mature, complete, accurate". Added 36 new repos, each checked on GitHub on 16 Oct 2026. |
| 1.0 | 16 Oct 2026 | First design: LinkML schema → LLM-drafted YARRRML → Morph-KGC / SDM-RDFizer / RMLStreamer / Ontop → ER → SHACL → RDF store + Neo4j projection. |

**Assumption (unchanged):** many heterogeneous tables (CSV/Excel/Parquet exports plus several relational databases), from different sources with overlapping entities, at large scale (tens of millions of rows and up). **New in v2:** the domain is financial services, so parties, accounts, instruments, trades, payments and ownership are the core entity types. The repos referred to are under `external/` in this repo. The research survey is in [`kg-from-structured-data.md`](kg-from-structured-data.md).

---

## 1. What v1 got right, and what it was missing

v1's core idea still holds. The codebases stay separate and act as stages of one pipeline, joined by one shared schema, one mapping per source and one stable ID rule per entity type. RDF named graphs are the system of record and Neo4j is a projection. LLMs draft mappings and deterministic engines execute them.

Reviewing it against the goal of a *mature, complete and accurate* graph shows seven gaps:

| # | Gap in v1 | Why it matters | Fix in v2 |
|---|---|---|---|
| 1 | The schema was written from scratch | 30 teams would still argue about what a "counterparty" is; nothing tied our terms to industry definitions | Anchor the LinkML schema to **FIBO** classes and **CDM** types; code lists come from ISO standards (§3, §4) |
| 2 | Stable IDs relied on "shared business keys" without saying which | In finance, the keys already exist and are public: LEI, BIC, ISIN, MIC, FIGI | An identifier hierarchy per entity type, backed by GLEIF's open mapping files (§5) |
| 3 | Entity resolution was "an ER tool for the rest" | Matching is where most accuracy is lost | A three-tier ER design: deterministic on identifiers, probabilistic with Splink linked against GLEIF, then human review with a resolver graph (nomenklatura pattern) (§5) |
| 4 | No schema matching step | The LLM drafter had no hints about which columns in different sources mean the same thing | Valentine plus OAK lexical matching produce candidate alignments, stored as **SSSOM** (§4, stage 2) |
| 5 | Validation came only after mapping (SHACL) | Bad source data reached the mapping engine and failed late | Data-quality gates on the raw tables (Great Expectations / Soda / Frictionless), then SHACL on the graph (stages 0 and 6) |
| 6 | No provenance or lineage beyond named graphs | Auditors and regulators ask "where did this fact come from?" | PROV-O metadata per named graph, RML-star annotations for per-triple confidence, OpenLineage events from Dagster (stage 7) |
| 7 | "Mature, complete, accurate" was not defined | Nothing could be measured, so nothing could improve | KPIs with targets per entity type, computed on every run (§7) |

---

## 2. The v2 pipeline

```
 sources (CSV / Excel / Parquet / Postgres / ... / ISO 20022 XML / FpML / XBRL filings)
   │
 0 │ land, profile, quality gate ........ ydata-profiling, great_expectations | soda-core, frictionless-py,
   │                                      datacontract-cli (per-source contract)
   ▼
 R │ REFERENCE-DATA LAYER (built once, refreshed on its own schedule, own named graphs)
   │   ontology .......................... FIBO (Release modules only, extracted with ROBOT) + OMG LCC
   │   canonical trade/event model ....... FINOS CDM (Rune DSL), ACTUS terms
   │   payments vocabulary ............... ISO 20022 data dictionary + external code sets
   │   identifiers + golden entities ..... GLEIF LEI L1/L2, BIC↔LEI, ISIN↔LEI, MIC (ga-group/iso10383), OpenFIGI
   │   code lists ........................ ISO 4217, 3166, 10962 CFI, 20275 ELF (lenu)
   ▼
 1 │ shared schema ...................... LinkML, every class/slot bound to a FIBO / CDM IRI
   │                                      schema-automator to bootstrap from tables; gen-owl, gen-shacl
   │                                      same model in Neo4j via mcp-neo4j data-modeling (OWL import)
   ▼
 2 │ schema matching .................... valentine (column↔column), OAK + LLM (column↔FIBO term)
   │                                      results as SSSOM (sssom-py), reviewed like code
   │ mapping draft ...................... LLM writes YARRRML from profile + SSSOM + schema
   │                                      (patterns: llm4vkg, autorml, kg-pipeline/llm4rml)
   │ compile + test ..................... yatter, rml-test-cases in CI, blinkg metrics for the drafter
   ▼
 3 │ harmonise .......................... RML-FNML / Morph-KGC Python UDFs; linkml-map pre-step;
   │                                      ISO code-list lookups from layer R
   ▼
 4 │ materialise ........................ morph-kgc (default) | sdm-rdfizer (joins) | rmlstreamer (streams)
   │                                      rmlmapper-java (tie-breaker) | ontop (virtual, live DBs)
   │ domain parsers in front of RML ..... prowide-iso20022 / pyiso20022 (payments XML), CDM ingest
   │                                      functions (FpML → CDM JSON), Arelle (XBRL → xBRL-JSON/CSV)
   ▼
 5 │ entity resolution .................. T1 identifiers (LEI/BIC/ISIN/FIGI via GLEIF maps)
   │                                      T2 splink, link-only against GLEIF golden copy (zingg at Spark scale)
   │                                      T3 review queue + resolver graph (nomenklatura pattern), dedupe for small sets
   │                                      → canonical-ID crosswalk + owl:sameAs graph; silk/LIMES for RDF↔RDF links
   ▼
 6 │ validate ........................... pySHACL / Jena SHACL with shapes generated from LinkML; FIBO-derived shapes
   ▼
 7 │ load + provenance .................. QLever (scale) | Oxigraph (dev) | Jena Fuseki (SHACL + TDB2)
   │                                      PROV-O per named graph, RML-star per-triple confidence, OpenLineage
   │                                      Neo4j projection via neosemantics; GDS for clustering and similarity
   ▼
 8 │ complete + use ..................... pykeen link prediction → suggested edges for review
   │                                      SPARQL / Cypher / neo4j-graphrag-python / mcp-neo4j cypher
   ▼
 Q │ quality scorecard (every run) ...... completeness, accuracy, consistency, freshness KPIs (§7)

 orchestration: dagster (one asset per source graph, asset checks = quality gates + SHACL)
```

---

## 3. Using the financial-services standards

Each standard plays one or more of four roles:

- **Reference ontology**: classes and properties our schema binds to.
- **Canonical definitions**: the agreed meaning of a term, used in the data dictionary and in LLM prompts.
- **Identifier source**: a public key that makes records from different sources merge without fuzzy matching.
- **Code list / golden data**: reference values and authoritative entity records to validate and link against.

| Standard | Repo (checked 16 Oct 2026) | Role | How it plugs into the pipeline |
|---|---|---|---|
| **FIBO** (EDM Council, OMG) | [edmcouncil/fibo](https://github.com/edmcouncil/fibo), ~706★, MIT, last commit 15 Oct 2026, quarterly releases | Reference ontology, canonical definitions | Bind LinkML classes to FIBO IRIs, e.g. `LegalEntity` → `fibo-be-le-lp:LegalEntity`, `LEIRegisteredEntity` → `fibo-be-le-lei:LEIRegisteredEntity`. Use only modules marked `hasMaturityLevel Release` (FIBO's *Production* build), not Provisional. Extract the slice we need with ROBOT (`robot extract --method BOT`) because the full ontology is large. FIBO's `skos:definition` texts become our data dictionary and go into the LLM drafter's prompt. FIBO already ships `FND/Accounting/ISO4217-CurrencyCodes.rdf`, `SEC/Equities/EquityCFIClassificationIndividuals.rdf` (ISO 10962), `BE/LegalEntities/LEIEntities.rdf` and an `ACTUS/` mapping, so we reuse those IRIs rather than minting our own. |
| **FINOS CDM** (ISDA / ISLA / ICMA) | [finos/common-domain-model](https://github.com/finos/common-domain-model), ~281★, Community Specification License 1.0, commits daily (8.0.0-dev); language: [finos/rune-dsl](https://github.com/finos/rune-dsl), ~49★, Apache-2.0 | Canonical model for trades, products, lifecycle events, legal agreements; party identifiers | Use CDM as the **canonical shape for trade and event sources**. CDM ships ingestion functions written in Rune (`ingest-fpml-confirmation-*.rosetta`) that turn FpML into CDM objects, and synonym sources for FpML, FIX, ISO 20022 and DTCC. Pipeline: source → CDM JSON (via the CDM Java or Python package) → RML JSONPath mapping → RDF. CDM's `PartyIdentifierTypeEnum` (BIC, LEI, MIC) and `EntityIdentifierTypeEnum` match our identifier hierarchy in §5. Rune has no OWL generator, so the CDM → FIBO alignment (e.g. CDM `Party` ↔ FIBO `LegalEntity`, CDM `InterestRatePayout` ↔ FIBO DER classes) is hand-written once as an SSSOM file. That last point is my design choice, not something CDM provides. |
| **ISO 20022** | No official GitHub; message XSDs, MDRs and the e-Repository (data dictionary) are free downloads from [iso20022.org](https://www.iso20022.org/iso-20022-message-definitions). Parsers: [prowide/prowide-iso20022](https://github.com/prowide/prowide-iso20022), ~236★, Apache-2.0, last commit 23 Sep 2026; [phoughton/pyiso20022](https://github.com/phoughton/pyiso20022), MIT, v1.6.2 Jan 2026 (pacs, pain, camt, admi, colr, head) | Canonical definitions for payments, accounts, financial institutions; external code sets | Payment messages (pacs.008, camt.053, pain.001) are XML sources. Morph-KGC and RMLMapper read XML via XPath, so map them directly, or parse with pyiso20022 / prowide first when the XML is messy. Feed the XSD for each message type to the LLM drafter as the schema for that source; it is far more precise than a data sample. Use the data dictionary's business components (Party, Account, FinancialInstitution, Agent roles such as Debtor, Creditor, DebtorAgent) as canonical definitions. Load the ISO 20022 External Code Sets (purpose codes, category purpose and so on) as SKOS concept schemes in layer R. Alignment to FIBO is an SSSOM file we maintain. |
| **LEI / GLEIF** (ISO 17442) | Data: free daily Golden Copy (CSV/XML/JSON) and API, CC0. Ontologies: L1, L2, RA, ELF, published as RDF at [gleif.org/ontology](https://www.gleif.org/en/lei-data/semantic-representation-of-the-lei/lei-model-in-rdf-resource-description-framework) (not on GitHub). Tools: [Sociovestix/lenu](https://github.com/Sociovestix/lenu), ~24★, CC0, built with GLEIF; [ggravlingen/pygleif](https://github.com/ggravlingen/pygleif) (API client, last commit 20 Sep 2026) | Identifier source; golden entity records; parent/child relationships | **The backbone of party ER.** Load the Golden Copy L1 (who is who) and L2 (who owns whom) into layer R as their own named graphs. Use GLEIF's published mapping files (BIC↔LEI, ISIN↔LEI, MIC↔LEI, OpenCorporates↔LEI) as deterministic crosswalks in ER tier 1. Use L1 legal name, legal address, jurisdiction and ELF code as the reference side of Splink link-only matching in tier 2. Use L2 for corporate hierarchy edges instead of deriving them from internal data. |
| **ISO 20275 ELF** (entity legal forms) | ELF code list from GLEIF; [Sociovestix/lenu](https://github.com/Sociovestix/lenu) | Code list; ER feature | Run lenu over legal names in sources that lack a legal-form column. It predicts the ELF code per jurisdiction, so "Acme GmbH" and "Acme AG" stop matching each other. The ELF code becomes a Splink comparison column. |
| **ISO 10383 MIC** | [ga-group/iso10383](https://github.com/ga-group/iso10383), ~2★, last commit 12 Oct 2026 | Identifier source, code list | Already RDF, in FIBO's `fibo-fbc-fct-mkti` namespace, with history and FpML business centres. Load it directly into layer R. |
| **ISO 4217, 3166, 10962 CFI** | Inside FIBO (above) and OMG LCC (imported by FIBO) | Code lists | Bind LinkML enums to these IRIs with `meaning:`, so `currency: USD` becomes a FIBO currency IRI and SHACL can check the value is a real code. |
| **FIGI** (OMG / Bloomberg) | [OpenFIGI/api-examples](https://github.com/OpenFIGI/api-examples), last commit 1 Jul 2025 | Identifier source for instruments | Call the OpenFIGI mapping API in tier 1 ER to turn ISIN, CUSIP, SEDOL and tickers into one FIGI per instrument. Cache the results in layer R. |
| **FpML** (ISDA) | No GitHub; schemas from fpml.org | Source format for OTC derivatives | Don't map FpML directly. Ingest it through CDM's FpML ingestion functions so derivatives land in one canonical shape. |
| **ACTUS** | [actusfrf/actus-dictionary](https://github.com/actusfrf/actus-dictionary), ~20★, CC BY-SA 4.0, last commit 23 Mar 2026; [actusfrf/actus-service](https://github.com/actusfrf/actus-service) | Canonical contract terms (loans, bonds, swaps) | Use the JSON dictionary of contract types and terms as canonical attribute names when mapping loan and contract tables. FIBO's `ACTUS/ACTUSContractTermMapping.rdf` links those terms to FIBO, so we get the ontology binding for free. |
| **XBRL** | [Arelle/Arelle](https://github.com/Arelle/Arelle), ~235★, Apache-2.0, commits daily | Source format for financial statements | Convert filings to xBRL-JSON or xBRL-CSV with Arelle, then map with RML. Entity identifiers in filings are often LEIs (ESEF requires them), which feed tier 1 ER. |
| **BODS** (beneficial ownership) | [openownership/data-standard](https://github.com/openownership/data-standard), ~81★, v0.4 (May 2024), last commit 2 Sep 2026 | Canonical model for ownership and control | Use BODS's statement pattern (entity, person, ownership-or-control statement with share ranges and dates) as the template for ownership edges below the level GLEIF L2 covers. |
| **FollowTheMoney** | [opensanctions/followthemoney](https://github.com/opensanctions/followthemoney), ~102★, MIT, last commit 5 Oct 2026 (moved from `alephdata/`); [opensanctions/nomenklatura](https://github.com/opensanctions/nomenklatura), ~259★, MIT; [opensanctions/yente](https://github.com/opensanctions/yente) | Entity model and ER toolkit for people, companies, sanctions | FtM has an RDF/OWL export and a tested matching stack. Use nomenklatura's **resolver graph** design (judgements: same, different, unsure; connected components pick the canonical ID) for our ER tier 3. Use yente with the OpenSanctions data to link parties to sanctions and PEP lists. Check the OpenSanctions data licence before commercial use. |

**Licence note.** FIBO (MIT), GLEIF data (CC0) and lenu (CC0) carry no restrictions. CDM is under the Community Specification License, and ACTUS's dictionary is CC BY-SA, which means attribution and share-alike on derived dictionaries. ISO 20022 material is free to download under ISO 20022's terms of use. FpML has its own public licence. Read each licence before redistributing derived artefacts.

---

## 4. Stage by stage, with what to change in each repo's use

### Stage 0. Land, profile, quality gate
- **ydata-profiling (v1 kept, use more).** Export the profile as JSON, not HTML. Keep key candidates, null rates, distinct counts and top values, and pass them to the LLM drafter. Run it on every refresh and diff profiles to spot drift. That drift detection is new.
- **New: [great-expectations/great_expectations](https://github.com/great-expectations/great_expectations)** (last commit 16 Oct 2026) or **[sodadata/soda-core](https://github.com/sodadata/soda-core)** (16 Oct 2026). Write source-level expectations: keys unique, LEI matches `^[A-Z0-9]{18}[0-9]{2}$` with a valid ISO 7064 check digit, currency in ISO 4217. Failing rows go to a quarantine table instead of the graph. Pick one tool; Soda is lighter, Great Expectations has more built-in checks.
- **New: [frictionlessdata/frictionless-py](https://github.com/frictionlessdata/frictionless-py)** (15 Oct 2026) to validate raw CSV/Excel structure (encoding, headers, types) before anything else.
- **New: [datacontract/datacontract-cli](https://github.com/datacontract/datacontract-cli)** (14 Oct 2026). Each source owner signs a YAML contract (schema, semantics, SLA). The contract is the input that tells us when a mapping must change.

### Layer R. Reference data (new)
Build once, refresh on each standard's own schedule (FIBO quarterly, GLEIF daily, ISO 20022 yearly), each in its own named graph such as `<urn:ref:gleif:l1:2026-10-16>`.
- **New: [ontodev/robot](https://github.com/ontodev/robot)** (1 Oct 2026) extracts the FIBO modules we use, merges imports, runs reasoner checks and produces one release file for our schema.
- **New: [INCATools/ontology-access-kit](https://github.com/INCATools/ontology-access-kit)** (OAK, 15 Oct 2026) gives lexical search over FIBO labels and synonyms. It drives column-to-term matching in stage 2 and supplies retrieval context for the LLM.

### Stage 1. Shared schema
- **linkml (use more).** Set `class_uri` and `slot_uri` to FIBO or CDM IRIs, and add `exact_mappings` / `close_mappings` where we refine a term. Bind enums to ISO code IRIs with `meaning:`. Generate OWL, SHACL, JSON Schema and Pydantic. The Pydantic models give the thin `pipeline/` layer typed objects for free.
- **New: [linkml/schema-automator](https://github.com/linkml/schema-automator)** (13 Oct 2026) induces a draft LinkML schema from a table or a database. Use it to bootstrap each source's local view, then align that view to the shared schema.
- **mcp-neo4j data-modeling (as v1).** Import the generated OWL so Neo4j labels equal RDF classes.

### Stage 2. Schema matching, then mapping draft
- **New: [delftdata/valentine](https://github.com/delftdata/valentine)** (~126★, Apache-2.0, 14 Oct 2026). Runs COMA, Cupid, Similarity Flooding and distribution-based matchers across source tables. It finds that `cpty_lei` in source A and `LEI_CODE` in source B are the same column before any LLM is involved.
- **New: [mapping-commons/sssom-py](https://github.com/mapping-commons/sssom-py)** (~64★, MIT, 14 Oct 2026) and the [SSSOM spec](https://github.com/mapping-commons/sssom). Every alignment (column ↔ FIBO slot, source code ↔ ISO code, CDM type ↔ FIBO class) is a row with a confidence score and a justification such as `semapv:LexicalMatching` or `semapv:ManualMappingCuration`. Reviewers approve SSSOM rows, and the LLM drafter receives approved rows as hard constraints. This makes the review loop auditable.
- **LLM drafter (v1 repos, used more precisely).** From **LLM4VKG**, take relational pattern detection (FK, join table, subclass) and run it before matching. From **AutoRML**, take column type annotation first, then rule generation. From **kg-pipeline/llm4rml**, take the parse → error → repair loop, extended in v2 with "run on the sample, then SHACL-validate, then repair".
- **yatter (use both directions).** Compile YARRRML → RML for execution, and convert RML → YARRRML when reviewing an engine's or a tool's output.
- **New: [RMLio/rml-test-cases](https://github.com/RMLio/rml-test-cases)** (11 Mar 2026). Run the conformance suite in CI against the pinned versions of Morph-KGC and SDM-RDFizer. A failing case means that engine can't be trusted with that feature.
- **blinkg (as v1).** Score the drafter each time the prompt or model changes, and record precision and recall in the scorecard.
- **Ontology-level alignment, when an internal ontology already exists.** **New: [ernestojimenezruiz/logmap-matcher](https://github.com/ernestojimenezruiz/logmap-matcher)** (7 Oct 2026) aligns it to FIBO at scale and checks the alignment stays logically coherent. Output goes to SSSOM.

### Stage 3. Harmonise
- **Morph-KGC Python UDFs** are easier to test than generic FNML functions. Put LEI check-digit validation, ISO code lookups and unit conversion in one tested Python module.
- **linkml-map (as v1)** for heavier reshaping into clean Parquet.

### Stage 4. Materialise
- **morph-kgc.** Use RML-star output to attach `prov:wasDerivedFrom <source row>` and an extraction confidence to each generated triple. Run one Morph-KGC call per source, each as a Dagster asset.
- **sdm-rdfizer, rmlstreamer, rmlmapper-java, ontop**: roles as in v1. With **Ontop**, use the FIBO slice as the ontology, so live databases answer FIBO-shaped SPARQL.
- **Domain parsers in front of RML (new):** prowide-iso20022 or pyiso20022 for payments, the CDM Java or Python distribution for FpML, Arelle for XBRL. Each turns a hard format into clean JSON or XML that a short RML mapping can handle.

### Stage 5. Entity resolution (rebuilt)
See §5.

### Stage 6. Validate
- **pySHACL (as v1)** for per-source graphs up to a few million triples.
- **New: [apache/jena](https://github.com/apache/jena)** (15 Oct 2026) `shacl` CLI and Fuseki for larger graphs. [TopQuadrant/shacl](https://github.com/TopQuadrant/shacl) (30 Jun 2026) is a second implementation to cross-check results when a shape behaves unexpectedly.
- Add **FIBO-derived shapes** for the rules FIBO states as OWL restrictions, such as "an LEI identifies exactly one LEIRegisteredEntity". OWL restrictions are not checked under the open-world assumption, so restating them as SHACL is what enforces them.

### Stage 7. Load and provenance
- **QLever** (scale) and **Oxigraph** (dev) as in v1. **Jena Fuseki with TDB2** is a middle option when we need SHACL, GeoSPARQL and updates in one server.
- **PROV-O metadata per named graph** records source system, extract time, mapping file git SHA, engine and version.
- **New: [OpenLineage/OpenLineage](https://github.com/OpenLineage/OpenLineage)** (16 Oct 2026). Dagster emits run lineage, so "which run produced this graph" is queryable outside the graph too.
- **neosemantics.** Set `handleVocabUris: "MAP"` with prefix mappings so Neo4j labels stay readable. Import `owl:sameAs` clusters as one node with all source IDs as properties, not as many nodes joined by `SAME_AS` edges, so Cypher users never see duplicates.
- **New: [neo4j/graph-data-science](https://github.com/neo4j/graph-data-science)** (8 Oct 2026). Weakly Connected Components over match edges gives cluster IDs. Node Similarity flags likely duplicates that the ER stage missed.

### Stage 8. Complete and use
- **New: [pykeen/pykeen](https://github.com/pykeen/pykeen)** (16 Oct 2026). Train link prediction on the graph and propose missing edges, such as a missing parent company or a missing instrument issuer. Proposals go to the review queue and are never asserted automatically. Accepted ones are counted in the completeness KPI.
- **neo4j-graphrag-python** and **mcp-neo4j cypher** as in v1. Give the Text2Cypher retriever the FIBO definitions of each label, so questions phrased in business terms find the right part of the graph.

---

## 5. Entity resolution, anchored on standards

ER decides accuracy more than any other stage. v2 resolves in three tiers, in order, and records every decision.

**Identifier hierarchy per entity type** (the first available identifier wins and becomes the IRI):

| Entity type | IRI key, in order | Crosswalk source |
|---|---|---|
| Legal entity | LEI → BIC (via BIC↔LEI) → national registry ID (via OpenCorporates↔LEI) → internal ID | GLEIF mapping files |
| Financial institution / agent | BIC → LEI | GLEIF BIC↔LEI |
| Instrument | FIGI → ISIN → CUSIP / SEDOL (mapped to FIGI via OpenFIGI) | OpenFIGI, GLEIF ISIN↔LEI for the issuer |
| Trading venue | MIC | ga-group/iso10383 |
| Trade / event | CDM trade identifier (UTI where reported) | CDM model |
| Person | Internal ID only; match in tier 2 | none public |

**Tier 1. Deterministic.** Records that share a hierarchy key merge by construction. The IRI template for every entity type lives in the LinkML schema, e.g. `https://kg.example/legal-entity/lei/{lei}`.

**Tier 2. Probabilistic.**
- **Splink (v1 repo, used differently).** Run it in **link-only mode against the GLEIF Golden Copy** rather than deduplicating sources against each other. Every party without an LEI is matched to the nearest GLEIF record on normalised name, ELF code (from lenu), legal-address country and city, and registration-authority ID. Matches above the upper threshold inherit the LEI. Pairs between the two thresholds go to tier 3. Train m/u probabilities with EM, turn on term-frequency adjustment for names, and keep the trained model in git.
- **[zinggAI/zingg](https://github.com/zinggAI/zingg)** (~1.3k★, AGPL-3.0, 16 Oct 2026) is the alternative if the volumes are already on Spark. AGPL needs legal review before use in a product.
- **dedupe (v1 repo)** suits small, messy entity types (internal counterparties without identifiers) where a person can label a few hundred pairs.
- **Person matching** never uses GLEIF. Match on name, date of birth and address with Splink, plus yente against sanctions lists when required.

**Tier 3. Review and resolver graph.** Store every judgement (same, different, unsure, with who and when) in a judgements table, following nomenklatura's design. Connected components over "same" edges give the canonical ID, and "different" edges block bad transitive merges. The canonical-ID crosswalk is fed back into the mappings as a lookup on the next run, so a decision is made once.

**RDF↔RDF linking (new).** **[silk-framework/silk](https://github.com/silk-framework/silk)** (~251★, 12 Oct 2026) or **[dice-group/LIMES](https://github.com/dice-group/LIMES)** (last commit Jun 2024, quieter) link our graph to external RDF such as GLEIF's RDF and Wikidata, producing `owl:sameAs` with link specifications we can version.

**Measured, not assumed.** Keep a labelled set of at least a few hundred pairs per entity type. Report ER precision and recall on it every run (§7).

---

## 6. Who does what (v2)

| Repo | Stage | Role | Status |
|---|---|---|---|
| ydata-profiling | 0 | JSON profiles for the drafter, drift detection | in `external/` |
| great_expectations or soda-core | 0 | Source data-quality gates | **new** |
| frictionless-py | 0 | Raw file structure validation | **new** |
| datacontract-cli | 0 | Per-source data contracts | **new** |
| fibo | R, 1 | Reference ontology and definitions | **new** |
| common-domain-model, rune-dsl | R, 4 | Canonical trade/event model and FpML ingestion | **new** |
| prowide-iso20022, pyiso20022 | 4 | ISO 20022 parsing | **new** |
| lenu, pygleif | R, 5 | ELF prediction, GLEIF API access | **new** |
| iso10383 | R | MIC codes as RDF | **new** |
| OpenFIGI api-examples | R, 5 | Instrument identifier mapping | **new** |
| actus-dictionary | 1 | Canonical contract terms | **new** |
| Arelle | 4 | XBRL parsing | **new** |
| openownership data-standard | 1 | Ownership model pattern | **new** |
| followthemoney, nomenklatura, yente | 5 | Resolver-graph pattern, sanctions linking | **new** |
| robot, ontology-access-kit | R, 2 | FIBO extraction, term search | **new** |
| linkml, linkml-map, schema-automator | 1, 3 | Schema, transforms, schema induction | linkml in `external/`, schema-automator **new** |
| valentine, sssom-py, logmap-matcher | 2 | Schema and ontology matching, mapping registry | **new** |
| mcp-neo4j | 1, 8 | Neo4j model alignment, agent querying | in `external/` |
| llm4vkg, autorml, kg-pipeline, blinkg | 2 | LLM drafter patterns and scoring | in `external/` |
| yatter / yarrrml-parser | 2 | YARRRML ↔ RML | in `external/` |
| rml-test-cases | 2 | Engine conformance in CI | **new** |
| morph-kgc, sdm-rdfizer, rmlstreamer, rmlmapper-java, ontop | 4 | Engines | in `external/` |
| splink, dedupe, zingg | 5 | Probabilistic ER | splink and dedupe in `external/`, zingg **new** (optional) |
| silk, LIMES | 5 | RDF link discovery | **new** |
| pySHACL, jena, TopQuadrant shacl | 6 | SHACL | pySHACL in `external/`, others **new** |
| qlever, oxigraph, jena fuseki | 7 | Stores | qlever and oxigraph in `external/` |
| neosemantics, apoc, graph-data-science | 7 | Neo4j projection and analytics | GDS **new** |
| OpenLineage | 7 | Lineage | **new** |
| dagster | all | Orchestration | in `external/` (link only) |
| pykeen | 8 | Link prediction for completeness | **new** |
| neo4j-graphrag-python | 8 | GraphRAG | in `external/` |
| sparql-anything, carml, neo4j-runway, awesome-kgc-tools | dev | Exploration and reference | in `external/` |

---

## 7. What "mature, complete, accurate" means here

Compute these on every run and publish a scorecard per source and per entity type. Targets are starting points to tune once real numbers exist.

| Dimension | KPI | How it's computed | Starting target |
|---|---|---|---|
| Accuracy: mapping | Triple precision and recall on gold samples | Expected-output tests per source (`tests/`), blinkg metrics for the drafter | ≥ 0.98 precision |
| Accuracy: ER | Pairwise precision and recall | Labelled pair set per entity type | ≥ 0.98 precision, ≥ 0.95 recall for legal entities |
| Accuracy: reference | Share of identifiers that pass validation | LEI check digit and GLEIF lookup, ISIN check digit, MIC and ISO code membership | 100% valid; invalid ones quarantined |
| Completeness: coverage | Share of legal entities with an LEI | Count after tier 1 and 2 | Track trend; report the residual |
| Completeness: attributes | Share of mandatory slots filled | SHACL `minCount` violations ÷ entities | ≥ 0.99 |
| Completeness: relations | Parent links present vs GLEIF L2 | Compare our ownership edges with L2 | ≥ 0.95 of L2 edges present |
| Consistency | SHACL violations per million triples | pySHACL / Jena report | 0 Violation-severity results at load |
| Freshness | Age of each named graph vs its source SLA | PROV-O timestamp vs data contract | within SLA |
| Traceability | Share of triples with provenance | RML-star annotations and graph-level PROV | 100% at graph level |

**Maturity levels** for the program as a whole:
1. **Pilot**: first slice live, gold tests per source, SHACL gate.
2. **Managed**: all sources mapped, ER tiers 1–3 running, scorecard published every run.
3. **Reference-grade**: every class bound to FIBO or CDM, all identifiers crosswalked through GLEIF/OpenFIGI, KPIs at target for two consecutive quarters.

---

## 8. Repository layout (v2)

```
ontology/
  schema/              LinkML (source of truth), with FIBO/CDM IRIs
  generated/           OWL, SHACL, JSON Schema, Pydantic
  fibo-slice/          ROBOT extract of the FIBO Release modules we use
reference/             loaders for GLEIF, MIC, ISO code lists, ISO 20022 code sets, OpenFIGI cache
alignments/            SSSOM files: column↔FIBO, code↔ISO, CDM↔FIBO
mappings/<source>/     YARRRML + sample rows + expected output + data contract
er/                    Splink models, labelled pairs, judgements table schema
pipeline/              Dagster assets: gate → map → validate → ER → load; scorecard
tests/                 mapping regression tests, rml-test-cases runner
external/              the upstream repos
docs/                  research, this design, and the HTML report
```

## 9. Suggested first slice (updated)

Pick **counterparties** across 2–3 internal sources plus the GLEIF Golden Copy.

1. Extract FIBO `BE/LegalEntities` and `FBC/FunctionalEntities` with ROBOT. Write the LinkML `LegalEntity` class bound to FIBO, with the identifier hierarchy from §5.
2. Load GLEIF L1, L2 and the BIC↔LEI map into layer R.
3. Profile and gate the sources, run Valentine across them, review the SSSOM file, then LLM-draft and review the three YARRRML mappings.
4. Materialise with Morph-KGC, run tier 1 ER, then Splink link-only against GLEIF, then review the uncertain middle band.
5. Validate with SHACL, load into Oxigraph, project into Neo4j, and publish the first scorecard: LEI coverage, ER precision on 300 labelled pairs, SHACL violations.

This proves the standards-anchored ID rule, the review loop and the scorecard. Trades via CDM and payments via ISO 20022 come next, each as its own slice.

## Sources

- FIBO: https://github.com/edmcouncil/fibo, https://spec.edmcouncil.org/fibo/
- FINOS CDM: https://github.com/finos/common-domain-model, mapping docs https://cdm.finos.org/docs/mapping
- Rune DSL: https://github.com/finos/rune-dsl
- ISO 20022 message definitions: https://www.iso20022.org/iso-20022-message-definitions
- GLEIF LEI in RDF: https://www.gleif.org/en/lei-data/semantic-representation-of-the-lei/lei-model-in-rdf-resource-description-framework
- GLEIF open-source page: https://www.gleif.org/en/about/open-source
- pyiso20022 on PyPI: https://pypi.org/project/pyiso20022/
- All other repos: GitHub links inline. Last-commit dates were read with `git clone --depth 1` on 16 Oct 2026; star counts come from the GitHub page the same day and are shown only where they were checked.
