## Instructions to import and execute chargingsession program

This program has been written and compiled in Java `v8.0`. So `v8.0` or higher is required to run this program.

1. Copy the zipfile to a location in your computer and unzip it.
2. Go to the directory where you have extracted the zip via command-line. `cd` into the `everon` directory.


#### For Unix based systems make use of *gradlew* script and for Windows make use of *gradlew.bat*. This guide is primarily for Unix based systems.


#### For generating Eclipse IDE specific files.

3. Check if the `gradlew` and `gradlew.bat` files are executable. If not make them executable. Command in linux and mac is: `sudo chmod +x gradlew`.

4. For Unix systems (linux, mac) run:

+ `./gradlew eclipse`

This will generate eclipse related files like .project that is required to import the project into the Eclipse IDE.

+ `./gradlew clEclEcl EclCl assemble`

This will generate the class-paths for Eclipse IDE.

#### Set things up

+ `./gradlew clean build`

This will run all the tests and also build the project. This also generates a runnable jar file inside the build/libs directory. This can be run as `java -jar everon-1.0.jar`.
The swagger UI would be available [here](http://localhost:8080/swagger-ui.html)

+ `./gradlew clean test`

This will re-run all the tests.

#### In order to import the project into Eclipse

1. Open the Eclipse IDE.

2. Click on `File` tab and click on `Import ...`

3. Click on `General` and expand it. Click on `Existing Projects into Workspace` and click `Next`.

4. Select radio button `Select Root Directory` and click on `Browse`.

5. Go to the directory where you have unzipped the project and into the everon folder where you have generated the Eclipse .project files.

6. Click `Open`. Eclipse will show the project that will be imported. Click `Finish`.

7. Sometimes it can happen that Eclipse does not refresh the project and dependencies fully. So right click on the project and click on `Refresh`.


#### In order to run the project from Eclipse

To run the program from Eclipse, open the Application.java file. Right click on the file and select `Run As -> Java Application` or `Run As -> Spring Boot App`
The application will be up and running on port `8080`.


#### In order to run the program from command-line (jar file)

The `./gradlew clean build` step generates an executable jar inside the everon/build/libs directory. This can be run as `java -jar everon-1.0.jar`.


#### In order to run the program from command-line manually

From the same directory where you ran the command for generating classpaths run command `./gradlew bootRun`.
This command will start and run the program as a Spring Boot App on port `8080`.


#### Java Docs

Java docs can be generated by running `./gradlew javadoc` command. It generates the javadocs inside `docs/javadocs` folder inside the
`build` directory inside the `everon` folder.

#### REST API Documentation

Once the application is up and running, you can view the documentation for the REST API by visiting [here](http://localhost:8080/swagger-ui.html).

The controller of interest is the charging-session-controller and the methods provided for accomplishing various tasks like starting, stopping, viewing, and
summarising the charging-sessions.

The model classes in use have also been documented.


#### Libraries used

Being a spring-boot application this program mainly uses `spring-boot-starter-web` for implementation and `spring-boot-test` and `spring-test` for testing.
Other libraries in use are the following:

1. `Guava` for the Table datastructure
2. `Swagger` for generating REST API documentation
3. `json-path`, `gson`, `hamcrest`, `mockito` and `junit-5` for testing purposes


#### Tests

There are 3 test classes:
1. Test for the controller layer
2. Test for the ChargeSessionsHandler
3. Integration test which try to mimic the real-world usage by actually starting the application, starting and stopping the charging-sessions and for viewing the charging-sessions summary.

Test coverage results are also generated inside the `jacocoHtml` folder inside the build directory. Currently the test coverage stands at `98%`.

#### Customising the fetch time for charging session summary

The actual requirements of this application stated the need to fetch the summary of all charging-sessions for the last minute. Later it was mentioned as an answer to a question that the preference is to get the summary for the last second.

Keeping in mind the initial and the later specified preferences the charging-session summary has been configured so that it can have a value between a `lower_limit` and an `upper_limit`. Currently these values have been configured as `1` second and `60` seconds in the `application.properties` file.

This argument is sent as a `request parameter`. If no parameter is mentioned then a default value of `1` second will be used to fetch the summary.


#### Rationale for the design

The main points to consider were:

1. The insertions and retrievals both should be `O(log N)`
2. The fetching of summary records should be `O(log N)`

The following points also stood out:

4. `PUT` operation needs to identify the entity by `id`
5. Fetching the summary records needed the `timestamps` as it was based on
   the number of seconds (ago).

One way of approaching this will be having:

1. a main structure for storing the `<timestamp, entity>` values and
2. a supporting index structure of the form `<id, timestamp>`.

The ids are `UUID` and the timestamps are `LocalDateTime`. Both of them being Comparables a data-structure that sorts them internally can be used. For example : a tree map.

Tree maps are backed by Red Black tree which gives efficient performance as N grows. Since the records are sorted internally according to their natural ordering the insertion and lookups will approach O(log N). In this specific approach it would mean that for a `PUT` operation there will be `2` lookups :

1. the `timestamp` lookup corresponding to the `id` from the `index`
2. the `entity` lookup corresponding to the `timestamp` from the main structure

This entails maintaining 2 datastuctures and locking them both at the same time for fetching and insertions as well except while generating summary in which case only the main datastructure will be used.

The `PUT` is the only operation that modifies a record provided that it is `IN_PROGRESS` status. This means that the index need to have only ids of the charging sessions that have a status of `IN_PROGRESS`.

Since the main structure has all the sessions irrespective of whether they are `IN_PROGRESS` or `FINISHED` the index structure will be
solely used to facilitate the `PUT` operation. This also means that clean-ups may have to be done on the index structure to cleanse it from the older entities that are in the `FINISHED` state hence having to lock them both again for these removals.


##### Simple design using a Table

In order to make things a bit simple the `Table` structure was used. This is a single data-structure that provides <row, col, cell>  structure like an excel sheet. A `treemap` based table is used. The result would be:

1. Since the `timestamps` are being used as rows this would allow for `O(log N)` insertions for `POST`.
2. Since the `timestamps` are being used as rows this would allow for `O(log N)` fetches for summaries.
3. Since the `id` is being used as columns this would allow for `O(log N)` fetches for `PUT`.
4. The `GET` operation would also be `O(N)`.

This means that all the requirements as stated in the assignment will be met.


#### Concurrency

There are operations to read and write. It has been assumed that the application tilts slightly more towards write heaviness. Read Write locks have been used to improve concurrency rather than just synchronizing the entire methods which do the reads and writes. The `chargingSessionsTest` tries to mimic concurrent writes and updates by using `IntStream.parallel`.


#### Things that can be improved

Since there is a single structure for storing entities at some point it has to be purged. In order to do that it is essential to know for how long does the entries have to be stored. The purges would be efficient as it is just a question of locking the structure at the time of purge. This can be a periodic job.