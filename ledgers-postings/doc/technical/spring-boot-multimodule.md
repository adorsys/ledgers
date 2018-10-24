# Multimodule Spring Service

This page will be used to analyze how to run the accounting module embedded inside another module. We are dealing with following question.

1- Is it possible to run a spring boot application inside another runtime without moving it's Application-Class away?

2- Most services are accessible over http. How do i design an abstraction layer that allow an embedding module to directly access the service layer?
  - IT is meaningful to put all functionality in the services and user the controller only as protocol layer
  - Is it meaningful to prevent deployment of controller and only allow access only thru the consuming module?
  	- In this case we might also consider developing two separated modules: 1 services module and a rest wrapper.
  	- Then the service module can directly be embedded into the consuming module without the need of activating those Rest services.
  	
3- It might be meaningful to distinguish between export interfaces, and journal entry end point:
  - In a clean design, the journal entry end point will have to be fully protected by the embedding product module to prevent the proliferation of unqualified postings.   