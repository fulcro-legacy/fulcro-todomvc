test:
	npm install
	lein do clean, doo chrome automated-tests once
	lein test-refresh :run-once

i18n-extract:
	@rm -rf target/
	@echo "Building Clojurescript"
	@lein cljsbuild once i18n
	@echo "Running extraction"
	@lein run -m clojure.main -e "(require 'fulcro.gettext) (fulcro.gettext/extract-strings {:js-path \"i18n/i18n.js\" :po \"i18n\"})"
	@echo "Done."
	@echo "Prior translations were merged in all of your exising locales (unless you saw errors)."
	@echo 
	@echo "If you have a new locale you'd like to support, generate a new PO file for it using:"
	@echo 
	@echo "msginit --no-translator -l LOCALE --no-wrap -o i18n/LOCALE.po -i i18n/messages.pot"
	@echo 
	@echo "Now give all of your PO files in i18n to a translator (or use PoEdit Pro to get close). Then put the"
	@echo "updated PO files back in i18n and run 'make i18n-generate"
	@echo 
	@echo "See the GNU Gettext manual for more instructions on working with POT and PO files."

i18n-generate:
	@echo "Analyzing PO files and generating CLJC..."
	@lein run -m clojure.main -e "(require 'fulcro.gettext) (fulcro.gettext/deploy-translations {:ns \"fulcro-template.locales\" :src \"src/main\" :po \"i18n\"})"
	@echo "Done."

.PHONY: i18n-extract i18n-generate test
