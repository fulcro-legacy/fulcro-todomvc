# Untangled • [TodoMVC](http://todomvc.com)

A client-only standard todomvc is on the `client-only` branch of this
git repository. This branch contains a full-stack implementation of 
the TodoMVC application, with the following extensions:

- Lists are persisted on the server (currently selected by adding a ?list=X param to URL)
- Internationalization (coming soon)
- Support VCR Viewer (coming soon)

## Configure it:

Copy `resources/config/defaults.edn` to `/usr/local/etc/todomvc.edn`.

```
{ 
 :datomic {
   :dbs {
     :todo {:url "datomic:mem://todo" 
            :schema "todomvc.migrations" 
            :auto-migrate true 
            :auto-drop true}}} 
 :port 3000} 
```

You can configure the datomic database URL to a file-based one if you 
wish, in which case you might want to turn off `auto-drop` so
your lists persist over server restarts. Leave
`auto-migrate` on, since that just makes sure the schema is up-to-date.

## Running it:

Start figwheel (the JVM options tell figwheel which builds to run):

```
JVM_OPTS="-Ddev -Dtest" lein run -m clojure.main script/figwheel.clj
```

which should start auto-building the cljs source and show a browser REPL.

Start a CLJ REPL:

```
lein repl
```

At the REPL, start the server:

```
(go)
```

Navigate to: [http://localhost:3000/dev.html?list=MyList](http://localhost:3000/dev.html?list=MyList)

The list URL parameter allows you to select which persisted TODO list on the server
you want.

Changes to the source should re-render without a browser reload. 

Changes to server code can be put into effect at the REPL (will wipe database) with:

```
(reset)
```

## Running the tests

The figwheel build above will start the client test build. Simply open
(any number of) browsers on 
[http://localhost:3000/test.html](http://localhost:3000/test.html)

## Internationalization

The i18n support has been integrated into this example. A tutorial video will be out soon. Note
that there are a few bugs with the implementation (e.g. plural support isn't translating
at the moment, and the string extraction is a bit fiddly). These are all easy to fix, just
not the priority yet.

## Viewing a Support Request

You'll need to start the build that creates the support viewer, so use this
to start figwheel:

```
JVM_OPTS="-Ddev -Dtest -Dsupport" lein run -m clojure.main script/figwheel.clj
```

Start the server as before.

Now load dev.html as before, do some operations, and create a support request. The 
server will persist it, and give a support ID in the server logs. Simply open
the support viewer with this URL:

[http://localhost:3000/support.html?id=ID_OF_REQUEST](http://localhost:3000/support.html?id=ID_OF_REQUEST)

You should now be able to step back in time through 100 steps of history. Each support request 
is store separately. The implementation on this server does not persist them
to Datomic, so they only exist for the lifetime of the server. NOTE: The timestamps
require Om alpha31+, which was not out at the time of this writing.

## Credit

Created by [NAVIS](http://www.thenavisway.com)
