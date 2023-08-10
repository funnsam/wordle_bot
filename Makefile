test: build run

run:
	java -jar target/wordle_bot-1.0-SNAPSHOT-jar-with-dependencies.jar $(shell cat api_key.txt)

build:
	mvn package assembly:single
