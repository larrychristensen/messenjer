# messenjer

A simple messenging app to demonstrate building an app with Clojure, ClojureScript, Kafka, Re-Frame, Pedestal, and Component.

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To start the back-end server you will need to open a REPL either from the command line:

    lein repl
    
Or if you are using Emacs with [Cider](https://cider.readthedocs.io/en/latest/) you can run the command to start the Cider REPL:

    C-c M-j
    
I haven't used [Cursive](https://cursive-ide.com/), but I hear it is really nice and I'm sure there's an easy way to start a REPL within it.
    
Once you have a REPL you can run this from within it:

    messenjer.server=> (def system-map (com.stuartsierra.component/start (messenjer.system/system :dev)))
    
To stop you will need to do this:

    messenjer.server=> (com.stuartsierra.component/stop system-map)
    
Within Emacs you should be able to save your file (C-x C-s) and reload it into the REPL (C-c C-w) to get your server-side changes to take effect. Your client-side changes will take effect immediately when you change a CLJS or CLJC file.

## License

Copyright © 2018 Larry Christensen

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
