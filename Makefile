.PHONY: test
test:
	export `cat .env | xargs` && scala-cli test .
reset:
	rm -rf .bsp .metals .scala-build .vscode