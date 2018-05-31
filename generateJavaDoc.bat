SET link=https://docs.oracle.com/javase/8/docs/api/
SET package=ru.ifmo.rain.khalimov.implementor
SET data=java\info\kgeorgiy\java\advanced\implementor\

javadoc -d javadoc -link %link% -cp .\artifacts\JarImplementorTest.jar;.\lib\*;.\java\ -private %package% %data%Impler.java %data%JarImpler.java %data%ImplerException.java