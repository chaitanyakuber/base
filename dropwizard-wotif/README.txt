Build
=====
    mvn clean install

Release
=======
    mvn -B release:prepare     # Remove "-B" if you want to override versioning defaults
    git push --tags
    mvn release:perform
    git push
