# More LCD related food for thought

Reading up about (and remembering) I2C alternatives to better utilize the precious number of GPIO pins of the Raspberry PI lead me back to investigate [using shift registers](http://playground.arduino.cc/Code/LCD3wires) and also doing more research about potentially using ARM assembly to [drive the GPIO directly](http://interrupthandler.com/raspberry-pi-os-dev-notes-lesson-1/). Since the ARM is a RISC processor and its assembly language hasn't changed that much since its [Archimedes](http://en.wikipedia.org/wiki/Archimedes_%28computer%29) times in the 80/90s, it's actually still quite similar (and IMO far less complex than x86) to the [6502](http://en.wikipedia.org/wiki/6502) dialect I've been growing up with. So familiar territory. Being able to talk to the pins directly rather than controlling them via Linux' `/dev/mem` & `/sys/class/gpio` sockets (as done by the [Rpi.GPIO Python package](http://sourceforge.net/p/raspberry-gpio-python/wiki/Home/)) is pretty trivial & could maybe help with achieving scrolling text on more displays with less RPi's. Using [WirinPi](https://github.com/WiringPI/WiringPI) in C is also probably good enough and would make [HTTP requests](http://coding.debuntu.org/c-linux-socket-programming-tcp-simple-http-client) to load content for the LCDs easier than in ASM. The [bare metal tutorial](http://wiki.osdev.org/ARM_RaspberryPI_Tutorial_C) on OSDev includes some useful references too. Just collecting pointers here. Until my missing components arrive I will go back & continue fleshing out the actual design process/approach over the next few days, something far more important, really! But at least I have some form of TODO list for this labeling aspect of the exhibit now too...