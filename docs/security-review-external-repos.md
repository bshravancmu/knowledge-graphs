# Security review of the vendored repos (`external/`)

Reviewed 2026-10-16 against the snapshots pinned in `external/README.md` (25 source copies; the three submodule links were not reviewed). Read-only: nothing was built or run.

**Scope picked.** Two things:
1. Known vulnerabilities, from OSV-Scanner over every lockfile and manifest (the OSV database includes GitHub Security Advisories and PyPA, RustSec and npm advisories), plus advisories published against the tools themselves.
2. Risk patterns in the source code that matter for how this pipeline uses the tools: user- or LLM-supplied SPARQL, Cypher and RML mappings; loading from arbitrary URLs and files; deserialising untrusted input; and LLM tools that write queries.

Every High finding below was checked by reading the code at the line given. Line numbers refer to the vendored snapshot.

## The short version

The repos are not malicious, and their libraries are mostly fine when **you** write the mapping, query or config. The danger comes when that input is untrusted: a third-party file, or anything an LLM wrote. The pipeline design has LLMs drafting YARRRML mappings and Cypher, so that applies here. Several tools treat a mapping or query as something closer to a program than to data. Treat LLM-drafted mappings and queries as code: review them, or run them in a sandbox with no secrets and no network.

## 1. Be careful: can run commands or leak secrets

| # | Repo | What | Where | Kind |
|---|---|---|---|---|
| 1 | **sparql-anything** | Any SPARQL query can run a shell command. `x-sparql-anything:command=...` is passed to `bash -c`, and no setting turns it off. Its Fuseki server has no auth, listens on all interfaces and enables CORS, so a web page open in your browser could send such a query. | `sparql-anything-model/.../resources/CommandInputService.java:20-43`; `sparql-anything-fuseki/.../Endpoint.java:93-96` | Feature, no off switch |
| 2 | **sparql-anything** | Any query can read local files or any URL (`location=file:///...`, cloud metadata at 169.254.169.254, internal hosts) and get the contents back as triples. | `LocationInputService.java:37-59` | Documented feature |
| 3 | **sdm-rdfizer** | `app.py` runs `os.system("python3 ... /" + config_file)` with the URL path. It listens on 0.0.0.0:4000 with no auth, so anyone who can reach it can run commands. | `app.py:13` | Bug (the Dockerfiles use the CLI instead, so this only matters if you run `app.py`) |
| 4 | **sdm-rdfizer** | The output path from the mapping (`rml:logicalTarget/.../rml:path`) is concatenated into `os.system("mv " + ...)` and `os.system("cp " + ...)` about 100 times. An LLM-written mapping with `rml:path "out.nt.gz; curl x\|sh #"` runs that command. | `rdfizer/rdfizer/__init__.py:2263, 3342, 10076, 10094...` | Bug |
| 5 | **morph-kgc** | In an HTTP-API source, any header value that equals the name of an environment variable is replaced with that variable's value, then sent to whatever URL the mapping names. A mapping can therefore send `OPENAI_API_KEY`, DB passwords or cloud credentials to an attacker's server. | `src/morph_kgc/source/http_api.py:53, 68, 112-114` | By design, no allowlist |
| 6 | **neosemantics (n10s)** | `n10s.rdf.import.fetch` and related procedures call `new URL(url).openConnection()` directly. This bypasses all of Neo4j's URL and file controls, so `file:///`, internal hosts and `jar:` URLs all work, with caller-chosen method, headers and body. | `CommonProcedures.java:152, 184, 200-208` | Bug |
| 7 | **neosemantics (n10s)** | SHACL validator config is read back from a graph node with Java `ObjectInputStream.readObject()` and no class filter. Anyone who can write Cypher can plant the bytes. That means remote code execution in Neo4j if a gadget chain is on the classpath (not confirmed), and a crash at the very least. | `validation/ValidatorConfig.java:54-73, 200-203` | Bug |

## 2. Lock down before deploying (risky defaults)

- **mcp-neo4j-cypher.** The write tool is on by default; pass `--read-only` (`server.py:48, 56`). HTTP and SSE transports have no auth, and the Dockerfile binds to 0.0.0.0. The default password is `"password"`. Even read-only mode still allows `LOAD CSV FROM 'http://...'` and `apoc.load.*`, so a prompt injection can reach the network. The pinned 0.6.0 already has the fix for the published read-only bypass (GHSA-x3cv-r3g3-fpg9) and the DNS-rebinding advisory. **mcp-neo4j-memory** has no read-only mode, and **aura-manager** exposes `delete_instance` with no extra confirmation.
- **neo4j-graphrag-python.** The Text2Cypher guard is solid: it runs `EXPLAIN` and rejects anything that isn't a read. Two gaps remain. Read queries can still use `LOAD CSV` and `apoc.load.*`, and there is no timeout. Also, label and property names taken from LLM-extracted entities are interpolated unescaped into schema queries (`schema.py:555, 707-709`), so a document can plant a label that injects Cypher later.
- **APOC.** File access is safely off by default (`apoc.import.file.enabled=false`), and the XML parsing is hardened. HTTP loads, however, can reach internal hosts unless you set `internal.dbms.cypher_ip_blocklist`. Also, `apoc.cypher.run*` defeats any text-based "is this read-only" filter. Restrict procedures with `dbms.security.procedures.allowlist`.
- **rmlmapper-java / CARML.** The mapping controls all I/O: any local path (including `../`), any JDBC URL with credentials, and any HTTP or SPARQL endpoint. CARML's Saxon XPath also allows `unparsed-text('file:///...')` and `doc('http://...')` inside references. Neither tool can load new Java code from a mapping.
- **Oxigraph `serve`.** It binds to localhost, but SPARQL UPDATE, `LOAD <http...>` and `SERVICE` to any URL are on with no auth, and `--cors` sets `*`. Use `serve-read-only` and leave `--cors` off.
- **QLever.** It always binds to 0.0.0.0. Writes need `--access-token`, but that token is printed in startup logs. `SERVICE` can reach any host until you set `service-allowed-iri-prefixes`.
- **pySHACL HTTP service** (not the library). Clients can make it read local files and URLs and follow `owl:imports`, and CORS is `*`. The library and CLI are fine: imports are off unless you ask for them.
- **linkml-map.** `multi_file_transformer.py:177` hard-codes `unrestricted_eval=True` (asteval, which is not a sandbox), so a malicious transform spec can run code. The default evaluator is hardened.
- **linkml-runtime.** RDF loads switch off TLS verification process-wide while they run (`loaders/rdf_loader.py:95`), so concurrent LLM API calls lose certificate checks during that window.
- **Ontop endpoint.** It listens on all interfaces at 8080 with no auth. It is read-only with no `SERVICE`, but every query becomes SQL run with your DB credentials. Keep `dev` mode off.

## 3. Treat these files as code (unpickled or executed when loaded)

- **dedupe** settings files: `pickle.load` (`api.py:1065`).
- **ydata-profiling** `.pp` reports: `pickle.loads`. Its HTML report also prints column names unescaped, so a CSV header like `<img onerror=...>` becomes XSS when someone opens the report. Both match published advisories (GHSA-cg49-hrj4-3rpr, GHSA-fpvj-m2h6-6wc5, GHSA-2r57-2mrh-ggjv) that list no fixed version. Telemetry is on by default; set `YDATA_PROFILING_NO_ANALYTICS=1`.
- **splink** model JSON: blocking rules and comparisons are raw SQL run in DuckDB or Spark. DuckDB can read files and URLs (`read_csv('/...')`, `COPY ... TO`).
- **autorml**: the opt-in MTab annotator uploads the whole input table to `mtab.kgraph.jp` with `verify=False`. Don't use it on financial data.
- **neo4j-runway** (archived): generated ingest Cypher interpolates LLM-chosen labels unescaped. Review it before running.

## 4. Known vulnerable dependencies (OSV-Scanner)

These are the versions pinned in each repo's lockfile. When you install a tool from PyPI or Maven you get its own resolution, often newer. Treat this table as "upgrade before relying on it", not as proof that a release you install is affected.

| Repo | Notable (CVSS ≥ 9 or relevant) | Total |
|---|---|---|
| mcp-neo4j (4 servers) | fastmcp SSRF/path traversal (GHSA-vv7q-7jx5-f767, 10.0; only in its OpenAPI provider), authlib JWS signature bypass, cryptography buffer overflow, aiohttp/h11 request smuggling, anyio TLS hostname | 127–296 per server |
| neo4j-runway | jinja2 sandbox escape, aiohttp, h11, anyio | 180 |
| neo4j-graphrag-python | authlib `alg: none` bypass, cryptography, anyio, pillow | 134 |
| linkml-map | gitpython (several RCE-class option-injection bugs) | 88 |
| neosemantics | jackson-databind polymorphic-typing bypasses (Java); docs site has old handlebars/convict | 19 (+77 docs) |
| yarrrml-parser | dev tooling only: minimatch/brace-expansion ReDoS, serialize-javascript | 35 |
| rmlstreamer | **postgresql JDBC 42.5.2 SQL injection (GHSA-24rp-q3w6-vc56, 10.0)**, kafka 2.4.1 deserialisation, moquette | 9 |
| ontop | Spring Boot 2.7.18 (end of life) actuator auth bypass in the endpoint; test-only JDBC drivers (Redshift RCE, MySQL) | ~6 per module |
| sdm-rdfizer | mysql-connector-python 8.0.22 connector takeover | 2 |
| splink, oxigraph (python), pyshacl | urllib3 decompression and proxy TLS issues, tornado, ujson DoS | 15–18 |
| oxigraph (Rust) | rustls 0.23.45 low/medium (fixed in 0.23.46) | 5 |

Coverage gaps: APOC (Gradle, no lockfile) and the Maven repos were scanned only for directly declared dependencies, not transitive ones. QLever (C++) has no manifest that the scanner reads. Maven artefacts with no advisories turned up for rmlmapper, CARML, sparql-anything or Ontop themselves. APOC's own past advisories (XXE, path traversal, 2018–2023) are fixed in versions far older than this pin.

## 5. Looked clean

- **yatter**: safe YAML.
- **blinkg**.
- **yarrrml-parser** runtime: it doesn't execute input, but it passes source, JDBC and credential values straight through to the RML it produces.
- **pyshacl** library and CLI.
- **ontop** apart from deployment hardening.
- **morph-kgc** apart from #5: GREL uses a safe AST evaluator, and UDF files come from config, not from the mapping.
- **linkml** core.
- **kg-pipeline-llm4rml**: only minor SPARQL string-building in evaluation code.
- **APOC**'s XML and file-import controls.

No repo uses lxml, unsafe `yaml.load`, `torch.load` or `marshal`.

## What to do, in order

1. Run sparql-anything only in a sandbox (no network, no secrets, not exposed), or reject queries containing `command=` or `location=`.
2. Never feed an LLM-written or third-party mapping to sdm-rdfizer. For morph-kgc, reject `htv:` HTTP sources or run it with an empty environment. More generally, check every mapping's source paths, URLs and output paths against an allowlist before any RML engine runs it.
3. Keep n10s off any Neo4j that receives LLM-generated Cypher, or remove its fetch and validation procedures from the procedure allowlist.
4. Neo4j: use a read-only role for LLM queries, run mcp-neo4j-cypher with `--read-only` over stdio, set `cypher_ip_blocklist`, and restrict `apoc.load.*`, `apoc.cypher.run*` and remote `LOAD CSV`.
5. Stores: `oxigraph serve-read-only`; QLever with `--access-token` and `service-allowed-iri-prefixes`; nothing exposed beyond localhost without an auth proxy in front.
6. Upgrade the flagged lockfile pins (especially the mcp-neo4j servers and the RMLStreamer postgres driver) when you build any of these.
