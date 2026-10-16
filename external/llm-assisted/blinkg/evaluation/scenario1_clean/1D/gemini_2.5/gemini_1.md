| CSV Column | Ontology Property | Entity Class | Subject Generation | Datatype |
| :--- | :--- | :--- | :--- | :--- |
| `fname` | `ex:firstname` | `ex:Person` | `ex:person_{fname}_{lname}` | `xsd:string` |
| `lname` | `ex:lastname` | `ex:Person` | `ex:person_{fname}_{lname}` | `xsd:string` |
| `amount` | `ex:owes` | `ex:Person` | `ex:person_{fname}_{lname}` | `xsd:double` |
