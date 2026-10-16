| CSV Column | Ontology Property | Entity Class | Subject Generation | Datatype |
| --- | --- | --- | --- | --- |
| fname | ex:firstname | ex:Person | Use a unique identifier for each person and append it to the base URI (e.g., <http://example.org/#person1>) | xsd:string |
| lname | ex:lastname | ex:Person | Use the same unique identifier as in the fname column and append it to the base URI | xsd:string |
| amount | ex:owes | ex:Person | Use the same unique identifier as in the fname column and append it to the base URI | xsd:double |
