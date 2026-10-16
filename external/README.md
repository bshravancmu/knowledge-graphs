# External repositories

Public repositories relevant to building knowledge graphs from structured data (CSV/Excel, relational databases, JSON/XML), collected for later analysis and consolidation.

Most are **source snapshots**: a plain copy of the upstream repository's files at the commit listed below, without its git history. Each snapshot keeps its upstream LICENSE file, and the upstream license applies to that folder. Three are kept as git submodules instead (see the table below for why).

See [`docs/kg-from-structured-data.md`](../docs/kg-from-structured-data.md) for the research behind this selection and [`docs/kg-pipeline-design.md`](../docs/kg-pipeline-design.md) for how they fit together into one pipeline.

## Snapshots and pins (taken 2026-10-16)

| Folder | Upstream | Commit | Form |
|---|---|---|---|
| `entity-resolution/dedupe` | [dedupeio/dedupe](https://github.com/dedupeio/dedupe) | [`3f61e7910291`](https://github.com/dedupeio/dedupe/tree/3f61e79102910bd355e920a2df7e44c14c9cb247) | source copy |
| `entity-resolution/splink` | [moj-analytical-services/splink](https://github.com/moj-analytical-services/splink) | [`12aee3fd1fac`](https://github.com/moj-analytical-services/splink/tree/12aee3fd1facd505464a62a6e91f5831f6c10558) | source copy |
| `llm-assisted/autorml` | [dtai-kg/AutoRML](https://github.com/dtai-kg/AutoRML) | [`f396524d249f`](https://github.com/dtai-kg/AutoRML/tree/f396524d249ff47732d50b438bf36cdc6b6ee371) | source copy |
| `llm-assisted/blinkg` | [citiususc/blinkg](https://github.com/citiususc/blinkg) | [`99f0b5c4b4aa`](https://github.com/citiususc/blinkg/tree/99f0b5c4b4aafddf5a25939b3f297ef5d874db14) | source copy |
| `llm-assisted/kg-pipeline-llm4rml` | [Vehnem/kg-pipeline](https://github.com/Vehnem/kg-pipeline) | [`eb3a211d07a1`](https://github.com/Vehnem/kg-pipeline/tree/eb3a211d07a1a710f31294f353b780e794cedad9) | source copy |
| `llm-assisted/llm4vkg` | [HomuraT/LLM4VKG](https://github.com/HomuraT/LLM4VKG) | [`f107bd9b737a`](https://github.com/HomuraT/LLM4VKG/tree/f107bd9b737aa37c69ad55c4b9f3435137122b32) | submodule (link only): upstream has no license, so it can't be redistributed |
| `llm-assisted/mcp-neo4j` | [neo4j-contrib/mcp-neo4j](https://github.com/neo4j-contrib/mcp-neo4j) | [`1c7ad0793b5f`](https://github.com/neo4j-contrib/mcp-neo4j/tree/1c7ad0793b5f06b42b9c44f855c23eb2064c3bb7) | source copy |
| `llm-assisted/neo4j-graphrag-python` | [neo4j/neo4j-graphrag-python](https://github.com/neo4j/neo4j-graphrag-python) | [`029ebd93a1bb`](https://github.com/neo4j/neo4j-graphrag-python/tree/029ebd93a1bbd87ab1daa57e2ed79396ce129c2c) | source copy |
| `llm-assisted/neo4j-runway` | [a-s-g93/neo4j-runway](https://github.com/a-s-g93/neo4j-runway) | [`7920da5754db`](https://github.com/a-s-g93/neo4j-runway/tree/7920da5754db7bc0273ff9b33072b3a99b1d1a26) | source copy |
| `mapping-tools/awesome-kgc-tools` | [kg-construct/awesome-kgc-tools](https://github.com/kg-construct/awesome-kgc-tools) | [`96632cad8cfe`](https://github.com/kg-construct/awesome-kgc-tools/tree/96632cad8cfe570c74b77537f5e361364d67948a) | submodule (link only): upstream has no license (it is a link list anyway) |
| `mapping-tools/linkml-map` | [linkml/linkml-map](https://github.com/linkml/linkml-map) | [`76db207d832d`](https://github.com/linkml/linkml-map/tree/76db207d832d07c1e51fc9fd2e2d0c50ee3cb57e) | source copy |
| `mapping-tools/linkml` | [linkml/linkml](https://github.com/linkml/linkml) | [`a23121e5d9ab`](https://github.com/linkml/linkml/tree/a23121e5d9ab6a5e70ba4c3ecc4f753bb4212fb7) | source copy |
| `mapping-tools/yarrrml-parser` | [RMLio/yarrrml-parser](https://github.com/RMLio/yarrrml-parser) | [`93a76eee1f26`](https://github.com/RMLio/yarrrml-parser/tree/93a76eee1f26a06d1095d802062c831826365e03) | source copy |
| `mapping-tools/yatter` | [citiususc/yatter](https://github.com/citiususc/yatter) | [`0b40dff6234f`](https://github.com/citiususc/yatter/tree/0b40dff6234fbbee868444d855576ef7626cbf5e) | source copy |
| `orchestration/dagster` | [dagster-io/dagster](https://github.com/dagster-io/dagster) | [`c97898c3e4a8`](https://github.com/dagster-io/dagster/tree/c97898c3e4a8a3cc5e32fcc2ca97aea810342ed9) | submodule (link only): too large (~350 MB) for a general-purpose platform we only orchestrate with |
| `profiling/ydata-profiling` | [ydataai/ydata-profiling](https://github.com/ydataai/ydata-profiling) | [`98b1abaf5b62`](https://github.com/ydataai/ydata-profiling/tree/98b1abaf5b629d31fcfe6b0cb85c4ca9782e21a0) | source copy |
| `property-graph/apoc` | [neo4j/apoc](https://github.com/neo4j/apoc) | [`4288f52f6087`](https://github.com/neo4j/apoc/tree/4288f52f6087287c767d47cc90a3359f98cc7346) | source copy |
| `property-graph/neosemantics` | [neo4j-labs/neosemantics](https://github.com/neo4j-labs/neosemantics) | [`d5cfd2ada13b`](https://github.com/neo4j-labs/neosemantics/tree/d5cfd2ada13b4573e87d47bebd83931cf5402dfe) | source copy |
| `rdf-engines/carml` | [carml/carml](https://github.com/carml/carml) | [`f58ee44b1ccc`](https://github.com/carml/carml/tree/f58ee44b1ccc162b6b65f180ba3eb00ffcdb5e86) | source copy |
| `rdf-engines/morph-kgc` | [morph-kgc/morph-kgc](https://github.com/morph-kgc/morph-kgc) | [`544cd60943f3`](https://github.com/morph-kgc/morph-kgc/tree/544cd60943f3671cd96931e8a5b47717da0abc35) | source copy |
| `rdf-engines/ontop` | [ontop/ontop](https://github.com/ontop/ontop) | [`964dbb79431f`](https://github.com/ontop/ontop/tree/964dbb79431fb958fce9de0784424f7c0e35f9bf) | source copy |
| `rdf-engines/rmlmapper-java` | [RMLio/rmlmapper-java](https://github.com/RMLio/rmlmapper-java) | [`86e961d73aee`](https://github.com/RMLio/rmlmapper-java/tree/86e961d73aee622b76761b4f40802873a40800e5) | source copy |
| `rdf-engines/rmlstreamer` | [RMLio/RMLStreamer](https://github.com/RMLio/RMLStreamer) | [`1f4dbad3e8c9`](https://github.com/RMLio/RMLStreamer/tree/1f4dbad3e8c96ee412e31b2ff64b597e5b745074) | source copy |
| `rdf-engines/sdm-rdfizer` | [SDM-TIB/SDM-RDFizer](https://github.com/SDM-TIB/SDM-RDFizer) | [`b8c71fdd96b4`](https://github.com/SDM-TIB/SDM-RDFizer/tree/b8c71fdd96b45e84850097bfb6815a7a49a9e477) | source copy |
| `rdf-engines/sparql-anything` | [SPARQL-Anything/sparql.anything](https://github.com/SPARQL-Anything/sparql.anything) | [`7da0dbb36829`](https://github.com/SPARQL-Anything/sparql.anything/tree/7da0dbb3682907f22841181e9c544a7de457b34a) | source copy |
| `stores/oxigraph` | [oxigraph/oxigraph](https://github.com/oxigraph/oxigraph) | [`2283f42a7eb4`](https://github.com/oxigraph/oxigraph/tree/2283f42a7eb4ae40330a9c9ce7ee9585ff72daee) | source copy |
| `stores/qlever` | [ad-freiburg/qlever](https://github.com/ad-freiburg/qlever) | [`c3aef76f516f`](https://github.com/ad-freiburg/qlever/tree/c3aef76f516fdf4deb77c61f27b755cfea8aac1d) | source copy |
| `validation/pyshacl` | [RDFLib/pySHACL](https://github.com/RDFLib/pySHACL) | [`469cca7a22a0`](https://github.com/RDFLib/pySHACL/tree/469cca7a22a078b36c167c1e8dadecf5e5ec6c75) | source copy |

To fetch the three submodules: `git submodule update --init --depth 1`.

**Changes from upstream:** the snapshots are byte-for-byte copies except for three large data files over 50 MB that were left out to stay within GitHub's file-size guidance. All three are test or benchmark data, not source: `stores/qlever/e2e/scientist-collection.zip`, `rdf-engines/sdm-rdfizer/kgcw_2023_challenge/results/results.zip` and `mapping-tools/linkml/tests/data/hp.dill`. Upstream repositories that have their own nested submodules (ontop, qlever, oxigraph, kg-pipeline) still list them in their `.gitmodules`, but those nested folders were not copied.

To refresh a snapshot, re-copy the upstream tree at a newer commit and update its row above.

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
