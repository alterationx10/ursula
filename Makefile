.PHONY: test publish-local bundle-local publish-central reset
test:
	export `cat .env | xargs` && scala-cli test .
publish-local:
	scala-cli --power publish local .
bundle-local:
	mkdir -p ursula-bundle/dev/alteration/x10/ursula_3/${URSULA_VERSION}
	scala-cli --power publish local . --gpg-key ${PGP_KEY_ID} --gpg-option --pinentry-mode --gpg-option loopback --gpg-option --passphrase --gpg-option ${PGP_PASSPHRASE}
	for DIR in srcs docs poms jars; do \
		cp  ~/.ivy2/local/dev.alteration.x10/ursula_3/${URSULA_VERSION}/$$DIR/* ursula-bundle/dev/alteration/x10/ursula_3/${URSULA_VERSION}; \
	done
	cd ursula-bundle/dev/alteration/x10/ursula_3/${URSULA_VERSION} && \
		rename 's/ursula_3/ursula_3-${URSULA_VERSION}/' *
	cd ursula-bundle && \
		zip -r ursula-${URSULA_VERSION}.zip .
publish-central: bundle-local
	curl \
            --request POST \
            --header 'Authorization: Bearer ${CENTRAL_TOKEN}' \
            --form bundle=@ursula-bundle/ursula-${URSULA_VERSION}.zip \
            https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC
	rm -rf ursula-bundle
reset:
	rm -rf .bsp .metals .scala-build .vscode .idea project target
