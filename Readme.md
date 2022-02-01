# DiskStalker

`DiskStalker` is a small application for monitoring contents of selected folders. 
It provides the ability to set notifications about three types of statistics:
* total folder size
* number of files inside folder
* size of largest file

App also shows stats about type of files inside selected folder and presents list of largest files.

DiskStalker is using polling method for tracking changes in directory. Better approach is to use [WatchService API](https://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html) but it does not work well on Windows (it blocks the files, so you are not able to delete files from observed folder, see [StackOverflow](https://stackoverflow.com/questions/56847367/can-you-prevent-watchservice-from-locking-files-on-windows)).

Presentation: [YouTube](https://youtu.be/nOFRaL8o1tU)
## Screenshots

| ![1](./img/1.png) |
|:--:|
| *Main View* |
___

| ![2](./img/2.png) |
|:--:|
| *Files type view* |
___

| ![3](./img/3.png) |
|:--:|
| *Largest files view* |
___

| ![4](./img/4.png) |
|:--:|
| *Total folder limit set* |
___

| ![5](./img/5.png) |
|:--:|
| *Total folder limit exceeded* |

## Usage
Visit [Releases](https://github.com/rivit98/DiskStalker/releases) and download .jar. Then run:
```
java -jar nameOfTheJarFile.jar
```

## Importing
```
git clone https://github.com/rivit98/DiskStalker.git
```
and import as Gradle project


## Used technologies and libraries
* Java 15
* JavaFX
* RxJava3
* Spring (for dependency injection)
* SQLite
* Apache commons IO
* Apache Tika
* Mockito
* JUnit5


## Known issues and possible improvements
* something is wrong with TypeRecognizer and 'unknown' file types
* removing huge folders (~30k files) taking quite long time
* usage of observable buffering might be significant improve of removing times

## Authors

<table align="center">
<tr>
<th align="center">
    <img src="https://avatars.githubusercontent.com/u/70373402?v=4" width="100"><br> 
    <a href="https://github.com/wnekus">Kamil Wnęk</a>
</th>

<th align="center">
    <img src="https://avatars.githubusercontent.com/u/44415389?v=4" width="100"><br> 
    <a href="https://github.com/kuczi55">Kamil Koczera</a>
</th>

<th align="center">
    <img src="https://avatars.githubusercontent.com/u/19514368?v=4" width="100"><br> 
    <a href="https://github.com/rivit98">Albert Gierlach</a>
</th>
</tr>
</table>
