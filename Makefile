test:
	env $(cat .env | xargs) >/dev/null && scala-cli test .
test-native:
	env $(cat .env | xargs) >/dev/null && scala-cli test . --native
reset:
	rm -rf .bsp .metals .scala-build .vscode