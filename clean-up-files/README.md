
# Clean up files app

Since around 2009, I've been backing up images and documents from various phones and laptops to a 2TB WD storage device attached to my network. This has resulted in many duplicate files across the filesystem.

Given the path to a folder and types of files to act on, this Java application will clean up the files in the folder,
- where we have duplicates across the filesystem, we keep one copy and remove the others
- tba

### Data oriented programming

After watching a Devoxx talk, Data Oriented Programming in Java 21, by Nicolai Parlog, I coerced part of the solution into a Data Oriented Programming model

See,
- Data Oriented Programming in Java 21, Nicolai Parlog, Devoxx, https://youtu.be/8FRU_aGY4mY
- Data Oriented Programming in Java, Brian Goetz, InfoQ, https://www.infoq.com/articles/data-oriented-programming-java

- was not able to apply DOP to GroupFiles as sealed...permits prevents Mockito from mocking - it can't create a mock because the mock class it instantiates is not in the permits list
- classes modelling data are only data, so, there is no value in unit testing them
- trying to apply DOP to GroupFiles and being blocked by Mockito, it probably a smell that GroupFiles are not classes modelling data and only data