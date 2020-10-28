validate:
	@mvn validate

dependencies:
	@mvn install -DskipTests

build: dependencies
	@mvn hpi:hpi

run:
	@mvn hpi:run

clean:
	@rm -rf .mvn target

all: clean build