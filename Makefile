.PHONY: test
test:
	export $(cat .env | xargs) >/dev/null && scala-cli test .
reset:
	rm -rf .bsp .metals .scala-build .vscode