# Library releases

Anyone who's been trying to follow my process will have noticed that there haven't been any code check ins into my [DevArt project repository](https://github.com/postspectacular/devart-template) on GitHub so far. The reason for this is that I've spent the past month(s) busily preparing a bunch of standalone libraries which will provide the core functionality for what I've set out to achieve with this project and therefore needed to be dealt with first. Last night I've finally reached a point where some of these libraries are stable enough to be let loose onto the wider public and have baked some initial releases:

### thi.ng/geom

- [Announcement email](https://groups.google.com/d/msg/clojure/hWqPXn1_pK4/uHZnAPxQkIkJ)
- [GitHub project](https://github.com/thi-ng/geom/blob/master/src/index.org)

The GH page gives a good overview of this project's objectives & current state of affairs...

### thi.ng/luxor

- [Announcement email](https://groups.google.com/d/msg/clojure/MEAmgLKcqbc/HXbLfgQiRtEJ)
- [GitHub project](https://github.com/thi-ng/luxor)

Luxor is a Clojure based scene generator for [Luxrender](http://luxrender.net), an open source, physically based & unbiased rendering engine. As stated previously, I'm planning to use this to generate high quality 3D previews of the objects to be printed in the gallery. Once the daily voting process is over, a couple of Linux instances with Luxrender will be launched in the Google Compute Engine to render a 360 turntable animation of the chosen object overnight. The next morning, the resulting video is then downloaded onto the Raspberry PI in the gallery and displayed on the preview screen part of the exhibit.

With this essential ground work complete, I'm now in a position to start checking in more code related parts into the my DevArt repo - so watch this space...
