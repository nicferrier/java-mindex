# Maven Indexing

The definition of eccentric might be persisting in attempting to use
Emacs to write Java code 20 years after it became a very bad idea.

Well, by that definition, I am eccentric.

But I persisted because I always thought there must be a better way
than all these highly integrated *magic* tools like IntelliJ or, God
forbid, Eclipse.

As time has gone on I feel like I've been proved right. Language
implementations these days take tooling much more seriously, some
compilers even having options to output abstract syntax trees along
side regular code.

But Java programmers haven't moved, they've got their magic tools and
they're happy I guess.

There are several things a Java programmer needs before they can
quickly write code:

* resolve class names that you type to imports and add the imports (and pom.xml) automatically
* discover classes, methods and exceptions through completion 

These are all just facilities that depend on indexing. That's why
IntelliJ is slow the first time it loads, or if it's cached index
disappears.

There's no reason why I can't have a similar indexer, I can just build
one with an interface that tools like Emacs will talk to. For example,
it could be a webserver.


## What does mindex do?

It simply walks all of the maven repository and reads in every Jar
file and reads the classes in the jar file and stores them in indexes.

What it does not do yet is to then read the class files from each jar
and further index each method... but that's coming.


## Other exciting things

[This](https://markusjais.com/file-system-events-with-java-7/) seems
to suggest I can have inotify-like file system events to tell me when
there's a change in the Maven repository and then we could regenerate
the index.
