LEIN_RUN = rlwrap lein run -m clojure.main ./script/figwheel.clj

dev:
	JVM_OPTS="-server -Ddev -Dsupport -Dtest" ${LEIN_RUN}

tests:
	npm install
	lein doo chrome automated-tests once

production:
	lein cljsbuild once production

help:
	@ make -rpn | sed -n -e '/^$$/ { n ; /^[^ ]*:/p; }' | sort | egrep --color '^[^ ]*:'

.PHONY: dev tutorial tests help
