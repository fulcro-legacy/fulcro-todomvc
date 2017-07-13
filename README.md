# Fulcro • [TodoMVC](http://todomvc.com)

A client-only standard todomvc is on the `client-only` branch of this
git repository. The `master` branch is the released version, and we
use Git Flow, so the `develop` branch is active development.

Both `develop` and `master` contain a full-stack implementation of 
the TodoMVC application, with the following extensions:

- Lists are persisted on the server (currently selected by adding a ?list=X param to URL)
- Internationalization 
- Support VCR Viewer

## Configure it:

WARNING: The server will not start without this step!

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

### Figwheel/Clojurescript Build

Start figwheel (the JVM options tell figwheel which builds to run):

```
JVM_OPTS="-Ddev -Dtest" lein run -m clojure.main script/figwheel.clj
```

which should start auto-building the cljs source and show a browser REPL.

You can do this in IntelliJ using a regular Clojure Main REPL that runs
`script/figwheel.clj` (Parameters field). The `-Ddev` and `-Dtest` options can go in the JVM 
arguments field.

Our internal figwheel support uses Java system properties to select the
builds you want to start (so you can create multiple run profiles for
different tasks that target only specific builds). The supported build IDs
are whatever builds are defined in the project file (it extracts build 
configurations from there). So, including `-Dtest` in the JVM arguments
will include the build of tests.

### Server

Start a CLJ REPL (e.g. command line or IntelliJ):

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
[http://localhost:3000/fulcro-spec-client-tests.html](http://localhost:3000/fulcro-spec-client-tests.html)

## Internationalization

The i18n support has been integrated into this example. A tutorial video will be out soon. 

## Viewing a Support Request

You'll need to start the build that creates the support viewer, so use this
to start figwheel (restart figwheel and add `-Dsupport` to your JVM options):

```
JVM_OPTS="-Ddev -Dtest -Dsupport" lein run -m clojure.main script/figwheel.clj
```

Start the server as before.

Now load dev.html as before, do some operations, and create a support request
using the Help button at the top of the screen. The server will persist it 
(in memory only), and give a support ID in the server logs. Simply open the 
support viewer with this URL (embedding the correct ID from the server log):

[http://localhost:3000/support.html?id=ID_OF_REQUEST](http://localhost:3000/support.html?id=ID_OF_REQUEST)

You should now be able to step back in time through up to 100 steps of history. Each support request 
is stored separately. The implementation on this server does not persist them
to Datomic, so they only exist for the lifetime of the server. 

## Credit

Currently maintained by Fulcrologic.

Based upon work Created by NAVIS.

