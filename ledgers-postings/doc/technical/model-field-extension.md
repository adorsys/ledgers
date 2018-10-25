# Model Field Extension

This framework will allow consuming modules to have the possibility to extend the data model with additional fields.

This page will be used to document research leading in the direction.

## Alternative 1

- Provide model extension classes that consume the same id like the core accounting classes.
- Design the DAO-Layer to store both model extension and core data classes within the same transaction.
- Design the VO-Layer to offer a getExtension() method on the core object that returns the corresponding model extension object.

## Alternative 2

- Find a way to extend JPA-Object with additional fields at deployment time.
- TODO

## Alternative 3
 