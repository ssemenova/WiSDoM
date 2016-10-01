
# -g babelify -g uglifyify

.phony: all clean

all: assets/bundle.min.js #assets/projecks.css

clean:
	rm assets/bundle.min.js

#assets/%.css: assets/%.scss
#	sass -t compressed $< $@

assets/bundle.min.js: index.js package.json *.vue vue/*
#	./node_modules/browserify/bin/cmd.js -t vueify -t babelify -t uglifyify index.js \#
	./node_modules/browserify/bin/cmd.js -t vueify index.js \
	> assets/bundle.min.js

assets:
	mkdir assets
