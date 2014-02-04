Below is a diagram to give a better overview of the technical aspects of this project. Apart from key components like the use of GAE and various frontend elements, none of this is set in stone yet and am sure some parts are likely to change as I progress. But this is a good reference to understand (even for myself) what I'm working towards over the coming months.

![System architecture](../project_images/2014-02-05-architecture.jpg "System architecture")

Maybe I should also list some of the main tools & languages I'm using to build this all:

## Emacs & Org-mode

It took me more than two decades to overcome my fear of Emacs, but ever since spending more time in SSH sessions (and needing an editor) and also since ever digging deeper into the world of [Clojure](http://www.creativeapplications.net/tutorials/introduction-to-clojure-part-1/), I quickly came to appreciate its magic and haven't looked back. Together with the [Org-mode](http://orgmode.org) add-on and its [literate programming](https://en.wikipedia.org/wiki/Literate_programming) [tools](http://orgmode.org/worg/org-contrib/babel/intro.html) I (re)discovered true joy in coding.

## Clojure

Speaking of [joy](http://joyofclojure.com/), [Clojure](http://clojure.org) has become my main weapon of choice over the past 3 years and this project too will be largely built in that language. It's not only its unique philosophy, but also its design as an hosted language, which makes it very valuable to me. Via its [ClojureScript](https://github.com/clojure/clojurescript) dialect & toolchain, I can re-use large amounts of code and compile into heavily optimized JavaScript (courtesy of [Google's Closure compiler](https://developers.google.com/closure/compiler/)). Since Clojure is running on the JVM, all App Engine parts will be built with Google's Java SDK for which I've already started creating nice wrappers to make the experience less verbose than in Java.

## Ring & Compojure

The Clojure community eschews monolithic frameworks (e.g. Rails, Spring) and instead seems to produce an unweildly number of tiny/small independent libraries which (usually) can be composed at will. IMHO this is largely made possible by (and a side effect of) working with a dynamically typed language with immutable data and powerful data abstractions. For web apps, however, most Clojure libraries are built on top of Ring and/or Compojure

## Leiningen

As the defacto build tool for Clojure, I'm using [Leiningen](http://leiningen.org) dozens of times each day. It's not just providing amazingly easy to use dependency management, but also is hugely extensible via [plugins](https://github.com/technomancy/leiningen/wiki/Plugins) (e.g. for unit testing, linting, refactoring, distribution etc.) and truly is at the heart of my dev process.

## Speclj

I have to admit that I'm not belonging to the TDD hardcore camp (anymore), largely because working in the REPL allows me to build & examine code in a far more detailed & interactive manner than writing unit tests. However, there're situations where I do appreciate the piece of mind of an extensive suite of test cases and for that I started using [Speclj](http://speclj.com), which has a nice auto runner (a lein plugin) and supports ClojureScript too.

## Blender

My relationship to Blender had been similar to the one I had with Emacs, largely thanks to its non-conforming UI. However, having jumped over my own laziness and taking on the learning curve, it's now my tool of choice for creating quick 3d mockups & sketches.

## LuxRender

[LuxRender](http://luxrender.net) is my favourite open source renderer and I'm currently investigating if/how I can utilize it within the proposed project setup.

## Meshlab

"Everytime I hear the word 'mesh' I reach for my browning", actually... [Meshlab](http://meshlab.sf.net) - it's a great 3D mesh viewer/processor with support for many file formats. Essential for my line of work...

