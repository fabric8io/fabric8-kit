## fabric8-build

This project contains various building blocks for the fabric8 developer toolbox, especially for our [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) and [fabric8-maven-plugin](https://github.com/fabric8io/fabric8-maven-plugin).

Actually it contains the following abstractions which has been extracted from both projects:

* **Generator** framework for automatically generating Docker images by examining project information.
* **Enricher** framework for creating and enhancing Kubernetes and OpenShift resource descriptors.
* **Profile** combining the configuration for generators and enrichers
* **Resource configuration** model objects for a simplified configuration of Kubernetes and OpenShift resource descriptors.
* **Image configuration** model objects for modeling Docker image configuration as it it used in the docker-maven-plugin.

One intention of extracting these parts from the originating plugins is also to separate Maven related and non-Maven related functionality so that the non-Maven parts can be reused for other build systems and IDEs like Gradle or Eclipse. Some thin adapter Maven specific modules like [fabric8-build-enricher-maven](enricher/maven/pom.xml)  and [fabric8-build-generator-maven](generator/maven/pom.xml) are provided as glue to get to the Maven specific build information like the project's coordinates.


By moving out common parts it will be now also be possible for the [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) to benefit from the generator framework for zero-config creation of Docker images.

### Roadmap

* [x] Extract enricher framework from fabric8-maven-plugin
* [x] Extract Image configuration model from docker-maven-plugin
* [x] Extract Resource configuration from fabric8-maven-plugin
* [ ] Extract Generator framework from fabric8-maven-plugin
* [ ] Extract Profile handling from fabric8-maven-plugin
* [ ] Extract Spring Boot generators, enricher and watcher in a Github repo `fabric8-build-spring-boot`
* [ ] Extract Vert.x generator and enricher in a Github repo `fabric8-build-vertx`
* [ ] Extract Wildfly Swarm generator and enricher in a Github repo `fabric8-build-wildfly-swarm`
* [ ] Extract all other generators into `fabric8-build-generator`
* [ ] Extract all other enrichers into `fabric8-build-enricher`, separate here between Maven specific and non-Maven specific enrichers
* [ ] Switch docker-maven-plugin to use this image config model
* [ ] Add generator functionality to docker-maven-plugin
* [ ] Switch fabric8-maven-plugin to use this resource config model
* [ ] Switch fabric8-maven-plugin to use enricher, generators, profiles from here
