# One pipeline from the collected repos: a KG from big, diverse tables

_16 Oct 2026. **Assumption:** many heterogeneous tables (CSV/Excel/Parquet exports plus several relational databases), from different sources with overlapping entities, at large scale (tens of millions of rows and up). The repos referred to are the submodules under `external/` in [PR #1](https://github.com/bshravancmu/knowledge-graphs/pull/1)._

## The idea in one paragraph

Don't merge the codebases. Use them as **stages of one pipeline** held together by three things that live in our repo: **one shared schema**, **one mapping file per source**, and **a stable ID rule for each entity type**. RDF (N-Quads, one named graph per source) is the canonical intermediate format, because every RML engine emits it and it carries provenance for free. Neo4j is a *projection* of that graph for apps and GraphRAG. LLMs are used where they help most: drafting each source's mapping. Engines do the heavy lifting deterministically.

## The recommended pipeline

```
 sources (CSV / Excel / Parquet / Postgres / MySQL / ...)
   │
 0 │ land + profile ............ (gap: lakehouse folder + a profiler)
   ▼
 1 │ shared schema ............. LinkML schema  → OWL + SHACL  (linkml)
   │                             same model as Neo4j labels     (mcp-neo4j data-modeling)
   ▼
 2 │ per-source mapping draft .. LLM writes YARRRML             (llm4vkg, autorml, kg-pipeline/llm4rml)
   │ human review + test ....... gold sample per source         (blinkg for scoring the generator)
   │ compile ................... YARRRML → RML                  (yatter | yarrrml-parser)
   ▼
 3 │ harmonise values .......... units, enums, code lists       (RML-FNML functions, linkml-map)
   ▼
 4 │ materialise at scale ...... default engine                 (morph-kgc)
   │                             join-heavy sources             (sdm-rdfizer)
   │                             streams / cluster scale        (rmlstreamer on Flink)
   │                             conformance reference          (rmlmapper-java)
   │ or virtualise ............. live, fast-changing DBs        (ontop)
   ▼
 5 │ entity resolution ......... shared ID templates first, ER tool for the rest  (gap)
   ▼
 6 │ validate .................. SHACL from step 1              (gap: SHACL runner)
   ▼
 7 │ load ...................... RDF store                      (gap: triple store)
   │                             Neo4j projection               (neosemantics, apoc)
   ▼
 8 │ use ....................... SPARQL / Cypher / GraphRAG     (neo4j-graphrag-python, mcp-neo4j cypher)
```

## Stage by stage

**0. Land and profile.** Copy file sources into one lake folder as Parquet. Morph-KGC reads Parquet and databases directly, so nothing needs reshaping. Profile every table for keys, null rates and candidate join columns. That profile is the LLM's input in step 2. None of the collected repos does profiling (see gaps).

**1. One shared schema.** Write the target model once as a **LinkML** YAML schema and generate OWL (for RML/SPARQL) and SHACL (for step 6) from it. Import that OWL into the **mcp-neo4j data-modeling** server, which accepts OWL Turtle, so the Neo4j labels and relationships match the RDF classes. This step stops 30 sources from inventing 30 meanings of "customer".

**2. One mapping per source, LLM-drafted, human-approved.**
- Give an LLM the shared schema, the table's profile and about 50 sample rows, and ask for **YARRRML**. YARRRML is far easier for models and people to read than raw RML Turtle.
- Reuse the patterns from the research repos. **LLM4VKG** detects relational patterns (FK, join table, subclass) before matching them to ontology terms. **AutoRML** annotates columns semantically before generating rules. **kg-pipeline/llm4rml** has the parse-error → repair loop.
- Compile with **yatter** (Python) or **yarrrml-parser** (JS), then run on the sample rows and diff against a small hand-checked expected output. Once the gold set has a few sources, use **BLINKG**'s metrics to measure how much the generator can be trusted.
- Keep the mappings in git under `mappings/<source>/`. Review them the way code is reviewed.

**3. Harmonise values inside the mapping.** Unit conversion, date formats, code-list lookups and string cleanup belong in RML-FNML functions, which Morph-KGC and RMLMapper both support. Heavier reshaping goes in a **linkml-map** pre-step that writes cleaned Parquet. Don't hand-edit data.

**4. Materialise, choosing the engine per source.**
- **Morph-KGC is the default.** Its mapping-partition design lets it process groups of rules in parallel and keeps memory bounded ([paper](https://semantic-web-journal.net/content/morph-kgc-scalable-knowledge-graph-materialization-mapping-partitions-1)). It also reads the widest set of sources.
- **SDM-RDFizer** handles sources whose mappings join several large tables, since it is built to optimise joins and duplicate removal.
- **RMLStreamer** handles feeds that never stop, or volumes beyond one machine, because it runs the same RML on an Apache Flink cluster.
- **RMLMapper** runs the conformance tests. When engines disagree on a mapping, its output is the tie-breaker.
- **Ontop** covers operational databases that change constantly or are too big to copy. Expose them virtually with the same schema, and query them alongside the materialised graph.
- Write each source to its own named graph (N-Quads). Re-running one source then only replaces that graph, which makes incremental refresh simple.

**5. Entity resolution.** This is the hardest part with many sources, and none of the collected repos solves it.
- **First line of defence:** IRI templates built on shared business keys, such as `/company/{lei}` or `/product/{gtin}`, defined once per entity type in the schema. Records from different sources that share a key then merge automatically.
- **For the rest:** a dedicated ER tool matches records on name, address and other fields, and writes `owl:sameAs` or a canonical-ID crosswalk table. That table can be fed back into the mappings as a lookup.

**6. Validate.** Run the SHACL shapes from step 1 over each source's graph before loading, and fail the run when a source breaks them. This catches mapping regressions when a source changes its columns.

**7. Load.**
- The RDF store is the system of record.
- Neo4j receives a projection through **neosemantics** (n10s), which imports RDF with the same model as step 1. For very large initial loads, export nodes and edges to CSV and use Neo4j's bulk importer. This is my inference from how n10s loads data, not something I benchmarked. **APOC** covers small direct loads and lookups.

**8. Use.** Query with SPARQL on the RDF store and Cypher on Neo4j. **neo4j-graphrag-python** adds retrieval and Q&A over the graph, and the **mcp-neo4j** cypher server lets agents query it. **SPARQL Anything** is the scratchpad for exploring a new source and prototyping its mapping before writing YARRRML.

## Who does what

| Repo | Role in the pipeline | Main path? |
|---|---|---|
| linkml-map | Value harmonisation pre-step (linkml itself for the schema) | yes |
| mcp-neo4j | Keep the Neo4j model aligned with the schema; agent querying | yes |
| llm4vkg, autorml, kg-pipeline | Patterns and prompts for the LLM mapping drafter | yes (as reference code) |
| blinkg | Score the mapping drafter against gold mappings | yes |
| yatter / yarrrml-parser | Compile YARRRML → RML (pick one; yatter fits a Python stack) | yes |
| morph-kgc | Default materialisation engine | yes |
| sdm-rdfizer | Engine for join-heavy sources | when needed |
| rmlstreamer | Engine for streaming or cluster scale | when needed |
| rmlmapper-java | Conformance reference and tie-breaker | testing |
| ontop | Virtual graph over live databases | when needed |
| neosemantics, apoc | Project into Neo4j | yes |
| neo4j-graphrag-python | GraphRAG and Q&A on top | yes |
| sparql-anything | Exploration and prototyping | dev only |
| carml | Alternative engine if we end up embedding in a JVM service | no |
| neo4j-runway | Archived; read its prompts for LLM data-modelling ideas | no |
| awesome-kgc-tools | Catalogue for finding replacements | no |

## Gaps in the original 20 repos (candidates now pinned under `external/` gap-filler folders)
1. **Entity resolution** across sources, for example Splink or dedupe.
2. **A SHACL runner**, for example pySHACL, or the validator built into the chosen triple store.
3. **A scalable triple store with a bulk loader**, for example QLever, Virtuoso, GraphDB or Oxigraph, depending on size and licence.
4. **Orchestration**, for example Dagster or Airflow, to run "per source: map → validate → replace named graph" on a schedule.
5. **Data profiling** to feed the LLM mapping drafter.

## How to consolidate our repo around this
```
ontology/            LinkML schema (source of truth) → generated OWL, SHACL
mappings/<source>/   YARRRML + sample rows + expected output
pipeline/            thin Python orchestration: compile → run engine → validate → load
tests/               mapping regression tests (sample in, expected triples out)
external/            the pinned upstream repos (PR #1)
docs/                research and this design
```
The only code we'd write is the thin `pipeline/` layer and the LLM mapping-drafter prompt. Everything else is configuration of the upstream tools.

## Suggested first slice
Pick 2–3 real sources that share an entity. Write the LinkML schema for that entity, then LLM-draft and review the three mappings. Run them through Morph-KGC into named graphs, validate with SHACL and load into one store. This proves the shared-ID rule and the review loop before scaling out to every source.
