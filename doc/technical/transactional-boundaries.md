# Transactional Boundaries

The accounting module is designed with following goals:

- Allow for isolated used as a micro service
- Allow for embedding in another service container

Each of this approaches presents advantages and drawbacks.

## Microservice

Using the accounting module as an isolated service runtime allows for clean separation between service components. 

But this operational mode brings the complexity of cutting transaction boundaries. So each request to the accounting module will have to be considered a proper transaction.

Having spited transaction boundaries will force the designer team find another way of enforcing consistency through the whole application landscape. View the fact that this accounting module dela only with book keeping functionality.


## Embedded Lib

Embedding the account module into the product module will allow us keep a common transaction boundary between the consuming product module and the account module.

This will also remove the whole transactional complexity while modeling the interaction between the accounting module and the product module.

Additionally, using this mode can allow for a natural extension of each domain model, by allowing a accounting extension module to run in the same transaction context with the core module.     