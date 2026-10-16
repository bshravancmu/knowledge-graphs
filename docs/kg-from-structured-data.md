# Building knowledge graphs from raw structured data

_Research notes, 16 Oct 2026. Scope: tabular files (CSV/Excel), relational databases, and JSON/XML; targets are both RDF graphs and property graphs (Neo4j-style); covers classic mapping methods and newer LLM-assisted ones._

## TL;DR

- **Structured data is the easy case for KG construction.** You don't need extraction (NER, relation extraction); you need a *mapping* from columns/keys to graph nodes, edges and properties, plus identity rules (how a row becomes a stable node ID).
- **RDF target:** write declarative mappings in **RML** (or the friendlier **YARRRML**) and run them with **Morph-KGC** (Python) or **RMLMapper** (Java). For a live relational DB you can skip materialising and use **Ontop** to expose it as a virtual KG over SPARQL.
- **Property-graph target (Neo4j):** model the graph, then load with `LOAD CSV` / `neo4j-admin import` / **APOC** (JDBC, JSON, XML) or **Apache Hop** for repeatable pipelines.
- **LLMs are now mostly used to write the mapping, not to produce the triples.** An LLM proposes the schema/ontology alignment and the RML or Cypher; a deterministic engine then executes it. This keeps the output reproducible and auditable. Repos below cover each stage.

## 1. The general pipeline

1. **Profile the source.** Tables, columns, types, primary/foreign keys, JSON paths. FKs and join tables are the strongest hints for edges.
2. **Choose or design a target schema.**
   - RDF: reuse an ontology (schema.org, domain ontologies) or write one; optionally constrain with SHACL.
   - Property graph: a labelled node/relationship model (e.g. drawn in arrows.app).
3. **Write mappings.** For each entity: which rows produce it, how its ID/IRI is built (a template like `http://ex.org/customer/{id}`), which columns become properties, which FKs become relationships.
4. **Execute.** Either *materialise* (generate triples/nodes once, load into a store) or *virtualise* (translate graph queries to SQL at query time).
5. **Clean and link.** Entity resolution / deduplication across sources, validation (SHACL, constraints), provenance.
6. **Iterate** as sources change (incremental mapping is an active research area, e.g. IncRML).

### Classic heuristics for relational → graph (the "direct mapping")
- Table → node type / class; row → node.
- Primary key → node identity.
- Foreign key → relationship (from referencing row to referenced row).
- Pure join table (two FKs, no other data) → many-to-many relationship; with extra columns → relationship properties (property graph) or a reified node (RDF).
- Other columns → properties / literals.

The W3C **Direct Mapping** spec does exactly this automatically; **R2RML** (W3C) lets you customise it, and **RML** generalises R2RML to CSV, JSON and XML.

## 2. RDF route: mapping languages and engines

| Spec | What it's for |
|---|---|
| R2RML (W3C Rec) | Relational DB → RDF |
| RML (W3C KG-Construct CG, modular redesign) | CSV, JSON, XML, DB → RDF; adds functions (FnO/FNML), RDF-star, collections, logical views |
| YARRRML | YAML syntax that compiles to RML/R2RML; much easier to write by hand or with an LLM |
| xR2RML | Adds MongoDB/NoSQL sources |
| SPARQL Anything (Facade-X) | Alternative to mappings: query any file directly with SPARQL `CONSTRUCT` |

Materialise vs virtualise: materialising (Morph-KGC, RMLMapper, SDM-RDFizer) gives you a standalone triple store; virtualising (Ontop) keeps the data in the DB and is better when the source changes constantly or is too big to copy.

## 3. Property-graph route (Neo4j and friends)

- **`LOAD CSV` / `neo4j-admin database import`**: built-in; admin import is the bulk path for very large initial loads.
- **APOC**: `apoc.load.jdbc`, `apoc.load.json`, `apoc.load.xml`, `apoc.load.xls`.
- **Neo4j Data Importer / Aura Import**: GUI that connects to Postgres, MySQL, Snowflake or CSVs (product, not an open-source repo).
- **Apache Hop with the Neo4j plugin**: Neo4j's docs recommend it for large or recurring loads with logging.
- **neosemantics (n10s)**: bridges the two worlds; import RDF (e.g. from an RML pipeline) into Neo4j, or export Neo4j as RDF.
- The older **Neo4j ETL Tool** still appears in docs navigation but is no longer recommended on the relational-import guide.

## 4. LLM-assisted construction from structured data

What LLMs are good at here:
- **Schema → ontology alignment**: matching column names and descriptions to ontology classes/properties, or proposing new ones when none match.
- **Mapping generation**: writing RML/YARRRML or Cypher ingestion scripts from a sample of the data plus the target ontology/model.
- **Graph data modelling**: proposing nodes/relationships from a data dictionary and use cases.
- **Messy cells**: extracting structured values from free-text columns inside an otherwise structured source.

What the research shows so far:
- Zero-shot RML generation works for small, clean cases but needs a validate-and-repair loop (parse the Turtle, feed errors back). In the 2024 KGCW study, Claude 3 Opus did best and most other models produced unusable RML ([paper](https://ceur-ws.org/Vol-3718/paper6.pdf)).
- **LLM4VKG** (IJCAI 2025) combines mapping-pattern detection with retriever/matcher/namer LLM modules to bootstrap Ontop mappings; it reports about 17% higher F1 than BootOX on the RODI benchmark ([paper](https://www.ijcai.org/proceedings/2025/0525.pdf)).
- Hybrid "LLM inside the mapping" (RML rules call an LLM only for free-text fields) beat a pure-LLM pipeline on error rate ([paper](https://ceur-ws.org/Vol-4091/paper54.pdf)).
- **BLINKG** is a benchmark specifically for LLM-generated mappings from (semi)structured data.

Recommended pattern: **LLM writes the mapping → human reviews → deterministic engine executes → SHACL/constraints validate.** Avoid having the LLM emit the triples or nodes row by row: it's slow, costly, and not reproducible.

## 5. GitHub repositories (verified)

Stars are as shown on the GitHub page on 16 Oct 2026. "Last push" is from the ecosyste.ms index on the same day where available; "n/a" means the page didn't show a date.

### 5a. RML / R2RML engines (structured → RDF)

| Repo | What it does | Stars | Activity |
|---|---|---|---|
| [morph-kgc/morph-kgc](https://github.com/morph-kgc/morph-kgc) | Python RML/R2RML/RML-star engine. Widest source support: SQL DBs, CSV/Excel/Parquet, JSON/XML, pandas, Databricks, Snowflake, Neo4j. Best starting point. | ~296 | Very active (pushed ~1 week ago) |
| [RMLio/rmlmapper-java](https://github.com/RMLio/rmlmapper-java) | Reference RML processor (Java). CSV/CSVW, Excel/ODS, JSON, XML, SQL DBs, SPARQL endpoints, web APIs. | ~202 | Active (~2–3 weeks ago) |
| [SDM-TIB/SDM-RDFizer](https://github.com/SDM-TIB/SDM-RDFizer) | Scalable Python RML engine tuned for large joins/duplicates. CSV, JSON, XML, RDB. | ~139 | Very active (~1 week ago) |
| [carml/carml](https://github.com/carml/carml) | Extensible Java RML library (good for embedding in a JVM app). | ~114 | Active (~10 days ago) |
| [anuzzolese/pyrml](https://github.com/anuzzolese/pyrml) | Lightweight Python RML engine. | ~37 | Active (~1 month ago) |
| [RMLio/RMLStreamer](https://github.com/RMLio/RMLStreamer) | Streaming RML on Apache Flink, for continuous or very large inputs. | ~62 | Maintained |
| [ontop/ontop](https://github.com/ontop/ontop) | Virtual KG: query relational DBs via SPARQL using R2RML/Ontop mappings, no copy. Mature and widely used. | ~982 | Active (not archived) |
| [SPARQL-Anything/sparql.anything](https://github.com/SPARQL-Anything/sparql.anything) | Query CSV, JSON, XML, Excel, HTML and more directly with SPARQL; `CONSTRUCT` builds the KG. | ~306 | Active |
| [sparna-git/xls2rdf](https://github.com/sparna-git/xls2rdf) | Excel → RDF (SKOS, instances, SHACL, OWL) via a header-row convention. Good for spreadsheet-maintained vocabularies. | ~29 | Maintained |

### 5b. Mapping authoring helpers

| Repo | What it does | Stars | Activity |
|---|---|---|---|
| [RMLio/yarrrml-parser](https://github.com/RMLio/yarrrml-parser) | Compile YARRRML → RML/R2RML (JS CLI). | ~57 | Active |
| [citiususc/yatter](https://github.com/citiususc/yatter) | Python YARRRML ↔ RML/R2RML translator (also at oeg-upm/yatter). | ~22 | Active |
| [oeg-upm/mapeathor](https://github.com/oeg-upm/mapeathor) | Write mappings in a spreadsheet, export R2RML/RML/YARRRML. | ~32 | Quiet (~12 months) |
| [linkml/linkml](https://github.com/linkml/linkml) + [linkml/linkml-map](https://github.com/linkml/linkml-map) | Define a schema in YAML and generate RDF/OWL/SHACL/JSON-Schema from it; linkml-map transforms TSV/CSV/JSON between models. Strong in biomedical data. | ~609 (linkml) | Active (linkml-map 0.5.4, Aug 2026) |
| [kg-construct/awesome-kgc-tools](https://github.com/kg-construct/awesome-kgc-tools) | Curated list of every KG-construction tool, from the W3C KG-Construct community. | ~86 | Maintained |

### 5c. Property-graph / Neo4j

| Repo | What it does | Stars | Activity |
|---|---|---|---|
| [neo4j/apoc](https://github.com/neo4j/apoc) | Cypher procedures incl. `apoc.load.jdbc/json/xml/xls` for loading structured sources. | ~213 | Active |
| [neo4j-labs/neosemantics](https://github.com/neo4j-labs/neosemantics) | RDF ↔ Neo4j (n10s). Lets you combine an RML pipeline with Neo4j. | ~968 | Maintained |
| [neo4j-labs/rdflib-neo4j](https://github.com/neo4j-labs/rdflib-neo4j) | RDFLib store backed by Neo4j: write triples from Python straight into Neo4j. | ~104 | Active (Oct 2026) |
| [apache/hop](https://github.com/apache/hop) | Open-source ETL with Neo4j plugin; recommended by Neo4j for large or recurring loads. | ~1.4k | Active |

### 5d. LLM-assisted mapping and modelling

| Repo | What it does | Stars | Activity |
|---|---|---|---|
| [neo4j-contrib/mcp-neo4j](https://github.com/neo4j-contrib/mcp-neo4j) | MCP servers; the **data-modeling** server lets an LLM agent design/validate a graph model, import/export Arrows or OWL, and generate ingestion Cypher. The current successor for LLM-driven Neo4j modelling. | ~979 | Very active |
| [HomuraT/LLM4VKG](https://github.com/HomuraT/LLM4VKG) | Code for LLM4VKG (IJCAI 2025): LLMs bootstrap ontology + mappings over a relational DB for Ontop. | ~21 | Research code |
| [dtai-kg/AutoRML](https://github.com/dtai-kg/AutoRML) | Auto-generates YARRRML/RML for tables from semantic table annotations (TorchicTab), then builds the KG. On PyPI (0.1.6, Jan 2026). | ~9 | Research code |
| [Vehnem/kg-pipeline](https://github.com/Vehnem/kg-pipeline) (`experiments/llm4rml`) | Prompts and evaluation for zero-shot LLM → RML with a parse-and-repair loop. | n/a | Research code |
| [pharmaKG/rmlai](https://github.com/pharmaKG/rmlai) | RML + LLM hybrid: LLM only extracts values from free-text fields inside JSON. | 0 | Research code |
| [citiususc/blinkg](https://github.com/citiususc/blinkg) | Benchmark for LLM-generated mappings from (semi)structured data, with gold mappings and metrics. | ~2 | Research code |
| [neo4j/neo4j-graphrag-python](https://github.com/neo4j/neo4j-graphrag-python) | Official Neo4j GraphRAG SDK; `SimpleKGPipeline` builds KGs with an LLM. Text/PDF-focused, but useful when tables include long free-text columns. | ~1.3k | Very active |
| [neo4j-labs/llm-graph-builder](https://github.com/neo4j-labs/llm-graph-builder) | Popular LLM KG builder app. **Unstructured sources only** (PDF, web, video), so not a fit for CSV/DB on its own. | ~5.3k | Very active |

### Avoid or treat with care
- **[a-s-g93/neo4j-runway](https://github.com/a-s-g93/neo4j-runway)**: this did exactly CSV → LLM data model → Neo4j ingestion, but it was **archived Dec 2025** (last PyPI release 0.14.0, Nov 2024). Worth reading for the approach; use mcp-neo4j's data-modeling server instead.
- **[kuzudb/kuzu](https://github.com/kuzudb/kuzu)**: embedded property-graph DB with fast `COPY FROM` CSV/Parquet. Its README now says the project is being archived.
- **Neo4j ETL Tool, D2RQ, Map-On**: older tools, not recommended for new work.

## 6. Suggested starter stack

- **RDF, files + DBs, Python:** YARRRML (LLM-drafted or hand-written) → yatter → Morph-KGC → load into a triple store (GraphDB, Fuseki, Oxigraph) → validate with SHACL.
- **RDF over a live relational DB:** Ontop (optionally bootstrap mappings with the LLM4VKG approach).
- **Neo4j:** design the model with mcp-neo4j-data-modeling (LLM agent) or arrows.app → generate Cypher → `LOAD CSV` / APOC for small to medium, `neo4j-admin import` or Apache Hop for large or recurring loads.
- **Both:** Morph-KGC → RDF → neosemantics into Neo4j, so one mapping feeds both stores.

## Sources
- awesome-kgc-tools list: https://github.com/kg-construct/awesome-kgc-tools
- ecosyste.ms RML topic index (push dates): https://repos.ecosyste.ms/hosts/GitHub/topics/rml
- Neo4j relational-to-graph guide: https://neo4j.com/docs/getting-started/data-import/relational-to-graph-import/
- LLM → RML study (KGCW 2024): https://ceur-ws.org/Vol-3718/paper6.pdf
- LLM4VKG (IJCAI 2025): https://www.ijcai.org/proceedings/2025/0525.pdf
- RML + LLMs for semi-structured data: https://ceur-ws.org/Vol-4091/paper54.pdf
- AutoRML (TGDK): https://drops.dagstuhl.de/entities/document/10.4230/TGDK.4.3.2
- neo4j-runway on PyPI: https://pypi.org/project/neo4j-runway
- Kuzu README: https://cdn.jsdelivr.net/gh/kuzudb/kuzu@master/README.md
