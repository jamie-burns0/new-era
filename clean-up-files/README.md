# Clean up files application

Since around 2009, I've been backing up images and documents from various phones and laptops to a 4TB WD storage device attached to my network. This has resulted in many duplicate files across the filesystem.

Given the path to a folder and types of files to act on, this Java application will clean up the files in the folder tree, such that,
- files having no content - zero-length files: are all removed
- files having identical content: we keep one file and remove the others
- files having unique content but sharing a filename: we keep one as is and rename the others

### Data oriented programming

After watching a Devoxx talk, Data Oriented Programming in Java 21, by Nicolai Parlog, I coerced part of the solution into a Data Oriented Programming model

See,
- Data Oriented Programming in Java 21, Nicolai Parlog, Devoxx - see https://youtu.be/8FRU_aGY4mY
- Data Oriented Programming in Java, Brian Goetz, InfoQ - see https://www.infoq.com/articles/data-oriented-programming-java

__TODO__
- was not able to apply DOP to GroupFiles as sealed...permits prevents Mockito from mocking - it can't create a mock because the mock class it instantiates is not in the permits list
- classes modelling data are only data, so, there is no value in unit testing them
- trying to apply DOP to GroupFiles and being blocked by Mockito - probably a smell that GroupFiles are not classes modelling data and only data



# Accessing network drive mapped on host
- https://dev.to/rpalo/mounting-network-drives-into-windows-subsystem-linux-3ef7

```
net use x: \\10.0.0.5\jamie /USER:cleanfiles secret
```

```
sudo mkdir /mnt/x
sudo mount -t drvfs x: /mnt/x

sudo umount /mnt/x
```


# A look at performance

Having settled on and implemented a strategy of breaking the work into two phases, it was time to look at performance.

In iteration 1, I had been performing expensive grouping and regrouping of files - converting maps of lists into other maps of lists. This was replaced by successively partitioning my list of files using `Collectors.partitioningBy(...)`. On each partition, one list was turned into actions, while the other, now smaller list, was repartitioned on a new criteria. So now, we are just slicing the list in two and working on the slice that remains.

### Files and Path

The storage device is wired into my home network but is accessed over the home wireless network. Accessing a lot of files over the wireless network is going to be slow. Any performance gain here is going to help. I was happy with my code to walk the folder tree. However, I remembered the `Files` class and took some time to dig around.

I only wanted to know three things about a file -  name, path and size. `Files.walk(...)`, says, "Returns a `Stream` that is __lazily__ populated with `Path`...". I had been walking the tree with `File::listFiles`. Maybe more was being returned for each `File` object than would be returned by each `Path` object - the methods on `File` tell me more about the file than the methods on `Path`. Reinforcing this idea, I found `Files.readAttributes(Path, attribute...)` which would let me get only the size attribute of the file represented by the `Path` object.

These changes significantly improved the time to capture the data I needed for each file. A folder with around 2000 images is now read in half the time.

### GraalVM for Java

GraalVM for Java 23 was released this week. For the third performance improvement, I built and ran the application as a GraalVM for Java Native Image. It was pretty straight forward out-of-the-box,
- install GraalVM for Java 23 - see https://www.graalvm.org/downloads/
- install the Native Image prerequisites - see https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites
- update to latest Gradle - v8.10.1 in this case
- follow the GraalVM Gradle plugin getting started instructions - see https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html

Getting my file names, paths and sizes was a little faster - any improvement here is welcome. Creating actions for each file was completed in half the time.

### VisualVM

Finally, although this was done first, and it's not a performance improvement, I installed VisualVM to monitor the heap. I went for VisualVM because it was free. However, it did cost me some time to get it working. For my development environments I run Visual Studio Code on Kali Linux instances running on WSL2. I gave the VisualVM extension a go first, but getting the visual part working in my environment was abandoned. Instead, I installed VisualVM on my Windows host, running that with,

```
C:\dev\tools\visualvm_219\bin\visualvm.exe --jdkhome "C:\dev\tools\jdk-22.0.2"
```

On my Kali Linux guest, I built the application jar with Gradle, then ran the jar with,

```
/opt/java/jdk-23/bin/java -Dcom.sun.management.jmxremote=true -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.port=9991 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.registry.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.net.preferIPv4Stack=true -jar ./app/build/libs/app.jar me.jamieburns.CleanApplication
```

In the VisualVM GUI, I added a JMX connection under Local connecting to `localhost:9991`.

Monitoring against a folder tree with around 12000 files, the heap usage graph showed a healthy sawtooth pattern oscillating between 5MB and 50MB of heap space.

- prepare a list of actions
- execute the list of actions
