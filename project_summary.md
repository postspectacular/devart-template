# co(de)factory
(working title)

## Authors
- Karsten Schmidt, postspectacular

## Description
TBD

## Link to Prototype

Links to prototypes (visual, interactive or even purely technical
ones) will be added to this section as soon as they're ready for
public consumption (aiming for at least one per week). Most likely
each will also be accompanied by a little blog post on the main
[DevArt](http://g.co/devart) site and I will also create an issue for
each of these prototypes to collect feedback from interested people.

- Prototype #0 (coming soon)

## Links to External Libraries

As part of this project commission I'm hoping to polish several new
libraries for initial public release. These libraries are playing a
key role in this project, however after completely throwing over
designs many times, some of these libs have been undergoing heavy
refactoring for >2.5 years, but have been used successfully in
production and are still kept in a private repo for another few
days/week until I'm happy with a public soft launch. I'm very keen to
get these out, but want to get it right and make it all useful to
others...

### Tweeny

[tweeny](http://code.thi.ng/tweeny) is a small Clojure library for
tweening of timebased values (e.g. keyframes). It can operate on
nested data structures and is completely independent from any
rendering aspects (keyframes aren't just useful for visual anim). It's
also ClojureScript compatible.

### Luxor

[luxor](http://code.thi.ng/luxor) is a small(ish) Clojure DSL to
describe & compile complete scene graphs for
[LuxRender](http://luxrender.net), incl. 3d mesh/light generation &
export, materials, volume media etc. Already published and used to
render 60sec animation (1800 scenes / frames), but requires...

### TBD Vol 1

Successor to my [toxiclibs](http://toxiclibs.org) project for Clojure
& ClojureScript. In it's 4th iteration and still far from finished,
this lib is a not a port, but a full re-imagination in approach,
enabled by the many new found virtues offered by its new host
language(s).

### TBD Vol. 2

Leightweight, semantic graph (RDF) framework with pluggable backends,
SPARQL-like query syntax (as sexp's), rule-based inferencing... coming
soon!

Happy coding! :)
