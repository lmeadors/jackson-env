jackson-env
===

This is a tool for managing multiple environments using json.

There are 3 ways to use this:

1. Stand-alone: Read in a json file, merge the values for one environment into a properties object.
2. With guice: Create a module and let it bind the properties for use with `@Named` annotations.
3. With spring: Read in a json file, merge the values for one environment into the system properties.

Here's a sample json configuration file:

	{
		// LOOK! A COMMENT!
		"globalSettings": {
			"some.key": "some.global.value",
			"another.key": "a.global.value"
		},
		"environmentSettings": {
			"PROD": {
				"some.key": "some.prod.value"
			},
			"TEST": {
			},
			"LOCAL": {
				"some.key": "some.local.value"
			}
		},
		"requiredSettings": [
			// none yet
		]
	}

So here we have defined three "environments": `PROD`, `TEST`, and `LOCAL`.

In all environments, there will be a value named `some.key`. It's value will vary, based on which environment is loaded.

For example, if we create a file at the root of a classpath and name it "environment.json", then run this code:

	final Map<String, String> properties = EnvironmentSettings
		.load("environment.json")
		.merge("LOCAL");

The `properties` object will look something like this:

- `some.key = some.local.value`
- `another.key = a.global.value`

If we change that "LOCAL" string in the example code to "TEST", the `properties` object will look something like this:

- `some.key = some.global.value`
- `another.key = a.global.value`

In addition to this, if we run that code and add this to the command line:

- `-Dsome.key=some.temporary.value`

Then the the `properties` object will look something like this:

- `some.key = some.temporary.value`
- `another.key = a.global.value`

So, the rules are:

- system properties trump everything
- environment values from the file come next
- global values come last

Simple.

Maven
---

To add this to your project, add this:

	<dependencies>
		<dependency>
			<groupId>com.elmsoftware</groupId>
			<artifactId>jackson-env</artifactId>
			<version>1.5</version>
		</dependency>
	</dependencies>

It's not in the central repositories, so you also have to add this:

	<repositories>
		<repository>
			<id>elm-software-releases</id>
			<url>https://github.com/lmeadors/maven-repo/raw/master/releases</url>
		</repository>
	</repositories>

If you want to use the latest snapshot, there is a snapshot repository, too:

	<repositories>
		<repository>
			<id>elm-software-releases</id>
			<url>https://github.com/lmeadors/maven-repo/raw/master/releases</url>
		</repository>
		<repository>
			<id>elm-software-snapshots</id>
			<url>https://github.com/lmeadors/maven-repo/raw/master/snapshots</url>
		</repository>
	</repositories>

Using jackson-env with guice
---

This is pretty simple - a module has been created for you. 

You have two options. You can load the `EnvironmentSettingsModule` as it is and get the properties bound for use in `@Named` annotations.

You can also extend the `EnvironmentSettingsModule` and implement this:

		protected  void configure(
			final Binder binder, 
			final Map<String, String> properties
		){
			// add your configuration here...
		}

What happens in the second case is that the properties in the merged json data are bound, then your configure method is called with the `Binder` instance passed in from Guice as well as the merged properties (in case you need them). 

In either case, you need to:

- name your environment file `environment.json` or set an environment variable named `environment.json` with the location of the json file on the classpath.
- set an environment variable named `environment` to the environment you want to merge down
- load the module

Just that simple:

	$ java -Denvironment=PROD ...

Using jackson-env with spring
---

The spring integration feels hokey to me still - if you have a better way, please for the love of all that is good, fork and send me a patch. Maybe JNDI?

You load up an instance of the `EnvironmentSettingsResolver` class, and it in turn merges one set of properties and tucks them away into the system properties.

Just make sure that it loads before anything that needs it.

