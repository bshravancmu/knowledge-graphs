|CSV Column|Ontology Property|Entity Class|                              Subject Generation                               |Datatype|
|:--------:|:---------------:|:----------:|:-----------------------------------------------------------------------------:|:------:|
|fname|ex:firstname|ex:Person|            "Concatenate fname and lname prefix with 'ex:Person_'"             |xsd:string|
|lname|ex:lastname|ex:Person|     "Concatenate fname and lname                prefix with 'ex:Person_'"     |xsd:string|
|amount|ex:owes|ex:Person| "Concatenate fname and lname                        prefix with 'ex:Person_'" |xsd:double|
