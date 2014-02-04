# co(de)factory
(working title)

## Authors
- Karsten Schmidt, postspectacular

## Description

Co(de)factory is a social collaboration piece designed to fulfill a multifaceted creative vision which addresses several aspects of contemporary artistic production. The project aims to bring creators together and allow them to individually and collaboratively design (and later fabricate) physical artifacts, using a set of custom developed in-browser 3d modeling tools.

Collaboration takes place both in the museum and remotely through a browser, on either a desktop, tablet or mobile. A high quality, next generation, open source 3D printer will form the central element of the piece in the gallery. Fabricating a single artefact each day, using light flashes to expose UV hardening resin, objects will slowly grow out from a pool of liquid in a spectacular way. In addition to the printing unit, the exhibit will contain all previously fabricated objects, thus resulting in dozens of objects over the period of the exhibition and reflecting the learning process and changing aesthetic preferences over time. The responsibility of selection and curation of the objects to be printed will be handed to museum visitors, thus creating a potential pressure on designers to achieve or refine a certain aesthetic. This is counterbalanced by the limited options of the available design process. Key aspect of this project is the definition and exploration of such a process, which not only encourages the re-use of ideas (whilst retaining provenance), but can also result in a large number of possible outcomes regardless (or even because) of its limited choices. At the same time, and since this is a public project, this requires breaking new ground to define an user interface which allows for the easy creation and manipulation of the resulting artefacts.

## Link to Prototype

Links to prototypes (visual, interactive or even purely technical ones) will be added to this section as soon as they're ready for public consumption (aiming for at least one per week). Most likely each will also be accompanied by a little blog post on the main [DevArt](http://g.co/devart) site and I will also create an issue for each of these prototypes to collect general feedback from interested people.

## Links to External Libraries

As part of this project commission I'm hoping to polish several new libraries for initial public release. These libraries are playing a key role in this project, however after completely throwing over designs many times, some of these libs have been undergoing heavy refactoring for >2.5 years, but have been used successfully in production and are still kept in a private repo for another few days/week until I'm happy with a public soft launch. I'm very keen to get these out, but want to get it right and make it all useful to others...

### Tweeny

[tweeny](http://code.thi.ng/tweeny) is a small Clojure library for tweening generic values (e.g. defined as key frames). It can operate on nested data structures and is completely independent from any rendering aspects (key frames aren't just useful for visual anim). It's also ClojureScript compatible.

### Luxor

[luxor](http://code.thi.ng/luxor) is a small(ish) Clojure DSL to describe & compile complete scene graphs for [LuxRender](http://luxrender.net), incl. 3d mesh/light generation & export, material definition, volumes etc. Already published and used in production to render 60sec animation (1800 scenes / frames), but requires...

### Yet-to-be-named

Successor to my [toxiclibs](http://toxiclibs.org) project for Clojure & ClojureScript (JS). By now in it's 4th iteration and still far from finished, this lib is a not a port, but a full re-imagination in approaching geometry problems, enabled by the many new found virtues offered by its new host language(s).

### Linked data kit

Leightweight, semantic graph (RDF) framework with pluggable storage backends, SPARQL-like query engine, rule-based inferencing...

A selection of other 3rd party libraries (potentially) used for this project will be discussed in further posts...

Happy coding! :)
