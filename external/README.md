# External repositories

Public repositories relevant to building knowledge graphs from structured data (CSV/Excel, relational databases, JSON/XML), kept here as **git submodules pinned to a specific commit** for later analysis and consolidation. Their code, history and licenses stay with the upstream projects; this repo only records which commit we looked at.

See [`docs/kg-from-structured-data.md`](../docs/kg-from-structured-data.md) for the research behind this selection and [`docs/kg-pipeline-design.md`](../docs/kg-pipeline-design.md) for how they fit together into one pipeline.

## Getting the code

```bash
# everything (shallow)
git submodule update --init --depth 1

# just one
git submodule update --init --depth 1 external/rdf-engines/morph-kgc

# move a submodule to the latest upstream commit on its tracked branch
git submodule update --remote external/rdf-engines/morph-kgc
```

Pins were taken on 2026-10-16.

## Index

### `rdf-engines/`: structured data → RDF
| Path | Upstream | Why it's here |
|---|---|---|
| `morph-kgc` | [morph-kgc/morph-kgc](https://github.com/morph-kgc/morph-kgc) | Python RML/R2RML engine with the widest source support (SQL, CSV/Excel/Parquet, JSON/XML, pandas, Snowflake, Databricks). Primary candidate. |
| `rmlmapper-java` | [RMLio/rmlmapper-java](https://github.com/RMLio/rmlmapper-java) | Reference RML processor (Java). |
| `sdm-rdfizer` | [SDM-TIB/SDM-RDFizer](https://github.com/SDM-TIB/SDM-RDFizer) | Python RML engine optimised for large joins and duplicates. |
| `carml` | [carml/carml](https://github.com/carml/carml) | Embeddable Java RML library. |
| `rmlstreamer` | [RMLio/RMLStreamer](https://github.com/RMLio/RMLStreamer) | Streaming RML on Apache Flink. |
| `ontop` | [ontop/ontop](https://github.com/ontop/ontop) | Virtual KG: SPARQL over a live relational DB via R2RML, no copying. |
| `sparql-anything` | [SPARQL-Anything/sparql.anything](https://github.com/SPARQL-Anything/sparql.anything) | Query CSV/JSON/XML/Excel directly with SPARQL; mapping-free alternative. |

### `mapping-tools/`: writing and translating mappings
| Path | Upstream | Why it's here |
|---|---|---|
| `yarrrml-parser` | [RMLio/yarrrml-parser](https://github.com/RMLio/yarrrml-parser) | YARRRML → RML/R2RML compiler (JS). |
| `yatter` | [citiususc/yatter](https://github.com/citiususc/yatter) | YARRRML ↔ RML/R2RML translator (Python). |
| `linkml` | [linkml/linkml](https://github.com/linkml/linkml) | Shared schema as YAML; generates OWL, SHACL, JSON Schema and Pydantic from one source of truth. |
| `linkml-map` | [linkml/linkml-map](https://github.com/linkml/linkml-map) | YAML-declared transformations between data models, incl. TSV/CSV. |
| `awesome-kgc-tools` | [kg-construct/awesome-kgc-tools](https://github.com/kg-construct/awesome-kgc-tools) | Curated catalogue of KG-construction tools from the W3C KG-Construct group. |

### `property-graph/`: Neo4j
| Path | Upstream | Why it's here |
|---|---|---|
| `neosemantics` | [neo4j-labs/neosemantics](https://github.com/neo4j-labs/neosemantics) | RDF ↔ Neo4j bridge (n10s); lets one RML pipeline feed both stores. |
| `apoc` | [neo4j/apoc](https://github.com/neo4j/apoc) | `apoc.load.jdbc/json/xml/xls` procedures for loading structured sources. |

### `llm-assisted/`: LLMs that design the model or write the mapping
| Path | Upstream | Why it's here |
|---|---|---|
| `mcp-neo4j` | [neo4j-contrib/mcp-neo4j](https://github.com/neo4j-contrib/mcp-neo4j) | MCP servers; the data-modeling server lets an LLM agent design a graph model and generate ingestion Cypher. |
| `neo4j-graphrag-python` | [neo4j/neo4j-graphrag-python](https://github.com/neo4j/neo4j-graphrag-python) | Official Neo4j KG-builder SDK (text-focused; useful for free-text columns). |
| `neo4j-runway` | [a-s-g93/neo4j-runway](https://github.com/a-s-g93/neo4j-runway) | CSV → LLM data model → Neo4j ingestion. **Archived Dec 2025**; kept as a reference design. |
| `llm4vkg` | [HomuraT/LLM4VKG](https://github.com/HomuraT/LLM4VKG) | IJCAI 2025: LLMs bootstrap ontology and Ontop mappings over a relational DB. |
| `autorml` | [dtai-kg/AutoRML](https://github.com/dtai-kg/AutoRML) | Auto-generates YARRRML/RML for tables from semantic table annotations. |
| `kg-pipeline-llm4rml` | [Vehnem/kg-pipeline](https://github.com/Vehnem/kg-pipeline) | Zero-shot LLM → RML experiments with a parse-and-repair loop (see `experiments/llm4rml`). |
| `blinkg` | [citiususc/blinkg](https://github.com/citiususc/blinkg) | Benchmark with gold mappings for LLM-generated KG mappings. |

### Gap-fillers: pipeline stages the KG tools above don't cover
| Path | Upstream | Why it's here |
|---|---|---|
| `entity-resolution/splink` | [moj-analytical-services/splink](https://github.com/moj-analytical-services/splink) | Probabilistic record linkage at scale on DuckDB, Spark or Athena; matches the same entity across sources. Primary ER candidate. |
| `entity-resolution/dedupe` | [dedupeio/dedupe](https://github.com/dedupeio/dedupe) | Active-learning fuzzy matching and deduplication; good when there's little labelled data. |
| `validation/pyshacl` | [RDFLib/pySHACL](https://github.com/RDFLib/pySHACL) | Runs the SHACL shapes generated from the LinkML schema against each source's graph. |
| `stores/qlever` | [ad-freiburg/qlever](https://github.com/ad-freiburg/qlever) | SPARQL engine built for very large graphs (claims hundreds of billions of triples on one machine). Pick for scale. |
| `stores/oxigraph` | [oxigraph/oxigraph](https://github.com/oxigraph/oxigraph) | Lightweight embeddable SPARQL store (Rust, Python bindings). Pick for dev, tests and small deployments. |
| `orchestration/dagster` | [dagster-io/dagster](https://github.com/dagster-io/dagster) | Asset-based orchestration: run "per source: map → validate → replace named graph" with lineage. |
| `profiling/ydata-profiling` | [ydataai/ydata-profiling](https://github.com/ydataai/ydata-profiling) | Profiles tables (keys, nulls, correlations) to feed the LLM mapping drafter. The package was renamed to `fg-data-profiling`; same repo. |

Deliberately left out: `neo4j-labs/llm-graph-builder` (unstructured sources only), `apache/hop` (large general-purpose ETL platform), `kuzudb/kuzu` (being archived).
