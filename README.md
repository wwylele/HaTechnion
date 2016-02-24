I don't like the app offered by my university, and want to get rid of those Hebrew course name. 

So I create my own one, to learn more about android development, and to make life easier.

I gitignore-ed the file `/app/build.gradle` You can build it from `/app/build.gradle.example` with some secrets filled:

 - `xxx_api_url` is the url of the (undocumented) API of the grade system.
 - (Optional) `xxx_mstranslator_client` and `xxx_mstranslator_secret` are for microsoft translator to translate the course name.
